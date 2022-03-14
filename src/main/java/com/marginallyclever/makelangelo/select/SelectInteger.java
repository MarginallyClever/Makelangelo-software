package com.marginallyclever.makelangelo.select;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;


/**
 * A JFormattedTextField that sets itself up to format integers. 
 * @author Dan Royer
 * @since 7.24.0
 */
public class SelectInteger extends Select {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3071134838779161675L;
	private JLabel label;
	private JFormattedTextField field;
	private int value;
	private Timer timer=null;

	public SelectInteger(String internalName,String labelKey,Locale locale,int defaultValue) {
		super(internalName);
		
		value = defaultValue;
		
		label = new JLabel(labelKey,JLabel.LEADING);
		field = new JFormattedTextField();
		field.setColumns(5);
		createAndAttachFormatter(locale);
		Dimension d = field.getPreferredSize();
		d.width = 100;
		field.setPreferredSize(d);
		field.setMinimumSize(d);
		field.setValue((Integer)defaultValue);
		field.setHorizontalAlignment(JTextField.RIGHT);
		field.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				validate();
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				validate();
			}

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				validate();
			}
			
			public void validate() {
				try {
					int newNumber = Integer.valueOf(field.getText());
					field.setForeground(UIManager.getColor("Textfield.foreground"));
					if(value != newNumber) {
						int oldValue = value;
						value = newNumber;
						
						if(timer!=null) timer.cancel();
						timer = new Timer("Delayed response");
						timer.schedule(new TimerTask() { 
							public void run() {
								firePropertyChange(oldValue,newNumber);
							}
						}, 100L); // brief delay in case someone is typing fast
					}
				} catch(NumberFormatException e1) {
					field.setForeground(Color.RED);
					return;
				}
			}
		});

		this.add(label,BorderLayout.LINE_START);
		this.add(field,BorderLayout.LINE_END);
	}

	public SelectInteger(String internalName,String labelKey,Locale locale) {
		this(internalName,labelKey,locale,0);
	}
	
	public SelectInteger(String internalName,String labelKey,int defaultValue) {
		this(internalName,labelKey,Locale.getDefault(),defaultValue);
	}
	
	public SelectInteger(String internalName) {
		super(internalName);
		createAndAttachFormatter(Locale.getDefault());
	}
	
	protected void createAndAttachFormatter(Locale locale) {
		NumberFormat nFloat = NumberFormat.getIntegerInstance(locale);
		nFloat.setGroupingUsed(false);
		
		NumberFormatter nff = new NumberFormatter(nFloat);
		DefaultFormatterFactory factory = new DefaultFormatterFactory(nff);
		field.setFormatterFactory(factory);
	}
	
	public void setReadOnly() {
		field.setEditable(false);
	}
	
	/**
	 * @return last valid integer typed into field.
	 */
	public int getValue() {
		return value;
	}
	
	public void setValue(int arg0) {
		field.setText(Integer.toString(arg0));
	}
}
