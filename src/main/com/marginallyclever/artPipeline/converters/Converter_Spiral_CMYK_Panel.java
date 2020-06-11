package com.marginallyclever.artPipeline.converters;

import java.util.Observable;

import com.marginallyclever.makelangelo.select.SelectBoolean;

public class Converter_Spiral_CMYK_Panel extends ImageConverterPanel {
	Converter_Spiral_CMYK converter;
	
	SelectBoolean toCornersField;
	
	public Converter_Spiral_CMYK_Panel(Converter_Spiral_CMYK arg0) {
		super();
		this.converter=arg0;
		
		this.add(toCornersField = new SelectBoolean("SpiralToCorners",converter.getToCorners()));
	}

	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);

		if(loadAndSaveImage!=null) loadAndSaveImage.reconvert();
	}
}
