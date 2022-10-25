package com.marginallyclever.makelangelo.makeart.turtlegenerator;

import com.marginallyclever.makelangelo.select.SelectPanel;

import java.io.Serial;

/**
 * All {@link TurtleGenerator} have a {@link TurtleGeneratorPanel}.
 * @author Dan Royer
 */
public class TurtleGeneratorPanel extends SelectPanel {
	@Serial
	private static final long serialVersionUID = 1L;

	public TurtleGeneratorPanel(TurtleGenerator generator) {
		super();
		generator.getPanelElements().forEach(this::add);
	}
}
