package com.marginallyclever.makelangeloRobot.generators;

import java.io.IOException;
import java.io.Writer;
import javax.swing.JPanel;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.MakelangeloRobotPanel;

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
	
	private MakelangeloRobotPanel robotPanel;

	float maxSize;

	
	private Turtle turtle;


	@Override
	public String getName() {
		return Translator.get("LSystemTreeName");
	}
	
	@Override
	public JPanel getPanel(MakelangeloRobotPanel arg0) {
		robotPanel = arg0;
		return new Generator_LSystemTree_Panel(this);
	}
	
	@Override
	public void regenerate() {
		robotPanel.regenerate(this);
	}

	@Override
	public boolean generate(Writer out) throws IOException {
		imageStart(out);
		liftPen(out);
		machine.writeChangeToDefaultColor(out);

		float v = Math.min((float)(machine.getPaperWidth() * machine.getPaperMargin()),
				(float)(machine.getPaperHeight() * machine.getPaperMargin())) / 2.0f;
		xMax = v;
		yMax = v;
		xMin = -v;
		yMin = -v;

		turtle = new Turtle();
		
		turtleStep = (float) ((xMax - xMin) / (Math.pow(2, order)));

		float xx = xMax - xMin;
		float yy = yMax - yMin;
		maxSize = xx > yy ? xx : yy;
		
		// move to starting position
		turtle.setX(0);
		turtle.setY(yMax - turtleStep / 2);
		moveTo(out, turtle.getX(), turtle.getY(), true);
		lowerPen(out);
		// do the curve
		lSystemTree(out, order, maxSize/4);
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
