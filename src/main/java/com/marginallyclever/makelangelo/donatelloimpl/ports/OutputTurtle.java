package com.marginallyclever.makelangelo.donatelloimpl.ports;

import com.marginallyclever.donatello.graphview.GraphViewProvider;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleToBufferedImageHelper;
import com.marginallyclever.nodegraphcore.port.Output;
import com.marginallyclever.nodegraphcore.port.Port;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * An {@link Output} that also displays the {@link Turtle}'s drawing as an image.
 */
public class OutputTurtle extends Output<Turtle> implements GraphViewProvider {
    private static final Logger logger = LoggerFactory.getLogger(OutputTurtle.class);

    private BufferedImage img = new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);

    public OutputTurtle(String _name) throws IllegalArgumentException {
        super(_name, Turtle.class, new Turtle());
    }

    @Override
    public void setValue(Object value) {
        super.setValue(value);
        if(!(value instanceof Turtle turtle)) return;

        if(turtle.hasDrawing()) {
            var helper = new TurtleToBufferedImageHelper();
            img = helper.generateThumbnail(turtle, null, DEFAULT_WIDTH);
        } else {
            img = new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);
        }
    }

    @Override
    public Rectangle getRectangle() {
        double w = img.getWidth();
        double h = img.getHeight();/*
        if(w<rectangle.width) {
            h = h/w * rectangle.width;
            w = rectangle.width;
        }*/
        if(h<Port.DEFAULT_HEIGHT) {
            rectangle.setBounds(rectangle.x,rectangle.y,Port.DEFAULT_WIDTH,Port.DEFAULT_HEIGHT);
            return rectangle;
        }
        double ratio = h/w;
        var newHeight = (double)rectangle.width * ratio;
        rectangle.setSize(rectangle.width,(int)Math.max(newHeight, Port.DEFAULT_HEIGHT));

        return rectangle;
    }

    @Override
    public void paint(Graphics g, Rectangle box) {
        if(img==null) return;
        var w = (int)box.getWidth();
        var h = (int)box.getHeight();
        var x = (int)box.getX();
        var y = (int)box.getY();
        g.drawImage(img, x, y, w, h, null);
    }
}
