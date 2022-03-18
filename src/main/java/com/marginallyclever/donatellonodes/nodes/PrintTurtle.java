package com.marginallyclever.donatellonodes.nodes;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleMove;
import com.marginallyclever.nodegraphcore.PrintWithGraphics;
import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.nodegraphcore.NodeVariable;

import java.awt.*;

public class PrintTurtle extends Node implements PrintWithGraphics {
    private final NodeVariable<Turtle> turtle = NodeVariable.newInstance("turtle", Turtle.class,new Turtle(),true,false);
    private final NodeVariable<Number> px = NodeVariable.newInstance("X",Number.class,0,true,false);
    private final NodeVariable<Number> py = NodeVariable.newInstance("Y",Number.class,0,true,false);
    private final NodeVariable<Color> travelColor = NodeVariable.newInstance("travel color",Color.class,Color.GREEN,true,false);
    private final NodeVariable<Boolean> showTravel = NodeVariable.newInstance("show travel",Boolean.class,false,true,false);

    public PrintTurtle() {
        super("PrintTurtle");
        addVariable(turtle);
        addVariable(px);
        addVariable(py);
    }

    @Override
    public Node create() {
        return new PrintTurtle();
    }

    @Override
    public void update() throws Exception {
        cleanAllInputs();
    }

    @Override
    public void print(Graphics g) {
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
                        ColorRGB c = m.getColor();
                        downColor = new Color(c.red, c.green, c.blue);
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
