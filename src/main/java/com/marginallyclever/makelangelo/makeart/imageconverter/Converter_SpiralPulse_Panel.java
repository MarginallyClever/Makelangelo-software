package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectDouble;

public class Converter_SpiralPulse_Panel extends ImageConverterPanel {
	private static final long serialVersionUID = 1L;
	
	public Converter_SpiralPulse_Panel(Converter_SpiralPulse converter) {
		super(converter);
		add(new SelectDouble("intensity", Translator.get("SpiralPulse.intensity"),converter.getIntensity()));
		add(new SelectDouble("spacing",Translator.get("SpiralPulse.spacing"),converter.getSpacing()));
		add(new SelectDouble("height",Translator.get("SpiralPulse.height"),converter.getHeight()));

		addPropertyChangeListener((evt)->{
			if(evt.getPropertyName().equals("intensity")) converter.setIntensity((double)evt.getNewValue());
			if(evt.getPropertyName().equals("spacing")) converter.setSpacing((double)evt.getNewValue());
			if(evt.getPropertyName().equals("height")) converter.setHeight((double)evt.getNewValue());
			fireRestartConversion();
		});
	}
}
