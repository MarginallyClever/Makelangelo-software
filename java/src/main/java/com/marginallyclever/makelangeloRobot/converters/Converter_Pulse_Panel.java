package com.marginallyclever.makelangeloRobot.converters;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.marginallyclever.makelangelo.SelectFloat;
import com.marginallyclever.makelangelo.Translator;

public class Converter_Pulse_Panel extends JPanel implements PropertyChangeListener, ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Converter_Pulse converter;
	
	SelectFloat   sizeField;	
	JComboBox<String> directionChoices;
	
	public Converter_Pulse_Panel(Converter_Pulse arg0) {
		this.converter=arg0;

		this.setLayout(new GridLayout(0, 1));
		this.add(new JLabel(Translator.get("HilbertCurveSize")));
		this.add(sizeField = new SelectFloat(converter.getScale()));

		this.add(directionChoices = new JComboBox<>(converter.getDirections()));
		directionChoices.setSelectedIndex(converter.getDirectionIndex());
		
		sizeField.addPropertyChangeListener("value",this);
		directionChoices.addActionListener(this);
	}

	private void validateInput() {
		converter.setScale(((Number)sizeField.getValue()).floatValue());
		converter.setDirectionIndex(directionChoices.getSelectedIndex());
		converter.reconvert();
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		validateInput();		
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		validateInput();
	}
}
