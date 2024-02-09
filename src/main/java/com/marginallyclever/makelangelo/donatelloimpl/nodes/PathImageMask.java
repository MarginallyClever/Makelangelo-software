package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.convenience.*;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.DockReceiving;
import com.marginallyclever.nodegraphcore.DockShipping;
import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.nodegraphcore.Packet;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Use an image to mask a path.  Lay the path over the image and remove all parts of the path where the image is brighter
 * than a cutoff value.  The fine grain resolution (and the amount of testing) is controlled by the stepSize.
 * @author Dan Royer
 * @since 2022-03-08
 */
public class PathImageMask extends Node {
    private final DockReceiving<BufferedImage> image = new DockReceiving<>("image", BufferedImage.class,new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB));
    private final DockReceiving<Turtle> turtle = new DockReceiving<>("turtle", Turtle.class,new Turtle());
    private final DockReceiving<Number> stepSize = new DockReceiving<>("stepSize", Number.class, 5);
    private final DockReceiving<Number> threshold = new DockReceiving<>("threshold", Number.class, 128);
    private final DockShipping<Turtle> outputAbove = new DockShipping<>("above", Turtle.class,new Turtle());
    private final DockShipping<Turtle> outputBelow = new DockShipping<>("below", Turtle.class,new Turtle());

    private final LineCollection listAbove = new LineCollection();
    private final LineCollection listBelow = new LineCollection();

    public PathImageMask() {
        super("PathImageMask");
        addVariable(image);
        addVariable(turtle);
        addVariable(stepSize);
        addVariable(threshold);
        addVariable(outputAbove);
        addVariable(outputBelow);
    }

    @Override
    public void update() {
        if(image.hasPacketWaiting()) image.receive();
        if(turtle.hasPacketWaiting()) turtle.receive();
        if(stepSize.hasPacketWaiting()) stepSize.receive();
        if(threshold.hasPacketWaiting()) threshold.receive();

        Turtle myTurtle = turtle.getValue();
        if(myTurtle==null || myTurtle.history.isEmpty()) return;

        LineCollection lines  = myTurtle.getAsLineSegments();
        BufferedImage src = image.getValue();
        
        listAbove.clear();
        listBelow.clear();
        
        double s = Math.max(1, stepSize.getValue().doubleValue());
        double c = Math.max(0,Math.min(255, threshold.getValue().doubleValue()));

        for(LineSegment2D line : lines) {
            scanLine(src,line,s,c);
        }

        Turtle resultAbove = new Turtle();
        resultAbove.addLineSegments(listAbove);
        outputAbove.send(new Packet<>(resultAbove));

        Turtle resultBelow = new Turtle();
        resultBelow.addLineSegments(listBelow);
        outputBelow.send(new Packet<>(resultBelow));
    }

    /**
     * Drag the pen across the paper from <code>seg.start</code> to <code>seg.end</code>, taking stepSize steps.  If the
     * intensity of img at a step is less than or equal to the channelCutoff, keep the step. Results will be in the
     * {@link #listAbove} and {@link #listBelow}.
     *
     * @param img the image to sample while converting along the line.
     * @param segment the line to walk.
     * @param stepSize millimeters level of detail for this line.
     * @param channelCutoff only put pen down when color below this amount.
     */
    private void scanLine(BufferedImage img, LineSegment2D segment, double stepSize, double channelCutoff) {
        Point2D P0 = segment.start;
        Point2D P1 = segment.end;

        LineCollection toKeep = new LineCollection();

        // clip line to image bounds because sampling outside limits causes exception.
        Point2D rMin = new Point2D(0,0);
        Point2D rMax = new Point2D(img.getWidth(),img.getHeight());
        if(!Clipper2D.clipLineToRectangle(P0, P1, rMax, rMin)) {
            // entire line clipped
            return;
        }

        // walk the line
        double dx = P1.x - P0.x;
        double dy = P1.y - P0.y;
        double distance = Math.sqrt(dx*dx+dy*dy);
        double total = Math.min(1,Math.ceil(distance / stepSize));
        Point2D a = P0;

        for( double i = 1; i <= total; ++i ) {
            double fraction = i / total;
            Point2D b = new Point2D(dx * fraction + P0.x,dy * fraction + P0.y);
            double sampleResult = sampleImageUnderStep(img,a,b);
            if(sampleResult < channelCutoff) {
                listBelow.add(new LineSegment2D(a,b, Color.BLACK));
            } else {
                listAbove.add(new LineSegment2D(a,b, Color.BLACK));
            }
            a = b;
        }
        
        // TODO run a mini-merge to reduce the number of new segments?
    }

    /**
     * Returns the average intensity of the image within the rectangle bounded by points <code>a</code> and <code>b</code>.
     * @param img the source image
     * @param a one corner of the rectangle.
     * @param b one corner of the rectangle.
     * @return the average intensity of the image within the rectangle bounded by points <code>a</code> and <code>b</code>.
     */
    private double sampleImageUnderStep(BufferedImage img, Point2D a, Point2D b) {
        // find the top-left and bottom-right corners
        int left = (int)Math.floor(Math.min(a.x,b.x));
        int right = (int)Math.ceil(Math.max(a.x,b.x));
        int bottom = (int)Math.floor(Math.min(a.y,b.y));
        int top = (int)Math.ceil(Math.max(a.y,b.y));
        double total = Math.max(1,(right-left) * (top-bottom));
        // get the average of the intensities
        double sum = 0;
        for(int y=bottom; y<top; ++y) {
            for(int x=left; x<right; ++x) {
                sum += intensity(img.getRGB(x,y));
            }
        }
        return Math.max(0,Math.min(255, sum / total ));
    }

    /**
     * Returns the average of the red, green, and blue color channels.
     * @param rgb the color in <code>0xRRGGBB</code> format.
     * @return the average of the red, green, and blue color channels.
     */
    private double intensity(int rgb) {
        double r = (rgb >> 16) & 0xff;
        double g = (rgb >>  8) & 0xff;
        double b = (rgb      ) & 0xff;
        return ( r + g + b ) / 3.0;
    }
}
