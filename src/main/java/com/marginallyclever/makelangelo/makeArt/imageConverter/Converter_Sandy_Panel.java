package com.marginallyclever.makelangelo.makeArt.imageConverter;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectOneOfMany;
import com.marginallyclever.makelangelo.select.SelectSlider;

public class Converter_Sandy_Panel extends ImageConverterPanel {
	private static final long serialVersionUID = 1L;
	
	public Converter_Sandy_Panel(Converter_Sandy converter) {
		super(converter);
		add(new SelectSlider("rings",Translator.get("SandyNoble.rings"),300,10,converter.getScale()));
		add(new SelectOneOfMany("direction",Translator.get("SandyNoble.center"),converter.getDirections(),converter.getDirectionIndex()));
	}
}
