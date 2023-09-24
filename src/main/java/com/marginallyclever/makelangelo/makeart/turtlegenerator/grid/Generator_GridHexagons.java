package com.marginallyclever.makelangelo.makeart.turtlegenerator.grid;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.tools.ResizeTurtleToPaperAction;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.TurtleGenerator;
import com.marginallyclever.makelangelo.select.SelectDouble;
import com.marginallyclever.makelangelo.turtle.Turtle;

import javax.vecmath.Vector2d;

/**
 * Draws pointy-top tiled hexagons based on the diameter of the hexagons.
 * @author Dan Royer
 * @since 2022-04-21
 */
public class Generator_GridHexagons extends TurtleGenerator {
	private final SelectDouble radius;
	private double minorRadius;
	private double majorRadius;

	public Generator_GridHexagons() {
		add(radius = new SelectDouble("radius",Translator.get("Generator_GridHexagons.radius"),25));
		radius.addPropertyChangeListener(evt->generate());
	}

	@Override
	public String getName() {
		return Translator.get("Generator_GridHexagons.Name");
	}

	@Override
	public void generate() {
		majorRadius = radius.getValue();
		minorRadius = majorRadius*Math.sqrt(3);

		double horiz = Math.sqrt(3)*majorRadius;
		double vert = (3.0/2.0)*majorRadius;

		double yMin = myPaper.getMarginBottom();
		double yMax = myPaper.getMarginTop();
		double xMin = myPaper.getMarginLeft();
		double xMax = myPaper.getMarginRight();

		Turtle turtle = new Turtle();

		// in pointy-top hexagons the height is radius*2.
		int cellsPerColumn = (int)Math.floor((yMax-yMin)/(vert+1));
		int cellsPerRow = (int)Math.floor((xMax-xMin)/(horiz+1));

		for(int y=0;y<cellsPerColumn;++y) {
			for(int x=0;x<cellsPerRow;++x) {
				Vector2d p = getCellCenter(x,y,horiz,vert);
				int mask = 0b111111;
				if(x!=0) mask &= ~(1<<3);
				if(y!=0) {
					if(x!=0) mask &= ~(1<<4);
					if(x!=cellsPerRow-1) mask &= ~(1<<5);
				}
				drawHexagon(turtle,p,mask);
			}
		}

		ResizeTurtleToPaperAction act = new ResizeTurtleToPaperAction(myPaper,false,"");
		turtle = act.run(turtle);

		notifyListeners(turtle);
	}

	/**
	 * Draw hexagon, skipping sides that are masked.
	 * @param turtle the drawing tool
	 * @param center the center of the hexagon
	 * @param sideMask bits 0..5 represent each side, starting left and going counter-clockwise.
	 */
	private void drawHexagon(Turtle turtle, Vector2d center, int sideMask) {
		turtle.setAngle(90);
		turtle.jumpTo(center.x+minorRadius,center.y-majorRadius/2);
		for(int i=0;i<6;++i) {
			if( (sideMask & (1<<i)) == 0 ) {
				turtle.penUp();
			} else {
				turtle.penDown();
			}
			turtle.forward(majorRadius);
			turtle.turn(60);
		}
	}

	private Vector2d getCellCenter(int x,int y,double horiz,double vert) {
		Vector2d pos = new Vector2d(x*horiz, y*vert);
		if((y%2)==1) {
			// shift this row over by half a cell
			pos.x+=horiz/2;
		}
		return pos;
	}
}
