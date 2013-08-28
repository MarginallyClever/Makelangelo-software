import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;


/**
 * Reduces any picture to a more manageable size
 * @author Dan
 */
public class Filter_Resize {
	private final double cm_to_mm=10.0f;
	private	double dots_per_cm=1;
	private double margin=0.9f;
	private double paper_top, paper_bottom, paper_left, paper_right;
	
	
	public Filter_Resize(double t,double b,double l,double r,double _dots_per_cm,double _margin) {
		paper_top=t;
		paper_bottom=b;
		paper_left=l;
		paper_right=r;
		dots_per_cm=_dots_per_cm;
		margin=_margin;
	}
	
	
	protected BufferedImage scaleImage(BufferedImage img, int width, int height, Color background) {
	    BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	    Graphics2D g = newImage.createGraphics();
	    try {
	        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
	                           RenderingHints.VALUE_INTERPOLATION_BICUBIC);
	        g.setBackground(background);
	        g.clearRect(0, 0, width, height);
	        g.drawImage(img, 0, 0, width, height, null);
	    } finally {
	        g.dispose();
	    }
	    return newImage;
	}


	public BufferedImage Process(BufferedImage img) {
		int w = img.getWidth();
		int h = img.getHeight();
		int max_w=(int)((paper_right-paper_left)*dots_per_cm*cm_to_mm*margin);
		int max_h=(int)((paper_top-paper_bottom)*dots_per_cm*cm_to_mm*margin);
		
		// adjust up
		if(w<max_w && h<max_h) {
			if(w>h) {
				h*=(float)max_w/(float)w;
				w=max_w;
			} else {
				w*=(float)max_h/(float)h;
				h=max_h;
			}
		}
		// adjust down
		if(w>max_w) {
			h*=(float)max_w/(float)w;
			w=max_w;
		}
		if(h>max_h) {
			w*=(float)max_h/(float)h;
			h=max_h;
		}
		// now scale the image
		return scaleImage(img, w,h,Color.WHITE);
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