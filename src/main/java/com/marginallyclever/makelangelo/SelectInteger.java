package com.marginallyclever.makelangelo;

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
public class SelectInteger extends JFormattedTextField {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8222996536287315655L;

	public SelectInteger(Locale locale,int defaultValue) {
		super();
		createAndAttachFormatter(locale);
		this.setValue((Integer)defaultValue);
	}

	public SelectInteger(Locale locale) {
		super();
		createAndAttachFormatter(locale);
	}
	
	public SelectInteger(int defaultValue) {
		super();
		createAndAttachFormatter(Locale.getDefault());
		this.setValue((Integer)defaultValue);
	}
	
	public SelectInteger() {
		super();
		createAndAttachFormatter(Locale.getDefault());
	}
	
	protected void createAndAttachFormatter(Locale locale) {
		NumberFormat nFloat = NumberFormat.getIntegerInstance(locale);
		nFloat.setGroupingUsed(false);
		
		NumberFormatter nff = new NumberFormatter(nFloat);
		DefaultFormatterFactory factory = new DefaultFormatterFactory(nff);
		setFormatterFactory(factory);
	}
	
	public void setReadOnly() {
		setEditable(false);
	}
}
