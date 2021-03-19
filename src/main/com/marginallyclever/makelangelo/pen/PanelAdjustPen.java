package com.marginallyclever.makelangelo.pen;

import com.marginallyclever.core.Translator;
import com.marginallyclever.core.select.SelectColor;
import com.marginallyclever.core.select.SelectDouble;
import com.marginallyclever.core.select.SelectPanel;


public class PanelAdjustPen extends SelectPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected SelectDouble penDiameter;
	protected SelectColor selectPenDownColor;
	protected SelectColor selectPenUpColor;

	private Pen myPen;
	
	public PanelAdjustPen(Pen pen) {
		super();
		myPen=pen;
	    
	    add(penDiameter = new SelectDouble(Translator.get("penToolDiameter"),myPen.getDiameter()));
		add(selectPenDownColor = new SelectColor(interiorPanel,Translator.get("pen down color"),myPen.getDownColor()));
		add(selectPenUpColor = new SelectColor(interiorPanel,Translator.get("pen up color"),myPen.getUpColor()));
		
		finish();
	}	
	
	public void save() {
		myPen.setDiameter(penDiameter.getValue());
		myPen.setDownColor(selectPenDownColor.getColor());
		myPen.setUpColor(selectPenUpColor.getColor());
	}
}
