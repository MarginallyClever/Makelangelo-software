package com.marginallyclever.makelangelo.makeart.turtletool;

import com.marginallyclever.convenience.LineSegment2D;
import com.marginallyclever.convenience.linecollection.LineCollection;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.lineweight.LineWeight;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.lineweight.LineWeightSegment;
import com.marginallyclever.makelangelo.turtle.Turtle;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Take a turtle line and thicken it by the intensity of the image.
 */
public class ThickenLinesByIntensity {
    private final double EPSILON = 1e-3;

    // refinement of lines for sampling.  must be greater than zero.
    private double stepSize = 5;
    // maximum thickness of the new line. must be greater than zero.
    private double maxLineWidth = 3.0;
    // the pen diameter, controls spacing between passes.
    private double penDiameter = 0.8;
    private TransformedImage image;

    private final LinkedList<LineWeightSegment> unsorted = new LinkedList<>();

    // segments sorted for drawing efficiency
    private final List<LineWeight> sortedLines = new ArrayList<>();

    /**
     *
     * @param turtle the turtle to use as the source of lines
     * @param image the image to use as the source of intensity values
     * @param stepSize the refinement of lines for sampling.  must be greater than zero.
     * @param maxLineWidth the maximum thickness of the new line. must be greater than zero.
     * @param penDiameter the pen diameter, controls spacing between passes.
     */
    public Turtle execute(Turtle turtle,TransformedImage image, double stepSize, double maxLineWidth, double penDiameter) {
        this.image = image;
        this.stepSize = stepSize;
        this.maxLineWidth = maxLineWidth-penDiameter;
        this.penDiameter = penDiameter;

        // for each color,
        Turtle result = new Turtle();
        List<Turtle> colors = turtle.splitByToolChange();
        for( Turtle t2 : colors ) {
            // generate the thick lines
            result.add(calculate(t2));
        }

        return result;
    }

    private Turtle calculate(Turtle from) {
        buildSegmentList(from);
        sortSegmentsIntoLines();
        Turtle turtle = generateAllThickLines();

        // clean up
        unsorted.clear();
        sortedLines.clear();
        return turtle;
    }

    /**
     * Generate the thick lines
     * @return a turtle of the thick lines
     */
    private Turtle generateAllThickLines() {
        Turtle turtle = new Turtle();

        for(LineWeight i : sortedLines) {
            if(i.segments.isEmpty()) continue;
            turtle.add(generateOneThickLine(i));
        }

        return turtle;
    }

    /**
     * A line that gets thicker where the segment weight goes up.  This is done by drawing the line multiple times
     * along the entire length of the line.
     * @param line the line to draw
     * @return a turtle of the offset line.
     */
    private Turtle generateOneThickLine(LineWeight line) {
        Turtle turtle = new Turtle();
        double numPasses = getNumberOfPasses(line);
        boolean first = true;
        // collect all the points, write them at the end.
        for(int pass=0; pass<=numPasses; ++pass) {
            double ratio = pass/numPasses;
            // collect all the points
            List<Point2d> offsetLine = generateOneThickLinePass(line,ratio);
            if((pass%2)==1) Collections.reverse(offsetLine);
            drawPoints(turtle,offsetLine,first);
            first=false;
        }

        return turtle;
    }

    /**
     * Draw the points in the offset line.
     * @param turtle the turtle to draw with
     * @param offsetLine the line to draw
     * @param first true if the turtle should jump to the first point
     */
    private void drawPoints(Turtle turtle, List<Point2d> offsetLine, boolean first) {
        // draw pass
        for( Point2d p : offsetLine ) {
            if(first) {
                turtle.jumpTo(p.x,p.y);
                first=false;
            }
            turtle.moveTo(p.x,p.y);
        }
    }

    /**
     * Find the thickest part of the line, which tells us how many passes we'll have to make.  Guaranteed to return at
     * least 1 pass.
     * @param line the line to draw
     * @return the number of passes needed to draw the line
     */
    int getNumberOfPasses(LineWeight line) {
        double maxWeight=1;
        for(LineWeightSegment s : line.segments) {
            maxWeight = Math.max(maxWeight,s.weight);
        }
        return (int)Math.max(1,Math.ceil(maxWeight / penDiameter));
    }

