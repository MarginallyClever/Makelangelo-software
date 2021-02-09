package com.marginallyclever.core.select;

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
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * A file selection dialog
 * @author Dan Royer
 * @since 7.24.0
 */
public class SelectFile extends Select {
	private JLabel label;
	private JTextField field;
	private JButton chooseButton;
	
	public SelectFile(String labelValue,FileNameExtensionFilter filter,String defaultValue) {
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

		chooseButton = new JButton("...");
		chooseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				field.setText(selectFile(filter,field.getText()));
			}
		});
		
		JPanel panel2 = new JPanel(new BorderLayout());
		panel2.add(field,BorderLayout.LINE_END);
		
		getPanel().add(label,BorderLayout.LINE_START);
		getPanel().add(panel2,BorderLayout.CENTER);
		getPanel().add(chooseButton,BorderLayout.LINE_END);
	}
	
	public String getText() {
		return field.getText();
	}
	
	static private String selectFile(FileNameExtensionFilter filter,String cancelValue) {
		JFileChooser choose = new JFileChooser();
		choose.setFileFilter(filter);
		int returnVal = choose.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = choose.getSelectedFile();
			return file.getAbsolutePath();
		} else {
			return cancelValue;
		}
	}

	/**
	 * Will notify observers that the value has changed.
	 * @param string
	 */
	public void setText(String string) {
		field.setText(string);
	}
}
