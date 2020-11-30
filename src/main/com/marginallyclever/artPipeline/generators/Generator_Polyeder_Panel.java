package com.marginallyclever.artPipeline.generators;

import java.util.Observable;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectInteger;
import com.marginallyclever.makelangelo.select.SelectOneOfMany;

public class Generator_Polyeder_Panel extends ImageGeneratorPanel {
	private Generator_Polyeder generator;
	private SelectInteger size;
	private SelectInteger flap;
	private SelectOneOfMany model;
	
	Generator_Polyeder_Panel(Generator_Polyeder generator) {
		super();
		
		this.generator = generator;
		String [] models=generator.getModelNames();

		add(size = new SelectInteger(Translator.get("Size"),generator.getLastSize()));
		add(flap = new SelectInteger(Translator.get("Flap"),generator.getLastFlap()));
		add(model = new SelectOneOfMany(Translator.get("Model"),models,generator.getLastModel()));
		finish();
	}

	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);
		
		generator.setSize(((Number)size.getValue()).intValue());
		generator.setFlap(((Number)flap.getValue()).intValue());
		generator.setModel(model.getSelectedIndex());
		makelangeloRobotPanel.regenerate(generator);
	}
}
