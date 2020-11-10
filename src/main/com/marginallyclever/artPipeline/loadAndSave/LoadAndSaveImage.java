package com.marginallyclever.artPipeline.loadAndSave;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
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

import com.marginallyclever.artPipeline.ImageManipulator;
import com.marginallyclever.artPipeline.TransformedImage;
import com.marginallyclever.artPipeline.converters.ImageConverter;
import com.marginallyclever.artPipeline.converters.ImageConverterPanel;
import com.marginallyclever.convenience.Turtle;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;
import com.marginallyclever.makelangeloRobot.MakelangeloRobotPanel;
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
	private JPanel conversionPanel;
	private static JComboBox<String> styleNames;
	private static JComboBox<String> fillNames;
	private JPanel cards;
	
	// Set of image file extensions.
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
	private ArrayList<String> imageConverterNames = new ArrayList<String>();
	private String[] imageFillNames;
	
	private ArrayList<SwingWorker<Void, Void>> workerList = new ArrayList<SwingWorker<Void, Void>>();
	private int workerCount = 0;

	
	public LoadAndSaveImage() {
		converters = ServiceLoader.load(ImageConverter.class);
		
		imageConverterNames.clear();
		
		for( ImageConverter ici : converters ) {
			imageConverterNames.add(ici.getName());
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

	protected boolean chooseImageConversionOptions(Component parent) {
		conversionPanel = new JPanel(new GridBagLayout());
		
		String [] array = (String[]) imageConverterNames.toArray(new String[0]);
		styleNames = new JComboBox<String>(array);
		fillNames = new JComboBox<String>(imageFillNames);
		
		cards = new JPanel(new CardLayout());
		cards.setPreferredSize(new Dimension(450,300));
		for( ImageConverter ici : converters ) {
			cards.add(ici.getPanel().getPanel(),ici.getName());
		}

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
		conversionPanel.add(styleNames, c);

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
		conversionPanel.add(fillNames, c);
		c.gridy = y;

		y++;
		c.anchor=GridBagConstraints.NORTH;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth=4;
		c.gridx=0;
		c.gridy=y;
		c.insets = new Insets(10, 0, 0, 0);
		cards.setPreferredSize(new Dimension(449,325));
		//previewPane.setBorder(BorderFactory.createLineBorder(new Color(255,0,0)));
		conversionPanel.add(cards,c);

		int p;
		p=getPreferredFillStyle();
		if(p>=fillNames.getItemCount()) p=0;
		fillNames.setSelectedIndex(p);
		
		styleNames.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
			    CardLayout cl = (CardLayout)(cards.getLayout());
			    cl.show(cards, (String)e.getItem());

			    int index = styleNames.getSelectedIndex();
				ImageConverter requestedConverter = getConverter(index);
				if(requestedConverter == chosenConverter) return;
				changeConverter(index);
		    }
		});
		
		fillNames.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				changeConverter(styleNames.getSelectedIndex());
		    }
		});
		
		p=getPreferredDrawStyle();
		if(p>=styleNames.getItemCount()) p=0;
		styleNames.setSelectedIndex(p);
		
		int result = JOptionPane.showConfirmDialog(parent, conversionPanel, Translator.get("ConversionOptions"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			setPreferredDrawStyle(styleNames.getSelectedIndex());
			setPreferredFillStyle(fillNames.getSelectedIndex());
			return true;
		}
		
		if(swingWorker!=null) {
			swingWorker.cancel(true);
		}
		stopSwingWorker();

		return false;
	}
	
	private void changeConverter(int index) {
		ImageConverter requestedConverter = getConverter(index);

		//Log.message("Changing converter");
		stopSwingWorker();

		ImageConverterPanel.loadAndSaveImage = this;
		ImageConverter.loadAndSaveImage = this;
		
		chosenConverter = requestedConverter;
		Log.message("Converter="+chosenConverter.getName());
		
		switch(fillNames.getSelectedIndex()) {
			case 0:  scaleToFillPaper();  break;
			case 1:  scaleToFitPaper();  break;
			default: break;
		}
		
		startSwingWorker();
	}
	
	public void reconvert() {
		changeConverter(styleNames.getSelectedIndex());
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
		
		chooseImageConversionOptions(robot.getControlPanel());
		
		return true;
	}

	// adjust image to fill the paper
	public void scaleToFillPaper() {
		MakelangeloRobotSettings s = chosenRobot.getSettings();

		double width  = s.getMarginWidth();
		double height = s.getMarginHeight();

		float f;
		if( s.getPaperWidth() > s.getPaperHeight() ) {
			f = (float)( width / (double)img.getSourceImage().getWidth() );
		} else {
			f = (float)( height / (double)img.getSourceImage().getHeight() );
		}
		img.setScale(f,-f);
	}
	
	public void scaleToFitPaper() {
		MakelangeloRobotSettings s = chosenRobot.getSettings();
		
		double width  = s.getMarginWidth();
		double height = s.getMarginHeight();
		
		float f;
		if( s.getPaperWidth() < s.getPaperHeight() ) {
			f = (float)( width / (double)img.getSourceImage().getWidth() );
		} else {
			f = (float)( height / (double)img.getSourceImage().getHeight() );
		}
		img.setScale(f,-f);
	}
	
	protected void stopSwingWorker() {
		chosenRobot.setDecorator(null);
		if(chosenConverter!=null) {
			chosenConverter.stopIterating();
		}
		if(swingWorker!=null) {
			Log.message("Stopping swingWorker");
			if(swingWorker.cancel(true)) {
				Log.message("stopped OK");
			} else {
				Log.message("stop FAILED");
			}
		}
	}

	protected void startSwingWorker() {
		//Log.message("Starting swingWorker");

		machine = chosenRobot.getSettings();
		
		chosenConverter.setProgressMonitor(pm);
		chosenConverter.setRobot(chosenRobot);
		chosenConverter.setImage(img);
		chosenRobot.setDecorator(chosenConverter);
		
		swingWorker = new SwingWorker<Void, Void>() {
			@Override
			public Void doInBackground() {
				chosenConverter.setSwingWorker(swingWorker);

				pm = new ProgressMonitor(null, Translator.get("Converting"), "", 0, 100);
				pm.setProgress(0);
				pm.setMillisToPopup(0);
				
				while(!isCancelled() && chosenConverter.iterate()) {
					try {
						Thread.sleep(5);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
						Log.message("LoadAndSaveImage iterate interrupted.");
						break;
					}
				}
				chosenRobot.setDecorator(null);
				
				pm.setProgress(100);
				if(pm!=null) pm.close();
				
				if(isCancelled()==false) {
					Log.message("swingWorker finishing");
					chosenConverter.finish();
					Turtle t=chosenConverter.turtle;
					chosenRobot.setTurtle(t);
				}

				return null;
			}

			@Override
			public void done() {
				Log.message("swingWorker ended");
				workerList.remove(swingWorker);
				workerCount--;
				Log.message("removed worker.  "+workerCount+"/"+workerList.size()+" workers now.");
				swingWorker=null;
				MakelangeloRobotPanel panel = chosenRobot.getControlPanel();
				if(panel!=null) panel.updateButtonAccess();
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
						Log.message(Translator.get("Finished"));
					} else if (swingWorker.isCancelled() || pm.isCanceled()) {
						if (pm.isCanceled()) {
							swingWorker.cancel(true);
						}
						Log.message(Translator.get("Cancelled"));
					}
				}
			}
		});
		
		workerList.add(swingWorker);
		workerCount++;
		Log.message("added worker.  "+workerCount+"/"+workerList.size()+" workers now.");

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
