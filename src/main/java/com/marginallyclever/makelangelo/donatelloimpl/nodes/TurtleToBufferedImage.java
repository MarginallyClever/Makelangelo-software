package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.makelangelo.donatelloimpl.ports.InputTurtle;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputImage;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleMove;
import com.marginallyclever.nodegraphcore.Node;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * Convert a {@link Turtle} to a {@link BufferedImage}.
 */
public class TurtleToBufferedImage extends Node {
    private final InputTurtle turtle = new InputTurtle("turtle");
    private final OutputImage output = new OutputImage("output");

    public TurtleToBufferedImage() {
        super("TurtleToBufferedImage");
        addVariable(turtle);
        addVariable(output);
    }

    @Override
    public void update() {
        Turtle myTurtle = turtle.getValue();
        if(myTurtle!=null && !myTurtle.history.isEmpty()) {
            Rectangle2D r = myTurtle.getBounds();
            int h = (int)Math.ceil(r.getHeight());
            int w = (int)Math.ceil(r.getWidth());
            BufferedImage img = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = (Graphics2D)img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,RenderingHints.VALUE_STROKE_PURE);
            g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
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
            output.send(img);
        }
    }
}
