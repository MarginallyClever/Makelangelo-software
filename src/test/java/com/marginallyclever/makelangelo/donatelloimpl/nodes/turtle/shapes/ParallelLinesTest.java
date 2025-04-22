package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle.shapes;

import com.marginallyclever.makelangelo.turtle.Turtle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ParallelLinesTest {
    public static Turtle createParallelLines() {
        ParallelLines parallelLines = new ParallelLines();
        parallelLines.getPort("numLines").setValue(16);
        parallelLines.getPort("spacing").setValue(16);
        parallelLines.getPort("length").setValue(256);
        parallelLines.update();
        return (Turtle)parallelLines.getPort("output").getValue();
    }

    @Test
    public void test() {
        Turtle turtle = createParallelLines();
        // Check the turtle's bounds
        var bounds = turtle.getBounds();
        Assertions.assertEquals(256, bounds.width, 0.1);
        Assertions.assertEquals(240, bounds.height, 0.1);
        // Check the bounds position
        Assertions.assertEquals(-128, bounds.getX(), 0.1);
        Assertions.assertEquals(-120, bounds.getY(), 0.1);
        /*
        // Save the turtle to a file
        var saver = new SaveTurtle();
        saver.getPort("turtle").setValue(turtle);
        saver.getPort("filename").setValue("parallelLines.svg");
        saver.update();
        */
    }
}
