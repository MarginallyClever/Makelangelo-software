package com.marginallyclever.makelangelo.makeart.imagefilter;

import com.marginallyclever.makelangelo.makeart.TransformedImage;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Scale every pixel in the image by a value.
 * @author Dan Royer
 * @since 7.46.0
 */
public class FilterScale extends ImageFilter {
    private final TransformedImage a;
    private final double scale;

    public FilterScale(TransformedImage a, double scale) {
        this.a = a;
        this.scale = scale;
    }

    @Override
    public TransformedImage filter() {
        TransformedImage result = new TransformedImage(a);
        BufferedImage aa = a.getSourceImage();
        int w = aa.getWidth();
        int h = aa.getHeight();

        BufferedImage rr = result.getSourceImage();

        for (int y = 0; y < h; ++y) {
            for (int x = 0; x < w; ++x) {
                Color diff = new Color(aa.getRGB(x, y));
                Color r2 = new Color(
                    (int)Math.max(0,Math.min(255,diff.getRed() * scale)),
                    (int)Math.max(0,Math.min(255,diff.getGreen() * scale)),
                    (int)Math.max(0,Math.min(255,diff.getBlue() * scale)));
                rr.setRGB(x, y, r2.getRGB());
            }
        }

        return result;
    }
}
