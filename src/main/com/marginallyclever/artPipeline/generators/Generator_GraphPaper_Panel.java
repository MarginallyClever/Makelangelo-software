package com.marginallyclever.artPipeline.generators;

import java.util.Observable;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectFloat;

public class Generator_GraphPaper_Panel extends ImageGeneratorPanel {
	private SelectFloat angle;
	private Generator_GraphPaper generator;
	
	Generator_GraphPaper_Panel(Generator_GraphPaper generator_GraphPaper) {
		super();
		
		this.generator = generator_GraphPaper;

		add(angle = new SelectFloat(Translator.get("HilbertCurveOrder"),Generator_GraphPaper.getAngle()));
		finish();
	}

	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);

		float newOrder = angle.getValue();
		
		if(newOrder != Generator_GraphPaper.getAngle()) {
			Generator_GraphPaper.setAngle(newOrder);
			makelangeloRobotPanel.regenerate(generator);
		}
	}
}
