package com.marginallyclever.imageFilters;

import com.marginallyclever.basictypes.C3;
import com.marginallyclever.basictypes.TransformedImage;


/**
 * Inverts the colors in an image.
 *
 * @author Dan
 */
public class Filter_Invert extends ImageFilter {
  public TransformedImage filter(TransformedImage img) {
    int h = img.getSourceImage().getHeight();
    int w = img.getSourceImage().getWidth();
    int x, y;

    for (y = 0; y < h; ++y) {
      for (x = 0; x < w; ++x) {
        C3 color = new C3(img.getSourceImage().getRGB(x, y));
        color.red   = 255 - color.red;
        color.green = 255 - color.green;
        color.blue  = 255 - color.blue;
        img.getSourceImage().setRGB(x, y, color.toInt());
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
