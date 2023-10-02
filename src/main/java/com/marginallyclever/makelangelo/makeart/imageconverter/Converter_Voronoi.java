package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.voronoi.VoronoiCell;
import com.marginallyclever.convenience.voronoi.VoronoiTesselator2;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.FilterDesaturate;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.select.SelectBoolean;
import com.marginallyclever.makelangelo.select.SelectInteger;
import com.marginallyclever.makelangelo.select.SelectSlider;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.prep.PreparedPolygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared methods for Voronoi converters
 * @author Dan Royer
 * @since 7.39.9
 */
public abstract class Converter_Voronoi extends ImageConverterIterative {
    private static final Logger logger = LoggerFactory.getLogger(Converter_Voronoi.class);
    private static int numCells = 9000;
    private static boolean drawVoronoi = false;

    private final VoronoiTesselator2 voronoiDiagram = new VoronoiTesselator2();
    protected final List<VoronoiCell> cells = new ArrayList<>();
    private int iterations;
    private int lowpassCutoff = 128;
    private int cellBuffer = 100;


    public Converter_Voronoi() {
        super();

        SelectInteger selectCells = new SelectInteger("cells",Translator.get("Converter_VoronoiStippling.CellCount"),getNumCells());
        add(selectCells);
        selectCells.addPropertyChangeListener(evt->{
            setNumCells((int)evt.getNewValue());
            fireRestart();
        });

        SelectBoolean selectDrawVoronoi = new SelectBoolean("drawVoronoi", Translator.get("Converter_VoronoiStippling.DrawBorders"), getDrawVoronoi());
        add(selectDrawVoronoi);
        selectDrawVoronoi.addPropertyChangeListener(evt -> setDrawVoronoi((boolean) evt.getNewValue()));

        SelectSlider selectCutoff = new SelectSlider("cutoff", Translator.get("Converter_VoronoiStippling.Cutoff"),255,0,getLowpassCutoff());
        add(selectCutoff);
        selectCutoff.addPropertyChangeListener(evt-> setLowpassCutoff((int)evt.getNewValue()));
    }