    private List<Point2d> generateOneThickLinePass(LineWeight line, double distance) {
        List<Point2d> offsetSequence = new ArrayList<>();

        LineWeightSegment start = line.segments.getFirst();

        // add first point at start of line.  include the end cap offset.
        var s0 = getOffsetLine(start, adjustedOffset(start.weight,distance));
        Vector2d uStart = getUnitVector(s0);
        uStart.scale(start.weight/2);
        offsetSequence.add(new Point2d(s0.getX1()-uStart.x,s0.getY1()-uStart.y));

        // add the middle points of the line
        for( var seg : line.segments ) {
            var s1 = getOffsetLine(seg, adjustedOffset(seg.weight,distance));
            var intersection = findIntersection(s0,s1);
            offsetSequence.add((intersection != null) ? intersection : new Point2d(s1.getX1(), s1.getY1()));
            s0=s1;
        }

        // add last point at start of line.  include the end cap offset.
        Vector2d uEnd = getUnitVector(s0);
        uEnd.scale(line.segments.getLast().weight/2);
        offsetSequence.add(new Point2d(s0.getX2()+uEnd.x,s0.getY2()+uEnd.y));

        // done!
        return offsetSequence;
    }

    private Vector2d getUnitVector(Line2D s0) {
        double dx = s0.getX2()-s0.getX1();
        double dy = s0.getY2()-s0.getY1();
        Vector2d u = new Vector2d(dx,dy);
        u.normalize();
        return u;
    }

    /**
     * Find the intersection of two line segments.
     * @param a the first line
     * @param b the second line
     * @return the intersection point or null if the lines do not intersect.
     */
    private Point2d findIntersection(Line2D a, Line2D b) {
        double x1 = a.getX1(), y1 = a.getY1();
        double x2 = a.getX2(), y2 = a.getY2();
        double x3 = b.getX1(), y3 = b.getY1();
        double x4 = b.getX2(), y4 = b.getY2();

        double denom = ((x1-x2)*(y3-y4) - (y1-y2)*(x3-x4));
        if(Math.abs(denom)<1e-6) {
            // parallel or colinear.
            return null;
        }

        double ua = ((x4 - x3)*(y1 - y3) - (y4 - y3)*(x1 - x3)) / denom;
        double ub = ((x2 - x1)*(y1 - y3) - (y2 - y1)*(x1 - x3)) / denom;

        if (ua >= 0 && ua <= 1 && ub >= 0 && ub <= 1) {
            // intersection point is within both segments
            double x = x1 + ua * (x2 - x1);
            double y = y1 + ua * (y2 - y1);
            return new Point2d(x, y);
        }

        return null; // intersection point is outside the segments
    }

    /**
     * The offset of the line is the distance from the center of the line to the center of the new line.
     * A ratio of 0.0 is offset by -weight/2.0, and 1.0 is offset by +weight/2.0.
     * @param weight the weight of the line
     * @param ratio the ratio of the line
     * @return the adjusted offset
     */
    double adjustedOffset(double weight,double ratio) {
        //return weight*ratio - weight/2.0;
        return (weight / 2.0) * (2 * ratio - 1);
    }

    Line2D getOffsetLine(LineWeightSegment line, double distance) {
        var n = line.getNormal();
        n.normalize();
        n.scale(distance);

        // offset from the original line
        return new Line2D.Double(
                line.start.x+n.x, line.start.y+n.y,
                line.end.x  +n.x, line.end.y  +n.y);
    }

    /**
     * <p>Search through all unsorted segments for adjacent segments.  Start from a random segment and then
     * find any segment that touches the head or the tail of this segment.  Track the head and tail as we go.</p>
     * <p>Worst case is O(n<sup>2</sup>)</p>
     */
    private void sortSegmentsIntoLines() {
        while(!unsorted.isEmpty()) {
            growActiveLine();
        }
    }

