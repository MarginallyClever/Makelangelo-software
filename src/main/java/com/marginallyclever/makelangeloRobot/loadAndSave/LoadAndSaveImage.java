package com.marginallyclever.makelangeloRobot.loadAndSave;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.makelangelo.Log;
import com.marginallyclever.makelangeloRobot.TransformedImage;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.ImageManipulator;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;
import com.marginallyclever.makelangeloRobot.MakelangeloRobotPanel;
import com.marginallyclever.makelangeloRobot.converters.ImageConverter;
import com.marginallyclever.makelangeloRobot.converters.ImageConverterPanel;
import com.marginallyclever.makelangeloRobot.generators.Generator_Text;
import com.marginallyclever.makelangeloRobot.settings.MakelangeloRobotSettings;
import com.marginallyclever.util.PreferencesHelper;

/**
 * LoadImage uses an InputStream of data to create gcode. 
 * @author Dan Royer
 *
 */
public class LoadAndSaveImage extends ImageManipulator implements LoadAndSaveFileType {
	
	@SuppressWarnings("deprecation")
	private Preferences prefs = PreferencesHelper
			.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.LEGACY_MAKELANGELO_ROOT);

	private ServiceLoader<ImageConverter> converters;
	private ImageConverter chosenConverter;
	private TransformedImage img;
	private MakelangeloRobot chosenRobot;
	private boolean wasCancelled;
	JPanel conversionPanel;
	JComboBox<String> conversionStyleOptions;
	JComboBox<String> conversionFillOptions;
	private JPanel converterOptionsContainer;
	
	/**
	 * Set of image file extensions.
	 */
	private static final Set<String> IMAGE_FILE_EXTENSIONS;
	static {
		IMAGE_FILE_EXTENSIONS = new HashSet<>();
		IMAGE_FILE_EXTENSIONS.add("jpg");
		IMAGE_FILE_EXTENSIONS.add("jpeg");
		IMAGE_FILE_EXTENSIONS.add("png");
		IMAGE_FILE_EXTENSIONS.add("wbmp");
		IMAGE_FILE_EXTENSIONS.add("bmp");
		IMAGE_FILE_EXTENSIONS.add("gif");
	}
	private static FileNameExtensionFilter filter = new FileNameExtensionFilter(Translator.get("FileTypeImage"),
			IMAGE_FILE_EXTENSIONS.toArray(new String[IMAGE_FILE_EXTENSIONS.size()]));
	private String[] imageConverterNames;
	private String[] imageFillNames;
	
	
	public LoadAndSaveImage() {
		converters = ServiceLoader.load(ImageConverter.class);
		Iterator<ImageConverter> ici = converters.iterator();
		int i=0;
		while(ici.hasNext()) {
			ici.next();
			i++;
		}
				
		imageConverterNames = new String[i];

		i=0;
		ici = converters.iterator();
		while (ici.hasNext()) {
			ImageManipulator f = ici.next();
			imageConverterNames[i++] = f.getName();
		}
		
		imageFillNames = new String[2];
		imageFillNames[0] = Translator.get("ConvertImagePaperFill");
		imageFillNames[1] = Translator.get("ConvertImagePaperFit");
	}
	
	@Override
	public FileNameExtensionFilter getFileNameFilter() {
		return filter;
	}

	@Override
	public boolean canLoad(String filename) {
		final String filenameExtension = filename.substring(filename.lastIndexOf('.') + 1);
		return IMAGE_FILE_EXTENSIONS.contains(filenameExtension.toLowerCase());
	}

	protected boolean chooseImageConversionOptions(MakelangeloRobot robot) {
		final MakelangeloRobot robot2 = robot;
		
		conversionPanel = new JPanel(new GridBagLayout());
		
		conversionStyleOptions = new JComboBox<String>(imageConverterNames);
		conversionFillOptions = new JComboBox<String>(imageFillNames);
		
		converterOptionsContainer = new JPanel();
		converterOptionsContainer.setPreferredSize(new Dimension(450,300));
		
		conversionStyleOptions.setSelectedIndex(getPreferredDrawStyle());
		conversionFillOptions.setSelectedIndex(getPreferredFillStyle());
		
		conversionStyleOptions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeConverter(conversionStyleOptions,robot2);
		    }
		});
		conversionFillOptions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeConverter(conversionStyleOptions,robot2);
		    }
		});

		GridBagConstraints c = new GridBagConstraints();
		int y = 0;
		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = y;
		c.ipadx=5;
		conversionPanel.add(new JLabel(Translator.get("ConversionStyle")), c);
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 3;
		c.gridx = 1;
		c.ipadx=0;
		conversionPanel.add(conversionStyleOptions, c);

		y++;
		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = y;
		c.ipadx=5;
		conversionPanel.add(new JLabel(Translator.get("ConversionFill")), c);
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 3;
		c.gridx = 1;
		c.ipadx=0;
		conversionPanel.add(conversionFillOptions, c);
		c.gridy = y;

		y++;
		c.anchor=GridBagConstraints.NORTH;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth=4;
		c.gridx=0;
		c.gridy=y;
		c.insets = new Insets(10, 0, 0, 0);
		converterOptionsContainer.setPreferredSize(new Dimension(449,325));
		//previewPane.setBorder(BorderFactory.createLineBorder(new Color(255,0,0)));
		conversionPanel.add(converterOptionsContainer,c);
		
		changeConverter(conversionStyleOptions,robot);
		
		int result = JOptionPane.showConfirmDialog(robot.getControlPanel(), conversionPanel, Translator.get("ConversionOptions"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			wasCancelled=false;
			stopSwingWorker();
			setPreferredDrawStyle(conversionStyleOptions.getSelectedIndex());
			setPreferredFillStyle(conversionFillOptions.getSelectedIndex());
			robot.getSettings().saveConfig();

			return true;
		} else {
			wasCancelled=true;
			stopSwingWorker();
		}

		return false;
	}
	
	private void changeConverter(JComboBox<String> options,MakelangeloRobot robot) {
		//System.out.println("Changing converter");
		stopSwingWorker();

		chosenConverter = getConverter(options.getSelectedIndex());
		chosenConverter.setLoadAndSave(this);
		ImageConverterPanel imageConverterPanel = chosenConverter.getPanel();
		imageConverterPanel.loadAndSaveImage = this;
		converterOptionsContainer.removeAll();
		if(imageConverterPanel!=null) {
			Log.info("Converter="+chosenConverter.getName());
			//System.out.println("Adding panel");
			converterOptionsContainer.add(imageConverterPanel);
			converterOptionsContainer.invalidate();
		}
		
		converterOptionsContainer.getParent().validate();
		converterOptionsContainer.getParent().repaint();

		switch(conversionFillOptions.getSelectedIndex()) {
			case 0:  scaleToFillPaper();  break;
			case 1:  scaleToFitPaper();  break;
			default: break;
		}
		
		createSwingWorker();
	}
	
	public void reconvert() {
		changeConverter(conversionStyleOptions,chosenRobot);
	}
	
	private ImageConverter getConverter(int arg0) throws IndexOutOfBoundsException {
		Iterator<ImageConverter> ici = converters.iterator();
		int i=0;
		while(ici.hasNext()) {
			ImageConverter chosenConverter = ici.next();
			if(i==arg0) {
				return chosenConverter;
			}
			i++;
		}
		
		throw new IndexOutOfBoundsException();
	}
	


	/**
	 * Load and convert the image in the chosen style
	 * @return false if loading cancelled or failed.
	 */
	@Override
	public boolean load(InputStream in,MakelangeloRobot robot) {
		try {
			img = new TransformedImage( ImageIO.read(in) );
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}
		
		chosenRobot = robot;

		switch(getPreferredFillStyle()) {
			case 0:  scaleToFillPaper();  break;
			case 1:  scaleToFitPaper();  break;
			default: break;
		}
		
		pm = new ProgressMonitor(null, Translator.get("Converting"), "", 0, 100);
		pm.setProgress(0);
		pm.setMillisToPopup(0);
		
		chooseImageConversionOptions(robot);
		
		return true;
	}

	/**
	 * adjust image to fill the paper
	 */
	public void scaleToFillPaper() {
		MakelangeloRobotSettings s = chosenRobot.getSettings();

		float flip = s.isReverseForGlass() ? -1 : 1;
		double width  = s.getPaperWidth ()*s.getPaperMargin();
		double height = s.getPaperHeight()*s.getPaperMargin();

		float f;
		if( s.getPaperWidth() > s.getPaperHeight() ) {
			f = (float)( width / (float)img.getSourceImage().getWidth() );
		} else {
			f = (float)( height / (float)img.getSourceImage().getHeight() );
		}
		img.setScaleX(f);
		img.setScaleY(-f * flip);
	}

	
	public void scaleToFitPaper() {
		MakelangeloRobotSettings s = chosenRobot.getSettings();
		
		float flip   = s.isReverseForGlass() ? -1 : 1;
		double width  = s.getPaperWidth ()*s.getPaperMargin();
		double height = s.getPaperHeight()*s.getPaperMargin();
		
		float f;
		if( s.getPaperWidth() < s.getPaperHeight() ) {
			f = (float)( width / img.getSourceImage().getWidth() );
		} else {
			f = (float)( height / img.getSourceImage().getHeight() );
		}
		img.setScaleX(f);
		img.setScaleY(-f * flip);
	}
	

	protected void stopSwingWorker() {
		if(chosenConverter!=null) {
			chosenConverter.stopIterating();
		}
		if(swingWorker!=null) {
			//System.out.println("Stopping swingWorker");
			if(swingWorker.cancel(true)) {
				System.out.println("stopped OK");
			} else {
				System.out.println("stop FAILED");
			}
		}
	}

	protected void createSwingWorker() {
		//System.out.println("Starting swingWorker");

		chosenConverter.setProgressMonitor(pm);
		chosenConverter.setRobot(chosenRobot);
		chosenConverter.setImage(img);
		chosenRobot.setDecorator(chosenConverter);
		
		swingWorker = new SwingWorker<Void, Void>() {
			@Override
			public Void doInBackground() {
				chosenConverter.setSwingWorker(swingWorker);
				
				// where to save temp output file?
				// FIXME going through temp files every time may be thrashing SSDs.
				File tempFile;
				try {
					tempFile = File.createTempFile("gcode", ".ngc");
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
				tempFile.deleteOnExit();

				// read in image
				Log.info(Translator.get("Converting") + " " + tempFile.getName());
				// convert with style

				try (OutputStream fileOutputStream = new FileOutputStream(tempFile);
					Writer out = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)) {

					while(chosenConverter.iterate()) {
						Thread.sleep(5);
					}
					
					if(wasCancelled==false) {
						chosenConverter.finish(out);
						
						if (chosenRobot.getSettings().shouldSignName()) {
							// Sign name
							Generator_Text ymh = new Generator_Text();
							ymh.setRobot(chosenRobot);
							ymh.signName(out);
						}
					}
				} catch (Exception e) {
					Log.error(Translator.get("Failed") + e.getLocalizedMessage());
					chosenRobot.setDecorator(null);
				}

				// out closed when scope of try() ended.

				if(wasCancelled==false) {
					LoadAndSaveGCode loader = new LoadAndSaveGCode();
					try (final InputStream fileInputStream = new FileInputStream(tempFile)) {
						loader.load(fileInputStream,chosenRobot);
						MakelangeloRobotPanel panel = chosenRobot.getControlPanel();
						if(panel!=null) panel.updateButtonAccess();
					} catch(IOException e) {
						e.printStackTrace();
					}
				}
				
				chosenRobot.setDecorator(null);
				
				tempFile.delete();

				pm.setProgress(100);
				return null;
			}

			@Override
			public void done() {
				if(pm!=null) pm.close();
				//System.out.println("swingWorker ended");
				swingWorker=null;
			}
		};

		swingWorker.addPropertyChangeListener(new PropertyChangeListener() {
			// Invoked when task's progress property changes.
			public void propertyChange(PropertyChangeEvent evt) {
				if (Objects.equals("progress", evt.getPropertyName())) {
					int progress = (Integer) evt.getNewValue();
					pm.setProgress(progress);
					String message = String.format("%d%%.\n", progress);
					pm.setNote(message);
					if (swingWorker.isDone()) {
						Log.info(Translator.get("Finished"));
					} else if (swingWorker.isCancelled() || pm.isCanceled()) {
						if (pm.isCanceled()) {
							swingWorker.cancel(true);
						}
						Log.info(Translator.get("Cancelled"));
					}
				}
			}
		});

		swingWorker.execute();
	}
	
	private void setPreferredDrawStyle(int style) {
		prefs.putInt("Draw Style", style);
	}
	private void setPreferredFillStyle(int style) {
		prefs.putInt("Fill Style", style);
	}

	private int getPreferredDrawStyle() {
		return prefs.getInt("Draw Style", 0);
	}

	private int getPreferredFillStyle() {
		return prefs.getInt("Fill Style", 0);
	}
	
	public boolean canSave(String filename) {
		return false;
	}
	
	public boolean save(OutputStream outputStream,MakelangeloRobot robot) {
		return false;
	}

	@Override
	public boolean canLoad() {
		return true;
	}

	@Override
	public boolean canSave() {
		return false;
	}
}
