package com.marginallyclever.makelangelo.makeart.turtlegenerator;

import com.marginallyclever.makelangelo.makeart.turtlegenerator.fractal.*;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.grid.Generator_GraphPaper;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.grid.Generator_GridFit;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.grid.Generator_GridHexagons;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.maze.Generator_MazeCircle;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.maze.Generator_MazeHoneycomb;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.maze.Generator_MazeRectangle;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.truchet.Generator_TruchetTiles;

public class TurtleGeneratorFactory {
	public static TurtleGenerator [] available = {
			new Generator_Border(),
			new Generator_Dragon(),
			new Generator_FibonacciSpiral(),
			new Generator_FillPage(),
			new Generator_FlowField(),
			new Generator_GosperCurve(),
			new Generator_GraphPaper(),
			new Generator_GridFit(),
			new Generator_GridHexagons(),
			new Generator_HilbertCurve(),
			new Generator_KochCurve(),
			new Generator_Lissajous(),
			new Generator_LSystemTree(),
			new Generator_MazeRectangle(),
			new Generator_MazeCircle(),
			new Generator_MazeHoneycomb(),
			new Generator_Package(),
			new Generator_Polyeder(),
			new Generator_SierpinskiTriangle(),
			new Generator_Spirograph(),
			new Generator_Text(),
			new Generator_TruchetTiles(),
			new Generator_Voronoi(),
	};
}
