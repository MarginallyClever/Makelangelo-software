package com.marginallyclever.makelangelo.select;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

/**
 * A short text input.
 * @author Dan Royer
 * @since 7.24.0
 */
public class SelectPassword extends Select {
	private final JPasswordField field;

	public SelectPassword(String internalName, String labelKey, String defaultText) {
		super(internalName);

		JLabel label = new JLabel(labelKey, JLabel.LEADING);

		field = new JPasswordField(defaultText);
		field.setEchoChar('*');
		Dimension d = field.getPreferredSize();
		d.width = 100;
		field.setPreferredSize(d);
		field.setMinimumSize(d);

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
				fireSelectEvent(null,field.getPassword());
			}
		});

		this.add(label,BorderLayout.LINE_START);
		this.add(field,BorderLayout.LINE_END);
	}

	public String getPassword() {
		return new String(field.getPassword());
	}
	
	public void setPassword(String str) {
		field.setText(str);
	}

	public boolean isEditable() {
		return field.isEditable();
	}

	public void setEditable(boolean b) {
		field.setEditable(b);
	}

	public boolean getDragEnabled() {
		return field.getDragEnabled();
	}

	public void setDragEnabled(boolean b) {
		field.setDragEnabled(b);
	}
}
