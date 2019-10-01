package com.marginallyclever.makelangeloRobot.converters;

import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectInteger;

public class Converter_CMYK_Panel extends ImageConverterPanel implements PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Converter_CMYK converter;
	
	SelectInteger passesField;
	
	public Converter_CMYK_Panel(Converter_CMYK arg0) {
		this.converter=arg0;
		
		passesField = new SelectInteger(converter.getPasses());

		setLayout(new GridLayout(4,1));
		this.add(new JLabel(Translator.get("ConverterCMYKPasses")));
		this.add(passesField);
		this.add(new JLabel(Translator.get("ConverterCMYKNote")));
		
		passesField.addPropertyChangeListener("value",this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		converter.setPasses(((Number)passesField.getValue()).intValue());
		if(loadAndSaveImage!=null) loadAndSaveImage.reconvert();
	}
}
