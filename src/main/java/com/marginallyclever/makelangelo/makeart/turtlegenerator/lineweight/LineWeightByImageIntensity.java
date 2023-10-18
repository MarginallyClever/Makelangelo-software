package com.marginallyclever.makelangelo.makeart.turtlegenerator.lineweight;

import com.marginallyclever.convenience.LineCollection;
import com.marginallyclever.convenience.LineSegment2D;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.TurtleGenerator;
import com.marginallyclever.makelangelo.select.SelectDouble;
import com.marginallyclever.makelangelo.select.SelectFile;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.vecmath.Vector2d;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    private static final List<LineSegmentWeight> unsorted = new ArrayList<>();

    // segments sorted for drawing efficiency
    private static final List<LineWeight> sortedLines = new ArrayList<>();
    private static final List<LineWeight> orderedLines = new ArrayList<>();

    public LineWeightByImageIntensity() {
        super();

        SelectDouble selectThickness = new SelectDouble("thickness", Translator.get("LineWeightByImageIntensity.thickness"),thickness);
        add(selectThickness);
        selectThickness.addPropertyChangeListener(e->{
            thickness = selectThickness.getValue();
            generate();
        });

        SelectFile selectFile = new SelectFile("image", Translator.get("LineWeightByImageIntensity.image"),imageName);
        add(selectFile);
        selectFile.addPropertyChangeListener(e->{
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

        notifyListeners(turtle);
    }

    private Turtle calculate(Turtle from) {
        Turtle turtle = new Turtle();
        buildSegmentList(from);
        sortSegmentsIntoLines();
        flipAndSmooth();
        sortLinesByTravel();
        generateThickLines(turtle);
        //generateLines(turtle);

        // clean up
        unsorted.clear();
        sortedLines.clear();
        orderedLines.clear();
        return turtle;
    }

    private void generateLines(Turtle turtle) {
        for(LineWeight line : orderedLines) {
            boolean first=true;
            for(LineSegmentWeight w : line.segments) {
                if (first) {
                    turtle.jumpTo(w.start.x, w.start.y);
                    first = false;
                }
                turtle.moveTo(w.end.x, w.end.y);
            }
        }
    }

    private void scaleImage(int mode) {
        double width  = myPaper.getMarginWidth();
        double height = myPaper.getMarginHeight();

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

    /**
     * Greedy sort lines by travel distance.
     */
    void sortLinesByTravel() {
        logger.debug("sortLinesByTravel");

        LineWeight activeLine = sortedLines.remove(0);
        orderedLines.add(activeLine);

        while(!sortedLines.isEmpty()) {
            LineWeight best = sortedLines.get(0);
            double [] bestScorePair = scoreLine(activeLine,best);
            boolean bestEnd = true;

            double bestScore = Math.min( bestScorePair[0], bestScorePair[1] );

            for( LineWeight b : sortedLines ) {
                double [] scorePair = scoreLine(activeLine,b);
                double newScore = Math.min( scorePair[0], scorePair[1] );
                if(bestScore > newScore) {
                    best = b;
                    bestScore = newScore;
                    bestEnd = scorePair[0] < scorePair[1];
                }
            }
            sortedLines.remove(best);
            if(!bestEnd) best.flip();
            orderedLines.add(best);
            activeLine = best;
        }

        logger.debug("orderedLines={}",orderedLines.size());
    }

    /**
     * @param a the first line
     * @param b the second line
     * @return min( dist(a.tail,b.head), dist(a.tail,b.tail) )
     */
    private double [] scoreLine(LineWeight a, LineWeight b) {
        LineSegmentWeight aTail = a.segments.get(a.segments.size()-1);
        LineSegmentWeight bHead = b.segments.get(0);
        LineSegmentWeight bTail = b.segments.get(b.segments.size()-1);

        double head = distSq(aTail.start.x,aTail.start.y,bHead.start.x,bHead.start.y);
        double tail = distSq(aTail.start.x,aTail.start.y,bTail.end.x,bTail.end.y);
        return new double[]{ head, tail };
    }

    private void flipAndSmooth() {
        logger.debug("flipAndSmooth");
        // all segments are now in lines.
        for( LineWeight n : sortedLines ) {
            // Check the tail of one segment meets the head of the next.
            flipSegments(n);
            // Some segments are shorter than others.  Smooth them out.
            smoothSegments(n);
        }
    }

    private void flipSegments(LineWeight line) {
        if(line.segments.size()==1) return;

        // flip segmentsn so that they are all head-to-tail.
        LineSegmentWeight tail = line.segments.get(0);
        for(int i=1;i<line.segments.size();++i) {
            LineSegmentWeight next = line.segments.get(i);
            // check if s is flipped
            if( distSq(tail.end.x,tail.end.y,next.start.x,next.start.y) > distSq(tail.end.x,tail.end.y,next.end.x,next.end.y) ) {
                // tail to next is end-to-end.  flip it.
                next.flip();
            }
            tail = next;
        }
        // lastly check that the head is the right way around.
        LineSegmentWeight head = line.segments.get(0);
        LineSegmentWeight headPlus1 = line.segments.get(1);
        if( distSq(head.end.x,head.end.y,headPlus1.start.x,headPlus1.start.y) > distSq(head.start.x,head.start.y,headPlus1.start.x,headPlus1.start.y) ) {
            // it is.  fix it.
            head.flip();
        }
    }

    double distSq(double x0,double y0,double x1,double y1) {
        double dx=x1-x0;
        double dy=y1-y0;
        return dx*dx+dy*dy;
    }

    /**
     * Lines can turn 90 corners.  smooth all segments between two corners.
     * @param line the line to smooth
     */
    private void smoothSegments(LineWeight line) {
        LineWeight temp = new LineWeight();

        // Find the next corner
        LineSegmentWeight head = line.segments.get(0);
        Vector2d n0 = head.getDelta();
        int headIndex=0;

        for(int i=1;i<line.segments.size();++i) {
            LineSegmentWeight s = line.segments.get(i);
            Vector2d n1 = s.getDelta();
            if(n0.dot(n1) < 0.1) {
                // The deltas are too different.  This segment has turned.
                smoothSection(temp,head,line.segments.get(i-1));

                head = s;
                headIndex=i;
                n0=n1;
            }
        }
        if(headIndex<line.segments.size()) {
            // There's a section after a corner.  Maybe we never hit a corner!
            // Either way, make sure that last straight part is processed.
            smoothSection(temp,head,line.segments.get(line.segments.size()-1));
        }

        // temp.segments is now filled with smoothed lines
        line.segments = temp.segments;
    }

    void smoothSection(LineWeight temp, LineSegmentWeight head, LineSegmentWeight s) {
        double dx=s.end.x-head.start.x;
        double dy=s.end.y-head.start.y;
        double len = Math.sqrt(dx*dx + dy*dy);
        for(double j=0;j<len;j+=stepSize) {
            double vA = (j         ) / len;
            double vB = (j+stepSize) / len;
            vB = Math.min(vB,1);
            double ax=head.start.x + dx*vA;
            double ay=head.start.y + dy*vA;
            double bx=head.start.x + dx*vB;
            double by=head.start.y + dy*vB;
            temp.segments.add(createLSW(new Point2D(ax,ay),new Point2D(bx,by)));
        }
    }

    private void generateThickLines(Turtle turtle) {
        logger.debug("generateThickLines");
        for(LineWeight i : orderedLines) {
            if(i.segments.isEmpty()) continue;
            generateOneLine(turtle,i);
        }
    }

    private void generateOneLine(Turtle turtle, LineWeight line) {
        // find the thickest part of the line, which tells us how many cycles we'll have to make.
        double maxWeight=0;
        for(LineSegmentWeight s : line.segments) {
            maxWeight = Math.max(maxWeight,s.weight);
        }

        maxWeight = Math.max(1,Math.ceil(maxWeight));

        LineSegmentWeight start = line.segments.get(0);
        // travel the length of the line and back maxWeight times.  Each time offset by a different amount.
        List<Point2D> offsetSequence = new ArrayList<>();

        boolean first=true;
        // collect all the points, write them at the end.
        for(int pass=0; pass<=maxWeight; ++pass) {
            double ratio = pass/maxWeight;

            // add first point at start of line
            double [] s0 = getOffsetLine(
                    start.start.x,start.start.y,
                    start.end.x,start.end.y,
                    adjustedOffset(start.weight,ratio));

            offsetSequence.add(new Point2D(s0[0],s0[1]));

            // add the middle point of the line
            for(int i=1;i<line.segments.size();++i) {
                LineSegmentWeight seg = line.segments.get(i);
                double [] s1 = getOffsetLine(
                        seg.start.x,seg.start.y,
                        seg.end.x,seg.end.y,
                        adjustedOffset(seg.weight,ratio));
                double [] inter = findIntersection(
                        s0[0],s0[1],s0[2],s0[3],
                        s1[0],s1[1],s1[2],s1[3]
                );
                offsetSequence.add(new Point2D(inter[0],inter[1]));
                s0=s1;
            }
            // add the last point of the line
            offsetSequence.add(new Point2D(s0[2],s0[3]));

            if((pass%2)==1) {
                Collections.reverse(offsetSequence);
            }

            // optimize
            List<Point2D> newSequence = optimizeLine(offsetSequence);
            for( Point2D p : newSequence ) {
                if(first) {
                    turtle.jumpTo(p.x,p.y);
                    first=false;
                }
                turtle.moveTo(p.x,p.y);
            }
            offsetSequence.clear();
        }
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

    private List<Point2D> optimizeLine(List<Point2D> original) {
        logger.debug("before={}",original.size());
        List<Point2D> improved = new ArrayList<>();
        int s = original.size();
        if(s<3) {
            improved.addAll(original);
            logger.debug("short");
            return improved;
        }

        Point2D p0 = original.get(0);
        Point2D p1 = original.get(1);
        Point2D p2 = null;
        improved.add(p0);

        for(int i=2;i<s;++i) {
            p2=original.get(i);
            // get unit vector a = p1 - p0
            double ax = p1.x-p0.x;
            double ay = p1.y-p0.y;
            double d01 = Math.sqrt(ax*ax + ay*ay);
            ax/=d01;
            ay/=d01;

            // get unit vector b = p1 - p0
            double bx = p2.x-p0.x;
            double by = p2.y-p0.y;
            double d02 = Math.sqrt(bx*bx + by*by);
            bx/=d02;
            by/=d02;

            // if a==b
            if( Math.abs(ax-bx)<EPSILON && Math.abs(ay-by)<EPSILON) {
                // do nothing
            } else {
                improved.add(p1);
            }
            p0=p1;
            p1=p2;
        }
        improved.add(p2);

        logger.debug("after={}",improved.size());
        return improved;
    }

    double[] getOffsetLine(double x0,double y0,double x1,double y1,double distance) {
        // get normal of each line, then scale by distance.
        double nx = y1-y0;
        double ny = x0-x1;
        double nd = Math.sqrt(nx*nx + ny*ny);
        if(nd==0) nd=1;
        nx *= distance / nd;
        ny *= distance / nd;

        // offset from the original line
        return new double[] {
            x0+nx, y0+ny,
            x1+nx, y1+ny
        };
    }

    private void sortSegmentsIntoLines() {
        logger.debug("sortSegmentsIntoLines");

        while(!unsorted.isEmpty()) {
            LineWeight activeLine = new LineWeight();
            activeLine.segments.add(unsorted.remove(0));
            sortedLines.add(activeLine);

            LineSegmentWeight head = activeLine.segments.get(0);
            LineSegmentWeight tail = head;

            boolean found;
            do {
                found=false;
                for (LineSegmentWeight s : unsorted) {
                    if (closeEnough(head, s)) {  // try to match with head of line
                        activeLine.segments.add(0, s);
                        head = s;
                        unsorted.remove(s);
                        found = true;
                        break;
                    } else if (closeEnough(tail, s)) {  // try to match with tail of line
                        activeLine.segments.add(s);
                        tail = s;
                        unsorted.remove(s);
                        found = true;
                        break;
                    }
                }
            } while(found);
        }

        logger.debug("sortedLines="+sortedLines.size());
    }

    /**
     * @param a the first line
     * @param b the second line
     * @return true if {@link LineSegmentWeight} a and b touch end to end.
     */
    boolean closeEnough(LineSegmentWeight a, LineSegmentWeight b) {
        // fast reject if truchet index too far apart
        if(Math.abs(a.ix-b.ix)>1 ||
           Math.abs(a.iy-b.iy)>1) return false;

        if(closeEnough(a.start,b.start)) return true;
        if(closeEnough(a.start,b.end)) return true;
        if(closeEnough(a.end,b.start)) return true;
        if(closeEnough(a.end,b.end)) return true;

        return false;
    }

    boolean closeEnough(Point2D p0,Point2D p1) {
        if(Math.abs(p0.x-p1.x)> EPSILON) return false;
        if(Math.abs(p0.y-p1.y)> EPSILON) return false;
        return true;
    }

    private void buildSegmentList(Turtle from) {
        logger.debug("buildSegmentList");

        LineCollection originalLines = from.getAsLineSegments();
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

        for(int i=0;i<pieces;++i) {
            double t0 = (double)(i  ) / (double)pieces;
            double t1 = (double)(i+1) / (double)pieces;

            addOneUnsortedSegment(
                    new Point2D(
                            segment.start.x + diff.x * t0,
                            segment.start.y + diff.y * t0),
                    new Point2D(
                            segment.start.x + diff.x * t1,
                            segment.start.y + diff.y * t1));
        }
    }

    private void addOneUnsortedSegment(Point2D start, Point2D end) {
        unsorted.add(createLSW(start,end));
    }

    private LineSegmentWeight createLSW(Point2D start,Point2D end) {
        // sample image intensity here from 0...1
        double mx = (start.x+end.x)/2.0;
        double my = (start.y+end.y)/2.0;

        double intensity = 1.0-(sourceImage.sample(mx,my,stepSize/2)/255.0);
        LineSegmentWeight a = new LineSegmentWeight(start,end,intensity*thickness);
        // make a fast search index
        a.ix = (int)Math.floor(mx * 6.0 / stepSize);
        a.iy = (int)Math.floor(my * 6.0 / stepSize);
        return a;
    }
}

