package com.marginallyclever.makelangelo;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ProgressMonitor;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.core.TransformedImage;
import com.marginallyclever.core.log.Log;
import com.marginallyclever.core.node.NodePanel;
import com.marginallyclever.core.turtle.Turtle;
import com.marginallyclever.makelangelo.nodes.ImageConverter;
import com.marginallyclever.makelangelo.nodes.LoadAndSaveFile;
import com.marginallyclever.makelangelo.robot.Robot;
import com.marginallyclever.util.PreferencesHelper;

/**
 * {@code ImageLoad} uses an InputStream of data to create a {@code TransformedImage}.
 * @author Dan Royer
 *
 */
@Deprecated
public class LoadImageDialog implements LoadAndSaveFile {
	// threading
	protected ProgressMonitor pm;
	protected LoadImageSwingWorker threadWorker;
	
	public void setThreadWorker(LoadImageSwingWorker p) {
		threadWorker = p;
	}

	public void setProgressMonitor(ProgressMonitor p) {
		pm = p;
	}
	
	// The loaded image remains here after loading.
	private TransformedImage img;
	
	private ServiceLoader<ImageConverter> converters;
	private ImageConverter chosenConverter;
	
	private JPanel conversionPanel;
	/**
	 * A collection of panels from each {@code ImageConverter}.
	 */
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

	public LoadImageDialog() {
		converters = ServiceLoader.load(ImageConverter.class);

		imageConverterNames.clear();

		for (ImageConverter ici : converters) {
			imageConverterNames.add(ici.getName());
		}
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

	/**
	 * Create and run the dialog to convert images into Turtles.
	 * @param parent the component that owns this dialog
	 * @return true if the "OK" button was pressed.
	 */
	protected boolean runDialog(Component parent) {
		conversionPanel = new JPanel(new GridBagLayout());

		String[] array = (String[]) imageConverterNames.toArray(new String[0]);
		JComboBox<String> styleNames = new JComboBox<String>(array);

		cards = new JPanel(new CardLayout());
		cards.setPreferredSize(new Dimension(450, 300));
		for (ImageConverter ici : converters) {
			cards.add(new NodePanel(ici), ici.getName());
		}

		GridBagConstraints c = new GridBagConstraints();
		int y = 0;
		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = y;
		c.ipadx = 5;
		conversionPanel.add(new JLabel(Translator.get("ConversionStyle")), c);
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 3;
		c.gridx = 1;
		c.ipadx = 0;
		conversionPanel.add(styleNames, c);

		y++;
		c.anchor = GridBagConstraints.NORTH;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 4;
		c.gridx = 0;
		c.gridy = y;
		c.insets = new Insets(10, 0, 0, 0);
		cards.setPreferredSize(new Dimension(449, 325));
		// previewPane.setBorder(BorderFactory.createLineBorder(new Color(255,0,0)));
		conversionPanel.add(cards, c);
		styleNames.setSelectedIndex(getPreferredDrawStyle());

		styleNames.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				CardLayout cl = (CardLayout) (cards.getLayout());
				cl.show(cards, (String) e.getItem());

				changeConverter(styleNames.getSelectedIndex());
			}
		});

		int result = JOptionPane.showConfirmDialog(parent, conversionPanel, Translator.get("ConversionOptions"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			setPreferredDrawStyle(styleNames.getSelectedIndex());
			return true;
		}

		if (threadWorker != null) {
			threadWorker.cancel(true);
		}
		stopSwingWorker();

		return false;
	}

	private void changeConverter(int index) {
		ImageConverter requestedConverter = getConverter(index);

		// Log.message("Changing converter");
		stopSwingWorker();
		chosenConverter = requestedConverter;
		Log.message("Converter=" + chosenConverter.getName());

		startSwingWorker();
	}

	@Deprecated
	public void restart() {
		// Log.message("Restarting");
		stopSwingWorker();
		startSwingWorker();
	}

	private ImageConverter getConverter(int arg0) throws IndexOutOfBoundsException {
		int i = 0;
		for( ImageConverter chosenConverter : converters ) {
			if(i == arg0) return chosenConverter;
			i++;
		}

		throw new IndexOutOfBoundsException();
	}

	/**
	 * Load and convert the image in the chosen style
	 * @return false if loading cancelled or failed.
	 */
	@Override
	@Deprecated
	public boolean load(InputStream in) {
		try {
			img = new TransformedImage(ImageIO.read(in));
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}

		if (!GraphicsEnvironment.isHeadless()) {
			//runDialog(robot.getControlPanel());
		}

		return true;
	}
	/*
	 * // adjust image to fill the paper
	 * 
	 * @Deprecated public void scaleToFillPaper(TransformedImage img,Turtle turtle)
	 * { double width = turtle.getMarginWidth(); double height =
	 * turtle.getMarginHeight();
	 * 
	 * float f; if( width > height ) { f = (float)( width /
	 * (double)img.getSourceImage().getWidth() ); } else { f = (float)( height /
	 * (double)img.getSourceImage().getHeight() ); } img.setScale(f,-f); }
	 * 
	 * @Deprecated public void scaleToFitPaper(TransformedImage img,Turtle turtle) {
	 * double width = turtle.getMarginWidth(); double height =
	 * turtle.getMarginHeight();
	 * 
	 * float f; if( width < height ) { f = (float)( width /
	 * (double)img.getSourceImage().getWidth() ); } else { f = (float)( height /
	 * (double)img.getSourceImage().getHeight() ); } img.setScale(f,-f); }
	 */

	protected void stopSwingWorker() {
		if (chosenConverter != null) {
			chosenConverter.stopIterating();
		}
		if (threadWorker != null) {
			Log.message("Stopping swingWorker");
			if (threadWorker.cancel(true)) {
				Log.message("stopped OK");
			} else {
				Log.message("stop FAILED");
			}
		}
	}

	protected void startSwingWorker() {
		Log.message("Starting thread 1");

		pm = new ProgressMonitor(null, Translator.get("Converting"), "", 0, 100);
		pm.setProgress(0);
		pm.setMillisToPopup(0);

		//chosenConverter.setProgressMonitor(pm);
		chosenConverter.inputImage.setValue(img);

		threadWorker = new LoadImageSwingWorker(chosenConverter, pm);

		threadWorker.execute();
	}

	private void setPreferredDrawStyle(int style) {
		Preferences prefs = PreferencesHelper
				.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.LEGACY_MAKELANGELO_ROOT);
		prefs.putInt("Draw Style", style);
	}

	private int getPreferredDrawStyle() {
		Preferences prefs = PreferencesHelper
				.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.LEGACY_MAKELANGELO_ROOT);
		return prefs.getInt("Draw Style", 0);
	}

	public boolean canSave(String filename) {
		return false;
	}

	@Override
	public boolean save(OutputStream outputStream, ArrayList<Turtle> turtles, Robot robot) {
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
