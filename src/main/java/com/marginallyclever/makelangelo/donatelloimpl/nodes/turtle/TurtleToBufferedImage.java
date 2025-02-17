package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle;

import com.marginallyclever.makelangelo.donatelloimpl.ports.InputTurtle;
import com.marginallyclever.donatello.ports.OutputImage;
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
        addPort(turtle);
        addPort(output);
    }

    @Override
    public void update() {
        Turtle source = turtle.getValue();
        output.setValue(generateImage(source,this));
    }

    public static BufferedImage generateImage(Turtle source,Node node) {
        if(source==null || source.history.isEmpty()) {
            return new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);
        }

        int size = source.history.size();

        node.setComplete(0);
        Rectangle2D r = source.getBounds();
        int h = (int) Math.ceil(r.getHeight());
        int w = (int) Math.ceil(r.getWidth());
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.translate(-r.getX(), -r.getY());

        TurtleMove previousMove = null;
        Color downColor = Color.BLACK;

        int i=0;
        for (TurtleMove m : source.history) {
            if (m == null) throw new NullPointerException();

            switch (m.type) {
                case TRAVEL -> previousMove = m;
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
            node.setComplete((int) (i++ * 100.0 / size));
        }

        node.setComplete(100);

        return img;
    }
}
