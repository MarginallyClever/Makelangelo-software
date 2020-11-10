package com.marginallyclever.artPipeline.converters;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.jogamp.opengl.GL2;
import com.marginallyclever.artPipeline.TransformedImage;
import com.marginallyclever.artPipeline.imageFilters.Filter_BlackAndWhite;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.convenience.Turtle;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.MakelangeloRobotDecorator;
import com.marginallyclever.voronoi.VoronoiCell;
import com.marginallyclever.voronoi.VoronoiGraphEdge;
import com.marginallyclever.voronoi.VoronoiTesselator;


/**
 * Dithering using a particle system
 *
 * @author Dan
 *         http://en.wikipedia.org/wiki/Fortune%27s_algorithm
 *         http://skynet.ie/~sos/mapviewer/voronoi.php
 * @since 7.0.0?
 */
public class Converter_VoronoiStippling extends ImageConverter implements MakelangeloRobotDecorator {
	private ReentrantLock lock = new ReentrantLock();

	private VoronoiTesselator voronoiTesselator = new VoronoiTesselator();
	private ArrayList<VoronoiCell> cells = new ArrayList<VoronoiCell>();
	private List<VoronoiGraphEdge> graphEdges = null;
	private static boolean drawBorders = true;
	private static int numCells = 1000;
	private static float maxDotSize = 5.0f;
	private static float minDotSize = 1.0f;
	private static float cutoff = 0;
	private double[] xValuesIn = null;
	private double[] yValuesIn = null;
	private double yMin, yMax;
	private double xMin, xMax;
	private int iterations;

	protected class QuadGraph {
		public Rectangle2D bounds;
		public ArrayList<VoronoiCell> sites;
		public QuadGraph[] children;
		public boolean flag;
		
		public QuadGraph(double x,double y,double x2,double y2) {
			bounds = new Rectangle2D.Double(x,y,0,0);
			bounds.add(x2,y2);
			sites = new ArrayList<VoronoiCell>();
			children=null;
			flag=false;
		}
		
		public void split(int depth) {
			if(depth<1) return;

			children = new QuadGraph[4];

		    children[0] = new QuadGraph(bounds.getMinX(), bounds.getMinY(), bounds.getCenterX(),bounds.getCenterY());
		    children[1] = new QuadGraph(bounds.getMaxX(), bounds.getMinY(), bounds.getCenterX(),bounds.getCenterY());
		    children[2] = new QuadGraph(bounds.getMaxX(), bounds.getMaxY(), bounds.getCenterX(),bounds.getCenterY());
		    children[3] = new QuadGraph(bounds.getMinX(), bounds.getMaxY(), bounds.getCenterX(),bounds.getCenterY());
		    
			for(int i=0;i<4;++i) {
				children[i].split(depth-1);
			}
		}
		
		public boolean insert(VoronoiCell e,int maxDepth) {
			if(maxDepth>0) {
				if(children==null) {
					split(1);
				}
				
				for(int i=0;i<4;++i) {
					QuadGraph child = children[i]; 
					if( child.bounds.contains(e.region) ) {
						// this cell fits entirely within this child
						if( child.insert(e,maxDepth-1) ) return true;
					}
				}
			}
			// did not fit in any one child.  add it here.
			sites.add(e);
			return true;
		}
		
		public double cellToPointSq(Rectangle2D area,VoronoiCell c) {
			double x = area.getCenterX() - c.centroid.x;
			double y = area.getCenterY() - c.centroid.y;
			return x*x + y*y;
		}
		
		// locate the cell under point x,y
		public VoronoiCell find(Rectangle2D area) {
			if(area.intersects(bounds)) {
				VoronoiCell bestFound = null;
				
				if(children!=null) {
					for(int i=0;i<4;++i) {
						// look into the children
						VoronoiCell bestChildFound = children[i].find(area);
						if(bestChildFound!=null) {
							if( bestFound==null ||
								cellToPointSq(area,bestFound) > cellToPointSq(area,bestChildFound) )
							{
								bestFound = bestChildFound;
							}
						}
					}
				}
				// it wasn't in a child, maybe it's in this node.
				// Is this target point inside the graph edges?
				if(!sites.isEmpty()) {
					double bestD = Double.MAX_VALUE;
					VoronoiCell bestCell = null;
					Iterator<VoronoiCell> si = sites.iterator();
					while(si.hasNext()) {
						VoronoiCell c = si.next();
						double d = cellToPointSq(area,c);
						if(bestD>d) {
							bestD = d;
							bestCell = c;
						}
					}
					if( bestFound==null ||
							cellToPointSq(area,bestFound) > cellToPointSq(area,bestCell) ) {
						bestFound = bestCell;
					}
				}
				return bestFound;
			}
			// nothing found?!
			return null;
		}
		
