package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.voronoi.VoronoiCell;
import com.marginallyclever.convenience.voronoi.VoronoiTesselator2;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.Filter_Greyscale;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.preview.PreviewListener;
import com.marginallyclever.makelangelo.select.SelectInteger;
import com.marginallyclever.makelangelo.select.SelectSlider;
import com.marginallyclever.makelangelo.select.SelectToggleButton;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.locationtech.jts.geom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Dithering using a particle system
 * See <a href="http://en.wikipedia.org/wiki/Fortune%27s_algorithm">...</a>
 * See <a href="http://skynet.ie/~sos/mapviewer/voronoi.php">...</a>
 * @author Dan
 * @since 7.0.0?
 */
public class Converter_VoronoiZigZag extends ImageConverterIterative implements PreviewListener {
	private static final Logger logger = LoggerFactory.getLogger(Converter_VoronoiZigZag.class);
	private static int numCells = 9000;
	private static int lowpassCutoff = 128;

	private final VoronoiTesselator2 voronoiDiagram = new VoronoiTesselator2();
	private final List<VoronoiCell> cells = new ArrayList<>();
	private final Lock lock = new ReentrantLock();

	private final List<VoronoiCell> solution = new ArrayList<>();
	private int renderMode;
	private boolean lowNoise;
	private int iterations;

	public Converter_VoronoiZigZag() {
		super();

		SelectToggleButton selectOptimizePath = new SelectToggleButton("optimizePath",Translator.get("VoronoiZigZag.optimizePath"));
		add(selectOptimizePath);
		selectOptimizePath.addPropertyChangeListener(evt -> {
			lowNoise = selectOptimizePath.isSelected();
			if(lowNoise) {
				logger.debug("Running Lin/Kerighan optimization...");
				renderMode = 1;
				greedyTour();
			} else {
				logger.debug("Evolving...");
				renderMode = 0;
			}
		});
		SelectInteger selectCells = new SelectInteger("cells",Translator.get("Converter_VoronoiStippling.CellCount"),getNumCells());
		add(selectCells);
		SelectSlider selectCutoff = new SelectSlider("cutoff",Translator.get("Converter_VoronoiStippling.Cutoff"),255,0,getLowpassCutoff());
		add(selectCutoff);

		selectCells.addPropertyChangeListener(evt->{
			setNumCells((int)evt.getNewValue());
			fireRestart();
		});
		selectCutoff.addPropertyChangeListener(evt-> setLowpassCutoff((int)evt.getNewValue()));
	}

	@Override
	public String getName() {
		return Translator.get("VoronoiZigZagName");
	}

	@Override
	public void start(Paper paper, TransformedImage image) {
		renderMode = 0;
		Filter_Greyscale bw = new Filter_Greyscale(255);
		super.start(paper, bw.filter(image));

		lock.lock();
		try {
			lowNoise=false;
			iterations = 0;

			Rectangle2D bounds = myPaper.getMarginRectangle();
			cells.clear();
			for(int i=0;i<numCells;++i) {
				cells.add(new VoronoiCell(
						Math.random()*bounds.getWidth()+bounds.getMinX(),
						Math.random()*bounds.getHeight()+bounds.getMinY()));
			}
		}
		finally {
			lock.unlock();
		}
	}
	
	@Override
	public boolean iterate() {
		iterations++;

		lock.lock();
		try {
			if(lowNoise) {
				optimizeTour();
			} else {
				double noiseLevel = evolveCells();
				System.out.println(iterations+": "+noiseLevel+" "+(noiseLevel/(float)numCells));
			}
		}
		finally {
			lock.unlock();
		}
		return true;
	}

	@Override
	public void generateOutput() {
		writeOutCells();

		fireConversionFinished();
	}

	@Override
	public void resume() {}

	/**
	 * Jiggle the dots until they make a nice picture
	 */
	private double evolveCells() {
		double change=10000;

		try {
			voronoiDiagram.tessellate(cells,myPaper.getMarginRectangle(),1e-6);
			change = adjustCenters(myImage);
		}
		catch (Exception e) {
			logger.error("Failed to evolve", e);
		}

		return change;
	}

