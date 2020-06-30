package com.marginallyclever.makelangelo.select;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Locale;

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
public class SelectFloat extends Select {
	private JLabel label;
	private JTextField field;
	private float value;

	public SelectFloat(String labelKey, Locale locale, float defaultValue) {
		super();

		value = defaultValue;
		
		label = new JLabel(labelKey, JLabel.LEADING);
		field = new JTextField();
		
		Dimension d = field.getPreferredSize();
		d.width = 100;
		field.setPreferredSize(d);
		field.setMinimumSize(d);
		field.setText(StringHelper.formatFloat(defaultValue));
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
					newNumber = Float.valueOf(field.getText());
				} catch (NumberFormatException e) {
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

	public void setReadOnly() {
		field.setEditable(false);
	}

	// @return last valid value typed into field
	public float getValue() {
		return value;
	}

	public void setValue(float newValue) {
		if(newValue!=value) {
			//Log.message("new "+newValue+" old "+oldValue);
			field.setText(StringHelper.formatFloat(newValue));
		}
	}
	
	public String getText() {
		return field.getText();
	}
}
