package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle;

import com.marginallyclever.donatello.ports.InputDouble;
import com.marginallyclever.donatello.ports.InputImage;
import com.marginallyclever.donatello.ports.InputInt;
import com.marginallyclever.makelangelo.donatelloimpl.ports.OutputTurtle;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.nodegraphcore.Node;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.Arrays;

/**
 * Use a bitmap intensity to control the flow field.
 */
public class FlowField extends Node {
    private final InputImage inputImage = new InputImage("inputImage");
    private final InputDouble spacingValue = new InputDouble("spacing", 10d);
    private final InputInt stepValue = new InputInt("step size", 3);
    private final InputInt numStepsValue = new InputInt("step count", 3);
    private final InputDouble startAngle = new InputDouble("start angle", 0d);
    private final OutputTurtle result = new OutputTurtle("result");

    public FlowField() {
        super("FlowField");
        addPort(inputImage);
        addPort(spacingValue);
        addPort(stepValue);
        addPort(numStepsValue);
        addPort(startAngle);
        addPort(result);
    }

    @Override
    public void update() {
        var img = inputImage.getValue();
        var turtle = new Turtle();
        int step = Math.max(1,stepValue.getValue());
        int numSteps = Math.max(1,numStepsValue.getValue());
        double angle = startAngle.getValue();
        FlowDrawer drawer = new FlowDrawer(img, turtle, step, angle);

        // move in a grid over the image and generate a flow field
        var spacing = Math.max(1,spacingValue.getValue());
        var w = img.getWidth();
        var h = img.getHeight();

        setComplete(0);
        for (double y = 0; y <= h; y+=spacing) {
            for (double x = 0; x <= w; x+=spacing) {
                drawer.draw((int)x, (int)y, numSteps);
            }
            setComplete((int)(100*y/h));
        }

        setComplete(100);
        result.setValue(turtle);
    }

    /**
     * Uses a {@link Turtle} to draws one flow line of a flow field based on the intensity of a {@link BufferedImage}.
     */
    static class FlowDrawer {
        private final Turtle turtle;
        private final int [] pixel;
        private final double strideLength;
        private final WritableRaster raster;
        private final double startDegrees;

        /**
         * @param img the image to read
         * @param turtle the turtle to draw with
         * @param strideLength the distance to move each step
         * @param startDegrees the starting angle offset
         */
        public FlowDrawer(BufferedImage img, Turtle turtle, double strideLength, double startDegrees) {
            this.turtle = turtle;
            this.strideLength = strideLength;
            this.startDegrees = startDegrees;

            ColorModel cm = img.getColorModel();
            raster = img.getRaster();

            int numComponents = cm.getNumComponents();
            pixel = new int[numComponents];
        }

        /**
         * Draw a flow line starting at x,y
         * @param x the x coordinate
         * @param y the y coordinate
         * @param numSteps the number of steps to draw
         */
        public void draw(double x, double y, int numSteps) {
            turtle.jumpTo(x, y);
            for(int i=0;i<numSteps;++i) {
                drawStep();
            }
        }

        private void drawStep() {
            int x = (int) turtle.getX();
            int y = (int) turtle.getY();
            if (!raster.getBounds().contains(x, y)) return;
            raster.getPixel(x, y, pixel);
            // get intensity of image at x,y
            double intensity = (double) Arrays.stream(pixel).sum() / pixel.length;
            // get angle of flow field as intensity * 180 / 255
            double degrees = startDegrees + intensity * 180.0 / 255.0;
            turtle.setAngle(degrees);
            var h = turtle.getHeading();
            var x2 = x + h.x;
            var y2 = y + h.y;
            // lift the pen if we are off the image.
            if (!raster.getBounds().contains(x2, y2)) {
                turtle.penUp();
            } else {
                turtle.penDown();
            }
            turtle.forward(strideLength);
        }
    }
}
