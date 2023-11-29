package com.marginallyclever.makelangelo.paper;

import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.*;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

public class PaperSettingsPanel extends SelectPanel {
	private static final Logger logger = LoggerFactory.getLogger(PaperSettingsPanel.class);

	private static final PaperSize[] commonPaperSizes = {
		new PaperSize("4A0",1682,2378),
		new PaperSize("2A0",1189,1682),
		new PaperSize("A0",841,1189),
		new PaperSize("A1",594,841),
		new PaperSize("A2",420,594),
		new PaperSize("A3",297,420),
		new PaperSize("A4",210,297),
		new PaperSize("A5",148,210),
		new PaperSize("A6",105,148),
		new PaperSize("A7",74,105),
		new PaperSize("US Half Letter",140,216),
		new PaperSize("US Letter",216,279),
		new PaperSize("US Legal",216,356),
		new PaperSize("US Junior Legal",127,203),
		new PaperSize("US Ledger / Tabloid",279,432),
		new PaperSize("ANSI A",216,279),
		new PaperSize("ANSI B",279,432),
		new PaperSize("ANSI C",432,559),
		new PaperSize("ANSI D",559,864),
		new PaperSize("ANSI E",864,1118),
		new PaperSize("Arch A",229,305),
		new PaperSize("Arch B",305,457),
		new PaperSize("Arch C",457,610),
		new PaperSize("Arch D",610,914),
		new PaperSize("Arch E",914,1219),
		new PaperSize("Arch E1",762,1067)
	};

	private final Paper myPaper;
	private final SelectOneOfMany paperSizes;
	private final SelectDouble pw, ph, shiftX, shiftY, ang;
	private final SelectBoolean isLandscape;
	private final SelectSlider paperMargin;
	private final SelectColor paperColor;
	
	public PaperSettingsPanel(Paper paper) {
		this.myPaper = paper;
		this.setName(PaperSettingsPanel.class.getSimpleName());

		// common paper sizes
		String[] commonPaperNames = new String[commonPaperSizes.length+1];
		commonPaperNames[0]="---";
		int i;
		for(i=0;i<commonPaperSizes.length;++i) {
			commonPaperNames[i+1] = commonPaperSizes[i].toString();
		}

		add(paperSizes = new SelectOneOfMany("size",Translator.get("PaperSettings.PaperSize"),commonPaperNames,0));
		add(pw = new SelectDouble("width",Translator.get("PaperSettings.PaperWidth"),myPaper.getPaperWidth()));
		add(ph = new SelectDouble("height",Translator.get("PaperSettings.PaperHeight"),myPaper.getPaperHeight()));
		add(shiftX = new SelectDouble("shiftx",Translator.get("PaperSettings.ShiftX"),myPaper.getCenterX()));
		add(shiftY = new SelectDouble("shifty",Translator.get("PaperSettings.ShiftY"),myPaper.getCenterY()));
		ang = new SelectDouble("rotation",Translator.get("PaperSettings.Rotation"),myPaper.getRotation());
		//add();
		add(isLandscape = new SelectBoolean("landscape",Translator.get("PaperSettings.Landscape"),false));
		add(paperMargin = new SelectSlider("margin",Translator.get("PaperSettings.PaperMargin"),50,0,100 - (int) (myPaper.getPaperMargin() * 100)));
		add(paperColor = new SelectColor("color",Translator.get("PaperSettings.PaperColor"),myPaper.getPaperColor(),this));

		getValuesFromPaper();// As the paper load this value from the pref when instancied.		
		onPaperWidthOrHeightChange(null);//this set the SelectOneOfMany paperSizes and the landscape checkbox to the correcte values.
		
		paperSizes.addSelectListener(this::onPaperSizePresetChange);
		pw.addSelectListener(this::onPaperWidthOrHeightChange);
		ph.addSelectListener(this::onPaperWidthOrHeightChange);
		shiftX.addSelectListener((e)->setPaperFromPanel());
		shiftY.addSelectListener((e)->setPaperFromPanel());
		//ang.addSelectListener((e)->setPaperFromPanel());
		isLandscape.addSelectListener(this::onLandscapeChange);
		paperMargin.addSelectListener((e)->setPaperFromPanel());
		paperColor.addSelectListener((e)->setPaperFromPanel());
	}

	/**
	 * Called when the user changes the paper width or height.
	 * @param e the event that triggered this call
	 */
	private void onPaperWidthOrHeightChange(SelectEvent e) {
		logger.debug("onPaperDimensionsChange()");
		double w=getPaperWidthFromPanel();
		double h=getPaperHeightFromPanel();

		int i = getCurrentPaperSizeChoice( h, w );
		if(i!=0) {
			logger.debug("landscape {} found", i);
			isLandscape.setSelected(true);
		} else {
			i = getCurrentPaperSizeChoice( w, h );
			if(i!=0) {
				logger.debug("portrait {} found", i);
				isLandscape.setSelected(false);
			}
		}
		if(paperSizes.getSelectedIndex()!=i) paperSizes.setSelectedIndex(i);
		logger.debug("onPaperDimensionsChange() done");
	}

