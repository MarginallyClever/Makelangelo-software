package com.marginallyclever.artPipeline.generators;

import java.util.Observable;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectInteger;

public class Generator_Dragon_Panel extends ImageGeneratorPanel {
	private SelectInteger field_order;
	private Generator_Dragon generator;
	
	Generator_Dragon_Panel(Generator_Dragon generator) {
		super();
		this.generator = generator;
		
		add(field_order = new SelectInteger(Translator.get("HilbertCurveOrder"),Generator_Dragon.getOrder()));
	}

	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);
		
		int newOrder = ((Number)field_order.getValue()).intValue();
		if(newOrder<1) newOrder=1;
		
		if(newOrder != Generator_Dragon.getOrder()) {
			Generator_Dragon.setOrder(newOrder);
			makelangeloRobotPanel.regenerate(generator);
		}
	}
}
