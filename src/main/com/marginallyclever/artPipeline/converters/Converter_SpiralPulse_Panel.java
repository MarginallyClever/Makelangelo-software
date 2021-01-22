package com.marginallyclever.artPipeline.converters;

import java.beans.PropertyChangeEvent;

import com.marginallyclever.makelangelo.select.SelectFloat;

public class Converter_SpiralPulse_Panel extends ImageConverterPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Converter_SpiralPulse converter;
	private SelectFloat intensityField;	
	private SelectFloat heightField;	
	private SelectFloat spacingField;	
	
	public Converter_SpiralPulse_Panel(Converter_SpiralPulse arg0) {
		super();
		
		converter=arg0;

		add(intensityField = new SelectFloat("ConverterIntensity",converter.getIntensity()));
		add(spacingField = new SelectFloat("SpiralPulseSpacing",converter.getSpacing()));
		add(heightField = new SelectFloat("SpiralPulseHeight",converter.getHeight()));
		finish();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		
		converter.setIntensity(intensityField.getValue());
		converter.setSpacing(spacingField.getValue());
		converter.setHeight(heightField.getValue());
		converter.restart();
	}
}
