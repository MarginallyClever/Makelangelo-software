package com.marginallyclever.makelangelo;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.select.SelectPanel;

public class ArtPanel2 {
	protected JFrame parentFrame;
	
	public ArtPanel2() {
		super();
	}
	
	public JPanel buildPanel(JFrame frame) {
		parentFrame = frame;
		
		JPanel rootPanel = new JPanel();
		rootPanel.setBorder(BorderFactory.createLoweredBevelBorder());
		
		SelectPanel firstLayer = new SelectPanel();
		rootPanel.add(firstLayer);
		
		// add image manipulators
		
		return rootPanel;
	}
	
	/**
	 * Run this to visually examine every panel element and how they look in next to each other.
	 * @param args ignored
	 */
	public static void main(String[] args) {
		Log.start();
		Translator.start();
		JFrame frame = new JFrame("Art Pipeline Panel 2");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		ArtPanel2 ap = new ArtPanel2();
		
		JPanel p = ap.buildPanel(frame);
		
		frame.setPreferredSize(new Dimension(800,600));
		frame.add(p);
		
		frame.pack();
		frame.setVisible(true);
	} 
}
