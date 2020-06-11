package com.marginallyclever.artPipeline.converters;

import java.util.Observable;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectFloat;
import com.marginallyclever.makelangelo.select.SelectOneOfMany;

public class Converter_Pulse_Panel extends ImageConverterPanel {
	private Converter_Pulse converter;
	private SelectFloat sizeField;	
	private SelectOneOfMany directionChoices;
	
	public Converter_Pulse_Panel(Converter_Pulse arg0) {
		super();
		this.converter=arg0;

		this.add(sizeField = new SelectFloat(Translator.get("HilbertCurveSize"),converter.getScale()));
		this.add(directionChoices = new SelectOneOfMany(Translator.get("Direction"),converter.getDirections(),converter.getDirectionIndex()));
	}

	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);
		
		converter.setScale(((Number)sizeField.getValue()).floatValue());
		converter.setDirectionIndex(directionChoices.getSelectedIndex());
		if(loadAndSaveImage!=null) loadAndSaveImage.reconvert();
	}
}
