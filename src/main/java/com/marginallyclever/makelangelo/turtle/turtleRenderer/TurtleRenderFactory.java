package com.marginallyclever.makelangelo.turtle.turtleRenderer;

import com.marginallyclever.makelangelo.Translator;
import java.util.Arrays;

import com.marginallyclever.makelangelo.plotter.marlinSimulation.MarlinSimulationVisualizer;

/**
 * {@link TurtleRenderFactory} is a Factory pattern for {@link TurtleRenderer} instances, written
 * as an enum so that it does extra checks at compile time.
 * 
 * @author Dan Royer
 */
public enum TurtleRenderFactory {
	DEFAULT("Default", new DefaultTurtleRenderer(),Translator.get("TurtleRenderFactory.DEFAULT")),// not coder friendly but allow latter the CI test not to miss this traduction keys.
	BARBER_POLE("Barber pole", new BarberPoleTurtleRenderer(),Translator.get("TurtleRenderFactory.BARBER_POLE")),
	SEPARATE_LOOP("Separate loops",new SeparateLoopTurtleRenderer(),Translator.get("TurtleRenderFactory.SEPARATE_LOOP")),
	MARLIN_SIM("Marlin simulation",new MarlinSimulationVisualizer(),Translator.get("TurtleRenderFactory.MARLIN_SIM"));

	private final TurtleRenderer turtleRenderer;

	private final String name;
	
	private final String traductedText;

	/**
	 * 
	 * @param name
	 * @param turtleRenderer
	 * @param traductedText the text (traducted) N.B. : not base on the name (used in findByName) to avoid a bad traduction (that give for two elements the same value) to assert any traduction may not create issues... 
	 */
	TurtleRenderFactory(String name, TurtleRenderer turtleRenderer, String traductedText) {
		this.name = name;
		this.turtleRenderer = turtleRenderer;
		this.traductedText = traductedText;
	}

	public TurtleRenderer getTurtleRenderer() {
		return turtleRenderer;
	}

	public String getName() {
		return name;
	}
	
	/**
	 * 
	 * @return The text (traducted) to used as text in the GUI ...
	 */
	public String getTraductedText() {
		return traductedText;
	}

	public static TurtleRenderFactory findByName(String name) {
		return Arrays.stream(values())
				.filter(enumValue -> enumValue.getName().contains(name))
				.findFirst()
				.orElseThrow();
	}
}
