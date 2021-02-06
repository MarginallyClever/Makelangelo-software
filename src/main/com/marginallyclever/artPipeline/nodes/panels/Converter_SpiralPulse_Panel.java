package com.marginallyclever.artPipeline.nodes.panels;

import java.beans.PropertyChangeEvent;

import com.marginallyclever.artPipeline.NodePanel;
import com.marginallyclever.artPipeline.nodes.Converter_SpiralPulse;
import com.marginallyclever.makelangelo.select.SelectFloat;

/**
 * GUI for {@link Converter_SpiralPulse}
 * @author Dan Royer
 *
 */
public class Converter_SpiralPulse_Panel extends NodePanel {
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
