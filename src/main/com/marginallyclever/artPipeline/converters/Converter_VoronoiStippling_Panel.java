package com.marginallyclever.artPipeline.converters;

import java.util.Observable;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectBoolean;
import com.marginallyclever.makelangelo.select.SelectFloat;
import com.marginallyclever.makelangelo.select.SelectInteger;

public class Converter_VoronoiStippling_Panel extends ImageConverterPanel {
	private SelectFloat text_dot_max;
	private SelectFloat text_dot_min;
	private SelectFloat field_cutoff;
	private SelectInteger text_cells;
	private SelectBoolean draw_borders;
	private Converter_VoronoiStippling converter;
	
	public Converter_VoronoiStippling_Panel(Converter_VoronoiStippling converter_VoronoiStippling) {
		super();
		converter = converter_VoronoiStippling;
		
		add(text_cells = new SelectInteger(Translator.get("voronoiStipplingCellCount"),converter.getNumCells  ()));
		add(text_dot_max = new SelectFloat(Translator.get("voronoiStipplingDotMax"),converter.getMaxDotSize   ()));
		add(text_dot_min = new SelectFloat(Translator.get("voronoiStipplingDotMin"),converter.getMinDotSize   ()));
		add(draw_borders = new SelectBoolean(Translator.get("voronoiStipplingCutoff"),converter.getDrawBorders()));
		add(field_cutoff = new SelectFloat(Translator.get("voronoiStipplingDrawBorders"),converter.getCutoff  ()));
		finish();
	}

	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);
		
		converter.setDrawBorders(draw_borders.isSelected());
		
		boolean goAgain = (converter.getNumCells() != text_cells.getValue())
						| (converter.getMinDotSize() != text_cells.getValue())
						| (converter.getMaxDotSize() != text_cells.getValue())
						| (converter.getCutoff() != text_cells.getValue());
		if(goAgain) {
			converter.setNumCells(text_cells.getValue());
			converter.setMinDotSize(text_dot_min.getValue());
			converter.setMaxDotSize(text_dot_max.getValue());
			converter.setCutoff(field_cutoff.getValue());
			converter.restart();
		}
	}
}
