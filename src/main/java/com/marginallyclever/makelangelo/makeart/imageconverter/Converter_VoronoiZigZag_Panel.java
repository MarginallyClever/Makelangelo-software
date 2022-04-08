package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectDouble;
import com.marginallyclever.makelangelo.select.SelectInteger;

public class Converter_VoronoiZigZag_Panel extends ImageConverterPanel {
	private static final long serialVersionUID = -5791313991426136610L;
	
	public Converter_VoronoiZigZag_Panel(Converter_VoronoiZigZag converter) {
		super(converter);
		
		add(new SelectInteger("count", Translator.get("Converter_VoronoiStippling.CellCount"),converter.getNumCells()));
		add(new SelectDouble("min",Translator.get("Converter_VoronoiStippling.DotMin"),converter.getMinDotSize()));
	}
}
