package com.marginallyclever.converters;

import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jogamp.opengl.GL2;
import com.marginallyclever.basictypes.Point2D;
import com.marginallyclever.makelangelo.DrawPanelDecorator;
import com.marginallyclever.makelangelo.MakelangeloRobotSettings;
import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.Translator;
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
	private int w, h;
	private BufferedImage src_img;
	private List<VoronoiGraphEdge> graphEdges = null;
	private int MAX_GENERATIONS = 40;
	private int MAX_CELLS = 5000;
	private float MAX_DOT_SIZE = 5.0f;
	private float MIN_DOT_SIZE = 1.0f;
	private Point2D bound_min = new Point2D();
	private Point2D bound_max = new Point2D();
	private int numEdgesInCell;
	private List<VoronoiCellEdge> cellBorder = null;
	private double[] xValuesIn = null;
	private double[] yValuesIn = null;


	public Converter_VoronoiStippling(Makelangelo gui, MakelangeloRobotSettings mc, Translator ms) {
		super(gui, mc, ms);
	}

	@Override
	public String getName() {
		return translator.get("voronoiStipplingName");
	}

	@Override
	public boolean convert(BufferedImage img,Writer out) throws IOException {
		JTextField text_gens = new JTextField(Integer.toString(MAX_GENERATIONS), 8);
		JTextField text_cells = new JTextField(Integer.toString(MAX_CELLS), 8);
		JTextField text_dot_max = new JTextField(Float.toString(MAX_DOT_SIZE), 8);
		JTextField text_dot_min = new JTextField(Float.toString(MIN_DOT_SIZE), 8);

		JPanel panel = new JPanel(new GridLayout(0, 1));
		panel.add(new JLabel(translator.get("voronoiStipplingCellCount")));
		panel.add(text_cells);
		panel.add(new JLabel(translator.get("voronoiStipplingGenCount")));
		panel.add(text_gens);
		panel.add(new JLabel(translator.get("voronoiStipplingDotMax")));
		panel.add(text_dot_max);
		panel.add(new JLabel(translator.get("voronoiStipplingDotMin")));
		panel.add(text_dot_min);


		int result = JOptionPane.showConfirmDialog(null, panel, getName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			MAX_GENERATIONS = Integer.parseInt(text_gens.getText());
			MAX_CELLS = Integer.parseInt(text_cells.getText());
			MAX_DOT_SIZE = Float.parseFloat(text_dot_max.getText());
			MIN_DOT_SIZE = Float.parseFloat(text_dot_min.getText());

			src_img = img;
			h = img.getHeight();
			w = img.getWidth();

			tool = machine.getCurrentTool();
			imageSetupTransform(img);

			cellBorder = new ArrayList<>();

			initializeCells(MIN_DOT_SIZE);
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
			gl2.glVertex2d(TX((float) e.x1), TY((float) e.y1));
			gl2.glVertex2d(TX((float) e.x2), TY((float) e.y2));
		}
		gl2.glEnd();

		// draw cell centers
		gl2.glPointSize(3);
		gl2.glColor3f(0, 0, 0);
		for (VoronoiCell c : cells) {
			float x = c.centroid.x;
			float y = c.centroid.y;
			float val = 1.0f - (sample1x1(src_img, (int) x, (int) y) / 255.0f);
			float r = (val * MAX_DOT_SIZE) / scale;
			gl2.glBegin(GL2.GL_TRIANGLE_FAN);
			for (float j = 0; j < Math.PI * 2; j += (Math.PI / 4)) {
				gl2.glVertex2d(TX((float) (x + Math.cos(j) * r)),
						TY((float) (y + Math.sin(j) * r)));
			}
			gl2.glEnd();
		}

		lock.unlock();
	}

	// set some starting points in a grid
	protected void initializeCells(double minDistanceBetweenSites) {
		mainGUI.log("<font color='green'>Initializing cells</font>\n");

		double totalArea = w * h;
		double pointArea = totalArea / (double) MAX_CELLS;
		float length = (float) Math.sqrt(pointArea);
		float x, y;

		cells = new VoronoiCell[MAX_CELLS];
		int used = 0;
		int dir = 1;

		try {


			for (y = 0; y < h; y += length) {
				if (dir == 1) {
					for (x = 0; x < w; x += length) {
						cells[used] = new VoronoiCell();
						//cells[used].centroid.set(x+((float)Math.random()*length/2),y+((float)Math.random()*length/2));
						cells[used].centroid.set(x, y);
						++used;
						if (used == MAX_CELLS) break;
					}
					dir = -1;
				} else {
					for (x = w - 1; x >= 0; x -= length) {
						cells[used] = new VoronoiCell();
						//cells[used].centroid.set((float)Math.random()*(float)w,(float)Math.random()*(float)h);
						//cells[used].centroid.set(x-((float)Math.random()*length/2),y-((float)Math.random()*length/2));
						cells[used].centroid.set(x, y);
						++used;
						if (used == MAX_CELLS) break;
					}
					dir = 1;
				}
				if (used == MAX_CELLS) break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// convert the cells to sites used in the Voronoi class.
		xValuesIn = new double[cells.length];
		yValuesIn = new double[cells.length];

		voronoiTesselator.Init(minDistanceBetweenSites);
	}


	/**
	 * Jiggle the dots until they make a nice picture
	 */
	protected void evolveCells() {
		try {
			mainGUI.log("<font color='green'>Mutating</font>\n");

			int generation = 0;
			do {
				generation++;
				mainGUI.log("<font color='green'>Generation " + generation + "</font>\n");

				assert !lock.isHeldByCurrentThread();
				lock.lock();
				//try {
					tessellateVoronoiDiagram();
				//} finally {
					lock.unlock();
				//}
				adjustCentroids();

				mainGUI.getDrawPanel().repaintNow();

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
		if (graphEdges != null) {
			mainGUI.log("<font color='green'>Writing gcode to " + dest + "</font>\n");

			imageStart(src_img, out);

			// set absolute coordinates
			out.write("G00 G90;\n");
			tool.writeChangeTo(out);
			liftPen(out);

			float d = tool.getDiameter();

			int i;

			for (i = 0; i < cells.length; ++i) {
				float x = cells[i].centroid.x;
				float y = cells[i].centroid.y;
				float val = 1.0f - (sample1x1(src_img,(int)x,(int)y) / 255.0f);
				float r = val * MAX_DOT_SIZE;
				if (r < MIN_DOT_SIZE) continue;
				r /= scale;

				// filled circles
				this.moveTo(out, x + (float) Math.cos(0) * r, y + (float) Math.cos(0) * r, true);
				while (r > d) {
					float detail = (float) (1.0 * Math.PI * r / d);
					if (detail < 4) detail = 4;
					if (detail > 20) detail = 20;
					for (float j = 1; j <= detail; ++j) {
						this.moveTo(out,
								x + r * (float) Math.cos((float) Math.PI * 2.0f * j / detail),
								y + r * (float) Math.sin((float) Math.PI * 2.0f * j / detail), false);
					}
					//r-=(d/(scale*1.5f));
					r -= d;
				}
				this.moveTo(out, x, y, false);
				this.moveTo(out, x, y, true);
			}

			liftPen(out);
		}
	}


	/**
	 * Overrides MoveTo() because optimizing for zigzag is different logic than straight lines.
	 */
	@Override
	protected void moveTo(Writer out, float x, float y, boolean up) throws IOException {
		if (lastUp != up) {
			if (up) liftPen(out);
			else lowerPen(out);
			lastUp = up;
		}
		tool.writeMoveTo(out, TX(x), TY(y));
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
		graphEdges = voronoiTesselator.generateVoronoi(xValuesIn, yValuesIn, 0, w - 1, 0, h - 1);
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
					bound_min.x = (float) e.x1;
					bound_max.x = (float) e.x2;
				} else {
					bound_min.x = (float) e.x2;
					bound_max.x = (float) e.x1;
				}
				if (e.y1 < e.y2) {
					bound_min.y = (float) e.y1;
					bound_max.y = (float) e.y2;
				} else {
					bound_min.y = (float) e.y2;
					bound_max.y = (float) e.y1;
				}
			} else {
				if (bound_min.x > e.x1) bound_min.x = (float) e.x1;
				if (bound_min.x > e.x2) bound_min.x = (float) e.x2;
				if (bound_max.x < e.x1) bound_max.x = (float) e.x1;
				if (bound_max.y < e.y2) bound_max.y = (float) e.y2;

				if (bound_min.y > e.y1) bound_min.y = (float) e.y1;
				if (bound_min.y > e.y2) bound_min.y = (float) e.y2;
				if (bound_max.y < e.y1) bound_max.y = (float) e.y1;
				if (bound_max.y < e.y2) bound_max.y = (float) e.y2;
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
						float val = (float) sample1x1(src_img, (int)x, (int)y) / 255.0f;
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
			if (wx < 0) wx = 0;
			if (wy < 0) wy = 0;
			if (wx >= w) wx = w - 1;
			if (wy >= h) wy = h - 1;

			// use the new center
			cells[i].centroid.set((float)wx, (float)wy);
		}
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
