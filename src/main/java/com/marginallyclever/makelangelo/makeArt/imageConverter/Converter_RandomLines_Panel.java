package com.marginallyclever.makelangelo.makeArt.imageConverter;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectInteger;

public class Converter_RandomLines_Panel extends ImageConverterPanel {
	private static final long serialVersionUID = 1L;
	public Converter_RandomLines_Panel(Converter_RandomLines converter) {
		super(converter);
		add(new SelectInteger("total",Translator.get("ConverterRandomLinesCount"),converter.getLineCount()));
	}
}
