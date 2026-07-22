package com.marginallyclever.makelangelo.makeart.turtletool;

import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

public class LineWeightByImageIntensityTest {
    @Test
    public void testRun() {
        LineWeightByImageIntensity tool = new LineWeightByImageIntensity();
        Turtle input = new Turtle();
        input.jumpTo(0, 0);
        input.moveTo(10, 10);

        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        TransformedImage image = new TransformedImage(img);

        // Just verify it runs without exception and returns a non-null turtle.
        // The actual logic is in ThickenLinesByIntensity which should be tested elsewhere.
        Turtle result = tool.run(input, image, 1.0, 3.0, 0.8);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.countPoints() > 0);
    }
}
