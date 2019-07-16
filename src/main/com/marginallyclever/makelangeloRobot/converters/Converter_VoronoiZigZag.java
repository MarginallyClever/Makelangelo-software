package com.marginallyclever.makelangeloRobot.converters;

import java.awt.Point;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

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
	private Point bound_min = new Point();
	private Point bound_max = new Point();
	private int numEdgesInCell;
	private List<VoronoiCellEdge> cellBorder = null;
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

	private float yBottom, yTop, xLeft, xRight;
	
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
		
		yBottom = (float)machine.getPaperBottom() * (float)machine.getPaperMargin();
		yTop    = (float)machine.getPaperTop()    * (float)machine.getPaperMargin();
		xLeft   = (float)machine.getPaperLeft()   * (float)machine.getPaperMargin();
		xRight  = (float)machine.getPaperRight()  * (float)machine.getPaperMargin();
		
		keepIterating=true;
		restart();
		renderMode = 0;
	}

	public void restart() {
		if(!keepIterating) {
			loadAndSave.reconvert();
			return;
		}
		lowNoise=false;
		keepIterating=true;
		cellBorder = new ArrayList<>();
		initializeCells(0.001);
	}
	
	@Override
	public boolean iterate() {
		if(lowNoise==true) {
			optimizeTour();
		} else {
			float noiseLevel = evolveCells();
			if( noiseLevel < 2*numCells ) {
				lowNoise=true;
				greedyTour();
				renderMode = 1;
				Log.info("Running Lin/Kerighan optimization...");
			}			
		}
		return keepIterating;
	}
	
	public void finish(Writer out) throws IOException {
		keepIterating=false;
		writeOutCells(out);
	}

	@Override
	public void render(GL2 gl2, MakelangeloRobotSettings settings) {
		super.render(gl2, settings);
		
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
			gl2.glBegin(GL2.GL_POINTS);

			for (i = 0; i < cells.length; ++i) {
				VoronoiCell c = cells[i];
				if (c == null)
					continue;
				float v = 1.0f - (float) sourceImage.sample1x1((int) c.centroid.getX(), (int) c.centroid.getY()) / 255.0f;
				if (v * 5 <= minDotSize) {
					gl2.glColor3f(0.8f, 0.8f, 0.8f);
				} else {
					gl2.glColor3f(0, 0, 0);
				}

				gl2.glVertex2d( c.centroid.getX(), c.centroid.getY() );
			}
			gl2.glEnd();
		}
		if (renderMode == 1 && solution != null) {
			// draw tour
			gl2.glColor3f(0, 0, 0);
			gl2.glBegin(GL2.GL_LINE_LOOP);
			for (i = 0; i < solutionContains; ++i) {
				VoronoiCell c = cells[solution[i]];
				gl2.glVertex2d( c.centroid.getX(), c.centroid.getY() );
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
				DecimalFormat flen = new DecimalFormat("#.##");
				String c;
				switch (color) {
				case 0:
					c = "yellow";
					break;
				case 1:
					c = "blue";
					break;
				case 2:
					c = "red";
					break;
				default:
					c = "white";
					break;
				}
				Log.info(c, formatTime(t_elapsed) + ": " + flen.format(len) + "mm");
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
		Log.info("green", "Finding greedy tour solution...");

		int i, j;
		double w, bestw;
		int besti;

		solutionContains = 0;
		for (i = 0; i < cells.length; ++i) {
			VoronoiCell c = cells[i];
			float v = 1.0f - (float) sourceImage.sample1x1( (int) c.centroid.getX(), (int) c.centroid.getY()) / 255.0f;
			if (v * 5 > minDotSize)
				solutionContains++;
		}

		try {
			solution = new int[solutionContains];

			// put all the points in the solution in no particular order.
			j = 0;
			for (i = 0; i < cells.length; ++i) {
				VoronoiCell c = cells[i];
				float v = 1.0f - (float) sourceImage.sample1x1( (int) c.centroid.getX(), (int) c.centroid.getY()) / 255.0f;
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
		double x = cells[a].centroid.getX() - cells[b].centroid.getX();
		double y = cells[a].centroid.getY() - cells[b].centroid.getY();
		return x * x + y * y;
	}

	// set some starting points in a grid
	protected void initializeCells(double minDistanceBetweenSites) {
		Log.info("green", "Initializing cells");

		cells = new VoronoiCell[numCells];
		for (int used = 0; used < cells.length; used++) {
			cells[used] = new VoronoiCell();
			cells[used].centroid.setLocation(xLeft   + ((float)Math.random()*(xRight-xLeft)),
											 yBottom + ((float)Math.random()*(yTop-yBottom))
											 );
		}

		// convert the cells to sites used in the Voronoi class.
		xValuesIn = new double[cells.length];
		yValuesIn = new double[cells.length];

		voronoiTesselator.Init(minDistanceBetweenSites);
	}

	/**
	 * Jiggle the dots until they make a nice picture
	 */
	protected float evolveCells() {
		float totalWeight=0;
		try {
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
	protected void writeOutCells(Writer out) throws IOException {
		if (graphEdges != null) {
			Log.info("Writing gcode.");
			
			imageStart(out);

			// find the tsp point closest to the calibration point
			int i;
			int besti = -1;
			double bestw = 1000000;
			double x, y, w;
			for (i = 0; i < solutionContains; ++i) {
				x = cells[solution[i]].centroid.getX();
				y = cells[solution[i]].centroid.getY();
				w = x * x + y * y;
				if (w < bestw) {
					bestw = w;
					besti = i;
				}
			}

			// write the entire sequence
			for (i = 0; i <= solutionContains; ++i) {
				int v = (besti + i) % solutionContains;
				x = cells[solution[v]].centroid.getX();
				y = cells[solution[v]].centroid.getY();

				this.moveTo(out, x, y, false);
			}

			imageEnd(out);
		}
	}

	// I have a set of points. I want a list of cell borders.
	// cell borders are halfway between any point and it's nearest neighbors.
	protected void tessellateVoronoiDiagram() {
		// convert the cells to sites used in the Voronoi class.
		int i;
		for (i = 0; i < cells.length; ++i) {
			xValuesIn[i] = cells[i].centroid.getX();
			yValuesIn[i] = cells[i].centroid.getY();
		}

		// scan left to right across the image, building the list of borders as
		// we go.
		graphEdges = voronoiTesselator.generateVoronoi(xValuesIn, yValuesIn, xLeft, xRight, yBottom, yTop);
	}

	protected void generateBounds(int cellIndex) {
		numEdgesInCell = 0;

		double cx = cells[cellIndex].centroid.getX();
		double cy = cells[cellIndex].centroid.getY();

		double dx, dy, nx, ny, dot1;

		// long ta = System.nanoTime();

		for (VoronoiGraphEdge e : graphEdges) {
			if (e.site1 != cellIndex && e.site2 != cellIndex)
				continue;
			if (numEdgesInCell == 0) {
				if (e.x1 < e.x2) {
					bound_min.setLocation(e.x1, bound_min.getY());
					bound_max.setLocation(e.x2, bound_max.getY());
				} else {
					bound_min.setLocation(e.x2, bound_min.getY());
					bound_max.setLocation(e.x1, bound_max.getY());
				}
				if (e.y1 < e.y2) {
					bound_min.setLocation(bound_min.getX(), (float) e.y1);
					bound_max.setLocation(bound_max.getX(), (float) e.y2);
				} else {
					bound_min.setLocation(bound_min.getX(), (float) e.y2);
					bound_max.setLocation(bound_max.getX(), (float) e.y1);
				}
			} else {
				if (bound_min.x > e.x1)	bound_min.setLocation(e.x1, bound_min.getY());
				if (bound_min.x > e.x2) bound_min.setLocation(e.x2, bound_min.getY());
				if (bound_max.x < e.x1)	bound_max.setLocation(e.x1, bound_max.getY());
				if (bound_max.x < e.x2)	bound_max.setLocation(e.x2, bound_max.getY());
				if (bound_min.y > e.y1)	bound_min.setLocation(bound_min.getX(), e.y1);
				if (bound_min.y > e.y2)	bound_min.setLocation(bound_min.getX(), e.y2);
				if (bound_max.y < e.y1)	bound_max.setLocation(bound_max.getX(), e.y1);
				if (bound_max.y < e.y2)	bound_max.setLocation(bound_max.getX(), e.y2);
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

		// long tc = System.nanoTime();

		// System.out.println("\t"+((tb-ta)/1e6)+"\t"+((tc-tb)/1e6));
	}

	protected boolean insideBorder(float x, float y) {
		double dx, dy;
		int i;
		Iterator<VoronoiCellEdge> ice = cellBorder.iterator();
		for (i = 0; i < numEdgesInCell; ++i) {
			VoronoiCellEdge ce = ice.next();

			// dot product the test point.
			dx = x - ce.px;
			dy = y - ce.py;
			// If they are opposite signs then the test point is outside the
			// cell
			if (dx * ce.nx + dy * ce.ny < 0)
				return false;
		}
		// passed all tests, must be in cell.
		return true;
	}

	/**
	 * Adjust the weighted center of each cell.
	 * weight is based on the intensity of the color of each pixel inside the cell.
	 * the center of the pixel must be inside the cell to be counted.
	 * @return
	 */
	protected float adjustCentroids() {
		int i;
		float weight, wx, wy, x, y;
		float totalWeight=0;
		float stepSize = 2;

		for (i = 0; i < cells.length; ++i) {
			generateBounds(i);
			int sx = (int) Math.floor(bound_min.x);
			int sy = (int) Math.floor(bound_min.y);
			int ex = (int) Math.floor(bound_max.x);
			int ey = (int) Math.floor(bound_max.y);
			// System.out.println("bounding "+i+" from "+sx+", "+sy+" to "+ex+",
			// "+ey);
			// System.out.println("centroid "+cells[i].centroid.x+",
			// "+cells[i].centroid.y);

			weight = 0;
			wx = 0;
			wy = 0;

			for (y = sy; y <= ey; y += stepSize) {
				for (x = sx; x <= ex; x += stepSize) {
					if (insideBorder(x, y)) {
						float val = (float) sourceImage.sample1x1((int) x, (int) y) / 255.0f;
						val = 1.0f - val;
						weight += val;
						wx += x * val;
						wy += y * val;
					}
				}
			}

			if (weight > 0.0f) {
				wx /= weight;
				wy /= weight;
				totalWeight+=weight;
			}

			// make sure centroid can't leave image bounds
			if (wx < xLeft) wx = xLeft;
			if (wy < yBottom) wy = yBottom;
			if (wx >= xRight) wx = xRight;
			if (wy >= yTop) wy = yTop;

			// use the new center
			cells[i].centroid.setLocation(wx, wy);
		}
		return totalWeight;
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
