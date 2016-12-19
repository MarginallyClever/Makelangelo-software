package com.marginallyclever.makelangeloRobot.converters;

import java.awt.GridLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.marginallyclever.makelangelo.SelectInteger;
import com.marginallyclever.makelangelo.Translator;

public class Converter_Sandy_Panel extends JPanel implements DocumentListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Converter_Sandy converter;
	
	SelectInteger sizeField;	
	JComboBox<String> direction_choices;
	
	public Converter_Sandy_Panel(Converter_Sandy arg0) {
		this.converter=arg0;

		this.setLayout(new GridLayout(0, 1));

		sizeField = new SelectInteger(converter.getScale());

		this.setLayout(new GridLayout(0,1));
		this.add(new JLabel(Translator.get("SandyRings")));
		this.add(sizeField);

		direction_choices = new JComboBox<>(converter.getDirections());
		direction_choices.setSelectedIndex(converter.getDirectionIndex());
		this.add(direction_choices);
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
		converter.setScale(((Number)sizeField.getValue()).intValue());
	}
}
