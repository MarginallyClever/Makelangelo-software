package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.makelangelo.donatelloimpl.ports.InputColor;
import com.marginallyclever.makelangelo.donatelloimpl.ports.InputInt;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputInt;
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
    private final OutputInt outx = new OutputInt("x",0);
    private final OutputInt outy = new OutputInt("y",0);
    private final OutputInt outw = new OutputInt("width",width.getValue());
    private final OutputInt outh = new OutputInt("height",height.getValue());

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
