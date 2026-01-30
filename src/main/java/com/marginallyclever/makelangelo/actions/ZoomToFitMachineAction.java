package com.marginallyclever.makelangelo.actions;

import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.preview.Camera;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * An action to zoom the camera to fit the machine limits.
 */
public class ZoomToFitMachineAction extends AbstractAction {
    private final Camera camera;
    private final Plotter plotter;

    public ZoomToFitMachineAction(String label, Camera camera, Plotter plotter) {
        super(label);
        this.camera = camera;
        this.plotter = plotter;
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control insert"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        var width = plotter.getSettings().getDouble(PlotterSettings.LIMIT_RIGHT) - plotter.getSettings().getDouble(PlotterSettings.LIMIT_LEFT);
        var height = plotter.getSettings().getDouble(PlotterSettings.LIMIT_TOP) - plotter.getSettings().getDouble(PlotterSettings.LIMIT_BOTTOM);
        camera.zoomToFit(width,height);
    }
}
