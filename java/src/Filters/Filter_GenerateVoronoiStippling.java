package Filters;

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



import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import Makelangelo.MachineConfiguration;
import Makelangelo.MainGUI;
import Makelangelo.Point2D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;


/**
 * Dithering using a particle system
 * @author Dan
 * http://en.wikipedia.org/wiki/Fortune%27s_algorithm
 * http://skynet.ie/~sos/mapviewer/voronoi.php
 */
public class Filter_GenerateVoronoiStippling extends Filter {
	public String GetName() { return "Voronoi stipples"; }
	
	class Point
	{
	    double x, y;

	    public Point()
	    {
	    }

	    public void setPoint(double x, double y)
	    {
	        this.x = x;
	        this.y = y;
	    }
	}

	
	public class Site
	{
	    Point coord;
	    int sitenbr;

	    public Site()
	    {
	        coord = new Point();
	    }
	}
	
	class Edge
	{
	    public double a = 0, b = 0, c = 0;
	    Site[] ep;  // JH: End points?
	    Site[] reg; // JH: Sites this edge bisects?
	    int edgenbr;

	    Edge()
	    {
	        ep = new Site[2];
	        reg = new Site[2];
	    }
	}

	public class Halfedge
	{
	    Halfedge ELleft, ELright;
	    Edge ELedge;
	    boolean deleted;
	    int ELpm;
	    Site vertex;
	    double ystar;
	    Halfedge PQnext;
	
	    public Halfedge()
	    {
	        PQnext = null;
	    }
	}
	
	public class GraphEdge
	{
	    public double x1, y1, x2, y2;

	    public int site1;
	    public int site2;
	}


	class VoronoiCell {
		public Point2D centroid = new Point2D();
		public float weight;
	}
	

	class CellEdge {
		double px,py;
		double nx,ny;
	}
	
	
	private int totalCells=1;
	private VoronoiCell [] cells = new VoronoiCell[1];
	private int w, h;
	private BufferedImage src_img;
	private List<GraphEdge> graphEdges = null;
	private int MAX_GENERATIONS=40;
	private int MAX_CELLS=5000;
	private Point2D bound_min = new Point2D();
	private Point2D bound_max = new Point2D();
	private int numEdgesInCell;
	private List<CellEdge> cellBorder = null;
	private double[] xValuesIn=null;
	private double[] yValuesIn=null;
	
	
	
