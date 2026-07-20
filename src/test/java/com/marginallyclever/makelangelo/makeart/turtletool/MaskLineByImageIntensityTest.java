package com.marginallyclever.makelangelo.makeart.turtletool;

import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

public class MaskLineByImageIntensityTest {
    @Test
    public void testKeepAll() {
        MaskLineByImageIntensity tool = new MaskLineByImageIntensity();
        Turtle input = new Turtle();
        input.jumpTo(0, 0);
        input.moveTo(10, 10);
        
        // Black image
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        TransformedImage mask = new TransformedImage(img);
        
        // threshold 0.5, monteCarlo 0 -> everything < 0.5 is masked (black is 0)
        Turtle result = tool.run(input, mask, 0.5, 0.0, 1234);
        Assertions.assertEquals(2, result.countPoints());
    }

    @Test
    public void testMaskAll() {
        MaskLineByImageIntensity tool = new MaskLineByImageIntensity();
        Turtle input = new Turtle();
        input.jumpTo(0, 0);
        input.moveTo(10, 10);
        
        // White image
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 100, 100);
        g.dispose();
        TransformedImage mask = new TransformedImage(img);
        
        // threshold 0.5 -> everything >= 0.5 is kept (white is 1.0)
        Turtle result = tool.run(input, mask, 0.5, 0.0, 1234);
        Assertions.assertEquals(0, result.countPoints());
    }
}
