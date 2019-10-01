package com.marginallyclever.makelangeloRobot.generators;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectBoolean;
import com.marginallyclever.makelangelo.select.SelectFloat;
import com.marginallyclever.makelangelo.select.SelectInteger;

public class Generator_Spirograph_Panel extends ImageGeneratorPanel implements DocumentListener, ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected SelectBoolean field_isEpitrochoid;
	protected SelectInteger field_majorRadius;
	protected SelectInteger field_minorRadius;
	protected SelectFloat field_pScale;
	protected SelectInteger field_numSamples;
	protected Generator_Spirograph generator;
	
	Generator_Spirograph_Panel(Generator_Spirograph generator) {
		this.generator = generator;
		
		field_majorRadius = new SelectInteger(Generator_Spirograph.getMajorRadius());
		field_majorRadius.getDocument().addDocumentListener(this);
		field_minorRadius = new SelectInteger(Generator_Spirograph.getMinorRadius());
		field_minorRadius.getDocument().addDocumentListener(this);
		field_pScale = new SelectFloat(Generator_Spirograph.getPScale());
		field_pScale.getDocument().addDocumentListener(this);
		field_numSamples = new SelectInteger(Generator_Spirograph.getNumSamples());
		field_numSamples.getDocument().addDocumentListener(this);
		field_isEpitrochoid = new SelectBoolean(Generator_Spirograph.getEpitrochoid());
		field_isEpitrochoid.addActionListener(this);
		
		setLayout(new GridLayout(9, 1));
		add(new JLabel(Translator.get("SpirographEpitrochoid")));
		add(field_isEpitrochoid);
		add(new JLabel(Translator.get("SpirographMajorRadius")));
		add(field_majorRadius);
		add(new JLabel(Translator.get("SpirographMinorRadius")));
		add(field_minorRadius);
		add(new JLabel(Translator.get("SpirographPScale")));
		add(field_pScale);
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
		int newMajorRadius = ((Number)field_majorRadius.getValue()).intValue();
		int newMinorRadius = ((Number)field_minorRadius.getValue()).intValue();
		float newPScale = ((Number)field_pScale.getValue()).floatValue();
		int newNumSamples = ((Number)field_numSamples.getValue()).intValue();
		boolean newEpitrochoid = field_isEpitrochoid.getValue();

		if(newMajorRadius != Generator_Spirograph.getMajorRadius() ||
			newMinorRadius != Generator_Spirograph.getMinorRadius() ||
			newPScale != Generator_Spirograph.getPScale() ||
			newNumSamples != Generator_Spirograph.getNumSamples() ||
			newEpitrochoid != Generator_Spirograph.getEpitrochoid() ) {
			Generator_Spirograph.setMajorRadius(newMajorRadius);
			Generator_Spirograph.setMinorRadius(newMinorRadius);
			Generator_Spirograph.setPScale(newPScale);
			Generator_Spirograph.setNumSamples(newNumSamples);
			Generator_Spirograph.setEpitrochoid(newEpitrochoid);
			makelangeloRobotPanel.regenerate(generator);
		}
	}
}
