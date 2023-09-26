package com.marginallyclever.makelangelo.makeart.imagefilter;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.makelangelo.makeart.TransformedImage;

import java.awt.image.BufferedImage;

/**
 * Any pixel above the threshold is made white.  Everything else is made black.
 * @author Dan Royer
 * @since 7.46.0
 */
public class FilterThreshold extends ImageFilter {
    private final TransformedImage a;
    private final int threshold;

    public FilterThreshold(TransformedImage a, int threshold) {
        this.a = a;
        this.threshold = threshold;
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

    int modify(int input) {
        return input >= threshold ? 255 : 0;
    }
}
