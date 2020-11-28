package com.marginallyclever.artPipeline.generators;

import com.marginallyclever.convenience.Turtle;
import com.marginallyclever.makelangelo.Translator;

public class Generator_HilbertCurve extends ImageGenerator {
	private float turtleStep = 10.0f;
	private double xMax = 7;
	private double xMin = -7;
	private double yMax = 7;
	private static int order = 4; // controls complexity of curve
	
	@Override
	public String getName() {
		return Translator.get("HilbertCurveName");
	}

	static public int getOrder() {
		return order;
	}
	static public void setOrder(int order) {
		if(order<1) order=1;
		Generator_HilbertCurve.order = order;
	}
	
	@Override
	public ImageGeneratorPanel getPanel() {
		return new Generator_HilbertCurve_Panel(this);
	}
	
	@Override
	public boolean generate() {
		double width=machine.getPaperRight()-machine.getPaperLeft();
		double height=machine.getPaperTop()-machine.getPaperBottom();
		double size=Math.min(width, height);

		turtle = new Turtle();
		turtleStep = (float) (size / (Math.pow(2, order)));

		// move to starting position
		turtle.moveTo(
				  turtleStep / 2-size/2+(machine.getPaperLeft()+machine.getPaperRight())/2 ,
				  turtleStep / 2-size/2+(machine.getPaperTop()+machine.getPaperBottom())/2 );
		

		turtle.penDown();
		hilbert(order);
	    
	    return true;
	}


	// Hilbert curve
	private void hilbert(int n) {
		if (n == 0) return;
		turtle.turn(90);
		treblih( n - 1);
		turtle.forward(turtleStep);
		turtle.turn(-90);
		hilbert( n - 1);
		turtle.forward(turtleStep);
		hilbert( n - 1);
		turtle.turn(-90);
		turtle.forward(turtleStep);
		treblih( n - 1);
		turtle.turn(90);
	}


	// evruc trebliH
	public void treblih(int n) {
		if (n == 0) return;
		turtle.turn(-90);
		hilbert( n - 1);
		turtle.forward(turtleStep);
		turtle.turn(90);
		treblih( n - 1);
		turtle.forward(turtleStep);
		treblih( n - 1);
		turtle.turn(90);
		turtle.forward(turtleStep);
		hilbert( n - 1);
		turtle.turn(-90);
	}
}
