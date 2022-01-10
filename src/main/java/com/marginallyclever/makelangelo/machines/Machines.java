package com.marginallyclever.makelangelo.machines;

import com.marginallyclever.makelangelo.plotter.plotterRenderer.*;

import java.util.Arrays;

public enum Machines {
    MAKELANGELO_5(new Makelangelo5()),
    MAKELANGELO_3_3(new Makelangelo3_3()),
    MAKELANGELO_CUSTOM(new MakelangeloCustom()),
    CARTESIAN(new Cartesian()),
    ZARPLOTTER(new Zarplotter());

    private final PlotterRenderer plotterRenderer;

    Machines(PlotterRenderer plotterRenderer) {
        this.plotterRenderer = plotterRenderer;
    }

    public PlotterRenderer getPlotterRenderer() {
        return plotterRenderer;
    }

    public static Machines findByName(String name) {
        return Arrays.stream(values())
                .filter(machine -> machine.getPlotterRenderer().getName().contains(name))
                .findFirst()
                .orElseThrow();
    }
}
