package com.marginallyclever.makelangelo.select;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

/**
 * A JFormattedTextField that sets itself up to format floating point numbers.
 * @author Dan Royer
 * @since 7.24.0
 */
public class SelectFloat extends Select {
	private JLabel label;
	private JFormattedTextField field;
	private double value;

	protected SelectFloat() {
		super();
		createAndAttachFormatter(Locale.getDefault());
	}

	public SelectFloat(String labelKey, Locale locale, float defaultValue) {
		super();

		value = defaultValue;
		
		label = new JLabel(labelKey, JLabel.LEADING);
		field = new JFormattedTextField();
		createAndAttachFormatter(locale);
		Dimension d = field.getPreferredSize();
		d.width = 100;
		field.setPreferredSize(d);
		field.setMinimumSize(d);
		field.setValue(defaultValue);
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
				@SuppressWarnings("unused")
				float newNumber;

				try {
					newNumber = Float.valueOf(field.getText());
					field.setForeground(UIManager.getColor("Textfield.foreground"));
					if(value != newNumber) {
						value = newNumber;
						setChanged();
						notifyObservers();
					}
				} catch (NumberFormatException e1) {
					field.setForeground(Color.RED);
					return;
				}
			}
		});

		panel.add(label, BorderLayout.LINE_START);
		panel.add(field, BorderLayout.LINE_END);
	}

	public SelectFloat(String labelKey, Locale locale) {
		this(labelKey, locale, 0);
	}

	public SelectFloat(String labelKey, float defaultValue) {
		this(labelKey, Locale.getDefault(), defaultValue);
	}

	protected void createAndAttachFormatter(Locale locale) {
		NumberFormat nFloat = NumberFormat.getNumberInstance(locale);
		nFloat.setMinimumFractionDigits(1);
		nFloat.setMaximumFractionDigits(3);
		nFloat.setGroupingUsed(false);

		NumberFormatter nff = new NumberFormatter(nFloat);
		DefaultFormatterFactory factory = new DefaultFormatterFactory(nff);
		field.setFormatterFactory(factory);
	}

	public void setReadOnly() {
		field.setEditable(false);
	}

	/**
	 * @return last valid value typed into field
	 */
	public float getValue() {
		return (float)value;
	}

	public void setValue(float newValue) {
		if(newValue!=Float.parseFloat(field.getText())) {
			field.setText(Float.toString(newValue));
		}
	}
}
