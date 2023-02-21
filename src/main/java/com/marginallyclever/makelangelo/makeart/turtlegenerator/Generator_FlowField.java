package com.marginallyclever.makelangelo.makeart.turtlegenerator;

import com.marginallyclever.convenience.noise.Noise;
import com.marginallyclever.convenience.noise.PerlinNoise;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectBoolean;
import com.marginallyclever.makelangelo.select.SelectDouble;
import com.marginallyclever.makelangelo.select.SelectReadOnlyText;
import com.marginallyclever.makelangelo.select.SelectSlider;
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
	private Noise noiseMaker = new PerlinNoise();

	public static void setStepSize(int stepSize) {
		Generator_FlowField.stepSize = stepSize;
	}
	public static int getStepSize() {
		return stepSize;
	}

	public Generator_FlowField() {
		super();
		SelectDouble fieldScaleX = new SelectDouble("scaleX",Translator.get("Generator_FlowField.scaleX"),scaleX);
		SelectDouble fieldScaleY = new SelectDouble("scaleY",Translator.get("Generator_FlowField.scaleY"),scaleY);
		SelectDouble fieldOffsetX = new SelectDouble("offsetX",Translator.get("Generator_FlowField.offsetX"),offsetX);
		SelectDouble fieldOffsetY = new SelectDouble("offsetY",Translator.get("Generator_FlowField.offsetY"),offsetY);
		SelectSlider fieldStepSize = new SelectSlider("stepSize",Translator.get("Generator_FlowField.stepSize"),20,3,stepSize);
		SelectBoolean fieldFromEdge = new SelectBoolean("fromEdge",Translator.get("Generator_FlowField.fromEdge"),fromEdge);
		SelectBoolean fieldRightAngle = new SelectBoolean("rightAngle",Translator.get("Generator_FlowField.rightAngle"),rightAngle);

		add(fieldScaleX);
		fieldScaleX.addPropertyChangeListener(evt->{
			scaleX = (fieldScaleX.getValue());
			generate();

		});
		add(fieldScaleY);
		fieldScaleY.addPropertyChangeListener(evt->{
			scaleY = (fieldScaleY.getValue());
			generate();

		});
		add(fieldOffsetX);
		fieldOffsetX.addPropertyChangeListener(evt->{
			offsetX = (fieldOffsetX.getValue());
			generate();

		});
		add(fieldOffsetY);
		fieldOffsetY.addPropertyChangeListener(evt->{
			offsetY =(fieldOffsetY.getValue());
			generate();

		});
		add(fieldStepSize);
		fieldStepSize.addPropertyChangeListener(evt->{
			stepSize = (fieldStepSize.getValue());
			generate();

		});
		add(fieldFromEdge);
		fieldFromEdge.addPropertyChangeListener(evt->{
			fromEdge = (fieldFromEdge.isSelected());
			generate();
		});
		add(fieldRightAngle);
		fieldRightAngle.addPropertyChangeListener(evt->{
			rightAngle = (fieldRightAngle.isSelected());
			generate();
		});
		add(new SelectReadOnlyText("url","<a href='https://en.wikipedia.org/wiki/Perlin_noise'>"+Translator.get("TurtleGenerators.LearnMore.Link.Text")+"</a>"));
	}

	@Override
	public String getName() {
		return Translator.get("Generator_FlowField.name");
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
		double v = noiseMaker.noise(turtle.getX() * scaleX + offsetX, turtle.getY() * scaleY + offsetY, 0);
		turtle.setAngle(v * 180 + (rightAngle?90:0));
		Vector2d nextStep = turtle.getHeading();
		nextStep.scale(stepSize);
		nextStep.add(turtle.getPosition());
		continueLine(turtle, r,!r.contains(nextStep.x,nextStep.y));
	}

	private void continueLine(Turtle turtle, Rectangle r, boolean reverse) {
		for(int i=0;i<200;++i) {
			double v = noiseMaker.noise(turtle.getX() * scaleX + offsetX, turtle.getY() * scaleY + offsetY, 0);
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
				double v = noiseMaker.noise(x*scaleX + offsetX,y*scaleY+offsetY,0);
				turtle.jumpTo(x,y);
				turtle.setAngle(v*180);
				turtle.forward(stepSize);
			}
		}
	}
}
