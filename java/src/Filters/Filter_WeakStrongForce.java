package Filters;



import java.awt.image.BufferedImage;

import Makelangelo.Point2D;



/**
 * Dithering using a particle system
 * @author Dan
 */
public class Filter_WeakStrongForce extends Filter {
	private final int totalPoints=500;
	Point2D[] points = null;
	Point2D[] forces = null;
	
	public BufferedImage Process(BufferedImage img) {
		points = new Point2D[totalPoints];
		forces = new Point2D[totalPoints];
		
		int h = img.getHeight();
		int w = img.getWidth();
		int i=0;

		double totalArea = w*h;
		double pointArea = totalArea/totalPoints;
		float length = (float)Math.sqrt(pointArea);
		float x,y;
		for(y = length/2; y < h; y += length ) {
			for(x = length/2; x < w; x += length ) {
				points[i++].set(x,y);
			}
		}
		
		
		return img;
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