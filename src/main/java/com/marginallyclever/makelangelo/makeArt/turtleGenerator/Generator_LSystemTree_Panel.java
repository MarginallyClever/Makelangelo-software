package com.marginallyclever.makelangelo.makeArt.turtleGenerator;

import java.beans.PropertyChangeEvent;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;
import com.marginallyclever.makelangelo.select.SelectSlider;

/**
 * Panel for {@link Generator_LSystemTree}
 * @author Dan Royer
 *
 */
public class Generator_LSystemTree_Panel extends TurtleGeneratorPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SelectSlider field_order;
	private SelectSlider field_branches;
	private SelectSlider field_orderScale;
	private SelectSlider field_angle;
	private SelectSlider field_noise;
	private Generator_LSystemTree generator;
	
	Generator_LSystemTree_Panel(Generator_LSystemTree generator) {
		super();
		
		this.generator = generator;

		add(field_order      = new SelectSlider("order",Translator.get("HilbertCurveOrder"),10,1,generator.getOrder()));
		add(field_branches   = new SelectSlider("branches",Translator.get("LSystemBranches"),8,1,generator.getBranches()));
		add(field_orderScale = new SelectSlider("scale",Translator.get("LSystemOrderScale"),100,1,(int)(generator.getScale()*100)));
		add(field_angle      = new SelectSlider("angle",Translator.get("LSystemAngle"),360,1,(int)generator.getAngle()));
		add(field_noise      = new SelectSlider("noise",Translator.get("LSystemNoise"),100,0,(int)generator.getNoise()));
		add(new SelectReadOnlyText("url","<a href='https://en.wikipedia.org/wiki/L-system'>Learn more</a>"));
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		
		generator.setOrder(field_order.getValue());
		generator.setBranches(field_branches.getValue());
		generator.setScale(field_orderScale.getValue()/100.0f);
		generator.setAngle(field_angle.getValue());
		generator.setNoise(field_noise.getValue());
		generator.generate();
	}
}
