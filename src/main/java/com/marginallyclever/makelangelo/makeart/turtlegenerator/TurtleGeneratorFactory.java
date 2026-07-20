package com.marginallyclever.makelangelo.makeart.turtlegenerator;

import com.marginallyclever.makelangelo.editorcontext.EditorContext;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.fractal.*;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.grid.Generator_GraphPaper;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.grid.Generator_GridFit;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.grid.Generator_GridHexagons;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.lineweight.LineWeightByImageIntensity;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.maze.Generator_MazeCircle;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.maze.Generator_MazeHoneycomb;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.maze.Generator_MazeRectangle;

/**
 * Factory class for creating {@link TurtleGenerator} objects.
 */
public class TurtleGeneratorFactory {
	public static TurtleGeneratorLeaf available = null;

	public static void setup(EditorContext context) {
		available = new TurtleGeneratorLeaf("MenuGenerate",new TurtleGeneratorLeaf[]{
				new TurtleGeneratorLeaf(new Generator_Border()),
				new TurtleGeneratorLeaf("MenuGenerate.Fractals", new TurtleGeneratorLeaf[]{
						new TurtleGeneratorLeaf(new Generator_FibonacciSpiral()),
						new TurtleGeneratorLeaf(new Generator_LSystem()),
						new TurtleGeneratorLeaf(new Generator_LSystemTree()),
				}),
				new TurtleGeneratorLeaf("MenuGenerate.Grids", new TurtleGeneratorLeaf[]{
						new TurtleGeneratorLeaf(new Generator_GraphPaper()),
						new TurtleGeneratorLeaf(new Generator_GridFit()),
						new TurtleGeneratorLeaf(new Generator_GridHexagons()),
				}),
				new TurtleGeneratorLeaf("MenuGenerate.SpaceFillers", new TurtleGeneratorLeaf[]{
						new TurtleGeneratorLeaf(new Generator_FillPage()),
						new TurtleGeneratorLeaf(new Generator_FlowField()),
						new TurtleGeneratorLeaf(new Generator_Spiral()),
						new TurtleGeneratorLeaf(new Generator_TruchetTiles()),
						new TurtleGeneratorLeaf(new Generator_Voronoi()),
				}),
				new TurtleGeneratorLeaf("MenuGenerate.Mazes", new TurtleGeneratorLeaf[]{
						new TurtleGeneratorLeaf(new Generator_MazeCircle()),
						new TurtleGeneratorLeaf(new Generator_MazeHoneycomb()),
						new TurtleGeneratorLeaf(new Generator_MazeRectangle()),
				}),
				new TurtleGeneratorLeaf(new LineWeightByImageIntensity()),
				new TurtleGeneratorLeaf(new Generator_AnalogClock()),
				new TurtleGeneratorLeaf(new Generator_Lissajous()),
				new TurtleGeneratorLeaf(new Generator_Package()),
				new TurtleGeneratorLeaf(new Generator_Polyeder()),
				new TurtleGeneratorLeaf(new Generator_RegistrationMarks()),
				new TurtleGeneratorLeaf(new Generator_Spirograph()),
				new TurtleGeneratorLeaf(new Generator_Text()),
		});
	}
}
