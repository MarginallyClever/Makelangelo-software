package com.marginallyclever.artPipeline.converters;

import java.awt.Rectangle;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.jogamp.opengl.GL2;
import com.marginallyclever.artPipeline.TransformedImage;
import com.marginallyclever.artPipeline.imageFilters.Filter_BlackAndWhite;
import com.marginallyclever.convenience.Turtle;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.MakelangeloRobotDecorator;
import com.marginallyclever.voronoi.VoronoiCell;
import com.marginallyclever.voronoi.VoronoiGraphEdge;
import com.marginallyclever.voronoi.VoronoiTesselator;

/**
 * Dithering using a particle system
 * 
 * @author Dan http://en.wikipedia.org/wiki/Fortune%27s_algorithm
 *         http://skynet.ie/~sos/mapviewer/voronoi.php
 * @since 7.0.0?
 */
public class Converter_VoronoiZigZag extends ImageConverter implements MakelangeloRobotDecorator {
	private ReentrantLock lock = new ReentrantLock();

	private VoronoiTesselator voronoiTesselator = new VoronoiTesselator();
	private VoronoiCell[] cells = new VoronoiCell[1];
	private TransformedImage sourceImage;
	private List<VoronoiGraphEdge> graphEdges = null;
	private static int numCells = 3000;
	private static float minDotSize = 1.0f;
	private double[] xValuesIn = null;
	private double[] yValuesIn = null;
	private int[] solution = null;
	private int solutionContains;
	private int renderMode;
	private boolean lowNoise;

	// processing tools
	private long t_elapsed, t_start;
	private double progress;
	private double old_len, len;
	private long time_limit = 10 * 60 * 1000; // 10 minutes

	private double yBottom, yTop, xLeft, xRight;
	
	@Override
	public String getName() {
		return Translator.get("VoronoiZigZagName");
	}

	@Override
	public ImageConverterPanel getPanel() {
		return new Converter_VoronoiZigZag_Panel(this);
	}
	
	@Override
	public void setImage(TransformedImage img) {
		// make black & white
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		sourceImage = bw.filter(img);
		
		yBottom = machine.getMarginBottom();
		yTop    = machine.getMarginTop();
		xLeft   = machine.getMarginLeft();
		xRight  = machine.getMarginRight();
		
		keepIterating=true;
		restart();
		renderMode = 0;
	}

	public void restart() {
		if(!keepIterating) {
			loadAndSaveImage.reconvert();
			return;
		}
		lowNoise=false;
		keepIterating=true;
		initializeCells(0.5);
	}
	
	@Override
	public boolean iterate() {
		if(lowNoise==true) {
			optimizeTour();
		} else {
			double noiseLevel = evolveCells();
			if( noiseLevel < 2*numCells ) {
				lowNoise=true;
				greedyTour();
				renderMode = 1;
				Log.message("Running Lin/Kerighan optimization...");
			}			
		}
		return keepIterating;
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

		int i;

		if (graphEdges != null) {
			// draw cell edges
			gl2.glColor3f(0.9f, 0.9f, 0.9f);
			gl2.glBegin(GL2.GL_LINES);
			for (VoronoiGraphEdge e : graphEdges) {
				gl2.glVertex2d( e.x1, e.y1 );
				gl2.glVertex2d( e.x2, e.y2 );
			}
			gl2.glEnd();
		}
		if (renderMode == 0) {
			// draw cell centers
			gl2.glPointSize(3);
			gl2.glColor3f(0, 0, 0);
			gl2.glBegin(GL2.GL_POINTS);
			for (VoronoiCell c : cells) {
				Point2D p = c.centroid;
				gl2.glVertex2d(p.x,p.y);
			}
			gl2.glEnd();
		}
		if (renderMode == 1 && solution != null) {
			// draw tour
			gl2.glColor3f(0, 0, 0);
			gl2.glBegin(GL2.GL_LINE_LOOP);
			for (i = 0; i < solutionContains; ++i) {
				VoronoiCell c = cells[solution[i]];
				gl2.glVertex2d( c.centroid.x, c.centroid.y );
			}
			gl2.glEnd();
		}
		lock.unlock();
	}

