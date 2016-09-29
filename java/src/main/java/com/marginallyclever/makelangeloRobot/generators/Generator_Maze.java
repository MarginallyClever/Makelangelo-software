/*
 * https://en.wikipedia.org/wiki/Maze_generation_algorithm#Recursive_backtracker
 */
package com.marginallyclever.makelangeloRobot.generators;

import java.awt.GridLayout;
import java.io.IOException;
import java.io.Writer;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.marginallyclever.makelangelo.Translator;

public class Generator_Maze extends ImageGenerator {
	protected class MazeCell {
		int x, y;
		boolean visited;
		boolean onStack;
	}

	protected class MazeWall {
		int cellA, cellB;
		boolean removed;
	}

	protected static int rows = 5, columns = 5;
	protected float xMax, xMin, yMax, yMin;
	protected MazeCell[] cells;
	protected MazeWall[] walls;

	@Override
	public String getName() {
		return Translator.get("MazeName");
	}

	@Override
	public String getPreviewImage() {
		return "/images/generators/maze.JPG";
	}


	@Override
	public boolean generate(Writer out) throws IOException {
		while (true) {
			final JTextField field_rows = new JTextField(Integer.toString(rows));
			final JTextField field_columns = new JTextField(Integer.toString(columns));

			JPanel panel = new JPanel(new GridLayout(0, 1));
			panel.add(new JLabel(Translator.get("MazeRows")));
			panel.add(field_rows);
			panel.add(new JLabel(Translator.get("MazeColumns")));
			panel.add(field_columns);

			int result = JOptionPane.showConfirmDialog(null, panel, getName(), JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE);
			if (result != JOptionPane.OK_OPTION)
				return false;
			else {
				rows = Integer.parseInt(field_rows.getText());
				columns = Integer.parseInt(field_columns.getText());

				if (rows < 1)
					continue;
				if (columns < 1)
					continue;

				createMazeNow(out);
				return true;
			}
		}
	}

	/**
	 * build a list of walls in the maze, cells in the maze, and how they connect to each other.
	 * @param out
	 * @throws IOException
	 */
	private void createMazeNow(Writer out) throws IOException {
		imageStart(out);
		liftPen(out);
		machine.writeChangeTo(out);
		
		// build the cells
		cells = new MazeCell[rows * columns];

		int x, y, i = 0;
		for (y = 0; y < rows; ++y) {
			for (x = 0; x < columns; ++x) {
				cells[i] = new MazeCell();
				cells[i].visited = false;
				cells[i].onStack = false;
				cells[i].x = x;
				cells[i].y = y;
				++i;
			}
		}

		// build the graph
		walls = new MazeWall[((rows - 1) * columns) + ((columns - 1) * rows)];
		i = 0;
		for (y = 0; y < rows; ++y) {
			for (x = 0; x < columns; ++x) {
				if (x < columns - 1) {
					// vertical wall between horizontal cells
					walls[i] = new MazeWall();
					walls[i].removed = false;
					walls[i].cellA = y * columns + x;
					walls[i].cellB = y * columns + x + 1;
					++i;
				}
				if (y < rows - 1) {
					// horizontal wall between vertical cells
					walls[i] = new MazeWall();
					walls[i].removed = false;
					walls[i].cellA = y * columns + x;
					walls[i].cellB = y * columns + x + columns;
					++i;
				}
			}
		}

		int unvisitedCells = cells.length; // -1 for initial cell.
		int cellsOnStack = 0;

		// Make the initial cell the current cell and mark it as visited
		int currentCell = 0;
		cells[currentCell].visited = true;
		--unvisitedCells;

		// While there are unvisited cells
		while (unvisitedCells > 0) {
			// If the current cell has any neighbours which have not been visited
			// Choose randomly one of the unvisited neighbours
			int nextCell = chooseUnvisitedNeighbor(currentCell);
			if (nextCell != -1) {
				// Push the current cell to the stack
				cells[currentCell].onStack = true;
				++cellsOnStack;
				// Remove the wall between the current cell and the chosen cell
				int wallIndex = findWallBetween(currentCell, nextCell);
				assert (wallIndex != -1);
				walls[wallIndex].removed = true;
				// Make the chosen cell the current cell and mark it as visited
				currentCell = nextCell;
				cells[currentCell].visited = true;
				--unvisitedCells;
			} else if (cellsOnStack > 0) {
				// else if stack is not empty pop a cell from the stack
				for (i = 0; i < cells.length; ++i) {
					if (cells[i].onStack) {
						// Make it the current cell
						currentCell = i;
						cells[i].onStack = false;
						--cellsOnStack;
						break;
					}
				}
			}
		}

		// draw the maze
		drawMaze(out);
		liftPen(out);
	    moveTo(out, (float)machine.getHomeX(), (float)machine.getHomeY(),true);
	}

