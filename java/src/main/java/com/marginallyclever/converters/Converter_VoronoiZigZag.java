package com.marginallyclever.converters;

import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
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
import com.marginallyclever.filters.Filter_BlackAndWhite;
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
 * @author Dan
 * http://en.wikipedia.org/wiki/Fortune%27s_algorithm
 * http://skynet.ie/~sos/mapviewer/voronoi.php
 * @since 7.0.0?
 */
public class Converter_VoronoiZigZag extends ImageConverter implements DrawPanelDecorator {
	private ReentrantLock lock = new ReentrantLock();

	private VoronoiTesselator voronoiTesselator = new VoronoiTesselator();
	private VoronoiCell [] cells = new VoronoiCell[1];
	private int w, h;
	private BufferedImage src_img;
	private List<VoronoiGraphEdge> graphEdges = null;
	private int MAX_GENERATIONS=200;
	private int MAX_CELLS=3000;
	private float CUTOFF=1.0f;
	private Point2D bound_min = new Point2D();
	private Point2D bound_max = new Point2D();
	private int numEdgesInCell;
	private List<VoronoiCellEdge> cellBorder = null;
	private double[] xValuesIn=null;
	private double[] yValuesIn=null;
	private int[] solution = null;
	private int solutionContains;
	private int renderMode;

	// processing tools
	private long t_elapsed,t_start;
	private double progress;
	private double old_len,len;
	private long time_limit=10*60*1000;  // 10 minutes


	public Converter_VoronoiZigZag(Makelangelo gui,
			MakelangeloRobotSettings mc, Translator ms) {
		super(gui, mc, ms);
	}

	@Override
	public String getName() { return translator.get("ZigZagName")+" 2"; }

	@Override
	public boolean convert(BufferedImage img,Writer out) throws IOException {
		JTextField text_gens = new JTextField(Integer.toString(MAX_GENERATIONS), 8);
		JTextField text_cells = new JTextField(Integer.toString(MAX_CELLS), 8);

		JPanel panel = new JPanel(new GridLayout(0,1));
		panel.add(new JLabel(translator.get("voronoiStipplingCellCount")));
		panel.add(text_cells);
		panel.add(new JLabel(translator.get("voronoiStipplingGenCount")));
		panel.add(text_gens);


		int result = JOptionPane.showConfirmDialog(null, panel, getName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			MAX_GENERATIONS = Integer.parseInt(text_gens.getText());
			MAX_CELLS = Integer.parseInt(text_cells.getText());

			// make black & white
			Filter_BlackAndWhite bw = new Filter_BlackAndWhite(mainGUI,machine,translator,255);
			img = bw.filter(img);

			src_img = img;
			h = img.getHeight();
			w = img.getWidth();

			tool = machine.getCurrentTool();
			imageSetupTransform(img);

			cellBorder = new ArrayList<>();

			initializeCells(0.001);

			renderMode=0;
			evolveCells();
			greedyTour();
			renderMode=1;
			optimizeTour();
			writeOutCells(out);
			return true;
		}
		return false;
	}

	public void render(GL2 gl2,MakelangeloRobotSettings machine) {
		lock.lock();
		gl2.glScalef(0.1f, 0.1f, 1);
		int i;

		if( graphEdges!=null ) {
			// draw cell edges
			gl2.glColor3f(0.9f,0.9f,0.9f);
			gl2.glBegin(GL2.GL_LINES);
			for (VoronoiGraphEdge e : graphEdges) {
				gl2.glVertex2d(TX((float) e.x1), TY((float) e.y1));
				gl2.glVertex2d(TX((float) e.x2), TY((float) e.y2));
			}
			gl2.glEnd();
		}
		if( renderMode==0 ) {
			// draw cell centers
			gl2.glPointSize(3);   	  
			gl2.glBegin(GL2.GL_POINTS);

			for(i=0;i<cells.length;++i) {
				VoronoiCell c = cells[i];
				float v = 1.0f - (float)sample1x1(src_img,(int)c.centroid.x,(int)c.centroid.y) / 255.0f;
				if(v*5 <= CUTOFF) {
					gl2.glColor3f(0.8f,0.8f,0.8f);	 
				} else {
					gl2.glColor3f(0,0,0);	 
				}

				gl2.glVertex2d(TX((float)c.centroid.x),TY((float)c.centroid.y));
			}
			gl2.glEnd();
		}
		if( renderMode==1 && solution!=null ) {
			// draw tour
			gl2.glColor3f(0,0,0);
			gl2.glBegin(GL2.GL_LINE_LOOP);
			for(i=0;i<solutionContains;++i) {
				VoronoiCell c = cells[solution[i]];
				gl2.glVertex2d(TX((float)c.centroid.x),TY((float)c.centroid.y));
			}
			gl2.glEnd();
		}
		lock.unlock();
	}


