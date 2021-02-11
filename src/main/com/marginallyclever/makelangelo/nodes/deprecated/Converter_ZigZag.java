package com.marginallyclever.makelangelo.nodes.deprecated;


import java.awt.Point;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import com.jogamp.opengl.GL2;
import com.marginallyclever.core.StringHelper;
import com.marginallyclever.core.TransformedImage;
import com.marginallyclever.core.imageFilters.Filter_BlackAndWhite;
import com.marginallyclever.core.imageFilters.Filter_DitherFloydSteinberg;
import com.marginallyclever.core.log.Log;
import com.marginallyclever.core.turtle.Turtle;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.nodes.ImageConverter;
import com.marginallyclever.makelangelo.robot.MakelangeloRobotDecorator;


/**
 * Generate a Gcode file from the BufferedImage supplied.<br>
 * Use the filename given in the constructor as a basis for the gcode filename, but change the extension to .ngc
 *
 * @author Dan
 */
@Deprecated
public class Converter_ZigZag extends ImageConverter implements MakelangeloRobotDecorator {
	// processing tools
	long t_elapsed, t_start;
	double progress;
	double old_len, len;
	long time_limit = 10 * 60 * 1000;  // 10 minutes

	int numPoints;
	Point[] points = null;
	int[] solution = null;
	int scount;

	
	private ReentrantLock lock = new ReentrantLock();

	
	public String getName() {
		return Translator.get("Converter_ZigZag.name");
	}


