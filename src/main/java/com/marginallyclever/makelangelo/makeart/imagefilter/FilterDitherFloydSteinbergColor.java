package com.marginallyclever.makelangelo.makeart.imagefilter;

import com.marginallyclever.convenience.ResizableImagePanel;
import com.marginallyclever.makelangelo.makeart.TransformedImage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;


/**
 * Floyd/Steinberg dithering
 * See <a href="http://stackoverflow.com/questions/5940188/how-to-convert-a-24-bit-png-to-3-bit-png-using-floyd-steinberg-dithering">http://stackoverflow.com/questions/5940188/how-to-convert-a-24-bit-png-to-3-bit-png-using-floyd-steinberg-dithering</a>
 *
 * @author Dan Royer
 */
public class FilterDitherFloydSteinbergColor extends ImageFilter {
    public final Color[] palette;
    private final TransformedImage img;

    public FilterDitherFloydSteinbergColor(TransformedImage img) {
        super();
        this.img = img;
        palette = new Color[] {
                Color.BLACK,
                Color.BLUE,
                Color.GREEN,
                Color.CYAN,
                Color.RED,
                Color.MAGENTA,
                Color.YELLOW,
                Color.WHITE,
        };
    }

    @Override
    public TransformedImage filter() {
        BufferedImage src = img.getSourceImage();
        int h = src.getHeight();
        int w = src.getWidth();

        TransformedImage after = new TransformedImage(img);
        BufferedImage afterBI = after.getSourceImage();
        Color [][] d = new Color[h][w];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                d[y][x] = new Color(src.getRGB(x, y));
            }
        }

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Color oldColor = d[y][x];
                Color newColor = findClosestPaletteColor(oldColor);
                afterBI.setRGB(x, y, newColor.hashCode());

                Color err = new Color(
                        oldColor.getRed() - newColor.getRed(),
                        oldColor.getGreen() - newColor.getGreen(),
                        oldColor.getBlue() - newColor.getBlue()
                );

                if (x + 1 < w              ) d[y    ][x + 1] = addColors(d[y    ][x + 1], scaleColor(err, 7.0 / 16.0));
                if (x - 1 >= 0 && y + 1 < h) d[y + 1][x - 1] = addColors(d[y + 1][x - 1], scaleColor(err, 3.0 / 16.0));
                if (y + 1 < h              ) d[y + 1][x    ] = addColors(d[y + 1][x    ], scaleColor(err, 5.0 / 16.0));
                if (x + 1 < w && y + 1 < h ) d[y + 1][x + 1] = addColors(d[y + 1][x + 1], scaleColor(err, 1.0 / 16.0));
            }
        }

        return after;
    }

    private Color addColors(Color a, Color b) {
        return new Color(
                Math.max(0,Math.min(255, a.getRed() + b.getRed())),
                Math.max(0,Math.min(255, a.getGreen() + b.getGreen())),
                Math.max(0,Math.min(255, a.getBlue() + b.getBlue()))
        );
    }

    private Color scaleColor(Color c, double scale) {
        return new Color(
                (int) (c.getRed() * scale),
                (int) (c.getGreen() * scale),
                (int) (c.getBlue() * scale)
        );
    }

    private Color findClosestPaletteColor(Color c) {
        Color closest = palette[0];
        double best = diffSquared(closest,c);

        for (Color n : palette) {
            double next = diffSquared(n,c);
            if (next < best) {
                closest = n;
                best = next;
            }
        }

        return closest;
    }

    private int diffSquared(Color a, Color b) {
        return Math.abs(a.getRed() - b.getRed())
                + Math.abs(a.getGreen() - b.getGreen())
                + Math.abs(a.getBlue() - b.getBlue());
    }

    public static void main(String[] args) throws IOException {
        TransformedImage src = new TransformedImage(ImageIO.read(new FileInputStream("src/test/resources/mandrill.png")));
        FilterDitherFloydSteinbergColor f = new FilterDitherFloydSteinbergColor(src);
        TransformedImage dest = f.filter();
        ResizableImagePanel.showImage(dest.getSourceImage(), "Filter_DitherFloydSteinbergColor");
    }
}