package com.marginallyclever.artPipeline.generators;

import java.util.Observable;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectInteger;

public class Generator_GosperCurve_Panel extends ImageGeneratorPanel {
	private SelectInteger field_order;
	private Generator_GosperCurve generator;
	
	Generator_GosperCurve_Panel(Generator_GosperCurve generator) {
		super();
		
		this.generator = generator;
		
		add(field_order = new SelectInteger(Translator.get("HilbertCurveOrder"),Generator_GosperCurve.getOrder()));
		finish();
	}

	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);
		
		int newOrder = ((Number)field_order.getValue()).intValue();
		if(newOrder<1) newOrder=1;
		
		if(newOrder != Generator_GosperCurve.getOrder()) {
			Generator_GosperCurve.setOrder(newOrder);
			makelangeloRobotPanel.regenerate(generator);
		}
	}
}
