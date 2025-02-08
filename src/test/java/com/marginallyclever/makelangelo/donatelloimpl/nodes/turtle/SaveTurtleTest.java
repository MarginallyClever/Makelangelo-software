package com.marginallyclever.makelangelo.donatelloimpl.nodes.turtle;

import com.marginallyclever.makelangelo.turtle.Turtle;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SaveTurtleTest {
    @Test
    public void test() {
        // make a simple drawing
        Turtle turtle = new Turtle();
        turtle.penDown();
        turtle.forward(100);
        turtle.turn(90);
        turtle.forward(100);

        SaveTurtle saveTurtle = new SaveTurtle();

        try {
            // create a temp file
            File tempSvgFile = File.createTempFile("turtleTest", ".svg");
            tempSvgFile.deleteOnExit();
            // set it as the output path
            saveTurtle.getPort("filename").setValue(tempSvgFile.getAbsolutePath());
            saveTurtle.getPort("turtle").setValue(turtle);
            saveTurtle.update();

            assertTrue(Files.size(tempSvgFile.toPath()) > 0, "The SVG file should not be empty.");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
