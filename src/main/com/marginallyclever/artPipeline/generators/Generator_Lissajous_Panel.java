package com.marginallyclever.artPipeline.generators;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectFloat;
import com.marginallyclever.makelangelo.select.SelectInteger;

public class Generator_Lissajous_Panel extends ImageGeneratorPanel implements DocumentListener, ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected SelectInteger field_a;
	protected SelectInteger field_b;
	protected SelectInteger field_numSamples;
	protected SelectFloat field_delta;
	protected Generator_Lissajous generator;
	
	Generator_Lissajous_Panel(Generator_Lissajous generator) {
		this.generator = generator;
		
		field_b = new SelectInteger(Generator_Lissajous.getB());
		field_b.getDocument().addDocumentListener(this);
		field_numSamples = new SelectInteger(Generator_Lissajous.getNumSamples());
		field_numSamples.getDocument().addDocumentListener(this);
		field_a = new SelectInteger(Generator_Lissajous.getA());
		field_a.getDocument().addDocumentListener(this);
		field_delta = new SelectFloat(Generator_Lissajous.getDelta());
		field_delta.getDocument().addDocumentListener(this);
		
		setLayout(new GridLayout(9, 1));
		add(new JLabel(Translator.get("LissajousA")));
		add(field_a);
		add(new JLabel(Translator.get("LissajousB")));
		add(field_b);
		add(new JLabel(Translator.get("LissajousDelta")));
		add(field_delta);
		add(new JLabel(Translator.get("SpirographNumSamples")));
		add(field_numSamples);
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
	
	public void validateInput() {
		updateMe();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		updateMe();
	}
	
	protected void updateMe() {
		int newB = ((Number)field_b.getValue()).intValue();
		int newA = ((Number)field_a.getValue()).intValue();
		float newDelta = ((Number)field_delta.getValue()).floatValue();
		int newNumSamples = ((Number)field_numSamples.getValue()).intValue();

		if(newB != Generator_Lissajous.getB() ||
			newA != Generator_Lissajous.getA() ||
			newDelta != Generator_Lissajous.getDelta() ||
			newNumSamples != Generator_Lissajous.getNumSamples() ) {
			Generator_Lissajous.setB(newB);
			Generator_Lissajous.setA(newA);
			Generator_Lissajous.setDelta(newDelta);
			Generator_Lissajous.setNumSamples(newNumSamples);
			makelangeloRobotPanel.regenerate(generator);
		}
	}
}
