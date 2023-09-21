package com.marginallyclever.makelangelo.makeart.turtlegenerator.maze;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link MazeCell}s are the rooms separted by {@link MazeWall}s
 * @since 7.43.0
 * @author Dan Royer
 */
public class MazeCell {
    public int x, y;
    public boolean visited = false;

    public List<MazeWall> walls = new ArrayList<>();

    public MazeCell(int x,int y) {
        this.x=x;
        this.y=y;
    }
    
    @Override
    public String toString() {
        return "(" + y + "," + x + ")" + (visited ? "v" : "");
    }
}
