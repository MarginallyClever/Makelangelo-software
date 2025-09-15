package com.marginallyclever.makelangelo.makeart.imagefilter;

import com.marginallyclever.makelangelo.makeart.TransformedImage;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.FileInputStream;
import java.io.IOException;

public class FilterSobelTest {
    @Test
    public void testSobel() throws IOException {
        final String SRC_IMAGE = "src/test/resources/com/marginallyclever/makelangelo/makeart/imagefilter/mandrill.png";
        final String RESULT_IMAGE = "src/test/resources/com/marginallyclever/makelangelo/makeart/imagefilter/mandrill-sobel.png";
        TransformedImage src = new TransformedImage( ImageIO.read(new FileInputStream(SRC_IMAGE)) );
        FilterSobel filter = new FilterSobel(src);
        TransformedImage dest = filter.filter();

        // to generate the test image
        //ImageIO.write(dest.getSourceImage(), "png", new java.io.File(RESULT_IMAGE));

        TransformedImage compare = new TransformedImage( ImageIO.read(new FileInputStream(RESULT_IMAGE)) );
        FilterInvertTest.compareBitmaps(compare.getSourceImage(),dest.getSourceImage());
    }
}
