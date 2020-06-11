package com.marginallyclever.makelangelo.select;

import java.awt.BorderLayout;
import java.awt.Color;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

import com.marginallyclever.makelangelo.Translator;

/**
 * A JFormattedTextField that sets itself up to format floating point numbers. 
 * @author Dan Royer
 * @since 7.8.0
 */
public class SelectInteger extends Select {
	private JLabel label;
	private JFormattedTextField field;

	public SelectInteger(String labelKey,Locale locale,int defaultValue) {
		super();
		
		label = new JLabel(labelKey,SwingConstants.LEFT);
		field = new JFormattedTextField(); 
		createAndAttachFormatter(locale);
		field.setValue((Integer)defaultValue);

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
				@SuppressWarnings("unused")
				double newNumber;
				
				try {
					newNumber = Integer.valueOf(field.getText());
					field.setForeground(UIManager.getColor("Textfield.foreground"));
				} catch(NumberFormatException e1) {
					field.setForeground(Color.RED);
					return;
				}
			}
		});

		panel.setLayout(new BorderLayout());
		panel.add(label,BorderLayout.LINE_START);
		panel.add(field,BorderLayout.LINE_END);
	}

	public SelectInteger(String labelKey,Locale locale) {
		this(labelKey,locale,0);
	}
	
	public SelectInteger(String labelKey,int defaultValue) {
		this(labelKey,Locale.getDefault(),defaultValue);
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
		field.setFormatterFactory(factory);
	}
	
	public void setReadOnly() {
		field.setEditable(false);
	}
	
	public int getValue() {
		return Integer.getInteger(field.getText());
	}
}
