package com.marginallyclever.makelangelo.makeart.imagefilter;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.makelangelo.makeart.TransformedImage;

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
                ColorRGB diff = new ColorRGB(aa.getRGB(x, y));
                diff.red   = (int)Math.max(0,Math.min(255,diff.red * scale));
                diff.green = (int)Math.max(0,Math.min(255,diff.green * scale));
                diff.blue  = (int)Math.max(0,Math.min(255,diff.blue * scale));
                rr.setRGB(x, y, diff.toInt());
            }
        }

        return result;
    }
}
