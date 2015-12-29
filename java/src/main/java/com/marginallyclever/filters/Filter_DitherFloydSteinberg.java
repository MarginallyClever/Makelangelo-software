package com.marginallyclever.filters;

import java.awt.image.BufferedImage;

import com.marginallyclever.makelangelo.MakelangeloRobotSettings;
import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.Translator;

/**
 * Floyd/Steinberg dithering
 *
 * @author Dan
 * @see <a href="http://en.literateprograms.org/Floyd-Steinberg_dithering_%28C%29">http://en.literateprograms.org/Floyd-Steinberg_dithering_%28C%29</a> and <a href="http://www.home.unix-ag.org/simon/gimp/fsdither.c">http://www.home.unix-ag.org/simon/gimp/fsdither.c</a>
 */
public class Filter_DitherFloydSteinberg extends ImageFilter {
  private long tone;


  public Filter_DitherFloydSteinberg(Makelangelo gui, MakelangeloRobotSettings mc,
                                     Translator ms) {
    super(gui, mc, ms);
  }


  private int quantizeColor(int original) {
    int i = (int) Math.min(Math.max(original, 0), 255);
    return (i > tone) ? 255 : 0;
  }


  private void ditherDirection(BufferedImage img, int y, int[] error, int[] nexterror, int direction) {
    int w = img.getWidth();
    int oldPixel, newPixel, quant_error;
    int start, end, x;

    for (x = 0; x < w; ++x) nexterror[x] = 0;

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
      oldPixel = decode(img.getRGB(x, y)) + error[x];
      // newpixel := find_closest_palette_color(oldpixel)
      newPixel = quantizeColor(oldPixel);
      // pixel[x][y] := newpixel
      img.setRGB(x, y, encode(newPixel));
      // quant_error := oldpixel - newpixel
      quant_error = oldPixel - newPixel;
      // pixel[x+1][y  ] += 7/16 * quant_error
      // pixel[x-1][y+1] += 3/16 * quant_error
      // pixel[x  ][y+1] += 5/16 * quant_error
      // pixel[x+1][y+1] += 1/16 * quant_error
      nexterror[x] += 5.0 / 16.0 * quant_error;
      if (x + direction >= 0 && x + direction < w) {
        error[x + direction] += 7.0 / 16.0 * quant_error;
        nexterror[x + direction] += 1.0 / 16.0 * quant_error;
      }
      if (x - direction >= 0 && x - direction < w) {
        nexterror[x - direction] += 3.0 / 16.0 * quant_error;
      }
    }
  }

  
  public BufferedImage filter(BufferedImage img) {
    int y, x;
    int h = img.getHeight();
    int w = img.getWidth();
    int direction = 1;
    int[] error = new int[w];
    int[] nexterror = new int[w];

    for (y = 0; y < w; ++y) {
      error[y] = nexterror[y] = 0;
    }

    // find the average color of the system
    for (y = 0; y < h; ++y) {
      for (x = 0; x < w; ++x) {
        tone += decode(img.getRGB(x, y));
      }
    }

    tone /= (w * h);


    // for each y from top to bottom
    for (y = 0; y < h; ++y) {
      ditherDirection(img, y, error, nexterror, direction);

      direction = direction > 0 ? -1 : 1;
      int[] tmp = error;
      error = nexterror;
      nexterror = tmp;
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
