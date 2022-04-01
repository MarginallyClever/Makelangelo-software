package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectSlider;

public class Converter_EdgeDetection_Panel extends ImageConverterPanel {
	private static final long serialVersionUID = 1L;

	public Converter_EdgeDetection_Panel(Converter_EdgeDetection converter) {
		super(converter);
		add(new SelectSlider("passes",Translator.get("Converter_EdgeDetection.passes"),20,1,(int)(converter.getPasses())));
		add(new SelectSlider("stepSize",Translator.get("Converter_EdgeDetection.stepSize"),25,2,(int)converter.getStepSize()));
		add(new SelectSlider("sampleSize",Translator.get("Converter_EdgeDetection.sampleSize"),5,1,(int)converter.getSampleSize()));
	}
}
