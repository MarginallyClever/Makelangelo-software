import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;


/**
 * Resize and flip horizontally if needed.
 * @author Dan
 */
public class Filter_Resize {
	private final double cm_to_mm=10.0f;
	private	double dots_per_cm=1;
	private double margin=0.9f;
	
	
	public Filter_Resize(double _dots_per_cm) {
		dots_per_cm=_dots_per_cm;
	}
	
	
	protected BufferedImage scaleImage(BufferedImage img, int width, int height, Color background) {
	    BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	    Graphics2D g = newImage.createGraphics();
	    try {
	        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
	                           RenderingHints.VALUE_INTERPOLATION_BICUBIC);
	        g.setBackground(background);
	        g.clearRect(0, 0, width, height);
	        if(MachineConfiguration.getSingleton().reverseForGlass) {
	        	g.drawImage(img, width, 0, 0, height, 0,0,img.getWidth(),img.getHeight(), null);
	        } else {
	        	g.drawImage(img, 0, 0, width, height, null);
	        }
	    } finally {
	        g.dispose();
	    }
	    return newImage;
	}


	public BufferedImage Process(BufferedImage img) {
		MachineConfiguration mc = MachineConfiguration.getSingleton();
		int w = img.getWidth();
		int h = img.getHeight();
		int max_w=(int)((mc.paper_right-mc.paper_left)*dots_per_cm*cm_to_mm*margin);
		int max_h=(int)((mc.paper_top-mc.paper_bottom)*dots_per_cm*cm_to_mm*margin);
		
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