    @Override
    public void start(Paper paper, TransformedImage image) {
        // make black & white
        FilterDesaturate bw = new FilterDesaturate(image);
        super.start(paper, bw.filter());

        lock.lock();
        try {
            turtle = new Turtle();

            iterations=0;

            Rectangle2D bounds = myPaper.getMarginRectangle();
            cells.clear();
            int i=0;
            while(i<numCells) {
                double x = Math.random()*bounds.getWidth()+bounds.getMinX();
                double y = Math.random()*bounds.getHeight()+bounds.getMinY();
                if(image.canSampleAt(x,y)) {
                    if(image.sample1x1Unchecked(x,y) < Math.random()*255) {
                        cells.add( new VoronoiCell(x,y) );
                        i++;
                    }
                }
            }
            voronoiDiagram.setNumHulls(numCells);
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public boolean iterate() {
        iterations++;
        lock.lock();
        try {
            double noiseLevel = evolveCells();
            System.out.println(iterations+": "+noiseLevel+" "+(noiseLevel/(float)numCells));
        }
        finally {
            lock.unlock();
        }
        return true;
    }

    /**
     * Jiggle the dots until they make a nice picture
     */
    private double evolveCells() {
        double change=10000;

        try {
            voronoiDiagram.tessellate(cells,myPaper.getMarginRectangle(),1e-2);
            change = adjustCenters(myImage);
        }
        catch (Exception e) {
            logger.error("Failed to evolve", e);
        }

        return change;
    }

    private double adjustCenters(TransformedImage image) {
        double change=0;
        GeometryFactory factory = new GeometryFactory();

        for(int i=0;i<voronoiDiagram.getNumHulls();++i) {
            Polygon poly = voronoiDiagram.getHull(i);
            PreparedPolygon hull = new PreparedPolygon(poly);
            VoronoiCell cell = cells.get(i);
            cell.weight=0;
            cell.change=0;

            // sample every image coordinate inside the voronoi cell and find the weighted center
            double wx=0,wy=0;
            double weight=0;
            int hits=0;

            Point centroid = poly.getCentroid();
            cell.set(centroid.getX(),centroid.getY());

            Envelope e = poly.getEnvelopeInternal();
            double miny = Math.floor(e.getMinY());
            double maxy = Math.ceil(e.getMaxY());
            double minx = Math.floor(e.getMinX());
            double maxx = Math.ceil(e.getMaxX());

            double xDiff = maxx-minx;
            double stepSize = getStepSize(maxy, miny, xDiff);

            for(double y=miny;y<maxy;y+=stepSize) {
                double x0 = findLeftEdge(hull,factory,y,minx,maxx,stepSize);
                double x1 = findRightEdge(hull,factory,y,minx,maxx,stepSize);
                for (double x = x0; x <= x1; x+=stepSize) {
                    if(!image.canSampleAt(x,y)) continue;
                    double v = 255 - image.sample1x1Unchecked(x,y);
                    weight += v;
                    wx += v * x;
                    wy += v * y;
                    hits++;
                }
            }
            if(weight>0) {
                cell.weight = weight / hits;
                wx /= weight;
                wy /= weight;
                double dx = wx - cell.center.x;
                double dy = wy - cell.center.y;
                cell.change = (dx*dx+dy*dy);
                change += cell.change;
                cell.set(wx,wy);
            }
        }
        return change;
    }

    private double getStepSize(double maxy, double miny, double xDiff) {
        double yDiff = maxy - miny;
        double maxSize = Math.max(xDiff, yDiff);
        double minSize = Math.min(xDiff, yDiff);

        double scaleFactor = 1;
        // Maximum voronoi cell extent should be between
        // cellBuffer/2 and cellBuffer in size.
        while (maxSize > cellBuffer) {
            scaleFactor *= 0.5;
            maxSize *= 0.5;
        }

        while (maxSize < (cellBuffer / 2.0)) {
            scaleFactor *= 2;
            maxSize *= 2;
        }

        if ((minSize * scaleFactor) > (cellBuffer/2.0)) {
            // Special correction for objects of near-unity (square-like) aspect ratio,
            // which have larger area *and* where it is less essential to find the exact centroid:
            scaleFactor *= 0.5;
        }

        double stepSize = 1.0/scaleFactor;
        return stepSize;
    }

    private double findLeftEdge(PreparedPolygon poly,GeometryFactory factory,double y,double minx,double maxx,double stepSize) {
        Coordinate c = new Coordinate(minx,y);
        for(double x = minx; x < maxx; x+=stepSize) {
            c.x=x;
            if(poly.intersects(factory.createPoint(c))) return x;
        }
        return maxx;
    }

    private double findRightEdge(PreparedPolygon poly,GeometryFactory factory, double y,double minx,double maxx,double stepSize) {
        Coordinate c = new Coordinate(maxx,y);
        for(double x = maxx; x > minx; x-=stepSize) {
            c.x=x;
            if(poly.intersects(factory.createPoint(c))) return x;
        }
        return minx;
    }

    @Override
    public void stop() {
        super.stop();
        lock.lock();
        try {
            writeOutCells();
        }
        finally {
            lock.unlock();
        }
        fireConversionFinished();
    }

    protected void renderEdges(GL2 gl2) {
        gl2.glColor3d(0.9, 0.9, 0.9);

        for(int i=0;i<voronoiDiagram.getNumHulls();++i) {
            Polygon poly = voronoiDiagram.getHull(i);
            gl2.glBegin(GL2.GL_LINE_LOOP);
            for (Coordinate p : poly.getExteriorRing().getCoordinates()) {
                gl2.glVertex2d(p.x, p.y);
            }
            gl2.glEnd();
        }
    }

    public void setNumCells(int value) {
        numCells = Math.max(1,value);
    }
    public int getNumCells() {
        return numCells;
    }

    public void setDrawVoronoi(boolean arg0) {
        drawVoronoi = arg0;
    }
    public boolean getDrawVoronoi() {
        return drawVoronoi;
    }

    @Override
    public void generateOutput() {
        writeOutCells();

        fireConversionFinished();
    }

    @Override
    public void resume() {

    }

    abstract void writeOutCells();

    /**
     * Callback from {@link com.marginallyclever.makelangelo.preview.PreviewPanel} that it is time to render to the WYSIWYG display.
     *
     * @param gl2 the render context
     */
    @Override
    public void render(GL2 gl2) {
        ImageConverterThread thread = getThread();
        if(thread==null || thread.getPaused()) return;

        if (!drawVoronoi) return;

        lock.lock();
        try {
            renderEdges(gl2);
        }
        finally {
            lock.unlock();
        }
    }

    public int getLowpassCutoff() {
        return lowpassCutoff;
    }

    public void setLowpassCutoff(int lowpassCutoff) {
        this.lowpassCutoff = lowpassCutoff;
    }
}
