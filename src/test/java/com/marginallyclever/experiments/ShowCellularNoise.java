package com.marginallyclever.experiments;

import com.marginallyclever.convenience.noise.CellularNoise;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class ShowCellularNoise {
    public static void main(String[] args) {
        // create a jframe, put a panel in it, and show a cellular noise pattern
        CellularNoise noise = new CellularNoise();
        noise.setSeed(0);

        int width = 512;
        int height = 512;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int c = image.getColorModel().getNumComponents();
        int [] pixel = new int[c];
        var r = image.getRaster();

        double scale = 0.1;
        double px = 0;
        double py = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                var v2 = (noise.noise(px+x*scale, py+y*scale) + 1.0) / 2.0;
                int v = (int)(255 * Math.max(0.0,Math.min(1.0, v2 )));
                Arrays.fill(pixel, v);
                r.setPixel(x, y, pixel);
            }
        }

        JFrame frame = new JFrame("Cellular Noise");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setSize(width, height);
        frame.add(new JLabel(new ImageIcon(image)));
        frame.setVisible(true);
    }
}
