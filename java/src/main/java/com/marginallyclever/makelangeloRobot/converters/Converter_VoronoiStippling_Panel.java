package com.marginallyclever.makelangeloRobot.converters;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.marginallyclever.makelangelo.SelectFloat;
import com.marginallyclever.makelangelo.SelectInteger;
import com.marginallyclever.makelangelo.Translator;

public class Converter_VoronoiStippling_Panel extends JPanel implements DocumentListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	SelectFloat text_dot_max;
	SelectFloat text_dot_min;
	SelectInteger text_gens;
	SelectInteger text_cells;
	Converter_VoronoiStippling converter;
	
	public Converter_VoronoiStippling_Panel(Converter_VoronoiStippling converter_VoronoiStippling) {
		this.converter = converter_VoronoiStippling;
		
		text_gens = new SelectInteger(converter.getGenerations());
		text_cells = new SelectInteger(converter.getNumCells());
		text_dot_max = new SelectFloat(converter.getMaxDotSize());
		text_dot_min = new SelectFloat(converter.getMinDotSize());

		this.setLayout(new GridLayout(0, 1));
		this.add(new JLabel(Translator.get("voronoiStipplingCellCount")));
		this.add(text_cells);
		this.add(new JLabel(Translator.get("voronoiStipplingGenCount")));
		this.add(text_gens);
		this.add(new JLabel(Translator.get("voronoiStipplingDotMax")));
		this.add(text_dot_max);
		this.add(new JLabel(Translator.get("voronoiStipplingDotMin")));
		this.add(text_dot_min);
	}

	@Override
	public void changedUpdate(DocumentEvent arg0) {
		validateInput();
	}

	@Override
	public void insertUpdate(DocumentEvent arg0) {
		validateInput();
	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {
		validateInput();
	}

	private void validateInput() {
		converter.setGenerations(((Number)text_gens.getValue()).intValue());
		converter.setNumCells(((Number)text_cells.getValue()).intValue());
		converter.setMinDotSize(((Number)text_dot_min.getValue()).floatValue());
		converter.setMaxDotSize(((Number)text_dot_max.getValue()).floatValue());
	}
}
