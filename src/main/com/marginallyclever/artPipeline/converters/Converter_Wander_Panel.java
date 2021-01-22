package com.marginallyclever.artPipeline.converters;

import java.beans.PropertyChangeEvent;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectBoolean;
import com.marginallyclever.makelangelo.select.SelectInteger;

public class Converter_Wander_Panel extends ImageConverterPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Converter_Wander converter;
	private SelectInteger sizeField;
	private SelectBoolean cmykField;
	
	public Converter_Wander_Panel(Converter_Wander arg0) {
		super();
		
		converter=arg0;

		add(sizeField = new SelectInteger(Translator.get("ConverterWanderLineCount"),converter.getLineCount()));
		add(cmykField = new SelectBoolean(Translator.get("ConverterWanderCMYK"),converter.isCMYK()));
		finish();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		
		converter.setLineCount(sizeField.getValue());
		converter.setCMYK(cmykField.isSelected());
		converter.restart();
	}	
}
