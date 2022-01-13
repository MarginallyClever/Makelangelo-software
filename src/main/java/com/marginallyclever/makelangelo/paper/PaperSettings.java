package com.marginallyclever.makelangelo.paper;

import java.beans.PropertyChangeEvent;

import javax.swing.JFrame;

import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectBoolean;
import com.marginallyclever.makelangelo.select.SelectColor;
import com.marginallyclever.makelangelo.select.SelectDouble;
import com.marginallyclever.makelangelo.select.SelectOneOfMany;
import com.marginallyclever.makelangelo.select.SelectPanel;
import com.marginallyclever.makelangelo.select.SelectSlider;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PaperSettings extends SelectPanel {

	private static final Logger logger = LoggerFactory.getLogger(PaperSettings.class);
	
	private static final long serialVersionUID = 2824594482225714527L;

	private static PaperSize commonPaperSizes [] = {
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

	private Paper myPaper;
	private SelectOneOfMany paperSizes;
	private SelectDouble pw, ph, shiftX, shiftY, ang;
	private SelectBoolean isLandscape;
	private SelectSlider paperMargin;
	private SelectColor paperColor;
	
	public PaperSettings(Paper paper) {
		this.myPaper = paper;
		
		// common paper sizes
		String[] commonPaperNames = new String[commonPaperSizes.length+1];
		commonPaperNames[0]="---";
		int i;
		for(i=0;i<commonPaperSizes.length;++i) {
			commonPaperNames[i+1] = commonPaperSizes[i].toString();
		}

		double top = myPaper.getPaperTop();
		double bot = myPaper.getPaperBottom();
		double left = myPaper.getPaperLeft();
		double right = myPaper.getPaperRight();
		double rot = myPaper.getRotation();
		
		add(paperSizes = new SelectOneOfMany("size",Translator.get("PaperSettings.PaperSize"),commonPaperNames,0));
		add(pw = new SelectDouble("width",Translator.get("PaperSettings.PaperWidth"),right-left));
		add(ph = new SelectDouble("height",Translator.get("PaperSettings.PaperHeight"),top-bot));
		add(shiftX = new SelectDouble("shiftx",Translator.get("PaperSettings.ShiftX"),(float)(left+right)/2.0f));
		add(shiftY = new SelectDouble("shifty",Translator.get("PaperSettings.ShiftY"),(float)(top+bot)/2.0f));
		add(ang = new SelectDouble("rotation",Translator.get("PaperSettings.Rotation"),(float)rot));
		add(isLandscape = new SelectBoolean("landscape",Translator.get("PaperSettings.Landscape"),false));
		add(paperMargin = new SelectSlider("margin",Translator.get("PaperSettings.PaperMargin"),50,0,100 - (int) (myPaper.getPaperMargin() * 100)));
		add(paperColor = new SelectColor("color",Translator.get("PaperSettings.PaperColor"),myPaper.getPaperColor(),this));

		getValuesFromPaper();// As the paper load this value from the pref when instancied.		
		onPaperDimensionsChange(null);//this set the SelectOneOfMany paperSizes and the landscape checkbox to the correcte values.
		
		paperSizes.addPropertyChangeListener((e)->onPaperSizeChange(e));
		pw.addPropertyChangeListener((e)->onPaperDimensionsChange(e));
		ph.addPropertyChangeListener((e)->onPaperDimensionsChange(e));
		shiftX.addPropertyChangeListener((e)->setPaperFromPanel());
		shiftY.addPropertyChangeListener((e)->setPaperFromPanel());
		ang.addPropertyChangeListener((e)->setPaperFromPanel());
		isLandscape.addPropertyChangeListener((e)->onLandscapeChange(e));
		paperMargin.addPropertyChangeListener((e)->setPaperFromPanel());
		paperColor.addPropertyChangeListener((e)->setPaperFromPanel());
	}
	
	private void onPaperDimensionsChange(PropertyChangeEvent e) {
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

	private void onLandscapeChange(PropertyChangeEvent e) {
		logger.debug("onLandscapeChange()");
		double w = pw.getValue();
		double h = ph.getValue();
		pw.setValue(h);
		ph.setValue(w);
		setPaperFromPanel();
		logger.debug("onLandscapeChange() done");
	}

	private void onPaperSizeChange(PropertyChangeEvent e) {
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
		logger.debug("updatePaperFromPanel()");
		double w = ((Number)pw.getValue()).doubleValue();
		double h = ((Number)ph.getValue()).doubleValue();
		double sx = ((Number)shiftX.getValue()).doubleValue();
		double sy = ((Number)shiftY.getValue()).doubleValue();
		double rot = ((Number)ang.getValue()).doubleValue();
		myPaper.setPaperSize(w, h, sx, sy);
		myPaper.setRotation(rot);
		myPaper.setPaperColor(paperColor.getColor());
		myPaper.setPaperMargin((100 - paperMargin.getValue()) * 0.01);
		myPaper.saveConfig();
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
		double rot = ((Number)ang.getValue()).doubleValue();
		
		boolean data_is_sane=true;
		if( pwf<=0 ) data_is_sane=false;
		if( phf<=0 ) data_is_sane=false;

		if (data_is_sane) {
			myPaper.setPaperSize(pwf,phf,shiftxf,shiftyf);
			myPaper.setRotation(rot);
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
		
		JFrame frame = new JFrame(PaperSettings.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new PaperSettings(new Paper()));
		frame.pack();
		frame.setVisible(true);
	}
}
