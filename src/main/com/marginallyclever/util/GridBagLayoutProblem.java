package com.marginallyclever.util;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class GridBagLayoutProblem extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GridBagLayoutProblem() {
		problemVersion();
		//closeButNotQuiteVersion();
	}

	@SuppressWarnings("unused")
	private void closeButNotQuiteVersion() {
		JPanel p1, p0;
		p0 = new JPanel();
		p0.setLayout(new BoxLayout(p0,BoxLayout.PAGE_AXIS));
		add(p0);
		
		p1 = new JPanel(new FlowLayout());
		p0.add(p1);
		p1.add(new JButton("A6"));

		p1 = new JPanel(new FlowLayout());
		p0.add(p1);
		p1.add(new JButton("B2"));
		p1.add(new JButton("B2"));
		p1.add(new JButton("B2"));

		p1 = new JPanel(new FlowLayout());
		p0.add(p1);
		p1.add(new JButton("C3"));
		p1.add(new JButton("C3"));

		p1 = new JPanel(new FlowLayout());
		p0.add(p1);
		p1.add(new JButton("D6"));
	}
	
	private void problemVersion() {
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill=GridBagConstraints.BOTH;
        c.anchor=GridBagConstraints.CENTER;
        c.gridy=0;
        c.weightx=1;
		
        c.gridwidth=6;
        c.gridx=0;		add(new JButton("A6"),c);

        c.gridy++;
        c.gridwidth=2;
        c.gridx=0;		add(new JButton("B2"),c);
        c.gridx+=2;		add(new JButton("B2"),c);
        c.gridx+=2;		add(new JButton("B2"),c);
        
        c.gridy++;
        c.gridwidth=3;
        c.gridx=0;		add(new JButton("C3"),c);
        c.gridx+=3;		add(new JButton("C3"),c);

        c.gridwidth=6;
        c.gridx=0;		add(new JButton("D6"),c);
	}
	
	public static void main(String[] argv) {
		GridBagLayoutProblem c = new GridBagLayoutProblem();
		c.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		c.pack();
		c.setVisible(true);
	}
}
