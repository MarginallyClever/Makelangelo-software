package com.marginallyclever.makelangeloRobot.converters;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.marginallyclever.makelangelo.SelectFloat;
import com.marginallyclever.makelangelo.SelectInteger;
import com.marginallyclever.makelangelo.Translator;

public class Converter_Multipass_Panel extends JPanel implements DocumentListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Converter_Multipass converter;
	
	SelectFloat   angleField;
	SelectInteger passesField;
	
	public Converter_Multipass_Panel(Converter_Multipass arg0) {
		this.converter=arg0;
		
		angleField = new SelectFloat(converter.getAngle());
		passesField = new SelectInteger(converter.getPasses());

		JPanel panel = new JPanel(new GridLayout(0,1));
		panel.add(new JLabel(Translator.get("ConverterMultipassAngle")));
		panel.add(angleField);
		panel.add(new JLabel(Translator.get("ConverterMultipassLevels")));
		panel.add(passesField);
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
		converter.setAngle(((Number)angleField.getValue()).floatValue());
		converter.setPasses(((Number)passesField.getValue()).intValue());
	}
}
