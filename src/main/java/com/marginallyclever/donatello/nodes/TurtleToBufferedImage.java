package com.marginallyclever.donatello.nodes;

import com.marginallyClever.nodeGraphCore.Node;
import com.marginallyClever.nodeGraphCore.NodeVariable;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleMove;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class TurtleToBufferedImage extends Node {
    private final NodeVariable<Turtle> turtle = NodeVariable.newInstance("turtle", Turtle.class,new Turtle(),true,false);
    private final NodeVariable<BufferedImage> output = NodeVariable.newInstance("output", BufferedImage.class, new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB),false,true);

    public TurtleToBufferedImage() {
        super("TurtleToImage");
        addVariable(turtle);
        addVariable(output);
    }

    @Override
    public Node create() {
        return new TurtleToBufferedImage();
    }

    @Override
    public void update() {
        Turtle myTurtle = turtle.getValue();
        if(myTurtle!=null && !myTurtle.history.isEmpty()) {
            Rectangle2D r = myTurtle.getBounds();
            int h = (int)Math.ceil(r.getHeight());
            int w = (int)Math.ceil(r.getWidth());
            BufferedImage img = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.translate(-r.getX(),-r.getY());

            // where we're at in the drawing (to check if we're between first & last)
            int count = 0;
            int first = 0;
            int last = myTurtle.history.size();
            TurtleMove previousMove = null;

            Color downColor = new Color(0, 0, 0);

            try {
                count++;

                for (TurtleMove m : myTurtle.history) {
                    if (m == null) throw new NullPointerException();

                    boolean inShow = (count >= first && count < last);
                    switch (m.type) {
                        case TRAVEL -> {
                            count++;
                            previousMove = m;
                        }
                        case DRAW_LINE -> {
                            if (inShow && previousMove != null) {
                                g.setColor(downColor);
                                g.drawLine((int) previousMove.x, (int) previousMove.y, (int) m.x, (int) m.y);
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
                output.setValue(img);
                cleanAllInputs();
            } catch (Exception e) {
                //Log.error(e.getMessage());
            }
        }
    }
}
