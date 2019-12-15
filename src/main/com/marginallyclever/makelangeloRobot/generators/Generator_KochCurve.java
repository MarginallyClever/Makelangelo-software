package com.marginallyclever.makelangeloRobot.generators;

import com.marginallyclever.convenience.Turtle;
import com.marginallyclever.makelangelo.Translator;

public class Generator_KochCurve extends ImageGenerator {
	private float xMax = 7;
	private float xMin = -7;
	private float yMax = 7;
	private float yMin = -7;
	private static int order = 4; // controls complexity of curve

	private float maxSize;
	
	@Override
	public String getName() {
		return Translator.get("KochTreeName");
	}

	static public int getOrder() {
		return order;
	}
	static public void setOrder(int order) {
		if(order<1) order=1;
		Generator_KochCurve.order = order;
	}
	
	@Override
	public ImageGeneratorPanel getPanel() {
		return new Generator_KochCurve_Panel(this);
	}
	
	@Override
	public boolean generate() {
		float v = Math.max((float)(machine.getPaperWidth() * machine.getPaperMargin()),
						   (float)(machine.getPaperHeight() * machine.getPaperMargin()))/2.0f;
		xMax = v;
		yMax = v;
		xMin = -v;
		yMin = -v;

		turtle = new Turtle();
		
		float xx = xMax - xMin;
		float yy = yMax - yMin;
		maxSize = xx > yy ? xx : yy;
		
		// move to starting position
		if(machine.getPaperWidth() > machine.getPaperHeight()) {
			turtle.moveTo(-xMax,0);
		} else {
			turtle.moveTo(0,-yMax);
			turtle.turn(90);
		}
		
		turtle.penDown();
		drawTriangle(order, maxSize);
	    
	    return true;
	}


	// L System tree
	private void drawTriangle(int n, float distance) {
		if (n == 0) {
			turtle.forward(distance);
			return;
		}
		drawTriangle(n-1,distance/3.0f);
		if(n>1) {
			turtle.turn(-60);
			drawTriangle(n-1,distance/3.0f);
			turtle.turn(120);
			drawTriangle(n-1,distance/3.0f);
			turtle.turn(-60);
		} else {
			turtle.forward(distance/3.0f);
		}
		drawTriangle(n-1,distance/3.0f);
	}
}
