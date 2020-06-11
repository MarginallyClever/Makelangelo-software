package com.marginallyclever.artPipeline.converters;

import java.util.Observable;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectFloat;
import com.marginallyclever.makelangelo.select.SelectInteger;

public class Converter_Multipass_Panel extends ImageConverterPanel {
	Converter_Multipass converter;
	
	SelectFloat   angleField;
	SelectInteger passesField;
	
	public Converter_Multipass_Panel(Converter_Multipass arg0) {
		super();
		this.converter=arg0;
		
		add(angleField = new SelectFloat(Translator.get("ConverterMultipassAngle"),converter.getAngle()));
		add(passesField = new SelectInteger(Translator.get("ConverterMultipassLevels"),converter.getPasses()));
	}

	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);
		
		converter.setAngle(angleField.getValue());
		converter.setPasses(passesField.getValue());
		if(loadAndSaveImage!=null) loadAndSaveImage.reconvert();
	}
}
