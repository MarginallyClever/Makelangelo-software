package com.marginallyclever.makelangelo.apps.previewpanel.plotterrenderer;

import javax.vecmath.Vector2d;
import java.util.Arrays;

/**
 * {@link PlotterRendererFactory} is a Factory pattern for {@link PlotterRenderer} instances, written
 * as an enum so that it does extra checks at compile time.
 * 
 * @author coliss86
 */
public enum PlotterRendererFactory {
	// name must match enum label for PlotterRendererFactory.valueOf() to work.
	MAKELANGELO_5("MAKELANGELO_5", new Makelangelo5(), new Vector2d(650, 1000)),
	MAKELANGELO_5_HUGE("MAKELANGELO_5_HUGE", new Makelangelo5Huge(), new Vector2d(1336,2000)),
	MAKELANGELO_3_3("MAKELANGELO_3_3", new Makelangelo3_3(), null),
	MAKELANGELO_CUSTOM("MAKELANGELO_CUSTOM", new MakelangeloCustom(), null),
	CARTESIAN("CARTESIAN", new Cartesian(),null),
	ZARPLOTTER("ZARPLOTTER", new Zarplotter(),null);

	private final PlotterRenderer plotterRenderer;
	private final String name;
	private final Vector2d fixedSize;

	PlotterRendererFactory(String name, PlotterRenderer plotterRenderer,Vector2d fixedSize) {
		this.name = name;
		this.plotterRenderer = plotterRenderer;
		this.fixedSize = fixedSize;
	}

	public PlotterRenderer getPlotterRenderer() {
		return plotterRenderer;
	}

	public String getName() {
		return name;
	}

	/**
	 * @return the fixed size of the plotter renderer, or null if it is not fixed.
	 */
	public Vector2d getFixedSize() {
		return fixedSize;
	}

	public static PlotterRendererFactory findByName(String name) {
		return Arrays.stream(values())
				.filter(enumValue -> enumValue.getName().contains(name))
				.findFirst()
				.orElseThrow();
	}
}
