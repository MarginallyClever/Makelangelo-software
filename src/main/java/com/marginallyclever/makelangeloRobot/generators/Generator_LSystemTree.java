package com.marginallyclever.makelangeloRobot.generators;

import java.io.IOException;
import java.io.Writer;
import com.marginallyclever.makelangelo.Translator;

public class Generator_LSystemTree extends ImageGenerator {
	private float xMax = 7;
	private float xMin = -7;
	private float yMax = 7;
	private float yMin = -7;
	private static float turtleStep = 10.0f;
	private static int order = 4; // controls complexity of curve
	private static float angleSpan = 120;
	private static int numBranches = 3;
	private static float orderScale = 0.76f;
	
	float maxSize;

	
	private Turtle turtle;


	@Override
	public String getName() {
		return Translator.get("LSystemTreeName");
	}
	
	@Override
	public ImageGeneratorPanel getPanel() {
		return new Generator_LSystemTree_Panel(this);
	}
	
	@Override
	public boolean generate(Writer out) throws IOException {
		imageStart(out);
		liftPen(out);

		xMax = (float)(machine.getPaperWidth() * machine.getPaperMargin())/ 2.0f;
		yMax = (float)(machine.getPaperHeight() * machine.getPaperMargin())/ 2.0f;
		xMin = -xMax;
		yMin = -yMax;

		turtle = new Turtle();
		

		float xx = xMax - xMin;
		float yy = yMax - yMin;
		maxSize = xx > yy ? xx : yy;
		turtleStep = (float)(yy / order)*0.99f;
		
		// move to starting position
		turtle.setY(-yMax );
		turtle.turn(90);
		moveTo(out, turtle.getX(), turtle.getY(), true);
		lowerPen(out);
		// do the curve
		lSystemTree(out, order, turtleStep);
		liftPen(out);
	    moveTo(out, (float)machine.getHomeX(), (float)machine.getHomeY(),true);
		return true;
	}


	// recursive L System tree fractal
	private void lSystemTree(Writer output, int n, float distance) throws IOException {
		if (n == 0) return;
		// 
		turtleMove(output,distance);
		if(n>1) {
			float angleStep = angleSpan / (float)(numBranches-1);

			turtle.turn(-(angleSpan/2.0f));
			for(int i=0;i<numBranches;++i) {
				lSystemTree(output,n-1,distance*orderScale);
				turtle.turn(angleStep);
			}
			turtle.turn(-(angleSpan/2.0f)-angleStep);

		}
		turtleMove(output,-distance);
	}


	public void turtleMove(Writer output,float distance) throws IOException {
		turtle.move(distance);
		moveTo(output, turtle.getX(), turtle.getY(), false);
	}

	public void setOrder(int value) {
		order=value;	
	}
	public int getOrder() {
		return order;
	}

	public void setScale(float value) {
		orderScale = value;
	}
	public float getScale() {
		return orderScale;
	}

	public void setAngle(float value) {
		angleSpan = value;
	}
	public float getAngle() {
		return angleSpan;
	}

	public void setBranches(int value) {
		numBranches = value;
	}
	public int getBranches() {
		return numBranches;
	}
}
