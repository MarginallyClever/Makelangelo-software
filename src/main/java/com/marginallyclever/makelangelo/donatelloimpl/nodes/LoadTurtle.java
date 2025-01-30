package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.makelangelo.donatelloimpl.ports.InputFilename;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputDouble;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputTurtle;
import com.marginallyclever.makelangelo.makeart.io.TurtleFactory;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;

import java.awt.geom.Rectangle2D;

/**
 * Load a {@link Turtle} from a file.
 */
public class LoadTurtle extends Node {
    private final InputFilename filename = new InputFilename("filename","");
    private final OutputTurtle contents = new OutputTurtle("contents");
    private final OutputDouble w = new OutputDouble("width", 0d);
    private final OutputDouble h = new OutputDouble("height", 0d);
    private final OutputDouble length = new OutputDouble("length", 0d);


    public LoadTurtle() {
        super("LoadTurtle");
        addVariable(filename);
        addVariable(contents);
        addVariable(w);
        addVariable(h);
    }

    @Override
    public void update() {
        try {
            Turtle t = TurtleFactory.load(filename.getValue());
            contents.send(t);
            Rectangle2D r = t.getBounds();
            w.send(r.getWidth());
            h.send(r.getHeight());
            length.send(t.getDrawDistance());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
