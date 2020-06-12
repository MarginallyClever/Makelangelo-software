package com.marginallyclever.artPipeline.converters;

import java.util.Observable;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectSlider;

public class Converter_Crosshatch_Panel extends ImageConverterPanel {
	private Converter_Crosshatch converter;
	private	SelectSlider intensityField;
	
	public Converter_Crosshatch_Panel(Converter_Crosshatch arg0) {
		super();
		converter=arg0;
		
		add(intensityField = new SelectSlider(Translator.get("ConverterIntensity"),200,1,(int)(converter.getIntensity()*10.0)));
		finish();
	}


	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);
		
		converter.setIntensity((float)intensityField.getValue()/10.0f);
		converter.restart();
	}
}
