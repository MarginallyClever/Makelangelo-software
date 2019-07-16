package com.marginallyclever.voronoi;

/*
 * The author of this software is Steven Fortune.  Copyright (c) 1994 by AT&T
 * Bell Laboratories.
 * Permission to use, copy, modify, and distribute this software for any
 * purpose without fee is hereby granted, provided that this entire notice
 * is included in all copies of any software which is or includes a copy
 * or modification of this software and in all copies of the supporting
 * documentation for such software.
 * THIS SOFTWARE IS BEING PROVIDED "AS IS", WITHOUT ANY EXPRESS OR IMPLIED
 * WARRANTY.  IN PARTICULAR, NEITHER THE AUTHORS NOR AT&T MAKE ANY
 * REPRESENTATION OR WARRANTY OF ANY KIND CONCERNING THE MERCHANTABILITY
 * OF THIS SOFTWARE OR ITS FITNESS FOR ANY PARTICULAR PURPOSE.
 */

/*
 * This code was originally written by Stephan Fortune in C code.  I, Shane O'Sullivan,
 * have since modified it, encapsulating it in a C++ class and, fixing memory leaks and
 * adding accessors to the Voronoi Edges.
 * Permission to use, copy, modify, and distribute this software for any
 * purpose without fee is hereby granted, provided that this entire notice
 * is included in all copies of any software which is or includes a copy
 * or modification of this software and in all copies of the supporting
 * documentation for such software.
 * THIS SOFTWARE IS BEING PROVIDED "AS IS", WITHOUT ANY EXPRESS OR IMPLIED
 * WARRANTY.  IN PARTICULAR, NEITHER THE AUTHORS NOR AT&T MAKE ANY
 * REPRESENTATION OR WARRANTY OF ANY KIND CONCERNING THE MERCHANTABILITY
 * OF THIS SOFTWARE OR ITS FITNESS FOR ANY PARTICULAR PURPOSE.
 */

