package com.marginallyclever.artPipeline.nodes.panels;

import java.beans.PropertyChangeEvent;

import com.marginallyclever.artPipeline.nodes.Converter_RandomLines;
import com.marginallyclever.convenience.nodes.NodePanel;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectInteger;

/**
 * GUI for {@link Converter_RandomLines}
 * @author Dan Royer
 *
 */
public class Converter_RandomLines_Panel extends NodePanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Converter_RandomLines converter;
	private SelectInteger sizeField;	
	
	public Converter_RandomLines_Panel(Converter_RandomLines arg0) {
		super();
		converter=arg0;
		
		add(sizeField = new SelectInteger(Translator.get("ConverterRandomLinesCount"),converter.getLineCount()));
		finish();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		
		converter.setLineCount(sizeField.getValue());
		converter.restart();
	}
}