	private void optimizeTour() {
		old_len = getTourLength(solution);
		updateProgress(old_len, 2);

		// @TODO: make these optional for the very thorough people
		// once|=transposeForwardTest();
		// once|=transposeBackwardTest();

		keepIterating = flipTests();
	}

	public String formatTime(long millis) {
		String elapsed = "";
		long s = millis / 1000;
		long m = s / 60;
		long h = m / 60;
		m %= 60;
		s %= 60;
		if (h > 0)
			elapsed += h + "h";
		if (h > 0 || m > 0)
			elapsed += m + "m";
		elapsed += s + "s ";
		return elapsed;
	}

	public void updateProgress(double len, int color) {
		t_elapsed = System.currentTimeMillis() - t_start;
		double new_progress = 100.0 * (double) t_elapsed / (double) time_limit;
		if (new_progress > progress + 0.1) {
			// find the new tour length
			len = getTourLength(solution);
			if (old_len > len) {
				old_len = len;
				Log.message(formatTime(t_elapsed) + ": " + StringHelper.formatDouble(len) + "mm");
			}
			progress = new_progress;
			pm.setProgress((int) progress);
		}
	}

	private int ti(int x) {
		return (x + solutionContains) % solutionContains;
	}

	/**
	 * we have s1,s2...e-1,e.  check if s1,e-1,...s2,e is shorter
	 * @return true if something was improved.
	 */
	// 
	public boolean flipTests() {
		boolean once = false;
		int start, end, j, best_end;
		double a, b, c, d, temp_diff, best_diff;

		for (start = 0; start < solutionContains * 2 - 2 && !swingWorker.isCancelled() && !pm.isCanceled(); ++start) {
			a = calculateWeight(solution[ti(start)], solution[ti(start + 1)]);
			best_end = -1;
			best_diff = 0;

			for (end = start + 2; end < start + solutionContains && !swingWorker.isCancelled() && !pm.isCanceled(); ++end) {
				// before
				b = calculateWeight(solution[ti(end)], solution[ti(end - 1)]);
				// after
				c = calculateWeight(solution[ti(start)], solution[ti(end - 1)]);
				d = calculateWeight(solution[ti(end)], solution[ti(start + 1)]);

				temp_diff = (a + b) - (c + d);
				if (best_diff < temp_diff) {
					best_diff = temp_diff;
					best_end = end;
				}
			}

			if (best_end != -1 && !swingWorker.isCancelled() && !pm.isCanceled()) {
				once = true;
				// do the flip
				int begin = start + 1;
				int finish = best_end;
				if (best_end < begin)
					finish += solutionContains;
				int half = (finish - begin) / 2;
				int temp;
				while (lock.isLocked());

				lock.lock();
				// Makelangelo.getSingleton().Log("<font color='red'>flipping
				// "+(finish-begin));
				for (j = 0; j < half; ++j) {
					temp = solution[ti(begin + j)];
					solution[ti(begin + j)] = solution[ti(finish - 1 - j)];
					solution[ti(finish - 1 - j)] = temp;
				}
				lock.unlock();
				updateProgress(len, 1);
			}
		}
		return once;
	}

	private double calculateLength(int a, int b) {
		return Math.sqrt(calculateWeight(a, b));
	}

	/**
	 * Get the length of a tour segment
	 * 
	 * @param list
	 *            an array of indexes into the point list. the order forms the
	 *            tour sequence.
	 * @return the length of the tour
	 */
	private double getTourLength(int[] list) {
		double w = 0;
		for (int i = 0; i < solutionContains - 1; ++i) {
			w += calculateLength(list[i], list[i + 1]);
		}
		return w;
	}

