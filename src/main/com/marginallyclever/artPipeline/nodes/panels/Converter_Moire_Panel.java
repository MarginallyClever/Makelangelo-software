package com.marginallyclever.artPipeline.nodes.panels;

import java.beans.PropertyChangeEvent;

import com.marginallyclever.artPipeline.NodePanel;
import com.marginallyclever.artPipeline.nodes.Converter_Moire;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectFloat;
import com.marginallyclever.makelangelo.select.SelectOneOfMany;

/**
 * GUI for {@link Converter_Moire}
 * @author Dan Royer
 *
 */
public class Converter_Moire_Panel extends NodePanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Converter_Moire converter;
	private SelectFloat scaleField;	
	private SelectOneOfMany directionChoices;
	
	public Converter_Moire_Panel(Converter_Moire arg0) {
		super();
		converter=arg0;

		add(scaleField = new SelectFloat(Translator.get("HilbertCurveSize"),converter.getScale()));
		add(directionChoices = new SelectOneOfMany(Translator.get("Direction"),converter.getDirections(),converter.getDirectionIndex()));
		finish();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		
		converter.setScale(scaleField.getValue());
		converter.setDirectionIndex(directionChoices.getSelectedIndex());
		converter.restart();
	}
}