	public String formatTime(long millis) {
		String elapsed = "";
		long s = millis / 1000;
		long m = s / 60;
		long h = m / 60;
		m %= 60;
		s %= 60;
		if (h > 0) elapsed += h + "h";
		if (h > 0 || m > 0) elapsed += m + "m";
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
			//pm.setProgress((int) progress);
		}
	}


	// we have s1,s2...e-1,e
	// check if s1,e-1,...s2,e is shorter
	public int flipTests() {
		int start, end, j, once = 0;

		for (start = 0; start < numPoints - 2 /*&& !threadWorker.isCancelled() && !pm.isCanceled()*/; ++start) {
			float a = calculateWeight(solution[start], solution[start + 1]);
			int best_end = -1;
			double best_diff = 0;

			for (end = start + 2; end <= numPoints /*&& !threadWorker.isCancelled() && !pm.isCanceled()*/; ++end) {
				// before
				float b = calculateWeight(solution[end], solution[end - 1]);
				// after
				float c = calculateWeight(solution[start], solution[end - 1]);
				float d = calculateWeight(solution[end], solution[start + 1]);

				double temp_diff = (a + b) - (c + d);
				if (best_diff < temp_diff) {
					best_diff = temp_diff;
					best_end = end;
				}
			}

			if (best_end != -1 /*&& !threadWorker.isCancelled() && !pm.isCanceled()*/) {
				once = 1;
				// do the flip
				int begin = start + 1;
				int finish = best_end;
				int half = (finish - begin) / 2;
				int temp;
				while (lock.isLocked()) ;

				lock.lock();
				//Makelangelo.getSingleton().Log("<font color='red'>flipping "+(finish-begin)+"</font>\n");
				for (j = 0; j < half; ++j) {
					temp = solution[begin + j];
					solution[begin + j] = solution[finish - 1 - j];
					solution[finish - 1 - j] = temp;
				}
				lock.unlock();
				updateProgress(len, 1);
			}
		}
		return once;
	}

	@Override
	public void render(GL2 gl2) {
		if (points == null || solution == null) return;

		while (lock.isLocked());
		lock.lock();

		gl2.glColor3f(0, 0, 0);
		gl2.glBegin(GL2.GL_LINE_STRIP);
		for (int i = 0; i < points.length; ++i) {
			if (points[solution[i]] == null) break;
			gl2.glVertex2f((points[solution[i]].x) * 0.1f,
					(points[solution[i]].y) * 0.1f);
		}
		gl2.glEnd();

		lock.unlock();
	}


	protected float calculateWeight(int a, int b) {
		float x = points[a].x - points[b].x;
		float y = points[a].y - points[b].y;
		return x * x + y * y;
	}


	private void generateTSP(Turtle turtle) {
		greedyTour();

		Log.message("Running Kernighan–Lin optimization...");

		len = getTourLength(solution);
		old_len = len;

		t_elapsed = 0;
		t_start = System.currentTimeMillis();
		progress = 0;
		updateProgress(len, 2);

		int once = 1;
		while (once == 1 && t_elapsed < time_limit /*&& !threadWorker.isCancelled() && !pm.isCanceled()*/) {
			once = 0;
			//@TODO: make these optional for the very thorough people
			//once|=transposeForwardTest();
			//once|=transposeBackwardTest();

			once |= flipTests();

			updateProgress(len, 2);
		}

		convertAndSaveToGCode(turtle);
	}


	private double calculateLength(int a, int b) {
		return Math.sqrt(calculateWeight(a, b));
	}

	/**
	 * Get the length of a tour segment
	 *
	 * @param list an array of indexes into the point list.  the order forms the tour sequence.
	 * @return the length of the tour
	 */
	private double getTourLength(int[] list) {
		double w = 0;
		for (int i = 0; i < numPoints - 1; ++i) {
			w += calculateLength(list[i], list[i + 1]);
		}
		return w;
	}

	/**
	 * Starting with point 0, find the next nearest point and repeat until all points have been "found".
	 */
	private void greedyTour() {
		Log.message("Finding greedy tour solution...");

		int i;
		float w, bestw;
		int besti;

		// put all the points in the solution in no particular order.
		for (i = 0; i < numPoints; ++i) {
			solution[i] = i;
		}


		int scount = 0;
		solution[scount] = solution[0];

		do {
			// Find the nearest point not already in the line.
			// Any solution[n] where n>scount is not in the line.
			bestw = calculateWeight(solution[scount], solution[scount + 1]);
			besti = scount + 1;
			for (i = scount + 2; i < numPoints; ++i) {
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
		} while (scount < numPoints);
	}


	/**
	 * Open a file and write out the edge list as a set of GCode commands.
	 * Since all the points are connected in a single loop,
	 * start at the tsp point closest to the calibration point and go around until you get back to the start.
	 */
	private void convertAndSaveToGCode(Turtle turtle) {
		// find the tsp point closest to the calibration point
		int i;
		int besti = -1;
		float bestw = 1000000;
		float x, y, w;
		for (i = 0; i < numPoints; ++i) {
			x = points[solution[i]].x;
			y = points[solution[i]].y;
			w = x * x + y * y;
			if (w < bestw) {
				bestw = w;
				besti = i;
			}
		}

		turtle.reset();
		
		// jump to first point
		turtle.jumpTo(points[besti].x, points[besti].y);
		// move through entire list
		for (i = 1; i < numPoints; ++i) {
			int v = (besti + i) % numPoints;
			turtle.moveTo(points[v].x, points[v].y);
		}
		// close the loop
		turtle.moveTo(points[besti].x, points[besti].y);

		turtle.penUp();
	}


	protected void connectTheDots(Turtle turtle,TransformedImage img) {
		// from top to bottom of the margin area...
		double [] bounds = img.getBounds();
		double yBottom = bounds[TransformedImage.BOTTOM];
		double yTop    = bounds[TransformedImage.TOP];
		double xLeft   = bounds[TransformedImage.LEFT];
		double xRight  = bounds[TransformedImage.RIGHT];
		
		double x, y;
		int i;
		// count the points
		numPoints = 0;
		for (y = yBottom; y < yTop; ++y) {
			for (x = xLeft; x < xRight; ++x) {
				i = img.sample1x1(x, y);
				if (i == 0) {
					++numPoints;
				}
			}
		}

		Log.message(numPoints + " points.");
		points = new Point[numPoints + 1];
		solution = new int[numPoints + 1];

		// collect the point data
		numPoints = 0;
		for (y = yBottom; y < yTop; ++y) {
			for (x = xLeft; x < xRight; ++x) {
				i = img.sample1x1(x, y);
				if (i == 0) {
					Point p = new Point();
					p.setLocation( x, y );
					points[numPoints++] = p;
				}
			}
		}
	}
	

	@Override
	public boolean iterate() {
		Turtle turtle = new Turtle();
		
		// make black & white
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		TransformedImage img = bw.filter(inputImage.getValue());

		// Dither
		Filter_DitherFloydSteinberg fs = new Filter_DitherFloydSteinberg();
		img = fs.filter(img);

		// connect the dots
		connectTheDots(turtle,img);
		// Shorten the line that connects the dots
		generateTSP(turtle);
		
		ArrayList<Turtle> list = new ArrayList<Turtle>();
		list.add(turtle);
		setTurtleResult(list);
		return false;
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
