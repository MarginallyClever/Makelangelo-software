package com.marginallyclever.makelangelo.makeart.turtlegenerator.lineweight;

import com.marginallyclever.makelangelo.turtle.Turtle;

import javax.vecmath.Point2d;
import java.awt.*;

/**
 * Generate a {@link Turtle} of analog clock hands for any given time on a 12h clock.
 */
public class GenerateClockHands {
    /**
     * {@link Turtle} starts at 0,0 facing east, 3pm.  12 is up, 3 is right, 6 is down, 9 is left.
     * @param hour 0-12
     * @param minute 0-59
     * @return a {@link Turtle} that draws the clock hands.
     */
    public static Turtle generateClockHands(int hour, int minute, double radius) {
        Turtle result = new Turtle();

        // move to the end of the hour hand
        var h = getClockHandEnd(hour*5 + minute/12);
        result.setStroke(Color.BLACK);
        result.jumpTo(h.x*radius*0.75,h.y*radius*0.75);
        // draw to center
        result.penDown();
        result.moveTo(0,0);
        // draw to minute hand
        var m = getClockHandEnd(minute);
        result.moveTo(m.x*radius,m.y*radius);
        result.penUp();

        return result;
    }

    private static Point2d getClockHandEnd(int minute) {
        var x = Math.sin(Math.toRadians(6*minute));
        var y = Math.cos(Math.toRadians(6*minute));
        return new Point2d(x,y);
    }
}
