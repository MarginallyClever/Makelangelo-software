package com.marginallyClever.makelangelo.makeArt.imageConverter;

import com.marginallyClever.makelangelo.Translator;
import com.marginallyClever.makelangelo.select.SelectBoolean;

public class Converter_Spiral_CMYK_Panel extends ImageConverterPanel {
	private static final long serialVersionUID = 1L;
	
	public Converter_Spiral_CMYK_Panel(Converter_Spiral_CMYK converter) {
		super(converter);
		add(new SelectBoolean("toCorners", Translator.get("Spiral.toCorners"),converter.getToCorners()));
	}
}
