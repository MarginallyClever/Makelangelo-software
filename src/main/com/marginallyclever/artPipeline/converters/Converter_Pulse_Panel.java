package com.marginallyclever.artPipeline.converters;

import java.beans.PropertyChangeEvent;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectFloat;
import com.marginallyclever.makelangelo.select.SelectOneOfMany;

/**
 * GUI for {@link Converter_Pulse}
 * @author Dan Royer
 *
 */
public class Converter_Pulse_Panel extends ImageConverterPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Converter_Pulse converter;
	private SelectFloat sizeField;	
	private SelectOneOfMany directionChoices;
	
	public Converter_Pulse_Panel(Converter_Pulse arg0) {
		super();
		converter=arg0;

		add(sizeField = new SelectFloat(Translator.get("HilbertCurveSize"),converter.getScale()));
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
