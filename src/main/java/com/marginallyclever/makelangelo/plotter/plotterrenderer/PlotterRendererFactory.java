package com.marginallyclever.makelangelo.plotter.plotterrenderer;

import java.util.Arrays;

/**
 * {@link PlotterRendererFactory} is a Factory pattern for {@link PlotterRenderer} instances, written
 * as an enum so that it does extra checks at compile time.
 * 
 * @author coliss86
 */
public enum PlotterRendererFactory {
	MAKELANGELO_5("Makelangelo 5", new Makelangelo5()),
	MAKELANGELO_5_HUGE("Makelangelo 5 Huge", new Makelangelo5Huge()),
	MAKELANGELO_3_3("Makelangelo 3.3", new Makelangelo3_3()),
	MAKELANGELO_CUSTOM("Makelangelo (Custom)", new MakelangeloCustom()), 
	CARTESIAN("Cartesian", new Cartesian()),
	ZARPLOTTER("Zarplotter", new Zarplotter());

	private final PlotterRenderer plotterRenderer;

	private final String name;

	PlotterRendererFactory(String name, PlotterRenderer plotterRenderer) {
		this.name = name;
		this.plotterRenderer = plotterRenderer;
	}

	public PlotterRenderer getPlotterRenderer() {
		return plotterRenderer;
	}

	public String getName() {
		return name;
	}

	public static PlotterRendererFactory findByName(String name) {
		return Arrays.stream(values())
				.filter(enumValue -> enumValue.getName().contains(name))
				.findFirst()
				.orElseThrow();
	}
}
