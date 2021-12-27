package com.marginallyclever.makelangelo.select;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
				notifyPropertyChangeListeners(null,field.getText());
			}
		});
		
		pane = new JScrollPane(field);
		
		// resize the JScrollPane if the containing panel resizes
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				pane.setPreferredSize(getSize());
				pane.revalidate();
			}
		});
		pane.setMinimumSize(new Dimension(500, 100));
		pane.setPreferredSize(new Dimension(200, 350));
		
		this.add(label,BorderLayout.PAGE_START);
		this.add(pane,BorderLayout.CENTER);
	}

	public String getText() {
		return field.getText();
	}
	
	public void setText(String str) {
		field.setText(str);
	}

	public void append(String str) {
	    field.append(str);
	}

	public void setEditable(boolean b) {
	    field.setEditable(b);
	}
	
	public JTextArea getFeild(){
	    return field;
	}
	
}
