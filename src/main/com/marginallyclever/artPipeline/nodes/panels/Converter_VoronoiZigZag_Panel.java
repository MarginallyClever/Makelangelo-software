package com.marginallyclever.artPipeline.nodes.panels;

import java.beans.PropertyChangeEvent;

import com.marginallyclever.artPipeline.TurtleNodePanel;
import com.marginallyclever.artPipeline.nodes.Converter_VoronoiZigZag;
import com.marginallyclever.makelangelo.select.SelectFloat;
import com.marginallyclever.makelangelo.select.SelectInteger;

/**
 * GUI for {@link Converter_VoronoiZigZag}
 * @author Dan Royer
 *
 */
public class Converter_VoronoiZigZag_Panel extends TurtleNodePanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5791313991426136610L;
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
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		
		if( numCells.getValue() != converter.getNumCells() ||
			minDotSize.getValue() != converter.getMinDotSize() ) {
			converter.setNumCells(numCells.getValue());
			converter.setMinDotSize(minDotSize.getValue());
			converter.restart();
		}
	}
}
