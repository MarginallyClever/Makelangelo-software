package com.marginallyclever.artPipeline.generators;

import java.util.Observable;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;
import com.marginallyclever.makelangelo.select.SelectSlider;

public class Generator_SierpinskiTriangle_Panel extends ImageGeneratorPanel {
	SelectSlider field_order;
	Generator_SierpinskiTriangle generator;
	
	Generator_SierpinskiTriangle_Panel(Generator_SierpinskiTriangle generator) {
		super();
		
		this.generator = generator;
		
		add(field_order = new SelectSlider(Translator.get("HilbertCurveOrder"),10,1,Generator_SierpinskiTriangle.getOrder()));
		add(new SelectReadOnlyText("<a href='https://en.wikipedia.org/wiki/Sierpi%C5%84ski_triangle'>Learn more</a>"));
		finish();
	}

	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);
		
		int newOrder = field_order.getValue();
		if(newOrder<1) newOrder=1;
		
		if(newOrder != Generator_SierpinskiTriangle.getOrder()) {
			Generator_SierpinskiTriangle.setOrder(newOrder);
			makelangeloRobotPanel.regenerate(generator);
		}
	}
}
