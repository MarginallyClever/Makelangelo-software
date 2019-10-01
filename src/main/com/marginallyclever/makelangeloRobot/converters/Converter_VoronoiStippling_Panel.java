package com.marginallyclever.makelangeloRobot.converters;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectBoolean;
import com.marginallyclever.makelangelo.select.SelectFloat;
import com.marginallyclever.makelangelo.select.SelectInteger;

public class Converter_VoronoiStippling_Panel extends ImageConverterPanel implements ActionListener, PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	SelectFloat text_dot_max;
	SelectFloat text_dot_min;
	SelectFloat field_cutoff;
	SelectInteger text_cells;
	SelectBoolean draw_borders;
	Converter_VoronoiStippling converter;
	
	public Converter_VoronoiStippling_Panel(Converter_VoronoiStippling converter_VoronoiStippling) {
		this.converter = converter_VoronoiStippling;
		
		text_cells = new SelectInteger(converter.getNumCells());
		text_dot_max = new SelectFloat(converter.getMaxDotSize());
		text_dot_min = new SelectFloat(converter.getMinDotSize());
		draw_borders = new SelectBoolean(converter.getDrawBorders());
		field_cutoff = new SelectFloat(converter.getCutoff());
		
		this.setLayout(new GridLayout(0, 1));
		this.add(new JLabel(Translator.get("voronoiStipplingCellCount")));
		this.add(text_cells);
		this.add(new JLabel(Translator.get("voronoiStipplingDotMax")));
		this.add(text_dot_max);
		this.add(new JLabel(Translator.get("voronoiStipplingDotMin")));
		this.add(text_dot_min);
		this.add(new JLabel(Translator.get("voronoiStipplingCutoff")));
		this.add(field_cutoff);
		this.add(new JLabel(Translator.get("voronoiStipplingDrawBorders")));
		this.add(draw_borders);
		
		text_cells.addPropertyChangeListener("value",this);
		text_dot_max.addPropertyChangeListener("value",this);
		text_dot_min.addPropertyChangeListener("value",this);
		field_cutoff.addPropertyChangeListener("value",this);
		draw_borders.addActionListener(this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		int oldC = converter.getNumCells();
		converter.setNumCells(((Number)text_cells.getValue()).intValue());
		int newC = converter.getNumCells();
		converter.setMinDotSize(((Number)text_dot_min.getValue()).floatValue());
		converter.setMaxDotSize(((Number)text_dot_max.getValue()).floatValue());
		converter.setCutoff(((Number)field_cutoff.getValue()).floatValue());
		if(newC!=oldC) {
			converter.restart();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		converter.setDrawBorders(draw_borders.getValue());
	}
}
