package com.marginallyclever.makelangelo.makeArt.imageConverter;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeArt.imageFilter.Filter_BlackAndWhite;
import com.marginallyclever.makelangelo.makeArt.imageFilter.Filter_DitherFloydSteinberg;
import com.marginallyclever.makelangelo.preview.PreviewListener;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Generate a Gcode file from the BufferedImage supplied.<br>
 * Use the filename given in the constructor as a basis for the gcode filename, but change the extension to .ngc
 *
 * @author Dan
 */
@Deprecated
public class Converter_ZigZag extends ImageConverter implements PreviewListener {
	private static final Logger logger = LoggerFactory.getLogger(Converter_ZigZag.class);
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

	@Override
	public String getName() {
		return Translator.get("ZigZagName");
	}

	private String formatTime(long millis) {
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

	private void updateProgress(double len, int color) {
		t_elapsed = System.currentTimeMillis() - t_start;
		double new_progress = 100.0 * (double) t_elapsed / (double) time_limit;
		if (new_progress > progress + 0.1) {
			// find the new tour length
			len = getTourLength(solution);
			if (old_len > len) {
				old_len = len;
				logger.debug("{}: {}mm",formatTime(t_elapsed),  StringHelper.formatDouble(len));
			}
			progress = new_progress;
			setProgress((int) progress);
		}
	}

	// we have s1,s2...e-1,e
	// check if s1,e-1,...s2,e is shorter
	private int flipTests() {
		int start, end, j, once = 0;

		for (start = 0; start < numPoints - 2 && !isThreadCancelled(); ++start) {
			float a = calculateWeight(solution[start], solution[start + 1]);
			int best_end = -1;
			double best_diff = 0;

			for (end = start + 2; end <= numPoints && !isThreadCancelled(); ++end) {
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

			if (best_end != -1 && !isThreadCancelled()) {
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

	private float calculateWeight(int a, int b) {
		float x = points[a].x - points[b].x;
		float y = points[a].y - points[b].y;
		return x * x + y * y;
	}

	private void generateTSP() {
		greedyTour();

		logger.debug("Running Kernighan–Lin optimization...");

		len = getTourLength(solution);
		old_len = len;

		t_elapsed = 0;
		t_start = System.currentTimeMillis();
		progress = 0;
		updateProgress(len, 2);

		int once = 1;
		while (once == 1 && t_elapsed < time_limit && !isThreadCancelled()) {
			once = 0;
			//@TODO: make these optional for the very thorough people
			//once|=transposeForwardTest();
			//once|=transposeBackwardTest();

			once |= flipTests();

			updateProgress(len, 2);
		}

		convertAndSaveToGCode();
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
		logger.debug("Finding greedy tour solution...");

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
	private void convertAndSaveToGCode() {
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

		turtle = new Turtle();
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

	private void connectTheDots() {
		// from top to bottom of the margin area...
		double yBottom = myPaper.getMarginBottom();
		double yTop    = myPaper.getMarginTop()   ;
		double xLeft   = myPaper.getMarginLeft()  ;
		double xRight  = myPaper.getMarginRight() ;
		
		double x, y;
		int i;
		// count the points
		numPoints = 0;
		for (y = yBottom; y < yTop; ++y) {
			for (x = xLeft; x < xRight; ++x) {
				i = myImage.sample1x1(x, y);
				if (i == 0) {
					++numPoints;
				}
			}
		}

		logger.debug("{} points.", numPoints);
		points = new Point[numPoints + 1];
		solution = new int[numPoints + 1];

		// collect the point data
		numPoints = 0;
		for (y = yBottom; y < yTop; ++y) {
			for (x = xLeft; x < xRight; ++x) {
				i = myImage.sample1x1(x, y);
				if (i == 0) {
					Point p = new Point();
					p.setLocation( x, y );
					points[numPoints++] = p;
				}
			}
		}
	}

	public void finish() {
		// make black & white
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		myImage = bw.filter(myImage);

		// Dither
		Filter_DitherFloydSteinberg fs = new Filter_DitherFloydSteinberg();
		myImage = fs.filter(myImage);

		// connect the dots
		connectTheDots();
		// Shorten the line that connects the dots
		generateTSP();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// TODO Auto-generated method stub
		
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
