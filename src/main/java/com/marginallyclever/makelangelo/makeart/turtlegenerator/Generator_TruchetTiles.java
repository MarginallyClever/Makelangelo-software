package com.marginallyclever.makelangelo.makeart.turtlegenerator;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.tools.CropTurtle;
import com.marginallyclever.makelangelo.makeart.truchet.*;
import com.marginallyclever.makelangelo.select.SelectBoolean;
import com.marginallyclever.makelangelo.select.SelectRandomSeed;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;
import com.marginallyclever.makelangelo.select.SelectSlider;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generate random Truchet tiles using the {@link TruchetTileFactory} as a menu of available tiles.
 * @author Dan Royer
 * @since 7.48.0
 */
public class Generator_TruchetTiles extends TurtleGenerator {
	private static final Logger logger = LoggerFactory.getLogger(Generator_TruchetTiles.class);
	private final SelectSlider lineSpacing;
	private final SelectSlider linesPerTile;
	private static int spaceBetweenLines = 10;
	private static int linesPerTileCount = 10;
	private static int seed=0;
	private static final Random random = new Random();

	private final List<Boolean> allowedTiles = new ArrayList<>();

	public Generator_TruchetTiles() {
		super();

		SelectRandomSeed selectRandomSeed = new SelectRandomSeed("randomSeed",Translator.get("Generator.randomSeed"),seed);
		add(selectRandomSeed);
		selectRandomSeed.addSelectListener((evt)->{
			seed = (int)evt.getNewValue();
			random.setSeed(seed);
			generate();
		});

		List<String> names = TruchetTileFactory.getNames();
		// first time
		if(allowedTiles.size() != names.size()) {
			for(String name : names) {
				allowedTiles.add(true);
			}
		}

		for(int i=0;i<names.size();++i) {
			SelectBoolean allow = new SelectBoolean("allow"+i,Translator.get("Generator_TruchetTiles.allow",new String[]{names.get(i)}),allowedTiles.get(i));
			add(allow);
			int finalI = i;
			allow.addSelectListener(evt->{
				allowedTiles.set(finalI,allow.isSelected());
					generate();
			});
		}

		add(lineSpacing = new SelectSlider("lineSpacing",Translator.get("Generator_TruchetTiles.LineSpacing"),20,2,Generator_TruchetTiles.getSpacing()));
		lineSpacing.addSelectListener(evt->generate());
		add(linesPerTile = new SelectSlider("linesPerTile",Translator.get("Generator_TruchetTiles.LinesPerTile"),15,1,Generator_TruchetTiles.getLinesPerTile()));
		linesPerTile.addSelectListener(evt->generate());
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

		int tileSize = spaceBetweenLines * linesPerTileCount;

		Turtle turtle = new Turtle();

		List<TruchetTile> ttgList = new ArrayList<>();
		for(int i=0;i<allowedTiles.size();++i) {
			if(allowedTiles.get(i)) {
				ttgList.add(TruchetTileFactory.getTile(i,turtle,spaceBetweenLines,linesPerTileCount));
			}
		}

		if(!ttgList.isEmpty()) {
			for(double y=yMin;y<yMax;y+= tileSize) {
				for(double x=xMin;x<xMax;x+= tileSize) {
					int v = (int)(random.nextDouble()* ttgList.size());
					ttgList.get(v).drawTile(x,y);
				}
			}
		}

		CropTurtle.run(turtle,myPaper.getMarginRectangle());
		notifyListeners(turtle);
	}
}
