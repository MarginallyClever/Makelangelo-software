package com.marginallyclever.makelangelo.makeart.turtlegenerator.maze;

/**
 * {@link MazeWall}s separate {@link MazeCell}s.
 * @since 7.43.0
 * @author Dan Royer
  */
public class MazeWall {
    public int cellA, cellB;
    public boolean removed = false;

    @Override
    public String toString() {
        return "[" + cellA + "," + cellB + "]";
    }
}
