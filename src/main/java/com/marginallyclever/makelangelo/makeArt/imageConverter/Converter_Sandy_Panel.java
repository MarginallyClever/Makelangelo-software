package com.marginallyClever.makelangelo.makeArt.imageConverter;

import com.marginallyClever.makelangelo.Translator;
import com.marginallyClever.makelangelo.select.SelectOneOfMany;
import com.marginallyClever.makelangelo.select.SelectSlider;

public class Converter_Sandy_Panel extends ImageConverterPanel {
	private static final long serialVersionUID = 1L;
	
	public Converter_Sandy_Panel(Converter_Sandy converter) {
		super(converter);
		add(new SelectSlider("rings",Translator.get("SandyNoble.rings"),300,10,converter.getScale()));
		add(new SelectOneOfMany("direction",Translator.get("SandyNoble.center"),converter.getDirections(),converter.getDirectionIndex()));
	}
}
