package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.voronoi.VoronoiCell;
import com.marginallyclever.convenience.voronoi.VoronoiTesselator2;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.Filter_Greyscale;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.preview.PreviewListener;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.prep.PreparedPolygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Shared methods for Voronoi converters
 * @author Dan Royer
 * @since 7.39.9
 */
public abstract class Converter_Voronoi extends ImageConverterIterative implements PreviewListener {
    private static final Logger logger = LoggerFactory.getLogger(Converter_Voronoi.class);
    private static int numCells = 9000;
    private static boolean drawVoronoi = false;

    private final VoronoiTesselator2 voronoiDiagram = new VoronoiTesselator2();
    protected final List<VoronoiCell> cells = new ArrayList<>();

    protected final Lock lock = new ReentrantLock();

    private int iterations;


    @Override
    public void start(Paper paper, TransformedImage image) {
        // make black & white
        Filter_Greyscale bw = new Filter_Greyscale(255);
        super.start(paper, bw.filter(image));

        lock.lock();
        try {
            turtle = new Turtle();

            iterations=0;

            Rectangle2D bounds = myPaper.getMarginRectangle();
            cells.clear();
            for(int i=0;i<numCells;++i) {
                cells.add(new VoronoiCell(
                        Math.random()*bounds.getWidth()+bounds.getMinX(),
                        Math.random()*bounds.getHeight()+bounds.getMinY()));
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
            int miny = (int) Math.floor(e.getMinY());
            int maxy = (int) Math.ceil(e.getMaxY());
            int minx = (int) Math.floor(e.getMinX());
            int maxx = (int) Math.ceil(e.getMaxX());

            for(int y=miny;y<maxy;++y) {
                int x0 = findLeftEdge(hull,y,factory,minx,maxx);
                int x1 = findRightEdge(hull,y,factory,minx,maxx);
                for (int x = x0; x <= x1; ++x) {
                    if(!image.canSampleAt(x,y)) continue;

                    int v = 255 - image.sample1x1(x,y);
                    weight += v;
                    wx += v * x;
                    wy += v * y;
                    hits++;
                }
            }
            if(hits>0 && weight>0) {
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

    private int findLeftEdge(PreparedPolygon poly,int y,GeometryFactory factory,int minx,int maxx) {
        Coordinate c = new Coordinate(minx,y);
        for(int x = minx; x < maxx; ++x) {
            c.x=x;
            if(poly.intersects(factory.createPoint(c))) return x;
        }
        return maxx;
    }

    private int findRightEdge(PreparedPolygon poly, int y,GeometryFactory factory,int minx,int maxx) {
        Coordinate c = new Coordinate(maxx,y);
        for(int x = maxx; x > minx; --x) {
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
}
