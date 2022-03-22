package com.marginallyclever.makelangelo.makeart.imageConverter;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectDouble;
import com.marginallyclever.makelangelo.select.SelectOneOfMany;

/**
 * GUI for {@link Converter_Moire}
 * @author Dan Royer
 *
 */
public class Converter_Moire_Panel extends ImageConverterPanel {
	private static final long serialVersionUID = 1L;
	
	public Converter_Moire_Panel(Converter_Moire converter) {
		super(converter);
		add(new SelectDouble("size",Translator.get("HilbertCurveSize"),converter.getScale()));
		add(new SelectOneOfMany("direction",Translator.get("Direction"),converter.getDirections(),converter.getDirectionIndex()));
	}
}
