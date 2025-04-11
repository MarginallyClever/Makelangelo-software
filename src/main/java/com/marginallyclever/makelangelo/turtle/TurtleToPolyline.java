package com.marginallyclever.makelangelo.turtle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * <p>Convert a {@link Turtle} to a list of {@link Polyline}s.</p>
 */
public class TurtleToPolyline {
    private static final Logger logger = LoggerFactory.getLogger(TurtleToPolyline.class);

    private final List<Polyline> polylines = new ArrayList<>();
    private final Lock lock = new ReentrantLock();

    public TurtleToPolyline(@Nonnull Turtle myTurtle) {
        if (!myTurtle.hasDrawing()) return;

        PolylineBuilder builder = new PolylineBuilder();
        builder.add(0,0);
        try {
            for( var layer : myTurtle.getLayers() ) {
                for( var line : layer.getAllLines() ) {
                    if( line.isEmpty() ) continue;
                    builder.clear();
                    for( var p : line.getAllPoints() ) {
                        builder.add((int) p.x, (int) p.y);
                    }
                    polylines.add(builder.compile(layer.getColor()));
                }
            }
        }
        catch(Exception e) {
            logger.error("Failed to generate polylines", e);
        }
    }

    private void drawPolylines(Graphics2D g2) {
        lock.lock();
        try {
            polylines.forEach(p -> p.draw(g2));
        } finally {
            lock.unlock();
        }
    }
}
