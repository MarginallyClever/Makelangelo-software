package com.marginallyClever.makelangelo.makeArt.imageConverter;

import com.marginallyClever.makelangelo.Translator;
import com.marginallyClever.makelangelo.select.SelectBoolean;
import com.marginallyClever.makelangelo.select.SelectInteger;

public class Converter_Wander_Panel extends ImageConverterPanel {
	private static final long serialVersionUID = 1L;
	
	public Converter_Wander_Panel(Converter_Wander converter) {
		super(converter);
		add(new SelectInteger("count",Translator.get("ConverterWanderLineCount"),converter.getLineCount()));
		add(new SelectBoolean("cmyk",Translator.get("ConverterWanderCMYK"),converter.isCMYK()));
	}
}
