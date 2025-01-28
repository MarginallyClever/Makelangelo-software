package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.nodegraphcore.PrintWithGraphics;
import com.marginallyclever.nodegraphcore.port.Input;
import com.marginallyclever.nodegraphcore.port.Output;

import java.awt.*;

/**
 * A node that creates a canvas with a given size and color.
 */
public class Canvas extends Node implements PrintWithGraphics {
    private final Input<Number> width = new Input<>("width", Number.class,1);
    private final Input<Number> height = new Input<>("height", Number.class,1);
    private final Input<Color> color = new Input<>("color", Color.class,Color.WHITE);
    private final Output<Number> outx = new Output<>("x", Number.class,0);
    private final Output<Number> outy = new Output<>("y", Number.class,0);
    private final Output<Number> outw = new Output<>("width", Number.class,width.getValue());
    private final Output<Number> outh = new Output<>("height", Number.class,height.getValue());

    public Canvas() {
        super("Canvas");
        addVariable(width);
        addVariable(height);
        addVariable(color);
        addVariable(outx);
        addVariable(outy);
        addVariable(outw);
        addVariable(outh);
    }

    @Override
    public void update() {
        var w = Math.max(1,width.getValue().intValue());
        var h = Math.max(1,height.getValue().intValue());
        outx.send(0);
        outy.send(0);
        outw.send(w);
        outh.send(h);
    }


    @Override
    public void print(Graphics g) {
        var w = Math.max(1,width.getValue().intValue());
        var h = Math.max(1,height.getValue().intValue());
        g.setColor(color.getValue());
        g.fillRect(0,0,w,h);
    }
}
