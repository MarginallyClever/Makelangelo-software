package com.marginallyclever.makelangelo.makeart.turtletool;

import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleIterator;

import javax.annotation.Nonnull;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

/**
 * Compare a line to a bitmap image and replace the line with a wave, such that the amplitude and frequency are
 * controlled by the intensity of the image at the same point.
 */
public class WaveByIntensity {
    private final TransformedImage img;
    private final double halfLineHeight;
    private final double stepSize;
    // controls the rate of oscillation of the wave.
    private double wavePosition = 0;

    /**
     * @param img the source image to sample from
     * @param halfLineHeight the width of the pulse line.
     * @param stepSize the speed at which to walk the line.
     */
    public WaveByIntensity(@Nonnull TransformedImage img, double halfLineHeight, double stepSize) {
        this.halfLineHeight = halfLineHeight;
        this.stepSize = stepSize;
        this.img = img;
    }

    public Turtle turtleToWave(@Nonnull Turtle turtle) {
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
            var wave = lineToWave(prev, next);
            result.add(wave);
            prev = next;
        }

        return result;
    }

    /**
     * Convert a line to a wave based on the intensity of the image.
     * @param p the start of the line
     * @param b the end of the line
     */
    public Turtle lineToWave(Point2d p, Point2d b) {
        Turtle turtle = new Turtle();
        // find the length of the line and the unit vector
        var unitVector = new Vector2d(b.x - p.x,b.y - p.y);
        double len = unitVector.length();
        unitVector.scale(1.0/len);
        // find the orthogonal vector
        var orthogonal = new Vector2d(-unitVector.y,unitVector.x);

        Point2d interpolated = new Point2d();
        Point2d offset = new Point2d();
        calculatePoint(p,orthogonal,offset);
        turtle.jumpTo(offset.x,offset.y);

        // controls the frequency of the wave.
        double i=0;
        while(i<=len) {
            double intensity = 1.0-(getIntensityAtPoint(p)/255.0);
            var safeI = stepSize * ((intensity*0.9)+0.1);
            i += safeI;
            wavePosition += stepSize;
            interpolated.set(
                    p.x + unitVector.x * i,
                    p.y + unitVector.y * i);
            calculatePoint(interpolated,orthogonal,offset);
            turtle.moveTo(offset.x,offset.y);
        }
        return turtle;
    }

    /**
     * Calculate the point on the wave based on the intensity of the image.
     * @param p the point on the line
     * @param orthogonal the orthogonal vector to the line (the x-axis of the cos function)
     * @param d the point on the wave
     */
    private void calculatePoint(Point2d p, Vector2d orthogonal, Point2d d) {
        // read a block of the image and find the average intensity in this block
        double z = getIntensityAtPoint(p);
        // the sum controls the height of the pulse.
        var h = (z<=1) ? 0 : Math.cos(wavePosition*2.0) * halfLineHeight*(z/255.0);
        d.x = p.x + orthogonal.x * h;
        d.y = p.y + orthogonal.y * h;
    }

    private double getIntensityAtPoint(Point2d p) {
        // read a block of the image and find the average intensity in this block
        return ( 255.0f - img.sample( p.x, p.y, halfLineHeight) );
    }
}
