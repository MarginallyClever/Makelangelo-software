package com.marginallyclever.makelangelo.select;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.marginallyclever.convenience.GridBagConstraintsLabel;
import com.marginallyclever.convenience.GridBagConstraintsValue;

public class SelectTextArea extends Select {
	private JLabel label;
	private JTextArea field;
	
	public SelectTextArea(String labelKey,String defaultText) {
		super();
		
		label = new JLabel(labelKey,SwingConstants.LEFT);
		field = new JTextArea(defaultText,4,20);
		
		panel.setLayout(new GridLayout(0,1));
		panel.add(label,new GridBagConstraintsLabel());
		panel.add(field,new GridBagConstraintsValue());

		field.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				validate();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				validate();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				validate();
			}
			
			void validate() {
				setChanged();
				notifyObservers();
			}
		});
	}

	public String getText() {
		return field.getText();
	}
	
	public void setText(String str) {
		field.setText(str);
	}
}
