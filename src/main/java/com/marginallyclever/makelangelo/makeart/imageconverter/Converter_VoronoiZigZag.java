package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.voronoi.VoronoiCell;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.select.SelectToggleButton;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Dithering using a particle system.
 * See <a href="http://en.wikipedia.org/wiki/Fortune%27s_algorithm">Fortune's algorithm</a>
 * and <a href="http://skynet.ie/~sos/mapviewer/voronoi.php">Voronoi</a>
 * @author Dan
 * @since 7.0.0?
 */
public class Converter_VoronoiZigZag extends Converter_Voronoi {
	private static final Logger logger = LoggerFactory.getLogger(Converter_VoronoiZigZag.class);
	private int renderMode;
	private boolean lowNoise;

	public Converter_VoronoiZigZag() {
		super();

		SelectToggleButton selectOptimizePath = new SelectToggleButton("optimizePath",Translator.get("VoronoiZigZag.optimizePath"));
		add(selectOptimizePath);
		selectOptimizePath.addSelectListener(evt -> {
			lowNoise = selectOptimizePath.isSelected();
			if(lowNoise) {
				logger.debug("Running Lin/Kerighan optimization...");
				renderMode = 1;
				sortLowPassToEnd();
				greedySort();
			} else {
				logger.debug("Evolving...");
				renderMode = 0;
			}
		});
	}

	@Override
	public String getName() {
		return Translator.get("VoronoiZigZagName");
	}

	@Override
	public void start(Paper paper, TransformedImage image) {
		renderMode = 0;
		lowNoise = false;
		turtle = new Turtle();
		super.start(paper, image);
	}

	@Override
	public boolean iterate() {
		if(!lowNoise) {
			super.iterate();
			return true;
		}

		lock.lock();
		try {
			optimizeTour();
		}
        finally {
			lock.unlock();
		}
		return true;
	}

	@Override
	public void resume() {}

	@Override
	public void render(GL2 gl2) {
		super.render(gl2);
		if(getThread().getPaused()) return;

		gl2.glPushMatrix();
		gl2.glTranslated(myPaper.getCenterX(),myPaper.getCenterY(),0);

		lock.lock();
		try {
			if (renderMode == 0) renderPoints(gl2);
			if (renderMode == 1 && cells != null) drawTour(gl2);
		}
		finally {
			lock.unlock();
		}

		gl2.glPopMatrix();
	}

	private void renderPoints(GL2 gl2) {
		int lpc = getLowpassCutoff();

		lock.lock();
		try {
			gl2.glBegin(GL2.GL_POINTS);
			for( VoronoiCell c : cells ) {
				if(c.weight<lpc) {
					gl2.glColor3f(1, 0, 0);
				} else {
					gl2.glColor3f(0, 0, 0);
				}
				gl2.glVertex2d(c.center.x, c.center.y);
			}
			gl2.glEnd();
		}
		finally {
			lock.unlock();
		}
	}

	private void drawTour(GL2 gl2) {
		int lpc = getLowpassCutoff();

		lock.lock();
		try {
			gl2.glColor3f(0, 0, 0);
			gl2.glBegin(GL2.GL_LINE_STRIP);
			for (VoronoiCell c : cells) {
				if (c.weight < lpc) break;
				gl2.glVertex2d(c.center.x, c.center.y);
			}
			gl2.glEnd();
		}
		finally {
			lock.unlock();
		}
	}

	private void optimizeTour() {
		flipTests();
	}

	private int ti(int x) {
		int solutionContains = cells.size();
		return (x + solutionContains) % solutionContains;
	}

