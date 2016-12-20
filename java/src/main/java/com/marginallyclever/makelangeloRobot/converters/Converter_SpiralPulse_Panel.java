package com.marginallyclever.makelangeloRobot.converters;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.marginallyclever.makelangelo.SelectFloat;
import com.marginallyclever.makelangelo.Translator;

public class Converter_SpiralPulse_Panel extends JPanel implements DocumentListener {
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
		this.add(new JLabel(Translator.get("SpiralPulseIntensity")));
		this.add(intensityField = new SelectFloat(converter.getIntensity()));
		this.add(new JLabel(Translator.get("SpiralPulseSpacing")));
		this.add(spacingField = new SelectFloat(converter.getSpacing()));
		this.add(new JLabel(Translator.get("SpiralPulseHeight")));
		this.add(heightField = new SelectFloat(converter.getHeight()));
	}

	@Override
	public void changedUpdate(DocumentEvent arg0) {
		validateInput();
	}

	@Override
	public void insertUpdate(DocumentEvent arg0) {
		validateInput();
	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {
		validateInput();
	}

	private void validateInput() {
		converter.setIntensity(((Number)intensityField.getValue()).floatValue());
		converter.setSpacing(((Number)spacingField.getValue()).floatValue());
		converter.setHeight(((Number)heightField.getValue()).floatValue());
	}
}
