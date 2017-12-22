package com.marginallyclever.makelangeloRobot.converters;

import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import com.marginallyclever.makelangelo.SelectFloat;
import com.marginallyclever.makelangelo.SelectInteger;
import com.marginallyclever.makelangelo.Translator;

public class Converter_Multipass_Panel extends ImageConverterPanel implements PropertyChangeListener {
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

		setLayout(new GridLayout(0,1));
		this.add(new JLabel(Translator.get("ConverterMultipassAngle")));
		this.add(angleField);
		this.add(new JLabel(Translator.get("ConverterMultipassLevels")));
		this.add(passesField);
		
		angleField.addPropertyChangeListener("value",this);
		passesField.addPropertyChangeListener("value",this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		converter.setAngle(((Number)angleField.getValue()).floatValue());
		converter.setPasses(((Number)passesField.getValue()).intValue());
		converter.reconvert();
	}
}
