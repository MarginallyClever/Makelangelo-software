package com.marginallyclever.makelangelo.makeArt.turtleGenerator;

import java.beans.PropertyChangeEvent;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;
import com.marginallyclever.makelangelo.select.SelectSlider;

/**
 * Panel for {@link Generator_Dragon}
 * @author Dan Royer
 *
 */
public class Generator_Dragon_Panel extends TurtleGeneratorPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SelectSlider fieldOrder;
	private Generator_Dragon generator;
	
	Generator_Dragon_Panel(Generator_Dragon generator) {
		super();
		this.generator = generator;

		add(fieldOrder = new SelectSlider("order",Translator.get("HilbertCurveOrder"),16,0,Generator_Dragon.getOrder()));
		add(new SelectReadOnlyText("url","<a href='https://en.wikipedia.org/wiki/Dragon_curve'>Learn more</a>"));
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		
		int newOrder = fieldOrder.getValue();
		if(newOrder<1) newOrder=1;
		
		if(newOrder != Generator_Dragon.getOrder()) {
			Generator_Dragon.setOrder(newOrder);
			generator.generate();
		}
	}
}
