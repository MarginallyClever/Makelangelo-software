package com.marginallyclever.converters;

import java.awt.GridLayout;
import java.awt.Point;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jogamp.opengl.GL2;
import com.marginallyclever.basictypes.TransformedImage;
import com.marginallyclever.imageFilters.Filter_BlackAndWhite;
import com.marginallyclever.makelangelo.DrawPanelDecorator;
import com.marginallyclever.makelangelo.Log;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.MakelangeloRobotSettings;
import com.marginallyclever.voronoi.VoronoiCell;
import com.marginallyclever.voronoi.VoronoiCellEdge;
import com.marginallyclever.voronoi.VoronoiGraphEdge;
import com.marginallyclever.voronoi.VoronoiTesselator;


/**
 * Dithering using a particle system
 *
 * @author Dan
 *         http://en.wikipedia.org/wiki/Fortune%27s_algorithm
 *         http://skynet.ie/~sos/mapviewer/voronoi.php
 * @since 7.0.0?
 */
public class Converter_VoronoiStippling extends ImageConverter implements DrawPanelDecorator {
	private ReentrantLock lock = new ReentrantLock();

	private VoronoiTesselator voronoiTesselator = new VoronoiTesselator();
	private VoronoiCell[] cells = new VoronoiCell[1];
	private TransformedImage sourceImage;
	private List<VoronoiGraphEdge> graphEdges = null;
	private static int MAX_GENERATIONS = 400;
	private static int MAX_CELLS = 1000;
	private static float MAX_DOT_SIZE = 5.0f;
	private static float MIN_DOT_SIZE = 1.0f;
	private Point bound_min = new Point();
	private Point bound_max = new Point();
	private int numEdgesInCell;
	private List<VoronoiCellEdge> cellBorder = null;
	private double[] xValuesIn = null;
	private double[] yValuesIn = null;

	private float yBottom, yTop, xLeft, xRight;
	

	@Override
	public String getName() {
		return Translator.get("voronoiStipplingName");
	}

	@Override
	public boolean convert(TransformedImage img,Writer out) throws IOException {
		JTextField text_gens = new JTextField(Integer.toString(MAX_GENERATIONS), 8);
		JTextField text_cells = new JTextField(Integer.toString(MAX_CELLS), 8);
		JTextField text_dot_max = new JTextField(Float.toString(MAX_DOT_SIZE), 8);
		JTextField text_dot_min = new JTextField(Float.toString(MIN_DOT_SIZE), 8);

		JPanel panel = new JPanel(new GridLayout(0, 1));
		panel.add(new JLabel(Translator.get("voronoiStipplingCellCount")));
		panel.add(text_cells);
		panel.add(new JLabel(Translator.get("voronoiStipplingGenCount")));
		panel.add(text_gens);
		panel.add(new JLabel(Translator.get("voronoiStipplingDotMax")));
		panel.add(text_dot_max);
		panel.add(new JLabel(Translator.get("voronoiStipplingDotMin")));
		panel.add(text_dot_min);


		int result = JOptionPane.showConfirmDialog(null, panel, getName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			MAX_GENERATIONS = Integer.parseInt(text_gens.getText());
			MAX_CELLS = Integer.parseInt(text_cells.getText());
			MAX_DOT_SIZE = Float.parseFloat(text_dot_max.getText());
			MIN_DOT_SIZE = Float.parseFloat(text_dot_min.getText());

			// make black & white
			Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
			img = bw.filter(img);

			sourceImage = img;
			
			yBottom = (float)machine.getPaperBottom() * (float)machine.getPaperMargin() * 10;
			yTop    = (float)machine.getPaperTop()    * (float)machine.getPaperMargin() * 10;
			xLeft   = (float)machine.getPaperLeft()   * (float)machine.getPaperMargin() * 10;
			xRight  = (float)machine.getPaperRight()  * (float)machine.getPaperMargin() * 10;
			
			tool = machine.getCurrentTool();

			cellBorder = new ArrayList<>();

			initializeCells(0.001);
			evolveCells();
			writeOutCells(out);

			return true;
		}
		return false;
	}


