package com.marginallyclever.makelangelo.makeart.imagefilter;

import com.marginallyclever.makelangelo.makeart.TransformedImage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.FileInputStream;
import java.io.IOException;

public class FilterGradientTest {
    @Test
    @Disabled
    public void testFilterGradient() throws IOException {
        // load src/test/resources/peppers.jpg
        TransformedImage img = new TransformedImage(ImageIO.read(new FileInputStream("src/test/resources/peppers.jpg")));
        FilterGradient filter = new FilterGradient(img);
        var result = filter.filter();
        // save the result to peppers-gradient.png
        ImageIO.write(result.getSourceImage(), "png", new java.io.File("src/test/resources/peppers-gradient.png"));
    }
}
