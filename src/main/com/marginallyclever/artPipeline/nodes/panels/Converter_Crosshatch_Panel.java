package com.marginallyclever.artPipeline.nodes.panels;

import java.beans.PropertyChangeEvent;

import com.marginallyclever.artPipeline.NodePanel;
import com.marginallyclever.artPipeline.nodes.Converter_Crosshatch;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectSlider;

/**
 * GUI for {@link Converter_Crosshatch}
 * @author Dan Royer
 *
 */
public class Converter_Crosshatch_Panel extends NodePanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Converter_Crosshatch converter;
	private	SelectSlider intensityField;
	
	public Converter_Crosshatch_Panel(Converter_Crosshatch arg0) {
		super();
		converter=arg0;
		
		add(intensityField = new SelectSlider(Translator.get("ConverterIntensity"),200,1,(int)(converter.getIntensity()*10.0)));
		finish();
	}


	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		
		converter.setIntensity((float)intensityField.getValue()/10.0f);
		converter.restart();
	}
}
