package com.marginallyclever.makelangelo.plotter.plotterrenderer;

import com.jogamp.opengl.GL2;
import com.marginallyclever.makelangelo.plotter.Plotter;

import static com.marginallyclever.convenience.DrawingHelper.drawRectangle;

public class Makelangelo3_3 implements PlotterRenderer {

	@Override
	public void render(GL2 gl2,Plotter robot) {
		paintControlBox(gl2,robot);
		Polargraph.paintMotors(gl2,robot);
		if(robot.getDidFindHome()) 
			Polargraph.paintPenHolderToCounterweights(gl2,robot);		
	}
	
	/**
	 * paint the controller and the LCD panel
	 * @param gl2   the render context
	 * @param robot the machine to draw.
	 */
	private void paintControlBox(GL2 gl2,Plotter robot) {
		double cy = robot.getLimitTop();
		double left = robot.getLimitLeft();
		double right = robot.getLimitRight();
		double cx = 0;

		gl2.glPushMatrix();
		gl2.glTranslated(cx, cy, 0);
		
		// mounting plate for PCB
		gl2.glColor3f(1,0.8f,0.5f);
		float w =80;
		float h = 50;
		drawRectangle(gl2, h, w, -h, -w);

		// wires to each motor
		gl2.glBegin(GL2.GL_LINES);
		final float SPACING=2;
		float y=SPACING*-1.5f;
		gl2.glColor3f(1, 0, 0);		gl2.glVertex2d(left, y);	gl2.glVertex2d(right, y);  y+=SPACING;
		gl2.glColor3f(0, 1, 0);		gl2.glVertex2d(left, y);	gl2.glVertex2d(right, y);  y+=SPACING;
		gl2.glColor3f(0, 0, 1);		gl2.glVertex2d(left, y);	gl2.glVertex2d(right, y);  y+=SPACING;
		gl2.glColor3f(1, 1, 0);		gl2.glVertex2d(left, y);	gl2.glVertex2d(right, y);;
		gl2.glEnd();
		
		// RUMBA in v3 (135mm*75mm)
		h = 75f/2;
		w = 135f/2;
		gl2.glColor3d(0.9,0.9,0.9);
		drawRectangle(gl2, h, w, -h, -w);

		renderLCD(gl2);

		gl2.glPopMatrix();
	}
	
	protected void renderLCD(GL2 gl2) {
		// position
		gl2.glPushMatrix();
		gl2.glTranslated(-180, 0, 0);
		
		// mounting plate for LCD
		float w = 80f;
		float h = 50f;
		gl2.glColor3f(1,0.8f,0.5f);
		drawRectangle(gl2, h, w, -h, -w);

		// LCD red
		w = 150f/2;
		h = 56f/2;
		gl2.glColor3f(0.8f,0.0f,0.0f);
		drawRectangle(gl2, h, w, -h, -w);

		// LCD green
		gl2.glPushMatrix();
		gl2.glTranslated(-(2.6)/2, -0.771, 0);
		
		w = 98f/2;
		h = 60f/2;
		gl2.glColor3f(0,0.6f,0.0f);
		drawRectangle(gl2, h, w, -h, -w);

		// LCD black
		h = 40f/2;
		gl2.glColor3f(0,0,0);
		drawRectangle(gl2, h, w, -h, -w);

		// LCD blue
		h = 25f/2;
		w = 75f/2;
		gl2.glColor3f(0,0,0.7f);
		drawRectangle(gl2, h, w, -h, -w);
		
		gl2.glPopMatrix();

		// clean up
		gl2.glPopMatrix();
	}
}
