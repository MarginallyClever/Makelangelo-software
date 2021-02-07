package com.marginallyclever.artPipeline.nodes.panels;

import java.beans.PropertyChangeEvent;

import com.marginallyclever.artPipeline.NodePanel;
import com.marginallyclever.artPipeline.nodes.fractals.Generator_SierpinskiTriangle;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;
import com.marginallyclever.makelangelo.select.SelectSlider;

/**
 * Panel for {@link Generator_SierpinskiTriangle}
 * @author Dan Royer
 *
 */
public class Generator_SierpinskiTriangle_Panel extends NodePanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	SelectSlider field_order;
	Generator_SierpinskiTriangle generator;
	
	public Generator_SierpinskiTriangle_Panel(Generator_SierpinskiTriangle generator) {
		super();
		
		this.generator = generator;
		
		add(field_order = new SelectSlider(Translator.get("HilbertCurveOrder"),10,1,Generator_SierpinskiTriangle.getOrder()));
		add(new SelectReadOnlyText("<a href='https://en.wikipedia.org/wiki/Sierpi%C5%84ski_triangle'>Learn more</a>"));
		finish();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		
		Generator_SierpinskiTriangle.setOrder(field_order.getValue());
		generator.restart();
	}
}
