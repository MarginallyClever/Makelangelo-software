package com.marginallyclever.makelangelo.makeArt.io.image;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeArt.imageConverter.ImageConverter;
import com.marginallyclever.makelangelo.makeArt.imageConverter.ImageConverterFactory;
import com.marginallyclever.makelangelo.makeArt.imageConverter.Converter_Boxes;
import com.marginallyclever.makelangelo.makeArt.imageConverter.Converter_CMYK;
import com.marginallyclever.makelangelo.makeArt.imageConverter.Converter_Crosshatch;
import com.marginallyclever.makelangelo.makeArt.imageConverter.Converter_Moire;
import com.marginallyclever.makelangelo.makeArt.imageConverter.Converter_Multipass;
import com.marginallyclever.makelangelo.makeArt.imageConverter.Converter_Pulse;
import com.marginallyclever.makelangelo.makeArt.imageConverter.Converter_RandomLines;
import com.marginallyclever.makelangelo.makeArt.imageConverter.Converter_Sandy;
import com.marginallyclever.makelangelo.makeArt.imageConverter.Converter_Spiral;
import com.marginallyclever.makelangelo.makeArt.imageConverter.Converter_Spiral_CMYK;
import com.marginallyclever.makelangelo.makeArt.imageConverter.Converter_SpiralPulse;
import com.marginallyclever.makelangelo.makeArt.imageConverter.Converter_VoronoiStippling;
import com.marginallyclever.makelangelo.makeArt.imageConverter.Converter_VoronoiZigZag;
import com.marginallyclever.makelangelo.makeArt.imageConverter.Converter_Wander;

import com.marginallyclever.util.PreferencesHelper;

public class LoadImagePanel {
	@SuppressWarnings("deprecation")
	private Preferences prefs = PreferencesHelper
			.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.LEGACY_MAKELANGELO_ROOT);
	
	private LoadImage loader;
	
	private JPanel conversionPanel;
	private static JComboBox<String> styleNames;
	private static JComboBox<String> fillNames;
	private JPanel cards;
	
	public LoadImagePanel(LoadImage loader) {
		this.loader = loader;

		conversionPanel = new JPanel(new GridBagLayout());
		
		String[] imageFillNames = {
			Translator.get("ConvertImagePaperFill"),
			Translator.get("ConvertImagePaperFit")
		};
		fillNames = new JComboBox<String>(imageFillNames);
		
		cards = new JPanel(new CardLayout());
		cards.setPreferredSize(new Dimension(450,300));

		ArrayList<String> imageConverterNames = new ArrayList<String>();
		for( ImageConverter ici : ImageConverterFactory.converters ) {
			imageConverterNames.add(ici.getName());
			cards.add(ici.getPanel().getPanel(),ici.getName());
		}
		String [] converterNameArray = (String[]) imageConverterNames.toArray(new String[0]);
		styleNames = new JComboBox<String>(converterNameArray);

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
		
		styleNames.addItemListener((e) -> {
		    CardLayout cl = (CardLayout)(cards.getLayout());
		    cl.show(cards, (String)e.getItem());

			switch(fillNames.getSelectedIndex()) {
				case 0:  loader.scaleToFillPaper();  break;
				case 1:  loader.scaleToFitPaper();  break;
				default: break;
			}
			loader.changeConverter(ImageConverterFactory.converters[styleNames.getSelectedIndex()]);
		});
		
		fillNames.addItemListener((e) -> {
			switch(fillNames.getSelectedIndex()) {
				case 0:  loader.scaleToFillPaper();  break;
				case 1:  loader.scaleToFitPaper();  break;
				default: break;
			}
			loader.reconvert();
		});
		
		p=getPreferredDrawStyle();
		if(p>=styleNames.getItemCount()) p=0;
		styleNames.setSelectedIndex(p);

		loader.changeConverter(ImageConverterFactory.converters[p]);
	}
	
	public void run(Component parentComponent) {
		int result = JOptionPane.showConfirmDialog(parentComponent, conversionPanel, Translator.get("ConversionOptions"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			setPreferredDrawStyle(styleNames.getSelectedIndex());
			setPreferredFillStyle(fillNames.getSelectedIndex());
		}
		loader.stopConversion();
	}
	
	public void setPreferredDrawStyle(int style) {
		prefs.putInt("Draw Style", style);
	}
	
	public void setPreferredFillStyle(int style) {
		prefs.putInt("Fill Style", style);
	}

	public int getPreferredDrawStyle() {
		return prefs.getInt("Draw Style", 0);
	}

	public int getPreferredFillStyle() {
		return prefs.getInt("Fill Style", 0);
	}
}
