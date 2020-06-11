package com.marginallyclever.artPipeline.generators;

import java.util.Observable;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectInteger;

public class Generator_SierpinskiTriangle_Panel extends ImageGeneratorPanel {
	SelectInteger field_order;
	Generator_SierpinskiTriangle generator;
	
	Generator_SierpinskiTriangle_Panel(Generator_SierpinskiTriangle generator) {
		super();
		
		this.generator = generator;
		
		field_order = new SelectInteger(Translator.get("HilbertCurveOrder"),Generator_SierpinskiTriangle.getOrder());
		add(field_order);
	}

	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);
		
		int newOrder = ((Number)field_order.getValue()).intValue();
		if(newOrder<1) newOrder=1;
		
		if(newOrder != Generator_SierpinskiTriangle.getOrder()) {
			Generator_SierpinskiTriangle.setOrder(newOrder);
			makelangeloRobotPanel.regenerate(generator);
		}
	}
}
