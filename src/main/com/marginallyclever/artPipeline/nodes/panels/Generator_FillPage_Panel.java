package com.marginallyclever.artPipeline.nodes.panels;

import java.beans.PropertyChangeEvent;

import com.marginallyclever.artPipeline.TurtleNodePanel;
import com.marginallyclever.artPipeline.nodes.Generator_FillPage;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectFloat;

/**
 * Panel for {@link Generator_FillPage}
 * @author Dan Royer
 *
 */
public class Generator_FillPage_Panel extends TurtleNodePanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SelectFloat angle;
	private Generator_FillPage generator;
	
	public Generator_FillPage_Panel(Generator_FillPage generator) {
		super();
		
		this.generator = generator;

		add(angle = new SelectFloat(Translator.get("HilbertCurveOrder"),Generator_FillPage.getAngle()));
		finish();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);

		float newOrder = angle.getValue();
		
		if(newOrder != Generator_FillPage.getAngle()) {
			Generator_FillPage.setAngle(newOrder);
			generator.restart();
		}
	}
}
