package com.marginallyclever.makelangelo.makeart.turtlegenerator;

import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.paper.Paper;

import java.awt.geom.Rectangle2D;

public class TurtleGeneratorHelper {

    /**
     * mode 0 = fill paper
     * mode 1 = fit paper
     * @param sourceImage the image to scale
     * @param paper the paper reference by which to scale the image.
     * @param mode the mode to scale the image.  mode 0: fill the paper.  mode 1: fit the image inside the margins.
     */
    public static void scaleImage(TransformedImage sourceImage, Paper paper, int mode) {
        Rectangle2D.Double rect = paper.getMarginRectangle();
        double width  = rect.getWidth();
        double height = rect.getHeight();

        double imageWidth = sourceImage.getSourceImage().getWidth();
        double imageHeight = sourceImage.getSourceImage().getHeight();
        double widthRatio = width / imageWidth;
        double heightRatio = height / imageHeight;
        double f = (mode == 1)
                ? Math.max(widthRatio, heightRatio)
                : Math.min(widthRatio, heightRatio);

        sourceImage.setScale((float)f, -(float)f);
    }
}
