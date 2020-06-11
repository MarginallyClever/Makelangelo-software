package com.marginallyclever.artPipeline.converters;

import java.util.Observable;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectInteger;
import com.marginallyclever.makelangelo.select.SelectOneOfMany;

public class Converter_Sandy_Panel extends ImageConverterPanel {
	private Converter_Sandy converter;
	private SelectInteger sizeField;	
	private SelectOneOfMany directionChoices;
	
	public Converter_Sandy_Panel(Converter_Sandy arg0) {
		super();
		converter=arg0;

		add(sizeField = new SelectInteger(Translator.get("SandyRings"),converter.getScale()));
		add(directionChoices = new SelectOneOfMany(Translator.get("Direction"),converter.getDirections(),converter.getDirectionIndex()));
		finish();
	}

	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);
		
		converter.setScale(((Number)sizeField.getValue()).intValue());
		converter.setDirection(directionChoices.getSelectedIndex());
		if(loadAndSaveImage!=null) loadAndSaveImage.reconvert();
	}
}
