package com.marginallyclever.artPipeline.converters;

import java.beans.PropertyChangeEvent;

import com.marginallyclever.makelangelo.select.SelectDouble;

/**
 * GUI for {@link Converter_SpiralPulse}
 * @author Dan Royer
 *
 */
public class Converter_SpiralPulse_Panel extends ImageConverterPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Converter_SpiralPulse converter;
	private SelectDouble intensityField;	
	private SelectDouble heightField;	
	private SelectDouble spacingField;	
	
	public Converter_SpiralPulse_Panel(Converter_SpiralPulse arg0) {
		super();
		
		converter=arg0;

		add(intensityField = new SelectDouble("ConverterIntensity",converter.getIntensity()));
		add(spacingField = new SelectDouble("SpiralPulseSpacing",converter.getSpacing()));
		add(heightField = new SelectDouble("SpiralPulseHeight",converter.getHeight()));
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
