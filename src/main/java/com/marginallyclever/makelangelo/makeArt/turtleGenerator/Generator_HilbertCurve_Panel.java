package com.marginallyclever.makelangelo.makeArt.turtleGenerator;

import java.beans.PropertyChangeEvent;

import com.marginallyClever.makelangelo.Translator;
import com.marginallyClever.makelangelo.select.SelectReadOnlyText;
import com.marginallyClever.makelangelo.select.SelectSlider;

/**
 * Panel for {@link Generator_HilbertCurve}
 * @author Dan Royer
 *
 */
public class Generator_HilbertCurve_Panel extends TurtleGeneratorPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SelectSlider fieldOrder;
	private Generator_HilbertCurve generator;
	
	Generator_HilbertCurve_Panel(Generator_HilbertCurve generator) {
		super();
		
		this.generator = generator;

		add(fieldOrder = new SelectSlider("order",Translator.get("HilbertCurveOrder"),8,1,Generator_HilbertCurve.getOrder()));
		add(new SelectReadOnlyText("url","<a href='https://en.wikipedia.org/wiki/Hilbert_curve'>"+Translator.get("TurtleGenerators.LearnMore.Link.Text")+"</a>"));
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		
		int newOrder = fieldOrder.getValue();
		if(newOrder<1) newOrder=1;
		
		if(newOrder != Generator_HilbertCurve.getOrder()) {
			Generator_HilbertCurve.setOrder(newOrder);
			generator.generate();
		}
	}
}
