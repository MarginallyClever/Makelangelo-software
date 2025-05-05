package com.marginallyclever.makelangelo.makeart.turtletool;

import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleIterator;

import javax.annotation.Nonnull;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

/**
 * Compare a line to a bitmap image and replace the line with a spiral, such that the amplitude and frequency are
 * controlled by the intensity of the image at the same point.
 */
public class SpiralByIntensity {
    private final TransformedImage img;
    private final double halfLineHeight;
    private final double stepSize;
    private double wavePosition = 0;

    /**
     * @param img the source image to sample from
     * @param halfLineHeight the width of the pulse line.
     * @param stepSize the speed at which to walk the line.
     */
    public SpiralByIntensity(@Nonnull TransformedImage img, double halfLineHeight, double stepSize) {
        this.halfLineHeight = halfLineHeight;
        this.stepSize = stepSize;
        this.img = img;
    }

    public Turtle turtleToSpiral(@Nonnull Turtle turtle) {
        Turtle result = new Turtle();
        TurtleIterator it = turtle.getIterator();
        Point2d prev = null;
        while (it.hasNext()) {
            var next = it.next();
            if(it.isTravel()) {
                prev = next;
                continue;
            }
            assert prev != null;
            var wave = lineToSpiral(prev, next);
            result.add(wave);
            prev = next;
        }

        return result;
    }

    /**
     * Convert a line to a wave based on the intensity of the image.
     * @param a the start of the line
     * @param b the end of the line
     */
    private Turtle lineToSpiral(Point2d a, Point2d b) {
        Turtle turtle = new Turtle();
        // find the length of the line and the unit vector
        var unitVector = new Vector2d(b.x - a.x,b.y - a.y);
        double len = unitVector.length();
        unitVector.scale(1.0/len);
        // find the orthogonal vector
        var orthogonal = new Vector2d(-unitVector.y,unitVector.x);

        Point2d interpolated = new Point2d();
        Point2d offset = new Point2d();
        calculatePoint(a,orthogonal,unitVector,offset);
        turtle.jumpTo(offset.x,offset.y);

        for (double p = 0; p <= len; p += this.stepSize) {
            wavePosition += this.stepSize;
            interpolated.set(
                    a.x + unitVector.x * p,
                    a.y + unitVector.y * p);
            calculatePoint(interpolated,orthogonal,unitVector,offset);
            turtle.moveTo(offset.x,offset.y);
        }
        return turtle;
    }

    /**
     * Calculate the point on the wave based on the intensity of the image.
     * @param a the point on the line
     * @param orthogonal the orthogonal vector to the line (the x-axis of the cos function)
     * @param unitVector the unit vector of the line (the y-axis of the cos function)
     * @param d the point on the wave
     */
    private void calculatePoint(Point2d a, Vector2d orthogonal, Vector2d unitVector, Point2d d) {
        // read a block of the image and find the average intensity in this block
        double z = (255.0f - img.sample( a.x, a.y, halfLineHeight));
        double hAdj = halfLineHeight*(z/255.0);
        // the sum controls the height of the pulse.
        var h = (z<=1) ? 0 : Math.cos(wavePosition) * hAdj;
        var w = (z<=1) ? 0 : Math.sin(wavePosition) * hAdj;
        d.x = a.x + orthogonal.x * h + unitVector.x * w;
        d.y = a.y + orthogonal.y * h + unitVector.y * w;
    }
}
