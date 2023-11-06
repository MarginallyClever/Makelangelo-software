package com.marginallyclever.makelangelo.makeart.turtlegenerator.maze;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectSlider;
import com.marginallyclever.makelangelo.turtle.Turtle;

/**
 * Makes a "well formed" maze.
 * See also <a href="https://en.wikipedia.org/wiki/Maze_generation_algorithm#Recursive_backtracker">wikipedia</a>
 * @author Dan Royer
 */
public class Generator_MazeRectangle extends Generator_Maze {
	private static int rows = 5, columns = 5;

	public Generator_MazeRectangle() {
		super();

		SelectSlider field_rows;
		SelectSlider field_columns;

		add(field_rows = new SelectSlider("rows",Translator.get("Generator_MazeRectangle.rows"),100,1,getRows()));
		field_rows.addSelectListener(evt->{
			setRows(field_rows.getValue());
			generate();
		});
		add(field_columns = new SelectSlider("columns",Translator.get("Generator_MazeRectangle.columns"),100,1,getCols()));
		field_columns.addSelectListener(evt->{
			setCols(field_columns.getValue());
			generate();
		});
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
	public void buildCells() {
		cells.clear();

		int x, y;
		for (y = 0; y < rows; ++y) {
			for (x = 0; x < columns; ++x) {
				cells.add(new MazeCell(x,y));
			}
		}
	}

	@Override
	public void buildWalls() {
		walls.clear();
		MazeWall w;
		int x, y;
		for (y = 0; y < rows; ++y) {
			for (x = 0; x < columns; ++x) {
				if (x < columns - 1) {
					// vertical wall between horizontal cells
					w = new MazeWall(
							y * columns + x,
							y * columns + x + 1);
					walls.add(w);
					cells.get(w.cellA).walls.add(w);
					cells.get(w.cellB).walls.add(w);
				}
				if (y < rows - 1) {
					// horizontal wall between vertical cells
					w = new MazeWall(
						(y  ) * columns + x,
						(y+1) * columns + x);
					walls.add(w);
					cells.get(w.cellA).walls.add(w);
					cells.get(w.cellB).walls.add(w);
				}
			}
		}
	}

	@Override
	public Turtle drawMaze() {
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
}
