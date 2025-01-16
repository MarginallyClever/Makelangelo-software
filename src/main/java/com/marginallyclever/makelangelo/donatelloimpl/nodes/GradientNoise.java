package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.convenience.noise.Noise;
import com.marginallyclever.convenience.noise.NoiseFactory;
import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.nodegraphcore.port.Input;
import com.marginallyclever.nodegraphcore.port.Output;

import java.awt.image.BufferedImage;

public class GradientNoise extends Node {
    private final Input<Number> width = new Input<>("width", Number.class, 256);
    private final Input<Number> height = new Input<>("height", Number.class, 256);
    private final Input<Number> style = new Input<>("style", Number.class, 0);
    private final Input<Number> seed = new Input<>("seed", Number.class, 0);
    private final Input<Number> scaleX = new Input<>("scale x", Number.class, 0.05);
    private final Input<Number> scaleY = new Input<>("scale y", Number.class, 0.05);
    private final Input<Number> translateX = new Input<>("translate x", Number.class, 0);
    private final Input<Number> translateY = new Input<>("translate y", Number.class, 0);
    private final Output<BufferedImage> output = new Output<>("output", BufferedImage.class, new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB));

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
                int c = (int)(noise.noise(x*sx+tx,y*sy+ty) * 255.0);
                pixels[0] = c;
                pixels[1] = c;
                pixels[2] = c;
                raster.setPixel(x,y,pixels);
            }
        }

        output.send(img);
    }
}
