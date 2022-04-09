package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectDouble;
import com.marginallyclever.makelangelo.select.SelectOneOfMany;
import com.marginallyclever.makelangelo.select.SelectSlider;

public class Converter_Pulse_Panel extends ImageConverterPanel {
	private static final long serialVersionUID = 1L;
	
	public Converter_Pulse_Panel(Converter_Pulse converter) {
		super(converter);
		add(new SelectDouble("size",Translator.get("HilbertCurveSize"),converter.getScale()));
		add(new SelectOneOfMany("direction",Translator.get("Direction"),converter.getDirections(),converter.getDirectionIndex()));
		add(new SelectSlider("cutoff",Translator.get("Converter_VoronoiStippling.Cutoff"),255,0,converter.getCutoff()));
	}
}
