package com.marginallyclever.convenience.select;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * A short text input field
 * @author Dan Royer
 * @since 7.24.0
 */
public class SelectString extends Select {
	private JLabel label;
	private JTextField field;
	
	public SelectString(String labelValue,String defaultValue) {
		super();
		
		label = new JLabel(labelValue,JLabel.LEADING);

		field = new JTextField(defaultValue, 16);
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
				notifyPropertyChangeListeners(new PropertyChangeEvent(parent, "value", null,null));
			}
		});
		//field.setBorder(new LineBorder(Color.BLACK));
		
		JPanel panel2 = new JPanel(new BorderLayout());
		panel2.add(field,BorderLayout.LINE_END);
		
		panel.add(label,BorderLayout.LINE_START);
		panel.add(panel2,BorderLayout.CENTER);
	}
	
	public String getText() {
		return field.getText();
	}
	
	/**
	 * Will notify observers that the value has changed.
	 * @param string
	 */
	public void setText(String string) {
		field.setText(string);
	}
}
