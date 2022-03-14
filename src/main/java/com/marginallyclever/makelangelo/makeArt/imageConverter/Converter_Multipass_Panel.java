package com.marginallyclever.makelangelo.makeArt.imageConverter;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectDouble;
import com.marginallyclever.makelangelo.select.SelectInteger;

public class Converter_Multipass_Panel extends ImageConverterPanel {
	private static final long serialVersionUID = 1L;
	public Converter_Multipass_Panel(Converter_Multipass converter) {
		super(converter);
		add(new SelectDouble("angle",Translator.get("ConverterMultipassAngle"),converter.getAngle()));
		add(new SelectInteger("level",Translator.get("ConverterMultipassLevels"),converter.getPasses()));
	}
}
