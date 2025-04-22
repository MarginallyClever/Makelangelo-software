package com.marginallyclever.makelangelo.turtle.turtlerenderer;

/**
 * {@link TurtleRenderFactory} is a Factory pattern for {@link TurtleRenderer} instances, written
 * as an enum so that it does extra checks at compile time.
 * 
 * @author Dan Royer
 */
public class TurtleRenderFactory {
	public static final int DEFAULT = 0;
	public static final int BARBER_POLE = 1;
	public static final int SEPARATE_LOOPS = 2;
	public static final int DIRECTION_LOOPS = 3;
	public static final int MARLIN_SIMULATION = 4;
	public static final int NUM_RENDERERS = 5;

	public static String [] getNames() {
		return new String [] {
				"Default",
				"Barber pole",
				"Separate loops",
				"Direction loops",
				"Marlin simulation"
		};
	}

	public static TurtleRenderer getTurtleRenderer(int index) {
		return switch(index) {
			case 1 -> new BarberPoleTurtleRenderer();
			case 2 -> new SeparateLoopTurtleRenderer();
			case 3 -> new DirectionLoopTurtleRenderer();
			case 4 -> new MarlinSimulationVisualizer();
			default -> new DefaultTurtleRenderer();
		};
	}
}
