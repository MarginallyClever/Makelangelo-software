package com.marginallyclever.makelangelo.makeart.turtletool;

import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point2d;
import java.util.Random;

/**
 * Compare the lines of a {@link com.marginallyclever.makelangelo.turtle.Turtle} to the intensity of an image.
 * Mask any part that falls below a threshold monte carlo.  The inputs should be the turtle, the image, the threshold,
 * the monte carlo percentage, and the random seed.
 */
public class MaskLineByImageIntensity {
    private static final Logger logger = LoggerFactory.getLogger(MaskLineByImageIntensity.class);
    private final Random rand = new Random();

    public MaskLineByImageIntensity() {}

    /**
     * Processes the lines of an input {@link Turtle} and filters segments based on the intensity
     * of a corresponding {@link TransformedImage} mask. Lines are preserved or removed based on
     * a threshold comparison and a Monte Carlo probability calculation.
     *
     * @param input the input {@link Turtle} containing the lines to be processed
     * @param mask the {@link TransformedImage} used as a mask to determine the intensity values for filtering
     * @param threshold the threshold value below which a line segment may be masked
     * @param monteCarloPercentage the percentage value representing the probability in the Monte Carlo filtering process
     * @param seedValue the seed value used to initialize the random number generator for consistent Monte Carlo processing
     * @return a new {@link Turtle} containing the filtered line segments
     */
    public Turtle run(Turtle input,
                      TransformedImage mask,
                      double threshold,
                      double monteCarloPercentage,
                      long seedValue) {
        rand.setSeed(seedValue);

        Turtle result = new Turtle();
        Turtle currentTurtle = null;

        var it = input.getIterator();
        Point2d prev = null;
        while(it.hasNext()) {
            Point2d p = it.next();
            if (it.isToolChange()) {
                prev = p;
                if (currentTurtle != null) {
                    result.add(currentTurtle);
                }
                currentTurtle = new Turtle();
                continue;
            }
            if(it.isTravel()) {
                prev = p;
                continue;
            }

            // check if the line from prev to p should be kept
            double intensity = mask.sample1x1(p.x, p.y) & 0xFF;
            /*
            boolean darkEnough = intensity < threshold;
            boolean isMontyCarlo = rand.nextDouble() * 100.0 >= monteCarloPercentage;
            if(isMontyCarlo) darkEnough = !darkEnough;
            boolean keep = (darkEnough ^ isMontyCarlo);
            */
            double variation = monteCarloPercentage / 100.0;
            double jitteredThreshold = Math.clamp(threshold + 255.0 * (rand.nextDouble() * 2.0 - 1.0) * variation,0,255);
            boolean keep = intensity <  jitteredThreshold;
            if (keep) {
                assert(prev!=null);
                assert(currentTurtle!=null);
                currentTurtle.jumpTo(prev.x, prev.y);
                currentTurtle.moveTo(p.x, p.y);
            }
            prev = p;
        }

        if (currentTurtle != null) {
            result.add(currentTurtle);
        }

        return result;
    }

}
