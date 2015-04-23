package com.marginallyclever.filters;

import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import com.marginallyclever.makelangelo.MachineConfiguration;
import com.marginallyclever.makelangelo.MainGUI;
import com.marginallyclever.makelangelo.MultilingualSupport;
import com.marginallyclever.makelangelo.Point2D;
import com.marginallyclever.voronoi.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;


/**
 * Dithering using a particle system
 * @author Dan
 * http://en.wikipedia.org/wiki/Fortune%27s_algorithm
 * http://skynet.ie/~sos/mapviewer/voronoi.php
 */
public class Filter_GenerateVoronoiStippling extends Filter {
	private VoronoiTesselator voronoiTesselator = new VoronoiTesselator();
	private int totalCells=1;
	private VoronoiCell [] cells = new VoronoiCell[1];
	private int w, h;
	private BufferedImage src_img;
	private List<VoronoiGraphEdge> graphEdges = null;
	private int MAX_GENERATIONS=40;
	private int MAX_CELLS=5000;
	private Point2D bound_min = new Point2D();
	private Point2D bound_max = new Point2D();
	private int numEdgesInCell;
	private List<VoronoiCellEdge> cellBorder = null;
	private double[] xValuesIn=null;
	private double[] yValuesIn=null;
	
	
	public Filter_GenerateVoronoiStippling(MainGUI gui,
			MachineConfiguration mc, MultilingualSupport ms) {
		super(gui, mc, ms);
		// TODO Auto-generated constructor stub
	}


