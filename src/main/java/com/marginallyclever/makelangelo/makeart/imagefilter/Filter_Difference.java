package com.marginallyclever.makelangelo.makeart.imagefilter;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.makelangelo.makeart.TransformedImage;

import java.awt.image.BufferedImage;

/**
 * Calculate the relative difference between two images.
 * @author Dan Royer
 * @since 7.46.0
 */
public class Filter_Difference extends ImageFilter {
    private final TransformedImage a,b;

    public Filter_Difference(TransformedImage a,TransformedImage b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public TransformedImage filter() {
        TransformedImage result = new TransformedImage(a);
        int w = a.getSourceImage().getWidth();
        int h = a.getSourceImage().getHeight();
        if(w != b.getSourceImage().getWidth() || h != b.getSourceImage().getHeight()) {
            throw new RuntimeException("Images must be the same size.");
        }

        BufferedImage aa = a.getSourceImage();
        BufferedImage bb = b.getSourceImage();
        BufferedImage rr = result.getSourceImage();

        for (int y = 0; y < h; ++y) {
            for (int x = 0; x < w; ++x) {
                ColorRGB diff = new ColorRGB(aa.getRGB(x, y));
                ColorRGB other = new ColorRGB(bb.getRGB(x, y));
                diff.red   = Math.max(0,diff.red   - other.red  );
                diff.green = Math.max(0,diff.green - other.green);
                diff.blue  = Math.max(0,diff.blue  - other.blue );
                rr.setRGB(x, y, diff.toInt());
            }
        }

        return result;
    }
}
