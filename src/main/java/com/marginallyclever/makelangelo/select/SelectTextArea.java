package com.marginallyclever.makelangelo.select;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

/**
 * A text input dialog with some limited formatting options.
 * @author Dan Royer
 * @since 7.24.0
 */
public class SelectTextArea extends Select {
	private final JTextArea field;

	public SelectTextArea(String internalName,String labelKey,String defaultText) {
		super(internalName);
		//this.setBorder(BorderFactory.createLineBorder(Color.RED));

		JLabel label = new JLabel(labelKey, JLabel.LEADING);
		
		field = new JTextArea(defaultText,4,20);
		field.setLineWrap(true);
		field.setWrapStyleWord(true);
		field.setBorder(BorderFactory.createLoweredBevelBorder());
		field.setFont(UIManager.getFont("Label.font"));
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
				firePropertyChange(null,field.getText());
			}
		});

		JScrollPane pane = new JScrollPane(field);
		pane.setPreferredSize(new Dimension(200, 150));
		
		this.add(label,BorderLayout.PAGE_START);
		this.add(pane,BorderLayout.CENTER);
	}

	public String getText() {
		return field.getText();
	}
	
	public void setText(String str) {
		field.setText(str);
	}

	public void setLineWrap(boolean wrap) {
		field.setLineWrap(wrap);
	}

	public boolean getLineWrap() {
		return field.getLineWrap();
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
