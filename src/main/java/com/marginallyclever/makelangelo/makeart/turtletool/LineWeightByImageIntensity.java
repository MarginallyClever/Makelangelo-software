package com.marginallyclever.makelangelo.makeart.turtletool;

import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thicken the lines of a {@link Turtle} based on the intensity of an image.
 */
public class LineWeightByImageIntensity {
    private static final Logger logger = LoggerFactory.getLogger(LineWeightByImageIntensity.class);

    public LineWeightByImageIntensity() {}

    /**
     * Processes the lines of an input {@link Turtle} and thickens them based on the intensity
     * of a corresponding {@link TransformedImage}.
     *
     * @param input the input {@link Turtle} containing the lines to be processed
     * @param image the {@link TransformedImage} used to determine the thickness
     * @param stepSize refinement of lines for sampling.
     * @param maxLineWidth maximum thickness of the new line.
     * @param penDiameter the pen diameter, controls spacing between passes.
     * @return a new {@link Turtle} containing the thickened line segments
     */
    public Turtle run(Turtle input,
                      TransformedImage image,
                      double stepSize,
                      double maxLineWidth,
                      double penDiameter) {
        ThickenLinesByIntensity tool = new ThickenLinesByIntensity();
        return tool.execute(input, image, stepSize, maxLineWidth, penDiameter);
    }
}
