package com.marginallyclever.makelangeloRobot.converters;

import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.marginallyclever.makelangelo.SelectInteger;
import com.marginallyclever.makelangelo.Translator;

public class Converter_RandomLines_Panel extends JPanel implements PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Converter_RandomLines converter;
	
	SelectInteger   sizeField;	
	
	public Converter_RandomLines_Panel(Converter_RandomLines arg0) {
		this.converter=arg0;

		this.setLayout(new GridLayout(0, 1));
		this.add(new JLabel(Translator.get("ConverterRandomLinesLineCount")));
		this.add(sizeField = new SelectInteger(converter.getLineCount()));

		sizeField.addPropertyChangeListener("value",this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		converter.setLineCount(((Number)sizeField.getValue()).intValue());
		converter.reconvert();
	}
}
