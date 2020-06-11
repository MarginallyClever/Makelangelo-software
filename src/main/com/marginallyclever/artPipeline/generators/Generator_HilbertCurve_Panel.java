package com.marginallyclever.artPipeline.generators;

import java.util.Observable;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectInteger;

public class Generator_HilbertCurve_Panel extends ImageGeneratorPanel {
	private SelectInteger field_order;
	private Generator_HilbertCurve generator;
	
	Generator_HilbertCurve_Panel(Generator_HilbertCurve generator) {
		super();
		
		this.generator = generator;
		
		add(field_order = new SelectInteger(Translator.get("HilbertCurveOrder"),Generator_HilbertCurve.getOrder()));
		finish();
	}

	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);
		
		int newOrder = ((Number)field_order.getValue()).intValue();
		if(newOrder<1) newOrder=1;
		
		if(newOrder != Generator_HilbertCurve.getOrder()) {
			Generator_HilbertCurve.setOrder(newOrder);
			makelangeloRobotPanel.regenerate(generator);
		}
	}
}
