package com.marginallyclever.artPipeline.converters;

import java.beans.PropertyChangeEvent;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectInteger;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;

/**
 * GUI for {@link Converter_CMYK}
 * @author Dan Royer
 *
 */
public class Converter_CMYK_Panel extends ImageConverterPanel {
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
