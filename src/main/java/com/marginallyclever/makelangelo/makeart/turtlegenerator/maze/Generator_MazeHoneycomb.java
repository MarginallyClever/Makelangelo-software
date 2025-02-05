package com.marginallyclever.makelangelo.makeart.turtlegenerator.maze;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.turtletool.ResizeTurtleToPaperAction;
import com.marginallyclever.donatello.select.SelectSlider;
import com.marginallyclever.makelangelo.turtle.Turtle;

import javax.vecmath.Vector2d;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Makes a "well formed" honeycomb maze.  This maze uses "point top" hexagons where odd-numbered rows are shifted to
 * the right.  See also
 * <a href="https://en.wikipedia.org/wiki/Maze_generation_algorithm#Recursive_backtracker">wikipedia</a>,
 * <a href="https://www.redblobgames.com/grids/hexagons/">redblobgames</a>.
 * @author Dan Royer
 * @since 7.43.0
 */
public class Generator_MazeHoneycomb extends Generator_Maze {
	private static int rows = 5, columns = 5;

	public Generator_MazeHoneycomb() {
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
		return Translator.get("Generator_MazeHoneycomb.name");
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


	private final int[] neighbors = new int[6];
	private int neighborCount;

	@Override
	public void buildWalls() {
		walls.clear();

		List<Long> duplicates = new ArrayList<>();

		MazeWall w;
		int x, y, currentCell=0;
		for (y = 0; y < rows; ++y) {
			boolean isOddRow = ((y%2)==1);
			for (x = 0; x < columns; ++x) {
				// get the six possible neighbors of this cell - two above, two below, two to the side
				neighborCount = 0;
				// above
				maybeAdd(y - 1, x + 0);
				maybeAdd(y - 1, x + (isOddRow? 1:-1));
				// below
				maybeAdd(y + 1, x + 0);
				maybeAdd(y + 1, x + (isOddRow? 1:-1));
				// left and right
				maybeAdd(y + 0, x - 1);
				maybeAdd(y + 0, x + 1);

				for (int i = 0; i < neighborCount; ++i) {
					// vertical wall between horizontal cells
					// do not add duplicates.
					long a = currentCell;
					long b = neighbors[i];
					if(a>b) {
						long tmp = a;
						a = b;
						b = tmp;
					}
					long address = a<<16 | b;
					if(duplicates.contains(address)) continue;
					duplicates.add(address);

					w = new MazeWall( currentCell, neighbors[i]);
					walls.add(w);
					cells.get(w.cellA).walls.add(w);
					cells.get(w.cellB).walls.add(w);
				}
				currentCell++; // current cell
			}
		}
	}

	private void maybeAdd(int y, int x) {
		if(y<0 || y>=rows) return;
		if(x<0 || x>=columns) return;
		neighbors[neighborCount] = (y * columns) + x;
		neighborCount++;
	}

	@Override
	public Turtle drawMaze() {
		Turtle turtle = new Turtle();

		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		double yMin = rect.getMinY();
		double yMax = rect.getMaxY();
		double xMin = rect.getMinX();
		double xMax = rect.getMaxX();

		double w = (xMax - xMin) / columns;
		double h = (yMax - yMin) / rows;
		double len = Math.min(w,h)/2.0;
		// In the point-top orientation, the horizontal distance between adjacent hexagon centers is
		double horiz = Math.sqrt(3) * len;
		// The vertical distance is
		double vert = 3.0/2.0 * len;

		drawInteriorWalls(turtle,len,horiz,vert);
		drawOutsideEdge(turtle,len,horiz,vert);

		ResizeTurtleToPaperAction act = new ResizeTurtleToPaperAction(myPaper,false,"");
		turtle = act.run(turtle);

		return turtle;
	}

	private void drawInteriorWalls(Turtle turtle, double len, double horiz, double vert) {
		int i;
		for (i = 0; i < walls.size(); ++i) {
			MazeWall wall = walls.get(i);
			if (wall.removed) {
				continue;
			}

			int a = wall.cellA;
			int b = wall.cellB;

			Vector2d aPos = getCellCenter(a,horiz,vert);
			Vector2d bPos = getCellCenter(b,horiz,vert);
			// get the midpoint
			Vector2d mid = new Vector2d();
			mid.add(aPos,bPos);
			mid.scale(0.5);
			// get a vector a to b, then rotate it 90 degrees and make it sin(PI/12) long.
			Vector2d n = new Vector2d();
			n.sub(aPos,bPos);
			double tmp = n.x;
			n.x = n.y;
			n.y = -tmp;
			n.normalize();
			n.scale(len/2.0);

			Vector2d start = new Vector2d(mid);
			start.sub(n);

			Vector2d end = new Vector2d(mid);
			end.add(n);

			turtle.jumpTo(start.x, start.y);
			turtle.moveTo(end.x, end.y);
		}
	}

	private void drawOutsideEdge(Turtle turtle,double len, double horiz, double vert) {
		// top edge
		for(int x=0;x<columns;++x) {
			Vector2d center = getCellCenter(cells.size()-1-x,horiz,vert);
			if(x>0) drawEdge(turtle, center, len, 1);
			drawEdge(turtle,center,len,2);
		}

		// bottom edge
		for(int x=0;x<columns;++x) {
			Vector2d center = getCellCenter(x,horiz,vert);
			if(x>0) drawEdge(turtle,center,len,4);
			drawEdge(turtle,center,len,5);
		}

		// left edge
		for(int y=0;y<rows;++y) {
			Vector2d center = getCellCenter(y*columns,horiz,vert);
			if((y%2)==0) {  // even row
				drawEdge(turtle,center,len,2);
				drawEdge(turtle,center,len,3);
				if(y!=0) drawEdge(turtle,center,len,4);
			} else {
				drawEdge(turtle,center,len,3);
			}
		}

		// right edge
		for(int y=0;y<rows;++y) {
			Vector2d center = getCellCenter((y+1)*columns-1,horiz,vert);
			if((y%2)==0) {  // even row
				drawEdge(turtle,center,len,0);
			} else {
				drawEdge(turtle,center,len,0);
				if(y!=rows-1) drawEdge(turtle,center,len,1);
				drawEdge(turtle,center,len,5);
			}
		}
	}

	/**
	 * draw edges of a hexagon
	 * @param turtle the drawing tool
	 * @param center position of the center of the hexagon
	 * @param len distance between center and corner
	 * @param index counter-clockwise order, starting with vertical side on the right.
	 */
	private void drawEdge(Turtle turtle, Vector2d center, double len, int index) {
		Vector2d c0 = getCorner(center,len,index);
		Vector2d c1 = getCorner(center,len,index+1);
		turtle.jumpTo(c0.x,c0.y);
		turtle.moveTo(c1.x,c1.y);
	}

	/**
	 * get the position of a corner of a hexagon
	 * @param center position of the center of the hexagon
	 * @param len distance between center and corner
	 * @param index counter-clockwise order, starting with the south-east corner.
	 * @return position of the corner
	 */
	private Vector2d getCorner(Vector2d center,double len,int index) {
		double angleRad = Math.toRadians(60.0*index-30.0);
		return new Vector2d(
				center.x + len * Math.cos(angleRad),
				center.y + len * Math.sin(angleRad));
	}


	private Vector2d getCellCenter(int index,double horiz,double vert) {
		MazeCell c = cells.get(index);
		Vector2d pos = new Vector2d(c.x*horiz, c.y*vert);
		if((c.y%2)==1) {
			// shift this row over by half a cell
			pos.x+=horiz/2;
		}
		return pos;
	}
}
