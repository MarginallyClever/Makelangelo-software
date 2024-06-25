package com.marginallyclever.makelangelo.makeart.imagefilter;

import com.marginallyclever.makelangelo.makeart.TransformedImage;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Calculate a-b for every pixel.
 * @author Dan Royer
 * @since 7.46.0
 */
public class FilterSubtract extends ImageFilter {
    private final TransformedImage a,b;

    public FilterSubtract(TransformedImage a, TransformedImage b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public TransformedImage filter() {
        TransformedImage result = new TransformedImage(a);
        BufferedImage aa = a.getSourceImage();
        BufferedImage bb = b.getSourceImage();
        int w = aa.getWidth();
        int h = aa.getHeight();
        if(w != bb.getWidth() || h != bb.getHeight()) {
            throw new RuntimeException("Images must be the same size.");
        }

        BufferedImage rr = result.getSourceImage();

        for (int y = 0; y < h; ++y) {
            for (int x = 0; x < w; ++x) {
                Color diff = new Color(aa.getRGB(x, y));
                Color other = new Color(bb.getRGB(x, y));
                Color r2 = new Color(
                    modify(diff.getRed()  , other.getRed()  ),
                    modify(diff.getGreen(), other.getGreen()),
                    modify(diff.getBlue() , other.getBlue() ));
                rr.setRGB(x, y, r2.getRGB());
            }
        }

        return result;
    }

    private int modify(int a,int b) {
        double v = a-b;
        return (int)Math.max(0,Math.min(255, v));
    }
}
