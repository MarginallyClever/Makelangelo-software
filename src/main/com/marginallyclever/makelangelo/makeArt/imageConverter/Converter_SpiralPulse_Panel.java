package com.marginallyclever.makelangelo.makeArt.imageConverter;

import com.marginallyclever.makelangelo.select.SelectDouble;

public class Converter_SpiralPulse_Panel extends ImageConverterPanel {
	private static final long serialVersionUID = 1L;
	
	public Converter_SpiralPulse_Panel(Converter_SpiralPulse converter) {
		super(converter);
		add(new SelectDouble("intensity","ConverterIntensity",converter.getIntensity()));
		add(new SelectDouble("spacing","SpiralPulseSpacing",converter.getSpacing()));
		add(new SelectDouble("height","SpiralPulseHeight",converter.getHeight()));
		finish();
	}
}
