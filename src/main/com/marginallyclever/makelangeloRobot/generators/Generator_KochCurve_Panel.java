package com.marginallyclever.makelangeloRobot.generators;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.marginallyclever.makelangelo.SelectInteger;
import com.marginallyclever.makelangelo.Translator;

public class Generator_KochCurve_Panel extends ImageGeneratorPanel implements DocumentListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	SelectInteger field_order;
	Generator_KochCurve generator;
	
	Generator_KochCurve_Panel(Generator_KochCurve generator) {
		this.generator = generator;
		
		field_order = new SelectInteger(Generator_KochCurve.getOrder());
		field_order.getDocument().addDocumentListener(this);
		setLayout(new GridLayout(0, 1));
		add(new JLabel(Translator.get("HilbertCurveOrder")));
		add(field_order);

	}

	@Override
	public void changedUpdate(DocumentEvent arg0) {
		validate();
	}

	@Override
	public void insertUpdate(DocumentEvent arg0) {
		validate();
	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {
		validate();
	}
	
	public void validate() {
		int newOrder = ((Number)field_order.getValue()).intValue();
		if(newOrder<1) newOrder=1;
		
		if(newOrder != Generator_KochCurve.getOrder()) {
			Generator_KochCurve.setOrder(newOrder);
			makelangeloRobotPanel.regenerate(generator);
		}
	}
}
