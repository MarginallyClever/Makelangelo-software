package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.donatello.ports.InputColor;
import com.marginallyclever.donatello.ports.InputInt;
import com.marginallyclever.donatello.ports.OutputInt;
import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.nodegraphcore.PrintWithGraphics;

import java.awt.*;

/**
 * A node that creates a canvas with a given size and color.
 */
public class Canvas extends Node implements PrintWithGraphics {
    private final InputInt width = new InputInt("width", 1);
    private final InputInt height = new InputInt("height", 1);
    private final InputColor color = new InputColor("color", Color.WHITE);
    private final InputInt layer = new InputInt("layer",0);
    private final OutputInt outx = new OutputInt("x",0);
    private final OutputInt outy = new OutputInt("y",0);
    private final OutputInt outw = new OutputInt("width out",width.getValue());
    private final OutputInt outh = new OutputInt("height out",height.getValue());

    public Canvas() {
        super("Canvas");
        addPort(width);
        addPort(height);
        addPort(color);
        addPort(outx);
        addPort(outy);
        addPort(outw);
        addPort(outh);
        addPort(layer);
    }

    @Override
    public void update() {
        var w = Math.max(1,width.getValue());
        var h = Math.max(1,height.getValue());
        outx.setValue(-w/2);
        outy.setValue(-h/2);
        outw.setValue(w);
        outh.setValue(h);
    }


    @Override
    public void print(Graphics g) {
        var x = outx.getValue();
        var y = outy.getValue();
        var w = Math.max(1,width.getValue());
        var h = Math.max(1,height.getValue());
        g.setColor(color.getValue());
        g.fillRect(x,y,w,h);
    }

    @Override
    public int getLayer() {
        return layer.getValue();
    }
}
