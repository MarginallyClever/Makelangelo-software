package com.marginallyclever.makelangelo.makeart.turtlegenerator.maze;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.TurtleGenerator;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;
import com.marginallyclever.makelangelo.select.SelectSlider;
import com.marginallyclever.makelangelo.turtle.Turtle;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Makes a "well formed" maze.
 * See also <a href="https://en.wikipedia.org/wiki/Maze_generation_algorithm#Recursive_backtracker">wikipedia</a>
 * @author Dan Royer
 */
public class Generator_MazeRectangle extends TurtleGenerator {
	private static int rows = 5, columns = 5;
	private final List<MazeCell> cells = new ArrayList<>();
	private final List<MazeWall> walls = new ArrayList<>();

	public Generator_MazeRectangle() {
		super();

		SelectSlider field_rows;
		SelectSlider field_columns;

		add(field_rows = new SelectSlider("rows",Translator.get("Generator_MazeRectangle.rows"),100,1,getRows()));
		field_rows.addPropertyChangeListener(evt->{
			setRows(field_rows.getValue());
			generate();
		});
		add(field_columns = new SelectSlider("columns",Translator.get("Generator_MazeRectangle.columns"),100,1,getCols()));
		field_columns.addPropertyChangeListener(evt->{
			setCols(field_columns.getValue());
			generate();
		});
		add(new SelectReadOnlyText("url","<a href='https://en.wikipedia.org/wiki/Maze_generation_algorithm'>"+Translator.get("TurtleGenerators.LearnMore.Link.Text")+"</a>"));
	}
	
	@Override
	public String getName() {
		return Translator.get("Generator_MazeRectangle.name");
	}

	public int getRows() {
		return rows;
	}
	public void setRows(int arg0) {
		if(arg0<1) arg0=1;
		rows=arg0;
	}
	public int getCols() {
		return columns;
	}
	public void setCols(int arg0) {
		if(arg0<1) arg0=1;
		columns=arg0;
	}

	/**
	 * build a list of walls in the maze, cells in the maze, and how they connect to each other.
	 */
	@Override
	public void generate() {
		cells.clear();

		// build the cells
		int x, y, i ;
		for (y = 0; y < rows; ++y) {
			for (x = 0; x < columns; ++x) {
				MazeCell c = new MazeCell();
				c.visited = false;
				c.x = x;
				c.y = y;
				cells.add(c);
			}
		}

		// build the graph
		walls.clear();
		for (y = 0; y < rows; ++y) {
			for (x = 0; x < columns; ++x) {
				if (x < columns - 1) {
					// vertical wall between horizontal cells
					MazeWall w = new MazeWall();
					w.removed = false;
					w.cellA = y * columns + x;
					w.cellB = y * columns + x + 1;
					walls.add(w);
				}
				if (y < rows - 1) {
					// horizontal wall between vertical cells
					MazeWall w = new MazeWall();
					w.removed = false;
					w.cellA = y * columns + x;
					w.cellB = y * columns + x + columns;
					walls.add(w);
				}
			}
		}

		Stack<MazeCell> stack = new Stack<>();
		int unvisitedCells = cells.size();
		int cellsOnStack = 0;

		// Make the initial cell the current cell and mark it as visited
		int currentCell = 0;
		cells.get(currentCell).visited = true;
		stack.add(cells.get(currentCell));
		--unvisitedCells;

		// While there are unvisited cells
		while (unvisitedCells > 0) {
			// If the current cell has any neighbours which have not been visited
			// Choose randomly one of the unvisited neighbours
			int nextCell = chooseUnvisitedNeighbor(currentCell);
			if (nextCell != -1) {
				// Push the current cell to the stack
				++cellsOnStack;
				// Remove the wall between the current cell and the next cell
				int wallIndex = findWallBetween(currentCell, nextCell);
				assert (wallIndex != -1);
				walls.get(wallIndex).removed = true;
				// Make the next cell into the current cell and mark it as visited
				currentCell = nextCell;
				stack.add(cells.get(currentCell));
				cells.get(currentCell).visited = true;
				--unvisitedCells;
			} else {
				// else if stack is not empty pop a cell from the stack
				MazeCell c = stack.pop();
				currentCell = cells.indexOf(c);
			}
		}

		// draw the maze
		Turtle turtle = drawMaze();

		notifyListeners(turtle);
	}

	private Turtle drawMaze() {
		double yMin = myPaper.getMarginBottom();
		double yMax = myPaper.getMarginTop();
		double xMin = myPaper.getMarginLeft();
		double xMax = myPaper.getMarginRight();
		
		double w = (xMax - xMin) / columns;
		double h = (yMax - yMin) / rows;

		Turtle turtle = new Turtle();
		
		// Draw outside edge
		turtle.jumpTo(xMin, yMax);
		turtle.moveTo(xMax, yMax);
		turtle.moveTo(xMax, yMin + h);
		// bottom right gap for exit is here
		turtle.jumpTo(xMax, yMin);
		turtle.moveTo(xMin, yMin);
		// top-left gap for entrance is left here
		turtle.moveTo(xMin, yMax - h);

		int i;
		for (i = 0; i < walls.size(); ++i) {
			MazeWall wall = walls.get(i);
			if (wall.removed)
				continue;
			int a = wall.cellA;
			int b = wall.cellB;
			int ax = cells.get(a).x;
			int ay = cells.get(a).y;
			int bx = cells.get(b).x;
			int by = cells.get(b).y;
			if (ay == by) {
				// vertical wall
				double x = xMin + (ax + 1) * w;
				double y0 = yMin + (ay + 0) * h;
				double y1 = yMin + (ay + 1) * h;
				turtle.jumpTo(x, y0);
				turtle.moveTo(x, y1);
			} else if (ax == bx) {
				// horizontal wall
				double x0 = xMin + (ax + 0) * w;
				double x1 = xMin + (ax + 1) * w;
				double y = yMin + (ay + 1) * h;
				turtle.jumpTo(x0, y);
				turtle.moveTo(x1, y);
			}
		}
		return turtle;
	}

	private int chooseUnvisitedNeighbor(int currentCell) {
		int x = cells.get(currentCell).x;
		int y = cells.get(currentCell).y;

		int[] candidates = new int[4];
		int found = 0;

		// left
		if (x > 0 && !cells.get(currentCell - 1).visited) {
			candidates[found++] = currentCell - 1;
		}
		// right
		if (x < columns - 1 && !cells.get(currentCell + 1).visited) {
			candidates[found++] = currentCell + 1;
		}
		// up
		if (y > 0 && !cells.get(currentCell - columns).visited) {
			candidates[found++] = currentCell - columns;
		}
		// down
		if (y < rows - 1 && !cells.get(currentCell + columns).visited) {
			candidates[found++] = currentCell + columns;
		}

		if (found == 0)
			return -1;

		// choose a random candidate
		int choice = (int) (Math.random() * found);
		assert (choice >= 0 && choice < found);

		return candidates[choice];
	}

	private int findWallBetween(int currentCell, int nextCell) {
		int i;
		for (i = 0; i < walls.size(); ++i) {
			if (walls.get(i).cellA == currentCell || walls.get(i).cellA == nextCell) {
				if (walls.get(i).cellB == currentCell || walls.get(i).cellB == nextCell)
					return i;
			}
		}
		return -1;
	}
}
