package com.marginallyclever.makelangeloRobot.converters;

import java.awt.GridLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.marginallyclever.makelangelo.SelectFloat;
import com.marginallyclever.makelangelo.Translator;

public class Converter_Pulse_Panel extends JPanel implements DocumentListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Converter_Pulse converter;
	
	SelectFloat   sizeField;	
	JComboBox<String> direction_choices;
	
	public Converter_Pulse_Panel(Converter_Pulse arg0) {
		this.converter=arg0;

		this.setLayout(new GridLayout(0, 1));
		this.add(new JLabel(Translator.get("HilbertCurveSize")));
		this.add(sizeField = new SelectFloat(converter.getScale()));

		String[] directions = {Translator.get("horizontal"), Translator.get("vertical") };
		this.add(direction_choices = new JComboBox<>(directions));
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
		converter.setScale(((Number)sizeField.getValue()).floatValue());
	}
}
