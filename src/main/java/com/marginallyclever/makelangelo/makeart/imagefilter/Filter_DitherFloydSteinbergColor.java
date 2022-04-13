package com.marginallyclever.makelangelo.makeart.imagefilter;

import com.marginallyclever.convenience.ColorPalette;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.makelangelo.makeart.TransformedImage;

import java.awt.image.BufferedImage;


/**
 * Floyd/Steinberg dithering
 *
 * @author Dan
 * See <a href="http://stackoverflow.com/questions/5940188/how-to-convert-a-24-bit-png-to-3-bit-png-using-floyd-steinberg-dithering">http://stackoverflow.com/questions/5940188/how-to-convert-a-24-bit-png-to-3-bit-png-using-floyd-steinberg-dithering</a>
 */
public class Filter_DitherFloydSteinbergColor extends ImageFilter {
  public ColorPalette palette;

  public Filter_DitherFloydSteinbergColor() {
    palette = new ColorPalette();
    palette.addColor(new ColorRGB(255, 0, 0));
    palette.addColor(new ColorRGB(0, 255, 0));
    palette.addColor(new ColorRGB(0, 0, 255));
  }


  private void ditherDirection(TransformedImage img, BufferedImage after, int y, ColorRGB[] error, ColorRGB[] nexterror, int direction) {
    int w = after.getWidth();
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
      after.setRGB(x, y, newPixel.toInt());
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

    TransformedImage after = new TransformedImage(img);
    BufferedImage afterBI = after.getSourceImage();
    
    // for each y from top to bottom
    for (y = 0; y < h; ++y) {
      ditherDirection(img, afterBI, y, error, nexterror, direction);

      direction = -direction;
      ColorRGB[] tmp = error;
      error = nexterror;
      nexterror = tmp;
    }

    return after;
  }
}