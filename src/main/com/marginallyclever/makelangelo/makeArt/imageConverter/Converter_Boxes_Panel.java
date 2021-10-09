package com.marginallyclever.makelangelo.makeArt.imageConverter;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectSlider;

public class Converter_Boxes_Panel extends ImageConverterPanel {
	private static final long serialVersionUID = 1L;
	
	public Converter_Boxes_Panel(Converter_Boxes converter) {
		super(converter);
		add(new SelectSlider("size",Translator.get("BoxGeneratorMaxSize"),40,1,converter.getBoxMasSize()));
		add(new SelectSlider("cutoff",Translator.get("BoxGeneratorCutoff"),255,0,converter.getCutoff()));
		finish();
	}
}
