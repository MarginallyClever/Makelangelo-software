package com.marginallyclever.makelangelo.makeArt.imageConverter;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectSlider;

public class Converter_Crosshatch_Panel extends ImageConverterPanel {
	private static final long serialVersionUID = 1L;
	
	public Converter_Crosshatch_Panel(Converter_Crosshatch converter) {
		super(converter);
		add(new SelectSlider("intensity",Translator.get("ConverterIntensity"),100,1,(int)(converter.getIntensity()*10.0)));
		add(new SelectSlider("pass90",Translator.get("pass90"),256,0,(int)converter.getPass90()));
		add(new SelectSlider("pass75",Translator.get("pass75"),256,0,(int)converter.getPass75()));
		add(new SelectSlider("pass15",Translator.get("pass15"),256,0,(int)converter.getPass15()));
		add(new SelectSlider("pass45",Translator.get("pass45"),256,0,(int)converter.getPass45()));
	}
}
