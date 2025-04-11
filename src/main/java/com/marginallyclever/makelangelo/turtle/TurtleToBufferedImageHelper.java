package com.marginallyclever.makelangelo.turtle;

import com.marginallyclever.donatello.graphview.GraphViewPanel;
import com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle.PrintTurtle;
import com.marginallyclever.makelangelo.turtle.turtlerenderer.TurtleRenderFacade;
import com.marginallyclever.nodegraphcore.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * Methods to print a {@link Turtle} to a {@link BufferedImage}.
 */
public class TurtleToBufferedImageHelper {
    private static final Logger logger = LoggerFactory.getLogger(TurtleToBufferedImageHelper.class);

    /**
     * Generate an image from a {@link Turtle}.  The size of the {@link BufferedImage} is the {@link Turtle}'s bounding
     * box, or the minimumWidth, whichever is greater.
     * @param source the turtle to draw
     * @param node the node that is calling this method
     * @param desiredWidth the minimum size of the image
     * @return a {@link BufferedImage} of the turtle's path
     */
    public BufferedImage generateThumbnail(Turtle source, Node node, int desiredWidth) {
        if (source == null || !source.hasDrawing()) {
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        }

        Rectangle2D r = source.getBounds();
        int h = (int) Math.ceil(r.getHeight());
        int w = (int) Math.ceil(r.getWidth());
        if (w == 0 || h == 0) {
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        }
        // ensure minimum size
        int newW = w;
        int newH = h;
        if(newW<desiredWidth) {
            newH = (int) (h * desiredWidth / (double) w);
            newW = desiredWidth;
        }
        logger.debug("thumbnail {}x{} @ {},{}", newW, newH, r.getX(), r.getY());
        // create a new image with the minimum size
        BufferedImage img = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.scale((newW+1) / (double)w, (newH+1) / (double)h);
        g.translate(-r.getX(), -r.getY());
        paintTurtle(g,source,node);
        g.dispose();

        // resize to thumbnail
        int desiredHeight = (int) Math.ceil(r.getHeight() * desiredWidth / (double)w);
        logger.debug("shrink {}x{}", desiredWidth, desiredHeight);
        BufferedImage img2 = new BufferedImage(desiredWidth, desiredHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img2.createGraphics();
        GraphViewPanel.setHints(g2);
        g2.drawImage(img, 0, 0, desiredWidth, desiredHeight, null);

        return img2;
    }

    /**
     * Generate an image from a {@link Turtle}.  The size of the {@link BufferedImage} is the {@link Turtle}'s bounding box.
     * @param source the turtle to draw
     * @param node the node that is calling this method
     * @return a {@link BufferedImage} of the turtle's path
     */
    public BufferedImage generateImage(Turtle source, PrintTurtle node) {
        if (source == null || !source.hasDrawing()) {
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        }

        Rectangle2D r = source.getBounds();
        int h = (int) Math.ceil(r.getHeight());
        int w = (int) Math.ceil(r.getWidth());
        logger.debug("image {}x{} @ {},{}", w, h,r.getX(),r.getY());

        if (w == 0 || h == 0) {
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        }

        BufferedImage img = new BufferedImage(w+1, h+1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.translate(-r.getX(), -r.getY());
        paintTurtle(g,source,node);
        g.dispose();

        return img;
    }

    private void paintTurtle(Graphics2D g,Turtle source, Node node) {
        if(node!=null) node.setComplete(0);

        TurtleRenderFacade trf = new TurtleRenderFacade();
        trf.setTurtle(source);

        if(node instanceof PrintTurtle pt)  {
            pt.setupTurtleRenderFacade(trf);
        }

        GraphViewPanel.setHints(g);
        trf.render(g);

        if(node!=null) node.setComplete(100);
    }
}
