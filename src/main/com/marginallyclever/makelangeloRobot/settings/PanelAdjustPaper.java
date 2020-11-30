package com.marginallyclever.makelangeloRobot.settings;

import java.util.Observable;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectBoolean;
import com.marginallyclever.makelangelo.select.SelectColor;
import com.marginallyclever.makelangelo.select.SelectFloat;
import com.marginallyclever.makelangelo.select.SelectOneOfMany;
import com.marginallyclever.makelangelo.select.SelectPanel;
import com.marginallyclever.makelangelo.select.SelectSlider;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;

public class PanelAdjustPaper extends SelectPanel {
	protected MakelangeloRobot robot;
	
	private SelectOneOfMany paperSizes;
	private SelectFloat pw, ph,sx,sy,ang;
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

	public PanelAdjustPaper(MakelangeloRobot robot) {
		this.robot = robot;
		
		beingModified=false;

		// common paper sizes
		String[] commonPaperNames = new String[commonPaperSizes.length+1];
		commonPaperNames[0]="---";
		int i;
		for(i=0;i<commonPaperSizes.length;++i) {
			commonPaperNames[i+1] = commonPaperSizes[i].toString();
		}
		
		add(paperSizes = new SelectOneOfMany(Translator.get("PaperSize"),commonPaperNames,0));
		double top = robot.getSettings().getPaperTop();
		double bot = robot.getSettings().getPaperBottom();
		double left = robot.getSettings().getPaperLeft();
		double right = robot.getSettings().getPaperRight();
		double rot = robot.getSettings().getRotation();
		add(pw = new SelectFloat(Translator.get("PaperWidth"),(float)(right-left)));
		add(ph = new SelectFloat(Translator.get("PaperHeight"),(float)(top-bot))); 
		add(sx = new SelectFloat("Shift X",(float)(left+right)/2.0f)); 
		add(sy = new SelectFloat("Shift y",(float)(top+bot)/2.0f)); 
		add(ang = new SelectFloat("Rotation",(float)rot));
		add(isLandscape = new SelectBoolean("\u21cb",false));
		add(paperMargin = new SelectSlider(Translator.get("PaperMargin"),50,0,100 - (int) (robot.getSettings().getPaperMargin() * 100)));
		add(paperColor = new SelectColor(panel,Translator.get("paper color"),robot.getSettings().getPaperColor()));
		finish();
		updateValues();
	}
	
	void updateValues() {
		double w = robot.getSettings().getPaperWidth();
		double h = robot.getSettings().getPaperHeight();

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
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		super.update(o, arg);

		if(beingModified) return;
		beingModified=true;
		
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
			float sw = pw.getValue();
			float sh = ph.getValue();
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
			MakelangeloRobotSettings s = robot.getSettings();
			s.setPaperSize(pwf,phf,shiftxf,shiftyf);
			s.setRotation(rot);
			s.setPaperColor(paperColor.getColor());

			double pm = (100 - paperMargin.getValue()) * 0.01;
			s.setPaperMargin(pm);
		}
	}
}
