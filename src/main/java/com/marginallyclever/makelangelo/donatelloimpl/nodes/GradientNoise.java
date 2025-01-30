package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.convenience.noise.Noise;
import com.marginallyclever.convenience.noise.NoiseFactory;
import com.marginallyclever.makelangelo.donatelloimpl.ports.InputDouble;
import com.marginallyclever.makelangelo.donatelloimpl.ports.InputInt;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputImage;
import com.marginallyclever.nodegraphcore.Node;

import java.awt.image.BufferedImage;

/**
 * Generate a 2D gradient noise image.
 */
public class GradientNoise extends Node {
    private final InputInt width = new InputInt("width", 256);
    private final InputInt height = new InputInt("height", 256);
    private final InputInt style = new InputInt("style", 0);
    private final InputInt seed = new InputInt("seed", 0);
    private final InputDouble scaleX = new InputDouble("scale x", 0.05d);
    private final InputDouble scaleY = new InputDouble("scale y", 0.05d);
    private final InputDouble translateX = new InputDouble("translate x", 0d);
    private final InputDouble translateY = new InputDouble("translate y", 0d);
    private final OutputImage output = new OutputImage("output");

    public GradientNoise() {
        super("GradientNoise");
        addVariable(width);
        addVariable(height);
        addVariable(style);
        addVariable(seed);
        addVariable(scaleX);
        addVariable(scaleY);
        addVariable(translateX);
        addVariable(translateY);
        addVariable(output);
    }

    @Override
    public void update() {
        int w = Math.max(1,width.getValue().intValue());
        int h = Math.max(1,height.getValue().intValue());
        int noiseStyle = Math.min(NoiseFactory.getNames().length-1,Math.max(0,style.getValue().intValue()));
        double tx = translateX.getValue().doubleValue();
        double ty = translateY.getValue().doubleValue();
        double sx = scaleX.getValue().doubleValue();
        double sy = scaleY.getValue().doubleValue();

        Noise noise = NoiseFactory.getNoise(noiseStyle);
        assert(noise!=null);

        BufferedImage img = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
        var raster = img.getRaster();
        var count = img.getColorModel().getNumComponents();
        // pixel buffer
        int [] pixels = new int[count];
        pixels[3] = 255;

        for(int y=0;y<h;++y) {
            for(int x=0;x<w;++x) {
                double n = noise.noise(x*sx+tx,y*sy+ty);
                int c = (int)Math.max(0,Math.min(255, (1.0+n) * 255.0/2.0));
                pixels[0] = c;
                pixels[1] = c;
                pixels[2] = c;
                raster.setPixel(x,y,pixels);
            }
        }

        output.send(img);
    }
}
