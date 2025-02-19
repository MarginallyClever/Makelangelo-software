package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle;

import com.marginallyclever.convenience.Clipper2D;
import com.marginallyclever.convenience.LineCollection;
import com.marginallyclever.convenience.LineSegment2D;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.donatello.ports.InputImage;
import com.marginallyclever.donatello.ports.InputInt;
import com.marginallyclever.donatello.ports.InputRange;
import com.marginallyclever.makelangelo.donatelloimpl.ports.InputTurtle;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputTurtle;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Use an image to mask a path.  Lay the path over the image and remove all parts of the path where the image is brighter
 * than a cutoff value.  The fine grain resolution (and the amount of testing) is controlled by the stepSize.
 * @author Dan Royer
 * @since 2022-03-08
 */
public class PathImageMask extends Node {
    private final InputImage image = new InputImage("image");
    private final InputTurtle turtle = new InputTurtle("turtle");
    private final InputInt stepSize = new InputInt("stepSize", 5);
    private final InputRange threshold = new InputRange("threshold",128,255,0);
    private final OutputTurtle outputAbove = new OutputTurtle("above");
    private final OutputTurtle outputBelow = new OutputTurtle("below");

    private final LineCollection listAbove = new LineCollection();
    private final LineCollection listBelow = new LineCollection();

    public PathImageMask() {
        super("PathImageMask");
        addPort(image);
        addPort(turtle);
        addPort(stepSize);
        addPort(threshold);
        addPort(outputAbove);
        addPort(outputBelow);
    }

    @Override
    public void update() {
        Turtle myTurtle = turtle.getValue();
        if(myTurtle==null || myTurtle.history.isEmpty()) return;

        setComplete(0);

        LineCollection lines  = myTurtle.getAsLineSegments();
        BufferedImage src = image.getValue();
        
        listAbove.clear();
        listBelow.clear();
        
        double s = Math.max(1, stepSize.getValue());
        double c = Math.max(0,Math.min(255, threshold.getValue()));
        int size = lines.size();
        int i=0;

        for (LineSegment2D line : lines) {
            scanLine(src, line, s, c);
            setComplete(i++ * 99 / size);
        }

        Turtle resultAbove = new Turtle();
        resultAbove.addLineSegments(listAbove);
        outputAbove.setValue(resultAbove);

        Turtle resultBelow = new Turtle();
        resultBelow.addLineSegments(listBelow);
        setComplete(100);
        outputBelow.setValue(resultBelow);
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

        // clip line to image bounds because sampling outside limits causes exception.
        int w2 = img.getWidth()/2;
        int h2 = img.getHeight()/2;
        Point2D rMin = new Point2D(-w2,-h2);
        Point2D rMax = new Point2D(w2,h2);
        if(!Clipper2D.clipLineToRectangle(P0, P1, rMax, rMin)) {
            // entire line clipped
            return;
        }
        // now we know all points in the line are inside the rectangle.

        // walk the line
        double dx = P1.x - P0.x;
        double dy = P1.y - P0.y;
        double distance = Math.sqrt(dx*dx+dy*dy);
        double total = Math.max(1,Math.ceil(distance / stepSize));
        Point2D a = new Point2D(P0);
        Point2D b = new Point2D();

        for( double i = 1; i <= total; ++i ) {
            double fraction = i / total;
            b.set(P0.x + dx * fraction,
                  P0.y + dy * fraction);
            var test = sampleImageUnderStep(img,a,b);
            if(test < channelCutoff) {
                listBelow.add(new LineSegment2D(a,b, Color.BLACK));
            } else {
                listAbove.add(new LineSegment2D(a,b, Color.BLACK));
            }
            a.set(b);
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
        int left = (int)Math.min(a.x,b.x);
        int right = (int)Math.max(a.x,b.x);
        int bottom = (int)Math.min(a.y,b.y);
        int top = (int)Math.max(a.y,b.y);
        int w2 = img.getWidth()/2;
        int h2 = img.getHeight()/2;

        // get the average of the intensities
        double sum = 0;
        int total = 0;
        for(int y=bottom+h2; y<=top+h2; ++y) {
            for(int x=left+w2; x<=right+w2; ++x) {
                if(x<0 || x>=img.getWidth() || y<0 || y>=img.getHeight()) continue;
                sum += intensity(img.getRGB(x, y));
                total++;
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
