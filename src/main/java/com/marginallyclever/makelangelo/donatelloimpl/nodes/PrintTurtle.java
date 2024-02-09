package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleMove;
import com.marginallyclever.nodegraphcore.DockReceiving;
import com.marginallyclever.nodegraphcore.DockShipping;
import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.nodegraphcore.PrintWithGraphics;

import java.awt.*;

public class PrintTurtle extends Node implements PrintWithGraphics {
    private final DockReceiving<Turtle> turtle = new DockReceiving<>("turtle", Turtle.class,new Turtle());
    private final DockReceiving<Number> px = new DockReceiving<>("X",Number.class,0);
    private final DockReceiving<Number> py = new DockReceiving<>("Y",Number.class,0);
    private final DockReceiving<Color> travelColor = new DockReceiving<>("travel color",Color.class,Color.GREEN);
    private final DockShipping<Boolean> showTravel = new DockShipping<>("show travel",Boolean.class,false);

    public PrintTurtle() {
        super("PrintTurtle");
        addVariable(turtle);
        addVariable(px);
        addVariable(py);
    }

    @Override
    public void update() {}

    @Override
    public void print(Graphics g) {
        if(turtle.hasPacketWaiting()) turtle.receive();
        if(px.hasPacketWaiting()) px.receive();
        if(py.hasPacketWaiting()) py.receive();
        if(travelColor.hasPacketWaiting()) travelColor.receive();

        Turtle myTurtle = turtle.getValue();
        if(myTurtle==null || myTurtle.history.isEmpty()) return;

        int dx=px.getValue().intValue();
        int dy=py.getValue().intValue();
        g.translate(dx,dy);

        // where we're at in the drawing (to check if we're between first & last)
        boolean showPenUp = showTravel.getValue();
        int count = 0;
        int first=0;
        int last=myTurtle.history.size();
        TurtleMove previousMove = null;

        Color upColor = travelColor.getValue();
        Color downColor = new Color(0,0,0);

        try {
            count++;

            for (TurtleMove m : myTurtle.history) {
                if(m==null) throw new NullPointerException();

                boolean inShow = (count >= first && count < last);
                switch (m.type) {
                    case TRAVEL -> {
                        if (inShow && previousMove != null) {
                            if (showPenUp) {
                                g.setColor(upColor);
                                g.drawLine((int) previousMove.x, (int)previousMove.y, (int) m.x, (int) m.y);
                            }
                        }
                        count++;
                        previousMove = m;
                    }
                    case DRAW_LINE -> {
                        if (inShow && previousMove != null) {
                            g.setColor(downColor);
                            g.drawLine((int) previousMove.x, (int)previousMove.y, (int) m.x, (int) m.y);
                        }
                        count++;
                        previousMove = m;
                    }
                    case TOOL_CHANGE -> {
                        downColor = m.getColor();
                        ((Graphics2D) g).setStroke(new BasicStroke((int) m.getDiameter()));
                    }
                }
            }
        }
        catch(Exception e) {
            //Log.error(e.getMessage());
        }

        g.translate(-dx,-dy);
    }
}
