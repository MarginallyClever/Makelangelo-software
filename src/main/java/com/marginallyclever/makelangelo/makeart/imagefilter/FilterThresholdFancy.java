package com.marginallyclever.makelangelo.makeart.imagefilter;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.makelangelo.makeart.TransformedImage;

import java.awt.image.BufferedImage;

/**
 * For any pixel u 0...1,<br>
 * <ul>
 *     <li>if u >= e, then v = 1</li>
 *     <li>if u <  e, then v = 1 + tanh(phi*(u-e))</li>
 * </ul>
 * @author Dan Royer
 * @since 7.46.0
 */
public class FilterThresholdFancy extends ImageFilter {
    private final TransformedImage a;
    private final int threshold;
    private final double phi;

    /**
     * @param a source image
     * @param threshold 0...255
     * @param phi >=0
     */
    public FilterThresholdFancy(TransformedImage a, int threshold, double phi) {
        this.a = a;
        this.threshold = threshold;
        this.phi = phi;
    }

    @Override
    public TransformedImage filter() {
        TransformedImage result = new TransformedImage(a);
        int w = a.getSourceImage().getWidth();
        int h = a.getSourceImage().getHeight();

        BufferedImage aa = a.getSourceImage();
        BufferedImage rr = result.getSourceImage();

        for (int y = 0; y < h; ++y) {
            for (int x = 0; x < w; ++x) {
                ColorRGB diff = new ColorRGB(aa.getRGB(x, y));
                diff.red   = modify(diff.red  );
                diff.green = modify(diff.green);
                diff.blue  = modify(diff.blue );
                rr.setRGB(x, y, diff.toInt());
            }
        }

        return result;
    }

    int modify(double input) {
        if(input >= threshold) return 255;
        // diff will always be <0
        double diff = input - threshold;
        double diffUnit = diff / 255.0;
        double temp = phi * diffUnit;
        // if tau>=0 then tanh will always be -1...0
        double vUnit = 1.0 + Math.tanh(temp);
        double v = vUnit * 255.0;
        return (int)Math.max( 0, Math.min(255,v) );
    }
}
