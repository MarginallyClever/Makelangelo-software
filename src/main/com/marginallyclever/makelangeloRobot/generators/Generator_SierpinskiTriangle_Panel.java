package com.marginallyclever.makelangeloRobot.generators;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectInteger;

public class Generator_SierpinskiTriangle_Panel extends ImageGeneratorPanel implements DocumentListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	SelectInteger field_order;
	Generator_SierpinskiTriangle generator;
	
	Generator_SierpinskiTriangle_Panel(Generator_SierpinskiTriangle generator) {
		this.generator = generator;
		
		field_order = new SelectInteger(Generator_SierpinskiTriangle.getOrder());
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
		
		if(newOrder != Generator_SierpinskiTriangle.getOrder()) {
			Generator_SierpinskiTriangle.setOrder(newOrder);
			makelangeloRobotPanel.regenerate(generator);
		}
	}
}
