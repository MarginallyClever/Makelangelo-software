package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.convenience.voronoi.VoronoiCell;
import com.marginallyclever.convenience.voronoi.VoronoiTesselator2;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.InfillTurtle;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.Filter_BlackAndWhite;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.preview.PreviewListener;
import com.marginallyclever.makelangelo.select.SelectBoolean;
import com.marginallyclever.makelangelo.select.SelectDouble;
import com.marginallyclever.makelangelo.select.SelectInteger;
import com.marginallyclever.makelangelo.select.SelectSlider;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.locationtech.jts.geom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Voronoi graph based stippling.
 * See http://en.wikipedia.org/wiki/Fortune%27s_algorithm
 * See http://skynet.ie/~sos/mapviewer/voronoi.php
 * @author Dan
 * @since 7.0.0?
 */
public class Converter_VoronoiStippling extends IterativeImageConverter implements PreviewListener {
	private static final Logger logger = LoggerFactory.getLogger(Converter_VoronoiStippling.class);
	private static boolean drawVoronoi = false;
	private static int numCells = 1000;
	private static double maxDotSize = 5.0f;
	private static double minDotSize = 1.0f;
	private static int cutoff = 128;
	private final VoronoiTesselator2 voronoiDiagram = new VoronoiTesselator2();
	private List<VoronoiCell> cells = new ArrayList<>();

	private final Lock lock = new ReentrantLock();

	private int iterations;

	public Converter_VoronoiStippling() {
		super();

		SelectInteger selectCells = new SelectInteger("cells", Translator.get("Converter_VoronoiStippling.CellCount"), getNumCells());
		SelectDouble selectMax = new SelectDouble("max", Translator.get("Converter_VoronoiStippling.DotMax"), getMaxDotSize());
		SelectDouble selectMin = new SelectDouble("min", Translator.get("Converter_VoronoiStippling.DotMin"), getMinDotSize());
		SelectSlider selectCutoff = new SelectSlider("cutoff", Translator.get("Converter_VoronoiStippling.Cutoff"), 255,0, getCutoff());
		SelectBoolean selectDrawVoronoi = new SelectBoolean("drawVoronoi", Translator.get("Converter_VoronoiStippling.DrawBorders"), getDrawVoronoi());

		add(selectCells);
		add(selectMax);
		add(selectMin);
		add(selectCutoff);
		add(selectDrawVoronoi);

		selectCells.addPropertyChangeListener(evt -> {
			setNumCells((int) evt.getNewValue());
			fireRestart();
		});
		selectMax.addPropertyChangeListener(evt -> setMaxDotSize((double) evt.getNewValue()));
		selectMin.addPropertyChangeListener(evt -> setMinDotSize((double) evt.getNewValue()));
		selectCutoff.addPropertyChangeListener(evt -> setCutoff((int) evt.getNewValue()));
		selectDrawVoronoi.addPropertyChangeListener(evt -> setDrawVoronoi((boolean) evt.getNewValue()));
	}
	
	@Override
	public String getName() {
		return Translator.get("Converter_VoronoiStippling.Name");
	}

