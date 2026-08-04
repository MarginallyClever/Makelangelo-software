package com.marginallyclever.makelangelo.makeart.turtlegenerator;

import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.paper.Paper;
import org.junit.jupiter.api.Test;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TurtleGeneratorHelperTest {
    @Test
    public void scalesImageToFitPaperMargins() {
        TransformedImage image = new TransformedImage(new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB));
        Paper paper = new Paper();
        Rectangle2D.Double margin = paper.getMarginRectangle();

        TurtleGeneratorHelper.scaleImage(image, paper, 0);

        float expected = (float)Math.min(margin.getWidth() / 200.0, margin.getHeight() / 100.0);
        assertEquals(expected, image.getScaleX());
        assertEquals(-expected, image.getScaleY());
    }

    @Test
    public void scalesImageToFillPaperMargins() {
        TransformedImage image = new TransformedImage(new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB));
        Paper paper = new Paper();
        Rectangle2D.Double margin = paper.getMarginRectangle();

        TurtleGeneratorHelper.scaleImage(image, paper, 1);

        float expected = (float)Math.max(margin.getWidth() / 200.0, margin.getHeight() / 100.0);
        assertEquals(expected, image.getScaleX());
        assertEquals(-expected, image.getScaleY());
    }
}
