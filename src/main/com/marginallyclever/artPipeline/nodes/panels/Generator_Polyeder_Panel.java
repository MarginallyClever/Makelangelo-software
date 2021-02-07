package com.marginallyclever.artPipeline.nodes.panels;

import java.beans.PropertyChangeEvent;

import com.marginallyclever.artPipeline.NodePanel;
import com.marginallyclever.artPipeline.nodes.polyhedron.Generator_Polyeder;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectInteger;
import com.marginallyclever.makelangelo.select.SelectOneOfMany;

/**
 * Panel for {@link Generator_Polyeder}
 * @author Dan Royer
 *
 */
public class Generator_Polyeder_Panel extends NodePanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Generator_Polyeder generator;
	private SelectInteger size;
	private SelectInteger flap;
	private SelectOneOfMany model;
	
	public Generator_Polyeder_Panel(Generator_Polyeder generator) {
		super();
		
		this.generator = generator;
		String [] models=generator.getModelNames();

		add(size = new SelectInteger(Translator.get("Size"),generator.getLastSize()));
		add(flap = new SelectInteger(Translator.get("Flap"),generator.getLastFlap()));
		add(model = new SelectOneOfMany(Translator.get("Model"),models,generator.getLastModel()));
		finish();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		
		generator.setSize(((Number)size.getValue()).intValue());
		generator.setFlap(((Number)flap.getValue()).intValue());
		generator.setModel(model.getSelectedIndex());
		generator.restart();
	}
}
