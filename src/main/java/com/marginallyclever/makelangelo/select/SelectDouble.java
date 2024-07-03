package com.marginallyclever.makelangelo.select;

import com.marginallyclever.convenience.helpers.StringHelper;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


/**
 * A JFormattedTextField that sets itself up to format floating point numbers.
 * @author Dan Royer
 * @since 7.24.0
 */
public class SelectDouble extends Select {
	private final JTextField field = new JTextField();
	private double value;
	private Timer timer;

	public SelectDouble(String internalName,String labelKey, Locale locale, double defaultValue) {
		super(internalName);

		value = defaultValue;

		JLabel label = new JLabel(labelKey, JLabel.LEADING);
		label.setName(internalName+".label");

		field.setName(internalName+".field");
		Dimension d = field.getPreferredSize();
		d.width = 100;
		field.setPreferredSize(d);
		field.setMinimumSize(d);
		field.setText(StringHelper.formatDouble(defaultValue));
		field.setHorizontalAlignment(JTextField.RIGHT);

		field.getDocument().addDocumentListener(new DocumentListener() {
        	@Override
			public void changedUpdate(DocumentEvent arg0) {
				if(arg0.getLength()==0) return;
				validate();
			}

        	@Override
			public void insertUpdate(DocumentEvent arg0) {
				if(arg0.getLength()==0) return;
				validate();
			}

        	@Override
			public void removeUpdate(DocumentEvent arg0) {
				if(arg0.getLength()==0) return;
				validate();
			}

			public void validate() {
				try {
					double newValue = Float.parseFloat(field.getText());
					field.setForeground(UIManager.getColor("Textfield.foreground"));
					if(value != newValue) {
						double oldValue = value; 
						value = newValue;
						
						if(timer!=null) timer.cancel();
						timer = new Timer("Delayed response");
						timer.schedule(new TimerTask() { 
							public void run() {
								fireSelectEvent(oldValue,newValue);
							}
						}, 100L); // brief delay in case someone is typing fast
					}
				} catch (NumberFormatException e) {
					field.setForeground(Color.RED);
				}
			}
		});

		this.add(label, BorderLayout.LINE_START);
		this.add(field, BorderLayout.LINE_END);
	}

	public SelectDouble(String internalName,String labelKey, Locale locale) {
		this(internalName,labelKey, locale, 0);
	}

	public SelectDouble(String internalName,String labelKey, double defaultValue) {
		this(internalName,labelKey, Locale.getDefault(), defaultValue);
	}

	public SelectDouble(String internalName,String labelKey) {
		this(internalName,labelKey, Locale.getDefault(), 0);
	}

	protected SelectDouble(String internalName) {
		this(internalName,"", Locale.getDefault(), 0);
	}

	public void setReadOnly(boolean state) {
		field.setEditable(!state);
	}

	// @return last valid value typed into field
	public double getValue() {
		return value;
	}

	/**
	 * Set the value visible in the field.  Do not fire a property change event.
	 * @param newValue the new value to display
     */
	public void setValue(double newValue) {
		field.setText(StringHelper.formatDouble(newValue));
	}
	
	public String getText() {
		return field.getText();
	}
}
