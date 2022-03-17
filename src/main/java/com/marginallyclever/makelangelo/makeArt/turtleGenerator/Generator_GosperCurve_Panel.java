package com.marginallyclever.makelangelo.makeArt.turtleGenerator;

import java.beans.PropertyChangeEvent;

import com.marginallyClever.makelangelo.Translator;
import com.marginallyClever.makelangelo.select.SelectReadOnlyText;
import com.marginallyClever.makelangelo.select.SelectSlider;

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

		add(fieldOrder = new SelectSlider("order",
				Translator.get("HilbertCurveOrder")// As this is the same concept and translation value but this translation key is confusing as we are in GosperCurve_Panle ...
				,6,1,Generator_GosperCurve.getOrder()));
		add(new SelectReadOnlyText("url","<a href='https://en.wikipedia.org/wiki/Gosper_curve'>"+Translator.get("TurtleGenerators.LearnMore.Link.Text")+"</a>"));
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
