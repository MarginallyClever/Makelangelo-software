package com.marginallyclever.makelangelo.select;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.marginallyclever.makelangelo.Translator;

public class SelectFile extends Select implements ActionListener {
	private JLabel label;
	private JTextField field;
	private JButton chooseButton;
	
	public SelectFile(String labelValue,String defaultValue) {
		super();
		
		panel.setLayout(new GridBagLayout());
		GridBagConstraints buttonConstraint = new GridBagConstraints();

		buttonConstraint.anchor = GridBagConstraints.EAST;
		buttonConstraint.gridwidth = 1;

		label = new JLabel(Translator.get(labelValue),SwingConstants.LEFT);

		field = new JTextField(defaultValue, 32);

		chooseButton = new JButton(Translator.get("Choose"));
		chooseButton.addActionListener(this);
		
		panel.setLayout(new BorderLayout());
		panel.add(label,BorderLayout.LINE_START);
		panel.add(field,BorderLayout.CENTER);
		panel.add(chooseButton,BorderLayout.LINE_END);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		field.setText(selectFile(field.getText()));
	}
	
	public String getText() {
		return field.getText();
	}
	
	
	static private String selectFile(String cancelValue) {
		JFileChooser choose = new JFileChooser();
		int returnVal = choose.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = choose.getSelectedFile();
			return file.getAbsolutePath();
		} else {
			return cancelValue;
		}
	}
}
