package com.marginallyclever.makelangelo.plotter.plotterrenderer;

import java.util.Arrays;

/**
 * {@link PlotterRendererFactory} is a Factory pattern for {@link PlotterRenderer} instances, written
 * as an enum so that it does extra checks at compile time.
 * 
 * @author coliss86
 */
public enum PlotterRendererFactory {
	// name must match enum label for PlotterRendererFactory.valueOf() to work.
	MAKELANGELO_5("MAKELANGELO_5", new Makelangelo5()),
	MAKELANGELO_5_HUGE("MAKELANGELO_5_HUGE", new Makelangelo5Huge()),
	MAKELANGELO_3_3("MAKELANGELO_3_3", new Makelangelo3_3()),
	MAKELANGELO_CUSTOM("MAKELANGELO_CUSTOM", new MakelangeloCustom()),
	CARTESIAN("CARTESIAN", new Cartesian()),
	ZARPLOTTER("ZARPLOTTER", new Zarplotter());

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
