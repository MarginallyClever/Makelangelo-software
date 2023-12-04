package com.marginallyclever.makelangelo.select;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * A container for all Select elements, to facilitate formatting as a group.
 * @author Dan Royer
 * @since 7.24.0
 */
public class SelectPanel extends JPanel {
	private final JPanel interiorPanel = new JPanel();
	
	public SelectPanel() {
		super(new BorderLayout());
		add(interiorPanel,BorderLayout.PAGE_START);

		interiorPanel.setBorder(new EmptyBorder(5,5,5,5));
		interiorPanel.setLayout(new BoxLayout(interiorPanel, BoxLayout.Y_AXIS));
	}
	
	public void add(JComponent c) {
		interiorPanel.add(c);
	}
}
