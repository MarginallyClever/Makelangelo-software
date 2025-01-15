package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.donatello.graphview.GraphViewPanel;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleMove;
import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.nodegraphcore.PrintWithGraphics;
import com.marginallyclever.nodegraphcore.port.Input;
import com.marginallyclever.nodegraphcore.port.Output;

import java.awt.*;

public class PrintTurtle extends Node implements PrintWithGraphics {
    //private static final Logger logger = LoggerFactory.getLogger(PrintTurtle.class);
    private final Input<Turtle> turtle = new Input<>("turtle", Turtle.class,new Turtle());
    private final Input<Number> px = new Input<>("X",Number.class,0);
    private final Input<Number> py = new Input<>("Y",Number.class,0);
    private final Output<Boolean> showTravel = new Output<>("show travel",Boolean.class,false);
    private final Input<Color> travelColor = new Input<>("travel color",Color.class,Color.GREEN);

    public PrintTurtle() {
        super("PrintTurtle");
        addVariable(turtle);
        addVariable(px);
        addVariable(py);
        addVariable(showTravel);
        addVariable(travelColor);
    }

    @Override
    public void update() {}

    @Override
    public void print(Graphics g) {
        Turtle myTurtle = turtle.getValue();
        if(myTurtle==null || myTurtle.history.isEmpty()) return;

        Graphics2D g2 = (Graphics2D)g.create();
        GraphViewPanel.setHints(g2);

        int dx=px.getValue().intValue();
        int dy=py.getValue().intValue();
        g2.translate(dx,dy);

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
                                g2.setColor(upColor);
                                g2.drawLine((int) previousMove.x, (int)previousMove.y, (int) m.x, (int) m.y);
                            }
                        }
                        count++;
                        previousMove = m;
                    }
                    case DRAW_LINE -> {
                        if (inShow && previousMove != null) {
                            g2.setColor(downColor);
                            g2.drawLine((int) previousMove.x, (int)previousMove.y, (int) m.x, (int) m.y);
                        }
                        count++;
                        previousMove = m;
                    }
                    case TOOL_CHANGE -> {
                        downColor = m.getColor();
                        g2.setStroke(new BasicStroke((int) m.getDiameter()));
                    }
                }
            }
        }
        catch(Exception e) {
            //logger.error(e.getMessage());
        }
    }
}
