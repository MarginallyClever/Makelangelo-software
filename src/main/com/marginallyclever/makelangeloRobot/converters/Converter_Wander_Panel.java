package com.marginallyclever.makelangeloRobot.converters;

import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;

import com.marginallyclever.makelangelo.SelectBoolean;
import com.marginallyclever.makelangelo.SelectInteger;
import com.marginallyclever.makelangelo.Translator;

public class Converter_Wander_Panel extends ImageConverterPanel implements PropertyChangeListener, ItemListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Converter_Wander converter;
	
	SelectInteger   sizeField;
	SelectBoolean   cmykField;
	
	public Converter_Wander_Panel(Converter_Wander arg0) {
		this.converter=arg0;

		this.setLayout(new GridLayout(0, 1));

		this.add(new JLabel(Translator.get("ConverterWanderLineCount")));
		this.add(sizeField = new SelectInteger(converter.getLineCount()));

		this.add(new JLabel(Translator.get("ConverterWanderCMYK")));
		this.add(cmykField = new SelectBoolean(converter.isCMYK()));

		sizeField.addPropertyChangeListener("value",this);
		cmykField.addItemListener(this);
	}

	// int field
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		converter.setLineCount(((Number)sizeField.getValue()).intValue());
		converter.setCMYK(cmykField.getValue());
		if(loadAndSaveImage!=null) loadAndSaveImage.reconvert();
	}

	// checkbox
	@Override
	public void itemStateChanged(ItemEvent e) {
		converter.setLineCount(((Number)sizeField.getValue()).intValue());
		converter.setCMYK(cmykField.getValue());
		if(loadAndSaveImage!=null) loadAndSaveImage.reconvert();
	}
	
}
