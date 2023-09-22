package com.marginallyclever.makelangelo.makeart.turtlegenerator.truchet;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.tools.CropTurtle;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.TurtleGenerator;
import com.marginallyclever.makelangelo.select.SelectBoolean;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;
import com.marginallyclever.makelangelo.select.SelectSlider;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Generate random Truchet tiles.
 * @author Dan Royer
 *
 */
public class Generator_TruchetTiles extends TurtleGenerator {
	private static final Logger logger = LoggerFactory.getLogger(Generator_TruchetTiles.class);
	private final SelectBoolean allowDiagonalChoice;
	private final SelectBoolean allowOrthogonalChoice;
	private final SelectBoolean allowCurvedChoice;
	private final SelectSlider lineSpacing;
	private final SelectSlider linesPerTile;
	private static int spaceBetweenLines = 10;
	private static int linesPerTileCount = 10;
	private static boolean allowDiagonal = true;
	private static boolean allowOrthogonal = true;
	private static boolean allowCurved = true;

	public Generator_TruchetTiles() {
		super();

		add(allowDiagonalChoice = new SelectBoolean("allowDiagonal",Translator.get("Generator_TruchetTiles.diagonal"),allowDiagonal));
		allowDiagonalChoice.addPropertyChangeListener(evt->generate());
		add(allowOrthogonalChoice = new SelectBoolean("allowOrthogonal",Translator.get("Generator_TruchetTiles.orthogonal"),allowOrthogonal));
		allowOrthogonalChoice.addPropertyChangeListener(evt->generate());
		add(allowCurvedChoice = new SelectBoolean("allowCurved",Translator.get("Generator_TruchetTiles.curved"),allowCurved));
		allowCurvedChoice.addPropertyChangeListener(evt->generate());

		add(lineSpacing = new SelectSlider("lineSpacing",Translator.get("Generator_TruchetTiles.LineSpacing"),20,2,Generator_TruchetTiles.getSpacing()));
		lineSpacing.addPropertyChangeListener(evt->generate());
		add(linesPerTile = new SelectSlider("linesPerTile",Translator.get("Generator_TruchetTiles.LinesPerTile"),15,1,Generator_TruchetTiles.getLinesPerTile()));
		linesPerTile.addPropertyChangeListener(evt->generate());
		add(new SelectReadOnlyText("url","<a href='https://en.wikipedia.org/wiki/Truchet_tiles'>"+Translator.get("TurtleGenerators.LearnMore.Link.Text")+"</a>"));
	}

	private static int getSpacing() {
		return spaceBetweenLines;
	}

	private static int getLinesPerTile() {
		return linesPerTileCount;
	}

	@Override
	public String getName() {
		return Translator.get("Generator_TruchetTiles.Name");
	}

	@Override
	public void generate() {
		double yMin = myPaper.getMarginBottom();
		double yMax = myPaper.getMarginTop();
		double xMin = myPaper.getMarginLeft();
		double xMax = myPaper.getMarginRight();

		spaceBetweenLines = lineSpacing.getValue();
		linesPerTileCount = linesPerTile.getValue();
		allowDiagonal = allowDiagonalChoice.isSelected();
		allowOrthogonal = allowOrthogonalChoice.isSelected();
		allowCurved = allowCurvedChoice.isSelected();

		int tileSize = spaceBetweenLines * linesPerTileCount;

		Turtle turtle = new Turtle();

		List<TruchetTileGenerator> ttgList = new ArrayList<>();
		if(allowDiagonal  ) ttgList.add(new TruchetDiagonal  (turtle,spaceBetweenLines,linesPerTileCount));
		if(allowOrthogonal) ttgList.add(new TruchetOrthogonal(turtle,spaceBetweenLines,linesPerTileCount));
		if(allowCurved    ) ttgList.add(new TruchetCurved    (turtle,spaceBetweenLines,linesPerTileCount));

		if(!ttgList.isEmpty()) {
			for(double y=yMin;y<yMax;y+= tileSize) {
				for(double x=xMin;x<xMax;x+= tileSize) {
					int v = (int)(Math.random()* ttgList.size());
					ttgList.get(v).drawTile(x,y);
				}
			}
		}

		CropTurtle.run(turtle,myPaper.getMarginRectangle());
		notifyListeners(turtle);
	}
}
