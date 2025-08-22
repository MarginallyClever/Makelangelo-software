package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.voronoi.VoronoiCell;
import com.marginallyclever.convenience.voronoi.VoronoiTesselator2;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.FilterDesaturate;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.donatello.select.SelectBoolean;
import com.marginallyclever.donatello.select.SelectInteger;
import com.marginallyclever.donatello.select.SelectRandomSeed;
import com.marginallyclever.donatello.select.SelectSlider;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.preview.OpenGLPanel;
import com.marginallyclever.makelangelo.preview.ShaderProgram;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.prep.PreparedPolygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.IntStream;

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
    private static int seed=0;
    private static final Random random = new Random();


    public Converter_Voronoi() {
        super();

        SelectRandomSeed selectRandomSeed = new SelectRandomSeed("randomSeed",Translator.get("Generator.randomSeed"),seed);
        add(selectRandomSeed);
        selectRandomSeed.addSelectListener(evt->{
            seed = (int)evt.getNewValue();
            random.setSeed(seed);
            fireRestart();
        });
        
        SelectInteger selectCells = new SelectInteger("cells",Translator.get("Converter_VoronoiStippling.CellCount"),getNumCells());
        add(selectCells);
        selectCells.addSelectListener(evt->{
            setNumCells((int)evt.getNewValue());
            fireRestart();
        });

        SelectBoolean selectDrawVoronoi = new SelectBoolean("drawVoronoi", Translator.get("Converter_VoronoiStippling.DrawBorders"), getDrawVoronoi());
        add(selectDrawVoronoi);
        selectDrawVoronoi.addSelectListener(evt -> setDrawVoronoi((boolean) evt.getNewValue()));

        SelectSlider selectCutoff = new SelectSlider("cutoff", Translator.get("Converter_VoronoiStippling.Cutoff"),255,0,getLowpassCutoff());
        add(selectCutoff);
        selectCutoff.addSelectListener(evt-> setLowpassCutoff((int)evt.getNewValue()));
    }

    @Override
    public void start(Paper paper, TransformedImage image) {
        // make black & white
        FilterDesaturate bw = new FilterDesaturate(image);
        super.start(paper, bw.filter());

        lock.lock();
        try {
            turtle = new Turtle();
            turtle.setStroke(Color.BLACK,settings.getDouble(PlotterSettings.DIAMETER));

            iterations=0;

            Rectangle2D bounds = paper.getMarginRectangle();

            cells.clear();
            int i=0;
            do {
                double x = random.nextDouble() * bounds.getWidth()+bounds.getMinX();
                double y = random.nextDouble() * bounds.getHeight()+bounds.getMinY();
                if(image.canSampleAt(x,y)) {
                    if(image.sample1x1Unchecked(x,y) < random.nextDouble()*255) {
                        cells.add( new VoronoiCell(x,y) );
                        i++;
                    }
                }
            } while(i<numCells);
            voronoiDiagram.setNumHulls(numCells);
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public boolean iterate() {
        turtle.getLayers().clear();

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
        GeometryFactory factory = new GeometryFactory();

        DoubleAdder change = new DoubleAdder();

        IntStream.range(0,voronoiDiagram.getNumHulls()).parallel().forEach(i -> {
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
                change.add(cell.change);
                cell.set(wx,wy);
            }
        });

        return change.sum();
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
        writeOutCells();
    }

    protected void renderEdges(GL3 gl) {
/*
        gl.glColor3d(0.9, 0.9, 0.9);

        double cx = myPaper.getCenterX();
        double cy = myPaper.getCenterY();
        gl.glPushMatrix();
        gl.glTranslated(cx, cy, 0);

        for(int i=0;i<voronoiDiagram.getNumHulls();++i) {
            Polygon poly = voronoiDiagram.getHull(i);
            gl.glBegin(GL3.GL_LINE_LOOP);
            for (Coordinate p : poly.getExteriorRing().getCoordinates()) {
                gl.glVertex2d(p.x, p.y);
            }
            gl.glEnd();
        }
        gl.glPopMatrix();
        */
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
        lock.lock();
        try {
            writeOutCells();
        }
        finally {
            lock.unlock();
        }
        fireConversionFinished();
    }

    @Override
    public void resume() {

    }

    abstract void writeOutCells();

    /**
     * Callback from {@link OpenGLPanel} that it is time to render to the WYSIWYG display.
     *
     * @param shader the render context
     */
    @Override
    public void render(ShaderProgram shader, GL3 gl) {/*
        ImageConverterThread thread = getThread();
        if(thread==null || thread.getPaused()) return;

        if (!drawVoronoi) return;

        lock.lock();
        try {
            renderEdges(gl);
        }
        finally {
            lock.unlock();
        }*/
    }

    public int getLowpassCutoff() {
        return lowpassCutoff;
    }

    public void setLowpassCutoff(int lowpassCutoff) {
        this.lowpassCutoff = lowpassCutoff;
    }
}
