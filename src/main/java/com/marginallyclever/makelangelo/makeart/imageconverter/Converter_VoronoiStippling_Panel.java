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
		add(new SelectBoolean("drawVoronoi",Translator.get("Converter_VoronoiStippling.DrawBorders"),converter.getDrawVoronoi()));

		addPropertyChangeListener((evt)->{
			if(evt.getPropertyName().equals("drawVoronoi")) converter.setDrawVoronoi((boolean)evt.getNewValue());
			else {
				boolean isDirty=false;
				if(evt.getPropertyName().equals("cells")) {
					isDirty=true;
					converter.setNumCells((int)evt.getNewValue());
				}
				if(evt.getPropertyName().equals("max")) {
					isDirty=true;
					converter.setMinDotSize((double)evt.getNewValue());
				}
				if(evt.getPropertyName().equals("min")) {
					isDirty=true;
					converter.setMaxDotSize((double)evt.getNewValue());
				}
				if(evt.getPropertyName().equals("cutoff")) {
					isDirty=true;
					converter.setCutoff((double)evt.getNewValue());
				}
				if(isDirty) fireRestartConversion();
			}
		});
	}
}
