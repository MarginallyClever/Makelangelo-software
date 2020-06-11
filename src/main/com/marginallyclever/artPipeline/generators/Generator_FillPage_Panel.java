package com.marginallyclever.artPipeline.generators;

import java.util.Observable;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectFloat;

public class Generator_FillPage_Panel extends ImageGeneratorPanel {
	private SelectFloat angle;
	private Generator_FillPage generator;
	
	Generator_FillPage_Panel(Generator_FillPage generator) {
		super();
		
		this.generator = generator;

		add(angle = new SelectFloat(Translator.get("HilbertCurveOrder"),Generator_FillPage.getAngle()));
		finish();
	}

	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);

		float newOrder = angle.getValue();
		
		if(newOrder != Generator_FillPage.getAngle()) {
			Generator_FillPage.setAngle(newOrder);
			makelangeloRobotPanel.regenerate(generator);
		}
	}
}
