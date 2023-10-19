package com.marginallyclever.makelangelo.turtle.turtlerenderer;

import com.marginallyclever.makelangelo.plotter.marlinsimulation.MarlinSimulationVisualizer;

import java.util.Arrays;

/**
 * {@link TurtleRenderFactory} is a Factory pattern for {@link TurtleRenderer} instances, written
 * as an enum so that it does extra checks at compile time.
 * 
 * @author Dan Royer
 */
public enum TurtleRenderFactory {
	DEFAULT("Default", new DefaultTurtleRenderer()),
	BARBER_POLE("Barber pole", new BarberPoleTurtleRenderer()),
	SEPARATE_LOOP("Separate loops",new SeparateLoopTurtleRenderer()),
	DIRECTION_LOOP("Direction loops",new DirectionLoopTurtleRenderer()),
	MARLIN_SIM("Marlin simulation",new MarlinSimulationVisualizer());

	private final TurtleRenderer turtleRenderer;

	private final String name;

	TurtleRenderFactory(String name, TurtleRenderer turtleRenderer) {
		this.name = name;
		this.turtleRenderer = turtleRenderer;
	}

	public TurtleRenderer getTurtleRenderer() {
		return turtleRenderer;
	}

	public String getName() {
		return name;
	}

	public static TurtleRenderFactory findByName(String name) {
		return Arrays.stream(values())
				.filter(enumValue -> enumValue.getName().contains(name))
				.findFirst()
				.orElseThrow();
	}
}
