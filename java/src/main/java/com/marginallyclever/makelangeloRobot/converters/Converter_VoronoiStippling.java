package com.marginallyclever.makelangeloRobot.converters;

import java.awt.Point;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JPanel;
import com.jogamp.opengl.GL2;
import com.marginallyclever.makelangelo.Log;
import com.marginallyclever.makelangeloRobot.TransformedImage;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.MakelangeloRobotDecorator;
import com.marginallyclever.makelangeloRobot.imageFilters.Filter_BlackAndWhite;
import com.marginallyclever.makelangeloRobot.settings.MakelangeloRobotSettings;
import com.marginallyclever.voronoi.VoronoiCell;
import com.marginallyclever.voronoi.VoronoiCellEdge;
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
	private static int numGenerations = 400;
	private static int numCells = 1000;
	private static float maxDotSize = 5.0f;
	private static float minDotSize = 1.0f;
	private Point boundMin = new Point();
	private Point boundMax = new Point();
	private int numEdgesInCell;
	private List<VoronoiCellEdge> cellBorder = null;
	private double[] xValuesIn = null;
	private double[] yValuesIn = null;
	private float yBottom, yTop, xLeft, xRight;

	@Override
	public String getName() {
		return Translator.get("voronoiStipplingName");
	}

	@Override
	public JPanel getPanel() {
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
		keepIterating=true;
		cellBorder = new ArrayList<>();
		initializeCells(0.001);
	}

	public void finish(Writer out) throws IOException {
		keepIterating=false;
		writeOutCells(out);
	}

	@Override
	public void render(GL2 gl2, MakelangeloRobotSettings machine) {
		if (graphEdges == null) return;

		while(lock.isLocked());
		lock.lock();

		gl2.glScalef(0.1f, 0.1f, 1);

		// draw cell edges
		gl2.glColor3f(0.9f, 0.9f, 0.9f);
		gl2.glBegin(GL2.GL_LINES);
		for (VoronoiGraphEdge e : graphEdges) {
			gl2.glVertex2d( e.x1, e.y1 );
			gl2.glVertex2d( e.x2, e.y2 );
		}
		gl2.glEnd();

		// draw cell centers
		gl2.glPointSize(3);
		gl2.glColor3f(0, 0, 0);
		for (VoronoiCell c : cells) {
			float x = (float)c.centroid.getX();
			float y = (float)c.centroid.getY();
			if( sourceImage.canSampleAt(x,y) ) {
				float val = 1.0f - (sourceImage.sample1x1( x, y) / 255.0f);
				float r = (val * maxDotSize);
				if(r<minDotSize) continue;
				gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				for (float j = 0; j < Math.PI * 2; j += (Math.PI / 4)) {
					gl2.glVertex2d(x + Math.cos(j) * r,
								   y + Math.sin(j) * r);
				}
				gl2.glEnd();
			}
		}

		lock.unlock();
	}

	// set some starting points in a grid
	protected void initializeCells(double minDistanceBetweenSites) {
		Log.write("green","Initializing cells");

		cells = new VoronoiCell[numCells];

		// from top to bottom of the margin area...
		float yBottom = (float)machine.getPaperBottom() * (float)machine.getPaperMargin();
		float yTop    = (float)machine.getPaperTop()    * (float)machine.getPaperMargin();
		float xLeft   = (float)machine.getPaperLeft()   * (float)machine.getPaperMargin();
		float xRight  = (float)machine.getPaperRight()  * (float)machine.getPaperMargin();
		
		int used;
		for (used=0;used<numCells;++used) {
			cells[used] = new VoronoiCell();
			cells[used].centroid.setLocation(xLeft   + ((float)Math.random()*(xRight-xLeft)),
											 yBottom + ((float)Math.random()*(yTop-yBottom))
											 );
		}

		// convert the cells to sites used in the Voronoi class.
		xValuesIn = new double[numCells];
		yValuesIn = new double[numCells];

		voronoiTesselator.Init(minDistanceBetweenSites);
	}


	/**
	 * Jiggle the dots until they make a nice picture
	 */
	protected float evolveCells() {
		float totalMagnitude=0;
		try {
			assert !lock.isHeldByCurrentThread();
			lock.lock();
			tessellateVoronoiDiagram();
			lock.unlock();
			totalMagnitude=adjustCentroids();
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
		if (graphEdges == null) return;

		Log.message("Writing gcode.");

		imageStart(out);
		liftPen(out);
		machine.writeChangeTo(out);

		float toolDiameter = machine.getPenDiameter();

		Arrays.sort(cells);
		
		int i;
		for (i = 0; i < cells.length; ++i) {
			float x = cells[i].centroid.x;
			float y = cells[i].centroid.y;
			float val = 1.0f - (sourceImage.sample1x1(x,y) / 255.0f);
			float r = val * maxDotSize;
			if (r < minDotSize) continue;

			float newX=0,newY=0;
			boolean first=true;
			// filled circles
			while (r > 0) {
				float detail = (float)Math.ceil(Math.PI * r*2 / (toolDiameter*4));
				if (detail < 4) detail = 4;
				if (detail > 20) detail = 20;
				for (float j = 0; j <= detail; ++j) {
					double v = Math.PI * 2.0f * j / detail;
					newX = x + r * (float) Math.cos(v);
					newY = y + r * (float) Math.sin(v);
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
		// convert the cells to sites used in the Voronoi class.
		int i;
		for (i = 0; i < cells.length; ++i) {
			xValuesIn[i] = cells[i].centroid.x;
			yValuesIn[i] = cells[i].centroid.y;
		}

		// scan left to right across the image, building the list of borders as we go.
		graphEdges = voronoiTesselator.generateVoronoi(xValuesIn, yValuesIn, xLeft, xRight, yBottom, yTop);
	}


	protected void generateBounds(int cellIndex) {
		numEdgesInCell = 0;

		float cx = cells[cellIndex].centroid.x;
		float cy = cells[cellIndex].centroid.y;

		double dx, dy, nx, ny, dot1;

		//long ta = System.nanoTime();

		for (VoronoiGraphEdge e : graphEdges) {
			if (e.site1 != cellIndex && e.site2 != cellIndex) continue;
			if (numEdgesInCell == 0) {
				if (e.x1 < e.x2) {
					boundMin.setLocation( e.x1, boundMin.getY() );
					boundMax.setLocation( e.x2, boundMax.getY() );
				} else {
					boundMin.setLocation( e.x2, boundMin.getY() );
					boundMax.setLocation( e.x1, boundMax.getY() );
				}
				if (e.y1 < e.y2) {
					boundMin.setLocation( boundMin.getX(), (float) e.y1 );
					boundMax.setLocation( boundMax.getX(), (float) e.y2 );
				} else {
					boundMin.setLocation( boundMin.getX(), (float) e.y2 );
					boundMax.setLocation( boundMax.getX(), (float) e.y1 );
				}
			} else {
				if (boundMin.x > e.x1) boundMin.setLocation( e.x1, boundMin.getY() );
				if (boundMin.x > e.x2) boundMin.setLocation( e.x2, boundMin.getY() );
				if (boundMax.x < e.x1) boundMax.setLocation( e.x1, boundMax.getY() );
				if (boundMax.x < e.x2) boundMax.setLocation( e.x2, boundMax.getY() );

				if (boundMin.y > e.y1) boundMin.setLocation( boundMin.getX(), e.y1 );
				if (boundMin.y > e.y2) boundMin.setLocation( boundMin.getX(), e.y2 );
				if (boundMax.y < e.y1) boundMax.setLocation( boundMax.getX(), e.y1 );
				if (boundMax.y < e.y2) boundMax.setLocation( boundMax.getX(), e.y2 );
			}

			// make a unnormalized vector along the edge of e
			dx = e.x2 - e.x1;
			dy = e.y2 - e.y1;
			// find a line orthogonal to dx/dy
			nx = dy;
			ny = -dx;
			// dot product the centroid and the normal.
			dx = cx - e.x1;
			dy = cy - e.y1;
			dot1 = (dx * nx + dy * ny);

			if (cellBorder.size() == numEdgesInCell) {
				cellBorder.add(new VoronoiCellEdge());
			}

			VoronoiCellEdge ce = cellBorder.get(numEdgesInCell++);
			ce.px = e.x1;
			ce.py = e.y1;
			if (dot1 < 0) {
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


	protected boolean insideBorder(double x, double y) {
		double dx, dy;
		int i;
		Iterator<VoronoiCellEdge> ice = cellBorder.iterator();
		for (i = 0; i < numEdgesInCell; ++i) {
			VoronoiCellEdge ce = ice.next();

			// dot product the test point.
			dx = x - ce.px;
			dy = y - ce.py;
			// If they are opposite signs then the test point is outside the cell
			if (dx * ce.nx + dy * ce.ny < 0) return false;
		}
		// passed all tests, must be in cell.
		return true;
	}


	/**
	 * Find the weighted center of each cell.
	 * weight is based on the intensity of the color of each pixel inside the cell
	 * the center of the pixel must be inside the cell to be counted.
	 * @return the total magnitude movement of all centers
	 */
	protected float adjustCentroids() {
		int i;
		double weight, wx, wy, x, y;
		//int step = (int) Math.ceil(machine.getDiameter() / (1.0 * scale));
		double stepSize = 2.0;
		float totalMagnitude=0;

		for (i = 0; i < cells.length; ++i) {
			generateBounds(i);
			double sx = Math.floor(boundMin.x);
			double sy = Math.floor(boundMin.y);
			double ex = Math.floor(boundMax.x);
			double ey = Math.floor(boundMax.y);


			//System.out.println("bounding "+i+" from "+sx+", "+sy+" to "+ex+", "+ey);
			//System.out.println("centroid "+cells[i].centroid.x+", "+cells[i].centroid.y);

			weight = 0;
			wx = 0;
			wy = 0;

			for (y = sy; y <= ey; y += stepSize) {
				for (x = sx; x <= ex; x += stepSize) {
					if (insideBorder(x, y)) {
						float val = 1.0f - ((float) sourceImage.sample1x1( (float)x, (float)y ) / 255.0f );
						weight += val;
						wx += x * val;
						wy += y * val;
					}
				}
			}
			if (weight > 0) {
				wx /= weight;
				wy /= weight;
				totalMagnitude+=weight;
			}

			// make sure centroid can't leave image bounds
			if (wx < xLeft) wx = xLeft;
			if (wy < yBottom) wy = yBottom;
			if (wx >= xRight) wx = xRight;
			if (wy >= yTop) wy = yTop;

			// use the new center
			cells[i].centroid.setLocation(wx, wy);
		}
		return totalMagnitude;
	}

	public void setGenerations(int value) {
		if(value<1) value=1;
		numGenerations = value;
	}
	public int getGenerations() {
		return numGenerations;
	}
	public void setNumCells(int value) {
		if(value<1) value=1;
		numCells = value;
	}
	public int getNumCells() {
		return numCells;
	}
	public void setMinDotSize(float value) {
		if(value<0.001) value=0.001f;
		minDotSize = value;
	}
	public float getMaxDotSize() {
		return maxDotSize;
	}
	public void setMaxDotSize(float value) {
		if(value<0.01) value=0.01f;
		maxDotSize = value;
	}
	public float getMinDotSize() {
		return minDotSize;
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
