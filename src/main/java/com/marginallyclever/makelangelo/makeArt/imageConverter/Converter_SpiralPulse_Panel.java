package com.marginallyClever.makelangelo.makeArt.imageConverter;

import com.marginallyClever.makelangelo.Translator;
import com.marginallyClever.makelangelo.select.SelectDouble;

public class Converter_SpiralPulse_Panel extends ImageConverterPanel {
	private static final long serialVersionUID = 1L;
	
	public Converter_SpiralPulse_Panel(Converter_SpiralPulse converter) {
		super(converter);
		add(new SelectDouble("intensity", Translator.get("SpiralPulse.intensity"),converter.getIntensity()));
		add(new SelectDouble("spacing",Translator.get("SpiralPulse.spacing"),converter.getSpacing()));
		add(new SelectDouble("height",Translator.get("SpiralPulse.height"),converter.getHeight()));
	}
}
