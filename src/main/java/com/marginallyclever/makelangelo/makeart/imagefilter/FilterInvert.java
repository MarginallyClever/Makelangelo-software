package com.marginallyclever.makelangelo.makeart.imagefilter;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.ResizableImagePanel;
import com.marginallyclever.makelangelo.makeart.TransformedImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;


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
        int x, y;

        TransformedImage after = new TransformedImage(img);
        BufferedImage afterBI = after.getSourceImage();

        for (y = 0; y < h; ++y) {
            for (x = 0; x < w; ++x) {
                ColorRGB color = new ColorRGB(src.getRGB(x, y));
                color.red = 255 - color.red;
                color.green = 255 - color.green;
                color.blue = 255 - color.blue;
                afterBI.setRGB(x, y, color.toInt());
            }
        }

        return after;
    }

    public static void main(String[] args) throws IOException {
        TransformedImage src = new TransformedImage( ImageIO.read(new FileInputStream("src/test/resources/mandrill.png")) );
        FilterInvert f = new FilterInvert(src);
        ResizableImagePanel.showImage(f.filter().getSourceImage(), "Filter_Invert" );
    }
}