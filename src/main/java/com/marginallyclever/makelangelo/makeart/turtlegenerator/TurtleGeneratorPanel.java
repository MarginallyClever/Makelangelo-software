package com.marginallyclever.makelangelo.makeart.turtlegenerator;

import com.marginallyclever.makelangelo.select.SelectPanel;

/**
 * All {@link TurtleGenerator} have a {@link TurtleGeneratorPanel}.
 * @author Dan Royer
 */
public class TurtleGeneratorPanel extends SelectPanel {
	public TurtleGeneratorPanel(TurtleGenerator generator) {
		super();
		generator.getPanelElements().forEach(this::add);
	}
}
