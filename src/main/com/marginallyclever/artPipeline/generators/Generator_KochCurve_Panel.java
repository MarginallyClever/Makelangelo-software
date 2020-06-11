package com.marginallyclever.artPipeline.generators;

import java.util.Observable;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectInteger;

public class Generator_KochCurve_Panel extends ImageGeneratorPanel {
	private SelectInteger field_order;
	private Generator_KochCurve generator;
	
	Generator_KochCurve_Panel(Generator_KochCurve generator) {
		super();
		
		this.generator = generator;
		
		add(field_order = new SelectInteger(Translator.get("HilbertCurveOrder"),Generator_KochCurve.getOrder()));
		finish();
	}

	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);
		
		int newOrder = field_order.getValue();
		if(newOrder<1) newOrder=1;
		
		if(newOrder != Generator_KochCurve.getOrder()) {
			Generator_KochCurve.setOrder(newOrder);
			makelangeloRobotPanel.regenerate(generator);
		}
	}
}
