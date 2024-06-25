package com.marginallyclever.makelangelo.makeart.imagefilter;

import com.marginallyclever.makelangelo.makeart.TransformedImage;

import java.awt.*;
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
                Color diff = new Color(aa.getRGB(x, y));
                Color r2 = new Color(
                    modify(diff.getRed()  ),
                    modify(diff.getGreen()),
                    modify(diff.getBlue() ));
                rr.setRGB(x, y, r2.getRGB());
            }
        }

        return result;
    }

    int modify(int input) {
        return input >= threshold ? 255 : 0;
    }
}
