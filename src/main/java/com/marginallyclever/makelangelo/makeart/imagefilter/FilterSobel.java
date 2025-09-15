package com.marginallyclever.makelangelo.makeart.imagefilter;

import com.marginallyclever.makelangelo.makeart.TransformedImage;

import javax.vecmath.Vector2d;
import java.awt.image.BufferedImage;

/**
 * Applies a Sobel filter to an image to detect edges.
 * See <a href="https://en.wikipedia.org/wiki/Sobel_operator">Sobel Operator</a>
 * @author Dan Royer
 */
public class FilterSobel extends ImageFilter {
    private final TransformedImage img;

    public FilterSobel(TransformedImage img) {
        super();
        this.img = img;
    }

    @Override
    public TransformedImage filter() {
        double [][] Gy = {
                {-1, -2, -1},
                { 0,  0,  0},
                { 1,  2,  1}
        };
        double [][] Gx = {
                {-1,  0, 1},
                {-2,  0, 2},
                {-1,  0, 1}
        };

        TransformedImage result = new TransformedImage(img);
        BufferedImage before = img.getSourceImage();
        BufferedImage after = result.getSourceImage();
        Vector2d sumR = new Vector2d();
        Vector2d sumG = new Vector2d();
        Vector2d sumB = new Vector2d();

        // convolve before into result using the Gy matrix
        for(int y=0;y<before.getHeight();y++) {
            for(int x=0;x<before.getWidth();x++) {
                sumR.set(0,0);
                sumG.set(0,0);
                sumB.set(0,0);

                for(int j=-1;j<=1;j++) {
                    for(int i=-1;i<=1;i++) {
                        int sampleX = Math.min(Math.max(x+i,0), before.getWidth()-1);
                        int sampleY = Math.min(Math.max(y+j,0), before.getHeight()-1);
                        int rgb = before.getRGB(sampleX, sampleY);
                        int red   = ((rgb >> 16) & 0xff);
                        int green = ((rgb >>  8) & 0xff);
                        int blue  = ((rgb      ) & 0xff);
                        sumR.x += red * Gx[j+1][i+1];
                        sumR.y += red * Gy[j+1][i+1];
                        sumG.x += green * Gx[j+1][i+1];
                        sumG.y += green * Gy[j+1][i+1];
                        sumB.x += blue * Gx[j+1][i+1];
                        sumB.y += blue * Gy[j+1][i+1];
                    }
                }

                int magR = (int)Math.clamp(sumR.length(), 0, 255);
                int magG = (int)Math.clamp(sumG.length(), 0, 255);
                int magB = (int)Math.clamp(sumB.length(), 0, 255);
                int edgeColor = (0xff << 24) | (magR << 16) | (magG << 8) | magB;
                after.setRGB(x, y, edgeColor);
            }
        }

        return result;
    }
}
