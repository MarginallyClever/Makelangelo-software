package com.marginallyclever.artPipeline.generators;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectInteger;

public class Generator_HilbertCurve_Panel extends ImageGeneratorPanel implements DocumentListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	SelectInteger field_order;
	Generator_HilbertCurve generator;
	
	Generator_HilbertCurve_Panel(Generator_HilbertCurve generator) {
		this.generator = generator;
		
		field_order = new SelectInteger(Generator_HilbertCurve.getOrder());
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
		
		if(newOrder != Generator_HilbertCurve.getOrder()) {
			Generator_HilbertCurve.setOrder(newOrder);
			makelangeloRobotPanel.regenerate(generator);
		}
	}
}
