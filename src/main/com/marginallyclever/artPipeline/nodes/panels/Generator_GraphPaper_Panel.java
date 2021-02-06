package com.marginallyclever.artPipeline.nodes.panels;

import java.beans.PropertyChangeEvent;

import com.marginallyclever.artPipeline.NodePanel;
import com.marginallyclever.artPipeline.nodes.Generator_GraphPaper;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectFloat;

/**
 * Panel for {@link Generator_GraphPaper}
 * @author Dan Royer
 * TODO graph paper needs to offer measurements for the grid size
 */
public class Generator_GraphPaper_Panel extends NodePanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SelectFloat angle;
	private Generator_GraphPaper generator;
	
	public Generator_GraphPaper_Panel(Generator_GraphPaper generator_GraphPaper) {
		super();
		
		this.generator = generator_GraphPaper;

		add(angle = new SelectFloat(Translator.get("HilbertCurveOrder"),Generator_GraphPaper.getAngle()));
		finish();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);

		float newOrder = angle.getValue();
		
		if(newOrder != Generator_GraphPaper.getAngle()) {
			Generator_GraphPaper.setAngle(newOrder);
			generator.restart();
		}
	}
}