		void render(GL2 gl2) {
			if(children!=null) {
				for(int i=0;i<4;++i) {
					children[i].render(gl2);
				}
				return;
			} else {
				gl2.glColor3f(1, 0, 1);
				gl2.glBegin(GL2.GL_LINE_LOOP);
				gl2.glVertex2d(bounds.getMinX(), bounds.getMinY());
				gl2.glVertex2d(bounds.getMinX(), bounds.getMaxY());
				gl2.glVertex2d(bounds.getMaxX(), bounds.getMaxY());
				gl2.glVertex2d(bounds.getMaxX(), bounds.getMinY());
				gl2.glEnd();
			}
		}
	}
	
	protected QuadGraph tree;
	
	@Override
	public String getName() {
		return Translator.get("voronoiStipplingName");
	}

	@Override
	public ImageConverterPanel getPanel() {
		return new Converter_VoronoiStippling_Panel(this);
	}

	@Override
	public void setImage(TransformedImage img) {
		// make black & white
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		sourceImage = bw.filter(img);
		
		yMin = machine.getMarginBottom();
		yMax = machine.getMarginTop();
		xMin = machine.getMarginLeft();
		xMax = machine.getMarginRight();

		keepIterating=true;
		restart();
	}
	
	public boolean iterate() {
		//float totalMagnitude = 
				evolveCells();
		//Log.message(totalMagnitude+"\t"+numCells+"\t"+(totalMagnitude/(float)numCells));
		return keepIterating;
	}
	
	public void restart() {
		if(!keepIterating) {
			loadAndSaveImage.reconvert();
			return;
		}
		
		while(lock.isLocked());
		lock.lock();
		iterations=0;
		keepIterating=true;
		initializeCells(0.5);
		lock.unlock();
	}

	@Override
	public void finish() {
		keepIterating=false;
		writeOutCells();
	}

	@Override
	public void render(GL2 gl2) {
		super.render(gl2);
		
		while(lock.isLocked());
		lock.lock();
		
		// draw cell edges
		if(drawBorders) {/*
			if(graphEdges != null) {
				gl2.glColor3f(0.9f, 0.0f, 0.0f);
				gl2.glBegin(GL2.GL_LINES);
				for (VoronoiGraphEdge e : graphEdges) {
					gl2.glVertex2d( e.x1, e.y1 );
					gl2.glVertex2d( e.x2, e.y2 );
				}
				gl2.glEnd();
			}*/
			if(tree!=null) tree.render(gl2);
		}

		//enderPolygons(gl2);
		//renderFirstCellBounds(gl2);  // bounds of first cell
		renderDots(gl2);  // dots sized by darkness
		//renderPoints(gl2);  // tiny points
		
		lock.unlock();
	}

	protected void renderPolygons(GL2 gl2) {
		gl2.glColor4d(1,1,0,0.25);
		Iterator<VoronoiCell> ci = cells.iterator();
		while(ci.hasNext()) {
			VoronoiCell c = ci.next();
			if(c.region==null) continue;
			//gl2.glBegin(GL2.GL_TRIANGLE_FAN);
			gl2.glBegin(GL2.GL_LINE_LOOP);
			gl2.glVertex2d( c.region.getMinX(),c.region.getMinY());
			gl2.glVertex2d( c.region.getMaxX(),c.region.getMinY());
			gl2.glVertex2d( c.region.getMaxX(),c.region.getMaxY());
			gl2.glVertex2d( c.region.getMinX(),c.region.getMaxY());
			//for(int i=0;i<c.region.npoints;++i) {
			//	gl2.glVertex2d( c.region.xpoints[i],c.region.ypoints[i] );
			//}
			gl2.glEnd();
			return;
		}
	}
	
	protected void renderFirstCellBounds(GL2 gl2) {
		gl2.glColor3f(1,0,0);
		gl2.glBegin(GL2.GL_LINE_LOOP);
		Rectangle2D bounds = cells.get(0).region.getBounds2D();
		if(bounds!=null) {
			gl2.glVertex2d( bounds.getMinX(),bounds.getMinY() );
			gl2.glVertex2d( bounds.getMaxX(),bounds.getMinY() );
			gl2.glVertex2d( bounds.getMaxX(),bounds.getMaxY() );
			gl2.glVertex2d( bounds.getMinX(),bounds.getMaxY() );
		}
		gl2.glEnd();
	}
	
