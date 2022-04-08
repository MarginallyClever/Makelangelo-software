package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectBoolean;
import com.marginallyclever.makelangelo.select.SelectDouble;
import com.marginallyclever.makelangelo.select.SelectInteger;

public class Converter_VoronoiStippling_Panel extends ImageConverterPanel {
	private static final long serialVersionUID = 1L;
	
	public Converter_VoronoiStippling_Panel(Converter_VoronoiStippling converter) {
		super(converter);		
		add(new SelectInteger("cells",Translator.get("Converter_VoronoiStippling.CellCount"),converter.getNumCells()));
		add(new SelectDouble("max",Translator.get("Converter_VoronoiStippling.DotMax"),converter.getMaxDotSize()));
		add(new SelectDouble("min",Translator.get("Converter_VoronoiStippling.DotMin"),converter.getMinDotSize()));
		add(new SelectDouble("cutoff",Translator.get("Converter_VoronoiStippling.Cutoff"),converter.getCutoff()));
		add(new SelectBoolean("drawBorderVoronoi",Translator.get("Converter_VoronoiStippling.DrawBordersVoronoi"),converter.getDrawBordersVoronoi()));
		add(new SelectBoolean("drawBorderTree",Translator.get("Converter_VoronoiStippling.DrawBorders"),converter.getDrawBordersTree()));
	}
}
