package com.marginallyclever.makelangelo.makeArt.imageConverter;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectDouble;
import com.marginallyclever.makelangelo.select.SelectOneOfMany;

public class Converter_Pulse_Panel extends ImageConverterPanel {
	private static final long serialVersionUID = 1L;
	
	public Converter_Pulse_Panel(Converter_Pulse converter) {
		super(converter);
		add(new SelectDouble("size",Translator.get("HilbertCurveSize"),converter.getScale()));
		add(new SelectOneOfMany("direction",Translator.get("Direction"),converter.getDirections(),converter.getDirectionIndex()));
	}
}
