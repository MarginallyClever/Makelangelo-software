package com.marginallyclever.makelangelo.makeart.imagefilter;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.ResizableImagePanel;
import com.marginallyclever.makelangelo.makeart.TransformedImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Calculate the extended difference of gaussians.
 * <pre>(1+tau)* G(sigma) - tau * G(k*sigma)</pre>
 * where G is a gaussian blur.
 * @author Dan Royer
 * @since 7.46.0
 */
public class FilterExtendedDifferenceOfGaussians extends ImageFilter {
    private final TransformedImage imgA, imgB;
    private final double phi;

    /**
     *
     * @param imgA an image blurred with a small sigma
     * @param imgB an image blurred with a larger sigma
     * @param phi a value between 0 and 1
     */
    public FilterExtendedDifferenceOfGaussians(TransformedImage imgA, TransformedImage imgB, double phi) {
        this.imgA = imgA;
        this.imgB = imgB;
        this.phi = phi;
    }

    @Override
    public TransformedImage filter() {
        BufferedImage aa = imgA.getSourceImage();
        BufferedImage bb = imgB.getSourceImage();
        int w = aa.getWidth();
        int h = aa.getHeight();
        if(w != bb.getWidth() || h != bb.getHeight()) {
            throw new RuntimeException("Images must be the same size.");
        }

        TransformedImage result = new TransformedImage(imgA);
        BufferedImage rr = result.getSourceImage();

        ColorRGB diff = new ColorRGB();
        for (int y = 0; y < h; ++y) {
            for (int x = 0; x < w; ++x) {
                ColorRGB colorA  = new ColorRGB(aa.getRGB(x, y));
                ColorRGB colorB = new ColorRGB(bb.getRGB(x, y));
                diff.red   = modify(colorA.red,   colorB.red  );
                diff.green = modify(colorA.green, colorB.green);
                diff.blue  = modify(colorA.blue,  colorB.blue );
                rr.setRGB(x, y, diff.toInt());
            }
        }

        return result;
    }

    int modify(double a,double b) {
        a/=255.0;
        b/=255.0;
        double v = (1.0+phi)*a - phi*b;
        v*=255.0;
        return (int)Math.max(0,Math.min(255, v));
    }

    public static void main(String[] args) throws IOException {
        TransformedImage src = new TransformedImage(ImageIO.read(new FileInputStream("src/test/resources/mandrill.png")));
        FilterGaussianBlur a = new FilterGaussianBlur(src,4);
        FilterGaussianBlur b = new FilterGaussianBlur(src,6);
        TransformedImage a2 = a.filter();
        TransformedImage b2 = b.filter();
        FilterExtendedDifferenceOfGaussians f = new FilterExtendedDifferenceOfGaussians(a2,b2,50.0);
        TransformedImage dest = f.filter();
        ResizableImagePanel.showImage(dest.getSourceImage(), "Filter_ExtendedDifferenceOfGaussians");
    }
}