/*
 * Java Version by Zhenyu Pan
 * Permission to use, copy, modify, and distribute this software for any
 * purpose without fee is hereby granted, provided that this entire notice
 * is included in all copies of any software which is or includes a copy
 * or modification of this software and in all copies of the supporting
 * documentation for such software.
 * THIS SOFTWARE IS BEING PROVIDED "AS IS", WITHOUT ANY EXPRESS OR IMPLIED
 * WARRANTY.  IN PARTICULAR, NEITHER THE AUTHORS NOR AT&T MAKE ANY
 * REPRESENTATION OR WARRANTY OF ANY KIND CONCERNING THE MERCHANTABILITY
 * OF THIS SOFTWARE OR ITS FITNESS FOR ANY PARTICULAR PURPOSE.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import java.awt.Point;


public class VoronoiTesselator {
  // ************* Private members ******************
  private double borderMinX, borderMaxX, borderMinY, borderMaxY;
  private int siteidx;
  private double xmin, xmax, ymin, ymax, deltax, deltay;
  private int nvertices;
  private int nedges;
  private int nsites;
  private VoronoiSite[] sites;
  private VoronoiSite bottomsite;
  private int sqrt_nsites;
  private double minDistanceBetweenSites;
  private int PQcount;
  private int PQmin;
  private int PQhashsize;
  private VoronoiHalfEdge[] PQhash;

  private final static int LE = 0;
  private final static int RE = 1;

  private int ELhashsize;
  private VoronoiHalfEdge ELhash[];
  private VoronoiHalfEdge ELleftend, ELrightend;
  private List<VoronoiGraphEdge> allEdges;


  /*********************************************************
   * Public methods
   ********************************************************/

  public VoronoiTesselator() {
  }

  public void Init(double minDistanceBetweenSites) {
    siteidx = 0;
    this.sites = null;
    this.allEdges = null;
    this.minDistanceBetweenSites = minDistanceBetweenSites;
  }

  /**
   * @param xValuesIn Array of X values for each site.
   * @param yValuesIn Array of Y values for each site. Must be identical length to yValuesIn
   * @param minX      The minimum X of the bounding box around the voronoi
   * @param maxX      The maximum X of the bounding box around the voronoi
   * @param minY      The minimum Y of the bounding box around the voronoi
   * @param maxY      The maximum Y of the bounding box around the voronoi
   * @return the <code>com.marginallyclever.voronoi.VoronoiGraphEdge</code>s of this VoronoiTesselator.
   */
  public List<VoronoiGraphEdge> generateVoronoi(double[] xValuesIn, double[] yValuesIn,
                                                double minX, double maxX, double minY, double maxY) {
    sort(xValuesIn, yValuesIn, xValuesIn.length);

    // Check bounding box inputs - if mins are bigger than maxes, swap them
    double temp = 0;
    if (minX > maxX) {
      temp = minX;
      minX = maxX;
      maxX = temp;
    }
    if (minY > maxY) {
      temp = minY;
      minY = maxY;
      maxY = temp;
    }
    borderMinX = minX;
    borderMinY = minY;
    borderMaxX = maxX;
    borderMaxY = maxY;

    siteidx = 0;
    voronoi_bd();

    return allEdges;
  }


  /*********************************************************
   * Private methods - implementation details
   ********************************************************/

  private void sort(double[] xValuesIn, double[] yValuesIn, int count) {
    sites = null;
    allEdges = new LinkedList<>();

    nsites = count;
    nvertices = 0;
    nedges = 0;

    double sn = (double) nsites + 4;
    sqrt_nsites = (int) Math.sqrt(sn);

    // Copy the inputs so we don't modify the originals
    double[] xValues = new double[count];
    double[] yValues = new double[count];
    for (int i = 0; i < count; i++) {
      xValues[i] = xValuesIn[i];
      yValues[i] = yValuesIn[i];
    }
    sortNode(xValues, yValues, count);
  }

  private void qsort(VoronoiSite[] sites) {
    List<VoronoiSite> listSites = new ArrayList<>(sites.length);
    Collections.addAll(listSites, sites);

    Collections.sort(listSites, new Comparator<VoronoiSite>() {
      @Override
      public final int compare(VoronoiSite p1, VoronoiSite p2) {
        Point s1 = p1.coord, s2 = p2.coord;
        if (s1.y < s2.y) {
          return (-1);
        }
        if (s1.y > s2.y) {
          return (1);
        }
        if (s1.getX() < s2.getX()) {
          return (-1);
        }
        if (s1.getX() > s2.getX()) {
          return (1);
        }
        return (0);
      }
    });

    // Copy back into the array
    for (int i = 0; i < sites.length; i++) {
      sites[i] = listSites.get(i);
    }
  }

  private void sortNode(double xValues[], double yValues[], int numPoints) {
    int i;
    nsites = numPoints;
    sites = new VoronoiSite[nsites];
    xmin = xValues[0];
    ymin = yValues[0];
    xmax = xValues[0];
    ymax = yValues[0];
    for (i = 0; i < nsites; i++) {
      sites[i] = new VoronoiSite();
      sites[i].coord.setLocation( xValues[i], yValues[i] );
      sites[i].sitenbr = i;

      if (xValues[i] < xmin) {
        xmin = xValues[i];
      } else if (xValues[i] > xmax) {
        xmax = xValues[i];
      }

      if (yValues[i] < ymin) {
        ymin = yValues[i];
      } else if (yValues[i] > ymax) {
        ymax = yValues[i];
      }
    }
    qsort(sites);
    deltay = ymax - ymin;
    deltax = xmax - xmin;
  }

  /* return a single in-storage site */
  private VoronoiSite nextone() {
    VoronoiSite s;
    if (siteidx < nsites) {
      s = sites[siteidx];
      siteidx += 1;
      return (s);
    } else {
      return (null);
    }
  }

  private VoronoiEdge bisect(VoronoiSite s1, VoronoiSite s2) {
    double dx, dy, adx, ady;
    VoronoiEdge newedge;

    newedge = new VoronoiEdge();

    // store the sites that this edge is bisecting
    newedge.reg[0] = s1;
    newedge.reg[1] = s2;
    // to begin with, there are no endpoints on the bisector - it goes to
    // infinity
    newedge.ep[0] = null;
    newedge.ep[1] = null;

    // get the difference in x dist between the sites
    dx = s2.coord.getX() - s1.coord.getX();
    dy = s2.coord.y - s1.coord.y;
    // make sure that the difference in positive
    adx = dx > 0 ? dx : -dx;
    ady = dy > 0 ? dy : -dy;
    newedge.c = (double) (s1.coord.getX() * dx + s1.coord.y * dy + (dx * dx + dy
        * dy) * 0.5);// get the slope of the line

    if (adx > ady) {
      newedge.a = 1.0f;
      newedge.b = dy / dx;
      newedge.c /= dx;// set formula of line, with x fixed to 1
    } else {
      newedge.b = 1.0f;
      newedge.a = dx / dy;
      newedge.c /= dy;// set formula of line, with y fixed to 1
    }

    newedge.edgenbr = nedges;

    nedges += 1;
    return (newedge);
  }

  private void makevertex(VoronoiSite v) {
    v.sitenbr = nvertices;
    nvertices += 1;
  }

  private boolean PQinitialize() {
    PQcount = 0;
    PQmin = 0;
    PQhashsize = 4 * sqrt_nsites;
    PQhash = new VoronoiHalfEdge[PQhashsize];

    for (int i = 0; i < PQhashsize; i += 1) {
      PQhash[i] = new VoronoiHalfEdge();
    }
    return true;
  }

  private int PQbucket(VoronoiHalfEdge he) {
    int bucket;

    bucket = (int) ((he.ystar - ymin) / deltay * PQhashsize);
    if (bucket < 0) {
      bucket = 0;
    }
    if (bucket >= PQhashsize) {
      bucket = PQhashsize - 1;
    }
    if (bucket < PQmin) {
      PQmin = bucket;
    }
    return (bucket);
  }

  // push the HalfEdge into the ordered linked list of vertices
  private void PQinsert(VoronoiHalfEdge he, VoronoiSite v, double offset) {
    VoronoiHalfEdge last, next;

    he.vertex = v;
    he.ystar = (double) (v.coord.y + offset);
    last = PQhash[PQbucket(he)];
    while ((next = last.PQnext) != null
        && (he.ystar > next.ystar || (he.ystar == next.ystar && v.coord.getX() > next.vertex.coord.getX()))) {
      last = next;
    }
    he.PQnext = last.PQnext;
    last.PQnext = he;
    PQcount += 1;
  }

  // remove the HalfEdge from the list of vertices
  private void PQdelete(VoronoiHalfEdge he) {
    VoronoiHalfEdge last;

    if (he.vertex != null) {
      last = PQhash[PQbucket(he)];
      while (last.PQnext != he) {
        last = last.PQnext;
      }

      last.PQnext = he.PQnext;
      PQcount -= 1;
      he.vertex = null;
    }
  }

  private boolean PQempty() {
    return (PQcount == 0);
  }

  private Point PQ_min() {
    Point answer = new Point();

    while (PQhash[PQmin].PQnext == null) {
      PQmin += 1;
    }
    answer.setLocation( PQhash[PQmin].PQnext.vertex.coord.x, 
    					PQhash[PQmin].PQnext.ystar );
    return (answer);
  }

  private VoronoiHalfEdge PQextractmin() {
    VoronoiHalfEdge curr;

    curr = PQhash[PQmin].PQnext;
    PQhash[PQmin].PQnext = curr.PQnext;
    PQcount -= 1;
    return (curr);
  }

  private VoronoiHalfEdge HEcreate(VoronoiEdge e, int pm) {
    VoronoiHalfEdge answer;
    answer = new VoronoiHalfEdge();
    answer.ELedge = e;
    answer.ELpm = pm;
    answer.PQnext = null;
    answer.vertex = null;
    return (answer);
  }

  private boolean ELinitialize() {
    int i;
    ELhashsize = 2 * sqrt_nsites;
    ELhash = new VoronoiHalfEdge[ELhashsize];

    for (i = 0; i < ELhashsize; i += 1) {
      ELhash[i] = null;
    }
    ELleftend = HEcreate(null, 0);
    ELrightend = HEcreate(null, 0);
    ELleftend.ELleft = null;
    ELleftend.ELright = ELrightend;
    ELrightend.ELleft = ELleftend;
    ELrightend.ELright = null;
    ELhash[0] = ELleftend;
    ELhash[ELhashsize - 1] = ELrightend;

    return true;
  }

  private VoronoiHalfEdge ELright(VoronoiHalfEdge he) {
    return (he.ELright);
  }

  private VoronoiHalfEdge ELleft(VoronoiHalfEdge he) {
    return (he.ELleft);
  }

  private VoronoiSite leftreg(VoronoiHalfEdge he) {
    if (he.ELedge == null) {
      return (bottomsite);
    }
    return (he.ELpm == LE ? he.ELedge.reg[LE] : he.ELedge.reg[RE]);
  }

  private void ELinsert(VoronoiHalfEdge lb, VoronoiHalfEdge newHe) {
    newHe.ELleft = lb;
    newHe.ELright = lb.ELright;
    (lb.ELright).ELleft = newHe;
    lb.ELright = newHe;
  }

  /*
   * This delete routine can't reclaim node, since pointers from hash table
   * may be present.
   */
  private void ELdelete(VoronoiHalfEdge he) {
    (he.ELleft).ELright = he.ELright;
    (he.ELright).ELleft = he.ELleft;
    he.deleted = true;
  }

  /* Get entry from hash table, pruning any deleted nodes */
  private VoronoiHalfEdge ELgethash(int b) {
    VoronoiHalfEdge he;

    if (b < 0 || b >= ELhashsize) {
      return (null);
    }
    he = ELhash[b];
    if (he == null || !he.deleted) {
      return (he);
    }

        /* Hash table points to deleted half edge. Patch as necessary. */
    ELhash[b] = null;
    return (null);
  }

  private VoronoiHalfEdge ELleftbnd(Point p) {
    int i, bucket;
    VoronoiHalfEdge he;

        /* Use hash table to get close to desired halfedge */
    // use the hash function to find the place in the hash map that this
    // HalfEdge should be
    bucket = (int) ((p.getX() - xmin) / deltax * ELhashsize);

    // make sure that the bucket position in within the range of the hash
    // array
    if (bucket < 0) {
      bucket = 0;
    }
    if (bucket >= ELhashsize) {
      bucket = ELhashsize - 1;
    }

    he = ELgethash(bucket);
    if (he == null)
    // if the HE isn't found, search backwards and forwards in the hash map
    // for the first non-null entry
    {
      for (i = 1; i < ELhashsize; i += 1) {
        if ((he = ELgethash(bucket - i)) != null) {
          break;
        }
        if ((he = ELgethash(bucket + i)) != null) {
          break;
        }
      }
    }
        /* Now search linear list of halfedges for the correct one */
    if (he == ELleftend || (he != ELrightend && right_of(he, p))) {
      // keep going right on the list until either the end is reached, or
      // you find the 1st edge which the point isn't to the right of
      do {
        he = he.ELright;
      } while (he != ELrightend && right_of(he, p));
      he = he.ELleft;
    } else
    // if the point is to the left of the HalfEdge, then search left for
    // the HE just to the left of the point
    {
      do {
        he = he.ELleft;
      } while (he != ELleftend && !right_of(he, p));
    }

        /* Update hash table and reference counts */
    if (bucket > 0 && bucket < ELhashsize - 1) {
      ELhash[bucket] = he;
    }
    return (he);
  }

  private void pushGraphEdge(VoronoiSite leftSite, VoronoiSite rightSite, double x1, double y1, double x2, double y2) {
    VoronoiGraphEdge newEdge = new VoronoiGraphEdge();
    allEdges.add(newEdge);
    newEdge.x1 = x1;
    newEdge.y1 = y1;
    newEdge.x2 = x2;
    newEdge.y2 = y2;

    newEdge.site1 = leftSite.sitenbr;
    newEdge.site2 = rightSite.sitenbr;
  }

  private void clip_line(VoronoiEdge e) {
    double pxmin, pxmax, pymin, pymax;
    VoronoiSite s1, s2;
    double x1 = 0, x2 = 0, y1 = 0, y2 = 0;

    x1 = e.reg[0].coord.getX();
    x2 = e.reg[1].coord.getX();
    y1 = e.reg[0].coord.y;
    y2 = e.reg[1].coord.y;

    // if the distance between the two points this line was created from is
    // less than the square root of 2, then ignore it
    if (Math.sqrt(((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1))) < minDistanceBetweenSites) {
      return;
    }
    pxmin = borderMinX;
    pxmax = borderMaxX;
    pymin = borderMinY;
    pymax = borderMaxY;

    if (e.a == 1.0 && e.b >= 0.0) {
      s1 = e.ep[1];
      s2 = e.ep[0];
    } else {
      s1 = e.ep[0];
      s2 = e.ep[1];
    }

    if (e.a == 1.0) {
      y1 = pymin;
      if (s1 != null && s1.coord.y > pymin) {
        y1 = s1.coord.y;
      }
      if (y1 > pymax) {
        y1 = pymax;
      }
      x1 = e.c - e.b * y1;
      y2 = pymax;
      if (s2 != null && s2.coord.y < pymax) {
        y2 = s2.coord.y;
      }

      if (y2 < pymin) {
        y2 = pymin;
      }
      x2 = (e.c) - (e.b) * y2;
      if (((x1 > pxmax) & (x2 > pxmax)) | ((x1 < pxmin) & (x2 < pxmin))) {
        return;
      }
      if (x1 > pxmax) {
        x1 = pxmax;
        y1 = (e.c - x1) / e.b;
      }
      if (x1 < pxmin) {
        x1 = pxmin;
        y1 = (e.c - x1) / e.b;
      }
      if (x2 > pxmax) {
        x2 = pxmax;
        y2 = (e.c - x2) / e.b;
      }
      if (x2 < pxmin) {
        x2 = pxmin;
        y2 = (e.c - x2) / e.b;
      }
    } else {
      x1 = pxmin;
      if (s1 != null && s1.coord.getX() > pxmin) {
        x1 = s1.coord.getX();
      }
      if (x1 > pxmax) {
        x1 = pxmax;
      }
      y1 = e.c - e.a * x1;
      x2 = pxmax;
      if (s2 != null && s2.coord.getX() < pxmax) {
        x2 = s2.coord.getX();
      }
      if (x2 < pxmin) {
        x2 = pxmin;
      }
      y2 = e.c - e.a * x2;
      if (((y1 > pymax) & (y2 > pymax)) | ((y1 < pymin) & (y2 < pymin))) {
        return;
      }
      if (y1 > pymax) {
        y1 = pymax;
        x1 = (e.c - y1) / e.a;
      }
      if (y1 < pymin) {
        y1 = pymin;
        x1 = (e.c - y1) / e.a;
      }
      if (y2 > pymax) {
        y2 = pymax;
        x2 = (e.c - y2) / e.a;
      }
      if (y2 < pymin) {
        y2 = pymin;
        x2 = (e.c - y2) / e.a;
      }
    }

    pushGraphEdge(e.reg[0], e.reg[1], x1, y1, x2, y2);
  }

  private void endpoint(VoronoiEdge e, int lr, VoronoiSite s) {
    e.ep[lr] = s;
    if (e.ep[RE - lr] == null) {
      return;
    }
    clip_line(e);
  }

  /* returns 1 if p is to right of halfedge e */
  private boolean right_of(VoronoiHalfEdge el, Point p) {
    VoronoiEdge e;
    VoronoiSite topsite;
    boolean right_of_site;
    boolean above, fast;
    double dxp, dyp, dxs, t1, t2, t3, yl;

    e = el.ELedge;
    topsite = e.reg[1];
    if (p.getX() > topsite.coord.getX()) {
      right_of_site = true;
    } else {
      right_of_site = false;
    }
    if (right_of_site && el.ELpm == LE) {
      return (true);
    }
    if (!right_of_site && el.ELpm == RE) {
      return (false);
    }

    if (e.a == 1.0) {
      dyp = p.y - topsite.coord.y;
      dxp = p.getX() - topsite.coord.getX();
      fast = false;
      if ((!right_of_site & (e.b < 0.0)) | (right_of_site & (e.b >= 0.0))) {
        above = dyp >= e.b * dxp;
        fast = above;
      } else {
        above = p.getX() + p.y * e.b > e.c;
        if (e.b < 0.0) {
          above = !above;
        }
        if (!above) {
          fast = true;
        }
      }
      if (!fast) {
        dxs = topsite.coord.getX() - (e.reg[0]).coord.getX();
        above = e.b * (dxp * dxp - dyp * dyp) < dxs * dyp
            * (1.0 + 2.0 * dxp / dxs + e.b * e.b);
        if (e.b < 0.0) {
          above = !above;
        }
      }
    } else /* e.b==1.0 */

    {
      yl = e.c - e.a * p.getX();
      t1 = p.y - yl;
      t2 = p.getX() - topsite.coord.getX();
      t3 = yl - topsite.coord.y;
      above = t1 * t1 > t2 * t2 + t3 * t3;
    }
    return (el.ELpm == LE ? above : !above);
  }

  private VoronoiSite rightreg(VoronoiHalfEdge he) {
    if (he.ELedge == (VoronoiEdge) null)
    // if this halfedge has no edge, return the bottom site (whatever
    // that is)
    {
      return (bottomsite);
    }

    // if the ELpm field is zero, return the site 0 that this edge bisects,
    // otherwise return site number 1
    return (he.ELpm == LE ? he.ELedge.reg[RE] : he.ELedge.reg[LE]);
  }

  private double dist(VoronoiSite s, VoronoiSite t) {
    double dx, dy;
    dx = s.coord.getX() - t.coord.getX();
    dy = s.coord.y - t.coord.y;
    return (double) (Math.sqrt(dx * dx + dy * dy));
  }

  // create a new site where the HalfEdges el1 and el2 intersect - note that
  // the Point in the argument list is not used, don't know why it's there
  private VoronoiSite intersect(VoronoiHalfEdge el1, VoronoiHalfEdge el2) {
    VoronoiEdge e1, e2, e;
    VoronoiHalfEdge el;
    double d, xint, yint;
    boolean right_of_site;
    VoronoiSite v;

    e1 = el1.ELedge;
    e2 = el2.ELedge;
    if (e1 == null || e2 == null) {
      return null;
    }

    // if the two edges bisect the same parent, return null
    if (e1.reg[1] == e2.reg[1]) {
      return null;
    }

    d = e1.a * e2.b - e1.b * e2.a;
    if (-1.0e-10 < d && d < 1.0e-10) {
      return null;
    }

    xint = (e1.c * e2.b - e2.c * e1.b) / d;
    yint = (e2.c * e1.a - e1.c * e2.a) / d;

    if ((e1.reg[1].coord.y < e2.reg[1].coord.y)
        || (e1.reg[1].coord.y == e2.reg[1].coord.y && e1.reg[1].coord.getX() < e2.reg[1].coord.getX())) {
      el = el1;
      e = e1;
    } else {
      el = el2;
      e = e2;
    }

    right_of_site = xint >= e.reg[1].coord.getX();
    if ((right_of_site && el.ELpm == LE)
        || (!right_of_site && el.ELpm == RE)) {
      return null;
    }

    // create a new site at the point of intersection - this is a new vector
    // event waiting to happen
    v = new VoronoiSite();
    v.coord.setLocation( xint, yint );
    return (v);
  }

  /*
   * implicit parameters: nsites, sqrt_nsites, xmin, xmax, ymin, ymax, deltax,
   * deltay (can all be estimates). Performance suffers if they are wrong;
   * better to make nsites, deltax, and deltay too big than too small. (?)
   */
  private boolean voronoi_bd() {
    VoronoiSite newsite, bot, top, temp, p;
    VoronoiSite v;
    Point newintstar = null;
    int pm;
    VoronoiHalfEdge lbnd, rbnd, llbnd, rrbnd, bisector;
    VoronoiEdge e;

    PQinitialize();
    ELinitialize();

    bottomsite = nextone();
    newsite = nextone();
    while (true) {
      if (!PQempty()) {
        newintstar = PQ_min();
      }
      // if the lowest site has a smaller y value than the lowest vector
      // intersection,
      // process the site otherwise process the vector intersection

      if (newsite != null
          && (PQempty() || newsite.coord.y < newintstar.y || (newsite.coord.y == newintstar.y && newsite.coord.getX() < newintstar.getX()))) {
                /* new site is smallest -this is a site event */
        // get the first HalfEdge to the LEFT of the new site
        lbnd = ELleftbnd((newsite.coord));
        // get the first HalfEdge to the RIGHT of the new site
        rbnd = ELright(lbnd);
        // if this halfedge has no edge,bot =bottom site (whatever that
        // is)
        bot = rightreg(lbnd);
        // create a new edge that bisects
        e = bisect(bot, newsite);

        // create a new HalfEdge, setting its ELpm field to 0
        bisector = HEcreate(e, LE);
        // insert this new bisector edge between the left and right
        // vectors in a linked list
        ELinsert(lbnd, bisector);

        // if the new bisector intersects with the left edge,
        // remove the left edge's vertex, and put in the new one
        if ((p = intersect(lbnd, bisector)) != null) {
          PQdelete(lbnd);
          PQinsert(lbnd, p, dist(p, newsite));
        }
        lbnd = bisector;
        // create a new HalfEdge, setting its ELpm field to 1
        bisector = HEcreate(e, RE);
        // insert the new HE to the right of the original bisector
        // earlier in the IF stmt
        ELinsert(lbnd, bisector);

        // if this new bisector intersects with the new HalfEdge
        if ((p = intersect(bisector, rbnd)) != null) {
          // push the HE into the ordered linked list of vertices
          PQinsert(bisector, p, dist(p, newsite));
        }
        newsite = nextone();
      } else if (!PQempty())
            /* intersection is smallest - this is a vector event */ {
        // pop the HalfEdge with the lowest vector off the ordered list
        // of vectors
        lbnd = PQextractmin();
        // get the HalfEdge to the left of the above HE
        llbnd = ELleft(lbnd);
        // get the HalfEdge to the right of the above HE
        rbnd = ELright(lbnd);
        // get the HalfEdge to the right of the HE to the right of the
        // lowest HE
        rrbnd = ELright(rbnd);
        // get the Site to the left of the left HE which it bisects
        bot = leftreg(lbnd);
        // get the Site to the right of the right HE which it bisects
        top = rightreg(rbnd);

        v = lbnd.vertex; // get the vertex that caused this event
        makevertex(v); // set the vertex number - couldn't do this
        // earlier since we didn't know when it would be processed
        endpoint(lbnd.ELedge, lbnd.ELpm, v);
        // set the endpoint of
        // the left HalfEdge to be this vector
        endpoint(rbnd.ELedge, rbnd.ELpm, v);
        // set the endpoint of the right HalfEdge to
        // be this vector
        ELdelete(lbnd); // mark the lowest HE for
        // deletion - can't delete yet because there might be pointers
        // to it in Hash Map
        PQdelete(rbnd);
        // remove all vertex events to do with the right HE
        ELdelete(rbnd); // mark the right HE for
        // deletion - can't delete yet because there might be pointers
        // to it in Hash Map
        pm = LE; // set the pm variable to zero

        if (bot.coord.y > top.coord.y)
        // if the site to the left of the event is higher than the
        // Site
        { // to the right of it, then swap them and set the 'pm'
          // variable to 1
          temp = bot;
          bot = top;
          top = temp;
          pm = RE;
        }
        e = bisect(bot, top); // create an Edge (or line)
        // that is between the two Sites. This creates the formula of
        // the line, and assigns a line number to it
        bisector = HEcreate(e, pm); // create a HE from the Edge 'e',
        // and make it point to that edge
        // with its ELedge field
        ELinsert(llbnd, bisector); // insert the new bisector to the
        // right of the left HE
        endpoint(e, RE - pm, v); // set one endpoint to the new edge
        // to be the vector point 'v'.
        // If the site to the left of this bisector is higher than the
        // right Site, then this endpoint
        // is put in position 0; otherwise in pos 1

        // if left HE and the new bisector intersect, then delete
        // the left HE, and reinsert it
        if ((p = intersect(llbnd, bisector)) != null) {
          PQdelete(llbnd);
          PQinsert(llbnd, p, dist(p, bot));
        }

        // if right HE and the new bisector intersect, then
        // reinsert it
        if ((p = intersect(bisector, rrbnd)) != null) {
          PQinsert(bisector, p, dist(p, bot));
        }
      } else {
        break;
      }
    }

    for (lbnd = ELright(ELleftend); lbnd != ELrightend; lbnd = ELright(lbnd)) {
      e = lbnd.ELedge;
      clip_line(e);
    }

    return true;
  }
}
