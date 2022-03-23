package com.marginallyclever.donatelloimpl.nodes;

import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.nodegraphcore.NodeVariable;
import com.marginallyclever.makelangelo.makeart.io.vector.TurtleFactory;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaveTurtle extends Node {

    private static final Logger logger = LoggerFactory.getLogger(SaveTurtle.class);

    private final NodeVariable<String> filename = NodeVariable.newInstance("filename",String.class,null,true,false);
    private final NodeVariable<Turtle> turtle = NodeVariable.newInstance("turtle", Turtle.class,new Turtle(),true,false);

    public SaveTurtle() {
        super("SaveTurtle");
        addVariable(filename);
        addVariable(turtle);
    }

    @Override
    public Node create() {
        return new SaveTurtle();
    }

    @Override
    public void update() throws Exception {
        if(filename.getValue().isEmpty()) return;

        try {
            TurtleFactory.save(turtle.getValue(),filename.getValue());
            cleanAllInputs();
        } catch (Exception e) {
            logger.warn("Failed to update, ignoring", e);
        }
    }

}
