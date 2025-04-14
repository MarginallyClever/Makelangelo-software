package com.marginallyclever.convenience;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Color palette for quantization
 *
 * @author danroyer
 * @since 7.1.4-SNAPSHOT
 */
@Deprecated
public class ColorPalette {
    /**
     * List of colors in the form of red, green, and blue data values.
     * <p>
     * See ColorRGB
     */
    @Deprecated(forRemoval = true)
    protected List<Color> colors = new ArrayList<>();


    public ColorPalette() {}

    /**
     * add a color to the palette.  Does not check for duplicates.
     *
     * @param c color
     */
    @Deprecated(forRemoval = true)
    public void addColor(Color c) {
        colors.add(c);
    }

    /**
     * Removes a given color if it exists in the list of colors.
     *
     * @param c color to remove.
     *          See <a href="http://stackoverflow.com/a/223929">Iterating through a list, avoiding ConcurrentModificationException when removing in loop</a>
     */
    @Deprecated(forRemoval = true)
    public void removeColor(Color c) {
        for (final Iterator<Color> colorsIterator = colors.iterator(); colorsIterator.hasNext(); ) {
            final Color nextColor = colorsIterator.next();
            if (nextColor.equals(c)) {
                colorsIterator.remove();
            }
        }
    }


    /**
     * @return the number of colors in this palette
     */
    @Deprecated(forRemoval = true)
    public int numColors() {
        return colors.size();
    }


    /**
     * get the color at a given index.
     *
     * @param index
     * @return color for a given index
     */
    @Deprecated(forRemoval = true)
    public Color getColor(int index) throws IndexOutOfBoundsException {
        return colors.get(index);
    }


    /**
     * find the color in the palette that most closely matches a given color.
     *
     * @param c the color to match
     * @return the closest match
     */
    @Deprecated(forRemoval = true)
    public Color quantize(Color c) {
        int i = quantizeIndex(c);

        return this.getColor(i);
    }


    /**
     * Find the index of the color in the palette that most closely matches a given color.
     *
     * @param c the color to match
     * @return the index into the color palette of the closest match
     */
    @Deprecated(forRemoval = true)
    public int quantizeIndex(Color c) {
        Iterator<Color> i = colors.iterator();
        assert (i.hasNext());

        Color color, nearestColor = i.next();
        int index = 0;
        int nearestIndex = 0;

        while (i.hasNext()) {
            color = i.next();
            ++index;
            if (diffSquared(color, c) < diffSquared(nearestColor, c)) {
                nearestColor = color;
                nearestIndex = index;
            }
        }

        return nearestIndex;
    }

    @Deprecated(forRemoval = true)
    private int diffSquared(Color a, Color b) {
        return Math.abs(a.getRed() - b.getRed())
                + Math.abs(a.getGreen() - b.getGreen())
                + Math.abs(a.getBlue() - b.getBlue());
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
     *
     * @param r in the range 0...255
     * @param g in the range 0...255
     * @param b in the range 0...255
     */
    @Deprecated(forRemoval = true)
    public double[] convertRGBtoCMYK(double r, double g, double b) {
        r /= 255;
        g /= 255;
        b /= 255;

        double k = 1 - Math.max(r, Math.max(g, b));
        double c = (1 - r - k) / (1 - k);
        double m = (1 - g - k) / (1 - k);
        double y = (1 - b - k) / (1 - k);

        return new double[]{c, m, y, k};
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
     *
     * @param c
     * @param m
     * @param y
     * @param k
     */
    @Deprecated(forRemoval = true)
    public double[] convertCMYKtoRGB(double c, double m, double y, double k) {
        double r = 255 * (1 - c) * (1 - k);
        double g = 255 * (1 - m) * (1 - k);
        double b = 255 * (1 - y) * (1 - k);

        return new double[]{r, g, b};
    }

    public static String getHexCode(Color c) {
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }
}
