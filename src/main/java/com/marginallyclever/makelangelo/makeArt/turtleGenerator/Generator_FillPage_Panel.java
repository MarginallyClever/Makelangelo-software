package com.marginallyclever.makelangelo.makeArt.turtleGenerator;

import java.beans.PropertyChangeEvent;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectDouble;

/**
 * Panel for {@link Generator_FillPage}
 * @author Dan Royer
 *
 */
public class Generator_FillPage_Panel extends TurtleGeneratorPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SelectDouble angle;
	private SelectDouble penDiameter;
	private Generator_FillPage generator;
	
	Generator_FillPage_Panel(Generator_FillPage generator) {
		super();
		
		this.generator = generator;

		add(angle = new SelectDouble("order",Translator.get("HilbertCurveOrder"),Generator_FillPage.getAngle()));
		add(penDiameter = new SelectDouble("penDiameter",Translator.get("penDiameter"),Generator_FillPage.getPenDiameter()));
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);

		double newOrder = angle.getValue();
		double newDiameter = penDiameter.getValue();
		
		if(newOrder != Generator_FillPage.getAngle() || newDiameter != Generator_FillPage.getPenDiameter()) {
			Generator_FillPage.setAngle(newOrder);
			Generator_FillPage.setPenDiameter(newDiameter);
			generator.generate();
		}
	}
}
