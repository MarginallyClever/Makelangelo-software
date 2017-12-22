package com.marginallyclever.makelangeloRobot.converters;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import com.marginallyclever.makelangelo.SelectInteger;
import com.marginallyclever.makelangelo.Translator;

public class Converter_Sandy_Panel extends ImageConverterPanel implements PropertyChangeListener, ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Converter_Sandy converter;
	
	SelectInteger sizeField;	
	JComboBox<String> directionChoices;
	
	public Converter_Sandy_Panel(Converter_Sandy arg0) {
		this.converter=arg0;

		this.setLayout(new GridLayout(0, 1));

		sizeField = new SelectInteger(converter.getScale());

		this.setLayout(new GridLayout(0,1));
		this.add(new JLabel(Translator.get("SandyRings")));
		this.add(sizeField);

		directionChoices = new JComboBox<>(converter.getDirections());
		this.add(directionChoices);
		directionChoices.setSelectedIndex(converter.getDirectionIndex());
		
		sizeField.addPropertyChangeListener("value",this);
		directionChoices.addActionListener(this);
	}

	private void validateInput() {
		converter.setScale(((Number)sizeField.getValue()).intValue());
		converter.setDirection(directionChoices.getSelectedIndex());
		converter.reconvert();
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		validateInput();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		validateInput();
	}
}
