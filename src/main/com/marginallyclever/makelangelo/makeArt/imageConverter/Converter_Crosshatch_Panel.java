package com.marginallyclever.makelangelo.makeArt.imageConverter;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectSlider;

public class Converter_Crosshatch_Panel extends ImageConverterPanel {
	private static final long serialVersionUID = 1L;
	
	public Converter_Crosshatch_Panel(Converter_Crosshatch converter) {
		super(converter);
		add(new SelectSlider("intensity",Translator.get("ConverterIntensity"),100,1,(int)(converter.getIntensity()*10.0)));
		finish();
	}
}
