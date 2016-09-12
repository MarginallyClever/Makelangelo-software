package com.marginallyclever.makelangelo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Color palette for quantization
 *
 * @author danroyer
 * @since 7.1.4-SNAPSHOT
 */
public class ColorPalette {

  /**
   * List of colors in the form of red, green, and blue data values.
   *
   * @see ColorRGB
   */
  protected List<ColorRGB> colors;
  

  public ColorPalette() {
    colors = new ArrayList<>();
  }

  /**
   * add a color to the palette.  Does not check for duplicates.
   * @param c
   */
  public void addColor(ColorRGB c) {
    colors.add(c);
  }

  /**
   * Removes a given color if it exists in {@link ColorPalette#colors}.
   *
   * @param c color to remove.
   * @see <a href="http://stackoverflow.com/a/223929">Iterating through a list, avoiding ConcurrentModificationException when removing in loop</a>
   */
  public void removeColor(ColorRGB c) {
    for (final Iterator<ColorRGB> colorsIterator = colors.iterator(); colorsIterator.hasNext(); ) {
      final ColorRGB nextColor = colorsIterator.next();
      if (nextColor.equals(c)) {
        colorsIterator.remove();
      }
    }
  }


  /**
   * 
   * @return the number of colors in this palette
   */
  public int numColors() {
    return colors.size();
  }


  /**
   * get the color at a given index.
   * @param index
   * @return
   */
  public ColorRGB getColor(int index) throws IndexOutOfBoundsException {
    return colors.get(index);
  }


  /**
   * find the color in the palette that most closely matches a given color.
   * @param c the color to match
   * @return the closest match
   */
  public ColorRGB quantize(ColorRGB c) {
    int i = quantizeIndex(c);

    return this.getColor(i);
  }


  /**
   * Find the index of the color in the palette that most closely matches a given color.
   * @param c the color to match
   * @return the index into the color palette of the closest match 
   */
  public int quantizeIndex(ColorRGB c) {
    Iterator<ColorRGB> i = colors.iterator();
    assert (i.hasNext());

    ColorRGB color, nearestColor = i.next();
    int index = 0;
    int nearestIndex = 0;

    while (i.hasNext()) {
      color = i.next();
      ++index;
      if (color.diff(c) < nearestColor.diff(c)) {
        nearestColor = color;
        nearestIndex = index;
      }
    }

    return nearestIndex;
  }
  
  /**
   * The black key (K) color is calculated from the red (R'), green (G') and blue (B') colors:
   * K = 1-max(R', G', B')
   * The cyan color (C) is calculated from the red (R') and black (K) colors:
   * C = (1-R'-K) / (1-K)
   * The magenta color (M) is calculated from the green (G') and black (K) colors:
   * M = (1-G'-K) / (1-K)
   * The yellow color (Y) is calculated from the blue (B') and black (K) colors:
   * Y = (1-B'-K) / (1-K)
   * see http://www.rapidtables.com/convert/color/rgb-to-cmyk.htm
   * @param r in the range 0...255
   * @param g in the range 0...255
   * @param b in the range 0...255
   */
  public double[] convertRGBtoCMYK(double r,double g,double b) {
	  r/=255;
	  g/=255;
	  b/=255;
	  
	  double k = 1 - Math.max(r,Math.max(g,b));
	  double c = (1-r-k) / (1-k);
	  double m = (1-g-k) / (1-k);
	  double y = (1-b-k) / (1-k);
	  
	  return new double[]{c,m,y,k};
  }
  
  /**
   * The R,G,B values are given in the range of 0..255.
   * The red (R) color is calculated from the cyan (C) and black (K) colors:
   * R = 255 × (1-C) × (1-K)
   * The green color (G) is calculated from the magenta (M) and black (K) colors:
   * G = 255 × (1-M) × (1-K)
   * The blue color (B) is calculated from the yellow (Y) and black (K) colors:
   * B = 255 × (1-Y) × (1-K)
   * see http://www.rapidtables.com/convert/color/cmyk-to-rgb.htm
   * @param c
   * @param m
   * @param y
   * @param k
   */
  public double[] convertCMYKtoRGB(double c,double m,double y,double k) {
	  double r = 255 * (1-c)*(1-k);
	  double g = 255 * (1-m)*(1-k);
	  double b = 255 * (1-y)*(1-k);
	  
	  return new double[]{r,g,b};
  }
}
