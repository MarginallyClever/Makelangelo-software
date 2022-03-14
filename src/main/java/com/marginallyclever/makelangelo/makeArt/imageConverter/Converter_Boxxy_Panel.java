package com.marginallyClever.makelangelo.makeArt.imageConverter;

import com.marginallyClever.makelangelo.Translator;
import com.marginallyClever.makelangelo.select.SelectSlider;

public class Converter_Boxxy_Panel extends ImageConverterPanel {
	private static final long serialVersionUID = 1L;
	
	public Converter_Boxxy_Panel(Converter_Boxxy converter) {
		super(converter);
		add(new SelectSlider("size",Translator.get("BoxGeneratorMaxSize"),40,1,converter.getBoxMasSize()));
		add(new SelectSlider("cutoff",Translator.get("BoxGeneratorCutoff"),255,0,converter.getCutoff()));
	}
}
