package com.marginallyclever.makelangeloRobot.generators;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.MakelangeloRobotPanel;

public class Generator_FillPage extends ImageGenerator {
	private static float angle = 0;

	MakelangeloRobotPanel robotPanel;
	

	@Override
	public String getName() {
		return Translator.get("FillPageName");
	}

	static public float getAngle() {
		return angle;
	}
	static public void setAngle(int value) {
		angle = value;
	}
	
	@Override
	public ImageGeneratorPanel getPanel() {
		return new Generator_FillPage_Panel(this);
	}
	
	@Override
	public boolean generate() {
		double majorX = Math.cos(Math.toRadians(angle));
		double majorY = Math.sin(Math.toRadians(angle));

		// figure out how many lines we're going to have on this image.
		float stepSize = machine.getPenDiameter();

		// from top to bottom of the margin area...
		double yBottom = machine.getPaperBottom() * machine.getPaperMargin();
		double yTop    = machine.getPaperTop()    * machine.getPaperMargin();
		double xLeft   = machine.getPaperLeft()   * machine.getPaperMargin();
		double xRight  = machine.getPaperRight()  * machine.getPaperMargin();
		double dy = (yTop - yBottom)/2;
		double dx = (xRight - xLeft)/2;
		double radius = Math.sqrt(dx*dx+dy*dy);

		turtle.reset();
		boolean first=true;
		turtle.penUp();
		
		int i=0;
		for(double a = -radius;a<radius;a+=stepSize) {
			double majorPX = majorX * a;
			double majorPY = majorY * a;
			double startPX = majorPX - majorY * radius;
			double startPY = majorPY + majorX * radius;
			double endPX   = majorPX + majorY * radius;
			double endPY   = majorPY - majorX * radius;

			if(first) {
				turtle.moveTo(startPX,startPY);
				turtle.penDown();
				first=false;
			}
			if ((i % 2) == 0) 	{
				turtle.moveTo(startPX, startPY);
				turtle.moveTo(endPX, endPY);
			} else {
				turtle.moveTo(endPX, endPY);
				turtle.moveTo(startPX, startPY);
			}
			++i;
		}
	    
	    return true;
	}
}
