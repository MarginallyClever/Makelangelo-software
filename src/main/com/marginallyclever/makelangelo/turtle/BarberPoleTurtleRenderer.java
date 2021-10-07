package com.marginallyclever.makelangelo.turtle;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.makelangelo.preferences.GFXPreferences;

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
	private float lineWidth=1;
	private float[] lineWidthBuf = new float[1];
	private int b=0;
		
	@Override
	public void start(GL2 gl2) {
		this.gl2=gl2;
		showPenUp = GFXPreferences.getShowPenUp();
		//colorTravel.set(settings.getPenUpColor());
		//colorDraw.set(settings.getPenDownColorDefault());
		float penDiameter = 0.8f;//settings.getPenDiameter();
		
		gl2.glGetFloatv(GL2.GL_LINE_WIDTH, lineWidthBuf, 0);
		lineWidth = lineWidthBuf[0];

		float newDiameter = 2.0f * 100.0f * penDiameter / lineWidth;
		gl2.glLineWidth(newDiameter);
		gl2.glBegin(GL2.GL_LINES);
		b=0;
	}

	@Override
	public void end() {
		gl2.glEnd();
		gl2.glLineWidth(lineWidth);
	}
	
	private void setDrawColor() {
		if(b==0)
			gl2.glColor3d(1,0,0);
		//else if(b==1)
//			gl2.glColor3d(1,0,1);
		else
			gl2.glColor3d(0,0,1);
		b=(b+1)%2;
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
}