	public String GetName() { return "main.java.com.marginallyclever.voronoi stipples"; }
	
	
	public void Convert(BufferedImage img) throws IOException {
		JTextField text_gens = new JTextField(Integer.toString(MAX_GENERATIONS), 8);
		JTextField text_cells = new JTextField(Integer.toString(MAX_CELLS), 8);
	
		JPanel panel = new JPanel(new GridLayout(0,1));
		panel.add(new JLabel("Number of cells"));
		panel.add(text_cells);
		panel.add(new JLabel("Number of generations"));
		panel.add(text_gens);
		
	    int result = JOptionPane.showConfirmDialog(null, panel, GetName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
	    if (result == JOptionPane.OK_OPTION) {
	    	MAX_GENERATIONS = Integer.parseInt(text_gens.getText());
	    	MAX_CELLS = Integer.parseInt(text_cells.getText());
	    	
			src_img = img;
			h = img.getHeight();
			w = img.getWidth();
			
			tool = machine.GetCurrentTool();
			ImageSetupTransform(img);
	
			cellBorder = new ArrayList<VoronoiCellEdge>();
	
		    
			initializeCells(0.5);
			evolveCells();
			writeOutCells();
	    }
	}


	// set some starting points in a grid
	protected void initializeCells(double minDistanceBetweenSites) {
		mainGUI.Log("<font color='green'>Initializing cells</font>\n");

		totalCells=MAX_CELLS;

		double totalArea = w*h;
		double pointArea = totalArea/totalCells;
		float length = (float)Math.sqrt(pointArea);
		float x,y;
		totalCells=0;
		for(y = length/2; y < h; y += length ) {
			for(x = length/2; x < w; x += length ) {
				totalCells++;
			}
		}

		cells = new VoronoiCell[totalCells];
		int used=0;
		
		for(y = length/2; y < h; y += length ) {
			for(x = length/2; x < w; x += length ) {
				cells[used]=new VoronoiCell();
				//cells[used].centroid.set((float)Math.random()*(float)w,(float)Math.random()*(float)h);
				cells[used].centroid.set(x,y);
				++used;
			}
		}

		// convert the cells to sites used in the main.java.com.marginallyclever.voronoi class.
		xValuesIn = new double[cells.length];
		yValuesIn = new double[cells.length];
		
		voronoiTesselator.Init(minDistanceBetweenSites);
	}


	// jiggle the dots until they make a nice picture
	protected void evolveCells() {
		try {
			mainGUI.Log("<font color='green'>Mutating</font>\n");
	
			int generation=0;
			float change=0;
			do {
				generation++;
				mainGUI.Log("<font color='green'>Generation "+generation+"</font>\n");
	
				tessellateVoronoiDiagram();
				change = AdjustCentroids();
				
				// do again if things are still moving a lot.  Cap the # of times so we don't have an infinite loop.
			} while(change>=1 && generation<MAX_GENERATIONS);  // TODO these are a guess. tweak?  user set?
			
			mainGUI.Log("<font color='green'>Last "+generation+"</font>\n");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	// write cell centroids to gcode.
	protected void writeOutCells() throws IOException {
		if(graphEdges != null ) {
			mainGUI.Log("<font color='green'>Writing gcode to "+dest+"</font>\n");
			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(dest),"UTF-8");
			
			ImageStart(src_img,out);
	
			// set absolute coordinates
			out.write("G00 G90;\n");
			tool.WriteChangeTo(out);
			liftPen(out);

			int i;
/*
			for(i=0;i<graphEdges.size();++i) {
				GraphEdge e= graphEdges.get(i);
				
				this.MoveTo(out, (float)e.x1,(float)e.y1, true);
				this.MoveTo(out, (float)e.x1,(float)e.y1, false);
				this.MoveTo(out, (float)e.x2,(float)e.y2, false);
				this.MoveTo(out, (float)e.x2,(float)e.y2, true);
			}
//*/		
			//float step = (int)Math.ceil(tool.GetDiameter()/scale);
			float most=cells[0].weight;
			//float least=cells[0].weight;
			for(i=1;i<cells.length;++i) {
				if(most<cells[i].weight) most=cells[i].weight;
				//if(least>cells[i].weight) least=cells[i].weight;
			}
			
			for(i=0;i<cells.length;++i) {
				float r = 5f * cells[i].weight / most;
				r/=scale;
				if(r<1) continue;
				//System.out.println(i+"\t"+v);
				float x=cells[i].centroid.x;
				float y=cells[i].centroid.y;
				/*
				// boxes
				this.MoveTo(out, x-r, y-r, true);
				this.MoveTo(out, x+r, y-r, false);
				this.MoveTo(out, x+r, y+r, false);
				this.MoveTo(out, x-r, y+r, false);
				this.MoveTo(out, x-r, y-r, false);
				this.MoveTo(out, x-r, y-r, true);
				
				// filled boxes
				this.MoveTo(out, x-r, y-r, true);
				this.MoveTo(out, x+r, y-r, false);
				this.MoveTo(out, x+r, y+r, false);
				this.MoveTo(out, x-r, y+r, false);
				this.MoveTo(out, x-r, y-r, false);
				for(float j=y-r;j<y+r;j+=step) {
					this.MoveTo(out, x+r, j, false);
					this.MoveTo(out, x-r, j, false);					
				}
				this.MoveTo(out, x-r, y-r, false);
				this.MoveTo(out, x-r, y-r, true);

				// circles
				this.MoveTo(out, x-r*(float)Math.sin(0), y-r*(float)Math.cos(0), true);
				float detail=(float)(0.5*Math.PI*r);
				if(detail<4) detail=4;
				if(detail>20) detail=20;
				for(float j=1;j<=detail;++j) {
					this.MoveTo(out, 
							x-r*(float)Math.sin(j*(float)Math.PI*2.0f/detail),
							y-r*(float)Math.cos(j*(float)Math.PI*2.0f/detail), false);
				}
				this.MoveTo(out, x-r*(float)Math.sin(0), y-r*(float)Math.cos(0), false);
				this.MoveTo(out, x-r*(float)Math.sin(0), y-r*(float)Math.cos(0), true);
				*/
				// filled circles
				this.MoveTo(out, x-r*(float)Math.sin(0), y-r*(float)Math.cos(0), true);
				while(r>1) {
					float detail=(float)(0.5*Math.PI*r);
					if(detail<4) detail=4;
					if(detail>10) detail=10;
					for(float j=1;j<=detail;++j) {
						this.MoveTo(out, 
								x-r*(float)Math.sin(j*(float)Math.PI*2.0f/detail),
								y-r*(float)Math.cos(j*(float)Math.PI*2.0f/detail), false);
					}
					r-=(tool.GetDiameter()/(scale*1.5f));
				}
				this.MoveTo(out, x, y-r, false);
				this.MoveTo(out, x, y-r, true);
			}
			
			liftPen(out);
			SignName(out);
			tool.WriteMoveTo(out, 0, 0);
			out.close();
		}
	}
	

	/**
	 * Overrides MoveTo() because optimizing for zigzag is different logic than straight lines.
	 */
	protected void MoveTo(OutputStreamWriter out,float x,float y,boolean up) throws IOException {
		if(lastup!=up) {
			if(up) liftPen(out);
			else   lowerPen(out);
			lastup=up;
		}
		tool.WriteMoveTo(out, TX(x), TY(y));
	}
	
	
	// I have a set of points.  I want a list of cell borders.
	// cell borders are halfway between any point and it's nearest neighbors.
	protected void tessellateVoronoiDiagram() {
		// convert the cells to sites used in the main.java.com.marginallyclever.voronoi class.
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
		
		Iterator<VoronoiGraphEdge> ige = graphEdges.iterator();
		while(ige.hasNext()) {
			VoronoiGraphEdge e = ige.next();
			if(e.site1 != cellIndex && e.site2 != cellIndex ) continue;
			if(numEdgesInCell==0) {
				if(e.x1<e.x2) {
					bound_min.x=(float)e.x1;
					bound_max.x=(float)e.x2;
				} else {
					bound_min.x=(float)e.x2;
					bound_max.x=(float)e.x1;					
				}
				if(e.y1<e.y2) {
					bound_min.y=(float)e.y1;
					bound_max.y=(float)e.y2;
				} else {
					bound_min.y=(float)e.y2;
					bound_max.y=(float)e.y1;					
				}
			} else {
				if(bound_min.x>e.x1) bound_min.x=(float)e.x1;
				if(bound_min.x>e.x2) bound_min.x=(float)e.x2;
				if(bound_max.x<e.x1) bound_max.x=(float)e.x1;
				if(bound_max.y<e.y2) bound_max.y=(float)e.y2;
				
				if(bound_min.y>e.y1) bound_min.y=(float)e.y1;
				if(bound_min.y>e.y2) bound_min.y=(float)e.y2;
				if(bound_max.y<e.y1) bound_max.y=(float)e.y1;
				if(bound_max.y<e.y2) bound_max.y=(float)e.y2;
			}

			// make a unnormalized vector along the edge of e
			dx = e.x2 - e.x1;
			dy = e.y2 - e.y1;
			// find a line orthogonal to dx/dy
			nx=dy;
			ny=-dx;
			// dot product the centroid and the normal.
			dx = cx-e.x1;
			dy = cy-e.y1;
			dot1=(dx*nx+dy*ny);
			
			if(cellBorder.size()==numEdgesInCell) {
				cellBorder.add(new VoronoiCellEdge());
			}

			VoronoiCellEdge ce=cellBorder.get(numEdgesInCell++);
			ce.px = e.x1;
			ce.py = e.y1;
			if(dot1<0) {
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
	
	
	protected boolean insideBorder(int x,int y) {
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
	protected float AdjustCentroids() {
		int i,x,y;
		float change=0;
		float weight,wx,wy;
		int step = (int)Math.ceil(tool.GetDiameter()/(1.0*scale));
		
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

			for(y = sy; y <= ey; y+=step) {
				for(x = sx; x <= ex; x+=step) {
					if(insideBorder(x, y)) {
						float val = (float)sample1x1(src_img,x,y) / 255.0f;
						val = 1.0f - val;
						weight += val;
						wx += x * val;
						wy += y * val;
					}
				}
			}
			if( weight > 0 ) {
				wx /= weight;
				wy /= weight;

				cells[i].weight = weight;
				
				// make sure centroid can't leave image bounds
				if(wx<0) wx=0;
				if(wy<0) wy=0;
				if(wx>=w) wx = w-1;
				if(wy>=h) wy = h-1;

				float dx = wx - cells[i].centroid.x;
				float dy = wy - cells[i].centroid.y;
				
				change += dx*dx+dy*dy;
				//change = (float)Math.sqrt(change);
				
				// use the new center
				cells[i].centroid.set(wx, wy);
			}
		}
		
		return change;
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
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */