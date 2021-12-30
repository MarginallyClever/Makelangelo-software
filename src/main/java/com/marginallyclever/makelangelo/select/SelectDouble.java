package com.marginallyclever.makelangelo.select;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import com.marginallyclever.convenience.StringHelper;


/**
 * A JFormattedTextField that sets itself up to format floating point numbers.
 * @author Dan Royer
 * @since 7.24.0
 */
public class SelectDouble extends Select {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7904178922597267242L;
	private JLabel label;
	private JTextField field;
	private double value;
	private Timer timer;

	public SelectDouble(String internalName,String labelKey, Locale locale, double defaultValue) {
		super(internalName);

		value = defaultValue;
		
		label = new JLabel(labelKey, JLabel.LEADING);
		label.setName(internalName+".label");
		field = new JTextField();
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
					double newValue = Float.valueOf(field.getText());
					field.setForeground(UIManager.getColor("Textfield.foreground"));
					if(value != newValue) {
						double oldValue = value; 
						value = newValue;
						
						if(timer!=null) timer.cancel();
						timer = new Timer("Delayed response");
						timer.schedule(new TimerTask() { 
							public void run() {
								firePropertyChange(oldValue,newValue);
							}
						}, 100L); // brief delay in case someone is typing fast
					}
				} catch (NumberFormatException e) {
					field.setForeground(Color.RED);
					return;
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

	public void setReadOnly() {
		field.setEditable(false);
	}

	// @return last valid value typed into field
	public double getValue() {
		return value;
	}

	/**
	 * Set the value visible in the field.  Do not fire a property change event.
	 * @param newValue
	 */
	public void setValue(double newValue) {
		field.setText(StringHelper.formatDouble(newValue));
	}
	
	public String getText() {
		return field.getText();
	}
}