	private void onLandscapeChange(SelectEvent e) {
		logger.debug("onLandscapeChange()");
		double w = pw.getValue();
		double h = ph.getValue();
		pw.setValue(h);
		ph.setValue(w);
		setPaperFromPanel();
		logger.debug("onLandscapeChange() done");
	}

	/**
	 * Called when the user selects a paper size from the list.
	 * @param e the event that triggered this call
	 */
	private void onPaperSizePresetChange(SelectEvent e) {
		logger.debug("onPaperSizeChange()");
		final int selectedIndex = paperSizes.getSelectedIndex();
		if(selectedIndex != 0) {
			logger.debug("found index {}", selectedIndex);
			PaperSize s = commonPaperSizes[selectedIndex-1];
			double w = s.width;
			double h = s.height;
			if(isLandscape.isSelected()) {
				double temp = w;
				w = h;
				h = temp;
			}
			
			boolean isDirty=false;
			if(w != getPaperWidthFromPanel()) {
				pw.setValue(w);
				isDirty=true;
			}
			if(h != getPaperHeightFromPanel()) {
				ph.setValue(h);
				isDirty=true;
			}
			if(isDirty)	setPaperFromPanel();
		}
		logger.debug("onPaperSizeChange() done");
	}
	
	/**
	 * @return the value displayed on the panel
	 */
	private double getPaperWidthFromPanel() {
		double w=0;
		try {
			w = ((Number)pw.getValue()).doubleValue();
		} catch(Exception e) {
			logger.error("Failed to get paper width", e);
		}
		return w;
	}
	
	/**
	 * @return the value displayed on the panel
	 */
	private double getPaperHeightFromPanel() {
		double h=0;
		try {
			h = ((Number)ph.getValue()).doubleValue();
		} catch(Exception e) {
			logger.error("Failed to get paper height", e);
		}
		return h;
	}
	
	/**
	 * Apply this panel values to {@code myPaper}
	 */
	private void setPaperFromPanel() {
		double w = ((Number)pw.getValue()).doubleValue();
		double h = ((Number)ph.getValue()).doubleValue();
		double sx = ((Number)shiftX.getValue()).doubleValue();
		double sy = ((Number)shiftY.getValue()).doubleValue();
		//double rot = ((Number)ang.getValue()).doubleValue();
		myPaper.setPaperSize(w, h, sx, sy);
		//myPaper.setRotation(rot);
		myPaper.setPaperColor(paperColor.getColor());
		myPaper.setPaperMargin((100 - paperMargin.getValue()) * 0.01);
	}

	/**
	 * Load the values from {@code myPaper} into this panel
	 */
	private void getValuesFromPaper() {
		pw.setValue(myPaper.getPaperWidth());
		ph.setValue(myPaper.getPaperHeight());
	}

	/**
	 * Find the index of {@code commonPaperSizes} that matches the desired width and height. 
	 * @param paperWidth mm
	 * @param paperHeight mm
	 * @return the index from the commonPaperSizes list, or 0 if not found.
	 */
	public int getCurrentPaperSizeChoice(double paperWidth,double paperHeight) {
		int i;
		for(i=0;i<commonPaperSizes.length;++i) {
			if(paperWidth == commonPaperSizes[i].width && 
				paperHeight == commonPaperSizes[i].height)
				return i+1;
		}

		return 0;
	}
	
	public void save() {
		double pwf = ((Number)pw.getValue()).doubleValue();
		double phf = ((Number)ph.getValue()).doubleValue();
		double shiftxf = ((Number)shiftX.getValue()).doubleValue();
		double shiftyf = ((Number)shiftY.getValue()).doubleValue();
		//double rot = ((Number)ang.getValue()).doubleValue();
		
		boolean data_is_sane=true;
		if( pwf<=0 ) data_is_sane=false;
		if( phf<=0 ) data_is_sane=false;

		if (data_is_sane) {
			myPaper.setPaperSize(pwf,phf,shiftxf,shiftyf);
			//myPaper.setRotation(rot);
			myPaper.setPaperColor(paperColor.getColor());

			double pm = (100 - paperMargin.getValue()) * 0.01;
			myPaper.setPaperMargin(pm);
			myPaper.saveConfig();
		}
	}
	
	// TEST
	
	public static void main(String[] args) {
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();
		
		JFrame frame = new JFrame(PaperSettingsPanel.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new PaperSettingsPanel(new Paper()));
		frame.pack();
		frame.setVisible(true);
	}
}
