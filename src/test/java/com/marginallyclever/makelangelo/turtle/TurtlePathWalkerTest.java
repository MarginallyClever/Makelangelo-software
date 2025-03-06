package com.marginallyclever.makelangelo.turtle;

import com.marginallyclever.makelangelo.makeart.io.TurtleFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TurtlePathWalkerTest {
    /**
     * Compare a TurtlePathWalker to Turtle.interpolate for a simple path.
     */
    @Test
    public void compareToInterpolate() throws Exception {
        Turtle t0 = TurtleFactory.load("src/test/resources/com/marginallyclever/makelangelo/makeart/io/file_example_MP3_1MG.mp3");
        TurtlePathWalker walker = new TurtlePathWalker(t0);
        double dist = t0.getDrawDistance();
        double pace = 1.0;
        int maxIter = 5000;
        for( double i=pace; i<dist && maxIter>0; i+=pace ) {
            maxIter--;
            var p1 = TurtleTest.interpolate(t0,i);
            var p2 = walker.walk(pace);
            System.out.println("i="+i+" d="+walker.getTSum()+" p1="+p1.x+","+p1.y+" p2="+p2.x+","+p2.y);
            Assertions.assertEquals(p1.x,p2.x, 1e-1,"i="+i);
            Assertions.assertEquals(p1.y,p2.y, 1e-1,"i="+i);
        }
    }
}
