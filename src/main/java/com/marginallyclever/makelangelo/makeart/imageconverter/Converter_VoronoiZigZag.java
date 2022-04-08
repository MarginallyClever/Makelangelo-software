package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imageFilter.Filter_BlackAndWhite;
import com.marginallyclever.makelangelo.makeart.imageconverter.voronoi.VoronoiCell;
import com.marginallyclever.makelangelo.makeart.imageconverter.voronoi.VoronoiDiagram;
import com.marginallyclever.makelangelo.preview.PreviewListener;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Dithering using a particle system
 * 
 * @author Dan http://en.wikipedia.org/wiki/Fortune%27s_algorithm
 *         http://skynet.ie/~sos/mapviewer/voronoi.php
 * @since 7.0.0?
 */
public class Converter_VoronoiZigZag extends ImageConverter implements PreviewListener {
	private static final Logger logger = LoggerFactory.getLogger(Converter_VoronoiZigZag.class);
	private static int numCells = 3000;
	private static double minDotSize = 1.0f;

	private final VoronoiDiagram voronoiDiagram = new VoronoiDiagram();
	private final Lock lock = new ReentrantLock();

	private int[] solution = null;
	private int solutionContains;
	private int renderMode;
	private boolean lowNoise;

	// processing tools
	private long t_elapsed, t_start;
	private double progress;
	private double old_len, len;
	private long time_limit = 10 * 60 * 1000; // 10 minutes

	private int iterations;
	
	@Override
	public String getName() {
		return Translator.get("VoronoiZigZagName");
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		boolean isDirty=false;
		if(evt.getPropertyName().equals("count")) {
			isDirty=true;
			setNumCells((int)evt.getNewValue());
		}
		if(evt.getPropertyName().equals("min")) {
			isDirty=true;
			setMinDotSize((double)evt.getNewValue());
		}
		if(isDirty) restart();
	}
	
	@Override
	public void setImage(TransformedImage img) {
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		myImage = bw.filter(img);
		restart();
		renderMode = 0;
	}

	public void restart() {
		if(myImage==null) return;

		lock.lock();
		try {
			lowNoise=false;
			iterations = 0;
			keepIterating = true;
			voronoiDiagram.initializeCells(numCells, myPaper.getMarginRectangle(), 0.5);
		}
		finally {
			lock.unlock();
		}
		t_start = System.currentTimeMillis();
	}
	
	@Override
	public boolean iterate() {
		iterations++;

		lock.lock();
		try {
			if(lowNoise==true) {
				optimizeTour();
			} else {
				double noiseLevel = evolveCells();
				System.out.println(iterations+": "+noiseLevel+"\t"+numCells+"\t"+(noiseLevel/(float)numCells));
				if( noiseLevel < 100 ) {
					System.out.println("done");
					lowNoise=true;
					greedyTour();
					renderMode = 1;
					logger.debug("Running Lin/Kerighan optimization...");
				}
			}
		}
		finally {
			lock.unlock();
		}
		return keepIterating;
	}

	/**
	 * Jiggle the dots until they make a nice picture
	 */
	private double evolveCells() {
		double change=10000;

		try {
			voronoiDiagram.tessellate();
			change = voronoiDiagram.adjustCentroids(myImage);
		}
		catch (Exception e) {
			logger.error("Failed to evolve", e);
		}

		return change;
	}

	@Override
	public void finish() {
		keepIterating=false;
		writeOutCells();
	}

	@Override
	public void render(GL2 gl2) {
		lock.lock();
		try {
			voronoiDiagram.renderEdges(gl2);
			if (renderMode == 0) voronoiDiagram.renderPoints(gl2);
			if (renderMode == 1 && solution != null) drawTour(gl2);
		}
		finally {
			lock.unlock();
		}
	}

