package com.marginallyclever.makelangelo.apps.previewpanel.plotterrenderer;

import com.jogamp.opengl.GL3;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;

import static com.marginallyclever.convenience.helpers.DrawingHelper.drawRectangle;

public class Makelangelo3_3 implements PlotterRenderer {

	@Override
	public void render(GL3 gl,Plotter robot) {
		paintControlBox(gl,robot);
		Polargraph.paintMotors(gl,robot);
		if(robot.getDidFindHome()) 
			Polargraph.paintPenHolderToCounterweights(gl,robot);		
	}
	
	/**
	 * paint the controller and the LCD panel
	 * @param gl   the render context
	 * @param robot the machine to draw.
	 */
	private void paintControlBox(GL3 gl,Plotter robot) {
		double cy = robot.getSettings().getDouble(PlotterSettings.LIMIT_TOP);
		double left = robot.getSettings().getDouble(PlotterSettings.LIMIT_LEFT);
		double right = robot.getSettings().getDouble(PlotterSettings.LIMIT_RIGHT);
		double cx = 0;

		gl.glPushMatrix();
		gl.glTranslated(cx, cy, 0);
		
		// mounting plate for PCB
		gl.glColor3f(1,0.8f,0.5f);
		float w =80;
		float h = 50;
		drawRectangle(gl, h, w, -h, -w);

		// wires to each motor
		gl.glBegin(GL3.GL_LINES);
		final float SPACING=2;
		float y=SPACING*-1.5f;
		gl.glColor3f(1, 0, 0);		gl.glVertex2d(left, y);	gl.glVertex2d(right, y);  y+=SPACING;
		gl.glColor3f(0, 1, 0);		gl.glVertex2d(left, y);	gl.glVertex2d(right, y);  y+=SPACING;
		gl.glColor3f(0, 0, 1);		gl.glVertex2d(left, y);	gl.glVertex2d(right, y);  y+=SPACING;
		gl.glColor3f(1, 1, 0);		gl.glVertex2d(left, y);	gl.glVertex2d(right, y);;
		gl.glEnd();
		
		// RUMBA in v3 (135mm*75mm)
		h = 75f/2;
		w = 135f/2;
		gl.glColor3d(0.9,0.9,0.9);
		drawRectangle(gl, h, w, -h, -w);

		renderLCD(gl);

		gl.glPopMatrix();
	}
	
	protected void renderLCD(GL3 gl) {
		// position
		gl.glPushMatrix();
		gl.glTranslated(-180, 0, 0);
		
		// mounting plate for LCD
		float w = 80f;
		float h = 50f;
		gl.glColor3f(1,0.8f,0.5f);
		drawRectangle(gl, h, w, -h, -w);

		// LCD red
		w = 150f/2;
		h = 56f/2;
		gl.glColor3f(0.8f,0.0f,0.0f);
		drawRectangle(gl, h, w, -h, -w);

		// LCD green
		gl.glPushMatrix();
		gl.glTranslated(-(2.6)/2, -0.771, 0);
		
		w = 98f/2;
		h = 60f/2;
		gl.glColor3f(0,0.6f,0.0f);
		drawRectangle(gl, h, w, -h, -w);

		// LCD black
		h = 40f/2;
		gl.glColor3f(0,0,0);
		drawRectangle(gl, h, w, -h, -w);

		// LCD blue
		h = 25f/2;
		w = 75f/2;
		gl.glColor3f(0,0,0.7f);
		drawRectangle(gl, h, w, -h, -w);
		
		gl.glPopMatrix();

		// clean up
		gl.glPopMatrix();
	}
}
