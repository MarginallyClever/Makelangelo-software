package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.convenience.voronoi.VoronoiCell;
import com.marginallyclever.convenience.voronoi.VoronoiTesselator2;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.Filter_BlackAndWhite;
import com.marginallyclever.makelangelo.preview.PreviewListener;
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
public class Converter_VoronoiStippling extends ImageConverter implements PreviewListener {
	private static final Logger logger = LoggerFactory.getLogger(Converter_VoronoiStippling.class);
	private static boolean drawVoronoi = false;
	private static int numCells = 1000;
	private static double maxDotSize = 5.0f;
	private static double minDotSize = 1.0f;
	private static double cutoff = 0;
	private final VoronoiTesselator2 voronoiDiagram = new VoronoiTesselator2();
	private List<VoronoiCell> cells = new ArrayList<>();

	private final Lock lock = new ReentrantLock();

	private int iterations;
	
	public Converter_VoronoiStippling() {
		super();
	}
	
	@Override
	public String getName() {
		return Translator.get("Converter_VoronoiStippling.Name");
	}


	@Override
	public void setImage(TransformedImage img) {
		// make black & white
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		myImage = bw.filter(img);
		keepIterating=true;
		restart();
	}

	@Override
	public boolean iterate() {
		iterations++;
		lock.lock();
		try {
			double noiseLevel = evolveCells();
			System.out.println(iterations+": "+noiseLevel+" "+(noiseLevel/(float)numCells));
			keepIterating = noiseLevel > numCells/2;
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
				for (int x = (int) e.getMinX(); x < (int) e.getMaxX(); ++x) {
					Point c = factory.createPoint(new Coordinate(x,y));
					if(poly.contains(c) && image.canSampleAt(x,y)) {
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

	private void tessellateNow() {
		Point2D [] points = new Point2D[numCells];
		int i=0;
		for(VoronoiCell cell : cells) {
			points[i++] = new Point2D(cell.center.x,cell.center.y);
		}
		voronoiDiagram.tessellate(points,myPaper.getMarginRectangle(),1e-6);
	}

	public void restart() {
		if(myImage==null) return;

		lock.lock();
		try {
			iterations = 0;
			keepIterating = true;

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
	public void finish() {
		keepIterating=false;
		writeOutCells();
	}

	@Override
	public void render(GL2 gl2) {
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
			myImage.canSampleAt(x,y);
			double val = 1.0 - myImage.sample(x,y,3)/255.0;
			if(val>cutoff) {
				double r = val * scale + minDotSize;
				gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				for(int j = 0; j < 8; ++j) {
					double d = j * 2.0 * Math.PI / 8.0;
					gl2.glVertex2d(x + Math.cos(d) * r,
								   y + Math.sin(d) * r);
				}
				gl2.glEnd();
			}
		}
	}

	/**
	 * write cell centers to gcode.
	 */
	private void writeOutCells() {
		turtle = new Turtle();

		double toolDiameter = 1;

		for( VoronoiCell c : cells ) {
			double x = c.center.x;
			double y = c.center.y;
			double val = c.weight/255.0f;
			if(val<cutoff) continue;

			double r = val * (maxDotSize-minDotSize);

			double newX=0,newY=0;
			boolean first=true;
			// filled circles
			while (r > 0) {
				float detail = (float)Math.ceil(Math.PI * r*2 / (toolDiameter*4));
				if (detail < 4) detail = 4;
				if (detail > 20) detail = 20;
				for (float j = 0; j <= detail; ++j) {
					double v = Math.PI * 2.0f * j / detail;
					newX = x + r * Math.cos(v);
					newY = y + r * Math.sin(v);
					if(first) {
						if(isInsidePaperMargins(newX,newY)) {
							turtle.jumpTo(newX, newY);
							first=false;
						}
					} else {
						turtle.moveTo(newX, newY);
					}
				}
				r -= toolDiameter;
			}
			if(first==false) {
				turtle.penUp();
			}
		}
	}

	public void setNumCells(int value) {
		if(value<1) value=1;
		if(numCells!=value) {
			numCells = value;
			if(keepIterating) {
				restart();
			}
		}
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
	
	public void setCutoff(double value) {
		if(value<0f) value=0f;
		cutoff = value;
	}
	public double getCutoff() {
		return cutoff;
	}
	
	public double getMaxDotSize() {
		return maxDotSize;
	}
	public void setMaxDotSize(double value) {
		if(value<=minDotSize) value=minDotSize+1;
		maxDotSize = value;
	}

	public void setDrawVoronoi(boolean arg0) {
		drawVoronoi = arg0;
	}
	public boolean getDrawVoronoi() {
		return drawVoronoi;
	}
}