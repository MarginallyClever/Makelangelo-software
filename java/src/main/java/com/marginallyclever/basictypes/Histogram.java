package com.marginallyclever.basictypes;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Histogram of an image with 8 bits red, 8 bits green, and 8 bits blue.
 *
 * @author danroyer
 * @since 7.1.4-SNAPSHOT?
 */
public class Histogram {
  public char[] red = new char[256];
  public char[] green = new char[256];
  public char[] blue = new char[256];

  public Histogram() {}

  public void getHistogramOf(BufferedImage img) {
    int w = img.getWidth();
    int h = img.getHeight();
    int x, y;

    for (y = 0; y < h; ++y) {
      for (x = 0; x < w; ++x) {
        Color c = new Color(img.getRGB(x, y));
        red[c.getRed()]++;
        green[c.getGreen()]++;
        blue[c.getBlue()]++;
      }
    }
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
