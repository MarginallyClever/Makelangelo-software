package com.marginallyclever.artPipeline.converters;

import java.util.Observable;

import com.marginallyclever.makelangelo.select.SelectFloat;

public class Converter_SpiralPulse_Panel extends ImageConverterPanel {
	Converter_SpiralPulse converter;
	
	SelectFloat intensityField;	
	SelectFloat heightField;	
	SelectFloat spacingField;	
	
	public Converter_SpiralPulse_Panel(Converter_SpiralPulse arg0) {
		super();
		
		converter=arg0;

		add(intensityField = new SelectFloat("ConverterIntensity",converter.getIntensity()));
		add(spacingField = new SelectFloat("SpiralPulseSpacing",converter.getSpacing()));
		add(heightField = new SelectFloat("SpiralPulseHeight",converter.getHeight()));
	}

	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);
		
		converter.setIntensity(intensityField.getValue());
		converter.setSpacing(spacingField.getValue());
		converter.setHeight(heightField.getValue());
		if(loadAndSaveImage!=null) loadAndSaveImage.reconvert();
	}
}
