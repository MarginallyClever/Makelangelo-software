package com.marginallyclever.basictypes;

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
   * @see C3
   */
  protected List<C3> colors;
  

  public ColorPalette() {
    colors = new ArrayList<>();
  }

  /**
   * add a color to the palette.  Does not check for duplicates.
   * @param c
   */
  public void addColor(C3 c) {
    colors.add(c);
  }

  /**
   * Removes a given color if it exists in {@link ColorPalette#colors}.
   *
   * @param c color to remove.
   * @see <a href="http://stackoverflow.com/a/223929">Iterating through a list, avoiding ConcurrentModificationException when removing in loop</a>
   */
  public void removeColor(C3 c) {
    for (final Iterator<C3> colorsIterator = colors.iterator(); colorsIterator.hasNext(); ) {
      final C3 nextColor = colorsIterator.next();
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
  public C3 getColor(int index) throws IndexOutOfBoundsException {
    return colors.get(index);
  }


  /**
   * find the color in the palette that most closely matches a given color.
   * @param c the color to match
   * @return the closest match
   */
  public C3 quantize(C3 c) {
    int i = quantizeIndex(c);

    return this.getColor(i);
  }


  /**
   * Find the index of the color in the palette that most closely matches a given color.
   * @param c the color to match
   * @return the index into the color palette of the closest match 
   */
  public int quantizeIndex(C3 c) {
    Iterator<C3> i = colors.iterator();
    assert (i.hasNext());

    C3 color, nearestColor = i.next();
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
}
