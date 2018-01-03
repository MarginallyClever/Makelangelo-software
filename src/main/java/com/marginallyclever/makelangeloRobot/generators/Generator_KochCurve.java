package com.marginallyclever.makelangeloRobot.generators;

import java.io.IOException;
import java.io.Writer;
import com.marginallyclever.makelangelo.Translator;

public class Generator_KochCurve extends ImageGenerator {
	private Turtle turtle;
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
	public boolean generate(Writer out) throws IOException {
		imageStart(out);
		liftPen(out);
		machine.writeChangeToDefaultColor(out);

		float v = Math.min((float)(machine.getPaperWidth() * machine.getPaperMargin()),
						   (float)(machine.getPaperHeight() * machine.getPaperMargin()))/2.0f;
		xMax = v;
		yMax = v;
		xMin = -v;
		yMin = -v;

		turtle = new Turtle();
		
		float xx = xMax - xMin;
		float yy = yMax - yMin;
		maxSize = xx > yy ? xx : yy;
		
		liftPen(out);
		// move to starting position
		turtle.setX(xMax);
		turtle.setY(0);
		moveTo(out, turtle.getX(), turtle.getY(), true);
		lowerPen(out);
		// do the curve
		turtle.turn(90);
		drawTriangel(out, order, maxSize);
		liftPen(out);
	    moveTo(out, (float)machine.getHomeX(), (float)machine.getHomeY(),true);
	    
	    return true;
	}


	// L System tree
	private void drawTriangel(Writer output, int n, float distance) throws IOException {
		if (n == 0) {
			turtleMove(output,distance);
			return;
		}
		drawTriangel(output,n-1,distance/3.0f);
		if(n>1) {
			turtle.turn(-60);
			drawTriangel(output,n-1,distance/3.0f);
			turtle.turn(120);
			drawTriangel(output,n-1,distance/3.0f);
			turtle.turn(-60);
		} else {
			turtleMove(output,distance/3.0f);
		}
		drawTriangel(output,n-1,distance/3.0f);
	}


	public void turtleMove(Writer output,float distance) throws IOException {
		//turtle_x += turtle_dx * distance;
		//turtle_y += turtle_dy * distance;
		//output.write(new String("G0 X"+(turtle_x)+" Y"+(turtle_y)+"\n").getBytes());
		turtle.move(distance);
		moveTo(output, turtle.getX(), turtle.getY(), false);
	}
}
