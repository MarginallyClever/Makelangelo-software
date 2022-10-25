package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.preview.PreviewListener;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.FileInputStream;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;


public class SelectImageConverterPanel extends JPanel implements PreviewListener, ImageConverterListener {
	private static final Logger logger = LoggerFactory.getLogger(SelectImageConverterPanel.class);
	@Serial
	private static final long serialVersionUID = 5574250944369730761L;

	/**
	 * Set of image file extensions.
	 * TODO These should be populated from the ImageIO.getReaderFileSuffixes() method after the image converters are loaded.
 	 */
	public static final String [] IMAGE_FILE_EXTENSIONS = {"jpg","jpeg","png","wbmp","bmp","gif","qoi"};

	@SuppressWarnings("deprecation")
	private final Preferences prefs = PreferencesHelper
			.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.LEGACY_MAKELANGELO_ROOT);
	
	private final Paper myPaper;
	private final TransformedImage myImage;

	private static JComboBox<String> styleNames;
	private static JComboBox<String> fillNames;
	private final JPanel cards = new JPanel(new CardLayout());

	private ImageConverter myConverter;
	
	public SelectImageConverterPanel(Paper paper, TransformedImage image) {
		super();
		myPaper = paper;
		myImage = image;

		cards.setPreferredSize(new Dimension(450, 300));

		fillNames = getFillSelection();
		styleNames = getStyleSelection();

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		int y = 0;

		y++;
		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = y;
		c.ipadx = 5;
		this.add(new JLabel(Translator.get("ConversionFill")), c);
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 3;
		c.gridx = 1;
		c.ipadx = 0;
		this.add(fillNames, c);

		y++;
		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = y;
		c.ipadx = 5;
		this.add(new JLabel(Translator.get("ConversionStyle")), c);
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 3;
		c.gridx = 1;
		c.ipadx = 0;
		this.add(styleNames, c);

		y++;
		c.anchor = GridBagConstraints.NORTH;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 4;
		c.gridx = 0;
		c.gridy = y;
		c.insets = new Insets(10, 0, 0, 0);
		cards.setPreferredSize(new Dimension(449, 325));
		cards.setBorder(BorderFactory.createLoweredBevelBorder());
		this.add(cards, c);

	}

	/**
	 * Start the image conversion process.
	 */
	public void run() {
		scaleImage(fillNames.getSelectedIndex());
		showCard((String) styleNames.getSelectedItem());
		int first = styleNames.getSelectedIndex();
		changeConverter(ImageConverterFactory.getList()[first]);
	}
	
	private JComboBox<String> getStyleSelection() {
		ArrayList<String> imageConverterNames = new ArrayList<>();
		for( ImageConverter i : ImageConverterFactory.getList() ) {
			imageConverterNames.add(i.getName());
			cards.add(new ImageConverterPanel(i), i.getName());
		}
		
		JComboBox<String> box = new JComboBox<>(imageConverterNames.toArray(new String[0]));
		box.setSelectedIndex(getPreferredDrawStyle());
		box.addItemListener(this::onConverterChanged);

		return box;
	}

	private JComboBox<String> getFillSelection() {
		String[] imageFillNames = {
				Translator.get("ConvertImagePaperFill"),
				Translator.get("ConvertImagePaperFit")
		};
		JComboBox<String> box = new JComboBox<>(imageFillNames);

		int p = getPreferredFillStyle();
		if(p>=box.getItemCount()) p=0;
		box.setSelectedIndex(p);
		box.addItemListener((e) ->{
			scaleImage(box.getSelectedIndex());
			setPreferredFillStyle(box.getSelectedIndex());
			restart();
		});

		return box;
	}

	private void onConverterChanged(ItemEvent e) {
		logger.debug("onConverterChanged");

		showCard((String)e.getItem());

		int first = (styleNames!=null ? styleNames.getSelectedIndex() : 0);
		setPreferredDrawStyle(first);
		changeConverter(ImageConverterFactory.getList()[first]);
	}

	private void showCard(String cardName) {
		CardLayout cl = (CardLayout)(cards.getLayout());
		cl.show(cards, cardName);
	}

	private void scaleImage(int mode) {
		double width  = myPaper.getMarginWidth();
		double height = myPaper.getMarginHeight();

		boolean test;
		if (mode == 0) {
			test = width < height;  // fill paper
		} else {
			test = width > height;  // fit paper
		}

		float f;
		if( test ) {
			f = (float)( width / (double)myImage.getSourceImage().getWidth() );
		} else {
			f = (float)( height / (double)myImage.getSourceImage().getHeight() );
		}
		myImage.setScale(f,-f);
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
	
	public static boolean isFilenameForAnImage(String filename) {
		final String filenameExtension = filename.substring(filename.lastIndexOf('.') + 1);
		List<String> valid = Arrays.asList(IMAGE_FILE_EXTENSIONS);
		return valid.contains(filenameExtension.toLowerCase());
	}

	private void stopConversion() {
		if(myConverter != null) {
			logger.debug("Stop conversion");
			myConverter.stop();
		}
	}
	
	private void startConversion() {
		if(myConverter==null || myImage==null || myPaper==null) return;

		logger.debug("startConversion() {}", myConverter.getName());

		myConverter.start(myPaper,myImage);
	}
	
	private void changeConverter(ImageConverter converter) {
		logger.debug("changeConverter() {}", converter.getName());

		stopConversion();
		eraseOldTurtle();

		if(myConverter != null) myConverter.removeImageConverterListener(this);
		myConverter = converter;
		myConverter.addImageConverterListener(this);

		startConversion();
	}

	private void restart() {
		logger.debug("restart()");
		stopConversion();
		eraseOldTurtle();
		startConversion();
	}

	private void eraseOldTurtle() {
		onConvertFinished(new Turtle());
	}

	@Override
	public void render(GL2 gl2) {
		if( myConverter != null && myConverter instanceof PreviewListener ) {
			((PreviewListener)myConverter).render(gl2);
		}
	}

	@Override
	public void onRestart(ImageConverter converter) {
		restart();
	}

	@Override
	public void onConvertFinished(Turtle turtle) {
		notifyListeners(new ActionEvent(turtle,0,"turtle"));
	}

	public void loadingFinished() {
		logger.debug("loadingFinished()");
		if(myConverter != null) myConverter.stop();
	}

	// OBSERVER PATTERN

	private final ArrayList<ActionListener> listeners = new ArrayList<>();
	public void addActionListener(ActionListener a) {
		listeners.add(a);
	}

	@SuppressWarnings("unused")
	public void removeActionListener(ActionListener a) {
		listeners.remove(a);
	}
	
	private void notifyListeners(ActionEvent e) {
		for( ActionListener a : listeners ) {
			a.actionPerformed(e);
		}
	}

	// TEST
	
	public static void main(String[] args) throws Exception {
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();

		TransformedImage image = new TransformedImage(ImageIO.read(new FileInputStream("C:/Users/aggra/Documents/drawbot art/grumpyCat.jpg")));
		JFrame frame = new JFrame(SelectImageConverterPanel.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new SelectImageConverterPanel(new Paper(),image));
		frame.pack();
		frame.setVisible(true);
	}
}
