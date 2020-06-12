package com.marginallyclever.artPipeline.converters;

import java.util.Observable;

import com.marginallyclever.makelangelo.select.SelectBoolean;

public class Converter_Spiral_Panel extends ImageConverterPanel {
	private Converter_Spiral converter;
	private SelectBoolean toCornersField;
	
	public Converter_Spiral_Panel(Converter_Spiral arg0) {
		super();
		
		converter=arg0;
		add(toCornersField = new SelectBoolean("SpiralToCorners",converter.getToCorners()));
		finish();
	}

	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);
		
		converter.setToCorners(toCornersField.isSelected());
		converter.restart();
	}
}
