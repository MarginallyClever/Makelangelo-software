package com.marginallyclever.makelangelo.select;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class SelectReadOnlyText extends Select {
	private JLabel label;
	
	public SelectReadOnlyText(String labelKey) {
		super();
		
		label = new JLabel(labelKey,SwingConstants.LEFT);

		panel.setLayout(new BorderLayout());
		panel.add(label,BorderLayout.CENTER);
	}
}
