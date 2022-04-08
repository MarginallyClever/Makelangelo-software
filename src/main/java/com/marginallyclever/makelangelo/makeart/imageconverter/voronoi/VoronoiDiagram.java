package com.marginallyclever.makelangelo.makeart.imageconverter.voronoi;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * In mathematics, a Voronoi diagram is a partition of a plane into regions close to each of a given set of objects.
 * In the simplest case, these objects are just finitely many points in the plane (called seeds, sites, or generators).
 * For each seed there is a corresponding region, called a Voronoi cell, consisting of all points of the plane closer
 * to that seed than to any other.
 *
 * {@link VoronoiDiagram} is a class that implements the algorithm for computing and working with Voronoi diagrams.
 */
public class VoronoiDiagram {
    private static final Logger logger = LoggerFactory.getLogger(VoronoiDiagram.class);

    private final List<Point2D> points = new ArrayList<>();
    private final List<VoronoiCell> cells = new ArrayList<>();
    private final List<VoronoiGraphEdge> graphEdges = new ArrayList<>();
    private final VoronoiTesselator voronoiTesselator = new VoronoiTesselator();
    private Rectangle2D bounds;

    private double[] xValuesIn = null;
    private double[] yValuesIn = null;

    public VoronoiDiagram() {
        super();
    }

    // set some starting points in a grid
    public void initializeCells(int numCells, Rectangle2D bounds, double minDistanceBetweenSites) {
        logger.debug("Initializing cells");

        this.bounds = bounds;

        // convert the cells to sites used in the Voronoi class.
        xValuesIn = new double[numCells];
        yValuesIn = new double[numCells];

        // from top to bottom of the margin area...
        cells.clear();
        int used;
        for (used=0;used<numCells;++used) {
            VoronoiCell c = new VoronoiCell();

            double x=0,y=0;
            for(int i=0;i<30;++i) {
                x = bounds.getX() + Math.random() * bounds.getWidth();
                y = bounds.getY() + Math.random() * bounds.getHeight();
            }
            c.centroid.set(x,y);
            cells.add(c);
        }

        voronoiTesselator.Init(minDistanceBetweenSites);
    }

    public void renderPoints(GL2 gl2) {
        gl2.glColor3f(0, 0, 0);
        gl2.glBegin(GL2.GL_POINTS);
        for( VoronoiCell c : cells ) {
            Point2D p = c.centroid;
            gl2.glVertex2d(p.x,p.y);
        }
        gl2.glEnd();
    }

    public void renderEdges(GL2 gl2) {
        gl2.glColor3f(0.9f, 0.9f, 0.9f);
        gl2.glBegin(GL2.GL_LINES);
        for(VoronoiGraphEdge edge : graphEdges) {
            gl2.glVertex2d(edge.x1, edge.y1);
            gl2.glVertex2d(edge.x2, edge.y2);
        }
        gl2.glEnd();
    }

    public List<VoronoiCell> getCells() {
        return cells;
    }

    public List<VoronoiGraphEdge> getGraphEdges() {
        return graphEdges;
    }
    /**
     *  I have a set of points.  I want a list of cell borders.
     *  cell borders are halfway between any point and it's nearest neighbors.
     */
    public void tessellate() {
        int i=0;
        for( VoronoiCell c : cells ) {
            xValuesIn[i] = c.centroid.x;
            yValuesIn[i] = c.centroid.y;
            c.clear();
            ++i;
        }

        // scan left to right across the image, building the list of borders as we go.
        graphEdges.clear();
        graphEdges.addAll(voronoiTesselator.generateVoronoi(xValuesIn, yValuesIn, bounds.getMinX(), bounds.getMaxX(), bounds.getMinY(), bounds.getMaxY()));

        for( VoronoiGraphEdge edge : graphEdges ) {
            VoronoiCell a = cells.get(edge.site1);
            a.addPoint(edge.x1, edge.y1);
            a.addPoint(edge.x2, edge.y2);
            VoronoiCell b = cells.get(edge.site1);
            b.addPoint(edge.x1, edge.y1);
            b.addPoint(edge.x2, edge.y2);
        }
    }

    /**
     * Use the weight of the image pixels to adjust the centroid of each cell.
     * @param image
     * @return the total distance moved.
     */
    public double adjustCentroids(TransformedImage image) {
        updateCellWeights(image);

        double totalMagnitude=0;

        double w = 0.25;//Math.pow(iterations,-0.8);

        for (VoronoiCell c : cells) {
            c.scaleByWeight();

            double ox = c.centroid.x;
            double oy = c.centroid.y;
            double dx2 = (c.wx - ox) * 0.25;
            double dy2 = (c.wy - oy) * 0.25;

            totalMagnitude += Math.abs(dx2) + Math.abs(dy2);

            double nx = ox + dx2;// + (Math.random()-0.5) * w;
            double ny = oy + dy2;// + (Math.random()-0.5) * w;

            // make sure centroid can't leave image bounds
            if(nx <  bounds.getMinX()) nx = bounds.getMinX();
            if(nx >= bounds.getMaxX()) nx = bounds.getMaxX()-1;
            if(ny <  bounds.getMinY()) ny = bounds.getMinY();
            if(ny >= bounds.getMaxY()) ny = bounds.getMaxY()-1;

            c.centroid.set(nx, ny);
        }
        return totalMagnitude;
    }

    private void updateCellWeights(TransformedImage image) {
        for (VoronoiCell c : cells) {
            Rectangle2D bounds = c.getBounds();
            for (double y = bounds.getMinY(); y <= bounds.getMaxY(); y++) {
                for (double x = bounds.getMinX(); x <= bounds.getMaxX(); x++) {
                    if (c.contains(x, y) && image.canSampleAt(x, y)) {
                        double weight = 255.0 - image.sample1x1Unchecked( x,y );
                        c.addWeight(x, y,weight);
                    }
                }
            }
        }
    }
}
