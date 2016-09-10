package com.marginallyclever.makelangeloRobot.imageFilters;

import com.marginallyclever.makelangelo.ColorPalette;
import com.marginallyclever.makelangelo.ColorRGB;
import com.marginallyclever.makelangeloRobot.TransformedImage;


/**
 * Floyd/Steinberg dithering
 *
 * @author Dan
 * @see <a href="http://stackoverflow.com/questions/5940188/how-to-convert-a-24-bit-png-to-3-bit-png-using-floyd-steinberg-dithering">http://stackoverflow.com/questions/5940188/how-to-convert-a-24-bit-png-to-3-bit-png-using-floyd-steinberg-dithering</a>
 */
public class Filter_DitherFloydSteinbergColor extends ImageFilter {
  public ColorPalette palette;

  public Filter_DitherFloydSteinbergColor() {
    palette = new ColorPalette();
    palette.addColor(new ColorRGB(255, 0, 0));
    palette.addColor(new ColorRGB(0, 255, 0));
    palette.addColor(new ColorRGB(0, 0, 255));
  }


  private void ditherDirection(TransformedImage img, int y, ColorRGB[] error, ColorRGB[] nexterror, int direction) {
    int w = img.getSourceImage().getWidth();
    ColorRGB oldPixel = new ColorRGB(0, 0, 0);
    ColorRGB newPixel = new ColorRGB(0, 0, 0);
    ColorRGB quant_error = new ColorRGB(0, 0, 0);
    int start, end, x;

    for (x = 0; x < w; ++x) nexterror[x].set(0, 0, 0);

    if (direction > 0) {
      start = 0;
      end = w;
    } else {
      start = w - 1;
      end = -1;
    }

    // for each x from left to right
    for (x = start; x != end; x += direction) {
      // oldpixel := pixel[x][y]
      oldPixel.set(new ColorRGB(img.getSourceImage().getRGB(x, y)).add(error[x]));
      // newpixel := find_closest_palette_color(oldpixel)
      newPixel = palette.quantize(oldPixel);
      // pixel[x][y] := newpixel
      img.getSourceImage().setRGB(x, y, newPixel.toInt());
      // quant_error := oldpixel - newpixel
      quant_error.set(oldPixel.sub(newPixel));
      // pixel[x+1][y  ] += 7/16 * quant_error
      // pixel[x-1][y+1] += 3/16 * quant_error
      // pixel[x  ][y+1] += 5/16 * quant_error
      // pixel[x+1][y+1] += 1/16 * quant_error
      nexterror[x].add(quant_error.mul(5.0 / 16.0));
      if (x + direction >= 0 && x + direction < w) {
        error[x + direction].add(quant_error.mul(7.0 / 16.0));
        nexterror[x + direction].add(quant_error.mul(1.0 / 16.0));
      }
      if (x - direction >= 0 && x - direction < w) {
        nexterror[x - direction].add(quant_error.mul(3.0 / 16.0));
      }
    }
  }

  
  public TransformedImage filter(TransformedImage img) {
    int y;
    int h = img.getSourceImage().getHeight();
    int w = img.getSourceImage().getWidth();
    int direction = 1;
    ColorRGB[] error = new ColorRGB[w];
    ColorRGB[] nexterror = new ColorRGB[w];

    for (y = 0; y < w; ++y) {
      error[y] = new ColorRGB(0, 0, 0);
      nexterror[y] = new ColorRGB(0, 0, 0);
    }

    // for each y from top to bottom
    for (y = 0; y < h; ++y) {
      ditherDirection(img, y, error, nexterror, direction);

      direction = -direction;
      ColorRGB[] tmp = error;
      error = nexterror;
      nexterror = tmp;
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
