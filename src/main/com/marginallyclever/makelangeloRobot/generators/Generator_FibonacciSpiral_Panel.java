package com.marginallyclever.makelangeloRobot.generators;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectInteger;

public class Generator_FibonacciSpiral_Panel extends ImageGeneratorPanel implements DocumentListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	SelectInteger field_order;
	Generator_FibonacciSpiral generator;
	
	Generator_FibonacciSpiral_Panel(Generator_FibonacciSpiral generator_FibonacciSpiral) {
		this.generator = generator_FibonacciSpiral;
		
		field_order = new SelectInteger(Generator_Dragon.getOrder());
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
		if(newOrder<3) newOrder=1;
		
		if(newOrder != Generator_FibonacciSpiral.getOrder()) {
			Generator_FibonacciSpiral.setOrder(newOrder);
			makelangeloRobotPanel.regenerate(generator);
		}
	}
}