	private double adjustCenters(TransformedImage image) {
		double change=0;
		GeometryFactory factory = new GeometryFactory();

		for(int i=0;i<voronoiDiagram.getNumHulls();++i) {
			Polygon poly = voronoiDiagram.getHull(i);
			VoronoiCell cell = cells.get(i);

			// sample every image coordinate inside the voronoi cell and find the weighted center
			double wx=0,wy=0;
			double weight=0;
			int hits=0;

			Point center = poly.getCentroid();
			cell.center.set(center.getX(),center.getY());
			Envelope e = poly.getEnvelopeInternal();
			int miny = (int) Math.floor(e.getMinY());
			int maxy = (int) Math.ceil(e.getMaxY());
			for(int y=miny;y<maxy;++y) {
				int x0 = findLeftEdge(poly,e,y,factory);
				int x1 = findRightEdge(poly,e,y,factory);
				for (int x = x0; x <=x1; ++x) {
					if(image.canSampleAt(x,y)) {
						double sampleWeight = 255.0 - image.sample(x,y,1);
						weight += sampleWeight;
						wx += sampleWeight*x;
						wy += sampleWeight*y;
						hits++;
					}
				}
			}
			if(hits>0 && weight>0) {
				cell.weight = weight / hits;
				wx /= weight;
				wy /= weight;
				double dx = wx - cell.center.x;
				double dy = wy - cell.center.y;
				change += Math.sqrt(dx*dx+dy*dy);
				cell.center.set(wx,wy);
			}
		}
		return change;
	}

	private int findLeftEdge(Polygon poly, Envelope e,int y,GeometryFactory factory) {
		int minx = (int) Math.floor(e.getMinX());
		int maxx = (int) Math.ceil(e.getMaxX());
		int x;
		for(x = minx; x < maxx; ++x) {
			Point c = factory.createPoint(new Coordinate(x,y));
			if(poly.contains(c)) break;
		}
		return x;
	}

	private int findRightEdge(Polygon poly, Envelope e,int y,GeometryFactory factory) {
		int minx = (int) Math.floor(e.getMinX());
		int maxx = (int) Math.ceil(e.getMaxX());
		int x;
		for(x = maxx; x > minx; --x) {
			Point c = factory.createPoint(new Coordinate(x,y));
			if(poly.contains(c)) break;
		}
		return x;
	}
	
	@Override
	public void stop() {
		super.stop();
		lock.lock();
		try {
			writeOutCells();
		}
		finally {
			lock.unlock();
		}
		fireConversionFinished();
	}

	@Override
	public void render(GL2 gl2) {
		if(getThread().getPaused()) return;

		lock.lock();
		try {
			renderEdges(gl2);
			if (renderMode == 0) renderPoints(gl2);
			if (renderMode == 1 && solution != null) drawTour(gl2);
		}
		finally {
			lock.unlock();
		}
	}

	private void renderPoints(GL2 gl2) {
		gl2.glColor3f(0, 0, 0);

		gl2.glBegin(GL2.GL_POINTS);
		for( VoronoiCell c : cells ) {
			if (c.weight < lowpassCutoff) continue;
			gl2.glVertex2d(c.center.x, c.center.y);
		}
		gl2.glEnd();
	}

	private void renderEdges(GL2 gl2) {
		gl2.glColor3d(0.9, 0.9, 0.9);

		for(int i=0;i<voronoiDiagram.getNumHulls();++i) {
			Polygon poly = voronoiDiagram.getHull(i);
			gl2.glBegin(GL2.GL_LINE_LOOP);
			for (Coordinate p : poly.getExteriorRing().getCoordinates()) {
				gl2.glVertex2d(p.x, p.y);
			}
			gl2.glEnd();
		}
	}

	private void drawTour(GL2 gl2) {
		gl2.glColor3f(0, 0, 0);
		gl2.glBegin(GL2.GL_LINE_STRIP);
		for( VoronoiCell c : solution ) {
			gl2.glVertex2d(c.center.x, c.center.y);
		}
		gl2.glEnd();
	}

	private void optimizeTour() {
		// @TODO: make these optional for the very thorough people
		// once|=transposeForwardTest();
		// once|=transposeBackwardTest();

		flipTests();
	}

