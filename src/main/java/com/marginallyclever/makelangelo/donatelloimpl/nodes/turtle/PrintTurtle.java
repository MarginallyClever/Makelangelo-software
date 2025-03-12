package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle;

import com.marginallyclever.donatello.graphview.GraphViewPanel;
import com.marginallyclever.donatello.ports.InputBoolean;
import com.marginallyclever.donatello.ports.InputColor;
import com.marginallyclever.donatello.ports.InputInt;
import com.marginallyclever.donatello.ports.InputOneOfMany;
import com.marginallyclever.makelangelo.donatelloimpl.ports.InputTurtle;
import com.marginallyclever.makelangelo.turtle.*;
import com.marginallyclever.makelangelo.turtle.turtlerenderer.TurtleRenderFactory;
import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.nodegraphcore.PrintWithGraphics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    private final InputInt lineThickness = new InputInt("line thickness",1);
    private final InputInt layer = new InputInt("layer",5);
    private final InputOneOfMany style = new InputOneOfMany("style");

    private BufferedImage image = new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);
    private final Point topLeft = new Point(0,0);
    private final Lock lock = new ReentrantLock();

    public PrintTurtle() {
        super("PrintTurtle");
        addPort(turtle);
        addPort(showTravel);
        addPort(travelColor);
        addPort(lineThickness);
        addPort(style);
        addPort(layer);

        ArrayList<String> names = new ArrayList<>();
        Arrays.stream(TurtleRenderFactory.values()).forEach(f-> names.add(f.getName()));
        style.setOptions(names.toArray(new String[0]));
    }

    @Override
    public void update() {
        lock.lock();
        try {
            setComplete(0);
            Turtle myTurtle = turtle.getValue();
            if(myTurtle==null || !myTurtle.hasDrawing()) {
                image = new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);
                topLeft.setLocation(0,0);
                setComplete(100);
                return;
            }
            image = TurtleToBufferedImageHelper.generateImage(myTurtle,this);
            var r = myTurtle.getBounds();
            topLeft.setLocation(r.x,r.y);
            //generatePolylines(myTurtle);
            setComplete(100);
        } catch(Exception e) {
            logger.error("Failed to update", e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int getLayer() {
        return layer.getValue();
    }

    @Override
    public void print(Graphics g) {
        if(getComplete()<100) return;
        Turtle myTurtle = turtle.getValue();
        if(myTurtle==null || myTurtle.history.isEmpty()) return;
        //drawPolyglines(g);
        g.drawImage(image,topLeft.x,topLeft.y,null);
    }

    @Deprecated
    private final List<Polyline> polylines = new ArrayList<>();

    @Deprecated
    private void generatePolylines(Turtle myTurtle) {
        polylines.clear();
        if (myTurtle == null || myTurtle.history.isEmpty()) return;

        int size = myTurtle.history.size();
        int count = 0;

        setComplete(0);

        // where we're at in the drawing (to check if we're between first & last)
        boolean showPenUp = showTravel.getValue();
        TurtleMove previousMove = null;

        Color upColor = travelColor.getValue();
        Color downColor = Color.BLACK;
        PolylineBuilder builder = new PolylineBuilder();
        builder.add(0,0);
        try {
            for (TurtleMove move : myTurtle.history) {
                if(move==null) throw new NullPointerException();
                if(move.type == MovementType.TOOL_CHANGE) {
                    downColor = move.getColor();
                    count++;
                    continue;
                }

                if ( previousMove != null) {
                    if( previousMove.type != move.type ) {
                        polylines.add(builder.compile(previousMove.type == MovementType.TRAVEL ? upColor : downColor));
                        builder.clear();
                        builder.add((int) previousMove.x, (int) previousMove.y);
                    }
                    if((move.type == MovementType.TRAVEL && showPenUp) || move.type == MovementType.DRAW_LINE) {
                        builder.add((int) move.x, (int) move.y);
                    }
                }
                previousMove = move;
                setComplete((int) (100.0 * count++ / size));
            }
            if(builder.getSize()>0 && previousMove!=null) {
                polylines.add(builder.compile(previousMove.type == MovementType.TRAVEL ? upColor : downColor));
            }
        }
        catch(Exception e) {
            logger.error("Failed to generate polylines", e);
        }
        setComplete(100);
    }

    @Deprecated
    private void drawPolylines(Graphics g) {
        Graphics2D g2 = (Graphics2D)g.create();
        GraphViewPanel.setHints(g2);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(lineThickness.getValue()));

        lock.lock();
        try {
            polylines.forEach(p -> p.draw(g2));
        } finally {
            lock.unlock();
        }

        g2.dispose();
    }

    public boolean showTravel() {
        return showTravel.getValue();
    }

    public Color travelColor() {
        return travelColor.getValue();
    }
}
