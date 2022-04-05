package com.marginallyclever.makelangelo.makeart.turtlegenerator;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectBoolean;
import com.marginallyclever.makelangelo.select.SelectDouble;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;
import com.marginallyclever.makelangelo.select.SelectSlider;

import java.beans.PropertyChangeEvent;

/**
 * Panel for {@link Generator_Dragon}
 * @author Dan Royer
 *
 */
public class Generator_FlowField_Panel extends TurtleGeneratorPanel {
	private static final long serialVersionUID = 1L;
	private SelectDouble fieldScaleX;
	private SelectDouble fieldScaleY;
	private SelectDouble fieldOffsetX;
	private SelectDouble fieldOffsetY;
	private SelectSlider fieldStepSize;
	private SelectBoolean fieldFromEdge;
	private Generator_FlowField generator;

	Generator_FlowField_Panel(Generator_FlowField generator) {
		super();
		this.generator = generator;

		add(fieldScaleX = new SelectDouble("scaleX",Translator.get("Generator_FlowField.scaleX"),Generator_FlowField.getScaleX()));
		add(fieldScaleY = new SelectDouble("scaleY",Translator.get("Generator_FlowField.scaleY"),Generator_FlowField.getScaleY()));
		add(fieldOffsetX = new SelectDouble("offsetX",Translator.get("Generator_FlowField.offsetX"),Generator_FlowField.getOffsetX()));
		add(fieldOffsetY = new SelectDouble("offsetY",Translator.get("Generator_FlowField.offsetY"),Generator_FlowField.getOffsetY()));
		add(fieldStepSize = new SelectSlider("stepSize",Translator.get("Generator_FlowField.stepSize"),20,1,Generator_FlowField.getStepSize()));
		add(fieldFromEdge = new SelectBoolean("fromEdge",Translator.get("Generator_FlowField.fromEdge"),Generator_FlowField.getFromEdge()));
		add(new SelectReadOnlyText("url","<a href='https://en.wikipedia.org/wiki/Perlin_noise'>"+Translator.get("TurtleGenerators.LearnMore.Link.Text")+"</a>"));
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		
		double  scaleX   = fieldScaleX.getValue();
		double  scaleY   = fieldScaleY.getValue();
		double  offsetX  = fieldOffsetX.getValue();
		double  offsetY  = fieldOffsetY.getValue();
		int     stepSize = fieldStepSize.getValue();
		boolean fromEdge = fieldFromEdge.isSelected();

		if(scaleX != Generator_FlowField.getScaleX()
		|| scaleY != Generator_FlowField.getScaleY()
		|| offsetX != Generator_FlowField.getOffsetX()
		|| offsetY != Generator_FlowField.getOffsetY()
		|| stepSize != Generator_FlowField.getStepSize()
		|| fromEdge != Generator_FlowField.getFromEdge()) {
			Generator_FlowField.setScaleX(scaleX);
			Generator_FlowField.setScaleY(scaleY);
			Generator_FlowField.setOffsetX(offsetX);
			Generator_FlowField.setOffsetY(offsetY);
			Generator_FlowField.setStepSize(stepSize);
			Generator_FlowField.setFromEdge(fromEdge);
			generator.generate();
		}
	}
}