	private int ti(int x) {
		int solutionContains = solution.size();
		return (x + solutionContains) % solutionContains;
	}

	/**
	 * we have s1,s2...e-1,e.  check if s1,e-1..(flip everything)...s2,e is shorter
	 */
	public void flipTests() {
		int start, end, j, bestIndex;
		double bestDiff;

		int solutionContains = solution.size();

		for (start = 0; start < solutionContains - 2 && !isThreadCancelled(); ++start) {
			VoronoiCell p0 = solution.get(ti(start));
			VoronoiCell p1 = solution.get(ti(start+1));

			double a = calculateLengthSq(p0,p1);
			bestIndex = -1;
			bestDiff = 0;

			for (end = start + 2; end < solutionContains && !isThreadCancelled(); ++end) {
				VoronoiCell p2 = solution.get(ti(end-1));
				VoronoiCell p3 = solution.get(ti(end));

				double b = a + calculateLengthSq(p2, p3);
				double c = calculateLengthSq(p0, p2) + calculateLengthSq(p1, p3);
				// the existing model is distance b.  the new possibility is c.
				double diff = b - c;
				if (bestDiff < diff) {
					bestDiff = diff;
					bestIndex = end;
				}
			}

			if (bestIndex != -1 && !isThreadCancelled()) {
				// do the flip
				int begin = start + 1;
				int finish = bestIndex;
				if (bestIndex < begin) finish += solutionContains;
				int half = (finish - begin) / 2;

				logger.debug("flipping {} {}",finish,begin);
				for (j = 0; j < half; ++j) {
					int a1 = ti(begin + j);
					int b1 = ti(finish-1 - j);
					swapSolution(a1,b1);
				}
			}
		}
	}

	/**
	 * Returns the travel distance of the solution path.
	 * @return the travel distance of the solution path.
	 */
	@Deprecated
	private double getTourLength() {
		if(solution.size()<2) return 0;

		double sum = 0;
		Iterator<VoronoiCell> iter = solution.iterator();
		VoronoiCell p0 = iter.next();
		while(iter.hasNext()) {
			VoronoiCell p1 = iter.next();
			sum += calculateLength(p0,p1);
			p0=p1;
		}
		return sum;
	}

	/**
	 * Starting with point 0, find the next nearest point and repeat until all
	 * points have been "found".
	 */
	private void greedyTour() {
		logger.debug("greedy tour started...");

		lock.lock();
		try {
			// collect all cells above the cutoff value.
			solution.clear();
			for (VoronoiCell c : cells) {
				if (c.weight > lowpassCutoff) {
					solution.add(c);
				}
			}

			// do a greedy sort
			int size = solution.size();
			for (int i = 0; i < size - 1; ++i) {
				VoronoiCell p0 = solution.get(i);
				double bestDistance = Double.MAX_VALUE;
				int bestIndex = i + 1;
				for (int j = i + 1; j < size; ++j) {
					// Find the nearest point not already in the line.
					VoronoiCell p1 = solution.get(j);
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

	private void swapSolution(int a,int b) {
		VoronoiCell temp = solution.get(a);
		solution.set(a,solution.get(b));
		solution.set(b,temp);
	}

	private double calculateLengthSq(VoronoiCell a, VoronoiCell b) {
		double x = a.center.x - b.center.x;
		double y = a.center.y - b.center.y;
		return x * x + y * y;
	}

	private double calculateLength(VoronoiCell a, VoronoiCell b) {
		return Math.sqrt(calculateLengthSq(a, b));
	}

	/**
	 * write cell centers to a {@link Turtle}.
	 */
	private void writeOutCells() {
		turtle = new Turtle();

		boolean first=true;
		for( VoronoiCell c : solution ) {
			double x = c.center.x;
			double y = c.center.y;
			if(first) {
				turtle.jumpTo(x, y);
				first=false;
			} else turtle.moveTo(x, y);
		}
	}

	public void setNumCells(int value) {
		numCells = Math.max(1,value);
	}
	
	public int getNumCells() {
		return numCells;
	}
	
	public void setLowpassCutoff(int value) {
		lowpassCutoff = Math.max(1,Math.min(255,value));
	}
	
	public int getLowpassCutoff() {
		return lowpassCutoff;
	}
}