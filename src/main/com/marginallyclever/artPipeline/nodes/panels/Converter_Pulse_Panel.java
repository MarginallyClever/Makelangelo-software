package com.marginallyclever.artPipeline.nodes.panels;

import java.beans.PropertyChangeEvent;

import com.marginallyclever.artPipeline.nodes.Converter_Pulse;
import com.marginallyclever.convenience.nodes.NodePanel;
import com.marginallyclever.convenience.select.SelectDouble;
import com.marginallyclever.convenience.select.SelectOneOfMany;
import com.marginallyclever.makelangelo.Translator;

/**
 * GUI for {@link Converter_Pulse}
 * @author Dan Royer
 *
 */
public class Converter_Pulse_Panel extends NodePanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Converter_Pulse converter;
	private SelectDouble sizeField;	
	private SelectOneOfMany directionChoices;
	
	public Converter_Pulse_Panel(Converter_Pulse arg0) {
		super();
		converter=arg0;

		add(sizeField = new SelectDouble(Translator.get("HilbertCurveSize"),(float)converter.getScale()));
		add(directionChoices = new SelectOneOfMany(Translator.get("Direction"),converter.getDirections(),converter.getDirectionIndex()));
		finish();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);

		converter.setScale(sizeField.getValue());
		converter.setDirectionIndex(directionChoices.getSelectedIndex());
		converter.restart();
	}
}
