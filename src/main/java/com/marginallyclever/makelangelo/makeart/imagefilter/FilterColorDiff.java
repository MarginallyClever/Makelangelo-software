package com.marginallyclever.makelangelo.makeart.imagefilter;

import com.marginallyclever.makelangelo.makeart.TransformedImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Given a single color, calculate the difference between that color and the color of each pixel in the image.
 * Matching colors should be white.  Different colors should be black.
 * @author Dan Royer
 */
public class FilterColorDiff extends ImageFilter {
    private static final Logger logger = LoggerFactory.getLogger(FilterColorDiff.class);
    protected static double levels = 2;

    private final TransformedImage img;
    private final TransformedImage result;
    private final Color color;

    /**
     * @param img the <code>java.awt.image.BufferedImage</code> this filter is using as source material.
     */
    public FilterColorDiff(TransformedImage img, Color color) {
        super();
        this.img = img;
        this.color = color;
        result = new TransformedImage(img);
    }

    public TransformedImage getResult() {
        return result;
    }

    /**
     * Converts an image to 4 greyscale images, one for each channel of CMYK.
     * See <a href="http://www.rapidtables.com/convert/color/rgb-to-cmyk.htm">RGB to CMYK</a>
     * @return the original image.
     */
    @Override
    public TransformedImage filter() {
        int h = img.getSourceImage().getHeight();
        int w = img.getSourceImage().getWidth();

        BufferedImage bi = img.getSourceImage();
        BufferedImage cc = result.getSourceImage();

        double cr = color.getRed();
        double cg = color.getGreen();
        double cb = color.getBlue();

        for (int py = 0; py < h; ++py) {
            for (int px = 0; px < w; ++px) {
                int pixel = bi.getRGB(px, py);

                int pr = (pixel >> 16) & 0xff;
                int pg = (pixel >>  8) & 0xff;
                int pb = (pixel      ) & 0xff;

                int dr = (int)Math.abs(pr - cr);
                int db = (int)Math.abs(pb - cb);
                int dg = (int)Math.abs(pg - cg);

                cc.setRGB(px, py, ImageFilter.encode32bit(dr,dg,db,255));
            }
        }

        return img;
    }
}