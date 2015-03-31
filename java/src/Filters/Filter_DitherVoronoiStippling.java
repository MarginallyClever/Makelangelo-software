package Filters;



import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;

import Makelangelo.MainGUI;
import Makelangelo.Point2D;
import Makelangelo.Polygon2D;



/**
 * Dithering using a particle system
 * @author Dan
 * http://en.wikipedia.org/wiki/Fortune%27s_algorithm
 * http://skynet.ie/~sos/mapviewer/voronoi.php
 */
public class Filter_DitherVoronoiStippling extends Filter {
	public String GetName() { return "Voronoi stipples"; }
	
	class VoronoiCell implements Comparable<VoronoiCell> {
		public Polygon2D border = new Polygon2D();
		public Point2D centroid = new Point2D();
		public float weight;
		
		// http://www.mkyong.com/java/java-object-sorting-example-comparable-and-comparator/
		public int compareTo(VoronoiCell b) {
			if( this.centroid.y < b.centroid.y ) return -1;
			if( this.centroid.y > b.centroid.y ) return  1;
			if( this.centroid.x < b.centroid.x ) return -1;
			if( this.centroid.x > b.centroid.x ) return  1;

			return 0;
		}
	}
	
	private final int totalCells=500;
	VoronoiCell [] cells = new VoronoiCell[totalCells];
	int w, h, used;
	BufferedImage src_img;
	
	
	public void Convert(BufferedImage img) throws IOException {
		src_img = img;
		h = img.getHeight();
		w = img.getWidth();

		initializeCells();
		evolveCells();
		writeOutCells();
	}


	// jiggle the dots until they make a nice picture
	protected void evolveCells() {
		MainGUI.getSingleton().Log("<font color='green'>Mutating</font>\n");

		int generation=0;
		float change;
		do {
			generation++;
			MainGUI.getSingleton().Log("<font color='green'>Generation "+generation+"</font>\n");
			
			// 
			tessellateVoronoiDiagram();
			
			// adjust centroids
			change = AdjustCentroids();
		} while(change>=1 && generation<100);  // TODO these are a guess. tweak?  user set?
	}
	
	
	// write cell centroids to gcode.
	protected void writeOutCells() throws IOException {
		MainGUI.getSingleton().Log("<font color='green'>Writing gcode to "+dest+"</font>\n");
		OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(dest),"UTF-8");
		
		ImageStart(src_img,out);

		// set absolute coordinates
		out.write("G00 G90;\n");
		tool.WriteChangeTo(out);
		liftPen(out);
		
		int i;
		for(i=0;i<used;++i) {
			this.MoveTo(out, cells[i].centroid.x, cells[i].centroid.y, true);
			this.MoveTo(out, cells[i].centroid.x, cells[i].centroid.y, false);
			this.MoveTo(out, cells[i].centroid.x, cells[i].centroid.y, true);
		}
		
		liftPen(out);
		SignName(out);
		tool.WriteMoveTo(out, 0, 0);
		out.close();
	}


	// set some starting points in a grid
	protected void initializeCells() {
		MainGUI.getSingleton().Log("<font color='green'>Initializing cells</font>\n");

		used=0;
		
		double totalArea = w*h;
		double pointArea = totalArea/totalCells;
		float length = (float)Math.sqrt(pointArea);
		float x,y;
		
		for(y = length/2; y < h; y += length ) {
			for(x = length/2; x < w; x += length ) {
				if(used < totalCells) {
					cells[used]=new VoronoiCell();
					cells[used].centroid.set(x,y);
					++used;
				}
			}
		}
	}
	
	
	// I have a set of points.  I want a list of cell borders.
	// cell borders are halfway between any point and it's nearest neighbors.
	protected void tessellateVoronoiDiagram() {
		// throw out any junk borders we might have from the last generation.
		int i;
		for(i=0;i<used;++i) {
			cells[i].border.removeAll();
		}

		// sort cells from left to right, top to bottom.
		Arrays.sort(cells);
		
		// scan left to right across the image, building the list of borders as we go.
		
		// sort the borders so the points are in clockwise order for easier in/out testing.
		for(i=0;i<used;++i) {
			cells[i].border.makeClockwise();
		}
	}

	// find the weighted center of each cell.
	// weight is based on the intensity of the color of each pixel inside the cell
	// the center of the pixel must be inside the cell to be counted.
	protected float AdjustCentroids() {
		int i;
		Point2D start = new Point2D();
		Point2D end = new Point2D();
		float change=0;
		
		for(i=0;i<cells.length;++i) {
			cells[i].border.getClip(start,end);
			
			int sx = (int)Math.floor(start.x);
			int sy = (int)Math.floor(start.y);
			int ex = (int)Math.floor(end.x);
			int ey = (int)Math.floor(end.y);
			
			float weight=0;
			float wx=0;
			float wy=0;
			
			for(int y = sy; y <= ey; y++) {
				for(int x = sx; x <= ex; x++) {
					if(cells[i].border.insideBorder(x, y)) {
						float f = sample1x1(src_img,x,y) / 255.0f;
						
						weight += f;
						wx += x * f;
						wy += y * f;
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
				// change = Math.sqrt(change);
				
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