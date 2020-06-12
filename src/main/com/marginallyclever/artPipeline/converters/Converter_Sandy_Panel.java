package com.marginallyclever.artPipeline.converters;

import java.util.Observable;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectOneOfMany;
import com.marginallyclever.makelangelo.select.SelectSlider;

public class Converter_Sandy_Panel extends ImageConverterPanel {
	private Converter_Sandy converter;
	private SelectSlider sizeField;	
	private SelectOneOfMany directionChoices;
	
	public Converter_Sandy_Panel(Converter_Sandy arg0) {
		super();
		converter=arg0;

		add(sizeField = new SelectSlider(Translator.get("SandyRings"),300,10,converter.getScale()));
		add(directionChoices = new SelectOneOfMany(Translator.get("Direction"),converter.getDirections(),converter.getDirectionIndex()));
		finish();
	}

	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);

		converter.setScale(sizeField.getValue());
		converter.setDirection(directionChoices.getSelectedIndex());
		converter.restart();
	}
}
