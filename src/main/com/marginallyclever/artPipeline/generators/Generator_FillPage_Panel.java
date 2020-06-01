package com.marginallyclever.artPipeline.generators;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectFloat;

public class Generator_FillPage_Panel extends ImageGeneratorPanel implements DocumentListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	SelectFloat field_angle;
	Generator_FillPage generator;
	
	Generator_FillPage_Panel(Generator_FillPage generator) {
		this.generator = generator;
		
		field_angle = new SelectFloat(Generator_FillPage.getAngle());
		field_angle.getDocument().addDocumentListener(this);

		setLayout(new GridLayout(0, 1));
		add(new JLabel(Translator.get("HilbertCurveOrder")));
		add(field_angle);
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
		int newOrder = ((Number)field_angle.getValue()).intValue();
		
		if(newOrder != Generator_FillPage.getAngle()) {
			Generator_FillPage.setAngle(newOrder);
			makelangeloRobotPanel.regenerate(generator);
		}
	}
}
