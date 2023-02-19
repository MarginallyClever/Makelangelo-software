package com.marginallyclever.makelangelo.makeart.turtlegenerator;

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
			new Generator_HilbertCurve(),
			new Generator_KochCurve(),
			new Generator_Lissajous(),
			new Generator_LSystemTree(),
			new Generator_Maze(),
			new Generator_Package(),
			new Generator_Polyeder(),
			new Generator_SierpinskiTriangle(),
			new Generator_Spirograph(),
			new Generator_Text(),
			new Generator_TruchetTiles(),
			new Generator_Voronoi(),
	};
}
