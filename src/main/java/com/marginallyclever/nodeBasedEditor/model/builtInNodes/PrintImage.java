package com.marginallyclever.nodeBasedEditor.model.builtInNodes;

import com.marginallyclever.nodeBasedEditor.model.Node;
import com.marginallyclever.nodeBasedEditor.model.NodeVariable;
import com.marginallyclever.nodeBasedEditor.PrintWithGraphics;

import java.awt.*;
import java.awt.image.BufferedImage;

public class PrintImage extends Node implements PrintWithGraphics {
    private final NodeVariable<BufferedImage> image = NodeVariable.newInstance("image", BufferedImage.class,null,true,false);
    private final NodeVariable<Number> px = NodeVariable.newInstance("X",Number.class,0,true,false);
    private final NodeVariable<Number> py = NodeVariable.newInstance("Y",Number.class,0,true,false);

    public PrintImage() {
        super("PrintImage");
        addVariable(image);
        addVariable(px);
        addVariable(py);
    }

    @Override
    public Node create() {
        return new PrintImage();
    }

    @Override
    public void update() {
        if(!isDirty()) return;
        cleanAllInputs();
    }

    @Override
    public void print(Graphics g) {
        g.drawImage((BufferedImage)image.getValue(),px.getValue().intValue(),py.getValue().intValue(),null);
    }
}
