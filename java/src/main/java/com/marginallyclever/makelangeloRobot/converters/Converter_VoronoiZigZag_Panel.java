package com.marginallyclever.makelangeloRobot.converters;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.marginallyclever.makelangelo.SelectFloat;
import com.marginallyclever.makelangelo.SelectInteger;
import com.marginallyclever.makelangelo.Translator;

public class Converter_VoronoiZigZag_Panel extends JPanel implements DocumentListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	SelectFloat text_dot_max;
	SelectFloat text_dot_min;
	SelectInteger text_gens;
	SelectInteger text_cells;
	Converter_VoronoiZigZag converter;
	
	public Converter_VoronoiZigZag_Panel(Converter_VoronoiZigZag converter_VoronoiZigZag) {
		this.converter = converter_VoronoiZigZag;
		
		text_gens = new SelectInteger(converter.getGenerations());
		text_cells = new SelectInteger(converter.getNumCells());

		this.setLayout(new GridLayout(0, 1));
		this.add(new JLabel(Translator.get("voronoiStipplingCellCount")));
		this.add(text_cells);
		this.add(new JLabel(Translator.get("voronoiStipplingGenCount")));
		this.add(text_gens);
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
		converter.reconvert();
	}
}
