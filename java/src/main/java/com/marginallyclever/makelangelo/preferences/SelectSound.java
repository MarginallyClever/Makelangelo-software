package com.marginallyclever.makelangelo.preferences;

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

import com.marginallyclever.makelangelo.Translator;

public class SelectSound extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1328306913825662751L;
	private JLabel label;
	private JTextField fieldValue;
	private JButton chooseButton;
	
	public SelectSound(String labelValue,String defaultValue) {
		this.setLayout(new GridBagLayout());
		GridBagConstraints labelConstraint = new GridBagConstraints();
		GridBagConstraints fieldConstraint = new GridBagConstraints();
		GridBagConstraints buttonConstraint = new GridBagConstraints();

		labelConstraint.anchor = GridBagConstraints.EAST;
		labelConstraint.fill = GridBagConstraints.HORIZONTAL;
		labelConstraint.gridwidth = 4;
		labelConstraint.gridx = 0;
		labelConstraint.gridy=0;
		
		fieldConstraint.anchor = GridBagConstraints.EAST;
		fieldConstraint.gridwidth = labelConstraint.gridwidth-1;
		fieldConstraint.gridx = 0;

		buttonConstraint.anchor = GridBagConstraints.EAST;
		buttonConstraint.gridwidth = 1;
		buttonConstraint.gridx = fieldConstraint.gridwidth + fieldConstraint.gridx;

		fieldConstraint.gridy=buttonConstraint.gridy=1;

		label = new JLabel(Translator.get(labelValue));
		this.add(label, labelConstraint);

		fieldValue = new JTextField(defaultValue, 32);
		this.add(fieldValue, fieldConstraint);

		chooseButton = new JButton(Translator.get("Choose"));
		chooseButton.addActionListener(this);
		this.add(chooseButton, buttonConstraint);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		fieldValue.setText(selectFile(fieldValue.getText()));
	}
	
	public String getText() {
		return fieldValue.getText();
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
