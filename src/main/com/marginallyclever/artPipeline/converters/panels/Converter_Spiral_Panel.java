package com.marginallyclever.artPipeline.converters.panels;

import java.beans.PropertyChangeEvent;

import com.marginallyclever.artPipeline.TurtleNodePanel;
import com.marginallyclever.artPipeline.converters.Converter_Spiral;
import com.marginallyclever.makelangelo.select.SelectBoolean;

/**
 * GUI for {@link Converter_Spiral}
 * @author Dan Royer
 *
 */
public class Converter_Spiral_Panel extends TurtleNodePanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Converter_Spiral converter;
	private SelectBoolean toCornersField;
	
	public Converter_Spiral_Panel(Converter_Spiral arg0) {
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
