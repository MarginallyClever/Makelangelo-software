package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.jogamp.opengl.GL2;
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
	private static boolean drawBorders = false;
	private static boolean drawVoronoi = false;
	private static int numCells = 1000;
	private static double maxDotSize = 5.0f;
	private static double minDotSize = 1.0f;
	private static double cutoff = 0;
	
	private final ReentrantLock lock = new ReentrantLock();
	private final VoronoiDiagram voronoiDiagram = new VoronoiDiagram();

	private int iterations;
	
	public Converter_VoronoiStippling() {
		super();
	}
	
	@Override
	public String getName() {
		return Translator.get("Converter_VoronoiStippling.Name");
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().equals("drawQuadTree")) setDrawBorders((boolean)evt.getNewValue());
		else if(evt.getPropertyName().equals("drawVoronoi")) setDrawVoronoi((boolean)evt.getNewValue());
		else {
			boolean isDirty=false;
			if(evt.getPropertyName().equals("cells")) {
				isDirty=true;
				setNumCells((int)evt.getNewValue());
			}
			if(evt.getPropertyName().equals("max")) {
				isDirty=true;
				setMinDotSize((double)evt.getNewValue());
			}
			if(evt.getPropertyName().equals("min")) {
				isDirty=true;
				setMaxDotSize((double)evt.getNewValue());
			}
			if(evt.getPropertyName().equals("cutoff")) {
				isDirty=true;
				setCutoff((double)evt.getNewValue());
			}
			if(isDirty) restart();
		}
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
		double noiseLevel = evolveCells();
		System.out.println(iterations+": "+noiseLevel+"\t"+numCells+"\t"+(noiseLevel/(float)numCells));
		return keepIterating;
	}

	/**
	 * Jiggle the dots until they make a nice picture
	 */
	private double evolveCells() {
		double change=10000;

		lock.lock();
		try {
			voronoiDiagram.tessellate();
			change = voronoiDiagram.adjustCentroids(myImage);
		}
		catch (Exception e) {
			logger.error("Failed to evolve", e);
		}
		finally {
			lock.unlock();
		}
		return change;
	}

	private void restart() {		
		if(myImage==null) return;

		lock.lock();
		try {
			iterations = 0;
			keepIterating = true;
			voronoiDiagram.initializeCells(numCells,myPaper.getMarginRectangle(), 0.5);
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
		
		// draw cell edges
		if(drawVoronoi) voronoiDiagram.renderEdges(gl2);
		renderDots(gl2);  // dots sized by darkness
		
		lock.unlock();
	}

	private void renderDots(GL2 gl2) {
		double scale = maxDotSize - minDotSize;
		gl2.glColor3f(0, 0, 0);

		for( VoronoiCell c : voronoiDiagram.getCells() ) {
			double val = c.weight/255.0;
			if(val>cutoff) {
				double x = c.centroid.x;
				double y = c.centroid.y;
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
	 * write cell centroids to gcode.
	 */
	private void writeOutCells() {
		turtle = new Turtle();

		double toolDiameter = 1;

		for( VoronoiCell c : voronoiDiagram.getCells() ) {
			double x = c.centroid.x;
			double y = c.centroid.y;
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
	
	public void setDrawBorders(boolean arg0) {
		drawBorders=arg0;
	}
	public boolean getDrawBorders() {
		return drawBorders;
	}

	public void setDrawVoronoi(boolean arg0) {
		drawVoronoi = arg0;
	}
	public boolean getDrawVoronoi() {
		return drawVoronoi;
	}
}