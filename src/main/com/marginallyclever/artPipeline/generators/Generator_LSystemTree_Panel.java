package com.marginallyclever.artPipeline.generators;

import java.util.Observable;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;
import com.marginallyclever.makelangelo.select.SelectSlider;

public class Generator_LSystemTree_Panel extends ImageGeneratorPanel {
	private SelectSlider field_order;
	private SelectSlider field_branches;
	private SelectSlider field_orderScale;
	private SelectSlider field_angle;
	private SelectSlider field_noise;
	private Generator_LSystemTree generator;
	
	Generator_LSystemTree_Panel(Generator_LSystemTree generator) {
		super();
		
		this.generator = generator;

		add(field_order      = new SelectSlider(Translator.get("HilbertCurveOrder"),10,1,generator.getOrder()));
		add(field_branches   = new SelectSlider(Translator.get("LSystemBranches"),8,1,generator.getBranches()));
		add(field_orderScale = new SelectSlider(Translator.get("LSystemOrderScale"),100,1,(int)(generator.getScale()*100)));
		add(field_angle      = new SelectSlider(Translator.get("LSystemAngle"),360,1,(int)generator.getAngle()));
		add(field_noise      = new SelectSlider(Translator.get("LSystemNoise"),100,0,(int)generator.getNoise()));
		add(new SelectReadOnlyText("<a href='https://en.wikipedia.org/wiki/L-system'>Learn more</a>"));
		finish();
	}

	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);
		
		generator.setOrder(field_order.getValue());
		generator.setBranches(field_branches.getValue());
		generator.setScale(field_orderScale.getValue()/100.0f);
		generator.setAngle(field_angle.getValue());
		generator.setNoise(field_noise.getValue());
		makelangeloRobotPanel.regenerate(generator);
	}
}
