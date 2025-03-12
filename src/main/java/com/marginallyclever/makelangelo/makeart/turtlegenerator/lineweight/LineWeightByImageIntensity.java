package com.marginallyclever.makelangelo.makeart.turtlegenerator.lineweight;

import com.marginallyclever.convenience.linecollection.LineCollection;
import com.marginallyclever.convenience.LineSegment2D;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.TurtleGenerator;
import com.marginallyclever.donatello.select.SelectDouble;
import com.marginallyclever.donatello.select.SelectFile;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import java.awt.geom.Rectangle2D;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Generate a drawing where the thickness of the line is determined by the intensity of the image at that point.
 * @deprecated since 7.62.0 to be replaced with Donatello nodes.
 */
@Deprecated(since="7.62.0")
public class LineWeightByImageIntensity extends TurtleGenerator {
    private static final Logger logger = LoggerFactory.getLogger(LineWeightByImageIntensity.class);

    private final double EPSILON = 0.001;

    /**
     * must be greater than zero.
     */
    private static double stepSize = 5;

    /**
     * maximum thickness of the new line. must be greater than zero.
     */
    private static double thickness = 1.0;

    private static String imageName = null;
    private TransformedImage sourceImage;

    private static final LinkedList<LineWeightSegment> unsorted = new LinkedList<>();

    // segments sorted for drawing efficiency
    private static final List<LineWeight> sortedLines = new ArrayList<>();

    public LineWeightByImageIntensity() {
        super();

        SelectDouble selectThickness = new SelectDouble("thickness", Translator.get("LineWeightByImageIntensity.thickness"),thickness);
        add(selectThickness);
        selectThickness.addSelectListener(e->{
            thickness = selectThickness.getValue();
            generate();
        });

        SelectFile selectFile = new SelectFile("image", Translator.get("LineWeightByImageIntensity.image"),imageName,null);
        add(selectFile);
        selectFile.addSelectListener(e->{
            imageName = selectFile.getText();
            generate();
        });
    }

    @Override
    public String getName() {
        return Translator.get("LineWeightByImageIntensity.name");
    }

    @Override
    public void generate() {
        try {
            FileInputStream stream = new FileInputStream(imageName);
            sourceImage = new TransformedImage(ImageIO.read(stream));
        } catch(Exception e) {
            logger.error("generate {}",e.getMessage(),e);
            setTurtle(previousTurtle);
            return;
        }
        scaleImage(1);  // fill paper

        Turtle turtle = new Turtle();
        List<Turtle> colors = previousTurtle.splitByToolChange();
        for( Turtle t2 : colors ) {
            turtle.add(calculate(t2));
        }

        sourceImage = null;

        turtle.translate(myPaper.getCenterX(),myPaper.getCenterY());

        notifyListeners(turtle);
    }

    private Turtle calculate(Turtle from) {
        Turtle turtle = new Turtle();
        buildSegmentList(from);
        sortSegmentsIntoLines();
        generateThickLines(turtle);
        //generateThinLines(turtle);

        // clean up
        unsorted.clear();
        sortedLines.clear();
        return turtle;
    }

    /**
     * Generate thin lines for debugging
     * @param turtle the turtle to draw with
     */
    private void generateThinLines(Turtle turtle) {
        logger.debug("generateThinLines {}",sortedLines.size());
        for(LineWeight line : sortedLines) {
            boolean first=true;
            for(LineWeightSegment w : line.segments) {
                if (first) {
                    turtle.jumpTo(w.start.x, w.start.y);
                    first = false;
                }
                turtle.moveTo(w.end.x, w.end.y);
            }
        }
    }

    /**
     * mode 0 = fill paper
     * mode 1 = fit paper
     * @param mode the mode to scale the image
     */
    private void scaleImage(int mode) {
        Rectangle2D.Double rect = myPaper.getMarginRectangle();
        double width  = rect.getWidth();
        double height = rect.getHeight();

        boolean test;
        if (mode == 0) {
            test = width < height;  // fill paper
        } else {
            test = width > height;  // fit paper
        }

        float f;
        if( test ) {
            f = (float)( width / (double)sourceImage.getSourceImage().getWidth() );
        } else {
            f = (float)( height / (double)sourceImage.getSourceImage().getHeight() );
        }
        sourceImage.setScale(f,-f);
    }

    private void generateThickLines(Turtle turtle) {
        logger.debug("generateThickLines");
        for(LineWeight i : sortedLines) {
            if(i.segments.isEmpty()) continue;
            generateOneThickLine(turtle,i);
        }
    }

    private void generateOneThickLine(Turtle turtle, LineWeight line) {
        // find the thickest part of the line, which tells us how many cycles we'll have to make.
        double numPasses=0;
        for(LineWeightSegment s : line.segments) {
            numPasses = Math.max(numPasses,s.weight);
        }
        numPasses = Math.max(1,Math.ceil(numPasses));

        LineWeightSegment start = line.segments.get(0);

        boolean first=true;
        // collect all the points, write them at the end.
        for(int pass=0; pass<=numPasses; ++pass) {
            double ratio = pass/numPasses;
            List<Point2d> offsetLine = generateOneThickLinePass(line,start,ratio);
            if((pass%2)==1) Collections.reverse(offsetLine);

            // draw pass
            for( Point2d p : offsetLine ) {
                if(first) {
                    turtle.jumpTo(p.x,p.y);
                    first=false;
                }
                turtle.moveTo(p.x,p.y);
            }
        }
    }

