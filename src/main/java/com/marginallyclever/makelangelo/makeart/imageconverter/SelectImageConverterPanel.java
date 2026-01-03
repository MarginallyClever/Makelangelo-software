package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.FilterContrastAdjust;
import com.marginallyclever.makelangelo.makeart.io.LoadFilePanel;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.preview.PreviewListener;
import com.marginallyclever.makelangelo.rangeslider.RangeSlider;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * This panel allows the user to select an image converter and set its parameters.
 */
public class SelectImageConverterPanel extends JPanel implements PreviewListener, ImageConverterListener {
	private static final Logger logger = LoggerFactory.getLogger(SelectImageConverterPanel.class);

	/**
	 * Set of image file extensions.
	 */
	public static final String [] IMAGE_FILE_EXTENSIONS = Arrays.stream(ImageIO.getReaderFileSuffixes()).sorted().toArray(String[]::new);

	@SuppressWarnings("deprecation")
	private final Preferences prefs = PreferencesHelper
			.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.LEGACY_MAKELANGELO_ROOT);
	
	private final Paper myPaper;
	private final TransformedImage myImage;

	private static JComboBox<String> styleNames;
	private static JComboBox<String> fillNames;
	private final JPanel cards = new JPanel(new CardLayout());
	private final RangeSlider rangeSlider = new RangeSlider();
	private static int rangeSliderMin = 0;
	private static int rangeSliderMax = 255;
	private final PlotterSettings myPlotterSettings;

	private ImageConverter myConverter;
	
	public SelectImageConverterPanel(Paper paper, PlotterSettings plotterSettings, TransformedImage image) {
		super(new GridBagLayout());
		myPaper = paper;
		myImage = image;
		myPlotterSettings = plotterSettings;

		fillNames = getFillSelection();
		styleNames = getStyleSelection();

		Insets insetTop = new Insets(5, 0, 0, 0);
		Insets insetLeft = new Insets(0, 5, 0, 0);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0;
		c.weighty = 0;
		c.gridwidth = 1;

		int y = 0;

		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = y;
		c.insets = insetTop;
		this.add(new JLabel(Translator.get("ConversionFill")), c);
		c.gridx = 1;
		c.insets = insetLeft;
		this.add(fillNames, c);
		y++;

		c.gridx = 0;
		c.gridy = y;
		c.insets = insetTop;
		this.add(new JLabel(Translator.get("ConversionStyle")), c);
		c.gridx = 1;
		c.insets = insetLeft;
		this.add(styleNames, c);
		y++;

		c.gridx = 0;
		c.gridy = y;
		c.insets = insetTop;
		this.add(new JLabel(Translator.get("SelectImageConverterPanel.Contrast")), c);
		c.gridx = 1;
		c.insets = insetLeft;
		this.add(rangeSlider, c);
		y++;

		rangeSlider.setMinimum(0);
		rangeSlider.setMaximum(255);
		rangeSlider.setUpperValue(rangeSliderMax);
		rangeSlider.setValue(rangeSliderMin);
		rangeSlider.addChangeListener(this::onSliderChanged);

		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy = y;
		c.weightx=1;
		c.weighty=1;
		c.insets = insetTop;
		cards.setBorder(BorderFactory.createLoweredBevelBorder());
		this.add(cards, c);
	}

	private void onSliderChanged(ChangeEvent changeEvent) {
		rangeSliderMin = rangeSlider.getValue();
		rangeSliderMax = rangeSlider.getUpperValue();
		restart();
	}

	/**
	 * Start the image conversion process.
	 */
	public void run() {
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
				Translator.get("ConvertImagePaperFit"),
				Translator.get("ConvertImagePaperFill"),
		};
		JComboBox<String> box = new JComboBox<>(imageFillNames);

		int p = getPreferredFillStyle();
		if(p>=box.getItemCount()) p=0;
		box.setSelectedIndex(p);
		box.addItemListener((e) ->{
			setPreferredFillStyle(box.getSelectedIndex());
			restart();
		});

		return box;
	}

	private void onConverterChanged(ItemEvent e) {
		logger.debug("changing to {}", e.getItem());

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
		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		double width  = rect.getWidth();
		double height = rect.getHeight();

		boolean test;
		if (mode == 0) {
			test = width < height;  // fit paper
		} else {
			test = width > height;  // fill paper
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

		logger.debug("starting {}", myConverter.getName());

		scaleImage(fillNames.getSelectedIndex());
		FilterContrastAdjust filter = new FilterContrastAdjust(myImage,rangeSliderMin, rangeSliderMax);
		TransformedImage result = filter.filter();

		myConverter.start(myPaper,result);
	}
	
	private void changeConverter(ImageConverter converter) {
		logger.debug("changeConverter() {}", converter.getName());

		stopConversion();
		eraseOldTurtle();

		if(myConverter != null) myConverter.removeImageConverterListener(this);
		myConverter = converter;
		myConverter.setPlotterSettings(myPlotterSettings);
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
	public void render(Graphics graphics) {
		if( myConverter != null && myConverter instanceof PreviewListener ) {
			((PreviewListener)myConverter).render(graphics);
		}
	}

	@Override
	public void onRestart(ImageConverter converter) {
		restart();
	}

	@Override
	public void onConvertFinished(Turtle turtle) {
		notifyListeners(new ActionEvent(turtle,0, LoadFilePanel.COMMAND_TURTLE));
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
}
