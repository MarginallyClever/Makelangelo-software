package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle;

import com.marginallyclever.donatello.ports.InputBoolean;
import com.marginallyclever.donatello.ports.InputColor;
import com.marginallyclever.donatello.ports.InputInt;
import com.marginallyclever.donatello.ports.InputOneOfMany;
import com.marginallyclever.makelangelo.donatelloimpl.ports.InputTurtle;
import com.marginallyclever.makelangelo.turtle.PolylineBuilder;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleToBufferedImageHelper;
import com.marginallyclever.makelangelo.turtle.turtlerenderer.TurtleRenderFacade;
import com.marginallyclever.makelangelo.turtle.turtlerenderer.TurtleRenderFactory;
import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.nodegraphcore.PrintWithGraphics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>Print the {@link Turtle}'s path behind the {@link Node}s.</p>
 * <p>On {@link #update()} pass over the {@link Turtle} once and build a list of polylines for faster rendering.
 * This is done using a {@link PolylineBuilder} which also optimizes to remove points on nearly straight lines.</p>
 */
public class PrintTurtle extends Node implements PrintWithGraphics {
    private static final Logger logger = LoggerFactory.getLogger(PrintTurtle.class);

    private final InputTurtle turtle = new InputTurtle("turtle");
    private final InputBoolean showTravel = new InputBoolean("show travel",false);
    private final InputColor travelColor = new InputColor("travel color",Color.GREEN);
    private final InputInt layer = new InputInt("layer",5);
    private final InputOneOfMany style = new InputOneOfMany("style");

    private BufferedImage image = new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);
    private final Point topLeft = new Point(0,0);
    private final Lock lock = new ReentrantLock();
    private int updateCount = 0;

    public PrintTurtle() {
        super("PrintTurtle");
        addPort(turtle);
        addPort(showTravel);
        addPort(travelColor);
        addPort(style);
        addPort(layer);

        style.setOptions(TurtleRenderFactory.getNames());
    }

    @Override
    public void update() {
        setComplete(0);

        lock.lock();
        try {
            Turtle myTurtle = turtle.getValue();
            if (myTurtle == null || !myTurtle.hasDrawing()) {
                image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
                topLeft.setLocation(0, 0);
                setComplete(100);
                return;
            }

            logger.debug("start {} {}", Integer.toHexString(myTurtle.hashCode()), ++updateCount);

            var helper = new TurtleToBufferedImageHelper();
            image = helper.generateImage(myTurtle, this);

            logger.debug("done {}", Integer.toHexString(myTurtle.hashCode()));

            var r = myTurtle.getBounds();
            topLeft.setLocation(r.x, r.y);
        } finally {
            lock.unlock();
        }
        setComplete(100);
    }

    @Override
    public int getLayer() {
        return layer.getValue();
    }

    @Override
    public void print(Graphics g) {
        if(getComplete()<100) return;
        Turtle myTurtle = turtle.getValue();
        if(myTurtle==null || !myTurtle.hasDrawing()) return;
        g.drawImage(image,topLeft.x,topLeft.y,null);
    }

    public boolean showTravel() {
        return showTravel.getValue();
    }

    public Color travelColor() {
        return travelColor.getValue();
    }

    public void setupTurtleRenderFacade(@Nonnull TurtleRenderFacade trf) {
        trf.setRenderer(TurtleRenderFactory.getTurtleRenderer(style.getValue()));
        trf.setDownColor(Color.BLACK);
        trf.setUpColor(travelColor.getValue());
        trf.setShowTravel(showTravel.getValue());
    }
}
