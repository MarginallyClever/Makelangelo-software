package com.marginallyclever.makelangelo.makeart.imagefilter;

import com.marginallyclever.makelangelo.makeart.TransformedImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Converts an image four color channels: Cyan, Magenta, Yellow, and Black. *
 * @author Dan Royer
 */
public class FilterCMYK extends ImageFilter {
    private static final Logger logger = LoggerFactory.getLogger(FilterCMYK.class);
    protected static double levels = 2;

    protected TransformedImage img;
    protected TransformedImage channelCyan;
    protected TransformedImage channelMagenta;
    protected TransformedImage channelYellow;
    protected TransformedImage channelBlack;


    /**
     * @param img the <code>java.awt.image.BufferedImage</code> this filter is using as source material.
     */
    public FilterCMYK(TransformedImage img) {
        super();
        this.img = img;
    }


    public TransformedImage getC() {
        return channelCyan;
    }

    public TransformedImage getM() {
        return channelMagenta;
    }

    public TransformedImage getY() {
        return channelYellow;
    }

    public TransformedImage getK() {
        return channelBlack;
    }


    /**
     * Converts an image to 4 greyscale images, one for each channel of CMYK.
     * See <a href="http://www.rapidtables.com/convert/color/rgb-to-cmyk.htm">RGB to CMYK</a>
     * @return the original image.
     */
    @Override
    public TransformedImage filter() {
        BufferedImage bi = img.getSourceImage();
        int h = bi.getHeight();
        int w = bi.getWidth();

        channelCyan = new TransformedImage(img);
        channelMagenta = new TransformedImage(img);
        channelYellow = new TransformedImage(img);
        channelBlack = new TransformedImage(img);

        BufferedImage cc = channelCyan.getSourceImage();
        BufferedImage cm = channelMagenta.getSourceImage();
        BufferedImage cy = channelYellow.getSourceImage();
        BufferedImage ck = channelBlack.getSourceImage();

        for (int py = 0; py < h; ++py) {
            for (int px = 0; px < w; ++px) {
                int pixel = bi.getRGB(px, py);
                //double a = 255-((pixel>>24) & 0xff);
                double r = 1.0 - (double) ((pixel >> 16) & 0xff) / 255.0;
                double g = 1.0 - (double) ((pixel >> 8) & 0xff) / 255.0;
                double b = 1.0 - (double) ((pixel) & 0xff) / 255.0;
                // now convert to cmyk
                double k = Math.min(Math.min(r, g), b);   // should be Math.max(Math.max(r,g),b) but colors are inverted.
                double ik = 1.0 - k;

                double c, m, y;
                if(ik<1.0/255.0) {
                	c=m=y=0;
                } else {
                    c = (r - k) / ik;
                    m = (g - k) / ik;
                    y = (b - k) / ik;
                }
                cc.setRGB(px, py, ImageFilter.encode32bit(255 - (int) (c * 255.0)));
                cm.setRGB(px, py, ImageFilter.encode32bit(255 - (int) (m * 255.0)));
                cy.setRGB(px, py, ImageFilter.encode32bit(255 - (int) (y * 255.0)));
                ck.setRGB(px, py, ImageFilter.encode32bit(255 - (int) (k * 255.0)));
            }
        }

        return img;
    }

    /**
     * An experimental black &#38; white converter that doesn't just greyscale to 4 levels, it also tries to divide by histogram frequency.
     * Didn't look good so I left it for the lulz.
     *
     * @param img the <code>java.awt.image.BufferedImage</code> this filter is to process.
     * @return the altered image
     */
    @Deprecated
    public TransformedImage processViaHistogram(TransformedImage img) {
        int h = img.getSourceImage().getHeight();
        int w = img.getSourceImage().getWidth();

        int x, y, i;

        double[] histogram = new double[256];

        for (i = 0; i < 256; ++i) {
            histogram[i] = 0;
        }

        for (y = 0; y < h; ++y) {
            for (x = 0; x < w; ++x) {
                i = decode32bit(img.getSourceImage().getRGB(x, y));
                ++histogram[i];
            }
        }

        double histogram_area = 0;
        //logger.debug("histogram:");
        for (i = 1; i < 255; ++i) {
            logger.debug("{}={}", i, histogram[i]);
            histogram_area += histogram[i];
        }
        double histogram_zone = histogram_area / (double) levels;
        //logger.debug("histogram area: "+histogram_area);
        //logger.debug("histogram zone: "+histogram_zone);

        double histogram_sum = 0;
        x = 0;
        y = 0;
        for (i = 1; i < 255; ++i) {
            histogram_sum += histogram[i];
            //logger.debug("mapping "+i+" to "+x);
            if (histogram_sum > histogram_zone) {
                //logger.debug("level up at "+i+" "+histogram_sum+" vs "+histogram_zone);
                histogram_sum -= histogram_zone;
                x += (int) (256.0 / (double) levels);
                ++y;
            }
            histogram[i] = x;
        }

        //logger.debug("y="+y+" x="+x);
        int pixel, b;

        for (y = 0; y < h; ++y) {
            for (x = 0; x < w; ++x) {
                pixel = decode32bit(img.getSourceImage().getRGB(x, y));
                b = (int) histogram[pixel];
                img.getSourceImage().setRGB(x, y, ImageFilter.encode32bit(b));
            }
        }

        return img;
    }

    public static void main(String[] args) throws IOException {
        TransformedImage src = new TransformedImage( ImageIO.read(new FileInputStream("src/test/resources/mandrill.png")) );
        FilterCMYK f = new FilterCMYK(src);
        f.filter();

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Original", new JLabel(new ImageIcon(src.getSourceImage())));
        tabs.addTab("Cyan", new JLabel(new ImageIcon(f.channelCyan.getSourceImage())));
        tabs.addTab("Magenta", new JLabel(new ImageIcon(f.channelMagenta.getSourceImage())));
        tabs.addTab("Yellow", new JLabel(new ImageIcon(f.channelYellow.getSourceImage())));
        tabs.addTab("Black", new JLabel(new ImageIcon(f.channelBlack.getSourceImage())));

        JFrame frame = new JFrame("Filter_CMYK");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 500);
        frame.add(tabs);
        frame.setVisible(true);
    }
}