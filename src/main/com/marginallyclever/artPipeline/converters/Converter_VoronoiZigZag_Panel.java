package com.marginallyclever.artPipeline.converters;

import java.util.Observable;

import com.marginallyclever.makelangelo.select.SelectFloat;
import com.marginallyclever.makelangelo.select.SelectInteger;

public class Converter_VoronoiZigZag_Panel extends ImageConverterPanel {
	private SelectInteger numCells;
	private SelectFloat minDotSize;
	private Converter_VoronoiZigZag converter;
	
	public Converter_VoronoiZigZag_Panel(Converter_VoronoiZigZag converter_VoronoiZigZag) {
		super();
		
		converter = converter_VoronoiZigZag;
		
		add(numCells = new SelectInteger("voronoiStipplingCellCount",converter.getNumCells()));
		add(minDotSize = new SelectFloat("voronoiStipplingDotMin",converter.getMinDotSize()));
		finish();
	}

	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);
		
		if( numCells.getValue() != converter.getNumCells() ||
			minDotSize.getValue() != converter.getMinDotSize() ) {
			converter.setNumCells(numCells.getValue());
			converter.setMinDotSize(minDotSize.getValue());
			converter.restart();
		}
	}
}
