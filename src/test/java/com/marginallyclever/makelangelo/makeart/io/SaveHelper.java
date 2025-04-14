package com.marginallyclever.makelangelo.makeart.io;

import com.marginallyclever.makelangelo.turtle.Turtle;

import java.awt.*;

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
        Turtle turtle = new Turtle(new Color(0xff,0x99,0x00));
        turtle.jumpTo(-15, -7);
        turtle.moveTo(3, 4);
        turtle.jumpTo(12, 18);
        turtle.moveTo(7, 8);
        turtle.jumpTo(5, 2);
        turtle.moveTo(6, 10);

        turtle.setStroke(new Color(0x00, 0x44, 0xff));
        turtle.jumpTo(-15, -7);
        turtle.moveTo(20, 30);
        turtle.jumpTo(-4, -8);
        turtle.moveTo(10, 15);
        return turtle;
    }
}