	protected void renderPoints(GL2 gl2) {
		gl2.glColor3f(0, 0, 0);
		gl2.glBegin(GL2.GL_POINTS);
		Iterator<VoronoiCell> ci = cells.iterator();
		while(ci.hasNext()) {
			VoronoiCell c = ci.next();
			Point2D p = c.centroid;
			gl2.glVertex2d(p.x,p.y);
		}
		gl2.glEnd();
	}
	
	protected void renderDots(GL2 gl2) {
		float scale = maxDotSize - minDotSize;
		gl2.glColor3f(0, 0, 0);
		Iterator<VoronoiCell> ci = cells.iterator();
		while(ci.hasNext()) {
			VoronoiCell c = ci.next();
			double x = c.centroid.x;
			double y = c.centroid.y;
			double val = c.weight/255.0;
			if(val>cutoff) {
				double r = val * scale;
				gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				for (float j = 0; j < Math.PI * 2; j += (Math.PI / 6)) {
					gl2.glVertex2d(x + Math.cos(j) * r,
								   y + Math.sin(j) * r);
				}
				gl2.glEnd();
			}
		}
	}
	
	// set some starting points in a grid
	protected void initializeCells(double minDistanceBetweenSites) {
		Log.message("Initializing cells");

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
				x = xMin + Math.random()*(xMax-xMin);
				y = yMin + Math.random()*(yMax-yMin);
				if(sourceImage.canSampleAt((float)x, (float)y)) {
					float v = sourceImage.sample1x1Unchecked((float)x, (float)y);
					if(Math.random()*255 >= v) break;
				}
			}
			c.centroid.set(x,y);
			cells.add(c);
		}


		voronoiTesselator.Init(minDistanceBetweenSites);
	}


	/**
	 * Jiggle the dots until they make a nice picture
	 */
	protected float evolveCells() {
		float totalMagnitude=0;
		try {
			while(lock.isLocked());
			lock.lock();
			tessellateVoronoiDiagram();
			totalMagnitude=adjustCentroids();
			lock.unlock();
		} catch (Exception e) {
			e.printStackTrace();
			if(lock.isHeldByCurrentThread() && lock.isLocked()) {
				lock.unlock();
			}
		}
		return totalMagnitude;
	}


	/**
	 * write cell centroids to gcode.
	 */
	protected void writeOutCells() {
		turtle = new Turtle();

		float toolDiameter = machine.getPenDiameter();

		Iterator<VoronoiCell> ci = cells.iterator();
		while(ci.hasNext()) {
			VoronoiCell c = ci.next();
			double x = c.centroid.x;
			double y = c.centroid.y;
			double val = c.weight/255.0f;
			if(val<cutoff) continue;

			double r = val * (maxDotSize-minDotSize);

			double newX=0,newY=0;
			boolean first=true;
			// filled circles
			while (r > 0) {
				float detail = (float)Math.ceil(Math.PI * r*2 / (toolDiameter*4));
				if (detail < 4) detail = 4;
				if (detail > 20) detail = 20;
				for (float j = 0; j <= detail; ++j) {
					double v = Math.PI * 2.0f * j / detail;
					newX = x + r * Math.cos(v);
					newY = y + r * Math.sin(v);
					if(first) {
						if(isInsidePaperMargins(newX,newY)) {
							turtle.jumpTo(newX, newY);
							first=false;
						}
					} else {
						turtle.moveTo(newX, newY);
					}
				}
				r -= toolDiameter;
			}
			if(first==false) {
				turtle.penUp();
			}
		}
	}


	/**
	 *  I have a set of points.  I want a list of cell borders.
	 *  cell borders are halfway between any point and it's nearest neighbors.
	 */
	protected void tessellateVoronoiDiagram() {
		iterations++;

		int i=0;
		Iterator<VoronoiCell> ci = cells.iterator();
		while(ci.hasNext()) {
			VoronoiCell c = ci.next();
			xValuesIn[i] = c.centroid.x;
			yValuesIn[i] = c.centroid.y;
			i++;
			c.resetRegion();
		}

		// scan left to right across the image, building the list of borders as we go.
		graphEdges = voronoiTesselator.generateVoronoi(xValuesIn, yValuesIn, xMin, xMax, yMin, yMax);
		
		Iterator<VoronoiGraphEdge> v = graphEdges.iterator();
		while(v.hasNext()) {
			VoronoiGraphEdge e = v.next();
			VoronoiCell a = cells.get(e.site1);
			a.addPoint(e.x1, e.y1);
			a.addPoint(e.x2, e.y2);
			VoronoiCell b = cells.get(e.site1);
			b.addPoint(e.x1, e.y1);
			b.addPoint(e.x2, e.y2);
		}
	}


	protected void adjustCell(VoronoiCell c,double x,double y) {
		c.hits++;
		double sampleWeight = 255.0 - sourceImage.sample1x1Unchecked( (float)x, (float)y );
		c.weight += sampleWeight;
		c.wx += x * sampleWeight;
		c.wy += y * sampleWeight;
	}

	/**
	 * Find the weighted center of each cell.
	 * weight is based on the intensity of the color of each pixel inside the cell
	 * the center of the pixel must be inside the cell to be counted.
	 * @return the total magnitude movement of all centers
	 */
	protected float adjustCentroids() {
		double x, y;
		float totalMagnitude=0;
		
		tree = new QuadGraph(xMin,yMin,xMax,yMax);
		tree.split(4);
		
		int fails=0;
		Iterator<VoronoiCell> ci = cells.iterator();
		while(ci.hasNext()) {
			VoronoiCell c = ci.next();
			if(c.region==null) continue;
			boolean ok = tree.insert(c,8);
			if(!ok) {
				fails++;
			}
		}
		if( fails>0 ) {
			Log.message(iterations+" failed "+fails+" times");
		}

		Rectangle2D test = new Rectangle2D.Double();
		double v=5;
		for(y=yMin; y<=yMax; ++y) {			
			for(x=xMin; x<=xMax; ++x) {
				if(!sourceImage.canSampleAt((float)x, (float)y)) continue;
				// binary search over the cells to find the best fit.
				
				test.setRect(x-v, y-v, v*2, v*2);
				VoronoiCell bestCell = tree.find(test);
				if(bestCell!=null) {
					adjustCell(bestCell,x,y);
				} else {
					// this x,y at an empty quadtree cell.
				}
			}
		}
		
		double w = Math.pow(iterations,-0.8);


		ci = cells.iterator();
		while(ci.hasNext()) {
			VoronoiCell c = ci.next();
			
			if (c.hits>0 && c.weight > 0) {
				c.wx /= c.weight;
				c.wy /= c.weight;
			} else {
				c.weight=1;
				c.hits=1;
				c.wx = c.centroid.x;
				c.wy = c.centroid.y;
			}

			// make sure centroid can't leave image bounds
			if (c.wx < xMin || c.wx >= xMax || c.wy < yMin || c.wy >= yMax ) {
				if (c.wx <  xMin ) c.wx = xMin+1;
				if (c.wx >= xMax) c.wx = xMax-1;
				
				if (c.wy <  yMin) c.wy = yMin+1;
				if (c.wy >= yMax   ) c.wy = yMax-1;
			}
			
			double ox = c.centroid.x;
			double oy = c.centroid.y;
			double dx2 = (c.wx - ox) * 0.25;
			double dy2 = (c.wy - oy) * 0.25;

			c.weight /= (double)c.hits;
			totalMagnitude += Math.abs(dx2) + Math.abs(dy2);
			
			double nx = ox + dx2 + (Math.random()-0.5) * w;
			double ny = oy + dy2 + (Math.random()-0.5) * w;
			
			c.centroid.set(nx, ny);
		}
		return totalMagnitude;
	}

	public void setNumCells(int value) {
		if(value<1) value=1;
		if(numCells!=value) {
			numCells = value;
			if(keepIterating) {
				restart();
			}
		}
	}
	public int getNumCells() {
		return numCells;
	}
	
	public void setMinDotSize(float value) {
		if(value<0.001) value=0.001f;
		minDotSize = value;
	}
	public float getMinDotSize() {
		return minDotSize;
	}
	
	public void setCutoff(float value) {
		if(value<0f) value=0f;
		cutoff = value;
	}
	public float getCutoff() {
		return cutoff;
	}
	
	public float getMaxDotSize() {
		return maxDotSize;
	}
	public void setMaxDotSize(float value) {
		if(value<=minDotSize) value=minDotSize+1;
		maxDotSize = value;
	}
	
	public void setDrawBorders(boolean arg0) {
		drawBorders=arg0;
	}
	public boolean getDrawBorders() {
		return drawBorders;
	}
}


/**
 * This file is part of Makelangelo.
 * <p>
 * Makelangelo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Makelangelo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Makelangelo.  If not, see <http://www.gnu.org/licenses/>.
 */
