package com.marginallyclever.artPipeline.generators;

import java.util.Observable;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;
import com.marginallyclever.makelangelo.select.SelectSlider;

public class Generator_Dragon_Panel extends ImageGeneratorPanel {
	private SelectSlider fieldOrder;
	private Generator_Dragon generator;
	
	Generator_Dragon_Panel(Generator_Dragon generator) {
		super();
		this.generator = generator;

		add(fieldOrder = new SelectSlider(Translator.get("HilbertCurveOrder"),16,0,Generator_Dragon.getOrder()));
		add(new SelectReadOnlyText("<a href='https://en.wikipedia.org/wiki/Dragon_curve'>Learn more</a>"));
		finish();
	}

	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);
		
		int newOrder = fieldOrder.getValue();
		if(newOrder<1) newOrder=1;
		
		if(newOrder != Generator_Dragon.getOrder()) {
			Generator_Dragon.setOrder(newOrder);
			makelangeloRobotPanel.regenerate(generator);
		}
	}
}
