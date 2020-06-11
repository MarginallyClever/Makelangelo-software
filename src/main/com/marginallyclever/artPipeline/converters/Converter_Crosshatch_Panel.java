package com.marginallyclever.artPipeline.converters;

import java.util.Observable;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectFloat;

public class Converter_Crosshatch_Panel extends ImageConverterPanel {
	private Converter_Crosshatch converter;
	private	SelectFloat intensityField;
	
	public Converter_Crosshatch_Panel(Converter_Crosshatch arg0) {
		super();
		converter=arg0;
		
		add(intensityField = new SelectFloat(Translator.get("ConverterIntensity"),converter.getIntensity()));
		finish();
	}


	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);
		
		converter.setIntensity(((Number)intensityField.getValue()).floatValue());
		if(loadAndSaveImage!=null) loadAndSaveImage.reconvert();
	}
}
