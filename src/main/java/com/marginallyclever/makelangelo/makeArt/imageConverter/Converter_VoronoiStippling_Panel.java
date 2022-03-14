package com.marginallyclever.makelangelo.makeArt.imageConverter;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectBoolean;
import com.marginallyclever.makelangelo.select.SelectDouble;
import com.marginallyclever.makelangelo.select.SelectInteger;

public class Converter_VoronoiStippling_Panel extends ImageConverterPanel {
	private static final long serialVersionUID = 1L;
	
	public Converter_VoronoiStippling_Panel(Converter_VoronoiStippling converter) {
		super(converter);		
		add(new SelectInteger("cells",Translator.get("voronoiStipplingCellCount"),converter.getNumCells  ()));
		add(new SelectDouble("max",Translator.get("voronoiStipplingDotMax"),converter.getMaxDotSize   ()));
		add(new SelectDouble("min",Translator.get("voronoiStipplingDotMin"),converter.getMinDotSize   ()));
		add(new SelectBoolean("cutoff",Translator.get("voronoiStipplingCutoff"),converter.getDrawBorders()));
		add(new SelectDouble("drawBorder",Translator.get("voronoiStipplingDrawBorders"),converter.getCutoff  ()));
	}
}
