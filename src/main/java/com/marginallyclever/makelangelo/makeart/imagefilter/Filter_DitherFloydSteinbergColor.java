package com.marginallyclever.makelangelo.makeart.imagefilter;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.ResizableImagePanel;
import com.marginallyclever.makelangelo.makeart.TransformedImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;


/**
 * Floyd/Steinberg dithering
 * See <a href="http://stackoverflow.com/questions/5940188/how-to-convert-a-24-bit-png-to-3-bit-png-using-floyd-steinberg-dithering">http://stackoverflow.com/questions/5940188/how-to-convert-a-24-bit-png-to-3-bit-png-using-floyd-steinberg-dithering</a>
 *
 * @author Dan Royer
 */
public class Filter_DitherFloydSteinbergColor extends ImageFilter {
    public final ColorRGB[] palette;

    public Filter_DitherFloydSteinbergColor() {
        palette = new ColorRGB[]{
                new ColorRGB(0, 0, 0),
                new ColorRGB(0, 0, 255),
                new ColorRGB(0, 255, 0),
                new ColorRGB(0, 255, 255),
                new ColorRGB(255, 0, 0),
                new ColorRGB(255, 0, 255),
                new ColorRGB(255, 255, 0),
                new ColorRGB(255, 255, 255),
        };
    }

    public TransformedImage filter(TransformedImage img) {
        BufferedImage src = img.getSourceImage();
        int h = src.getHeight();
        int w = src.getWidth();

        TransformedImage after = new TransformedImage(img);
        BufferedImage afterBI = after.getSourceImage();
        ColorRGB [][] d = new ColorRGB[h][w];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                d[y][x] = new ColorRGB(src.getRGB(x, y));
            }
        }

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                ColorRGB oldColor = d[y][x];
                ColorRGB newColor = findClosestPaletteColor(oldColor);
                afterBI.setRGB(x, y, newColor.toInt());

                ColorRGB err = oldColor.sub(newColor);

                if (x + 1 < w              ) d[y    ][x + 1] = d[y    ][x + 1].add(err.mul(7. / 16));
                if (x - 1 >= 0 && y + 1 < h) d[y + 1][x - 1] = d[y + 1][x - 1].add(err.mul(3. / 16));
                if (y + 1 < h              ) d[y + 1][x    ] = d[y + 1][x    ].add(err.mul(5. / 16));
                if (x + 1 < w && y + 1 < h ) d[y + 1][x + 1] = d[y + 1][x + 1].add(err.mul(1. / 16));
            }
        }

        return after;
    }

    private ColorRGB findClosestPaletteColor(ColorRGB c) {
        ColorRGB closest = palette[0];
        double best = closest.diffSquared(c);

        for (ColorRGB n : palette) {
            double next = n.diffSquared(c);
            if (next < best) {
                closest = n;
                best = next;
            }
        }

        return closest;
    }

    public static void main(String[] args) throws IOException {
        TransformedImage src = new TransformedImage(ImageIO.read(new FileInputStream("src/test/resources/Lenna.png")));
        Filter_DitherFloydSteinbergColor f = new Filter_DitherFloydSteinbergColor();
        TransformedImage dest = f.filter(src);
        ResizableImagePanel.showImage(dest.getSourceImage(), "Filter_DitherFloydSteinbergColor");
    }
}