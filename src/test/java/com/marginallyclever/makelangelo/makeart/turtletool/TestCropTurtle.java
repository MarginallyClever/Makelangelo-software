package com.marginallyclever.makelangelo.makeart.turtletool;

import com.marginallyclever.makelangelo.turtle.Turtle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.geom.Rectangle2D;

public class TestCropTurtle {
    private Turtle testCropAB(double x0,double y0,double x1,double y1) {
        Rectangle2D.Double rectangle = new Rectangle2D.Double(0, 0, 100, 100);

        Turtle turtle = new Turtle();
        turtle.penUp();
        turtle.moveTo(x0, y0);
        turtle.penDown();
        turtle.moveTo(x1, y1);

        CropTurtle.run(turtle, rectangle);
        return turtle;
    }

    @Test
    public void testCropEntireTurtle() {
        Turtle turtle = testCropAB(200,200,200,0);
        Assertions.assertEquals("[TOOL R0 G0 B0 A255 D1.000, TRAVEL X200.000 Y200.000]",turtle.history.toString());
        Assertions.assertEquals("Turtle{history=2, p=(200.0, 0.0), n=(1.0, 0.0), angle=0.0, isUp=false, color=java.awt.Color[r=0,g=0,b=0], diameter=1.0}",turtle.toString());
    }

    @Test
    public void testCropVertical1() {
        Turtle turtle = testCropAB(50,-50,50,50);
        Assertions.assertEquals("[TOOL R0 G0 B0 A255 D1.000, TRAVEL X50.000 Y-50.000, TRAVEL X50.000 Y0.000, DRAW_LINE X50.000 Y50.000]",turtle.history.toString());
        Assertions.assertEquals("Turtle{history=4, p=(50.0, 50.0), n=(1.0, 0.0), angle=0.0, isUp=false, color=java.awt.Color[r=0,g=0,b=0], diameter=1.0}",turtle.toString());
    }

    @Test
    public void testCropVertical2() {
        Turtle turtle = testCropAB(50,150,50,50);
        Assertions.assertEquals("[TOOL R0 G0 B0 A255 D1.000, TRAVEL X50.000 Y150.000, TRAVEL X50.000 Y100.000, DRAW_LINE X50.000 Y50.000]",turtle.history.toString());
        Assertions.assertEquals("Turtle{history=4, p=(50.0, 50.0), n=(1.0, 0.0), angle=0.0, isUp=false, color=java.awt.Color[r=0,g=0,b=0], diameter=1.0}",turtle.toString());
    }

    @Test
    public void testCropVertical3() {
        Turtle turtle = testCropAB(50,50,50,150);
        Assertions.assertEquals("[TOOL R0 G0 B0 A255 D1.000, TRAVEL X50.000 Y50.000, DRAW_LINE X50.000 Y100.000]",turtle.history.toString());
        Assertions.assertEquals("Turtle{history=3, p=(50.0, 150.0), n=(1.0, 0.0), angle=0.0, isUp=false, color=java.awt.Color[r=0,g=0,b=0], diameter=1.0}",turtle.toString());
    }

    @Test
    public void testCropVertical4() {
        Turtle turtle = testCropAB(50,50,50,-50);
        Assertions.assertEquals("[TOOL R0 G0 B0 A255 D1.000, TRAVEL X50.000 Y50.000, DRAW_LINE X50.000 Y0.000]",turtle.history.toString());
        Assertions.assertEquals("Turtle{history=3, p=(50.0, -50.0), n=(1.0, 0.0), angle=0.0, isUp=false, color=java.awt.Color[r=0,g=0,b=0], diameter=1.0}",turtle.toString());
    }

    @Test
    public void testCropHorizontal1() {
        Turtle turtle = testCropAB(-50,50,50,50);
        Assertions.assertEquals("[TOOL R0 G0 B0 A255 D1.000, TRAVEL X-50.000 Y50.000, TRAVEL X0.000 Y50.000, DRAW_LINE X50.000 Y50.000]",turtle.history.toString());
        Assertions.assertEquals("Turtle{history=4, p=(50.0, 50.0), n=(1.0, 0.0), angle=0.0, isUp=false, color=java.awt.Color[r=0,g=0,b=0], diameter=1.0}",turtle.toString());
    }

    @Test
    public void testCropHorizontal2() {
        Turtle turtle = testCropAB(150,50,50,50);
        Assertions.assertEquals("[TOOL R0 G0 B0 A255 D1.000, TRAVEL X150.000 Y50.000, TRAVEL X100.000 Y50.000, DRAW_LINE X50.000 Y50.000]",turtle.history.toString());
        Assertions.assertEquals("Turtle{history=4, p=(50.0, 50.0), n=(1.0, 0.0), angle=0.0, isUp=false, color=java.awt.Color[r=0,g=0,b=0], diameter=1.0}",turtle.toString());
    }

    @Test
    public void testCropHorizontal3() {
        Turtle turtle = testCropAB(50,50,150,50);
        Assertions.assertEquals("[TOOL R0 G0 B0 A255 D1.000, TRAVEL X50.000 Y50.000, DRAW_LINE X100.000 Y50.000]",turtle.history.toString());
        Assertions.assertEquals("Turtle{history=3, p=(150.0, 50.0), n=(1.0, 0.0), angle=0.0, isUp=false, color=java.awt.Color[r=0,g=0,b=0], diameter=1.0}",turtle.toString());
    }

    @Test
    public void testCropHorizontal4() {
        Turtle turtle = testCropAB(50,50,-50,50);
        Assertions.assertEquals("[TOOL R0 G0 B0 A255 D1.000, TRAVEL X50.000 Y50.000, DRAW_LINE X0.000 Y50.000]",turtle.history.toString());
        Assertions.assertEquals("Turtle{history=3, p=(-50.0, 50.0), n=(1.0, 0.0), angle=0.0, isUp=false, color=java.awt.Color[r=0,g=0,b=0], diameter=1.0}",turtle.toString());
    }
}
