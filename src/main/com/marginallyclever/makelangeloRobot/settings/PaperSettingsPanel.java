package com.marginallyclever.makelangeloRobot.settings;

import java.beans.PropertyChangeEvent;

import javax.swing.JFrame;
import javax.swing.UIManager;

import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.Paper;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectBoolean;
import com.marginallyclever.makelangelo.select.SelectColor;
import com.marginallyclever.makelangelo.select.SelectDouble;
import com.marginallyclever.makelangelo.select.SelectOneOfMany;
import com.marginallyclever.makelangelo.select.SelectPanel;
import com.marginallyclever.makelangelo.select.SelectSlider;
import com.marginallyclever.util.PreferencesHelper;

public class PaperSettingsPanel extends SelectPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected Paper myPaper;
	
	private SelectOneOfMany paperSizes;
	private SelectDouble pw, ph,sx,sy,ang;
	private SelectBoolean isLandscape;
	private SelectSlider paperMargin;
	private boolean beingModified;
	private SelectColor paperColor;
	
	class PaperSize {
		public String name;
		public int width;
		public int height;
		
		PaperSize(String name,int width,int height) {
			this.name = name;
			this.width = width;
			this.height = height;
		}
		
		public String toString() {
			return name+" ("+width+" x "+height+")";
		}
	}
	
	private PaperSize commonPaperSizes [] = {
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

	public PaperSettingsPanel(Paper paper) {
		this.myPaper = paper;
		
		beingModified=false;

		// common paper sizes
		String[] commonPaperNames = new String[commonPaperSizes.length+1];
		commonPaperNames[0]="---";
		int i;
		for(i=0;i<commonPaperSizes.length;++i) {
			commonPaperNames[i+1] = commonPaperSizes[i].toString();
		}
		
		add(paperSizes = new SelectOneOfMany("size",Translator.get("PaperSize"),commonPaperNames,0));
		double top = myPaper.getPaperTop();
		double bot = myPaper.getPaperBottom();
		double left = myPaper.getPaperLeft();
		double right = myPaper.getPaperRight();
		double rot = myPaper.getRotation();
		add(pw = new SelectDouble("width",Translator.get("PaperWidth"),(float)(right-left)));
		add(ph = new SelectDouble("height",Translator.get("PaperHeight"),(float)(top-bot))); 
		add(sx = new SelectDouble("shiftx","Shift X",(float)(left+right)/2.0f)); 
		add(sy = new SelectDouble("shifty","Shift y",(float)(top+bot)/2.0f)); 
		add(ang = new SelectDouble("rotation","Rotation",(float)rot));
		add(isLandscape = new SelectBoolean("landscape","\u21cb",false));
		add(paperMargin = new SelectSlider("margin",Translator.get("PaperMargin"),50,0,100 - (int) (myPaper.getPaperMargin() * 100)));
		add(paperColor = new SelectColor("color",Translator.get("paper color"),myPaper.getPaperColor(),interiorPanel));
		finish();
		updateValues();
	}
	
	void updateValues() {
		double w = myPaper.getPaperWidth();
		double h = myPaper.getPaperHeight();

		int i = getCurrentPaperSizeChoice( h, w );
		if(i!=0) {
			isLandscape.setSelected(true);
		} else {
			i = getCurrentPaperSizeChoice( w, h );
			isLandscape.setSelected(false);
		}
		paperSizes.setSelectedIndex(i);
		pw.setValue((float)w);
		ph.setValue((float)h);
	}

	/**
	 * Must match commonPaperSizes
	 * @param paperWidth mm
	 * @param paperHeight mm
	 * @return the index from the commonPaperSizes list.
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

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);

		if(beingModified) return;
		beingModified=true;
		
		Object o = evt.getSource();
		
		if(o == paperSizes) {
			final int selectedIndex = paperSizes.getSelectedIndex();
			if(selectedIndex!= 0) {
				PaperSize s = commonPaperSizes[selectedIndex-1];
				float sw = s.width;
				float sh = s.height;
				if(isLandscape.isSelected()) {
					float temp = sw;
					sw = sh;
					sh = temp;
				}
				pw.setValue(sw);
				ph.setValue(sh);
			}
		} else if(o == isLandscape) {
			double sw = pw.getValue();
			double sh = ph.getValue();
			pw.setValue(sh);
			ph.setValue(sw);
		} else if( o == pw || o == ph ) {
			double w=0;
			double h=0;
			try {
				w = ((Number)pw.getValue()).doubleValue();
				h = ((Number)ph.getValue()).doubleValue();
			} catch(Exception err) {
				err.getMessage();
			}

			int i = getCurrentPaperSizeChoice( h, w );
			if(i!=0) {
				isLandscape.setSelected(true);
			} else {
				i = getCurrentPaperSizeChoice( w, h );
				isLandscape.setSelected(false);
			}
			paperSizes.setSelectedIndex(i);
		}
		
		beingModified=false;
	}
	
	public void save() {
		double pwf = ((Number)pw.getValue()).doubleValue();
		double phf = ((Number)ph.getValue()).doubleValue();
		double shiftxf = ((Number)sx.getValue()).doubleValue();
		double shiftyf = ((Number)sy.getValue()).doubleValue();
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
		}
	}
	
	// TEST
	
	public static void main(String[] args) {
		Log.start();
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {}
		
		JFrame frame = new JFrame(PaperSettingsPanel.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new PaperSettingsPanel(new Paper()));
		frame.pack();
		frame.setVisible(true);
	}
}
