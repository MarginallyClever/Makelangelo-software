package com.marginallyclever.makelangelo.makeart.turtlegenerator;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.fractal.*;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.grid.Generator_GraphPaper;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.grid.Generator_GridFit;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.grid.Generator_GridHexagons;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.lineweight.LineWeightByImageIntensity;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.maze.Generator_MazeCircle;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.maze.Generator_MazeHoneycomb;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.maze.Generator_MazeRectangle;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory class for creating {@link TurtleGenerator} objects.
 */
public class TurtleGeneratorFactory {
	/**
	 * This class is a factory for TurtleGenerator objects.
	 * It provides a static list of available generators, organized in a tree structure.
	 * Each generator can be accessed by its name or through the tree structure.
	 */
	public static class TurtleGeneratorLeaf {
		private final String name;
		public final List<TurtleGeneratorLeaf> children = new ArrayList<>();
		public TurtleGenerator generator;

		public String getName() {
			return name;
		}

		public TurtleGeneratorLeaf(String name, TurtleGeneratorLeaf[] kids) {
			this.name = name;
			children.addAll(List.of(kids));
		}
		public TurtleGeneratorLeaf(TurtleGenerator gen) {
			this.name = gen.getName();
			this.generator = gen;
		}

		public TurtleGenerator getGenerator() {
			return generator;
		}

		public List<TurtleGeneratorLeaf> getChildren() {
			return children;
		}
	};

	public static TurtleGeneratorLeaf available = new TurtleGeneratorLeaf(Translator.get("MenuGenerate"),new TurtleGeneratorLeaf[]{
			new TurtleGeneratorLeaf(new Generator_Border()),
			new TurtleGeneratorLeaf(Translator.get("MenuGenerate.Fractals"), new TurtleGeneratorLeaf[]{
                    new TurtleGeneratorLeaf(new Generator_FibonacciSpiral()),
                    new TurtleGeneratorLeaf(new Generator_LSystem()),
                    new TurtleGeneratorLeaf(new Generator_LSystemTree()),
            }),
			new TurtleGeneratorLeaf(Translator.get("MenuGenerate.Grids"), new TurtleGeneratorLeaf[]{
					new TurtleGeneratorLeaf(new Generator_GraphPaper()),
					new TurtleGeneratorLeaf(new Generator_GridFit()),
					new TurtleGeneratorLeaf(new Generator_GridHexagons()),
			}),
			new TurtleGeneratorLeaf(Translator.get("MenuGenerate.SpaceFillers"), new TurtleGeneratorLeaf[]{
					new TurtleGeneratorLeaf(new Generator_FillPage()),
					new TurtleGeneratorLeaf(new Generator_FlowField()),
					new TurtleGeneratorLeaf(new Generator_Spiral()),
					new TurtleGeneratorLeaf(new Generator_TruchetTiles()),
					new TurtleGeneratorLeaf(new Generator_Voronoi()),
			}),
			new TurtleGeneratorLeaf(Translator.get("MenuGenerate.Mazes"), new TurtleGeneratorLeaf[]{
					new TurtleGeneratorLeaf(new Generator_MazeCircle()),
					new TurtleGeneratorLeaf(new Generator_MazeHoneycomb()),
					new TurtleGeneratorLeaf(new Generator_MazeRectangle()),
			}),
			new TurtleGeneratorLeaf(new LineWeightByImageIntensity()),
			new TurtleGeneratorLeaf(new Generator_AnalogClock()),
			new TurtleGeneratorLeaf(new Generator_Lissajous()),
			new TurtleGeneratorLeaf(new Generator_Package()),
			new TurtleGeneratorLeaf(new Generator_Polyeder()),
			new TurtleGeneratorLeaf(new Generator_Spirograph()),
			new TurtleGeneratorLeaf(new Generator_Text()),
	});
}
