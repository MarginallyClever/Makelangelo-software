package com.marginallyclever.makelangeloRobot.converters;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.batik.ext.awt.geom.Polygon2D;

import com.jogamp.opengl.GL2;
import com.marginallyclever.makelangelo.Log;
import com.marginallyclever.makelangeloRobot.TransformedImage;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.MakelangeloRobotDecorator;
import com.marginallyclever.makelangeloRobot.imageFilters.Filter_BlackAndWhite;
import com.marginallyclever.makelangeloRobot.settings.MakelangeloRobotSettings;
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
	private VoronoiCell[] cells = new VoronoiCell[1];
	private TransformedImage sourceImage;
	private List<VoronoiGraphEdge> graphEdges = null;
	private static int numCells = 1000;
	private static float maxDotSize = 5.0f;
	private static float minDotSize = 1.0f;
	private double[] xValuesIn = null;
	private double[] yValuesIn = null;
	private float yBottom, yTop, xLeft, xRight;

	
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
		
		yBottom = (float)machine.getPaperBottom() * (float)machine.getPaperMargin();
		yTop    = (float)machine.getPaperTop()    * (float)machine.getPaperMargin();
		xLeft   = (float)machine.getPaperLeft()   * (float)machine.getPaperMargin();
		xRight  = (float)machine.getPaperRight()  * (float)machine.getPaperMargin();

		keepIterating=true;
		restart();
	}
	
	public boolean iterate() {
		//float totalMagnitude = 
				evolveCells();
		//System.out.println(totalMagnitude+"\t"+numCells+"\t"+(totalMagnitude/(float)numCells));
		return keepIterating;
	}
	
	public void restart() {
		if(!keepIterating) {
			loadAndSave.reconvert();
			return;
		}

		while(lock.isLocked());
		lock.lock();
		keepIterating=true;
		initializeCells(0.01);
		lock.unlock();
	}

	public void finish(Writer out) throws IOException {
		keepIterating=false;
		writeOutCells(out);
	}

	@Override
	public void render(GL2 gl2, MakelangeloRobotSettings settings) {
		super.render(gl2, settings);
		
		if (graphEdges == null) return;

		while(lock.isLocked());
		lock.lock();
		
		// draw cell edges
		gl2.glColor3f(0.9f, 0.9f, 0.9f);
		gl2.glBegin(GL2.GL_LINES);
		for (VoronoiGraphEdge e : graphEdges) {
			gl2.glVertex2d( e.x1, e.y1 );
			gl2.glVertex2d( e.x2, e.y2 );
		}
		gl2.glEnd();

		// draw bounds
		gl2.glColor3f(1,0,0);
		gl2.glBegin(GL2.GL_LINE_LOOP);
		Rectangle2D bounds = cells[0].region.getBounds2D();
		if(bounds!=null) {
			gl2.glVertex2d( bounds.getMinX(),bounds.getMinY() );
			gl2.glVertex2d( bounds.getMaxX(),bounds.getMinY() );
			gl2.glVertex2d( bounds.getMaxX(),bounds.getMaxY() );
			gl2.glVertex2d( bounds.getMinX(),bounds.getMaxY() );
		}
		gl2.glEnd();

		// draw cell centers
		//gl2.glPointSize(3);
		gl2.glColor3f(0, 0, 0);
		for (VoronoiCell c : cells) {
			float x = (float)c.centroid.getX();
			float y = (float)c.centroid.getY();
			//if( sourceImage.canSampleAt(x,y) ) 
			{
				float val = (float)c.weight/255.0f;//1.0f - (sourceImage.sample1x1( x, y) / 255.0f);
				float r = (val * maxDotSize);
				if(r<minDotSize) continue;
				gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				for (float j = 0; j < Math.PI * 2; j += (Math.PI / 4)) {
					gl2.glVertex2d(x + Math.cos(j) * r,
								   y + Math.sin(j) * r);
				}
				gl2.glEnd();
			}
		}//*/

		lock.unlock();
	}

	// set some starting points in a grid
	protected void initializeCells(double minDistanceBetweenSites) {
		Log.info("green","Initializing cells");

		cells = new VoronoiCell[numCells];
		// convert the cells to sites used in the Voronoi class.
		xValuesIn = new double[numCells];
		yValuesIn = new double[numCells];

		// from top to bottom of the margin area...
		int used;
		for (used=0;used<cells.length;++used) {
			cells[used] = new VoronoiCell();
		}
		
		used=0;
		while(used<cells.length) {
			Point2D p = new Point2D.Double(
					xLeft   + Math.random()*(xRight-xLeft),
					yBottom + Math.random()*(yTop-yBottom)
					);
			if(sourceImage.canSampleAt((float)p.getX(), (float)p.getY())) {
				float v = sourceImage.sample1x1Unchecked((float)p.getX(), (float)p.getY());
				if(Math.random()*256> v) {
					cells[used].centroid.setLocation(p);
					cells[used].oldCentroid.setLocation(cells[used].centroid);
					++used;
				}
			}
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
	 * @param out where to write
	 * @throws IOException
	 */
	protected void writeOutCells(Writer out) throws IOException {
		Log.info("Writing gcode.");

		imageStart(out);
		liftPen(out);
		machine.writeChangeToDefaultColor(out);

		float toolDiameter = machine.getPenDiameter();

		Arrays.sort(cells);
		
		int i;
		for (i = 0; i < cells.length; ++i) {
			double x = cells[i].centroid.getX();
			double y = cells[i].centroid.getY();
			double val = cells[i].weight/255.0f;
			double r = val * maxDotSize;
			if (r < minDotSize) continue;

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
							moveTo(out, newX, newY, true);
							lowerPen(out);
							first=false;
						}
					} else {
						moveTo(out, newX, newY, false);
					}
				}
				r -= toolDiameter;
			}
			if(first==false) {
				liftPen(out);
			}
		}
		
		liftPen(out);
	    moveTo(out, (float)machine.getHomeX(), (float)machine.getHomeY(),true);
	}



	/**
	 *  I have a set of points.  I want a list of cell borders.
	 *  cell borders are halfway between any point and it's nearest neighbors.
	 */
	protected void tessellateVoronoiDiagram() {
		
		int i;
		for (i = 0; i < cells.length; ++i) {
			xValuesIn[i] = cells[i].centroid.getX();
			yValuesIn[i] = cells[i].centroid.getY();
			cells[i].region = new Polygon2D();
		}

		// scan left to right across the image, building the list of borders as we go.
		graphEdges = voronoiTesselator.generateVoronoi(xValuesIn, yValuesIn, xLeft, xRight, yBottom, yTop);
		
		for (VoronoiGraphEdge e : graphEdges) {
			try {
				cells[e.site1].region.addPoint((float)e.x1, (float)e.y1);
				cells[e.site1].region.addPoint((float)e.x2, (float)e.y2);
				cells[e.site2].region.addPoint((float)e.x1, (float)e.y1);
				cells[e.site2].region.addPoint((float)e.x2, (float)e.y2);
			} catch(Exception err) {
				err.printStackTrace();
			}
		}
		
	}


	/**
	 * Find the weighted center of each cell.
	 * weight is based on the intensity of the color of each pixel inside the cell
	 * the center of the pixel must be inside the cell to be counted.
	 * @return the total magnitude movement of all centers
	 */
	protected float adjustCentroids() {
		double totalCellWeight, wx, wy, x, y;
		double stepSize;
		double minX,maxX,minY,maxY;
		double xDiff, yDiff, maxSize,minSize,scaleFactor;
		float totalMagnitude=0;
		double cellBuffer=100;

		int i=0;
		for (VoronoiCell c : cells) {
			if(c.region.npoints ==0) continue;
			Rectangle bounds = c.region.getBounds();
			
			minX = bounds.getMinX();
			minY = bounds.getMinY();
			maxX = bounds.getMaxX();
			maxY = bounds.getMaxY();
			
			xDiff = maxX-minX;
			yDiff = maxY-minY;
			if(minX==maxX) xDiff=1;
			if(minY==maxY) yDiff=1;
			maxSize = Math.max(xDiff,yDiff);
			minSize = Math.min(xDiff,yDiff);
			stepSize=1.0;
			
			scaleFactor=1.0;
			while(maxSize > cellBuffer) {
				scaleFactor *= 0.5;
				maxSize *= 0.5;
			}
			while(maxSize < (cellBuffer / 2)) {
				scaleFactor *= 2;
				maxSize *= 2;
			}
			if ((minSize * scaleFactor) > (cellBuffer/2)) {
				// Special correction for objects of near-unity (square-like) aspect ratio,
				// which have larger area *and* where it is less essential to find the exact centroid:
				scaleFactor *= 0.5;
			}

			if(i==0) {
				System.out.println((maxX-minX)+"\t"+(maxY-minY)+"\t"+stepSize);
			}

			stepSize = (1.0/scaleFactor);
			//stepSize = maxSize / cellBuffer;
			assert(stepSize>0 && stepSize<cellBuffer);

			int hits = 0;
			totalCellWeight = 0;
			wx = 0;
			wy = 0;

			float sampleWeight;
			for (y = minY; y <= maxY; y += stepSize) {
				for (x = minX; x <= maxX; x += stepSize) {
					if (c.region.contains(x,y)) {
						hits++;
						if(sourceImage.canSampleAt((float)x, (float)y)) {
							sampleWeight = 255.001f - ( (float)sourceImage.sample1x1Unchecked( (float)x, (float)y ) );
						} else sampleWeight = 0.001f; 
					} else sampleWeight = 0.001f;
					totalCellWeight += sampleWeight;
					wx += x * sampleWeight;
					wy += y * sampleWeight;
				}
			}

			if (totalCellWeight > 0) {
				wx /= totalCellWeight;
				wy /= totalCellWeight;
				totalMagnitude+=totalCellWeight;
			}
/*
			// make sure centroid can't leave image bounds
			if (wx <  xLeft ) wx = xLeft;
			if (wx >= xRight) wx = xRight;
			
			if (wy <  yBottom) wy = yBottom;
			if (wy >= yTop   ) wy = yTop;
*/
			// use the new center
			c.oldCentroid.setLocation(c.centroid);
			//if(hits>1)
			{
				c.centroid.setLocation(wx, wy);
				c.weight = totalCellWeight/(double)hits;
			}
			i++;
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
	
	public float getMaxDotSize() {
		return maxDotSize;
	}
	public void setMaxDotSize(float value) {
		if(value<0.01) value=0.01f;
		maxDotSize = value;
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
