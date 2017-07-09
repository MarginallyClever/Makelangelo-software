package com.marginallyclever.makelangeloRobot.imageFilters;

import java.awt.image.BufferedImage;

import com.marginallyclever.makelangeloRobot.TransformedImage;


/**
 * Converts an image to N shades of grey.
 *
 * @author Dan
 */
public class Filter_CMYK extends ImageFilter {
  protected static double levels = 2;
    
  protected TransformedImage channel_cyan;
  protected TransformedImage channel_magenta;
  protected TransformedImage channel_yellow;
  protected TransformedImage channel_black;


  public Filter_CMYK() {}


  public TransformedImage getC() {  return channel_cyan;  }
  public TransformedImage getM() {  return channel_magenta;  }
  public TransformedImage getY() {  return channel_yellow;  }
  public TransformedImage getK() {  return channel_black;  }
  
  
  // http://www.rapidtables.com/convert/color/rgb-to-cmyk.htm
  public TransformedImage filter(TransformedImage img) {
    int h = img.getSourceImage().getHeight();
    int w = img.getSourceImage().getWidth();
    int x, y;

    BufferedImage bi = img.getSourceImage();
    channel_cyan = new TransformedImage(img);
    channel_magenta = new TransformedImage(img);
    channel_yellow = new TransformedImage(img);
    channel_black = new TransformedImage(img);
    
    BufferedImage cc = channel_cyan.getSourceImage();
    BufferedImage cm = channel_magenta.getSourceImage();
    BufferedImage cy = channel_yellow.getSourceImage();
    BufferedImage ck = channel_black.getSourceImage();
    double r,g,b,k1,ik,c1,m1,y1;
    int pixel;
    
    for (y = 0; y < h; ++y) {
      for (x = 0; x < w; ++x) {
    	pixel = bi.getRGB(x,y);
    	//double a = 255-((pixel>>24) & 0xff);
		r = (double)((pixel >> 16) & 0xff) / 255.0;
		g = (double)((pixel >>  8) & 0xff) / 255.0;
		b = (double)((pixel      ) & 0xff) / 255.0;
		// now convert to cmyk
		k1 = 1.0-Math.max(Math.max(r,g),b);   // should be Math.max(Math.max(r,g),b) but colors are inverted.
		ik = 1.0-k1;
		
		if(ik<1.0/255.0) {
			c1=m1=y1=0;
		} else {
			c1 = (ik-r) / ik;
			m1 = (ik-g) / ik;
			y1 = (ik-b) / ik;
		}
        cc.setRGB(x, y, ImageFilter.encode32bit(255-(int)(c1*255.0)));
        cm.setRGB(x, y, ImageFilter.encode32bit(255-(int)(m1*255.0)));
        cy.setRGB(x, y, ImageFilter.encode32bit(255-(int)(y1*255.0)));
        ck.setRGB(x, y, ImageFilter.encode32bit(255-(int)(k1*255.0)));
      }
    }

    return img;
  }


  /**
   * An experimental black &#38; white converter that doesn't just greyscale to 4 levels, it also tries to divide by histogram frequency.
   * Didn't look good so I left it for the lulz.
   *
   * @param img the <code>java.awt.image.BufferedImage</code> this filter is to process.
   * @return the altered image
   */
  @Deprecated
  public TransformedImage processViaHistogram(TransformedImage img) {
    int h = img.getSourceImage().getHeight();
    int w = img.getSourceImage().getWidth();

    int x, y, i;

    double[] histogram = new double[256];

    for (i = 0; i < 256; ++i) {
      histogram[i] = 0;
    }

    for (y = 0; y < h; ++y) {
      for (x = 0; x < w; ++x) {
        i = decode32bit(img.getSourceImage().getRGB(x, y));
        ++histogram[i];
      }
    }

    double histogram_area = 0;
    //System.out.println("histogram:");
    for (i = 1; i < 255; ++i) {
      System.out.println(i + "=" + histogram[i]);
      histogram_area += histogram[i];
    }
    double histogram_zone = histogram_area / (double) levels;
    //System.out.println("histogram area: "+histogram_area);
    //System.out.println("histogram zone: "+histogram_zone);

    double histogram_sum = 0;
    x = 0;
    y = 0;
    for (i = 1; i < 255; ++i) {
      histogram_sum += histogram[i];
      //System.out.println("mapping "+i+" to "+x);
      if (histogram_sum > histogram_zone) {
        //System.out.println("level up at "+i+" "+histogram_sum+" vs "+histogram_zone);
        histogram_sum -= histogram_zone;
        x += (int) (256.0 / (double) levels);
        ++y;
      }
      histogram[i] = x;
    }

    //System.out.println("y="+y+" x="+x);
    int pixel, b;

    for (y = 0; y < h; ++y) {
      for (x = 0; x < w; ++x) {
        pixel = decode32bit(img.getSourceImage().getRGB(x, y));
        b = (int) histogram[pixel];
        img.getSourceImage().setRGB(x, y, ImageFilter.encode32bit(b));
      }
    }

    return img;
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
