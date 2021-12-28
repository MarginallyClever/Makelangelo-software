package com.marginallyclever.makelangelo.makeArt.turtleGenerator;

import java.beans.PropertyChangeEvent;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;
import com.marginallyclever.makelangelo.select.SelectSlider;

/**
 * Panel for {@link Generator_GosperCurve}
 * @author Dan Royer
 *
 */
public class Generator_GosperCurve_Panel extends TurtleGeneratorPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SelectSlider fieldOrder;
	private Generator_GosperCurve generator;
	
	Generator_GosperCurve_Panel(Generator_GosperCurve generator) {
		super();
		
		this.generator = generator;

		add(fieldOrder = new SelectSlider("order",Translator.get("HilbertCurveOrder"),6,1,Generator_GosperCurve.getOrder()));
		add(new SelectReadOnlyText("url","<a href='https://en.wikipedia.org/wiki/Hilbert_curve'>Learn more</a>"));
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		
		int newOrder = ((Number)fieldOrder.getValue()).intValue();
		if(newOrder<1) newOrder=1;
		
		if(newOrder != Generator_GosperCurve.getOrder()) {
			Generator_GosperCurve.setOrder(newOrder);
			generator.generate();
		}
	}
}
