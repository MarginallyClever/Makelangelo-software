package com.marginallyclever.makelangeloRobot.converters;

import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.marginallyclever.makelangelo.SelectInteger;
import com.marginallyclever.makelangelo.Translator;

public class Converter_VoronoiZigZag_Panel extends JPanel implements PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	SelectInteger numGenerations;
	SelectInteger numCells;
	Converter_VoronoiZigZag converter;
	
	public Converter_VoronoiZigZag_Panel(Converter_VoronoiZigZag converter_VoronoiZigZag) {
		this.converter = converter_VoronoiZigZag;
		
		numGenerations = new SelectInteger(converter.getGenerations());
		numCells = new SelectInteger(converter.getNumCells());

		this.setLayout(new GridLayout(0, 1));
		this.add(new JLabel(Translator.get("voronoiStipplingCellCount")));
		this.add(numCells);
		this.add(new JLabel(Translator.get("voronoiStipplingGenCount")));
		this.add(numGenerations);
		
		numGenerations.addPropertyChangeListener("value",this);
		numCells.addPropertyChangeListener("value",this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		converter.setGenerations(((Number)numGenerations.getValue()).intValue());
		converter.setNumCells(((Number)numCells.getValue()).intValue());
		converter.restart();
	}
}
