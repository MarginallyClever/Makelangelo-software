package com.marginallyclever.artPipeline.generators;

import java.security.SecureRandom;
import com.marginallyclever.convenience.Turtle;
import com.marginallyclever.makelangelo.Translator;

public class Generator_LSystemTree extends ImageGenerator {
	private static int order = 4; // controls complexity of curve
	private static double angleSpan = 120;
	private static int numBranches = 3;
	private static int noise = 0;
	private static double orderScale = 0.76f;
	private SecureRandom random;
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

		random = new SecureRandom();
		random.setSeed(0xDEADBEEF);
		
		// move to starting position
		turtle.moveTo(0,-machine.getMarginHeight()/2);
		turtle.turn(90);
		turtle.penDown();
		// do the curve
		lSystemTree(order, 10);

		return true;
	}


	// recursive L System tree fractal
	private void lSystemTree(int n, double distance) {
		if (n == 0) return;

		turtle.forward(distance);
		if(n>1) {
			double angleStep = angleSpan / (float)(numBranches-1);

			double oldAngle = turtle.getAngle();
			double len = distance*orderScale;
			turtle.turn(-(angleSpan/2.0f));
			for(int i=0;i<numBranches;++i) {
				lSystemTree(n-1,len - len*random.nextDouble()*(noise/100.0f) );
				if(noise>0) {
					turtle.turn(angleStep + (random.nextDouble()-0.5)*(noise/100.0f)*angleStep);
				} else {
					turtle.turn(angleStep);
				}
			}
			turtle.setAngle(oldAngle);
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

	public void setNoise(int value) {
		noise = value;		
	}

	public int getNoise() {
		return noise;		
	}
}
