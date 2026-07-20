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
     * @param mode the mode to scale the image
     */
    public static void scaleImage(TransformedImage sourceImage, Paper paper, int mode) {
        Rectangle2D.Double rect = paper.getMarginRectangle();
        double width  = rect.getWidth();
        double height = rect.getHeight();

        boolean test;
        if (mode == 0) {
            test = width < height;  // fill paper
        } else {
            test = width > height;  // fit paper
        }

        float f;
        if( test ) {
            f = (float)( width / (double)sourceImage.getSourceImage().getWidth() );
        } else {
            f = (float)( height / (double)sourceImage.getSourceImage().getHeight() );
        }
        sourceImage.setScale(f,-f);
    }
}
