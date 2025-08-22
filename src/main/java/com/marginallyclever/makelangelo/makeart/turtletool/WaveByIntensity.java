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
    private double minimumFrequency = 5.0;

    /**
     * @param img the source image to sample from
     * @param halfLineHeight the width of the pulse line.
     * @param stepSize the speed at which to walk the line.
     * @param minimumFrequency the minimum frequency of the wave, in mm.  Should be >= stepSize.
     */
    public WaveByIntensity(@Nonnull TransformedImage img, double halfLineHeight, double stepSize, double minimumFrequency) {
        this.halfLineHeight = halfLineHeight;
        this.stepSize = stepSize;
        this.img = img;
        this.minimumFrequency = minimumFrequency;
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
     * @param start the start of the line
     * @param end the end of the line
     */
    public Turtle lineToWave(Point2d start, Point2d end) {
        Turtle turtle = new Turtle();
        // find the length of the line and the unit vector
        var direction = new Vector2d(end.x - start.x,end.y - start.y);
        double len = direction.length();
        direction.scale(1.0/len);
        // find the orthogonal vector
        var orthogonal = new Vector2d(-direction.y,direction.x);

        Point2d interpolated = new Point2d();
        Point2d offset = new Point2d();
        calculatePoint(start,orthogonal,offset);
        turtle.jumpTo(offset.x,offset.y);

        // controls the frequency of the wave.
        double i=0;
        while(i<=len) {
            interpolated.set(
                    start.x + direction.x * i,
                    start.y + direction.y * i);
            double intensity = 1.0-(getIntensityAtPoint(interpolated)/255.0);
            var safeI = stepSize * ((intensity*0.8)+0.2);
            i += safeI * minimumFrequency;
            wavePosition += stepSize;
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
        double amplitude = getIntensityAtPoint(p);
        // the sum controls the height of the pulse.
        var h = (amplitude<=1) ? 0 : Math.cos(wavePosition*2.0) * halfLineHeight*(amplitude/255.0);
        d.x = p.x + orthogonal.x * h;
        d.y = p.y + orthogonal.y * h;
    }

    private double getIntensityAtPoint(Point2d p) {
        // read a block of the image and find the average intensity in this block
        return ( 255.0f - img.sample( p.x, p.y, halfLineHeight) );
    }
}
