package com.marginallyclever.makelangelo.makeart.imagefilter;

import com.marginallyclever.makelangelo.makeart.TransformedImage;

import javax.vecmath.Vector2d;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * <p>Calculates the gradient of an image. The gradient is calculated using a 3x1 filter.</p>
 * <p>See also <a href="https://en.wikipedia.org/wiki/Image_gradient">Wikipedia</a>.</p>
 */
public class FilterGradient extends ImageFilter {
    // private static final Logger logger = LoggerFactory.getLogger(FilterTangent.class);

    protected TransformedImage img;
    protected TransformedImage result;

    public FilterGradient(TransformedImage img) {
        super();
        this.img = img;
    }

    @Override
    public TransformedImage filter() {
        // use adjacent pixel to calculate the tangent of the greyscale intensity gradient.
        // put the X result in the red channel
        // put the Y result in the green channel
        FilterDesaturate desaturate = new FilterDesaturate(img);
        var grey = desaturate.filter();
        result = new TransformedImage(img);
        BufferedImage ri = result.getSourceImage();
        BufferedImage si = grey.getSourceImage();
        int h = si.getHeight();
        int w = si.getWidth();

        var ra = ri.getRaster();
        var sa = si.getRaster();

        for (int py = 0; py < h; ++py) {
            for (int px = 0; px < w; ++px) {
                Vector2d slope = calculateSlope(px, py, w, h, sa);
                // set the red channel to the X slope
                ra.setSample(px, py, 0, (int) slope.x);
                // set the green channel to the Y slope
                ra.setSample(px, py, 1, (int) slope.y);
                // set the blue channel to 0
                ra.setSample(px, py, 2, 0);
                if(ra.getNumBands()>3) {
                    // set the alpha channel to 255 (opaque)
                    ra.setSample(px, py, 3, 255);
                }
            }
        }

        return result;
    }

    /**
     * Calculate the slope of the greyscale intensity gradient at a pixel using a 3x1 filter.
     * @param px the pixel X coordinate
     * @param py the pixel Y coordinate
     * @param w the width of the image
     * @param h the height of the image
     * @param ba
     * @return
     */
    private Vector2d calculateSlope(int px, int py, int w, int h, WritableRaster ba) {
        double x = 0;
        double y = 0;

        // calculate the X slope
        if (px > 0 && px < w - 1) {
            int left = ba.getSample(px - 1, py, 0);
            int right = ba.getSample(px + 1, py, 0);
            x = right - left;
        }

        // calculate the Y slope
        if (py > 0 && py < h - 1) {
            int above = ba.getSample(px, py - 1, 0);
            int below = ba.getSample(px, py + 1, 0);
            y = below - above;
        }

        // the maximum of each axis is +/-256.  we need the range 0..255.
        x = Math.max(-256.0, Math.min(256.0, x));
        y = Math.max(-256.0, Math.min(256.0, y));
        x = x/2.0 + 128.0; // scale to 0..255
        y = y/2.0 + 128.0; // scale to 0..255

        return new Vector2d(x, y);
    }
}
