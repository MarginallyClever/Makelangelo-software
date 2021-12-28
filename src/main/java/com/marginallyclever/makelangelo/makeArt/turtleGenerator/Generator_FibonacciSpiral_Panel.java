package com.marginallyclever.makelangelo.makeArt.turtleGenerator;

import java.beans.PropertyChangeEvent;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;
import com.marginallyclever.makelangelo.select.SelectSlider;

/**
 * Panel for {@link Generator_FibonacciSpiral}
 * @author Dan Royer
 *
 */
public class Generator_FibonacciSpiral_Panel extends TurtleGeneratorPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SelectSlider fieldOrder;
	private Generator_FibonacciSpiral generator;
	
	Generator_FibonacciSpiral_Panel(Generator_FibonacciSpiral generator_FibonacciSpiral) {
		super();
		this.generator = generator_FibonacciSpiral;
		
		add(fieldOrder = new SelectSlider("order",Translator.get("HilbertCurveOrder"),16,0,Generator_Dragon.getOrder()));
		add(new SelectReadOnlyText("url","<a href='https://en.wikipedia.org/wiki/Fibonacci_number'>Learn more</a>"));
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		
		int newOrder = ((Number)fieldOrder.getValue()).intValue();
		if(newOrder<3) newOrder=1;
		
		if(newOrder != Generator_FibonacciSpiral.getOrder()) {
			Generator_FibonacciSpiral.setOrder(newOrder);
			generator.generate();
		}
	}
}
