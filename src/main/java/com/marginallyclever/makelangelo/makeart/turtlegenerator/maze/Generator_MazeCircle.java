package com.marginallyclever.makelangelo.makeart.turtlegenerator.maze;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;
import com.marginallyclever.makelangelo.select.SelectSlider;
import com.marginallyclever.makelangelo.turtle.Turtle;

import javax.vecmath.Vector2d;

/**
 * Makes a "well formed" maze in a circle.
 * See also <a href="https://en.wikipedia.org/wiki/Maze_generation_algorithm#Recursive_backtracker">wikipedia</a>
 * @author Dan Royer
 */
public class Generator_MazeCircle extends Generator_Maze {
	private static int rings = 5;

	public Generator_MazeCircle() {
		super();

		SelectSlider field_rings;

		add(field_rings = new SelectSlider("rings",Translator.get("Generator_MazeCircle.rings"),30,1,getRings()));
		field_rings.addPropertyChangeListener(evt->{
			setRings(field_rings.getValue());
			generate();
		});
		add(new SelectReadOnlyText("url","<a href='https://en.wikipedia.org/wiki/Maze_generation_algorithm'>"+Translator.get("TurtleGenerators.LearnMore.Link.Text")+"</a>"));
	}
	
	@Override
	public String getName() {
		return Translator.get("Generator_MazeCircle.name");
	}

	public void setRings(int arg0) {
		if(arg0<1) arg0=1;
		rings=arg0;
	}
	public int getRings() {
		return rings;
	}

	/**
	 * @param ring the ring to count
	 * @return the number of cells in the given ring.
	 */
	public int getCellsPerRing(int ring) {
		if(ring==0) return 1;
		int x = (int)Math.floor(Math.log(ring+1)/Math.log(2));
		return (int)Math.pow(2,x+3);
	}

	@Override
	public void buildCells() {
		cells.clear();
		for(int y = 0; y < 1+rings; ++y) {
			int count = getCellsPerRing(y);
			for(int x = 0; x < count; ++x) {
				cells.add(new MazeCell(x,y));
			}
		}
	}

	@Override
	public void buildWalls() {
		walls.clear();
		MazeWall w;

		// wall between cells in the same ring
		int first=1;
		for(int y = 1; y < 1+rings; ++y) {
			int count = getCellsPerRing(y);
			for (int x = 0; x < count; ++x) {
				w = new MazeWall(
						first + x,
						first + ((x + 1) % count));
				walls.add(w);
				cells.get(w.cellA).walls.add(w);
				cells.get(w.cellB).walls.add(w);
			}
			first+=count;
		}

		// wall between cells in adjacent rings
		first=1;
		int prev=0;
		int before=1;
		for(int y = 1; y < 1+rings; ++y) {
			int count = getCellsPerRing(y);
			for(int x = 0; x < count; ++x) {
				int b = first + x;
				int a = (y==1) ? 0 : (prev + getParentCell(b));  // only works if the ring ratio is always 1:2
				w = new MazeWall(a,b);
				walls.add(w);
				cells.get(w.cellA).walls.add(w);
				cells.get(w.cellB).walls.add(w);
			}
			first += count;
			prev += before;
			before = count;
		}
	}

	private int getParentCell(int cellIndex) {
		MazeCell c = cells.get(cellIndex);
		int count = getCellsPerRing(c.y);
		int inside = getCellsPerRing(c.y-1);
		if(count==inside) {
			return c.x;
		} else {
			return (int)Math.floor(c.x/2.0);
		}
	}

	@Override
	public Turtle drawMaze() {
		// find radius of maze
		double yMin = myPaper.getMarginBottom();
		double yMax = myPaper.getMarginTop();
		double xMin = myPaper.getMarginLeft();
		double xMax = myPaper.getMarginRight();

		double r = Math.min(xMax-xMin,yMax-yMin)/2.0;

		// find size of each ring
		double ringSize = r / (rings+1.0);

		Turtle turtle = new Turtle();

		// draw all walls that have not been removed
		int i;
		for(i=0;i<cells.size();++i) {
			MazeCell c = cells.get(i);
			for(MazeWall w : c.walls) {
				if (w.removed) continue;
				int a = w.cellA;
				int b = w.cellB;
				if(a<i || b<i) continue;
				int ax = cells.get(a).x;
				int ay = cells.get(a).y;
				int bx = cells.get(b).x;
				int by = cells.get(b).y;
				if (ay == by) {
					// wall between cells ax and bx in the same ring
					int count = getCellsPerRing(ay);
					Vector2d p = getAngleVector(bx, count);
					Vector2d q = new Vector2d();
					q.set(p);
					q.scale(ringSize * (ay));
					turtle.jumpTo(q.x, q.y);
					q.set(p);
					q.scale(ringSize * (ay + 1));
					turtle.moveTo(q.x, q.y);
				} else {
					// wall between two cells of different rings
					drawArc(turtle,
							0,
							0,
							ringSize * by,
							getAngle(bx + 0, getCellsPerRing(by)),
							getAngle(bx + 1, getCellsPerRing(by))
					);
				}
			}
		}

		int sum=getCellsPerRing(rings);
		for(i=0;i<sum-1;++i) {
			drawArc(turtle,0,0,r,getAngle(i,sum),getAngle(i+1,sum));
		}
		return turtle;
	}

	/**
	 * convert numerator/denominator into a unit vector pointing in the right direction.
	 * @param numerator position around circle
	 * @param denominator total steps around circle
	 * @return unit vector
	 */
	private Vector2d getAngleVector(double numerator, double denominator) {
		double angle = getAngle(numerator,denominator);
		return new Vector2d(Math.cos(angle),Math.sin(angle));
	}

	private double getAngle(double numerator, double denominator) {
		return Math.PI * 2.0 * numerator / denominator;
	}

	/**
	 * Draw an arc.
	 * @param turtle plotter
	 * @param cx center of arc
	 * @param cy center of arc
	 * @param radius of arc
	 * @param a1 start angle, in radians
	 * @param a2 end angle, in radians
	 */
	private void drawArc(Turtle turtle, double cx, double cy, double radius, double a1, double a2) {
		int steps = 10;
		double delta = (a2 - a1) / (double) steps;
		double f = a1;

		for (int i = 0; i <= steps; i++) {
			double x2 = cx + Math.cos(f) * radius;
			double y2 = cy + Math.sin(f) * radius;
			if(i==0) turtle.jumpTo(x2, y2);
			else     turtle.moveTo(x2, y2);
			f += delta;
		}
	}
}
