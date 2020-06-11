package com.marginallyclever.makelangelo.select;

import java.awt.BorderLayout;

import javax.swing.JLabel;

public class SelectReadOnlyText extends Select {
	private JLabel label;
	
	public SelectReadOnlyText(String labelKey) {
		super();
		
		label = new JLabel("<html>"+ labelKey+"</html>",JLabel.LEADING);

		panel.add(label,BorderLayout.CENTER);
	}
}
