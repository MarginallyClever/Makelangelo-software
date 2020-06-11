package com.marginallyclever.makelangelo.select;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class SelectPanel implements Observer {
	protected JPanel panel;
	private GridBagConstraints gbc;
	
	public SelectPanel() {
		super();
		
		panel = new JPanel();
		//p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
		panel.setLayout(new GridBagLayout());
		//p.setBorder(new LineBorder(Color.RED));
		panel.setBorder(new EmptyBorder(5,5,5,5));

		gbc = new GridBagConstraints();
		gbc.weightx=1;
		gbc.gridx  =0;
		gbc.gridy  =0;
		gbc.fill      = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.insets.set(5, 5, 5, 5); 
	}
	
	public void add(Select c) {
		gbc.gridy++;
		panel.add(c.getPanel(),gbc);
		c.addObserver(this);
	}
	
	public void finish() {
		gbc.weighty=1;
		gbc.gridy++;
		panel.add(new JLabel(""),gbc);
	}
	
	public JPanel getPanel() {
		return panel;
	}

	@Override
	public void update(Observable o, Object arg) {}
}