	public void Convert(BufferedImage img) throws IOException {
		JTextField text_gens = new JTextField(Integer.toString(MAX_GENERATIONS), 8);
		JTextField text_cells = new JTextField(Integer.toString(MAX_CELLS), 8);
	
		JPanel panel = new JPanel(new GridLayout(0,1));
		panel.add(new JLabel("Number of cells"));
		panel.add(text_cells);
		panel.add(new JLabel("Number of generations"));
		panel.add(text_gens);
		
	    int result = JOptionPane.showConfirmDialog(null, panel, GetName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
	    if (result == JOptionPane.OK_OPTION) {
	    	MAX_GENERATIONS = Integer.parseInt(text_gens.getText());
	    	MAX_CELLS = Integer.parseInt(text_cells.getText());
	    	
			src_img = img;
			h = img.getHeight();
			w = img.getWidth();
			
			tool = MachineConfiguration.getSingleton().GetCurrentTool();
			ImageSetupTransform(img);
	
			cellBorder = new ArrayList<CellEdge>();
	
		    
			initializeCells(0.5);
			evolveCells();
			writeOutCells();
	    }
	}


	// set some starting points in a grid
	protected void initializeCells(double minDistanceBetweenSites) {
		MainGUI.getSingleton().Log("<font color='green'>Initializing cells</font>\n");

		totalCells=MAX_CELLS;

		double totalArea = w*h;
		double pointArea = totalArea/totalCells;
		float length = (float)Math.sqrt(pointArea);
		float x,y;
		totalCells=0;
		for(y = length/2; y < h; y += length ) {
			for(x = length/2; x < w; x += length ) {
				totalCells++;
			}
		}

		cells = new VoronoiCell[totalCells];
		int used=0;
		
		for(y = length/2; y < h; y += length ) {
			for(x = length/2; x < w; x += length ) {
				cells[used]=new VoronoiCell();
				//cells[used].centroid.set((float)Math.random()*(float)w,(float)Math.random()*(float)h);
				cells[used].centroid.set(x,y);
				++used;
			}
		}

		// convert the cells to sites used in the Voronoi class.
		xValuesIn = new double[cells.length];
		yValuesIn = new double[cells.length];
		
        siteidx = 0;
        sites = null;
        allEdges = null;
        this.minDistanceBetweenSites = minDistanceBetweenSites;
	}


	// jiggle the dots until they make a nice picture
	protected void evolveCells() {
		try {
			MainGUI.getSingleton().Log("<font color='green'>Mutating</font>\n");
	
			int generation=0;
			float change=0;
			do {
				generation++;
				MainGUI.getSingleton().Log("<font color='green'>Generation "+generation+"</font>\n");
	
				tessellateVoronoiDiagram();
				change = AdjustCentroids();
				
				// do again if things are still moving a lot.  Cap the # of times so we don't have an infinite loop.
			} while(change>=1 && generation<MAX_GENERATIONS);  // TODO these are a guess. tweak?  user set?
			
			MainGUI.getSingleton().Log("<font color='green'>Last "+generation+"</font>\n");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	// write cell centroids to gcode.
	protected void writeOutCells() throws IOException {
		if(graphEdges != null ) {
			MainGUI.getSingleton().Log("<font color='green'>Writing gcode to "+dest+"</font>\n");
			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(dest),"UTF-8");
			
			ImageStart(src_img,out);
	
			// set absolute coordinates
			out.write("G00 G90;\n");
			tool.WriteChangeTo(out);
			liftPen(out);

			int i;
/*
			for(i=0;i<graphEdges.size();++i) {
				GraphEdge e= graphEdges.get(i);
				
				this.MoveTo(out, (float)e.x1,(float)e.y1, true);
				this.MoveTo(out, (float)e.x1,(float)e.y1, false);
				this.MoveTo(out, (float)e.x2,(float)e.y2, false);
				this.MoveTo(out, (float)e.x2,(float)e.y2, true);
			}
//*/		
			//float step = (int)Math.ceil(tool.GetDiameter()/scale);
			float most=cells[0].weight;
			//float least=cells[0].weight;
			for(i=1;i<cells.length;++i) {
				if(most<cells[i].weight) most=cells[i].weight;
				//if(least>cells[i].weight) least=cells[i].weight;
			}
			
			for(i=0;i<cells.length;++i) {
				float r = 5f * cells[i].weight / most;
				r/=scale;
				if(r<1) continue;
				//System.out.println(i+"\t"+v);
				float x=cells[i].centroid.x;
				float y=cells[i].centroid.y;
				/*
				// boxes
				this.MoveTo(out, x-r, y-r, true);
				this.MoveTo(out, x+r, y-r, false);
				this.MoveTo(out, x+r, y+r, false);
				this.MoveTo(out, x-r, y+r, false);
				this.MoveTo(out, x-r, y-r, false);
				this.MoveTo(out, x-r, y-r, true);
				
				// filled boxes
				this.MoveTo(out, x-r, y-r, true);
				this.MoveTo(out, x+r, y-r, false);
				this.MoveTo(out, x+r, y+r, false);
				this.MoveTo(out, x-r, y+r, false);
				this.MoveTo(out, x-r, y-r, false);
				for(float j=y-r;j<y+r;j+=step) {
					this.MoveTo(out, x+r, j, false);
					this.MoveTo(out, x-r, j, false);					
				}
				this.MoveTo(out, x-r, y-r, false);
				this.MoveTo(out, x-r, y-r, true);

				// circles
				this.MoveTo(out, x-r*(float)Math.sin(0), y-r*(float)Math.cos(0), true);
				float detail=(float)(0.5*Math.PI*r);
				if(detail<4) detail=4;
				if(detail>20) detail=20;
				for(float j=1;j<=detail;++j) {
					this.MoveTo(out, 
							x-r*(float)Math.sin(j*(float)Math.PI*2.0f/detail),
							y-r*(float)Math.cos(j*(float)Math.PI*2.0f/detail), false);
				}
				this.MoveTo(out, x-r*(float)Math.sin(0), y-r*(float)Math.cos(0), false);
				this.MoveTo(out, x-r*(float)Math.sin(0), y-r*(float)Math.cos(0), true);
				*/
				// filled circles
				this.MoveTo(out, x-r*(float)Math.sin(0), y-r*(float)Math.cos(0), true);
				while(r>1) {
					float detail=(float)(0.5*Math.PI*r);
					if(detail<4) detail=4;
					if(detail>10) detail=10;
					for(float j=1;j<=detail;++j) {
						this.MoveTo(out, 
								x-r*(float)Math.sin(j*(float)Math.PI*2.0f/detail),
								y-r*(float)Math.cos(j*(float)Math.PI*2.0f/detail), false);
					}
					r-=(tool.GetDiameter()/(scale*1.5f));
				}
				this.MoveTo(out, x, y-r, false);
				this.MoveTo(out, x, y-r, true);
			}
			
			liftPen(out);
			SignName(out);
			tool.WriteMoveTo(out, 0, 0);
			out.close();
		}
	}
	

	/**
	 * Overrides MoveTo() because optimizing for zigzag is different logic than straight lines.
	 */
	protected void MoveTo(OutputStreamWriter out,float x,float y,boolean up) throws IOException {
		if(lastup!=up) {
			if(up) liftPen(out);
			else   lowerPen(out);
			lastup=up;
		}
		tool.WriteMoveTo(out, TX(x), TY(y));
	}
	
	// I have a set of points.  I want a list of cell borders.
	// cell borders are halfway between any point and it's nearest neighbors.
	protected void tessellateVoronoiDiagram() {
		// convert the cells to sites used in the Voronoi class.
		int i;
		for(i=0;i<cells.length;++i) {
			xValuesIn[i]= cells[i].centroid.x;
			yValuesIn[i]= cells[i].centroid.y;
		}
		
		// scan left to right across the image, building the list of borders as we go.
		graphEdges = generateVoronoi(xValuesIn, yValuesIn, 0, w-1, 0, h-1);
	}
		
	
	protected void generateBounds(int cellIndex) {
		numEdgesInCell=0;

		float cx = cells[cellIndex].centroid.x;
		float cy = cells[cellIndex].centroid.y;

		double dx,dy,nx,ny,dot1;
		
		//long ta = System.nanoTime();
		
		Iterator<GraphEdge> ige = graphEdges.iterator();
		while(ige.hasNext()) {
			GraphEdge e = ige.next();
			if(e.site1 != cellIndex && e.site2 != cellIndex ) continue;
			if(numEdgesInCell==0) {
				if(e.x1<e.x2) {
					bound_min.x=(float)e.x1;
					bound_max.x=(float)e.x2;
				} else {
					bound_min.x=(float)e.x2;
					bound_max.x=(float)e.x1;					
				}
				if(e.y1<e.y2) {
					bound_min.y=(float)e.y1;
					bound_max.y=(float)e.y2;
				} else {
					bound_min.y=(float)e.y2;
					bound_max.y=(float)e.y1;					
				}
			} else {
				if(bound_min.x>e.x1) bound_min.x=(float)e.x1;
				if(bound_min.x>e.x2) bound_min.x=(float)e.x2;
				if(bound_max.x<e.x1) bound_max.x=(float)e.x1;
				if(bound_max.y<e.y2) bound_max.y=(float)e.y2;
				
				if(bound_min.y>e.y1) bound_min.y=(float)e.y1;
				if(bound_min.y>e.y2) bound_min.y=(float)e.y2;
				if(bound_max.y<e.y1) bound_max.y=(float)e.y1;
				if(bound_max.y<e.y2) bound_max.y=(float)e.y2;
			}

			// make a unnormalized vector along the edge of e
			dx = e.x2 - e.x1;
			dy = e.y2 - e.y1;
			// find a line orthogonal to dx/dy
			nx=dy;
			ny=-dx;
			// dot product the centroid and the normal.
			dx = cx-e.x1;
			dy = cy-e.y1;
			dot1=(dx*nx+dy*ny);
			
			if(cellBorder.size()==numEdgesInCell) {
				cellBorder.add(new CellEdge());
			}

			CellEdge ce=cellBorder.get(numEdgesInCell++);
			ce.px = e.x1;
			ce.py = e.y1;
			if(dot1<0) {
				ce.nx = -nx;
				ce.ny = -ny;
			} else {
				ce.nx = nx;
				ce.ny = ny;
			}
		}

		//long tc = System.nanoTime();
		
		//System.out.println("\t"+((tb-ta)/1e6)+"\t"+((tc-tb)/1e6));
	}
	
	
	protected boolean insideBorder(int x,int y) {
		double dx,dy;
		int i;
		Iterator<CellEdge> ice = cellBorder.iterator();
		for(i=0;i<numEdgesInCell;++i) {
			CellEdge ce = ice.next();
			
			// dot product the test point.
			dx = x-ce.px;
			dy = y-ce.py;
			// If they are opposite signs then the test point is outside the cell
			if( dx*ce.nx+dy*ce.ny < 0 ) return false;
		}
		// passed all tests, must be in cell.
		return true;
	}
	
	
	// find the weighted center of each cell.
	// weight is based on the intensity of the color of each pixel inside the cell
	// the center of the pixel must be inside the cell to be counted.
	protected float AdjustCentroids() {
		int i,x,y;
		float change=0;
		float weight,wx,wy;
		int step = (int)Math.ceil(tool.GetDiameter()/(1.0*scale));
		
		for(i=0;i<cells.length;++i) {
			generateBounds(i);		
			int sx = (int)Math.floor(bound_min.x);
			int sy = (int)Math.floor(bound_min.y);
			int ex = (int)Math.floor(bound_max.x);
			int ey = (int)Math.floor(bound_max.y);
			//System.out.println("bounding "+i+" from "+sx+", "+sy+" to "+ex+", "+ey);
			//System.out.println("centroid "+cells[i].centroid.x+", "+cells[i].centroid.y);
			
			weight=0;
			wx=0;
			wy=0;

			for(y = sy; y <= ey; y+=step) {
				for(x = sx; x <= ex; x+=step) {
					if(insideBorder(x, y)) {
						float val = (float)sample1x1(src_img,x,y) / 255.0f;
						val = 1.0f - val;
						weight += val;
						wx += x * val;
						wy += y * val;
					}
				}
			}
			if( weight > 0 ) {
				wx /= weight;
				wy /= weight;

				cells[i].weight = weight;
				
				// make sure centroid can't leave image bounds
				if(wx<0) wx=0;
				if(wy<0) wy=0;
				if(wx>=w) wx = w-1;
				if(wy>=h) wy = h-1;

				float dx = wx - cells[i].centroid.x;
				float dy = wy - cells[i].centroid.y;
				
				change += dx*dx+dy*dy;
				//change = (float)Math.sqrt(change);
				
				// use the new center
				cells[i].centroid.set(wx, wy);
			}
		}
		
		return change;
	}
	
	
	
	
	
    // ************* Private members ******************
    private double borderMinX, borderMaxX, borderMinY, borderMaxY;
    private int siteidx;
    private double xmin, xmax, ymin, ymax, deltax, deltay;
    private int nvertices;
    private int nedges;
    private int nsites;
    private Site[] sites;
    private Site bottomsite;
    private int sqrt_nsites;
    private double minDistanceBetweenSites;
    private int PQcount;
    private int PQmin;
    private int PQhashsize;
    private Halfedge PQhash[];

    private final static int LE = 0;
    private final static int RE = 1;

    private int ELhashsize;
    private Halfedge ELhash[];
    private Halfedge ELleftend, ELrightend;
    private List<GraphEdge> allEdges;
    

    /*********************************************************
     * Public methods
     ********************************************************/


    /**
     * 
     * @param xValuesIn Array of X values for each site.
     * @param yValuesIn Array of Y values for each site. Must be identical length to yValuesIn
     * @param minX The minimum X of the bounding box around the voronoi
     * @param maxX The maximum X of the bounding box around the voronoi
     * @param minY The minimum Y of the bounding box around the voronoi
     * @param maxY The maximum Y of the bounding box around the voronoi
     * @return
     */
    public List<GraphEdge> generateVoronoi(double[] xValuesIn, double[] yValuesIn,
            double minX, double maxX, double minY, double maxY)
    {
        sort(xValuesIn, yValuesIn, xValuesIn.length);

        // Check bounding box inputs - if mins are bigger than maxes, swap them
        double temp = 0;
        if (minX > maxX)
        {
            temp = minX;
            minX = maxX;
            maxX = temp;
        }
        if (minY > maxY)
        {
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

    private void sort(double[] xValuesIn, double[] yValuesIn, int count)
    {
        sites = null;
        allEdges = new LinkedList<GraphEdge>();

        nsites = count;
        nvertices = 0;
        nedges = 0;

        double sn = (double) nsites + 4;
        sqrt_nsites = (int) Math.sqrt(sn);

        // Copy the inputs so we don't modify the originals
        double[] xValues = new double[count];
        double[] yValues = new double[count];
        for (int i = 0; i < count; i++)
        {
            xValues[i] = xValuesIn[i];
            yValues[i] = yValuesIn[i];
        }
        sortNode(xValues, yValues, count);
    }

    private void qsort(Site[] sites)
    {
        List<Site> listSites = new ArrayList<Site>(sites.length);
        for (Site s: sites)
        {
            listSites.add(s);
        }

        Collections.sort(listSites, new Comparator<Site>()
        {
            @Override
            public final int compare(Site p1, Site p2)
            {
                Point s1 = p1.coord, s2 = p2.coord;
                if (s1.y < s2.y)
                {
                    return (-1);
                }
                if (s1.y > s2.y)
                {
                    return (1);
                }
                if (s1.x < s2.x)
                {
                    return (-1);
                }
                if (s1.x > s2.x)
                {
                    return (1);
                }
                return (0);
            }
        });

        // Copy back into the array
        for (int i=0; i<sites.length; i++)
        {
            sites[i] = listSites.get(i);
        }
    }

    private void sortNode(double xValues[], double yValues[], int numPoints)
    {
        int i;
        nsites = numPoints;
        sites = new Site[nsites];
        xmin = xValues[0];
        ymin = yValues[0];
        xmax = xValues[0];
        ymax = yValues[0];
        for (i = 0; i < nsites; i++)
        {
            sites[i] = new Site();
            sites[i].coord.setPoint(xValues[i], yValues[i]);
            sites[i].sitenbr = i;

            if (xValues[i] < xmin)
            {
                xmin = xValues[i];
            } else if (xValues[i] > xmax)
            {
                xmax = xValues[i];
            }

            if (yValues[i] < ymin)
            {
                ymin = yValues[i];
            } else if (yValues[i] > ymax)
            {
                ymax = yValues[i];
            }
        }
        qsort(sites);
        deltay = ymax - ymin;
        deltax = xmax - xmin;
    }

    /* return a single in-storage site */
    private Site nextone()
    {
        Site s;
        if (siteidx < nsites)
        {
            s = sites[siteidx];
            siteidx += 1;
            return (s);
        } else
        {
            return (null);
        }
    }

    private Edge bisect(Site s1, Site s2)
    {
        double dx, dy, adx, ady;
        Edge newedge;

        newedge = new Edge();

        // store the sites that this edge is bisecting
        newedge.reg[0] = s1;
        newedge.reg[1] = s2;
        // to begin with, there are no endpoints on the bisector - it goes to
        // infinity
        newedge.ep[0] = null;
        newedge.ep[1] = null;

        // get the difference in x dist between the sites
        dx = s2.coord.x - s1.coord.x;
        dy = s2.coord.y - s1.coord.y;
        // make sure that the difference in positive
        adx = dx > 0 ? dx : -dx;
        ady = dy > 0 ? dy : -dy;
        newedge.c = (double) (s1.coord.x * dx + s1.coord.y * dy + (dx * dx + dy
                * dy) * 0.5);// get the slope of the line

        if (adx > ady)
        {
            newedge.a = 1.0f;
            newedge.b = dy / dx;
            newedge.c /= dx;// set formula of line, with x fixed to 1
        } else
        {
            newedge.b = 1.0f;
            newedge.a = dx / dy;
            newedge.c /= dy;// set formula of line, with y fixed to 1
        }

        newedge.edgenbr = nedges;

        nedges += 1;
        return (newedge);
    }

    private void makevertex(Site v)
    {
        v.sitenbr = nvertices;
        nvertices += 1;
    }

    private boolean PQinitialize()
    {
        PQcount = 0;
        PQmin = 0;
        PQhashsize = 4 * sqrt_nsites;
        PQhash = new Halfedge[PQhashsize];

        for (int i = 0; i < PQhashsize; i += 1)
        {
            PQhash[i] = new Halfedge();
        }
        return true;
    }

    private int PQbucket(Halfedge he)
    {
        int bucket;

        bucket = (int) ((he.ystar - ymin) / deltay * PQhashsize);
        if (bucket < 0)
        {
            bucket = 0;
        }
        if (bucket >= PQhashsize)
        {
            bucket = PQhashsize - 1;
        }
        if (bucket < PQmin)
        {
            PQmin = bucket;
        }
        return (bucket);
    }

    // push the HalfEdge into the ordered linked list of vertices
    private void PQinsert(Halfedge he, Site v, double offset)
    {
        Halfedge last, next;

        he.vertex = v;
        he.ystar = (double) (v.coord.y + offset);
        last = PQhash[PQbucket(he)];
        while ((next = last.PQnext) != null
                && (he.ystar > next.ystar || (he.ystar == next.ystar && v.coord.x > next.vertex.coord.x)))
        {
            last = next;
        }
        he.PQnext = last.PQnext;
        last.PQnext = he;
        PQcount += 1;
    }

    // remove the HalfEdge from the list of vertices
    private void PQdelete(Halfedge he)
    {
        Halfedge last;

        if (he.vertex != null)
        {
            last = PQhash[PQbucket(he)];
            while (last.PQnext != he)
            {
                last = last.PQnext;
            }

            last.PQnext = he.PQnext;
            PQcount -= 1;
            he.vertex = null;
        }
    }

    private boolean PQempty()
    {
        return (PQcount == 0);
    }

    private Point PQ_min()
    {
        Point answer = new Point();

        while (PQhash[PQmin].PQnext == null)
        {
            PQmin += 1;
        }
        answer.x = PQhash[PQmin].PQnext.vertex.coord.x;
        answer.y = PQhash[PQmin].PQnext.ystar;
        return (answer);
    }

    private Halfedge PQextractmin()
    {
        Halfedge curr;

        curr = PQhash[PQmin].PQnext;
        PQhash[PQmin].PQnext = curr.PQnext;
        PQcount -= 1;
        return (curr);
    }

    private Halfedge HEcreate(Edge e, int pm)
    {
        Halfedge answer;
        answer = new Halfedge();
        answer.ELedge = e;
        answer.ELpm = pm;
        answer.PQnext = null;
        answer.vertex = null;
        return (answer);
    }

    private boolean ELinitialize()
    {
        int i;
        ELhashsize = 2 * sqrt_nsites;
        ELhash = new Halfedge[ELhashsize];

        for (i = 0; i < ELhashsize; i += 1)
        {
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

    private Halfedge ELright(Halfedge he)
    {
        return (he.ELright);
    }

    private Halfedge ELleft(Halfedge he)
    {
        return (he.ELleft);
    }

    private Site leftreg(Halfedge he)
    {
        if (he.ELedge == null)
        {
            return (bottomsite);
        }
        return (he.ELpm == LE ? he.ELedge.reg[LE] : he.ELedge.reg[RE]);
    }

    private void ELinsert(Halfedge lb, Halfedge newHe)
    {
        newHe.ELleft = lb;
        newHe.ELright = lb.ELright;
        (lb.ELright).ELleft = newHe;
        lb.ELright = newHe;
    }

    /*
     * This delete routine can't reclaim node, since pointers from hash table
     * may be present.
     */
    private void ELdelete(Halfedge he)
    {
        (he.ELleft).ELright = he.ELright;
        (he.ELright).ELleft = he.ELleft;
        he.deleted = true;
    }

    /* Get entry from hash table, pruning any deleted nodes */
    private Halfedge ELgethash(int b)
    {
        Halfedge he;

        if (b < 0 || b >= ELhashsize)
        {
            return (null);
        }
        he = ELhash[b];
        if (he == null || !he.deleted)
        {
            return (he);
        }

        /* Hash table points to deleted half edge. Patch as necessary. */
        ELhash[b] = null;
        return (null);
    }

    private Halfedge ELleftbnd(Point p)
    {
        int i, bucket;
        Halfedge he;

        /* Use hash table to get close to desired halfedge */
        // use the hash function to find the place in the hash map that this
        // HalfEdge should be
        bucket = (int) ((p.x - xmin) / deltax * ELhashsize);

        // make sure that the bucket position in within the range of the hash
        // array
        if (bucket < 0)
        {
            bucket = 0;
        }
        if (bucket >= ELhashsize)
        {
            bucket = ELhashsize - 1;
        }

        he = ELgethash(bucket);
        if (he == null)
        // if the HE isn't found, search backwards and forwards in the hash map
        // for the first non-null entry
        {
            for (i = 1; i < ELhashsize; i += 1)
            {
                if ((he = ELgethash(bucket - i)) != null)
                {
                    break;
                }
                if ((he = ELgethash(bucket + i)) != null)
                {
                    break;
                }
            }
        }
        /* Now search linear list of halfedges for the correct one */
        if (he == ELleftend || (he != ELrightend && right_of(he, p)))
        {
            // keep going right on the list until either the end is reached, or
            // you find the 1st edge which the point isn't to the right of
            do
            {
                he = he.ELright;
            } while (he != ELrightend && right_of(he, p));
            he = he.ELleft;
        } else
        // if the point is to the left of the HalfEdge, then search left for
        // the HE just to the left of the point
        {
            do
            {
                he = he.ELleft;
            } while (he != ELleftend && !right_of(he, p));
        }

        /* Update hash table and reference counts */
        if (bucket > 0 && bucket < ELhashsize - 1)
        {
            ELhash[bucket] = he;
        }
        return (he);
    }

    private void pushGraphEdge(Site leftSite, Site rightSite, double x1, double y1, double x2, double y2)
    {
        GraphEdge newEdge = new GraphEdge();
        allEdges.add(newEdge);
        newEdge.x1 = x1;
        newEdge.y1 = y1;
        newEdge.x2 = x2;
        newEdge.y2 = y2;

        newEdge.site1 = leftSite.sitenbr;
        newEdge.site2 = rightSite.sitenbr;
    }

    private void clip_line(Edge e)
    {
        double pxmin, pxmax, pymin, pymax;
        Site s1, s2;
        double x1 = 0, x2 = 0, y1 = 0, y2 = 0;

        x1 = e.reg[0].coord.x;
        x2 = e.reg[1].coord.x;
        y1 = e.reg[0].coord.y;
        y2 = e.reg[1].coord.y;

        // if the distance between the two points this line was created from is
        // less than the square root of 2, then ignore it
        if (Math.sqrt(((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1))) < minDistanceBetweenSites)
        {
            return;
        }
        pxmin = borderMinX;
        pxmax = borderMaxX;
        pymin = borderMinY;
        pymax = borderMaxY;

        if (e.a == 1.0 && e.b >= 0.0)
        {
            s1 = e.ep[1];
            s2 = e.ep[0];
        } else
        {
            s1 = e.ep[0];
            s2 = e.ep[1];
        }

        if (e.a == 1.0)
        {
            y1 = pymin;
            if (s1 != null && s1.coord.y > pymin)
            {
                y1 = s1.coord.y;
            }
            if (y1 > pymax)
            {
                y1 = pymax;
            }
            x1 = e.c - e.b * y1;
            y2 = pymax;
            if (s2 != null && s2.coord.y < pymax)
            {
                y2 = s2.coord.y;
            }

            if (y2 < pymin)
            {
                y2 = pymin;
            }
            x2 = (e.c) - (e.b) * y2;
            if (((x1 > pxmax) & (x2 > pxmax)) | ((x1 < pxmin) & (x2 < pxmin)))
            {
                return;
            }
            if (x1 > pxmax)
            {
                x1 = pxmax;
                y1 = (e.c - x1) / e.b;
            }
            if (x1 < pxmin)
            {
                x1 = pxmin;
                y1 = (e.c - x1) / e.b;
            }
            if (x2 > pxmax)
            {
                x2 = pxmax;
                y2 = (e.c - x2) / e.b;
            }
            if (x2 < pxmin)
            {
                x2 = pxmin;
                y2 = (e.c - x2) / e.b;
            }
        } else
        {
            x1 = pxmin;
            if (s1 != null && s1.coord.x > pxmin)
            {
                x1 = s1.coord.x;
            }
            if (x1 > pxmax)
            {
                x1 = pxmax;
            }
            y1 = e.c - e.a * x1;
            x2 = pxmax;
            if (s2 != null && s2.coord.x < pxmax)
            {
                x2 = s2.coord.x;
            }
            if (x2 < pxmin)
            {
                x2 = pxmin;
            }
            y2 = e.c - e.a * x2;
            if (((y1 > pymax) & (y2 > pymax)) | ((y1 < pymin) & (y2 < pymin)))
            {
                return;
            }
            if (y1 > pymax)
            {
                y1 = pymax;
                x1 = (e.c - y1) / e.a;
            }
            if (y1 < pymin)
            {
                y1 = pymin;
                x1 = (e.c - y1) / e.a;
            }
            if (y2 > pymax)
            {
                y2 = pymax;
                x2 = (e.c - y2) / e.a;
            }
            if (y2 < pymin)
            {
                y2 = pymin;
                x2 = (e.c - y2) / e.a;
            }
        }

        pushGraphEdge(e.reg[0], e.reg[1], x1, y1, x2, y2);
    }

    private void endpoint(Edge e, int lr, Site s)
    {
        e.ep[lr] = s;
        if (e.ep[RE - lr] == null)
        {
            return;
        }
        clip_line(e);
    }

    /* returns 1 if p is to right of halfedge e */
    private boolean right_of(Halfedge el, Point p)
    {
        Edge e;
        Site topsite;
        boolean right_of_site;
        boolean above, fast;
        double dxp, dyp, dxs, t1, t2, t3, yl;

        e = el.ELedge;
        topsite = e.reg[1];
        if (p.x > topsite.coord.x)
        {
            right_of_site = true;
        } else
        {
            right_of_site = false;
        }
        if (right_of_site && el.ELpm == LE)
        {
            return (true);
        }
        if (!right_of_site && el.ELpm == RE)
        {
            return (false);
        }

        if (e.a == 1.0)
        {
            dyp = p.y - topsite.coord.y;
            dxp = p.x - topsite.coord.x;
            fast = false;
            if ((!right_of_site & (e.b < 0.0)) | (right_of_site & (e.b >= 0.0)))
            {
                above = dyp >= e.b * dxp;
                fast = above;
            } else
            {
                above = p.x + p.y * e.b > e.c;
                if (e.b < 0.0)
                {
                    above = !above;
                }
                if (!above)
                {
                    fast = true;
                }
            }
            if (!fast)
            {
                dxs = topsite.coord.x - (e.reg[0]).coord.x;
                above = e.b * (dxp * dxp - dyp * dyp) < dxs * dyp
                        * (1.0 + 2.0 * dxp / dxs + e.b * e.b);
                if (e.b < 0.0)
                {
                    above = !above;
                }
            }
        } else /* e.b==1.0 */

        {
            yl = e.c - e.a * p.x;
            t1 = p.y - yl;
            t2 = p.x - topsite.coord.x;
            t3 = yl - topsite.coord.y;
            above = t1 * t1 > t2 * t2 + t3 * t3;
        }
        return (el.ELpm == LE ? above : !above);
    }

    private Site rightreg(Halfedge he)
    {
        if (he.ELedge == (Edge) null)
        // if this halfedge has no edge, return the bottom site (whatever
        // that is)
        {
            return (bottomsite);
        }

        // if the ELpm field is zero, return the site 0 that this edge bisects,
        // otherwise return site number 1
        return (he.ELpm == LE ? he.ELedge.reg[RE] : he.ELedge.reg[LE]);
    }

    private double dist(Site s, Site t)
    {
        double dx, dy;
        dx = s.coord.x - t.coord.x;
        dy = s.coord.y - t.coord.y;
        return (double) (Math.sqrt(dx * dx + dy * dy));
    }

    // create a new site where the HalfEdges el1 and el2 intersect - note that
    // the Point in the argument list is not used, don't know why it's there
    private Site intersect(Halfedge el1, Halfedge el2)
    {
        Edge e1, e2, e;
        Halfedge el;
        double d, xint, yint;
        boolean right_of_site;
        Site v;

        e1 = el1.ELedge;
        e2 = el2.ELedge;
        if (e1 == null || e2 == null)
        {
            return null;
        }

        // if the two edges bisect the same parent, return null
        if (e1.reg[1] == e2.reg[1])
        {
            return null;
        }

        d = e1.a * e2.b - e1.b * e2.a;
        if (-1.0e-10 < d && d < 1.0e-10)
        {
            return null;
        }

        xint = (e1.c * e2.b - e2.c * e1.b) / d;
        yint = (e2.c * e1.a - e1.c * e2.a) / d;

        if ((e1.reg[1].coord.y < e2.reg[1].coord.y)
                || (e1.reg[1].coord.y == e2.reg[1].coord.y && e1.reg[1].coord.x < e2.reg[1].coord.x))
        {
            el = el1;
            e = e1;
        } else
        {
            el = el2;
            e = e2;
        }

        right_of_site = xint >= e.reg[1].coord.x;
        if ((right_of_site && el.ELpm == LE)
                || (!right_of_site && el.ELpm == RE))
        {
            return null;
        }

        // create a new site at the point of intersection - this is a new vector
        // event waiting to happen
        v = new Site();
        v.coord.x = xint;
        v.coord.y = yint;
        return (v);
    }

    /*
     * implicit parameters: nsites, sqrt_nsites, xmin, xmax, ymin, ymax, deltax,
     * deltay (can all be estimates). Performance suffers if they are wrong;
     * better to make nsites, deltax, and deltay too big than too small. (?)
     */
    private boolean voronoi_bd()
    {
        Site newsite, bot, top, temp, p;
        Site v;
        Point newintstar = null;
        int pm;
        Halfedge lbnd, rbnd, llbnd, rrbnd, bisector;
        Edge e;

        PQinitialize();
        ELinitialize();

        bottomsite = nextone();
        newsite = nextone();
        while (true)
        {
            if (!PQempty())
            {
                newintstar = PQ_min();
            }
            // if the lowest site has a smaller y value than the lowest vector
            // intersection,
            // process the site otherwise process the vector intersection

            if (newsite != null
                    && (PQempty() || newsite.coord.y < newintstar.y || (newsite.coord.y == newintstar.y && newsite.coord.x < newintstar.x)))
            {
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
                if ((p = intersect(lbnd, bisector)) != null)
                {
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
                if ((p = intersect(bisector, rbnd)) != null)
                {
                    // push the HE into the ordered linked list of vertices
                    PQinsert(bisector, p, dist(p, newsite));
                }
                newsite = nextone();
            } else if (!PQempty())
            /* intersection is smallest - this is a vector event */
            {
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
                if ((p = intersect(llbnd, bisector)) != null)
                {
                    PQdelete(llbnd);
                    PQinsert(llbnd, p, dist(p, bot));
                }

                // if right HE and the new bisector intersect, then
                // reinsert it
                if ((p = intersect(bisector, rrbnd)) != null)
                {
                    PQinsert(bisector, p, dist(p, bot));
                }
            } else
            {
                break;
            }
        }

        for (lbnd = ELright(ELleftend); lbnd != ELrightend; lbnd = ELright(lbnd))
        {
            e = lbnd.ELedge;
            clip_line(e);
        }

        return true;
    }
}


/**
 * This file is part of DrawbotGUI.
 *
 * DrawbotGUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * DrawbotGUI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */