package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleMove;
import com.marginallyclever.nodegraphcore.DockReceiving;
import com.marginallyclever.nodegraphcore.DockShipping;
import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.nodegraphcore.Packet;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class TurtleToBufferedImage extends Node {
    private final DockReceiving<Turtle> turtle = new DockReceiving<>("turtle", Turtle.class,new Turtle());
    private final DockShipping<BufferedImage> output = new DockShipping<>("output", BufferedImage.class, new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB));

    public TurtleToBufferedImage() {
        super("TurtleToImage");
        addVariable(turtle);
        addVariable(output);
    }

    @Override
    public void update() {
        if(turtle.hasPacketWaiting()) turtle.receive();

        Turtle myTurtle = turtle.getValue();
        if(myTurtle!=null && !myTurtle.history.isEmpty()) {
            Rectangle2D r = myTurtle.getBounds();
            int h = (int)Math.ceil(r.getHeight());
            int w = (int)Math.ceil(r.getWidth());
            BufferedImage img = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.translate(-r.getX(),-r.getY());

            TurtleMove previousMove = null;
            Color downColor = Color.BLACK;

            for (TurtleMove m : myTurtle.history) {
                if (m == null) throw new NullPointerException();

                switch (m.type) {
                    case TRAVEL -> {
                        previousMove = m;
                    }
                    case DRAW_LINE -> {
                        if (previousMove != null) {
                            g.setColor(downColor);
                            g.drawLine((int) previousMove.x, (int) previousMove.y, (int) m.x, (int) m.y);
                        }
                        previousMove = m;
                    }
                    case TOOL_CHANGE -> {
                        downColor = m.getColor();
                        g.setStroke(new BasicStroke((int) m.getDiameter()));
                    }
                }
            }
            output.send(new Packet<>(img));
        }
    }
}
