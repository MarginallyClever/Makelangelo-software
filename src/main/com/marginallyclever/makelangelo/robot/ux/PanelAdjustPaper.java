package com.marginallyclever.makelangelo.robot.ux;

import java.beans.PropertyChangeEvent;

import com.marginallyclever.core.Translator;
import com.marginallyclever.core.select.SelectBoolean;
import com.marginallyclever.core.select.SelectColor;
import com.marginallyclever.core.select.SelectDouble;
import com.marginallyclever.core.select.SelectOneOfMany;
import com.marginallyclever.core.select.SelectPanel;
import com.marginallyclever.core.select.SelectSlider;
import com.marginallyclever.makelangelo.robot.Paper;
import com.marginallyclever.makelangelo.robot.PaperSize;

/**
 * UX to adjust the size of the {@link Paper} known to a {@link RobotController}
 * @author Dan Royer
 * @since before 7.25.0
 *
 */
public class PanelAdjustPaper extends SelectPanel {
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

	public PanelAdjustPaper(Paper paper) {
		myPaper = paper;
		
		beingModified=false;

		// common paper sizes
		String[] commonPaperNames = new String[Paper.commonPaperSizes.length+1];
		commonPaperNames[0]="---";
		int i;
		for(i=0;i<Paper.commonPaperSizes.length;++i) {
			commonPaperNames[i+1] = Paper.commonPaperSizes[i].toString();
		}
		
		add(paperSizes = new SelectOneOfMany(Translator.get("PaperSize"),commonPaperNames,0));
		double top = myPaper.getTop();
		double bot = myPaper.getBottom();
		double left = myPaper.getLeft();
		double right = myPaper.getRight();
		double rot = myPaper.getRotation();
		add(pw = new SelectDouble(Translator.get("PaperWidth"),(float)(right-left)));
		add(ph = new SelectDouble(Translator.get("PaperHeight"),(float)(top-bot))); 
		add(sx = new SelectDouble("Shift X",(float)(left+right)/2.0f)); 
		add(sy = new SelectDouble("Shift y",(float)(top+bot)/2.0f)); 
		add(ang = new SelectDouble("Rotation",(float)rot));
		add(isLandscape = new SelectBoolean("\u21cb",false));
		add(paperMargin = new SelectSlider(Translator.get("PaperMargin"),50,0,100 - (int) (myPaper.getMarginPercent() * 100)));
		add(paperColor = new SelectColor(interiorPanel,Translator.get("paper color"),myPaper.getPaperColor()));
		finish();
		updateValues();
	}
	
	void updateValues() {
		double w = myPaper.getWidth();
		double h = myPaper.getHeight();

		int i = myPaper.getCurrentPaperSizeChoice( w, h );
		if(i!=0) {
			isLandscape.setSelected(true);
		} else {
			i = myPaper.getCurrentPaperSizeChoice( w, h );
			isLandscape.setSelected(false);
		}
		paperSizes.setSelectedIndex(i);
		pw.setValue((float)w);
		ph.setValue((float)h);
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
				PaperSize s = Paper.commonPaperSizes[selectedIndex-1];
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

			int i = myPaper.getCurrentPaperSizeChoice( h, w );
			if(i!=0) {
				isLandscape.setSelected(true);
			} else {
				i = myPaper.getCurrentPaperSizeChoice( w, h );
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
}
