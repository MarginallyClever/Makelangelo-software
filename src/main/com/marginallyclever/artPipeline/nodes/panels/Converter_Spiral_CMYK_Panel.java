package com.marginallyclever.artPipeline.nodes.panels;

import java.beans.PropertyChangeEvent;

import com.marginallyclever.artPipeline.TurtleNodePanel;
import com.marginallyclever.artPipeline.nodes.Converter_Spiral_CMYK;
import com.marginallyclever.makelangelo.select.SelectBoolean;

/**
 * GUI for {@link Converter_Spiral_CMYK}
 * @author Dan Royer
 *
 */
public class Converter_Spiral_CMYK_Panel extends TurtleNodePanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Converter_Spiral_CMYK converter;
	private SelectBoolean toCornersField;
	
	public Converter_Spiral_CMYK_Panel(Converter_Spiral_CMYK arg0) {
		super();
		converter=arg0;
		
		add(toCornersField = new SelectBoolean("SpiralToCorners",converter.getToCorners()));
		finish();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);

		converter.setToCorners(toCornersField.isSelected());
		converter.restart();
	}
}