	private void drawTour(GL2 gl2) {
		List<VoronoiCell> cells = voronoiDiagram.getCells();

		gl2.glColor3f(0, 0, 0);
		gl2.glBegin(GL2.GL_LINE_STRIP);
		for (int i = 0; i < solutionContains; ++i) {
			VoronoiCell c = cells.get(solution[i]);
			gl2.glVertex2d(c.centroid.x, c.centroid.y);
		}
		gl2.glEnd();
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
				logger.debug("{}: {}mm", formatTime(t_elapsed), StringHelper.formatDouble(len));
			}
			progress = new_progress;
			setProgress((int) progress);
		}
	}

	private int ti(int x) {
		return (x + solutionContains) % solutionContains;
	}

	/**
	 * we have s1,s2...e-1,e.  check if s1,e-1,...s2,e is shorter
	 * @return true if something was improved.
	 */
	public boolean flipTests() {
		boolean once = false;
		int start, end, j, best_end;
		double a, b, c, d, temp_diff, best_diff;

		for (start = 0; start < solutionContains * 2 - 2 && !isThreadCancelled(); ++start) {
			a = calculateLengthSq(solution[ti(start)], solution[ti(start + 1)]);
			best_end = -1;
			best_diff = 0;

			for (end = start + 2; end < start + solutionContains && !isThreadCancelled(); ++end) {
				// before
				b = calculateLengthSq(solution[ti(end)], solution[ti(end - 1)]);
				// after
				c = calculateLengthSq(solution[ti(start)], solution[ti(end - 1)]);
				d = calculateLengthSq(solution[ti(end)], solution[ti(start + 1)]);

				temp_diff = (a + b) - (c + d);
				if (best_diff < temp_diff) {
					best_diff = temp_diff;
					best_end = end;
				}
			}

			if (best_end != -1 && !isThreadCancelled()) {
				once = true;
				// do the flip
				int begin = start + 1;
				int finish = best_end;
				if (best_end < begin)
					finish += solutionContains;
				int half = (finish - begin) / 2;
				int temp;

				lock.lock();
				try {
					// Makelangelo.getSingleton().Log("<font color='red'>flipping
					// "+(finish-begin));
					for (j = 0; j < half; ++j) {
						temp = solution[ti(begin + j)];
						solution[ti(begin + j)] = solution[ti(finish - 1 - j)];
						solution[ti(finish - 1 - j)] = temp;
					}
				}
				finally {
					lock.unlock();
				}
				updateProgress(len, 1);
			}
		}
		return once;
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
		logger.debug("Finding greedy tour solution...");

		int i, j;
		double w, bestw;
		int besti;

		solutionContains = 0;
		for( VoronoiCell c : voronoiDiagram.getCells() ) {
			float v = 1.0f - (float) myImage.sample1x1( (int) c.centroid.x, (int) c.centroid.y) / 255.0f;
			if (v * 5 > minDotSize)
				solutionContains++;
		}

		try {
			solution = new int[solutionContains];

			// put all the points in the solution in no particular order.
			j = 0;
			i=0;
			for( VoronoiCell c : voronoiDiagram.getCells() ) {
				float v = 1.0f - (float) myImage.sample1x1( (int) c.centroid.x, (int) c.centroid.y) / 255.0f;
				if (v * 5 > minDotSize) solution[j++] = i;
				++i;
			}

			int scount = 0;

			do {
				// Find the nearest point not already in the line.
				// Any solution[n] where n>scount is not in the line.
				bestw = calculateLengthSq(solution[scount], solution[scount + 1]);
				besti = scount + 1;
				for (i = scount + 2; i < solutionContains; ++i) {
					w = calculateLengthSq(solution[scount], solution[i]);
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
			logger.error("Failed to find a greedy tour solution", e);
		}
	}

	private double calculateLengthSq(int a, int b) {
		List<VoronoiCell> cells = voronoiDiagram.getCells();

		assert (a >= 0 && a < cells.size());
		assert (b >= 0 && b < cells.size());
		double x = cells.get(a).centroid.x - cells.get(b).centroid.x;
		double y = cells.get(a).centroid.y - cells.get(b).centroid.y;
		return x * x + y * y;
	}

	private double calculateLength(int a, int b) {
		return Math.sqrt(calculateLengthSq(a, b));
	}

	/**
	 * write cell centroids to a {@link Turtle}.
	 */
	private void writeOutCells() {
		turtle = new Turtle();

		List<VoronoiCell> cells = voronoiDiagram.getCells();

		int i;
		for (i = 0; i < solutionContains; ++i) {
			double x = cells.get(solution[i]).centroid.x;
			double y = cells.get(solution[i]).centroid.y;
			if(i==0) turtle.jumpTo(x, y);
			else turtle.moveTo(x, y);
		}
	}

	public void setNumCells(int value) {
		if(value<1) value=1;
		numCells = value;
	}
	
	public int getNumCells() {
		return numCells;
	}
	
	public void setMinDotSize(double value) {
		if(value<0.001) value=0.001f;
		minDotSize = value;
	}
	
	public double getMinDotSize() {
		return minDotSize;
	}
}