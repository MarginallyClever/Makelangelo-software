package com.marginallyclever.makelangelo.makeArt.turtleGenerator;

import java.beans.PropertyChangeEvent;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;
import com.marginallyclever.makelangelo.select.SelectSlider;

/**
 * Panel for {@link Generator_SierpinskiTriangle}
 * @author Dan Royer
 *
 */
public class Generator_SierpinskiTriangle_Panel extends TurtleGeneratorPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	SelectSlider field_order;
	Generator_SierpinskiTriangle generator;
	
	Generator_SierpinskiTriangle_Panel(Generator_SierpinskiTriangle generator) {
		super();
		
		this.generator = generator;
		
		add(field_order = new SelectSlider("order",Translator.get("HilbertCurveOrder"),10,1,Generator_SierpinskiTriangle.getOrder()));
		add(new SelectReadOnlyText("url","<a href='https://en.wikipedia.org/wiki/Sierpi%C5%84ski_triangle'>Learn more</a>"));
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		
		int newOrder = field_order.getValue();
		if(newOrder<1) newOrder=1;
		
		if(newOrder != Generator_SierpinskiTriangle.getOrder()) {
			Generator_SierpinskiTriangle.setOrder(newOrder);
			generator.generate();
		}
	}
}
