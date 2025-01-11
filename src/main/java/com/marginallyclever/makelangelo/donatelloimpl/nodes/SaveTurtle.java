package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettingsManager;
import com.marginallyclever.nodegraphcore.port.Input;
import com.marginallyclever.nodegraphcore.port.Output;
import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.makelangelo.makeart.io.TurtleFactory;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaveTurtle extends Node {

    private static final Logger logger = LoggerFactory.getLogger(SaveTurtle.class);

    private final Input<String> filename = new Input<>("filename",String.class,null);
    private final Output<Turtle> turtle = new Output<>("turtle", Turtle.class,new Turtle());

    public SaveTurtle() {
        super("SaveTurtle");
        addVariable(filename);
        addVariable(turtle);
    }

    @Override
    public void update() {
        if(filename.getValue().isEmpty()) return;

        try {
            PlotterSettings settings = PlotterSettingsManager.buildMakelangelo5();
            TurtleFactory.save(turtle.getValue(),filename.getValue(),settings);
        } catch (Exception e) {
            logger.warn("Failed to update, ignoring", e);
        }
    }

}
