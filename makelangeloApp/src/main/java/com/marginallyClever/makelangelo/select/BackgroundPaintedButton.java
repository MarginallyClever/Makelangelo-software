package com.marginallyClever.makelangelo.select;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JButton;

/**
 * A {@link JButton} filled with the background color.  Especially useful for color selection dialogs.
 * @author Dan Royer
 * @since 7.31.0
 *
 */
public class BackgroundPaintedButton extends JButton {
	private static final long serialVersionUID = 1294423717816753743L;

	public BackgroundPaintedButton(String label) {
		super(label);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
	    Graphics2D g2 = (Graphics2D)g;

	    Color c = getBackground();
	    g2.setPaint(c);
	    g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,0,0);
	}
}