	@Override
	public void render(GL2 gl2, MakelangeloRobotSettings machine) {
		if (graphEdges == null) return;

		while(lock.isLocked());
		lock.lock();

		gl2.glScalef(0.1f, 0.1f, 1);

		// draw cell edges
		gl2.glColor3f(0.9f, 0.9f, 0.9f);
		gl2.glBegin(GL2.GL_LINES);
		for (VoronoiGraphEdge e : graphEdges) {
			gl2.glVertex2d( e.x1, e.y1 );
			gl2.glVertex2d( e.x2, e.y2 );
		}
		gl2.glEnd();

		// draw cell centers
		gl2.glPointSize(3);
		gl2.glColor3f(0, 0, 0);
		for (VoronoiCell c : cells) {
			float x = (float)c.centroid.getX();
			float y = (float)c.centroid.getY();
			if( sourceImage.canSampleAt(x,y) ) {
				float val = 1.0f - (sourceImage.sample1x1( x, y) / 255.0f);
				float r = (val * MAX_DOT_SIZE);
				gl2.glBegin(GL2.GL_TRIANGLE_FAN);
				for (float j = 0; j < Math.PI * 2; j += (Math.PI / 4)) {
					gl2.glVertex2d(x + Math.cos(j) * r,
								   y + Math.sin(j) * r);
				}
				gl2.glEnd();
			}
		}

		lock.unlock();
	}

	// set some starting points in a grid
	protected void initializeCells(double minDistanceBetweenSites) {
		Log.write("green","Initializing cells");

		cells = new VoronoiCell[MAX_CELLS];

		// from top to bottom of the margin area...
		float yBottom = (float)machine.getPaperBottom() * (float)machine.getPaperMargin() * 10;
		float yTop    = (float)machine.getPaperTop()    * (float)machine.getPaperMargin() * 10;
		float xLeft   = (float)machine.getPaperLeft()   * (float)machine.getPaperMargin() * 10;
		float xRight  = (float)machine.getPaperRight()  * (float)machine.getPaperMargin() * 10;
		
		int used;
		for (used=0;used<MAX_CELLS;++used) {
			cells[used] = new VoronoiCell();
			cells[used].centroid.setLocation(xLeft   + ((float)Math.random()*(xRight-xLeft)),
											 yBottom + ((float)Math.random()*(yTop-yBottom))
											 );
		}

		// convert the cells to sites used in the Voronoi class.
		xValuesIn = new double[MAX_CELLS];
		yValuesIn = new double[MAX_CELLS];

		voronoiTesselator.Init(minDistanceBetweenSites);
	}


	/**
	 * Jiggle the dots until they make a nice picture
	 */
	protected void evolveCells() {
		try {
			Log.write("green","Mutating");

			int generation = 0;
			do {
				++generation;
				Log.write("green","Generation " + generation);

				assert !lock.isHeldByCurrentThread();
				lock.lock();
				tessellateVoronoiDiagram();
				lock.unlock();
				adjustCentroids();

				// Do again if things are still moving a lot.  Cap the # of times so we don't have an infinite loop.
			} while (generation < MAX_GENERATIONS);
		} catch (Exception e) {
			e.printStackTrace();
			if(lock.isHeldByCurrentThread() && lock.isLocked()) {
				lock.unlock();
			}
		}
	}


	// write cell centroids to gcode.
	protected void writeOutCells(Writer out) throws IOException {
		if (graphEdges == null) return;

		Log.message("Writing gcode.");

		imageStart(out);
		tool = machine.getCurrentTool();
		liftPen(out);
		tool.writeChangeTo(out);

		float toolDiameter = tool.getDiameter();

		Arrays.sort(cells);
		
		int i;
		for (i = 0; i < cells.length; ++i) {
			float x = cells[i].centroid.x;
			float y = cells[i].centroid.y;
			float val = 1.0f - (sourceImage.sample1x1(x,y) / 255.0f);
			float r = val * MAX_DOT_SIZE;
			if (r < MIN_DOT_SIZE) continue;

			float newX=0,newY=0;
			boolean first=true;
			// filled circles
			while (r > 0) {
				float detail = (float)Math.ceil(Math.PI * r*2 / (toolDiameter*4));
				if (detail < 4) detail = 4;
				if (detail > 20) detail = 20;
				for (float j = 0; j <= detail; ++j) {
					double v = Math.PI * 2.0f * j / detail;
					newX = x + r * (float) Math.cos(v);
					newY = y + r * (float) Math.sin(v);
					if(first) {
						moveTo(out, newX, newY, true);
						lowerPen(out);
						first=false;
					} else {
						moveTo(out, newX, newY, false);
					}
				}
				r -= toolDiameter;
			}
			if(first==false) {
				liftPen(out);
			}
		}
		
		liftPen(out);
	    moveTo(out, (float)machine.getHomeX(), (float)machine.getHomeY(),true);
	}



	// I have a set of points.  I want a list of cell borders.
	// cell borders are halfway between any point and it's nearest neighbors.
	protected void tessellateVoronoiDiagram() {
		// convert the cells to sites used in the Voronoi class.
		int i;
		for (i = 0; i < cells.length; ++i) {
			xValuesIn[i] = cells[i].centroid.x;
			yValuesIn[i] = cells[i].centroid.y;
		}

		// scan left to right across the image, building the list of borders as we go.
		graphEdges = voronoiTesselator.generateVoronoi(xValuesIn, yValuesIn, xLeft, xRight, yBottom, yTop);
	}


