package com.marginallyclever.makelangeloRobot.generators;

import com.marginallyclever.convenience.Turtle;
import com.marginallyclever.makelangelo.Translator;

public class Generator_LSystemTree extends ImageGenerator {
	private static int order = 4; // controls complexity of curve
	private static double angleSpan = 120;
	private static int numBranches = 3;
	private static double orderScale = 0.76f;
	
	float maxSize;
	

	@Override
	public String getName() {
		return Translator.get("LSystemTreeName");
	}
	
	@Override
	public ImageGeneratorPanel getPanel() {
		return new Generator_LSystemTree_Panel(this);
	}
	
	@Override
	public boolean generate() {
		turtle = new Turtle();
		
		// move to starting position
		turtle.moveTo(0,-machine.getMarginHeight()/2);
		turtle.turn(90);
		turtle.penDown();
		// do the curve
		lSystemTree(order, 10);

		System.out.print("\n");
		return true;
	}


	// recursive L System tree fractal
	private void lSystemTree(int n, double distance) {
		if (n == 0) return;

		turtle.forward(distance);
		if(n>1) {
			double angleStep = angleSpan / (float)(numBranches-1);

			turtle.turn(-(angleSpan/2.0f));
			for(int i=0;i<numBranches;++i) {
				lSystemTree(n-1,distance*orderScale);
				turtle.turn(angleStep);
			}
			turtle.turn(-(angleSpan/2.0f)-angleStep);
		}
		turtle.forward(-distance);
	}


	public void setOrder(int value) {
		order=value;	
	}
	public int getOrder() {
		return order;
	}

	public void setScale(double value) {
		orderScale = value;
	}
	public double getScale() {
		return orderScale;
	}

	public void setAngle(double value) {
		angleSpan = value;
	}
	public double getAngle() {
		return angleSpan;
	}

	public void setBranches(int value) {
		numBranches = value;
	}
	public int getBranches() {
		return numBranches;
	}
}
