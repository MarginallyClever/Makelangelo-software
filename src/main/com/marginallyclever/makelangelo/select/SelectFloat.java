package com.marginallyclever.makelangelo.select;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.text.DecimalFormat;
import java.text.ParseException;
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

	public SelectFloat(String labelKey, Locale locale, float defaultValue) {
		super();

		value = defaultValue;
		
		label = new JLabel(labelKey, JLabel.LEADING);
		createAndAttachFormatter(locale);
		Dimension d = field.getPreferredSize();
		d.width = 100;
		field.setPreferredSize(d);
		field.setMinimumSize(d);
		field.setValue(defaultValue);
		field.setFocusLostBehavior(JFormattedTextField.PERSIST);
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
				float newNumber;

				try {
					newNumber = (float)(double)field.getFormatter().stringToValue(field.getText());
				} catch (ParseException e) {
					field.setForeground(Color.RED);
					return;
				}
				
				field.setForeground(UIManager.getColor("Textfield.foreground"));
				if(value != newNumber) {
					value = newNumber;
					setChanged();
					notifyObservers();
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

	public SelectFloat(String labelKey) {
		this(labelKey, Locale.getDefault(), 0);
	}

	protected SelectFloat() {
		this("", Locale.getDefault(), 0);
	}

	protected void createAndAttachFormatter(Locale locale) {
		DecimalFormat nFloat = (DecimalFormat)DecimalFormat.getInstance(locale);
		nFloat.setMinimumFractionDigits(1);
		nFloat.setMaximumFractionDigits(3);
		nFloat.setDecimalSeparatorAlwaysShown(true);
		nFloat.setGroupingUsed(false);

		NumberFormatter nff = new NumberFormatter(nFloat);
		DefaultFormatterFactory factory = new DefaultFormatterFactory(nff);
		field = new JFormattedTextField(factory);
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
		//float oldValue = (float)field.getValue();
		//if(newValue!=oldValue) {
			//Log.message("new "+newValue+" old "+oldValue);
			field.setValue(newValue);
		//}
	}
	
	public String getText() {
		return field.getText();
	}
}
