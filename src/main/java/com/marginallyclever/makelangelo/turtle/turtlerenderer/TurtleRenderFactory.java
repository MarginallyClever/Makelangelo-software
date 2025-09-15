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

	private static final BarberPoleTurtleRenderer myBarberPoleTurtleRenderer = new BarberPoleTurtleRenderer();
	private static final SeparateLoopTurtleRenderer mySeparateLoopTurtleRenderer = new SeparateLoopTurtleRenderer();
	private static final DirectionLoopTurtleRenderer myDirectionLoopTurtleRenderer = new DirectionLoopTurtleRenderer();
	private static final MarlinSimulationVisualizer myMarlinSimulationVisualizer = new MarlinSimulationVisualizer();
    private static final DefaultTurtleRenderer myDefaultTurtleRenderer = new DefaultTurtleRenderer();

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
			case BARBER_POLE -> myBarberPoleTurtleRenderer;
			case SEPARATE_LOOPS -> mySeparateLoopTurtleRenderer;
			case DIRECTION_LOOPS -> myDirectionLoopTurtleRenderer;
			case MARLIN_SIMULATION -> myMarlinSimulationVisualizer;
			default -> myDefaultTurtleRenderer;
		};
	}
}
