package com.marginallyclever.makelangeloRobot.converters;

import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;

import com.marginallyclever.makelangelo.SelectFloat;
import com.marginallyclever.makelangelo.SelectInteger;
import com.marginallyclever.makelangelo.Translator;

public class Converter_VoronoiZigZag_Panel extends ImageConverterPanel implements PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	SelectInteger text_cells;
	SelectFloat text_dot_min;
	Converter_VoronoiZigZag converter;
	
	public Converter_VoronoiZigZag_Panel(Converter_VoronoiZigZag converter_VoronoiZigZag) {
		this.converter = converter_VoronoiZigZag;
		
		text_cells = new SelectInteger(converter.getNumCells());
		text_dot_min = new SelectFloat(converter.getMinDotSize());

		this.setLayout(new GridLayout(0, 1));
		this.add(new JLabel(Translator.get("voronoiStipplingCellCount")));
		this.add(text_cells);
		this.add(new JLabel(Translator.get("voronoiStipplingDotMin")));
		this.add(text_dot_min);
		
		text_cells.addPropertyChangeListener("value",this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		converter.setNumCells(((Number)text_cells.getValue()).intValue());
		converter.restart();
	}
}
