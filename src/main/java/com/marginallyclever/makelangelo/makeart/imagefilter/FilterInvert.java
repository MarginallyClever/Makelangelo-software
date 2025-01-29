package com.marginallyclever.makelangelo.makeart.imagefilter;

import com.marginallyclever.convenience.ResizableImagePanel;
import com.marginallyclever.makelangelo.makeart.TransformedImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.stream.IntStream;


/**
 * Inverts the colors in an image.
 *
 * @author Dan
 */
public class FilterInvert extends ImageFilter {
    private final TransformedImage img;

    public FilterInvert(TransformedImage img) {
        super();
        this.img = img;
    }

    public TransformedImage filter() {
        BufferedImage src = img.getSourceImage();
        int h = src.getHeight();
        int w = src.getWidth();

        TransformedImage after = new TransformedImage(img);
        BufferedImage afterBI = after.getSourceImage();
        var raster = src.getRaster();
        var afterRaster = afterBI.getRaster();
        var componentCount = src.getColorModel().getNumComponents();
        // Temporary array to hold pixel components

        IntStream.range(0, h).parallel().forEach(y -> {
            int[] pixel = new int[componentCount];
            for (int x = 0; x < w; ++x) {
                raster.getPixel(x,y,pixel);
                pixel[0] = 255 - pixel[0];
                pixel[1] = 255 - pixel[1];
                pixel[2] = 255 - pixel[2];
                afterRaster.setPixel(x, y, pixel);
            }
        });

        return after;
    }

    public static void main(String[] args) throws IOException {
        TransformedImage src = new TransformedImage( ImageIO.read(new FileInputStream("src/test/resources/mandrill.png")) );
        FilterInvert f = new FilterInvert(src);
        ResizableImagePanel.showImage(f.filter().getSourceImage(), "Filter_Invert" );
    }
}