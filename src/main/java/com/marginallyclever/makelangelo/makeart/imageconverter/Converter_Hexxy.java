package com.marginallyclever.makelangelo.makeart.imageconverter;


import com.marginallyclever.donatello.select.SelectSlider;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imagefilter.FilterDesaturate;
import com.marginallyclever.makelangelo.makeart.turtletool.InfillTurtle;
import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Turtle;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * A grid of hexagons across the paper, and make the hexagons bigger if the image is darker in that area.
 * Requested by @Harkish on Twitch.
 * @author Dan Royer
 */
public class Converter_Hexxy extends ImageConverter {
	public static int boxMaxSize=4; // 0.8*5
	private double majorRadius;
	private double minorRadius;

	public Converter_Hexxy() {
		super();

		SelectSlider size = new SelectSlider("size",Translator.get("Converter_Hexxy.MaxSize"),40,2,getBoxMasSize());
		size.addSelectListener((evt)->{
			setBoxMaxSize((int)evt.getNewValue());
			fireRestart();
		});
		add(size);
	}

	@Override
	public String getName() {
		return Translator.get("Converter_Hexxy.Name");
	}

	public void setBoxMaxSize(int arg0) {
		boxMaxSize=arg0;
	}
	
	public int getBoxMasSize() {
		return boxMaxSize;
	}

	@Override
	public void start(Paper paper, TransformedImage image) {
		super.start(paper, image);

		FilterDesaturate bw = new FilterDesaturate(myImage);
		TransformedImage img = bw.filter();

		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		double yMin = rect.getMinY();
		double yMax = rect.getMaxY();
		double xMin = rect.getMinX();
		double xMax = rect.getMaxX();

		turtle = new Turtle();
		double diameter = settings.getDouble(PlotterSettings.DIAMETER);
		turtle.setStroke(Color.BLACK,diameter);

		majorRadius = boxMaxSize;
		minorRadius = majorRadius*Math.sqrt(3);

		double horiz = Math.sqrt(3)*majorRadius;
		double vert = (3.0/2.0)*majorRadius;

		// in pointy-top hexagons the height is radius*2.
		int cellsPerColumn = (int)Math.floor((yMax-yMin)/(vert));
		int cellsPerRow = (int)Math.floor((xMax-xMin)/(horiz));

		double adjX = ((xMax-xMin) - (cellsPerRow*horiz))/2.0 + xMin;
		double adjY = ((yMax-yMin) - (cellsPerColumn*vert))/2.0 + yMin;

		InfillTurtle filler = new InfillTurtle();
		filler.setPenDiameter(diameter);

		double magic = (minorRadius - turtle.getDiameter()*2) / minorRadius;
		Vector2d p;
		// move over the entire page
		for(int y=0;y<cellsPerColumn;++y) {
			for(int x=0;x<cellsPerRow;++x) {
				// alternate direction to reduce travel
				if(y%2==0) {
					p = getCellCenter(x, y, horiz, vert);
				} else {
					p = getCellCenter(cellsPerRow-1-x, y, horiz, vert);
				}
				p.x += adjX;
				p.y += adjY;
				// find the hexagon size
				var sample = (img.sample(p.x, p.y, majorRadius) / 255.0);
				var inverseSample = 1.0 - sample;
				var intensity = inverseSample * magic;
				if(intensity<2.0/255.0) continue;

				// add a hexagon
				Turtle hexagon = new Turtle();
				hexagon.setStroke(turtle.getColor(),diameter);
				drawHexagon(hexagon,p,intensity);
				try {
					hexagon.add(filler.run(hexagon));
					// convert all travel moves within this hexagon to draw moves to save time.
					hexagon.convertTravelToDraw();

					turtle.add(hexagon);
				} catch (Exception ignore) {
					// do nothing.
				}
			}
		}

		//ResizeTurtleToPaperAction act = new ResizeTurtleToPaperAction(myPaper,false,"");
		//turtle = act.run(turtle);

		turtle.translate(myPaper.getCenterX(),myPaper.getCenterY());

		fireConversionFinished();
	}

	/**
	 * Draw hexagon, skipping sides that are masked.
	 * @param turtle the drawing tool
	 * @param center the center of the hexagon
	 */
	private void drawHexagon(Turtle turtle, Vector2d center,double scale) {
		turtle.setAngle(90);
		double r0 = minorRadius * scale;
		double r1 = majorRadius * scale;
		turtle.jumpTo(center.x+r0,center.y-r1/2);
		for(int i=0;i<6;++i) {
			turtle.forward(r1);
			turtle.turn(60);
		}
	}

	private Vector2d getCellCenter(int x,int y,double horiz,double vert) {
		Vector2d pos = new Vector2d(x*horiz, y*vert + vert/2);
		if((y%2)==1) {
			// shift this row over by half a cell
			pos.x+=horiz/2;
		}
		return pos;
	}
}
