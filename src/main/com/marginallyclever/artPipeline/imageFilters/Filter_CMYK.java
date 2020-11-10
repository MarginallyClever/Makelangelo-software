package com.marginallyclever.artPipeline.imageFilters;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Test;

import com.marginallyclever.artPipeline.TransformedImage;
import com.marginallyclever.convenience.log.Log;


/**
 * Converts an image to N shades of grey.
 *
 * @author Dan
 */
public class Filter_CMYK extends ImageFilter {
  protected static double levels = 2;
    
  protected TransformedImage channelCyan;
  protected TransformedImage channelMagenta;
  protected TransformedImage channelYellow;
  protected TransformedImage channelBlack;


  public Filter_CMYK() {}


  public TransformedImage getC() {  return channelCyan;  }
  public TransformedImage getM() {  return channelMagenta;  }
  public TransformedImage getY() {  return channelYellow;  }
  public TransformedImage getK() {  return channelBlack;  }
  
  
  // http://www.rapidtables.com/convert/color/rgb-to-cmyk.htm
  public TransformedImage filter(TransformedImage img) {
    int h = img.getSourceImage().getHeight();
    int w = img.getSourceImage().getWidth();
    int px, py;

    BufferedImage bi = img.getSourceImage();
    channelCyan = new TransformedImage(img);
    channelMagenta = new TransformedImage(img);
    channelYellow = new TransformedImage(img);
    channelBlack = new TransformedImage(img);
    
    BufferedImage cc = channelCyan.getSourceImage();
    BufferedImage cm = channelMagenta.getSourceImage();
    BufferedImage cy = channelYellow.getSourceImage();
    BufferedImage ck = channelBlack.getSourceImage();
    double r,g,b,k,ik,c,m,y;
    int pixel;
    
    for (py = 0; py < h; ++py) {
      for (px = 0; px < w; ++px) {
    	pixel = bi.getRGB(px,py);
    	//double a = 255-((pixel>>24) & 0xff);
		r = 1.0-(double)((pixel >> 16) & 0xff) / 255.0;
		g = 1.0-(double)((pixel >>  8) & 0xff) / 255.0;
		b = 1.0-(double)((pixel      ) & 0xff) / 255.0;
		// now convert to cmyk
		k = Math.min(Math.min(r,g),b);   // should be Math.max(Math.max(r,g),b) but colors are inverted.
		ik = 1.0 - k;
		
//		if(ik<1.0/255.0) {
//			c1=m1=y1=0;
//		} else {
			c = (r-k) / ik;
			m = (g-k) / ik;
			y = (b-k) / ik;
		//}
        cc.setRGB(px, py, ImageFilter.encode32bit(255-(int)(c*255.0)));
        cm.setRGB(px, py, ImageFilter.encode32bit(255-(int)(m*255.0)));
        cy.setRGB(px, py, ImageFilter.encode32bit(255-(int)(y*255.0)));
        ck.setRGB(px, py, ImageFilter.encode32bit(255-(int)(k*255.0)));
      }
    }

    return img;
  }

  @Test
  public void testConversion() {
		try {
			final String PATH_NAME = "target/classes/bill-murray";
			final String EXT = "jpg";
			File file = new File(PATH_NAME+"."+EXT);
			assert(file.isFile());
			TransformedImage img = new TransformedImage( ImageIO.read(new FileInputStream(file)) );
			Filter_CMYK filter = new Filter_CMYK();
			filter.filter(img);

		    ImageIO.write(filter.getC().getSourceImage(), EXT, new File(PATH_NAME+"C."+EXT));
		    ImageIO.write(filter.getM().getSourceImage(), EXT, new File(PATH_NAME+"M."+EXT));
		    ImageIO.write(filter.getY().getSourceImage(), EXT, new File(PATH_NAME+"Y."+EXT));
		    ImageIO.write(filter.getK().getSourceImage(), EXT, new File(PATH_NAME+"K."+EXT));
		} catch (IOException e1) {
			e1.printStackTrace();
			assert(false);
		}
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
    //Log.message("histogram:");
    for (i = 1; i < 255; ++i) {
      Log.message(i + "=" + histogram[i]);
      histogram_area += histogram[i];
    }
    double histogram_zone = histogram_area / (double) levels;
    //Log.message("histogram area: "+histogram_area);
    //Log.message("histogram zone: "+histogram_zone);

    double histogram_sum = 0;
    x = 0;
    y = 0;
    for (i = 1; i < 255; ++i) {
      histogram_sum += histogram[i];
      //Log.message("mapping "+i+" to "+x);
      if (histogram_sum > histogram_zone) {
        //Log.message("level up at "+i+" "+histogram_sum+" vs "+histogram_zone);
        histogram_sum -= histogram_zone;
        x += (int) (256.0 / (double) levels);
        ++y;
      }
      histogram[i] = x;
    }

    //Log.message("y="+y+" x="+x);
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