	private void optimizeTour() {
		int gen=0;
		int once=1;

		mainGUI.log("<font color='green'>Running Lin/Kerighan optimization...</font>\n");

		old_len = getTourLength(solution);
		updateProgress(old_len,2);

		progress=0;

		t_elapsed=0;
		t_start = System.currentTimeMillis();
		while(once==1 && t_elapsed<time_limit && !parent.isCancelled()) {
			once=0;
			//@TODO: make these optional for the very thorough people
			//once|=transposeForwardTest();
			//once|=transposeBackwardTest();

			once|=flipTests();
			mainGUI.getDrawPanel().repaintNow();
			gen++;
			mainGUI.log("<font color='green'>Generation "+gen+"</font>\n");
		}

		mainGUI.log("<font color='green'>Finished @ "+gen+"</font>\n");
	}


	public String formatTime(long millis) {
		String elapsed="";
		long s=millis/1000;
		long m=s/60;
		long h=m/60;
		m%=60;
		s%=60;
		if(h>0) elapsed+=h+"h";
		if(h>0||m>0) elapsed+=m+"m";
		elapsed+=s+"s ";
		return elapsed;
	}


	public void updateProgress(double len,int color) {
		t_elapsed=System.currentTimeMillis()-t_start;
		double new_progress = 100.0 * (double)t_elapsed / (double)time_limit;
		if( new_progress > progress + 0.1 ) {
			// find the new tour length
			len=getTourLength(solution);
			if( old_len > len ) {
				old_len=len;
				DecimalFormat flen=new DecimalFormat("#.##");
				String c;
				switch(color) {
				case  0: c="yellow";  break;
				case  1: c="blue";    break;
				case  2: c="red";   break;
				default: c="white";   break;
				}
				mainGUI.log("<font color='" + c + "'>" + formatTime(t_elapsed) + ": " + flen.format(len) + "mm</font>\n");
			}
			progress = new_progress;
			pm.setProgress((int)progress);
		}
	}


	private int ti(int x) {
		return (x+solutionContains)%solutionContains;
	}

	// we have s1,s2...e-1,e
	// check if s1,e-1,...s2,e is shorter
	public int flipTests() {
		int start, end, j, once=0, best_end;
		float a,b,c,d,temp_diff,best_diff;

		for(start=0;start<solutionContains*2-2 && !parent.isCancelled() && !pm.isCanceled();++start) {
			a=calculateWeight(solution[ti(start)],solution[ti(start+1)]);
			best_end=-1;
			best_diff=0;

			for(end=start+2;end<start+solutionContains && !parent.isCancelled() && !pm.isCanceled();++end) {
				// before
				b=calculateWeight(solution[ti(end  )],solution[ti(end  -1)]);
				// after
				c=calculateWeight(solution[ti(start)],solution[ti(end  -1)]);
				d=calculateWeight(solution[ti(end  )],solution[ti(start+1)]);

				temp_diff=(a+b)-(c+d);
				if(best_diff < temp_diff) {
					best_diff = temp_diff;
					best_end=end;
				}
			}

			if(best_end != -1 && !parent.isCancelled() && !pm.isCanceled()) {
				once = 1;
				// do the flip
				int begin=start+1;
				int finish=best_end;
				if(best_end<begin) finish+=solutionContains;
				int half=(finish-begin)/2;
				int temp;
				while(lock.isLocked());

				lock.lock();  
				//DrawbotGUI.getSingleton().Log("<font color='red'>flipping "+(finish-begin)+"</font>\n");
				for(j=0;j<half;++j) {
					temp = solution[ti(begin+j)];
					solution[ti(begin+j)]=solution[ti(finish-1-j)];
					solution[ti(finish-1-j)]=temp;
				}
				lock.unlock();
				updateProgress(len,1);
			}
		}
		return once;
	}


	private double calculateLength(int a,int b) {
		return Math.sqrt(calculateWeight(a,b));
	}

