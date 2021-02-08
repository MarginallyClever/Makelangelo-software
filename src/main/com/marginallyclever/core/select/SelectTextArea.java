package com.marginallyclever.core.select;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;

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
	private JLabel label;
	private JTextArea field;
	private JScrollPane pane;
	
	public SelectTextArea(String labelKey,String defaultText) {
		super();
		
		label = new JLabel(labelKey,JLabel.LEADING);
		field = new JTextArea(defaultText,4,20);
		field.setLineWrap(true);
		field.setWrapStyleWord(true);
		field.setBorder(BorderFactory.createLoweredBevelBorder());
		final Select parent = this;
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
				// TODO don't leave these null
				PropertyChangeEvent evt = new PropertyChangeEvent(parent,"field",null,null); 
				notifyPropertyChangeListeners(evt);
			}
		});
		
		pane = new JScrollPane(field);
		
		// resize the JScrollPane if the containing panel resizes
		panel.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				pane.setPreferredSize(panel.getSize());
				pane.revalidate();
			}
		});
		pane.setMinimumSize(new Dimension(500, 100));
		pane.setPreferredSize(new Dimension(200, 350));
		
		panel.add(label,BorderLayout.PAGE_START);
		panel.add(pane,BorderLayout.CENTER);
	}

	public String getText() {
		return field.getText();
	}
	
	public void setText(String str) {
		field.setText(str);
	}
}
