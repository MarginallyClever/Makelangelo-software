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

public class TurtleGeneratorFactory {
	public static class TurtleGeneratorNode {
		private final String name;
		public final List<TurtleGeneratorNode> children = new ArrayList<>();
		public TurtleGenerator generator;

		public String getName() {
			return name;
		}

		public TurtleGeneratorNode(String name, TurtleGeneratorNode [] kids) {
			this.name = name;
			children.addAll(List.of(kids));
		}
		public TurtleGeneratorNode(TurtleGenerator gen) {
			this.name = gen.getName();
			this.generator = gen;
		}

		public TurtleGenerator getGenerator() {
			return generator;
		}

		public List<TurtleGeneratorNode> getChildren() {
			return children;
		}
	};

	public static TurtleGeneratorNode available = new TurtleGeneratorNode(Translator.get("MenuGenerate"),new TurtleGeneratorNode[]{
			new TurtleGeneratorNode(new Generator_Border()),
			new TurtleGeneratorNode(Translator.get("MenuGenerate.Fractals"), new TurtleGeneratorNode[]{
                    new TurtleGeneratorNode(new Generator_Dragon()),
                    new TurtleGeneratorNode(new Generator_FibonacciSpiral()),
                    new TurtleGeneratorNode(new Generator_GosperCurve()),
                    new TurtleGeneratorNode(new Generator_HilbertCurve()),
                    new TurtleGeneratorNode(new Generator_KochCurve()),
                    new TurtleGeneratorNode(new Generator_LSystemTree()),
					new TurtleGeneratorNode(new Generator_SierpinskiTriangle()),
            }),
			new TurtleGeneratorNode(Translator.get("MenuGenerate.Grids"), new TurtleGeneratorNode[]{
					new TurtleGeneratorNode(new Generator_GraphPaper()),
					new TurtleGeneratorNode(new Generator_GridFit()),
					new TurtleGeneratorNode(new Generator_GridHexagons()),
			}),
			new TurtleGeneratorNode(Translator.get("MenuGenerate.SpaceFillers"), new TurtleGeneratorNode[]{
					new TurtleGeneratorNode(new Generator_FillPage()),
					new TurtleGeneratorNode(new Generator_FlowField()),
					new TurtleGeneratorNode(new Generator_Spiral()),
					new TurtleGeneratorNode(new Generator_TruchetTiles()),
					new TurtleGeneratorNode(new Generator_Voronoi()),
			}),
			new TurtleGeneratorNode(Translator.get("MenuGenerate.Mazes"), new TurtleGeneratorNode[]{
					new TurtleGeneratorNode(new Generator_MazeCircle()),
					new TurtleGeneratorNode(new Generator_MazeHoneycomb()),
					new TurtleGeneratorNode(new Generator_MazeRectangle()),
			}),
			new TurtleGeneratorNode(new LineWeightByImageIntensity()),
			new TurtleGeneratorNode(new Generator_AnalogClock()),
			new TurtleGeneratorNode(new Generator_Lissajous()),
			new TurtleGeneratorNode(new Generator_Package()),
			new TurtleGeneratorNode(new Generator_Polyeder()),
			new TurtleGeneratorNode(new Generator_Spirograph()),
			new TurtleGeneratorNode(new Generator_Text()),
	});
}
