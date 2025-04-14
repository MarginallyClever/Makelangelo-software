package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle;

import com.marginallyclever.donatello.ports.InputColor;
import com.marginallyclever.makelangelo.donatelloimpl.ports.InputTurtle;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputTurtle;
import com.marginallyclever.makelangelo.turtle.StrokeLayer;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;

import java.awt.*;

/**
 * Change the color of a {@link Turtle}.
 */
public class ColorTurtle extends Node {
    private final InputTurtle turtle = new InputTurtle("turtle");
    private final InputColor color = new InputColor("color", Color.BLACK);
    private final OutputTurtle output = new OutputTurtle("output");

    public ColorTurtle() {
        super("ColorTurtle");
        addPort(turtle);
        addPort(color);
        addPort(output);
    }

    @Override
    public void update() {
        Turtle input = turtle.getValue();
        Color c = color.getValue();
        Turtle moved = new Turtle(input);
        var allLayers = moved.getLayers();
        if( allLayers.size() == 1 ) {
            allLayers.getFirst().setColor(c);
        } else if( allLayers.size() > 1 ) {
            // many layers merge into one, provided they are the same diameter
            // TODO check for diameter changes
            StrokeLayer newLayer = new StrokeLayer(c, allLayers.getFirst().getDiameter());
            for( var layer : allLayers ) {
                newLayer.addAll(layer.getAllLines());
            }
            allLayers.clear();
            allLayers.add(newLayer);
        }

        output.setValue(moved);
    }
}
