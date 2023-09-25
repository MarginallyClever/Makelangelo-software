package com.marginallyclever.makelangelo.makeart.imagefilter;

import com.marginallyclever.makelangelo.makeart.TransformedImage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;

public class Filter_InvertTest {
    // pixel-to-pixel comparison
    public static void compareBitmaps(BufferedImage a,BufferedImage b) {
        int h = a.getHeight();
        int w = a.getWidth();
        int x, y;

        for (y = 0; y < h; ++y) {
            for (x = 0; x < w; ++x) {
                Assertions.assertEquals(a.getRGB(x, y), b.getRGB(x, y));
            }
        }
    }

    @Test
    public void test() throws IOException {
        TransformedImage src = new TransformedImage( ImageIO.read(new FileInputStream("src/test/resources/mandrill.png")) );
        Filter_Invert f = new Filter_Invert();
        TransformedImage dest = f.filter(src);
        //ImageIO.write(dest.getSourceImage(), "png", new java.io.File("src/test/resources/mandrill-inverse.png"));

        TransformedImage compare = new TransformedImage( ImageIO.read(new FileInputStream("src/test/resources/mandrill-inverse.png")) );
        compareBitmaps(compare.getSourceImage(),dest.getSourceImage());
    }
}