	/**
	 * Starting with point 0, find the next nearest point and repeat until all
	 * points have been "found".
	 */
	private void greedyTour() {
		Log.message("Finding greedy tour solution...");

		int i, j;
		double w, bestw;
		int besti;

		solutionContains = 0;
		for (i = 0; i < cells.length; ++i) {
			VoronoiCell c = cells[i];
			float v = 1.0f - (float) sourceImage.sample1x1( (int) c.centroid.x, (int) c.centroid.y) / 255.0f;
			if (v * 5 > minDotSize)
				solutionContains++;
		}

		try {
			solution = new int[solutionContains];

			// put all the points in the solution in no particular order.
			j = 0;
			for (i = 0; i < cells.length; ++i) {
				VoronoiCell c = cells[i];
				float v = 1.0f - (float) sourceImage.sample1x1( (int) c.centroid.x, (int) c.centroid.y) / 255.0f;
				if (v * 5 > minDotSize) {
					solution[j++] = i;
				}
			}

			int scount = 0;

			do {
				// Find the nearest point not already in the line.
				// Any solution[n] where n>scount is not in the line.
				bestw = calculateWeight(solution[scount], solution[scount + 1]);
				besti = scount + 1;
				for (i = scount + 2; i < solutionContains; ++i) {
					w = calculateWeight(solution[scount], solution[i]);
					if (w < bestw) {
						bestw = w;
						besti = i;
					}
				}
				i = solution[scount + 1];
				solution[scount + 1] = solution[besti];
				solution[besti] = i;
				scount++;
			} while (scount < solutionContains - 2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected double calculateWeight(int a, int b) {
		assert (a >= 0 && a < cells.length);
		assert (b >= 0 && b < cells.length);
		double x = cells[a].centroid.x - cells[b].centroid.x;
		double y = cells[a].centroid.y - cells[b].centroid.y;
		return x * x + y * y;
	}

	// set some starting points in a grid
	protected void initializeCells(double minDistanceBetweenSites) {
		Log.message("Initializing cells");

		cells = new VoronoiCell[numCells];
		// convert the cells to sites used in the Voronoi class.
		xValuesIn = new double[numCells];
		yValuesIn = new double[numCells];

		// from top to bottom of the margin area...
		int used;
		for (used=0;used<numCells;++used) {
			cells[used] = new VoronoiCell();
		}
		
		used=0;
		while(used<numCells) {
			double x=0,y=0;
			for(int i=0;i<30;++i) {
				x = xLeft   + Math.random()*(xRight-xLeft);
				y = yBottom + Math.random()*(yTop-yBottom);
				if(sourceImage.canSampleAt((float)x, (float)y)) {
					float v = sourceImage.sample1x1Unchecked((float)x, (float)y);
					if(Math.random()*255 > v) break;
				}
			}
			cells[used].centroid.set(x,y);
			++used;
		}


		voronoiTesselator.Init(minDistanceBetweenSites);
	}

	/**
	 * Jiggle the dots until they make a nice picture
	 */
	protected double evolveCells() {
		double totalWeight=0;
		try {
			while(lock.isLocked());
			lock.lock();
			tessellateVoronoiDiagram();
			lock.unlock();
			totalWeight = adjustCentroids();
		} catch (Exception e) {
			e.printStackTrace();
			if(lock.isHeldByCurrentThread() && lock.isLocked()) {
				lock.unlock();
			}
		}
		return totalWeight;
	}

	// write cell centroids to gcode.
	protected void writeOutCells() {
		turtle = new Turtle();
		
		if (graphEdges != null) {
			// find the tsp point closest to the calibration point
			int i;
			int besti = -1;
			double bestw = Float.MAX_VALUE;
			double x, y, w;
			for (i = 0; i < solutionContains; ++i) {
				x = cells[solution[i]].centroid.x;
				y = cells[solution[i]].centroid.y;
				w = x * x + y * y;
				if (w < bestw) {
					bestw = w;
					besti = i;
				}
			}

			// write the entire sequence
			for (i = 0; i <= solutionContains; ++i) {
				int v = (besti + i) % solutionContains;
				x = cells[solution[v]].centroid.x;
				y = cells[solution[v]].centroid.y;
				turtle.moveTo(x, y);
			}
		}
	}

	// I have a set of points. I want a list of cell borders.
	// cell borders are halfway between any point and it's nearest neighbors.
	protected void tessellateVoronoiDiagram() {
		// convert the cells to sites used in the Voronoi class.
		int i;
		for (i = 0; i < numCells; ++i) {
			xValuesIn[i] = cells[i].centroid.x;
			yValuesIn[i] = cells[i].centroid.y;
			cells[i].resetRegion();
		}

		// scan left to right across the image, building the list of borders as we go.
		graphEdges = voronoiTesselator.generateVoronoi(xValuesIn, yValuesIn, xLeft, xRight, yBottom, yTop);
		
		for (VoronoiGraphEdge e : graphEdges) {
			try {
				cells[e.site1].addPoint((float)e.x1, (float)e.y1);
				cells[e.site1].addPoint((float)e.x2, (float)e.y2);
				
				cells[e.site2].addPoint((float)e.x1, (float)e.y1);
				cells[e.site2].addPoint((float)e.x2, (float)e.y2);
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
		float totalMagnitude=0;

		for (VoronoiCell c : cells) {
			if(c.region==null) { 
				continue;
			}
			Rectangle bounds = c.region.getBounds();

			minX = bounds.getMinX();
			maxX = bounds.getMaxX();
			double dx=maxX-minX;

			minY = bounds.getMinY();
			maxY = bounds.getMaxY();
			double dy=maxY-minY;

			stepSize=1.0;
			if(dx<dy) {
				if(dx<1) {
					stepSize = dx/3.0;
				}
			} else {
				if(dy<1) {
					stepSize = dy/3.0;
				}
			}
			
			int hits = 0;
			totalCellWeight = 0;
			wx = 0;
			wy = 0;

			float sampleWeight;
			for (y = minY; y <= maxY; y +=stepSize) {
				for (x = minX; x <= maxX; x +=stepSize) {
					if (c.region.contains(x,y)) {
						if(sourceImage.canSampleAt((float)x, (float)y)) 
						{
							hits++;
							sampleWeight = 255.0f - (float)sourceImage.sample1x1Unchecked( (float)x, (float)y );
							totalCellWeight += sampleWeight;
							wx += (x) * sampleWeight;
							wy += (y) * sampleWeight;
						}
					}
				}
			}

			if (totalCellWeight > 0) {
				wx /= totalCellWeight;
				wy /= totalCellWeight;
				totalMagnitude+=totalCellWeight;
			} else {
				continue;
			}

			// make sure centroid can't leave image bounds
			if (wx < xLeft || wx >= xRight || wy < yBottom || wy >= yTop ) {
				/*
				// try the geometric centroid
				for(int j=0;j<c.region.npoints;++j) {
					wx = c.region.xpoints[j];
					wy = c.region.ypoints[j];
				}
				wx/= c.region.npoints;
				wy/= c.region.npoints;
				*/
				if (wx <  xLeft ) wx = xLeft+1;
				if (wx >= xRight) wx = xRight-1;
				
				if (wy <  yBottom) wy = yBottom+1;
				if (wy >= yTop   ) wy = yTop-1;
			}
			
			// use the new center
			if(hits>0)
			{
				double ox = c.centroid.x;
				double oy = c.centroid.y;
				double dx2 = wx - ox;
				double dy2 = wy - oy;

				c.weight = totalCellWeight/(double)hits;
				
				double nx = ox + dx2 * 0.25 + (Math.random()-0.5) * 0.8e-10;
				double ny = oy + dy2 * 0.25 + (Math.random()-0.5) * 0.8e-10;
				
				c.centroid.set(nx, ny);
			}
		}
		return totalMagnitude;
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
	public float getMinDotSize() {
		return minDotSize;
	}
}

/**
 * This file is part of Makelangelo.
 *
 * Makelangelo is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Makelangelo is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Makelangelo. If not, see <http://www.gnu.org/licenses/>.
 */
