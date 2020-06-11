package com.marginallyclever.artPipeline.generators;

import java.util.Observable;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectFloat;
import com.marginallyclever.makelangelo.select.SelectInteger;

public class Generator_LSystemTree_Panel extends ImageGeneratorPanel {
	private SelectInteger field_order;
	private SelectInteger field_branches;
	private SelectFloat field_orderScale;
	private SelectFloat field_angle;
	private Generator_LSystemTree generator;
	
	Generator_LSystemTree_Panel(Generator_LSystemTree generator) {
		super();
		
		this.generator = generator;

		add(field_order      = new SelectInteger(Translator.get("HilbertCurveOrder"),generator.getOrder     ()));
		add(field_branches   = new SelectInteger(Translator.get("LSystemBranches"),generator.getBranches    ()));
		add(field_orderScale = new SelectFloat(Translator.get("LSystemOrderScale"),(float)generator.getScale()));
		add(field_angle      = new SelectFloat(Translator.get("LSystemAngle"),(float)generator.getAngle     ()));
		finish();
	}

	@Override
	public void update(Observable o, Object arg) {
		super.update(o, arg);
		
		generator.setOrder(field_order.getValue());
		generator.setBranches(field_branches.getValue());
		generator.setScale(field_orderScale.getValue());
		generator.setAngle(field_angle.getValue());
		makelangeloRobotPanel.regenerate(generator);
	}
}