	@Override
	public void start(Paper paper, TransformedImage image) {
		// make black & white
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		super.start(paper, bw.filter(image));

		lock.lock();
		try {
			turtle = new Turtle();

			iterations=0;

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
	public void resume() {
		turtle = new Turtle();
		fireConversionFinished();
	}

	@Override
	public boolean iterate() {
		iterations++;
		lock.lock();
		try {
			double noiseLevel = evolveCells();
			System.out.println(iterations+": "+noiseLevel+" "+(noiseLevel/(float)numCells));
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

	/**
	 * Jiggle the dots until they make a nice picture
	 */
	private double evolveCells() {
		double change=10000;

		try {
			tessellateNow();
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

			Point centroid = poly.getCentroid();
			cell.center.set(centroid.getX(),centroid.getY());
			Envelope e = poly.getEnvelopeInternal();
			for(int y=(int)e.getMinY();y<(int)e.getMaxY();++y) {
				int x0 = findLeftEdge(poly,e,y,factory);
				int x1 = findRightEdge(poly,e,y,factory);
				for (int x = x0; x <= x1; ++x) {
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
			} else {
				cell.weight=0;
			}
		}
		return change;
	}

	private int findLeftEdge(Polygon poly, Envelope e,int y,GeometryFactory factory) {
		int x;
		for(x = (int) e.getMinX(); x < (int) e.getMaxX(); ++x) {
			Point c = factory.createPoint(new Coordinate(x,y));
			if(poly.contains(c)) break;
		}
		return x;
	}

	private int findRightEdge(Polygon poly, Envelope e,int y,GeometryFactory factory) {
		int x;
		for(x = (int) e.getMaxX(); x > (int) e.getMinX(); --x) {
			Point c = factory.createPoint(new Coordinate(x,y));
			if(poly.contains(c)) break;
		}
		return x;
	}

	private void tessellateNow() {
		Point2D [] points = new Point2D[numCells];
		int i=0;
		for(VoronoiCell cell : cells) {
			points[i++] = new Point2D(cell.center.x,cell.center.y);
		}
		voronoiDiagram.tessellate(points,myPaper.getMarginRectangle(),1e-6);
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
			if (drawVoronoi) renderEdges(gl2);
			renderDots(gl2);
		}
		finally {
			lock.unlock();
		}
	}

	private void renderEdges(GL2 gl2) {
		gl2.glColor3d(0.9, 0.9, 0.9);

		for(int i=0;i<voronoiDiagram.getNumHulls();++i) {
			boolean first = true;
			Polygon poly = voronoiDiagram.getHull(i);
			gl2.glBegin(GL2.GL_LINE_LOOP);
			for (Coordinate p : poly.getExteriorRing().getCoordinates()) {
				gl2.glVertex2d(p.x, p.y);
			}
			gl2.glEnd();
		}
	}

	private void renderDots(GL2 gl2) {
		double scale = maxDotSize - minDotSize;
		gl2.glColor3f(0, 0, 0);

		for( VoronoiCell c : cells ) {
			double x = c.center.x;
			double y = c.center.y;

			double val = c.weight;
			if(val<cutoff) continue;

			double r = ((val-cutoff)/255.0) * scale + minDotSize;
			drawCircle(gl2,x,y,r);
		}
	}

	private void drawCircle(GL2 gl2,double x, double y, double r) {
		gl2.glBegin(GL2.GL_TRIANGLE_FAN);
		float detail = (float)Math.ceil(r * Math.PI * 2.0);
		detail = Math.max(4, Math.min(20,detail));
		for (float j = 0; j <= detail; ++j) {
			double v = j * 2.0 * Math.PI / detail;
			gl2.glVertex2d(
					x + r * Math.cos(v),
					y + r * Math.sin(v) );
		}
		gl2.glEnd();
	}

	/**
	 * write cell centers to gcode.
	 */
	private void writeOutCells() {
		double scale = maxDotSize - minDotSize;
		turtle = new Turtle();

		for( VoronoiCell c : cells ) {
			double x = c.center.x;
			double y = c.center.y;
			double val = c.weight;
			if(val<cutoff) continue;

			double r = ((val-cutoff)/255.0) * scale + minDotSize;

			turtleCircle(x,y,r);
		}
	}

	// filled circles
	private void turtleCircle(double x, double y, double r) {
		double toolDiameter = 1;

		Turtle circle = new Turtle();

		float detail = (float)Math.ceil(r * Math.PI * 2.0);
		detail = Math.max(4, Math.min(20,detail));
		boolean first = true;

		for (float j = 0; j <= detail; ++j) {
			double v = j * 2.0 * Math.PI / detail;
			double newX = x + r * Math.cos(v);
			double newY = y + r * Math.sin(v);
			if(first) {
				circle.jumpTo(newX, newY);
				first=false;
			} else {
				circle.moveTo(newX, newY);
			}
		}

		InfillTurtle filler = new InfillTurtle();
		try {
			turtle.add(circle);
			turtle.add(filler.run(circle));
		} catch(Exception e) {}
	}

	public void setNumCells(int value) {
		value = Math.max(1,value);
		if(numCells!=value) numCells = value;
	}

	public int getNumCells() {
		return numCells;
	}

	public void setMinDotSize(double value) {
		minDotSize = Math.max(0.001,value);
	}
	public double getMinDotSize() {
		return minDotSize;
	}

	public void setCutoff(int value) {
		cutoff = Math.max(0,Math.min(255,value));
	}
	public int getCutoff() {
		return cutoff;
	}

	public double getMaxDotSize() {
		return maxDotSize;
	}
	public void setMaxDotSize(double value) {
		value = Math.max(value,minDotSize+1);
		maxDotSize = value;
	}

	public void setDrawVoronoi(boolean arg0) {
		drawVoronoi = arg0;
	}
	public boolean getDrawVoronoi() {
		return drawVoronoi;
	}
}