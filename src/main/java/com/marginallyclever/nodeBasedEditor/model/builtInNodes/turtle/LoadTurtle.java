package com.marginallyclever.nodeBasedEditor.model.builtInNodes.turtle;

import com.marginallyclever.makelangelo.makeArt.io.vector.LoadDXF;
import com.marginallyclever.makelangelo.makeArt.io.vector.LoadSVG;
import com.marginallyclever.makelangelo.makeArt.io.vector.LoadScratch3;
import com.marginallyclever.makelangelo.makeArt.io.vector.TurtleFactory;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodeBasedEditor.model.Node;
import com.marginallyclever.nodeBasedEditor.model.NodeVariable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;

public class LoadTurtle extends Node {
    private final NodeVariable<String> filename = NodeVariable.newInstance("filename",String.class,null,true,false);
    private final NodeVariable<Turtle> contents = NodeVariable.newInstance("contents", Turtle.class, null,false,true);

    public LoadTurtle(String startingValue) {
        super("LoadTurtle");
        addVariable(filename);
        addVariable(contents);
        filename.setValue(startingValue);
    }

    public LoadTurtle() {
        this(null);
    }

    @Override
    public Node create() {
        return new LoadTurtle();
    }

    @Override
    public void update() {
        if(!isDirty()) return;
        try {
            contents.setValue(TurtleFactory.load(filename.getValue()));
            cleanAllInputs();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
