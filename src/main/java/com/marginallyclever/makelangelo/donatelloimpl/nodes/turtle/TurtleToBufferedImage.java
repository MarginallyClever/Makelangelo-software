package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle;

import com.marginallyclever.donatello.graphview.GraphViewPanel;
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
        output.setValue(generateImage(source,this,(int)getRectangle().getWidth()));
    }

    /**
     * Generate an image from a {@link Turtle}.
     * @param source the turtle to draw
     * @param node the node that is calling this method
     * @param minimumWidth the minimum size of the image
     * @return
     */
    public static BufferedImage generateImage(Turtle source,Node node,int minimumWidth) {
        if (source == null || !source.hasDrawing()) {
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        }

        Rectangle2D r = source.getBounds();
        int h = (int) Math.ceil(r.getHeight());
        int w = (int) Math.ceil(r.getWidth());
        if (w == 0 || h == 0) {
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        }
        int newW = w;
        int newH = h;
        if(newW<minimumWidth) {
            newH = (int) (h * minimumWidth / (double) w);
            newW = minimumWidth;
        }

        BufferedImage img = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        GraphViewPanel.setHints(g);
        g.scale((newW+1) / (double)w, (newH+1) / (double)h);
        g.translate(-r.getX(), -r.getY());
        paintTurtle(g,source,node);
        g.dispose();

        return img;
    }

    public static BufferedImage generateImage(Turtle source,Node node) {
        if (source == null || !source.hasDrawing()) {
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        }

        Rectangle2D r = source.getBounds();
        int h = (int) Math.ceil(r.getHeight());
        int w = (int) Math.ceil(r.getWidth());
        if (w == 0 || h == 0) {
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        }

        BufferedImage img = new BufferedImage(w+1, h+1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        GraphViewPanel.setHints(g);
        g.translate(-r.getX(), -r.getY());
        paintTurtle(g,source,node);
        g.dispose();

        return img;
    }

    public static void paintTurtle(Graphics2D g,Turtle source, Node node) {
        TurtleMove previousMove = null;
        Color downColor = Color.BLACK;

        if(node!=null) node.setComplete(0);
        int i=0;
        int size = source.history.size();

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
            if(node!=null) node.setComplete((int) (i++ * 100.0 / size));
        }

        if(node!=null) node.setComplete(100);
    }
}
