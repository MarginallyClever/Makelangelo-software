package com.marginallyclever.artPipeline.converters;

import java.beans.PropertyChangeEvent;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectInteger;

public class Converter_RandomLines_Panel extends ImageConverterPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Converter_RandomLines converter;
	private SelectInteger sizeField;	
	
	public Converter_RandomLines_Panel(Converter_RandomLines arg0) {
		super();
		converter=arg0;
		
		add(sizeField = new SelectInteger(Translator.get("ConverterRandomLinesCount"),converter.getLineCount()));
		finish();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		
		converter.setLineCount(sizeField.getValue());
		converter.restart();
	}
}
