package com.marginallyclever.artPipeline.converters;

import java.util.Observable;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectInteger;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;

public class Converter_CMYK_Panel extends ImageConverterPanel {
	private Converter_CMYK converter;
	private SelectInteger passesField;
	
	public Converter_CMYK_Panel(Converter_CMYK arg0) {
		super();
		converter=arg0;
		
		add(passesField = new SelectInteger(Translator.get("ConverterCMYKPasses"),converter.getPasses()));
		add(new SelectReadOnlyText(Translator.get("ConverterCMYKNote")));
		finish();
	}

	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);
		
		converter.setPasses(passesField.getValue());
		converter.restart();
	}
}
