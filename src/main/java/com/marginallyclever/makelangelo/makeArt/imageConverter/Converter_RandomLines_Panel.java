package com.marginallyClever.makelangelo.makeArt.imageConverter;

import com.marginallyClever.makelangelo.Translator;
import com.marginallyClever.makelangelo.select.SelectInteger;

public class Converter_RandomLines_Panel extends ImageConverterPanel {
	private static final long serialVersionUID = 1L;
	public Converter_RandomLines_Panel(Converter_RandomLines converter) {
		super(converter);
		add(new SelectInteger("total",Translator.get("ConverterRandomLinesCount"),converter.getLineCount()));
	}
}
