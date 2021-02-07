package com.marginallyclever.artPipeline.nodes.panels;

import java.beans.PropertyChangeEvent;

import com.marginallyclever.artPipeline.deprecated.Converter_Wander;
import com.marginallyclever.convenience.nodes.NodePanel;
import com.marginallyclever.convenience.select.SelectBoolean;
import com.marginallyclever.convenience.select.SelectInteger;
import com.marginallyclever.makelangelo.Translator;

/**
 * GUI for {@link Converter_Wander}
 * @author Dan Royer
 *
 */
@Deprecated
public class Converter_Wander_Panel extends NodePanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Converter_Wander converter;
	private SelectInteger sizeField;
	private SelectBoolean cmykField;
	
	public Converter_Wander_Panel(Converter_Wander arg0) {
		super();
		
		converter=arg0;

		add(sizeField = new SelectInteger(Translator.get("ConverterWanderLineCount"),converter.getLineCount()));
		add(cmykField = new SelectBoolean(Translator.get("ConverterWanderCMYK"),converter.isCMYK()));
		finish();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		
		converter.setLineCount(sizeField.getValue());
		converter.setCMYK(cmykField.isSelected());
		converter.restart();
	}	
}
