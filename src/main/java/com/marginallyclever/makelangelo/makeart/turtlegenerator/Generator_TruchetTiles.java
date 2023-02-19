package com.marginallyclever.makelangelo.makeart.turtlegenerator;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.tools.CropTurtle;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;
import com.marginallyclever.makelangelo.select.SelectSlider;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generate random Truchet tiles.
 * @author Dan Royer
 *
 */
public class Generator_TruchetTiles extends TurtleGenerator {
	private static final Logger logger = LoggerFactory.getLogger(Generator_TruchetTiles.class);

	private final SelectSlider lineSpacing;
	private final SelectSlider linesPerTile;
	private static int spaceBetweenLines = 10;
	private static int linesPerTileCount = 10;
	private int tileSize = 10;
	private int iterSize = 5;
	private double tileAdj;

	private Turtle turtle;

	Generator_TruchetTiles() {
		super();

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

		tileAdj = spaceBetweenLines /2.0;
		tileSize = spaceBetweenLines * linesPerTileCount;
		iterSize = spaceBetweenLines * 5;

		turtle = new Turtle();

		for(double y=yMin;y<yMax;y+=tileSize) {
			for(double x=xMin;x<xMax;x+=tileSize) {
				if(Math.random() >= 0.5) tileA(x,y);
				else                     tileB(x,y);
			}
		}

		CropTurtle.run(turtle,myPaper.getMarginRectangle());
		notifyListeners(turtle);
	}


	// style=/
	void tileA(double x0,double y0) {
		double x1=x0+tileSize;
		double y1=y0+tileSize;

		for(double x=tileAdj;x<tileSize;x+= spaceBetweenLines) {
			interTile(x0+x,y0,x0,y0+x,(int)(x0/tileSize),(int)(y0/tileSize));
			interTile(x0+x,y1,x1,y0+x,(int)(x0/tileSize),(int)(y0/tileSize));
		}
	}

	// style=\
	void tileB(double x0,double y0) {
		double x1=x0+tileSize;
		double y1=y0+tileSize;

		for(double x=tileAdj;x<tileSize;x+=lineSpacing.getValue()) {
			interTile(x0+x,y0,x1,y1-x,(int)(x0/tileSize),(int)(y0/tileSize));
			interTile(x0+x,y1,x0,y1-x,(int)(x0/tileSize),(int)(y0/tileSize));
		}
	}


	// Interpolate from (x0,y0) to (x1,y1) in steps of length iterSize.
	void interTile(double x0,double y0,double x1,double y1,int ax,int ay) {
		turtle.penUp();
		turtle.moveTo(x0,y0);
		turtle.penDown();
		turtle.moveTo(x1,y1);
	}
}
