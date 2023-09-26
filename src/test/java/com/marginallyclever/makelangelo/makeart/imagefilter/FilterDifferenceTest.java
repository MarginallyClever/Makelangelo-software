package com.marginallyclever.makelangelo.makeart.imagefilter;

import com.marginallyclever.makelangelo.makeart.TransformedImage;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.FileInputStream;
import java.io.IOException;

public class FilterDifferenceTest {
    @Test
    public void test() throws IOException {
        TransformedImage src = new TransformedImage( ImageIO.read(new FileInputStream("src/test/resources/chicken.png")) );
        FilterDesaturate desaturate = new FilterDesaturate(src);

        FilterGaussianBlur a = new FilterGaussianBlur(desaturate.filter(),2);
        TransformedImage a2 = a.filter();
        ImageIO.write(a2.getSourceImage(), "jpg", new java.io.File("src/test/resources/chicken-a.jpg"));

        FilterGaussianBlur b = new FilterGaussianBlur(desaturate.filter(),4);
        TransformedImage b2 = b.filter();
        ImageIO.write(b2.getSourceImage(), "jpg", new java.io.File("src/test/resources/chicken-b.jpg"));

        FilterScale scale = new FilterScale(b2,0.95);
        TransformedImage afterScale = scale.filter();
        ImageIO.write(afterScale.getSourceImage(), "jpg", new java.io.File("src/test/resources/chicken-c.jpg"));

        FilterDifference diff = new FilterDifference(a2,afterScale);
        TransformedImage afterDiff = diff.filter();
        ImageIO.write(afterDiff.getSourceImage(), "jpg", new java.io.File("src/test/resources/chicken-d.jpg"));

        FilterThreshold threshold = new FilterThreshold(afterDiff,8);
        TransformedImage afterThreshold = threshold.filter();

        ImageIO.write(afterThreshold.getSourceImage(), "jpg", new java.io.File("src/test/resources/chicken-difference.jpg"));
    }

}
