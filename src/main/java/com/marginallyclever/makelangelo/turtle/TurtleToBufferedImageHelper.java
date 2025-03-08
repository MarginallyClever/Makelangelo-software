package com.marginallyclever.makelangelo.turtle;

import com.marginallyclever.donatello.graphview.GraphViewPanel;
import com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle.PrintTurtle;
import com.marginallyclever.makelangelo.turtle.turtlerenderer.TurtleRenderFacade;
import com.marginallyclever.makelangelo.turtle.turtlerenderer.TurtleRenderFactory;
import com.marginallyclever.nodegraphcore.Node;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Methods to print a {@link Turtle} to a {@link BufferedImage}.
 */
public class TurtleToBufferedImageHelper {
    /**
     * Generate an image from a {@link Turtle}.  The size of the {@link BufferedImage} is the {@link Turtle}'s bounding
     * box, or the minimumWidth, whichever is greater.
     * @param source the turtle to draw
     * @param node the node that is calling this method
     * @param minimumWidth the minimum size of the image
     * @return a {@link BufferedImage} of the turtle's path
     */
    public static BufferedImage generateImage(Turtle source, Node node, int minimumWidth) {
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

    /**
     * Generate an image from a {@link Turtle}.  The size of the {@link BufferedImage} is the {@link Turtle}'s bounding box.
     * @param source the turtle to draw
     * @param node the node that is calling this method
     * @return a {@link BufferedImage} of the turtle's path
     */
    public static BufferedImage generateImage(Turtle source, PrintTurtle node) {
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

    private static void paintTurtle(Graphics2D g,Turtle source, Node node) {
        TurtleRenderFacade trf = new TurtleRenderFacade();
        if(node instanceof PrintTurtle pt)  {
            ArrayList<String> names = new ArrayList<>();
            Arrays.stream(TurtleRenderFactory.values()).forEach(f-> names.add(f.getName()));
            var nameIndex = (int)pt.getPort("style").getValue();

            trf.setRenderer(TurtleRenderFactory.findByName(names.get(nameIndex)).getTurtleRenderer());
            trf.setPenDiameter((int)pt.getPort("line thickness").getValue());
            trf.setUpColor((Color)pt.getPort("travel color").getValue());
        }
        if(node!=null) node.setComplete(0);
        trf.setTurtle(source);
        trf.render(g);
        if(node!=null) node.setComplete(100);
    }
}
