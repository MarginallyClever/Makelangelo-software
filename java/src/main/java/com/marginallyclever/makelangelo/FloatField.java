package com.marginallyclever.makelangelo;

import java.text.NumberFormat;

import javax.swing.JFormattedTextField;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

/**
 * A JFormattedTextField that sets itself up to format floating point numbers. 
 * @author Dan Royer
 * @since 7.8.0
 */
public class FloatField extends JFormattedTextField {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2428427667156189335L;

	public FloatField(float defaultValue) {
		super();
		createAndAttachFormatter();
		this.setValue(defaultValue);
	}
	
	
	public FloatField() {
		super();
		createAndAttachFormatter();
	}
	
	protected void createAndAttachFormatter() {
		NumberFormat nFloat = NumberFormat.getNumberInstance();
		nFloat.setMinimumFractionDigits(1);
		nFloat.setMaximumFractionDigits(3);
		nFloat.setGroupingUsed(false);
		
		NumberFormatter nff = new NumberFormatter(nFloat);
		DefaultFormatterFactory factory = new DefaultFormatterFactory(nff);
		setFormatterFactory(factory);
	}
}