    /**
     * <p>Grow the active line by finding the next segment that is adjacent to the head or tail of the line.
     * The worst case would be O(n^2/2) but in practice lines arrive already pretty well sorted.</p>
     */
    private void growActiveLine() {
        LineWeight activeLine = new LineWeight();
        activeLine.segments.add(unsorted.removeFirst());
        sortedLines.add(activeLine);

        LineWeightSegment head = activeLine.segments.getFirst();
        LineWeightSegment tail = head;
        LineWeightSegment toRemove;
        do {
            toRemove = null;
            for (LineWeightSegment s : unsorted) {
                if (closeEnoughToHead(head, s)) {  // try to match with head of line
                    activeLine.segments.addFirst(s);
                    head = s;
                    toRemove = s;
                    break;
                } else if (closeEnoughToTail(tail, s)) {  // try to match with tail of line
                    activeLine.segments.addLast(s);
                    tail = s;
                    toRemove = s;
                    break;
                }
            }
            if(toRemove!=null) unsorted.remove(toRemove);
        } while(toRemove!=null);
    }

    /**
     * @param head the first line
     * @param next the second line
     * @return true if {@link LineWeightSegment} head and next are in sequence.
     */
    private boolean closeEnoughToHead(LineWeightSegment head,LineWeightSegment next) {
        if(next==null) {
            throw new IllegalArgumentException("next is null");
        }
        // fast reject if truchet index too far apart
        if(Math.abs(head.ix-next.ix)>2 || Math.abs(head.iy-next.iy)>2) return false;
        if(closeEnough(head.start,next.end)) return true;
        if(closeEnough(head.start,next.start)) {
            // next is backwards
            next.flip();
            return true;
        }
        return false;
    }

    /**
     * @param tail the first line
     * @param next the second line
     * @return true if {@link LineWeightSegment} tail and next are in sequence.
     */
    private boolean closeEnoughToTail(LineWeightSegment tail, LineWeightSegment next) {
        if(next==null) {
            throw new IllegalArgumentException("next is null");
        }
        // fast reject if truchet index too far apart
        if(Math.abs(tail.ix-next.ix)>2 || Math.abs(tail.iy-next.iy)>2) return false;
        if(closeEnough(tail.end,next.start)) return true;
        if(closeEnough(tail.end,next.end)) {
            next.flip();
            return true;
        }
        return false;
    }

    boolean closeEnough(Point2d p0,Point2d p1) {
        return p0.distanceSquared(p1)<EPSILON;
    }

    /**
     * Split the turtle into segments of a certain length.
     * @param from the turtle to split
     */
    private void buildSegmentList(Turtle from) {
        LineCollection originalLines = from.getAsLineCollection();
        for(LineSegment2D before : originalLines) {
            maybeSplitLine(before);
        }
    }

    /**
     * Add a segment to the list of unsorted lines.  Splits long lines into smaller pieces.
     * @param segment the segment to split
     */
    private void maybeSplitLine(LineSegment2D segment) {
        double beforeLen = Math.sqrt(segment.lengthSquared());
        int pieces = (int)Math.max(1,Math.ceil(beforeLen / stepSize));
        if(pieces==1) {
            addOneUnsortedSegment(segment.start,segment.end);
            return;
        }

        Vector2d diff = new Vector2d(
                segment.end.x - segment.start.x,
                segment.end.y - segment.start.y
        );

        Point2d a = segment.start;
        for(int i=0;i<pieces-1;++i) {
            double t1 = (double)(i+1) / (double)pieces;
            Point2d b = new Point2d(
                    segment.start.x + diff.x * t1,
                    segment.start.y + diff.y * t1);
            addOneUnsortedSegment(a,b);
            a=b;
        }
        addOneUnsortedSegment(a,segment.end);
    }

    private void addOneUnsortedSegment(Point2d start, Point2d end) {
        unsorted.add(createLineWeightSegment(start,end));
    }

    private LineWeightSegment createLineWeightSegment(Point2d start, Point2d end) {
        // sample image intensity here from 0...1
        double mx = (start.x+end.x)/2.0;
        double my = (start.y+end.y)/2.0;

        double intensity = 1.0-(image.sample(mx,my,stepSize/2.0)/255.0);

        LineWeightSegment a = new LineWeightSegment(start,end,intensity * maxLineWidth);
        // make a fast search index
        a.ix = (int)Math.floor(mx / stepSize);
        a.iy = (int)Math.floor(my / stepSize);
        return a;
    }
}
