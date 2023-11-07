package com.marginallyclever.makelangelo.makeart.turtlegenerator.maze;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.TurtleGenerator;
import com.marginallyclever.makelangelo.select.SelectRandomSeed;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;
import com.marginallyclever.makelangelo.turtle.Turtle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public abstract class Generator_Maze extends TurtleGenerator {
    protected final List<MazeCell> cells = new ArrayList<>();
    protected final List<MazeWall> walls = new ArrayList<>();
    private static int seed = 0;
    private static final Random random = new Random();

    public Generator_Maze() {
        super();
        add(new SelectReadOnlyText("url","<a href='https://en.wikipedia.org/wiki/Maze_generation_algorithm'>" +
                Translator.get("TurtleGenerators.LearnMore.Link.Text") + "</a>"));

        SelectRandomSeed selectRandomSeed = new SelectRandomSeed("randomSeed", Translator.get("Generator.randomSeed"), seed);
        add(selectRandomSeed);
        selectRandomSeed.addSelectListener((evt) -> {
            seed = (int) evt.getNewValue();
            random.setSeed((long) evt.getNewValue());
            generate();
        });
    }

    /**
     * build a list of walls in the maze, cells in the maze, and how they connect to each other.
     */
    @Override
    public void generate() {
        buildCells();
        buildWalls();
        buildMaze();

        // draw the maze
        Turtle turtle = drawMaze();

        turtle.translate(myPaper.getCenterX(),myPaper.getCenterY());

        notifyListeners(turtle);
    }

    /**
     * Build the list of rooms.
     */
    public abstract void buildCells();

    /**
     * Build the list of walls between rooms.
     */
    public abstract void buildWalls();

    public abstract Turtle drawMaze();


    /**
     * Depth first search of the maze, removing walls as we go.
     */
    protected void buildMaze() {
        int unvisitedCells = cells.size();
        Stack<MazeCell> stack = new Stack<>();

        // Make the initial cell the current cell and mark it as visited
        int currentCell = (int)(random.nextDouble()*unvisitedCells);
        cells.get(currentCell).visited = true;
        stack.add(cells.get(currentCell));
        --unvisitedCells;

        // While there are unvisited cells
        while (unvisitedCells > 0) {
            // If the current cell has any neighbours which have not been visited
            // Choose randomly one of the unvisited neighbours
            int nextCell = chooseUnvisitedNeighbor(currentCell);
            if (nextCell != -1) {
                // Remove the wall between the current cell and the next cell
                int wallIndex = findWallBetween(currentCell, nextCell);
                walls.get(wallIndex).removed = true;
                // Make the next cell into the current cell and mark it as visited
                currentCell = nextCell;

                cells.get(currentCell).visited = true;
                stack.add(cells.get(currentCell));
                --unvisitedCells;
            } else if(!stack.isEmpty()) {
                // else if stack is not empty pop a cell from the stack
                MazeCell c = stack.pop();
                currentCell = cells.indexOf(c);
            }
        }
    }

    protected int chooseUnvisitedNeighbor(int currentCell) {
        List<Integer> candidates = new ArrayList<>();
        MazeCell c = cells.get(currentCell);
        for(int i=0;i<c.walls.size();++i) {
            MazeWall w = c.walls.get(i);
            if(w.removed) continue;
            if(w.cellA==currentCell) {
                if(cells.get(w.cellB).visited) continue;
                candidates.add(w.cellB);
            } else {
                if(cells.get(w.cellA).visited) continue;
                candidates.add(w.cellA);
            }
        }

        if(candidates.isEmpty())
            return -1;

        // choose a random candidate
        int choice = (int) (random.nextDouble() * candidates.size());
        return candidates.get(choice);
    }

    protected int findWallBetween(int currentCell, int nextCell) {
        MazeCell c = cells.get(currentCell);
        for(MazeWall w : c.walls) {
            if(w.cellA==nextCell || w.cellB==nextCell) {
                return walls.indexOf(w);
            }
        }
        return -1;
    }
}
