package com.marginallyclever.artPipeline.converters;

import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectFloat;

public class Converter_SpiralPulse_Panel extends ImageConverterPanel implements PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Converter_SpiralPulse converter;
	
	SelectFloat intensityField;	
	SelectFloat heightField;	
	SelectFloat spacingField;	
	
	public Converter_SpiralPulse_Panel(Converter_SpiralPulse arg0) {
		this.converter=arg0;

		this.setLayout(new GridLayout(0, 1));

		this.setLayout(new GridLayout(0,1));
		this.add(new JLabel(Translator.get("ConverterIntensity")));
		this.add(intensityField = new SelectFloat(converter.getIntensity()));
		this.add(new JLabel(Translator.get("SpiralPulseSpacing")));
		this.add(spacingField = new SelectFloat(converter.getSpacing()));
		this.add(new JLabel(Translator.get("SpiralPulseHeight")));
		this.add(heightField = new SelectFloat(converter.getHeight()));
		
		intensityField.addPropertyChangeListener("value",this);
		spacingField.addPropertyChangeListener("value",this);
		heightField.addPropertyChangeListener("value",this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		converter.setIntensity(((Number)intensityField.getValue()).floatValue());
		converter.setSpacing(((Number)spacingField.getValue()).floatValue());
		converter.setHeight(((Number)heightField.getValue()).floatValue());
		if(loadAndSaveImage!=null) loadAndSaveImage.reconvert();
	}
}
