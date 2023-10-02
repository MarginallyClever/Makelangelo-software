package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.nodegraphcore.DockReceiving;
import com.marginallyclever.nodegraphcore.DockShipping;
import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.makelangelo.makeart.io.TurtleFactory;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaveTurtle extends Node {

    private static final Logger logger = LoggerFactory.getLogger(SaveTurtle.class);

    private final DockReceiving<String> filename = new DockReceiving<>("filename",String.class,null);
    private final DockShipping<Turtle> turtle = new DockShipping<>("turtle", Turtle.class,new Turtle());

    public SaveTurtle() {
        super("SaveTurtle");
        addVariable(filename);
        addVariable(turtle);
    }

    @Override
    public void update() {
        if(filename.hasPacketWaiting()) filename.receive();

        if(filename.getValue().isEmpty()) return;

        try {
            TurtleFactory.save(turtle.getValue(),filename.getValue());
        } catch (Exception e) {
            logger.warn("Failed to update, ignoring", e);
        }
    }

}
