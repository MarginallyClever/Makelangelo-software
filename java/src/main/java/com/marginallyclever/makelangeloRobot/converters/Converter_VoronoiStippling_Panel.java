package com.marginallyclever.makelangeloRobot.converters;

import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.marginallyclever.makelangelo.SelectFloat;
import com.marginallyclever.makelangelo.SelectInteger;
import com.marginallyclever.makelangelo.Translator;

public class Converter_VoronoiStippling_Panel extends JPanel implements PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	SelectFloat text_dot_max;
	SelectFloat text_dot_min;
	SelectInteger text_cells;
	Converter_VoronoiStippling converter;
	
	public Converter_VoronoiStippling_Panel(Converter_VoronoiStippling converter_VoronoiStippling) {
		this.converter = converter_VoronoiStippling;
		
		text_cells = new SelectInteger(converter.getNumCells());
		text_dot_max = new SelectFloat(converter.getMaxDotSize());
		text_dot_min = new SelectFloat(converter.getMinDotSize());

		this.setLayout(new GridLayout(0, 1));
		this.add(new JLabel(Translator.get("voronoiStipplingCellCount")));
		this.add(text_cells);
		this.add(new JLabel(Translator.get("voronoiStipplingDotMax")));
		this.add(text_dot_max);
		this.add(new JLabel(Translator.get("voronoiStipplingDotMin")));
		this.add(text_dot_min);
		
		text_cells.addPropertyChangeListener("value",this);
		text_dot_max.addPropertyChangeListener("value",this);
		text_dot_min.addPropertyChangeListener("value",this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		int oldC = converter.getNumCells();
		converter.setNumCells(((Number)text_cells.getValue()).intValue());
		int newC = converter.getNumCells();
		converter.setMinDotSize(((Number)text_dot_min.getValue()).floatValue());
		converter.setMaxDotSize(((Number)text_dot_max.getValue()).floatValue());
		if(newC!=oldC) {
			converter.restart();
		}
	}
}
