package com.marginallyclever.makelangelo.donatelloimpl.nodes;

import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;
import com.marginallyclever.nodegraphcore.port.Input;
import com.marginallyclever.nodegraphcore.port.Output;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.Arrays;

/**
 * Use a bitmap intensity to control the flow field.
 */
public class FlowField extends Node {
    private final Input<BufferedImage> inputImage = new Input<>("inputImage",BufferedImage.class, new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB));
    private final Input<Number> spacingValue = new Input<>("spacing",Number.class,10);
    private final Input<Number> stepValue = new Input<>("step size",Number.class,3);
    private final Input<Number> numStepsValue = new Input<>("step count",Number.class,3);
    private final Output<Turtle> result = new Output<>("result",Turtle.class,new Turtle());

    public FlowField() {
        super("FlowField");
        addVariable(inputImage);
        addVariable(spacingValue);
        addVariable(stepValue);
        addVariable(numStepsValue);
        addVariable(result);
    }

    @Override
    public void update() {
        var img = inputImage.getValue();
        ColorModel cm = img.getColorModel();
        var raster = img.getRaster();
        int numComponents = cm.getNumComponents();
        int [] pixel = new int[numComponents];

        int spacing = Math.max(1,spacingValue.getValue().intValue());
        int step = Math.max(1,stepValue.getValue().intValue());
        int numSteps = Math.max(1,numStepsValue.getValue().intValue());
        var turtle = new Turtle();

        // move in a grid over the image and generate a flow field
        var w = img.getWidth();
        var h = img.getHeight();
        var size = w*h;
        setComplete(0);
        for (int y = 0; y < h; y+=spacing) {
            for (int x = 0; x < w; x+=spacing) {
                drawFlowField(raster, numComponents, pixel, step, turtle, x, y,numSteps);
            }
            setComplete(100*y/h);
        }

        setComplete(100);
        result.send(turtle);
    }

    private void drawFlowField(WritableRaster raster, int numComponents, int[] pixel, int step, Turtle turtle, int x, int y, int numSteps) {
        turtle.jumpTo(x, y);
        for(int i=0;i<numSteps;++i) {
            drawFlowFieldStep(raster, numComponents, pixel, step, turtle);
        }
    }

    private void drawFlowFieldStep(WritableRaster raster, int numComponents, int[] pixel, int step, Turtle turtle) {
        int x = (int)turtle.getX();
        int y = (int)turtle.getY();
        if(!raster.getBounds().contains(x,y)) return;
        raster.getPixel(x, y, pixel);
        // get intensity of image at x,y
        double intensity = (double) Arrays.stream(pixel).sum() / numComponents;
        // get angle of flow field as intensity * 180 / 255
        double radians = intensity * 180.0/255.0;
        turtle.setAngle(radians);
        turtle.forward(step);
    }
}
