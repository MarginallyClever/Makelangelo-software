package com.marginallyclever.makelangeloRobot.settings;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import com.marginallyclever.makelangelo.Translator;

public class SelectColor extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1328306913825662751L;
	private JLabel label;
	private JLabel fieldValue;
	private JButton chooseButton;
	private JComponent parent;
	
	public SelectColor(JComponent parent,String labelValue,Color defaultValue) {
		this.parent = parent;
		this.setLayout(new GridBagLayout());
		GridBagConstraints labelConstraint = new GridBagConstraints();
		GridBagConstraints fieldConstraint = new GridBagConstraints();
		GridBagConstraints buttonConstraint = new GridBagConstraints();

		labelConstraint.anchor = GridBagConstraints.WEST;
		labelConstraint.fill = GridBagConstraints.HORIZONTAL;
		labelConstraint.gridwidth = 1;
		labelConstraint.weightx=1;
		labelConstraint.gridx=0;
		labelConstraint.gridy=0;
		labelConstraint.insets = new Insets(3,3,3,3);
		
		fieldConstraint.anchor = GridBagConstraints.EAST;
		fieldConstraint.gridwidth = 1;
		fieldConstraint.gridx = labelConstraint.gridwidth;
		fieldConstraint.gridy=0;
		fieldConstraint.insets = new Insets(3,3,3,1);

		buttonConstraint.anchor = GridBagConstraints.EAST;
		buttonConstraint.gridwidth = 1;
		buttonConstraint.gridx = fieldConstraint.gridwidth + fieldConstraint.gridx;
		buttonConstraint.gridy=0;
		buttonConstraint.insets = new Insets(3,0,3,3);

		label = new JLabel(Translator.get(labelValue));
		this.add(label, labelConstraint);

		fieldValue = new JLabel("");
		fieldValue.setOpaque(true);
		fieldValue.setMinimumSize(new Dimension(80,20));
		fieldValue.setMaximumSize(fieldValue.getMinimumSize());
		fieldValue.setPreferredSize(fieldValue.getMinimumSize());
		fieldValue.setSize(fieldValue.getMinimumSize());
		fieldValue.setBackground(defaultValue);

		fieldValue.setBorder(new LineBorder(Color.BLACK));
		this.add(fieldValue, fieldConstraint);

		chooseButton = new JButton(Translator.get("Choose"));
		chooseButton.addActionListener(this);
		this.add(chooseButton, buttonConstraint);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Color c = JColorChooser.showDialog(parent, label.getText(), fieldValue.getBackground());
		fieldValue.setBackground(c);
	}
	
	public Color getColor() {
		return fieldValue.getBackground();
	}
}
