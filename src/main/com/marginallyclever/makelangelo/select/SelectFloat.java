package com.marginallyclever.makelangelo.select;

import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.JFormattedTextField;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

/**
 * A JFormattedTextField that sets itself up to format floating point numbers. 
 * @author Dan Royer
 * @since 7.8.0
 */
public class SelectFloat extends JFormattedTextField {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2428427667156189335L;

	public SelectFloat(Locale locale,float defaultValue) {
		super();
		createAndAttachFormatter(locale);
		this.setValue(defaultValue);
	}

	public SelectFloat(Locale locale) {
		super();
		createAndAttachFormatter(locale);
	}
	
	public SelectFloat(float defaultValue) {
		super();
		createAndAttachFormatter(Locale.getDefault());
		this.setValue(defaultValue);
	}
	
	public SelectFloat() {
		super();
		createAndAttachFormatter(Locale.getDefault());
	}
	
	protected void createAndAttachFormatter(Locale locale) {
		NumberFormat nFloat = NumberFormat.getNumberInstance(locale);
		nFloat.setMinimumFractionDigits(1);
		nFloat.setMaximumFractionDigits(3);
		nFloat.setGroupingUsed(false);
		
		NumberFormatter nff = new NumberFormatter(nFloat);
		DefaultFormatterFactory factory = new DefaultFormatterFactory(nff);
		setFormatterFactory(factory);
	}
	
	public void setReadOnly() {
		setEditable(false);
	}
}
