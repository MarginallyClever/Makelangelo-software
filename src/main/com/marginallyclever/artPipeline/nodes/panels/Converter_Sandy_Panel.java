package com.marginallyclever.artPipeline.nodes.panels;

import java.beans.PropertyChangeEvent;

import com.marginallyclever.artPipeline.nodes.Converter_Sandy;
import com.marginallyclever.convenience.nodes.NodePanel;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectOneOfMany;
import com.marginallyclever.makelangelo.select.SelectSlider;

/**
 * GUI for {@link Converter_Sandy}
 * @author Dan Royer
 *
 */
public class Converter_Sandy_Panel extends NodePanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Converter_Sandy converter;
	private SelectSlider sizeField;	
	private SelectOneOfMany directionChoices;
	
	public Converter_Sandy_Panel(Converter_Sandy arg0) {
		super();
		converter=arg0;

		add(sizeField = new SelectSlider(Translator.get("SandyRings"),300,10,converter.getScale()));
		add(directionChoices = new SelectOneOfMany(Translator.get("Direction"),converter.getDirections(),converter.getDirectionIndex()));
		finish();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);

		converter.setScale(sizeField.getValue());
		converter.setDirection(directionChoices.getSelectedIndex());
		converter.restart();
	}
}
