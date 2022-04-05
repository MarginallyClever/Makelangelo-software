package com.marginallyclever.makelangelo.makeart.turtlegenerator;

import com.marginallyclever.convenience.PerlinNoise;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.turtle.Turtle;

import javax.vecmath.Vector2d;
import java.awt.*;

/**
 * Two kinds of flow fields.  Uses Perlin noise to generate the field.
 * @author Dan Royer
 * @since 2022-04-05
 */
public class Generator_FlowField extends TurtleGenerator {
	private static double scaleX = 0.01; // controls complexity of curve
	private static double scaleY = 0.01; // controls complexity of curve
	private static double offsetX = 0; // controls complexity of curve
	private static double offsetY = 0; // controls complexity of curve
	private static int stepSize = 5; // controls complexity of curve
	private static boolean fromEdge = false;  // continuous lines
	private static boolean rightAngle = false;

	public static void setScaleX(double scaleX) {
		Generator_FlowField.scaleX = scaleX;
	}
	public static void setScaleY(double scaleY) {
		Generator_FlowField.scaleY = scaleY;
	}
	public static void setOffsetX(double offsetX) {
		Generator_FlowField.offsetX = offsetX;
	}
	public static void setOffsetY(double offsetY) {
		Generator_FlowField.offsetY = offsetY;
	}
	public static void setStepSize(int stepSize) {
		Generator_FlowField.stepSize = stepSize;
	}
	public static void setFromEdge(boolean fromEdge) {
		Generator_FlowField.fromEdge = fromEdge;
	}
	public static void setRightAngle(boolean rightAngle) {
		Generator_FlowField.rightAngle = rightAngle;
	}

	public static double getScaleX() {
		return scaleX;
	}
	public static double getScaleY() {
		return scaleY;
	}
	public static double getOffsetX() {
		return offsetX;
	}
	public static double getOffsetY() {
		return offsetY;
	}
	public static int getStepSize() {
		return stepSize;
	}
	public static boolean getFromEdge() {
		return fromEdge;
	}
	public static boolean getRightAngle() {
		return rightAngle;
	}

	@Override
	public String getName() {
		return Translator.get("Generator_FlowField.name");
	}

	@Override
	public TurtleGeneratorPanel getPanel() {
		return new Generator_FlowField_Panel(this);
	}
		
	@Override
	public void generate() {
		Turtle turtle = new Turtle();

		if (fromEdge) {
			fromEdge(turtle);
			if(rightAngle) {
				rightAngle=false;
				fromEdge(turtle);
				rightAngle=true;
			}
		} else {
			asGrid(turtle);
		}

		notifyListeners(turtle);
	}

	private void fromEdge(Turtle turtle) {
		double xMin = myPaper.getMarginLeft()+stepSize;
		double yMin = myPaper.getMarginBottom()+stepSize;
		double yMax = myPaper.getMarginTop()-stepSize;
		double xMax = myPaper.getMarginRight()-stepSize;
		Rectangle r = new Rectangle((int)xMin,(int)yMin,(int)(xMax-xMin),(int)(yMax-yMin));
		r.grow(1,1);

		for(double y = yMin; y<yMax; y+=stepSize) {
			makeLine(turtle, r, xMin, y);
			makeLine(turtle, r, xMax, y);
		}
		for(double x = xMin; x<xMax; x+=stepSize) {
			makeLine(turtle, r, x, yMin);
			makeLine(turtle, r, x, yMax);
		}
	}

	private void makeLine(Turtle turtle, Rectangle r, double x, double y) {
		turtle.jumpTo(x,y);
		// if the first step at this position would be outside the rectangle, reverse the direction.
		double v = PerlinNoise.noise(turtle.getX() * scaleX + offsetX, turtle.getY() * scaleY + offsetY, 0);
		turtle.setAngle(v * 180 + (rightAngle?90:0));
		Vector2d nextStep = turtle.getHeading();
		nextStep.scale(stepSize);
		nextStep.add(turtle.getPosition());
		continueLine(turtle, r,!r.contains(nextStep.x,nextStep.y));
	}

	private void continueLine(Turtle turtle, Rectangle r, boolean reverse) {
		for(int i=0;i<200;++i) {
			double v = PerlinNoise.noise(turtle.getX() * scaleX + offsetX, turtle.getY() * scaleY + offsetY, 0);
			turtle.setAngle(v * 180 + (rightAngle?90:0));
			Vector2d nextStep = turtle.getHeading();
			nextStep.scale(reverse?-stepSize:stepSize);
			nextStep.add(turtle.getPosition());
			// stop if we leave the rectangle
			if(!r.contains(nextStep.x,nextStep.y)) break;
			turtle.moveTo(nextStep.x,nextStep.y);
		}
	}

	private void asGrid(Turtle turtle) {
		double xMin = myPaper.getMarginLeft()+stepSize;
		double yMin = myPaper.getMarginBottom()+stepSize;
		double yMax = myPaper.getMarginTop()-stepSize;
		double xMax = myPaper.getMarginRight()-stepSize;

		for(double y = yMin; y<yMax; y+=stepSize) {
			for(double x = xMin; x<xMax; x+=stepSize) {
				double v = PerlinNoise.noise(x*scaleX + offsetX,y*scaleY+offsetY,0);
				turtle.jumpTo(x,y);
				turtle.setAngle(v*180);
				turtle.forward(stepSize);
			}
		}
	}
}