	/**
	 * Suppose we have a subset of points in a line [a,b...c,d].
	 * If distance(a,b)+distance(c,d) > distance(a,c)+distance(b,d) then flip b through c, inclusive.
	 * BUT only flip the candidate which will have the most effect.
	 */
	public void flipTests() {
		int lpc = getLowpassCutoff();
		int size = cells.size();

		for (int start = 0; start < size - 2 && !isThreadCancelled(); ++start) {
			VoronoiCell a = cells.get(ti(start  ));
			VoronoiCell b = cells.get(ti(start+1));
			if(a.weight<lpc || b.weight<lpc) break;

			double dAB = calculateLengthSq(a,b);
			int bestIndex = -1;
			double bestDiff = 0;

			for (int end = start + 2; end < size && !isThreadCancelled(); ++end) {
				VoronoiCell c = cells.get(ti(end-1));
				VoronoiCell d = cells.get(ti(end  ));
				if(c.weight<lpc || d.weight<lpc) break;

				double dOriginal = dAB + calculateLengthSq(c, d);
				double dAC = calculateLengthSq(a, c);
				double dBD = calculateLengthSq(b, d);
				double dFlipped = dAC+dBD;

				double diff = dOriginal - dFlipped;
				if (bestDiff < diff) {
					bestDiff = diff;
					bestIndex = end;
				}
			}

			if (bestIndex != -1 && !isThreadCancelled()) {
				flipAllBetween(start+1,bestIndex);
			}
		}
	}

	private void flipAllBetween(int begin, int finish) {
		if (finish < begin) finish += cells.size();
		int half = (finish - begin) / 2;

		//logger.debug("flipping {} {}",finish,begin);
		for (int j = 0; j < half; ++j) {
			int a1 = ti(begin + j);
			int b1 = ti(finish-1 - j);
			swapSolution(a1,b1);
		}
	}

	private void swapSolution(int a,int b) {
		VoronoiCell temp = cells.get(a);
		cells.set(a, cells.get(b));
		cells.set(b,temp);
	}

	private double calculateLengthSq(VoronoiCell a, VoronoiCell b) {
		double x = a.center.x - b.center.x;
		double y = a.center.y - b.center.y;
		return x * x + y * y;
	}

	// find all the values below the cutoff and move them to the end of the list.
	private void sortLowPassToEnd() {
		lock.lock();
		try {
			int lpc = getLowpassCutoff();
			List<VoronoiCell> toMove = new ArrayList<>();
			for( VoronoiCell c : cells) {
				if(c.weight<lpc) {
					toMove.add(c);
				}
			}
			cells.removeAll(toMove);
			cells.addAll(toMove);
		}
			finally {
			lock.unlock();
		}
	}

	/**
	 * Starting with point 0, find the next nearest point and repeat until all
	 * points have been "found".
	 */
	private void greedySort() {
		logger.debug("greedy tour started...");

		int lpc = getLowpassCutoff();

		lock.lock();
		try {
			// do a greedy sort
			int size = cells.size();
			for (int i = 0; i < size-1; ++i) {
				VoronoiCell p0 = cells.get(i);
				if(p0.weight<lpc) break;

				double bestDistance = Double.MAX_VALUE;
				int bestIndex = i + 1;
				for (int j = i + 1; j < size; ++j) {
					// Find the nearest point not already in the line.
					VoronoiCell p1 = cells.get(j);
					if(p1.weight<lpc) break;

					double d = calculateLengthSq(p0, p1);
					if (bestDistance > d) {
						bestDistance = d;
						bestIndex = j;
					}
				}
				if (i + 1 != bestIndex) {
					swapSolution(i + 1, bestIndex);
				}
			}
		}
		finally {
			lock.unlock();
		}
		logger.debug("greedy tour done.");
	}

	/**
	 * write cell centers to a {@link Turtle}.
	 */
	@Override
	public void writeOutCells() {
		int lpc = getLowpassCutoff();
		turtle = new Turtle();

		double cx = myPaper.getCenterX();
		double cy = myPaper.getCenterY();

		boolean first=true;
		for( VoronoiCell c : cells) {
			if(c.weight<lpc) continue;
			double x = cx + c.center.x;
			double y = cy + c.center.y;
			if(first) {
				turtle.jumpTo(x, y);
				first=false;
			} else turtle.moveTo(x, y);
		}
	}
}