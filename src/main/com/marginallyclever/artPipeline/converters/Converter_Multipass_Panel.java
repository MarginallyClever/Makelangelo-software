package com.marginallyclever.artPipeline.converters;

import java.beans.PropertyChangeEvent;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectDouble;
import com.marginallyclever.makelangelo.select.SelectInteger;

/**
 * GUI for {@link Converter_Multipass_Panel}
 * @author Dan Royer
 *
 */
public class Converter_Multipass_Panel extends ImageConverterPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Converter_Multipass converter;
	private SelectDouble   angleField;
	private SelectInteger passesField;
	
	public Converter_Multipass_Panel(Converter_Multipass arg0) {
		super();
		converter=arg0;
		
		add(angleField = new SelectDouble(Translator.get("ConverterMultipassAngle"),converter.getAngle()));
		add(passesField = new SelectInteger(Translator.get("ConverterMultipassLevels"),converter.getPasses()));
		finish();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		
		converter.setAngle(angleField.getValue());
		converter.setPasses(passesField.getValue());
		converter.restart();
	}
}
