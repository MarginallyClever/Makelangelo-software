package com.marginallyclever.makelangelo.makeArt.turtleGenerator;

import java.beans.PropertyChangeEvent;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectInteger;

/**
 * Panel for {@link Generator_Package}
 * @author Dan Royer
 *
 */
public class Generator_Package_Panel extends TurtleGeneratorPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1668021610842701532L;
	private Generator_Package generator;
	private SelectInteger width;
	private SelectInteger length;
	private SelectInteger height;
	
	Generator_Package_Panel(Generator_Package generator) {
		super();
		
		this.generator = generator;

		add(width = new SelectInteger("width",Translator.get("Width"),generator.getLastWidth()));
		add(height = new SelectInteger("height",Translator.get("Height"),generator.getLastHeight()));
		add(length = new SelectInteger("length",Translator.get("Length"),generator.getLastLength()));
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		
		generator.setWidth(((Number)width.getValue()).intValue());
		generator.setHeight(((Number)height.getValue()).intValue());
		generator.setLength(((Number)length.getValue()).intValue());
		generator.generate();
	}
}
