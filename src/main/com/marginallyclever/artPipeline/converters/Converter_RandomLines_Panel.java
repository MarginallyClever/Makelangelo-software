package com.marginallyclever.artPipeline.converters;

import java.util.Observable;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectInteger;

public class Converter_RandomLines_Panel extends ImageConverterPanel {
	private Converter_RandomLines converter;
	private SelectInteger sizeField;	
	
	public Converter_RandomLines_Panel(Converter_RandomLines arg0) {
		super();
		converter=arg0;
		
		add(sizeField = new SelectInteger(Translator.get("ConverterRandomLinesCount"),converter.getLineCount()));
		finish();
	}

	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);
		
		converter.setLineCount(sizeField.getValue());
		converter.restart();
	}
}
