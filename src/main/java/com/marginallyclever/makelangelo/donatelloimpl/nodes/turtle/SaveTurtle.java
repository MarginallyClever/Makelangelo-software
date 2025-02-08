package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle;

import com.marginallyclever.donatello.ports.InputFilename;
import com.marginallyclever.makelangelo.donatelloimpl.ports.InputTurtle;
import com.marginallyclever.makelangelo.makeart.io.TurtleFactory;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettingsManager;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Save a {@link Turtle} to a file.
 */
public class SaveTurtle extends Node {
    private static final Logger logger = LoggerFactory.getLogger(SaveTurtle.class);

    private final InputFilename filename = new InputFilename("filename");
    private final InputTurtle turtle = new InputTurtle("turtle");

    public SaveTurtle() {
        super("SaveTurtle");

        addPort(filename);
        addPort(turtle);

        filename.setFileChooser(TurtleFactory.getSaveFileChooser());
        filename.setDialogType(true);
    }

    @Override
    public void update() {
        String filenameValue = filename.getValue().get();
        if(filenameValue==null || filenameValue.isEmpty())  return;

        try {
            PlotterSettings settings = PlotterSettingsManager.buildMakelangelo5();
            TurtleFactory.save(turtle.getValue(),filenameValue,settings);
        } catch (Exception e) {
            logger.warn("Failed to save", e);
        }
    }
}
