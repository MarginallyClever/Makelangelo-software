package com.marginallyclever.makelangelo.makeArt.io.vector;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.makelangelo.turtle.Turtle;

public class SaveHelper {

    public static Turtle simpleMoves() {
        Turtle turtle = new Turtle();
        turtle.jumpTo(-15, -7);
        turtle.moveTo(3, 4);
        turtle.moveTo(7, 8);
        turtle.jumpTo(12, 18);
        return turtle;
    }

    public static Turtle multiColorsMoves() {
        Turtle turtle = new Turtle(new ColorRGB(1,2,3));
        turtle.jumpTo(-15, -7);
        turtle.moveTo(3, 4);
        turtle.jumpTo(12, 18);
        turtle.moveTo(7, 8);
        turtle.jumpTo(5, 2);
        turtle.moveTo(6, 10);

        turtle.setColor(new ColorRGB(4, 5, 6));
        turtle.moveTo(20, 30);
        turtle.jumpTo(-15, -7);
        turtle.moveTo(10, 15);
        turtle.jumpTo(-4, -8);
        return turtle;
    }
}
