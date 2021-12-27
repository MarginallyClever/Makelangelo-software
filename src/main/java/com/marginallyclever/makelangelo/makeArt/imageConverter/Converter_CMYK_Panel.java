package com.marginallyclever.makelangelo.makeArt.imageConverter;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectInteger;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;

public class Converter_CMYK_Panel extends ImageConverterPanel {
	private static final long serialVersionUID = 1L;
	
	public Converter_CMYK_Panel(Converter_CMYK converter) {
		super(converter);
		add(new SelectInteger("passes",Translator.get("ConverterCMYKPasses"),converter.getPasses()));
		add(new SelectReadOnlyText("note",Translator.get("ConverterCMYKNote")));
	}
}
