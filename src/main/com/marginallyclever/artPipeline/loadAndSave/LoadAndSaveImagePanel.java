package com.marginallyclever.artPipeline.loadAndSave;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.ServiceLoader;
import java.util.prefs.Preferences;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.marginallyclever.artPipeline.converters.ImageConverter;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.util.PreferencesHelper;

public class LoadAndSaveImagePanel {
	@SuppressWarnings("deprecation")
	private Preferences prefs = PreferencesHelper
			.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.LEGACY_MAKELANGELO_ROOT);
	
	private LoadAndSaveImage loader;
	
	private JPanel conversionPanel;
	private static JComboBox<String> styleNames;
	private static JComboBox<String> fillNames;
	private JPanel cards;

	private ArrayList<String> imageConverterNames = new ArrayList<String>();
	private String[] imageFillNames = {
		Translator.get("ConvertImagePaperFill"),
		Translator.get("ConvertImagePaperFit")
	};
	
	public LoadAndSaveImagePanel(LoadAndSaveImage loader) {
		this.loader = loader;

		imageConverterNames.clear();
		
		conversionPanel = new JPanel(new GridBagLayout());

		fillNames = new JComboBox<String>(imageFillNames);
		
		cards = new JPanel(new CardLayout());
		cards.setPreferredSize(new Dimension(450,300));
		ServiceLoader<ImageConverter> converterServices = ServiceLoader.load(ImageConverter.class);
		ArrayList<ImageConverter> converters = new ArrayList<ImageConverter>();
		for( ImageConverter ici : converterServices ) {
			converters.add(ici);
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
			loader.changeConverter(converters.get(styleNames.getSelectedIndex()));
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

		loader.changeConverter(converters.get(p));
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
