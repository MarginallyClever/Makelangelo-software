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
        Filter_GaussianBlur a = new Filter_GaussianBlur(src,2);
        TransformedImage a2 = a.filter();
        ImageIO.write(a2.getSourceImage(), "jpg", new java.io.File("src/test/resources/chicken-a.jpg"));

        Filter_GaussianBlur b = new Filter_GaussianBlur(src,4);
        TransformedImage b2 = b.filter();
        ImageIO.write(b2.getSourceImage(), "jpg", new java.io.File("src/test/resources/chicken-b.jpg"));

        Filter_Scale scale = new Filter_Scale(b2,0.85);
        TransformedImage afterScale = scale.filter();
        ImageIO.write(afterScale.getSourceImage(), "jpg", new java.io.File("src/test/resources/chicken-c.jpg"));

        Filter_Difference diff = new Filter_Difference(a2,afterScale);
        TransformedImage afterDiff = diff.filter();
        ImageIO.write(afterDiff.getSourceImage(), "jpg", new java.io.File("src/test/resources/chicken-d.jpg"));

        Filter_Threshold threshold = new Filter_Threshold(afterDiff,8);
        TransformedImage afterThreshold = threshold.filter();

        ImageIO.write(afterThreshold.getSourceImage(), "jpg", new java.io.File("src/test/resources/chicken-difference.jpg"));
    }

}
