package com.marginallyclever.makelangeloRobot.generators;

import java.io.IOException;
import java.io.Writer;
import com.marginallyclever.makelangelo.Translator;

public class Generator_GosperCurve extends ImageGenerator {
	private Turtle turtle;
	private float turtleStep = 10.0f;
	private float xMax = 7;
	private float xMin = -7;
	private float yMax = 7;
	private static int order = 4; // controls complexity of curve
	
	@Override
	public String getName() {
		return Translator.get("GosperCurveName");
	}

	static public int getOrder() {
		return order;
	}
	static public void setOrder(int order) {
		if(order<1) order=1;
		Generator_GosperCurve.order = order;
	}
	
	@Override
	public ImageGeneratorPanel getPanel() {
		return new Generator_GosperCurve_Panel(this);
	}
	
	@Override
	public boolean generate(Writer out) throws IOException {
		imageStart(out);
		liftPen(out);
		machine.writeChangeToDefaultColor(out);

		float v = Math.min((float)(machine.getPaperWidth()  * machine.getPaperMargin()),
				           (float)(machine.getPaperHeight() * machine.getPaperMargin()))/2.0f;
		xMax = v;
		yMax = v;
		xMin = -v;

		turtle = new Turtle();
		turtleStep = 10;//(float) ((v/2) / (Math.pow(2, order)));

		// move to starting position
		turtle.setX(0);//xMax - turtleStep / 2);
		turtle.setY(0);//yMax - turtleStep / 2);
		moveTo(out, turtle.getX(), turtle.getY(), true);
		lowerPen(out);
		// do the curve
		GosperA(out, order);
		liftPen(out);
	    moveTo(out, (float)machine.getHomeX(), (float)machine.getHomeY(),true);
	    
	    return true;
	}


	// Gosper curve A = A-B--B+A++AA+B-
	private void GosperA(Writer output, int n) throws IOException {
		if (n == 0) {
			turtle_goForward(output);
			return;
		}
		GosperA(output,n-1);
		turtle.turn(-60);
		GosperB(output,n-1);
		turtle.turn(-60);
		turtle.turn(-60);
		GosperB(output,n-1);
		turtle.turn(60);
		GosperA(output,n-1);
		turtle.turn(60);
		turtle.turn(60);
		GosperA(output,n-1);
		GosperA(output,n-1);
		turtle.turn(60);
		GosperB(output,n-1);
		turtle.turn(-60);
	}


	// Gosper curve B = +A-BB--B-A++A+B
	public void GosperB(Writer output, int n) throws IOException {
		if (n == 0) {
			turtle_goForward(output);
			return;
		}
		turtle.turn(60);
		GosperA(output,n-1);
		turtle.turn(-60);
		GosperB(output,n-1);
		GosperB(output,n-1);
		turtle.turn(-60);
		turtle.turn(-60);
		GosperB(output,n-1);
		turtle.turn(-60);
		GosperA(output,n-1);
		turtle.turn(60);
		turtle.turn(60);
		GosperA(output,n-1);
		turtle.turn(60);
		GosperB(output,n-1);
	}


	public void turtle_goForward(Writer output) throws IOException {
		//turtle_x += turtle_dx * distance;
		//turtle_y += turtle_dy * distance;
		//output.write(new String("G0 X"+(turtle_x)+" Y"+(turtle_y)+"\n").getBytes());
		turtle.move(turtleStep);
		moveTo(output, turtle.getX(), turtle.getY(), false);
	}
}
