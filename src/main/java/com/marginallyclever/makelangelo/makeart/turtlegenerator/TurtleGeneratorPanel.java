package com.marginallyclever.makelangelo.makeart.turtlegenerator;

import com.marginallyclever.makelangelo.select.SelectPanel;

/**
 * All {@link TurtleGenerator} have a {@link TurtleGeneratorPanel}.
 * @author Dan Royer
 */
public class TurtleGeneratorPanel extends SelectPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final TurtleGenerator generator;

	public TurtleGeneratorPanel(TurtleGenerator generator) {
		super();
		this.generator = generator;
		generator.getPanelElements().forEach(this::add);
	}
}
