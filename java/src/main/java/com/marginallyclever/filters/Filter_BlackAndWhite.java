package com.marginallyclever.filters;

import java.awt.image.BufferedImage;


/**
 * Converts an image to N shades of grey.
 *
 * @author Dan
 */
public class Filter_BlackAndWhite extends ImageFilter {
  double levels = 2;


  public Filter_BlackAndWhite(int _levels) {
    levels = (double) _levels;
  }


  public BufferedImage filter(BufferedImage img) {
    int h = img.getHeight();
    int w = img.getWidth();
    int x, y, i;

    double max_intensity = -1000;
    double min_intensity = 1000;

    for (y = 0; y < h; ++y) {
      for (x = 0; x < w; ++x) {
        i = decode(img.getRGB(x, y));
        if (max_intensity < i) max_intensity = i;
        if (min_intensity > i) min_intensity = i;
      }
    }
    double intensity_range = max_intensity - min_intensity;

    double ilevels = 1;
    if (levels != 0) ilevels = 1.0 / levels;

    //System.out.println("min_intensity="+min_intensity);
    //System.out.println("max_intensity="+max_intensity);
    //System.out.println("levels="+levels);
    //System.out.println("inverse="+ilevels);

    double pixel;

    for (y = 0; y < h; ++y) {
      for (x = 0; x < w; ++x) {
        pixel = decode(img.getRGB(x, y));

        double a = (pixel - min_intensity) / intensity_range;
        double c = Math.ceil(a * levels) * ilevels;
        int b = (int) (c * 255.0);
        if (b > 255) b = 255;
        if (b < 0) b = 0;
        //if(b==255) System.out.println(x+"\t"+y+"\t"+i+"\t"+b);
        img.setRGB(x, y, ImageFilter.encode(b));
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
  public BufferedImage processViaHistogram(BufferedImage img) {
    int h = img.getHeight();
    int w = img.getWidth();

    int x, y, i;

    double[] histogram = new double[256];

    for (i = 0; i < 256; ++i) {
      histogram[i] = 0;
    }

    for (y = 0; y < h; ++y) {
      for (x = 0; x < w; ++x) {
        i = decode(img.getRGB(x, y));
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
        pixel = decode(img.getRGB(x, y));
        b = (int) histogram[pixel];
        img.setRGB(x, y, ImageFilter.encode(b));
      }
    }

    return img;
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
