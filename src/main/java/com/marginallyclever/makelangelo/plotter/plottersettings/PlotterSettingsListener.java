package com.marginallyclever.makelangelo.plotter.plottersettings;

import java.util.EventListener;

/**
 * Interface for listening to changes in the plotter settings.
 */
public interface PlotterSettingsListener extends EventListener {
	void settingsChangedEvent(PlotterSettings settings);
}
