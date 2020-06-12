package com.marginallyclever.artPipeline.converters;

import java.util.Observable;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectFloat;
import com.marginallyclever.makelangelo.select.SelectOneOfMany;

public class Converter_Moire_Panel extends ImageConverterPanel {
	private Converter_Moire converter;
	private SelectFloat scaleField;	
	private SelectOneOfMany directionChoices;
	
	public Converter_Moire_Panel(Converter_Moire arg0) {
		super();
		converter=arg0;

		add(scaleField = new SelectFloat(Translator.get("HilbertCurveSize"),converter.getScale()));
		add(directionChoices = new SelectOneOfMany(Translator.get("Direction"),converter.getDirections(),converter.getDirectionIndex()));
		finish();
	}

	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		super.update(o, arg);
		
		converter.setScale(scaleField.getValue());
		converter.setDirectionIndex(directionChoices.getSelectedIndex());
		converter.restart();
	}
}
