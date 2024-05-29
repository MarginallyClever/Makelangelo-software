package com.marginallyclever.makelangelo.select;

import javax.swing.*;
import java.awt.*;

/**
 * A {@link JButton} filled with the background color.  Especially useful for color selection dialogs.
 * @author Dan Royer
 * @since 7.31.0
 *
 */
public class BackgroundPaintedButton extends JButton {
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
