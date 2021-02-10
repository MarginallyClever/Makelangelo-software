package com.marginallyclever.core.turtle;

import com.jogamp.opengl.GL2;
import com.marginallyclever.core.ColorRGB;

/**
 * Draws Turtle instructions one line segment at a time.
 * @author Dan Royer
 *
 */
public class DefaultTurtleRenderer implements TurtleRenderer {
	protected GL2 gl2;
	
	public ColorRGB colorTravel = new ColorRGB(0,255,0);
	public ColorRGB colorDraw = new ColorRGB(0,0,0);
	public boolean showPenUp = false;
	public float lineWidth=1;
	public float[] lineWidthBuf = new float[1];
	
	public DefaultTurtleRenderer(GL2 gl2, boolean showPenUp) {
		this.gl2=gl2;
		this.showPenUp=showPenUp;
	}
	
	@Override
	public void start() {
		//colorTravel.set(settings.getPenUpColor());
		//colorDraw.set(settings.getPenDownColorDefault());
		float penDiameter = 0.8f;//settings.getPenDiameter();
		
		gl2.glGetFloatv(GL2.GL_LINE_WIDTH, lineWidthBuf, 0);
		lineWidth = lineWidthBuf[0];

		float newDiameter = 2.0f * 100.0f * penDiameter / lineWidth;
		gl2.glLineWidth(newDiameter);
		gl2.glBegin(GL2.GL_LINES);
	}

	@Override
	public void end() {
		gl2.glEnd();
		gl2.glLineWidth(lineWidth);
	}

	@Override
	public void draw(TurtleMove p0, TurtleMove p1) {
		gl2.glColor3d(
				colorDraw.getRed() / 255.0,
				colorDraw.getGreen() / 255.0,
				colorDraw.getBlue() / 255.0);
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
