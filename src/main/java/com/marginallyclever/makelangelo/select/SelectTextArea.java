package com.marginallyclever.makelangelo.select;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * A text input dialog with some limited formatting options.
 * @author Dan Royer
 * @since 7.24.0
 */
public class SelectTextArea extends Select {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5584940645277024457L;
	private JLabel label;
	private JTextArea field;
	private JScrollPane pane;
	
	public SelectTextArea(String internalName,String labelKey,String defaultText) {
		super(internalName);
		//this.setBorder(BorderFactory.createLineBorder(Color.RED));
		
		label = new JLabel(labelKey,JLabel.LEADING);
		
		field = new JTextArea(defaultText,4,20);
		field.setLineWrap(true);
		field.setWrapStyleWord(true);
		field.setBorder(BorderFactory.createLoweredBevelBorder());
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
		
		pane = new JScrollPane(field);
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
}
