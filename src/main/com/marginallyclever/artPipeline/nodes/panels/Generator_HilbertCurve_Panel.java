package com.marginallyclever.artPipeline.nodes.panels;

import java.beans.PropertyChangeEvent;

import com.marginallyclever.artPipeline.nodes.fractals.Generator_HilbertCurve;
import com.marginallyclever.convenience.nodes.NodePanel;
import com.marginallyclever.convenience.select.SelectSlider;
import com.marginallyclever.makelangelo.Translator;

/**
 * Panel for {@link Generator_HilbertCurve}
 * @author Dan Royer
 *
 */
public class Generator_HilbertCurve_Panel extends NodePanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SelectSlider fieldOrder;
	private Generator_HilbertCurve generator;
	
	public Generator_HilbertCurve_Panel(Generator_HilbertCurve generator) {
		super();
		
		this.generator = generator;

		add(fieldOrder = new SelectSlider(Translator.get("HilbertCurveOrder"),8,1,Generator_HilbertCurve.getOrder()));
		finish();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		
		int newOrder = fieldOrder.getValue();
		if(newOrder<1) newOrder=1;
		
		if(newOrder != Generator_HilbertCurve.getOrder()) {
			Generator_HilbertCurve.setOrder(newOrder);
			generator.restart();
		}
	}
}
