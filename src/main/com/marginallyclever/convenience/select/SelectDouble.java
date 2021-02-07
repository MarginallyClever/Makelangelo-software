package com.marginallyclever.convenience.select;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.marginallyclever.core.StringHelper;


/**
 * A JFormattedTextField that sets itself up to format doubles.
 * @author Dan Royer
 * @since 7.24.0
 */
public class SelectDouble extends Select {
	private JLabel label;
	private JTextField field;
	private double value;

	public SelectDouble(String labelKey, Locale locale, double defaultValue) {
		super();

		value = defaultValue;
		
		label = new JLabel(labelKey, JLabel.LEADING);
		field = new JTextField();
		
		Dimension d = field.getPreferredSize();
		d.width = 100;
		field.setPreferredSize(d);
		field.setMinimumSize(d);
		field.setText(StringHelper.formatDouble(defaultValue));
		field.setHorizontalAlignment(JTextField.RIGHT);
		Select parent = this;
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
				double newNumber;

				try {
					newNumber = Double.valueOf(field.getText());
				} catch (NumberFormatException e) {
					field.setForeground(Color.RED);
					return;
				}
				
				field.setForeground(UIManager.getColor("Textfield.foreground"));
				if(value != newNumber) {
					notifyPropertyChangeListeners(new PropertyChangeEvent(parent, "value", value, newNumber));
					value = newNumber;
				}
			}
		});

		panel.add(label, BorderLayout.LINE_START);
		panel.add(field, BorderLayout.LINE_END);
	}

	public SelectDouble(String labelKey, Locale locale) {
		this(labelKey, locale, 0);
	}

	public SelectDouble(String labelKey, double defaultValue) {
		this(labelKey, Locale.getDefault(), defaultValue);
	}

	public SelectDouble(String labelKey) {
		this(labelKey, Locale.getDefault(), 0);
	}

	protected SelectDouble() {
		this("", Locale.getDefault(), 0);
	}

	public void setReadOnly() {
		field.setEditable(false);
	}

	// @return last valid value typed into field
	public double getValue() {
		return value;
	}

	public void setValue(double newValue) {
		if(newValue!=value) {
			//Log.message("new "+newValue+" old "+oldValue);
			field.setText(StringHelper.formatDouble(newValue));
		}
	}
	
	public String getText() {
		return field.getText();
	}
}
