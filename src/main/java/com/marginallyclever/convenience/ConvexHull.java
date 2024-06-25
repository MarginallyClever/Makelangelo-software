package com.marginallyclever.convenience;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Vector2d;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Convex hull algorithm.
 * @author Dan Royer
 * @since 2022-04-06
 */
public class ConvexHull {
    private static final Logger logger = LoggerFactory.getLogger(ConvexHull.class);
    private final ArrayList<Vector2d> points = new ArrayList<>();

    public ConvexHull() {}

    public ConvexHull(ArrayList<Vector2d> points) {
        set(points);
    }

    public void add(Vector2d p) {
        int s = points.size();
        if(s<2) points.add(p);
        else if(s<3) addThirdPointClockwise(p);
        else if(!contains(p)) {
            try {
                addPointCarefully(p);
            } catch(Exception e) {
                logger.error("addPointCarefully() failure.");
            }
        }
    }

    public void clear() {
        points.clear();
    }

    public Rectangle2D getBounds() {
        Rectangle2D bounds = new Rectangle2D.Double();
        if(!points.isEmpty()) {
            Vector2d p = points.get(0);
            bounds.setRect(p.x,p.y,0,0);
            for(Vector2d p2 : points) {
                bounds.add(p2.x,p2.y);
            }
        }
        return bounds;
    }

    public void set(List<Vector2d> points) {
        this.points.clear();
        this.points.addAll(points);
        rebuildHull();
    }

    /**
     * The hull can be described as a fan of triangles all sharing p0.
     * if p is inside any of the triangles then it is inside the fan.
     * @param p the point
     * @return true if inside the fan.
     */
    public boolean contains(Vector2d p) {
        if(points.size()<3) return false;

        Vector2d a= points.get(0);
        int s = points.size();
        for(int i=0;i<s;++i) {
            int j=(i+1)%s;
            Vector2d b= points.get(j);
            if(pointIsOnTheLeft(p, a, b)) return false;
            a=b;
        }
        return true;
    }

    private void addThirdPointClockwise(Vector2d c) {
        Vector2d a= points.get(0);
        Vector2d b= points.get(1);
        if(pointIsOnTheLeft(c,a,b)) points.add(1, c);	// new order is acb
        else points.add( c);	// new order is abc
    }

    private boolean pointIsOnTheLeft(Vector2d c,Vector2d a,Vector2d b) {
        Vector2d d=new Vector2d();
        Vector2d e=new Vector2d();

        d.sub(b,a);
        d=orthogonalXY(d);
        e.sub(c,a);

        return d.dot(e)>0;
    }

    /**
     * See <a href="https://en.wikipedia.org/wiki/Gift_wrapping_algorithm">Gift wrapping algorithm</a>
     * @param p the point to add
     */
    private void addPointCarefully(Vector2d p) {
        points.add(p);
        rebuildHull();
    }

    private void rebuildHull() {
        ArrayList<Vector2d> toKeep = new ArrayList<>();
        int hullSize = points.size();
        if(hullSize<=3) return;

        Vector2d pointOnHull = getPointGuaranteedOnEdgeOfHull();
        Vector2d firstPoint = pointOnHull;
        Vector2d endPoint;
        do {
            toKeep.add(pointOnHull);
            endPoint = points.get(0);
            for( Vector2d b : points) {
                if(endPoint == pointOnHull || pointIsOnTheLeft(b, pointOnHull, endPoint)) {
                    endPoint = b;
                }
            }
            pointOnHull = endPoint;
            hullSize--;
        } while(endPoint!=firstPoint && hullSize>=0);

        if(toKeep.size()<3) {
            throw new IndexOutOfBoundsException("Algorithm failed.");
        }

        points.clear();
        points.addAll(toKeep);
    }

    private Vector2d getPointGuaranteedOnEdgeOfHull() {
        // first is left-most point in the set.
        Vector2d pointOnHull = points.get(0);
        for( Vector2d n : points) {
            if(pointOnHull.x>n.x) pointOnHull=n;
            else if(pointOnHull.x==n.x) {
                // two matching x, find the smallest y
                if(pointOnHull.y>n.y) pointOnHull=n;
            }
        }

        return pointOnHull;
    }

    @SuppressWarnings("unused")
    public Vector2d getCenterOfHull() {
        Vector2d center = new Vector2d();

        int s = points.size();
        for (Vector2d point : points) center.add(point);
        center.scale(1.0/(double)s);

        return center;
    }

    // Is point p inside triangle abc?  Works with clockwise and counter-clockwise triangles.
    @SuppressWarnings("unused")
    private boolean pointIsInTriangleXY(Vector2d p, Vector2d a, Vector2d b, Vector2d c) {
        boolean r0 = pointIsOnTheLeft(p, a, b);
        boolean r1 = pointIsOnTheLeft(p, b, c);
        if(r0!=r1) return false;
        boolean r2 = pointIsOnTheLeft(p, c, a);
        return (r0==r2);
    }

    private Vector2d orthogonalXY(Vector2d d) {
        return new Vector2d(d.y,-d.x);
    }

    @Override
    public String toString() {
        return "ConvexHull{" +
                "hull=" + points +
                '}';
    }

    public List<Vector2d> getPoints() {
        return points;
    }
}
