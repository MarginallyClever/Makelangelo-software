package com.marginallyclever.makelangelo.makeart.imagefilter;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.makelangelo.makeart.TransformedImage;

import java.awt.image.BufferedImage;

/**
 * Calculate abs(a-b) for every pixel
 * @author Dan Royer
 * @since 7.46.0
 */
public class FilterDifference extends ImageFilter {
    private final TransformedImage a,b;

    public FilterDifference(TransformedImage a, TransformedImage b) {
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
                ColorRGB diff = new ColorRGB(aa.getRGB(x, y));
                ColorRGB other = new ColorRGB(bb.getRGB(x, y));
                diff.red   = modify(diff.red  , other.red  );
                diff.green = modify(diff.green, other.green);
                diff.blue  = modify(diff.blue , other.blue );
                rr.setRGB(x, y, diff.toInt());
            }
        }

        return result;
    }

    private int modify(int a,int b) {
        double v = Math.abs(a-b);
        return (int)Math.max(0,Math.min(255, v));
    }
}
