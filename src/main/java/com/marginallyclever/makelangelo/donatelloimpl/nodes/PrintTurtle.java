package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.donatello.graphview.GraphViewPanel;
import com.marginallyclever.donatello.ports.InputBoolean;
import com.marginallyclever.donatello.ports.InputColor;
import com.marginallyclever.donatello.ports.InputInt;
import com.marginallyclever.makelangelo.donatelloimpl.ports.InputTurtle;
import com.marginallyclever.makelangelo.turtle.*;
import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.nodegraphcore.PrintWithGraphics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Print the {@link Turtle}'s path behind the {@link Node}s.</p>
 * <p>On {@link #update()} pass over the {@link Turtle} once and build a list of polylines for faster rendering.
 * This is done using a {@link PolylineBuilder} which also optimizes to remove points on nearly straight lines.</p>
 */
public class PrintTurtle extends Node implements PrintWithGraphics {
    private static final Logger logger = LoggerFactory.getLogger(PrintTurtle.class);

    private final InputTurtle turtle = new InputTurtle("turtle");
    private final InputInt px = new InputInt("X",0);
    private final InputInt py = new InputInt("Y",0);
    private final InputBoolean showTravel = new InputBoolean("show travel",false);
    private final InputColor travelColor = new InputColor("travel color",Color.GREEN);
    private final InputInt lineThickness = new InputInt("line thickness",1);

    private final List<Polyline> polylines = new ArrayList<>();

    public PrintTurtle() {
        super("PrintTurtle");
        addVariable(turtle);
        addVariable(px);
        addVariable(py);
        addVariable(showTravel);
        addVariable(travelColor);
        addVariable(lineThickness);
    }

    @Override
    public void update() {
        polylines.clear();
        Turtle myTurtle = turtle.getValue();
        if(myTurtle==null || myTurtle.history.isEmpty()) return;

        generatePolylines(myTurtle);
    }

    @Override
    public void print(Graphics g) {
        if(getComplete()<100) return;
        Turtle myTurtle = turtle.getValue();
        if(myTurtle==null || myTurtle.history.isEmpty()) return;

        Graphics2D g2 = (Graphics2D)g.create();
        GraphViewPanel.setHints(g2);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int dx=px.getValue();
        int dy=py.getValue();
        g2.translate(dx,dy);
        var lineThickness = this.lineThickness.getValue().floatValue();
        g2.setStroke(new BasicStroke(lineThickness));
        polylines.forEach(p -> p.draw(g2));
    }

    private void generatePolylines(Turtle myTurtle) {
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
}
