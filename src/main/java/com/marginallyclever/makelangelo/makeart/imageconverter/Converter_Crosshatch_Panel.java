package com.marginallyclever.makelangelo.makeart.imageconverter;

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

		addPropertyChangeListener((evt)->{
			if(evt.getPropertyName().equals("intensity")) {
				converter.setIntensity((float)((int)evt.getNewValue())/10.0f);
			}
			if(evt.getPropertyName().equals("pass90")) converter.setPass90((int)evt.getNewValue());
			if(evt.getPropertyName().equals("pass75")) converter.setPass75((int)evt.getNewValue());
			if(evt.getPropertyName().equals("pass15")) converter.setPass15((int)evt.getNewValue());
			if(evt.getPropertyName().equals("pass45")) converter.setPass45((int)evt.getNewValue());
			fireRestartConversion();
		});
	}
}
