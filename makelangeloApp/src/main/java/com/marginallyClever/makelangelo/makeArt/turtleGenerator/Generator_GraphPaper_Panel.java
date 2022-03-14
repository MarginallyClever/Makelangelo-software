package com.marginallyClever.makelangelo.makeArt.turtleGenerator;

import java.beans.PropertyChangeEvent;

import com.marginallyClever.makelangelo.Translator;
import com.marginallyClever.makelangelo.select.SelectDouble;

/**
 * Panel for {@link Generator_GraphPaper}
 * @author Dan Royer
 *
 */
public class Generator_GraphPaper_Panel extends TurtleGeneratorPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SelectDouble angle;
	private Generator_GraphPaper generator;
	
	Generator_GraphPaper_Panel(Generator_GraphPaper generator_GraphPaper) {
		super();
		
		this.generator = generator_GraphPaper;

		add(angle = new SelectDouble("order", Translator.get("HilbertCurveOrder"),Generator_GraphPaper.getAngle()));
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);

		double newOrder = angle.getValue();
		
		if(newOrder != Generator_GraphPaper.getAngle()) {
			Generator_GraphPaper.setAngle(newOrder);
			generator.generate();
		}
	}
}
