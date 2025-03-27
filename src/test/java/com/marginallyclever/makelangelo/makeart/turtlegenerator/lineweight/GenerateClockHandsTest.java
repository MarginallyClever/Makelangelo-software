package com.marginallyclever.makelangelo.makeart.turtlegenerator.lineweight;

import com.marginallyclever.makelangelo.makeart.io.TurtleFactory;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettingsManager;
import com.marginallyclever.makelangelo.turtle.Turtle;

public class GenerateClockHandsTest {
    /**
     * Generate a series of clock hands for 12 at 5 minute intervals.
     */
    public static void main(String[] args) {
        Turtle result = new Turtle();
        int radius = 50;
        int margin = 20;
        int x = 0;
        int y = 0;
        int k=0;
        for(int i=0;i<=60;i+=5) {
            var t = GenerateClockHands.generateClockHands(0,i,radius);
            t.translate(x,y);
            x += radius*2+margin;
            k++;
            if( 0 == (k % 3) ) {
                y -= radius*2+margin;
                x = 0;
            }

            result.add(t);
        }

        // save turtle to SVG file
        try {
            TurtleFactory.save(result, "clockhands.svg", PlotterSettingsManager.buildMakelangelo5());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
