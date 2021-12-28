package com.marginallyclever.makelangelo.select;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

/**
 * A file selection dialog
 * @author Dan Royer
 * @since 7.24.0
 */
public class SelectFile extends Select {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1731350044217921795L;
	private JLabel label;
	private JTextField field;
	private JButton chooseButton;
	private FileFilter filter = null;
	private JFileChooser choose = new JFileChooser();
	
	public SelectFile(String internalName,String labelValue,String defaultValue) {
		super(internalName);
		
		label = new JLabel(labelValue,JLabel.LEADING);

		field = new JTextField(defaultValue, 16);
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
		//field.setBorder(new LineBorder(Color.BLACK));

		chooseButton = new JButton("...");
		chooseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				field.setText(selectFile(field.getText()));
			}
		});
		
		JPanel panel2 = new JPanel(new BorderLayout());
		panel2.add(field,BorderLayout.LINE_END);
		
		this.add(label,BorderLayout.LINE_START);
		this.add(panel2,BorderLayout.CENTER);
		this.add(chooseButton,BorderLayout.LINE_END);
	}
	
	public String getText() {
		return field.getText();
	}
	
	private String selectFile(String cancelValue) {
		choose.setFileFilter(filter);
		int returnVal = choose.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = choose.getSelectedFile();
			return file.getAbsolutePath();
		} else {
			return cancelValue;
		}
	}
	
	public void setFilter(FileFilter filter) {
		this.filter = filter;
	}

	/**
	 * Will notify observers that the value has changed.
	 * @param string
	 */
	public void setText(String string) {
		field.setText(string);
	}

	public void setPathOnly() {
		choose.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	}
	
	public void setFileOnly() {
		choose.setFileSelectionMode(JFileChooser.FILES_ONLY);
	}
}
