package com.marginallyclever.artPipeline.converters;

import java.util.Observable;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectFloat;

public class Converter_Crosshatch_Panel extends ImageConverterPanel {
	Converter_Crosshatch converter;
	
	SelectFloat intensityField;
	
	public Converter_Crosshatch_Panel(Converter_Crosshatch arg0) {
		super();
		this.converter=arg0;
		
		intensityField = new SelectFloat(Translator.get("ConverterIntensity"),converter.getIntensity());
		this.add(intensityField);
	}


	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);
		
		converter.setIntensity(((Number)intensityField.getValue()).floatValue());
		if(loadAndSaveImage!=null) loadAndSaveImage.reconvert();
	}
}
