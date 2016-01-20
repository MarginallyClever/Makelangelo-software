package com.marginallyclever.converters;


import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.concurrent.locks.ReentrantLock;

import com.jogamp.opengl.GL2;
import com.marginallyclever.basictypes.Point2D;
import com.marginallyclever.filters.Filter_BlackAndWhite;
import com.marginallyclever.filters.Filter_DitherFloydSteinberg;
import com.marginallyclever.filters.Filter_Resize;
import com.marginallyclever.filters.ImageFilter;
import com.marginallyclever.makelangelo.DrawPanelDecorator;
import com.marginallyclever.makelangelo.Log;
import com.marginallyclever.makelangelo.MakelangeloRobotSettings;
import com.marginallyclever.makelangelo.Translator;


/**
 * Generate a Gcode file from the BufferedImage supplied.<br>
 * Use the filename given in the constructor as a basis for the gcode filename, but change the extension to .ngc
 *
 * @author Dan
 */
public class Converter_ZigZag extends ImageConverter implements DrawPanelDecorator {
	private ReentrantLock lock = new ReentrantLock();

	public String getName() {
		return Translator.get("ZigZagName");
	}

	// processing tools
	long t_elapsed, t_start;
	double progress;
	double old_len, len;
	long time_limit = 10 * 60 * 1000;  // 10 minutes

	int numPoints;
	Point2D[] points = null;
	int[] solution = null;
	int scount;


	public Converter_ZigZag(MakelangeloRobotSettings mc) {
		super(mc);
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
				Log.write( c , formatTime(t_elapsed) + ": " + flen.format(len) + "mm");
			}
			progress = new_progress;
			pm.setProgress((int) progress);
		}
	}


	// we have s1,s2...e-1,e
	// check if s1,e-1,...s2,e is shorter
	public int flipTests() {
		int start, end, j, once = 0;

		for (start = 0; start < numPoints - 2 && !parent.isCancelled() && !pm.isCanceled(); ++start) {
			float a = calculateWeight(solution[start], solution[start + 1]);
			int best_end = -1;
			double best_diff = 0;

			for (end = start + 2; end <= numPoints && !parent.isCancelled() && !pm.isCanceled(); ++end) {
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

			if (best_end != -1 && !parent.isCancelled() && !pm.isCanceled()) {
				once = 1;
				// do the flip
				int begin = start + 1;
				int finish = best_end;
				int half = (finish - begin) / 2;
				int temp;
				while (lock.isLocked()) ;

				lock.lock();
				//DrawbotGUI.getSingleton().Log("<font color='red'>flipping "+(finish-begin)+"</font>\n");
				for (j = 0; j < half; ++j) {
					temp = solution[begin + j];
					solution[begin + j] = solution[finish - 1 - j];
					solution[finish - 1 - j] = temp;
				}
				lock.unlock();
				if(drawPanel != null) drawPanel.repaintNow();
				updateProgress(len, 1);
			}
		}
		return once;
	}


	public void render(GL2 gl2, MakelangeloRobotSettings machine) {
		if (points == null || solution == null) return;

		while (lock.isLocked()) ;

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


	private void generateTSP(Writer out) throws IOException {
		greedyTour();

		Log.write("green","Running Lin/Kerighan optimization...");

		len = getTourLength(solution);
		old_len = len;

		t_elapsed = 0;
		t_start = System.currentTimeMillis();
		progress = 0;
		updateProgress(len, 2);

		int once = 1;
		while (once == 1 && t_elapsed < time_limit && !parent.isCancelled()) {
			once = 0;
			//@TODO: make these optional for the very thorough people
			//once|=transposeForwardTest();
			//once|=transposeBackwardTest();

			once |= flipTests();

			updateProgress(len, 2);
		}

		convertAndSaveToGCode(out);
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
		Log.write("green","Finding greedy tour solution...");

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


	private void moveTo(Writer out, int i, boolean up) throws IOException {
		tool.writeMoveTo(out, points[solution[i]].x, points[solution[i]].y);
	}


	/**
	 * Open a file and write out the edge list as a set of GCode commands.
	 * Since all the points are connected in a single loop,
	 * start at the tsp point closest to the calibration point and go around until you get back to the start.
	 */
	private void convertAndSaveToGCode(Writer out) throws IOException {
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

		out.write(machine.getConfigLine() + ";\n");
		out.write(machine.getBobbinLine() + ";\n");

		setAbsoluteMode(out);
		tool.writeChangeTo(out);
		liftPen(out);
		// move to the first point
		moveTo(out, besti, false);
		lowerPen(out);

		for (i = 1; i < numPoints; ++i) {
			moveTo(out, (besti + i) % numPoints, false);
		}
		moveTo(out, besti, false);

		// lift pen and return to home
		liftPen(out);
	}


	protected void connectTheDots(BufferedImage img) {
		tool = machine.getCurrentTool();
		imageSetupTransform(img);

		int x, y, i;
		// count the points
		numPoints = 0;
		for (y = 0; y < imageHeight; ++y) {
			for (x = 0; x < imageWidth; ++x) {
				i = ImageFilter.decode(img.getRGB(x, y));
				if (i == 0) {
					++numPoints;
				}
			}
		}

		Log.write("green", numPoints + " points.");
		points = new Point2D[numPoints + 1];
		solution = new int[numPoints + 1];

		// collect the point data
		numPoints = 0;
		for (y = 0; y < imageHeight; ++y) {
			for (x = 0; x < imageWidth; ++x) {
				i = ImageFilter.decode(img.getRGB(x, y));
				if (i == 0) {
					points[numPoints++] = new Point2D(TX(x), TY(y));
				}
			}
		}
	}

	/**
	 * The main entry point
	 *
	 * @param img the image to convert.
	 */
	public boolean convert(BufferedImage img,Writer out) throws IOException {
		// resize & flip as needed
		// Note that changing 250/250 here changes the number of dots a lot.
		Filter_Resize rs = new Filter_Resize(250, 250);
		rs.flipHorizontally = machine.isReverseForGlass();
		rs.targetWidth = machine.getPaperWidth();
		rs.targetHeight = machine.getPaperHeight();


		img = rs.filter(img);

		// make black & white
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		img = bw.filter(img);

		// Dither
		Filter_DitherFloydSteinberg fs = new Filter_DitherFloydSteinberg();
		img = fs.filter(img);

		// connect the dots
		connectTheDots(img);
		// Shorten the line that connects the dots
		generateTSP(out);

		return true;
	}
}


/**
 * This file is part of DrawbotGUI.
 * <p>
 * DrawbotGUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * DrawbotGUI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with DrawbotGUI.  If not, see <http://www.gnu.org/licenses/>.
 */