	private void drawMaze(Writer output) throws IOException {
		yMin = (float)machine.getPaperBottom() * (float)machine.getPaperMargin() * 10;
		yMax = (float)machine.getPaperTop()    * (float)machine.getPaperMargin() * 10;
		xMin = (float)machine.getPaperLeft()   * (float)machine.getPaperMargin() * 10;
		xMax = (float)machine.getPaperRight()  * (float)machine.getPaperMargin() * 10;
		
		float w = (xMax - xMin) / columns;
		float h = (yMax - yMin) / rows;

		// Draw outside edge
		liftPen(output);
		moveTo(output, xMin, yMax, true);
		moveTo(output, xMin, yMax, false);
		moveTo(output, xMax, yMax, false);
		moveTo(output, xMax, yMin + h, false);
		moveTo(output, xMax, yMin + h, true);
		// bottom right gap for exit is here
		moveTo(output, xMax, yMin, true);
		moveTo(output, xMax, yMin, false);
		moveTo(output, xMin, yMin, false);
		// top-left gap for entrance is left here
		moveTo(output, xMin, yMax - h, false);
		moveTo(output, xMin, yMax - h, true);

		int i;
		for (i = 0; i < walls.length; ++i) {
			if (walls[i].removed)
				continue;
			int a = walls[i].cellA;
			int b = walls[i].cellB;
			int ax = cells[a].x;
			int ay = cells[a].y;
			int bx = cells[b].x;
			int by = cells[b].y;
			if (ay == by) {
				// vertical wall
				float x = xMin + (ax + 1) * w;
				float y0 = yMin + (ay + 0) * h;
				float y1 = yMin + (ay + 1) * h;
				
				moveTo(output, x, y0, true);
				moveTo(output, x, y0, false);
				moveTo(output, x, y1, false);
				moveTo(output, x, y1, true);
			} else if (ax == bx) {
				// horizontal wall
				float x0 = xMin + (ax + 0) * w;
				float x1 = xMin + (ax + 1) * w;
				float y = yMin + (ay + 1) * h;
				moveTo(output, x0, y, true);
				moveTo(output, x0, y, false);
				moveTo(output, x1, y, false);
				moveTo(output, x1, y, true);
			}
		}
	}

	private int chooseUnvisitedNeighbor(int currentCell) {
		int x = cells[currentCell].x;
		int y = cells[currentCell].y;

		int[] candidates = new int[4];
		int found = 0;

		// left
		if (x > 0 && cells[currentCell - 1].visited == false) {
			candidates[found++] = currentCell - 1;
		}
		// right
		if (x < columns - 1 && !cells[currentCell + 1].visited) {
			candidates[found++] = currentCell + 1;
		}
		// up
		if (y > 0 && !cells[currentCell - columns].visited) {
			candidates[found++] = currentCell - columns;
		}
		// down
		if (y < rows - 1 && !cells[currentCell + columns].visited) {
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
		for (i = 0; i < walls.length; ++i) {
			if (walls[i].cellA == currentCell || walls[i].cellA == nextCell) {
				if (walls[i].cellB == currentCell || walls[i].cellB == nextCell)
					return i;
			}
		}
		return -1;
	}
}