    private List<Point2d> generateOneThickLinePass(LineWeight line, LineWeightSegment start, double distance) {
        List<Point2d> offsetSequence = new ArrayList<>();

        // add first point at start of line
        double [] s0 = getOffsetLine(start, adjustedOffset(start.weight,distance));

        Vector2d unit = line.segments.get(0).getUnit();
        unit.scale(distance);
        offsetSequence.add(new Point2d(s0[0]-unit.x,s0[1]-unit.y));

        // add the middle points of the line
        for(int i=1;i<line.segments.size();++i) {
            LineWeightSegment seg = line.segments.get(i);
            double [] s1 = getOffsetLine(seg, adjustedOffset(seg.weight,distance));
            if(Math.abs(dotProduct(s0,s1))<Math.cos(Math.toRadians(75))) {
                // this is a corner.  add a point at the intersection of the two lines.
                double [] inter = findIntersection(
                        s0[0],s0[1],s0[2],s0[3],
                        s1[0],s1[1],s1[2],s1[3]
                );
                offsetSequence.add(new Point2d(inter[0],inter[1]));
                //offsetSequence.add(new Point2d(s1[0],s1[1]));
            } else {
                offsetSequence.add(new Point2d(s1[0], s1[1]));
            }
            s0=s1;
        }
        // add the last point of the line
        unit = line.segments.get(line.segments.size()-1).getUnit();
        unit.scale(distance);
        offsetSequence.add(new Point2d(s0[2]+unit.x,s0[3]+unit.y));
        return offsetSequence;
    }

    /**
     * @param s0 the first line segment
     * @param s1 the second line
     * @return the dot product of the two lines
     */
    private double dotProduct(double[] s0, double[] s1) {
        double dx0 = s0[2]-s0[0];
        double dy0 = s0[3]-s0[1];
        double dx1 = s1[2]-s1[0];
        double dy1 = s1[3]-s1[1];
        return dx0*dx1 + dy0*dy1;
    }

    private double [] findIntersection(double x1,double y1,double x2,double y2,double x3,double y3,double x4,double y4) {
        double d = ((x1-x2)*(y3-y4) - (y1-y2)*(x3-x4));
        if(Math.abs(d)<0.01) {
            // lines are colinear (infinite solutions) or parallel (no solutions).
            double ix = (x4+x1)/2;
            double iy = (y4+y1)/2;
            return new double [] { ix, iy };
        }

        double t = ((x1-x3)*(y3-y4) - (y1-y3)*(x3-x4)) / d;
        //double u = ((x1-x2)*(y1-y3) - (y1-y2)*(x1-x3)) / d;

        double ix = x1+t*(x2-x1);
        double iy = y1+t*(y2-y1);
        return new double[] { ix, iy };
    }

    private double adjustedOffset(double weight,double ratio) {
        return weight*ratio - weight/2.0;
    }

    double[] getOffsetLine(LineWeightSegment line,double distance) {
        // get normal of each line, then scale by distance.
        double nx = line.end.y - line.start.y;
        double ny = line.start.x - line.end.x;
        double nd = Math.sqrt(nx*nx + ny*ny);
        if(nd==0) nd=1;
        nx *= distance / nd;
        ny *= distance / nd;

        // offset from the original line
        return new double[] {
                line.start.x+nx, line.start.y+ny,
                line.end.x  +nx, line.end.y  +ny
        };
    }

    /**
     * Search through all unsorted segments for adjacent segments.  Start from a random segment and then
     * Find any segment that touches the head or the tail of this segment.  Track the head and tail as we go.
     */
    private void sortSegmentsIntoLines() {
        logger.debug("sortSegmentsIntoLines");

        while(!unsorted.isEmpty()) {
            LineWeight activeLine = new LineWeight();
            activeLine.segments.add(unsorted.removeFirst());
            sortedLines.add(activeLine);

            growActiveLine(activeLine);
        }

        logger.debug("sortedLines="+sortedLines.size());
    }

    private void growActiveLine(LineWeight activeLine) {
        LineWeightSegment head = activeLine.segments.get(0);
        LineWeightSegment tail = head;

        boolean found;
        do {
            found=false;
            for (LineWeightSegment s : unsorted) {
                if (closeEnoughToHead(head, s)) {  // try to match with head of line
                    activeLine.segments.addFirst(s);
                    head = s;
                    unsorted.remove(s);
                    found = true;
                    break;
                } else if (closeEnoughToTail(tail, s)) {  // try to match with tail of line
                    activeLine.segments.addLast(s);
                    tail = s;
                    unsorted.remove(s);
                    found = true;
                    break;
                }
            }
        } while(found);
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
        if(Math.abs(head.ix-next.ix)>6 || Math.abs(head.iy-next.iy)>6) return false;
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

    private void buildSegmentList(Turtle from) {
        logger.debug("buildSegmentList before={}",from.countLoops());

        LineCollection originalLines = from.getAsLineSegments();
        logger.debug("originalLines={}",originalLines.size());

        for(LineSegment2D before : originalLines) {
            maybeSplitLine(before);
        }

        logger.debug("unsorted={}",unsorted.size());
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
        unsorted.add(createLSW(start,end));
    }

    private LineWeightSegment createLSW(Point2d start, Point2d end) {
        // sample image intensity here from 0...1
        double mx = (start.x+end.x)/2.0;
        double my = (start.y+end.y)/2.0;

        double intensity = 1.0-(sourceImage.sample(mx,my,stepSize/2)/255.0);
        LineWeightSegment a = new LineWeightSegment(start,end,intensity*thickness);
        // make a fast search index
        a.ix = (int)Math.floor(mx / stepSize);
        a.iy = (int)Math.floor(my / stepSize);
        return a;
    }
}