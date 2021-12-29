package com.marginallyclever.makelangelo.makeArt.imageConverter;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectBoolean;
import com.marginallyclever.makelangelo.select.SelectInteger;

public class Converter_Wander_Panel extends ImageConverterPanel {
	private static final long serialVersionUID = 1L;
	
	public Converter_Wander_Panel(Converter_Wander converter) {
		super(converter);
		add(new SelectInteger("count",Translator.get("ConverterWanderLineCount"),converter.getLineCount()));
		add(new SelectBoolean("cmyk",Translator.get("ConverterWanderCMYK"),converter.isCMYK()));
	}
}