	/**
	 * Get the length of a tour segment
	 * @param list an array of indexes into the point list.  the order forms the tour sequence.
	 * @return the length of the tour
	 */
	private double getTourLength(int[] list) {
		double w=0;
		for(int i=0;i<solutionContains-1;++i) {
			w+=calculateLength(list[i],list[i+1]);
		}
		return w;
	}


	/**
	 * Starting with point 0, find the next nearest point and repeat until all points have been "found".
	 */
	private void greedyTour() {
		mainGUI.log("<font color='green'>Finding greedy tour solution...</font>\n");

		int i,j;
		float w, bestw;
		int besti;


		solutionContains=0;
		for(i=0;i<cells.length;++i) {
			VoronoiCell c = cells[i];
			float v = 1.0f - (float)sample1x1(src_img,(int)c.centroid.x,(int)c.centroid.y) / 255.0f;
			if( v*5 > CUTOFF ) solutionContains++;
		}


		try {
			solution = new int[solutionContains];

			// put all the points in the solution in no particular order.
			j=0;
			for(i=0;i<cells.length;++i) {
				VoronoiCell c = cells[i];
				float v = 1.0f - (float)sample1x1(src_img,(int)c.centroid.x,(int)c.centroid.y) / 255.0f;
				if( v*5 > CUTOFF ) {
					solution[j++]=i;
				}
			}

			int scount=0;

			do {
				// Find the nearest point not already in the line.
				// Any solution[n] where n>scount is not in the line.
				bestw=calculateWeight(solution[scount],solution[scount+1]);
				besti=scount+1;
				for( i=scount+2; i<solutionContains; ++i ) {
					w=calculateWeight(solution[scount],solution[i]);
					if( w < bestw ) {
						bestw=w;
						besti=i;
					}
				}
				i=solution[scount+1];
				solution[scount+1]=solution[besti];
				solution[besti]=i;
				scount++;
			} while(scount<solutionContains-2);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}


	protected float calculateWeight(int a,int b) {
		assert(a>=0 && a < cells.length);
		assert(b>=0 && b < cells.length);
		float x = cells[a].centroid.x - cells[b].centroid.x;
		float y = cells[a].centroid.y - cells[b].centroid.y;
		return x*x+y*y;
	}


	// set some starting points in a grid
	protected void initializeCells(double minDistanceBetweenSites) {
		mainGUI.log("<font color='green'>Initializing cells</font>\n");

		double totalArea = w*h;
		double pointArea = totalArea/(double)MAX_CELLS;
		float length = (float)Math.sqrt(pointArea);
		float x,y;

		cells = new VoronoiCell[MAX_CELLS];
		int used=0;
		int dir=1;

		try {

			boolean place_at_random=false;//true;
			if(place_at_random) {
				for(used = 0; used < MAX_CELLS; used++ ) {
					cells[used]=new VoronoiCell();
					cells[used].centroid.set(((float)Math.random()*(float)w),((float)Math.random()*(float)h));
				}
			} else {
				for(y = 0; y < h; y += length ) {
					if(dir==1) {
						for(x = 0; x < w; x += length ) {
							cells[used]=new VoronoiCell();
							cells[used].centroid.set(x,y);
							++used;
							if(used==MAX_CELLS) break;
						}
						dir=-1;
					} else {
						for(x = w-1; x >= 0; x -= length ) {
							cells[used]=new VoronoiCell();
							cells[used].centroid.set(x,y);
							++used;
							if(used==MAX_CELLS) break;
						}
						dir=1;
					}
					if(used==MAX_CELLS) break;
				}
			}
		}
		catch(Exception e) {
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

			int generation=0;
			do {
				generation++;
				mainGUI.log("<font color='green'>Generation "+generation+"</font>\n");

				lock.lock();
				tessellateVoronoiDiagram();
				lock.unlock();
				adjustCentroids();

				mainGUI.getDrawPanel().repaintNow();

				// Do again if things are still moving a lot.  Cap the # of times so we don't have an infinite loop.
			} while(generation<MAX_GENERATIONS);

			mainGUI.log("<font color='green'>Last "+generation+"</font>\n");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}


	// write cell centroids to gcode.
	protected void writeOutCells(Writer out) throws IOException {
		if(graphEdges != null ) {
			mainGUI.log("<font color='green'>Writing gcode to "+dest+"</font>\n");
			imageStart(src_img, out);

			// set absolute coordinates
			out.write("G00 G90;\n");
			tool.writeChangeTo(out);
			liftPen(out);

			// find the tsp point closest to the calibration point
			int i;
			int besti=-1;
			float bestw=1000000;
			float x,y,w;
			for(i=0;i<solutionContains;++i) {
				x=w2-cells[solution[i]].centroid.x;
				y=h2-cells[solution[i]].centroid.y;
				w=x*x+y*y;
				if(w<bestw) {
					bestw=w;
					besti=i;
				}
			}

			// write the entire sequence
			for(i=0;i<=solutionContains;++i) {
				int v= (besti+i)%solutionContains;
				x=cells[solution[v]].centroid.x;
				y=cells[solution[v]].centroid.y;

				this.moveTo(out,x,y, false);
			}

			liftPen(out);
		}
	}


	/**
	 * Overrides MoveTo() because optimizing for zigzag is different logic than straight lines.
	 */
	@Override
	protected void moveTo(Writer out,float x,float y,boolean up) throws IOException {
		if(lastUp!=up) {
			if(up) liftPen(out);
			else   lowerPen(out);
			lastUp=up;
		}
		tool.writeMoveTo(out, TX(x), TY(y));
	}


	// I have a set of points.  I want a list of cell borders.
	// cell borders are halfway between any point and it's nearest neighbors.
	protected void tessellateVoronoiDiagram() {
		// convert the cells to sites used in the Voronoi class.
		int i;
		for(i=0;i<cells.length;++i) {
			xValuesIn[i]= cells[i].centroid.x;
			yValuesIn[i]= cells[i].centroid.y;
		}

		// scan left to right across the image, building the list of borders as we go.
		graphEdges = voronoiTesselator.generateVoronoi(xValuesIn, yValuesIn, 0, w-1, 0, h-1);
	}


	protected void generateBounds(int cellIndex) {
		numEdgesInCell=0;

		float cx = cells[cellIndex].centroid.x;
		float cy = cells[cellIndex].centroid.y;

		double dx,dy,nx,ny,dot1;

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


	protected boolean insideBorder(float x,float y) {
		double dx,dy;
		int i;
		Iterator<VoronoiCellEdge> ice = cellBorder.iterator();
		for(i=0;i<numEdgesInCell;++i) {
			VoronoiCellEdge ce = ice.next();

			// dot product the test point.
			dx = x-ce.px;
			dy = y-ce.py;
			// If they are opposite signs then the test point is outside the cell
			if( dx*ce.nx+dy*ce.ny < 0 ) return false;
		}
		// passed all tests, must be in cell.
		return true;
	}


	// find the weighted center of each cell.
	// weight is based on the intensity of the color of each pixel inside the cell
	// the center of the pixel must be inside the cell to be counted.
	protected void adjustCentroids() {
		int i;
		float weight,wx,wy,x,y;
		float stepSize=2;

		for(i=0;i<cells.length;++i) {
			generateBounds(i);
			int sx = (int)Math.floor(bound_min.x);
			int sy = (int)Math.floor(bound_min.y);
			int ex = (int)Math.floor(bound_max.x);
			int ey = (int)Math.floor(bound_max.y);
			//System.out.println("bounding "+i+" from "+sx+", "+sy+" to "+ex+", "+ey);
			//System.out.println("centroid "+cells[i].centroid.x+", "+cells[i].centroid.y);

			weight=0;
			wx=0;
			wy=0;

			for(y = sy; y <= ey; y+=stepSize) {
				for(x = sx; x <= ex; x+=stepSize) {
					if(insideBorder(x,y)) {
						float val = (float)sample1x1(src_img,(int)x, (int)y) / 255.0f;
						val = 1.0f - val;
						weight += val;
						wx += x * val;
						wy += y * val;
					}
				}
			}

			if( weight > 0.0f ) {
				wx /= weight;
				wy /= weight;
			}

			// make sure centroid can't leave image bounds
			if(wx<0) wx=0;
			if(wy<0) wy=0;
			if(wx>=w) wx = w-1;
			if(wy>=h) wy = h-1;

			// use the new center
			cells[i].centroid.set(wx, wy);
		}
	}
}


/**
 * This file is part of DrawbotGUI.
 *
 * DrawbotGUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DrawbotGUI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DrawbotGUI.  If not, see <http://www.gnu.org/licenses/>.
 */
