package com.marginallyclever.makelangelo.turtle.turtleRenderer;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.makelangelo.makelangeloSettingsPanel.GFXPreferences;
import com.marginallyclever.makelangelo.turtle.TurtleMove;

/**
 * Draws Turtle in red/blue sequence to show line segments.
 * @author Dan Royer
 *
 */
public class BarberPoleTurtleRenderer implements TurtleRenderer {
	private GL2 gl2;
	
	private ColorRGB colorTravel = new ColorRGB(0,255,0);
	private ColorRGB colorDraw = new ColorRGB(0,0,0);
	private boolean showPenUp = false;
	private float originalLineWidth=1;
	private int moveCounter;
		
	@Override
	public void start(GL2 gl2) {
		this.gl2=gl2;
		showPenUp = GFXPreferences.getShowPenUp();
		//colorTravel.set(settings.getPenUpColor());
		//colorDraw.set(settings.getPenDownColorDefault());
		float penDiameter = 0.8f;//settings.getPenDiameter();

		float[] lineWidthBuf = new float[1];
		gl2.glGetFloatv(GL2.GL_LINE_WIDTH, lineWidthBuf, 0);
		originalLineWidth = lineWidthBuf[0];

		setPenDiameter(penDiameter);
		gl2.glBegin(GL2.GL_LINES);
		moveCounter=0;
	}

	@Override
	public void end() {
		gl2.glEnd();
		gl2.glLineWidth(originalLineWidth);
	}
	
	private void setDrawColor() {
		if(moveCounter%2==0) gl2.glColor3d(1,0,0);
		//else if(moveCounter%3==1) gl2.glColor3d(1,0,1);
		else gl2.glColor3d(0,0,1);
		moveCounter++;
		
	}
	
	@Override
	public void draw(TurtleMove p0, TurtleMove p1) {
		setDrawColor();
		gl2.glVertex2d(p0.x, p0.y);
		gl2.glVertex2d(p1.x, p1.y);
	}

	@Override
	public void travel(TurtleMove p0, TurtleMove p1) {
		if(!showPenUp) return;
		
		gl2.glColor3d(
				colorTravel.getRed() / 255.0,
				colorTravel.getGreen() / 255.0,
				colorTravel.getBlue() / 255.0);
		gl2.glVertex2d(p0.x, p0.y);
		gl2.glVertex2d(p1.x, p1.y);
	}

	@Override
	public void setPenDownColor(ColorRGB color) {
		colorDraw.set(color);
	}
	
	@Override
	public void setPenDiameter(double d) {
		float newDiameter = 2.0f * (float)d / originalLineWidth;
		gl2.glLineWidth(newDiameter);
	}
}
