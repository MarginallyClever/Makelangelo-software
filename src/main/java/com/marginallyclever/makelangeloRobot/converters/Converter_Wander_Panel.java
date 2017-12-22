package com.marginallyclever.makelangeloRobot.converters;

import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import com.marginallyclever.makelangelo.SelectInteger;
import com.marginallyclever.makelangelo.Translator;

public class Converter_Wander_Panel extends ImageConverterPanel implements PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Converter_Wander converter;
	
	SelectInteger   sizeField;	
	
	public Converter_Wander_Panel(Converter_Wander arg0) {
		this.converter=arg0;

		this.setLayout(new GridLayout(0, 1));
		this.add(new JLabel(Translator.get("ConverterWanderLineCount")));
		this.add(sizeField = new SelectInteger(converter.getLineCount()));

		sizeField.addPropertyChangeListener("value",this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		converter.setLineCount(((Number)sizeField.getValue()).intValue());
		converter.reconvert();
	}
}
