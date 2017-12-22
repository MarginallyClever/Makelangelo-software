package com.marginallyclever.makelangeloRobot.converters;

import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import com.marginallyclever.makelangelo.SelectInteger;
import com.marginallyclever.makelangelo.Translator;

public class Converter_VoronoiZigZag_Panel extends ImageConverterPanel implements PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	SelectInteger numCells;
	Converter_VoronoiZigZag converter;
	
	public Converter_VoronoiZigZag_Panel(Converter_VoronoiZigZag converter_VoronoiZigZag) {
		this.converter = converter_VoronoiZigZag;
		
		numCells = new SelectInteger(converter.getNumCells());

		this.setLayout(new GridLayout(0, 1));
		this.add(new JLabel(Translator.get("voronoiStipplingCellCount")));
		this.add(numCells);
		
		numCells.addPropertyChangeListener("value",this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		converter.setNumCells(((Number)numCells.getValue()).intValue());
		converter.restart();
	}
}
