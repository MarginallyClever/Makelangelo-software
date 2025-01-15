package com.marginallyclever.makelangelo.makeart.imagefilter;

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
        var rasterA = aa.getRaster();
        var rasterB = bb.getRaster();
        var rasterR = rr.getRaster();
        var cm = aa.getColorModel();
        // Temporary array to hold pixel components
        int[] pixelA = new int[cm.getNumComponents()];
        int[] pixelB = new int[cm.getNumComponents()];

        for (int y = 0; y < h; ++y) {
            for (int x = 0; x < w; ++x) {
                rasterA.getPixel(x, y, pixelA);
                rasterB.getPixel(x, y, pixelB);
                pixelA[0] = modify(pixelA[0],pixelB[0]);
                pixelA[1] = modify(pixelA[1],pixelB[1]);
                pixelA[2] = modify(pixelA[2],pixelB[2]);
                rasterR.setPixel(x, y, pixelA);
            }
        }

        return result;
    }

    private int modify(int a,int b) {
        double v = Math.abs(a-b);
        return (int)Math.max(0,Math.min(255, v));
    }
}
