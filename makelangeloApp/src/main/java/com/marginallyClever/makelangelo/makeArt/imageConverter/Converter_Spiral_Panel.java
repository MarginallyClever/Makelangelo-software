package com.marginallyClever.makelangelo.makeArt.imageConverter;

import com.marginallyClever.makelangelo.Translator;
import com.marginallyClever.makelangelo.select.SelectBoolean;

public class Converter_Spiral_Panel extends ImageConverterPanel {
	private static final long serialVersionUID = 1L;

	public Converter_Spiral_Panel(Converter_Spiral converter) {
		super(converter);
		add(new SelectBoolean("toCorners", Translator.get("Spiral.toCorners"),converter.getToCorners()));
	}
}
