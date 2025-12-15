package com.marginallyclever.convenience;

import com.jogamp.opengl.GL3;

import javax.vecmath.Point2d;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * A quadtree is a data structure that can be used to quickly find all the objects that are within a given 2D area.
 */
public class QuadGraph {
    private static final int MAX_POINTS = 5;
    public final Rectangle2D bounds = new Rectangle2D.Double();
    public final List<Point2d> sites = new ArrayList<>();
    public QuadGraph[] children = null;

    /**
     * Setup a new quadtree node.
     * @param x lower left corner
     * @param y lower left corner
     * @param x2 upper right corner
     * @param y2 upper right corner
     */
    public QuadGraph(double x, double y, double x2, double y2) {
        bounds.setRect(x, y, x2-x, y2-y);
    }

    public void split() {
        if(children != null) return;
        double x1 = bounds.getMinX();
        double y1 = bounds.getMinY();
        double x2 = bounds.getMaxX();
        double y2 = bounds.getMaxY();
        double cx = (x1+x2)/2;
        double cy = (y1+y2)/2;
        children = new QuadGraph[4];
        children[0] = new QuadGraph(x1,y1,cx,cy);
        children[1] = new QuadGraph(x1,cy,cx,y2);
        children[2] = new QuadGraph(cx,cy,x2,y2);
        children[3] = new QuadGraph(cx,y1,x2,cy);
        moveSitesIntoChildren();
    }

    private void moveSitesIntoChildren() {
        // put all sites into the new children
        for(Point2d c : sites) {
            addCellToOneQuadrant(c);
        }
        sites.clear();
    }

    public boolean insert(Point2d e) {
        if(bounds.contains(e.x,e.y)) {
            if(sites.size()<MAX_POINTS && children == null) {
                sites.add(e);
                return true;
            } else {
                split();
                return addCellToOneQuadrant(e);
            }
        }
        return false;
    }

    private boolean addCellToOneQuadrant(Point2d e) {
        for(int i=0;i<4;++i) {
            if(children[i].insert(e)) return true;
        }
        return false;
    }

    // locate the cell under point x,y
    public Point2d search(Point2d p) {
        if(!bounds.contains(p.x,p.y)) return null;

        Point2d bestFound = null;
        double bestD = Double.MAX_VALUE;

        if (!sites.isEmpty()) {
            // search me
            for(Point2d c : sites) {
                double d = p.distanceSquared(c);
                if(bestD > d) {
                    bestD = d;
                    bestFound = c;
                }
            }
        }

        if(children != null) {
            for (int i = 0; i < 4; ++i) {
                // look into the children
                Point2d bestChildFound = children[i].search(p);
                if (bestChildFound != null && bestFound == null) {
                    double d = p.distanceSquared(bestChildFound);
                    if(bestD > d) {
                        bestFound = bestChildFound;
                    }
                }
            }
        }

        return bestFound;
    }

    public void render(GL3 gl2) {
        if (children != null) {
            for (int i = 0; i < 4; ++i) {
                children[i].render(gl2);
            }
        } else {/*
            gl2.glColor3f(1, 0, 1);
            gl2.glBegin(GL3.GL_LINE_LOOP);
            gl2.glVertex2d(bounds.getMinX(), bounds.getMinY());
            gl2.glVertex2d(bounds.getMinX(), bounds.getMaxY());
            gl2.glVertex2d(bounds.getMaxX(), bounds.getMaxY());
            gl2.glVertex2d(bounds.getMaxX(), bounds.getMinY());
            gl2.glEnd();*/
        }
    }

    public int countPoints() {
        int sum = sites.size();
        if(children != null) {
            for (int i = 0; i < 4; ++i) {
                sum += children[i].countPoints();
            }
        }
        return sum;
    }
}
