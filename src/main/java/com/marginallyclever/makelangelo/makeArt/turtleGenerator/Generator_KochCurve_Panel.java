package com.marginallyclever.makelangelo.makeArt.turtleGenerator;

import java.beans.PropertyChangeEvent;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;
import com.marginallyclever.makelangelo.select.SelectSlider;

/**
 * Panel for {@link Generator_KochCurve}
 * @author Dan Royer
 *
 */
public class Generator_KochCurve_Panel extends TurtleGeneratorPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SelectSlider fieldOrder;
	private Generator_KochCurve generator;
	
	Generator_KochCurve_Panel(Generator_KochCurve generator) {
		super();
		
		this.generator = generator;

		add(fieldOrder = new SelectSlider("order",Translator.get("HilbertCurveOrder"),7,1,Generator_HilbertCurve.getOrder()));
		add(new SelectReadOnlyText("url","<a href='https://en.wikipedia.org/wiki/Koch_curve'>Learn more</a>"));
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		
		int newOrder = fieldOrder.getValue();
		if(newOrder<1) newOrder=1;
		
		if(newOrder != Generator_KochCurve.getOrder()) {
			Generator_KochCurve.setOrder(newOrder);
			generator.generate();
		}
	}
}
