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
	DEFAULT("Default", new DefaultTurtleRenderer(),"TurtleRenderFactory.DEFAULT"),
	BARBER_POLE("Barber pole", new BarberPoleTurtleRenderer(),"TurtleRenderFactory.BARBER_POLE"),
	SEPARATE_LOOP("Separate loops",new SeparateLoopTurtleRenderer(),"TurtleRenderFactory.SEPARATE_LOOP"),
	MARLIN_SIM("Marlin simulation",new MarlinSimulationVisualizer(),"TurtleRenderFactory.MARLIN_SIM");

	private final TurtleRenderer turtleRenderer;

	private final String name;
	
	private final String translatKey;

	/**
	 * 
	 * @param name
	 * @param turtleRenderer
	 *  @param translatKey The key used with Translator.get in getTranslatedText to obtaine the translated Text.
	 */
	TurtleRenderFactory(String name, TurtleRenderer turtleRenderer, String translatKey) {
		this.name = name;
		this.turtleRenderer = turtleRenderer;
		this.translatKey = translatKey;
	}

	public TurtleRenderer getTurtleRenderer() {
		return turtleRenderer;
	}

	public String getName() {
		return name;
	}
	
	/**
	 * 
	 * @return The translated text to be used as text in the GUI
	 */
	public String getTranslatedText() {
		return Translator.get(translatKey);
	}

	public static TurtleRenderFactory findByName(String name) {
		return Arrays.stream(values())
				.filter(enumValue -> enumValue.getName().contains(name))
				.findFirst()
				.orElseThrow();
	}
}
