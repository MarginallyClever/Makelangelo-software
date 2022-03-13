package com.marginallyClever.makelangelo.makeArt.turtleGenerator;

import java.beans.PropertyChangeEvent;

import com.marginallyClever.makelangelo.Translator;
import com.marginallyClever.makelangelo.select.SelectInteger;
import com.marginallyClever.makelangelo.select.SelectOneOfMany;

/**
 * Panel for {@link Generator_Polyeder}
 * @author Dan Royer
 *
 */
public class Generator_Polyeder_Panel extends TurtleGeneratorPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Generator_Polyeder generator;
	private SelectInteger size;
	private SelectInteger flap;
	private SelectOneOfMany model;
	
	Generator_Polyeder_Panel(Generator_Polyeder generator) {
		super();
		
		this.generator = generator;
		String [] models=generator.getModelNames();

		add(size = new SelectInteger("size", Translator.get("Size"),generator.getLastSize()));
		add(flap = new SelectInteger("flap",Translator.get("Flap"),generator.getLastFlap()));
		add(model = new SelectOneOfMany("model",Translator.get("Model"),models,generator.getLastModel()));
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		
		generator.setSize(((Number)size.getValue()).intValue());
		generator.setFlap(((Number)flap.getValue()).intValue());
		generator.setModel(model.getSelectedIndex());
		generator.generate();
	}
}