	protected void generateBounds(int cellIndex) {
		numEdgesInCell = 0;

		float cx = cells[cellIndex].centroid.x;
		float cy = cells[cellIndex].centroid.y;

		double dx, dy, nx, ny, dot1;

		//long ta = System.nanoTime();

		for (VoronoiGraphEdge e : graphEdges) {
			if (e.site1 != cellIndex && e.site2 != cellIndex) continue;
			if (numEdgesInCell == 0) {
				if (e.x1 < e.x2) {
					bound_min.setLocation( e.x1, bound_min.getY() );
					bound_max.setLocation( e.x2, bound_max.getY() );
				} else {
					bound_min.setLocation( e.x2, bound_min.getY() );
					bound_max.setLocation( e.x1, bound_max.getY() );
				}
				if (e.y1 < e.y2) {
					bound_min.setLocation( bound_min.getX(), (float) e.y1 );
					bound_max.setLocation( bound_max.getX(), (float) e.y2 );
				} else {
					bound_min.setLocation( bound_min.getX(), (float) e.y2 );
					bound_max.setLocation( bound_max.getX(), (float) e.y1 );
				}
			} else {
				if (bound_min.x > e.x1) bound_min.setLocation( e.x1, bound_min.getY() );
				if (bound_min.x > e.x2) bound_min.setLocation( e.x2, bound_min.getY() );
				if (bound_max.x < e.x1) bound_max.setLocation( e.x1, bound_max.getY() );
				if (bound_max.x < e.x2) bound_max.setLocation( e.x2, bound_max.getY() );

				if (bound_min.y > e.y1) bound_min.setLocation( bound_min.getX(), e.y1 );
				if (bound_min.y > e.y2) bound_min.setLocation( bound_min.getX(), e.y2 );
				if (bound_max.y < e.y1) bound_max.setLocation( bound_max.getX(), e.y1 );
				if (bound_max.y < e.y2) bound_max.setLocation( bound_max.getX(), e.y2 );
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

		//long tc = System.nanoTime();

		//System.out.println("\t"+((tb-ta)/1e6)+"\t"+((tc-tb)/1e6));
	}


	protected boolean insideBorder(double x, double y) {
		double dx, dy;
		int i;
		Iterator<VoronoiCellEdge> ice = cellBorder.iterator();
		for (i = 0; i < numEdgesInCell; ++i) {
			VoronoiCellEdge ce = ice.next();

			// dot product the test point.
			dx = x - ce.px;
			dy = y - ce.py;
			// If they are opposite signs then the test point is outside the cell
			if (dx * ce.nx + dy * ce.ny < 0) return false;
		}
		// passed all tests, must be in cell.
		return true;
	}


	// find the weighted center of each cell.
	// weight is based on the intensity of the color of each pixel inside the cell
	// the center of the pixel must be inside the cell to be counted.
	protected void adjustCentroids() {
		int i;
		double weight, wx, wy, x, y;
		//int step = (int) Math.ceil(tool.getDiameter() / (1.0 * scale));
		double stepSize = 2.0;

		for (i = 0; i < cells.length; ++i) {
			generateBounds(i);
			double sx = Math.floor(bound_min.x);
			double sy = Math.floor(bound_min.y);
			double ex = Math.floor(bound_max.x);
			double ey = Math.floor(bound_max.y);


			//System.out.println("bounding "+i+" from "+sx+", "+sy+" to "+ex+", "+ey);
			//System.out.println("centroid "+cells[i].centroid.x+", "+cells[i].centroid.y);

			weight = 0;
			wx = 0;
			wy = 0;

			for (y = sy; y <= ey; y += stepSize) {
				for (x = sx; x <= ex; x += stepSize) {
					if (insideBorder(x, y)) {
						float val = (float) sourceImage.sample1x1( (float)x, (float)y ) / 255.0f;
						val = 1.0f - val;
						weight += val;
						wx += x * val;
						wy += y * val;
					}
				}
			}
			if (weight > 0) {
				wx /= weight;
				wy /= weight;
			}

			// make sure centroid can't leave image bounds
			if (wx < xLeft) wx = xLeft;
			if (wy < yBottom) wy = yBottom;
			if (wx >= xRight) wx = xRight;
			if (wy >= yTop) wy = yTop;

			// use the new center
			cells[i].centroid.setLocation(wx, wy);
		}
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
