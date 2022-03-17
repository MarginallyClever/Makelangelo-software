package com.marginallyClever.makelangelo.makeArt.imageFilter;

import java.awt.image.BufferedImage;

import com.marginallyClever.convenience.ColorRGB;
import com.marginallyClever.makelangelo.makeArt.TransformedImage;


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

    TransformedImage after = new TransformedImage(img);
    BufferedImage afterBI = after.getSourceImage();
    
    for (y = 0; y < h; ++y) {
      for (x = 0; x < w; ++x) {
        ColorRGB color = new ColorRGB(img.getSourceImage().getRGB(x, y));
        color.red   = 255 - color.red;
        color.green = 255 - color.green;
        color.blue  = 255 - color.blue;
        afterBI.setRGB(x, y, color.toInt());
      }
    }

    return after;
  }
}