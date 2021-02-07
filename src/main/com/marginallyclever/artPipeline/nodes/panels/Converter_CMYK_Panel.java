package com.marginallyclever.artPipeline.nodes.panels;

import java.beans.PropertyChangeEvent;

import com.marginallyclever.artPipeline.nodes.Converter_CMYK;
import com.marginallyclever.convenience.nodes.NodePanel;
import com.marginallyclever.convenience.select.SelectInteger;
import com.marginallyclever.convenience.select.SelectReadOnlyText;
import com.marginallyclever.makelangelo.Translator;

/**
 * GUI for {@link Converter_CMYK}
 * @author Dan Royer
 *
 */
public class Converter_CMYK_Panel extends NodePanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Converter_CMYK converter;
	private SelectInteger passesField;
	
	public Converter_CMYK_Panel(Converter_CMYK arg0) {
		super();
		converter=arg0;
		
		add(passesField = new SelectInteger(Translator.get("ConverterCMYKPasses"),converter.getPasses()));
		add(new SelectReadOnlyText(Translator.get("ConverterCMYKNote")));
		finish();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		
		converter.setPasses(passesField.getValue());
		converter.restart();
	}
}
