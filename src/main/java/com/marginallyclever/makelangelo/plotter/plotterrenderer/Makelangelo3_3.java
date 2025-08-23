package com.marginallyclever.makelangelo.plotter.plotterrenderer;

import com.jogamp.opengl.GL3;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.preview.ShaderProgram;

public class Makelangelo3_3 implements PlotterRenderer {

	@Override
	public void render(ShaderProgram shader, GL3 gl, Plotter robot) {
		paintControlBox(shader,gl,robot);
		Polargraph.paintMotors(shader,gl,robot);
		if(robot.getDidFindHome()) 
			Polargraph.paintPenHolderToCounterweights(shader,gl,robot);
	}
	
	/**
	 * paint the controller and the LCD panel
	 * @param shader the render context
	 * @param robot the machine to draw.
	 */
	private void paintControlBox(ShaderProgram shader, GL3 gl,Plotter robot) {/*
		double cy = robot.getSettings().getDouble(PlotterSettings.LIMIT_TOP);
		double left = robot.getSettings().getDouble(PlotterSettings.LIMIT_LEFT);
		double right = robot.getSettings().getDouble(PlotterSettings.LIMIT_RIGHT);
		double cx = 0;

		gl2.glPushMatrix();
		gl2.glTranslated(cx, cy, 0);
		
		// mounting plate for PCB
		float w =80;
		float h = 50;
		drawRectangle(gl2, h, w, -h, -w,new Color(1,0.8f,0.5f));

		// wires to each motor
		gl2.glBegin(GL3.GL_LINES);
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
		drawRectangle(gl2, h, w, -h, -w,new Color(0.9f,0.9f,0.9f));

		renderLCD(gl2);

		gl2.glPopMatrix();*/
	}
	
	protected void renderLCD(GL3 gl2) {/*
		// position
		gl2.glPushMatrix();
		gl2.glTranslated(-180, 0, 0);
		
		// mounting plate for LCD
		float w = 80f;
		float h = 50f;
		drawRectangle(gl2, h, w, -h, -w,new Color(1,0.8f,0.5f));

		// LCD red
		w = 150f/2;
		h = 56f/2;
		drawRectangle(gl2, h, w, -h, -w,new Color(0.8f,0.0f,0.0f));

		// LCD green
		gl2.glPushMatrix();
		gl2.glTranslated(-(2.6)/2, -0.771, 0);
		
		w = 98f/2;
		h = 60f/2;
		drawRectangle(gl2, h, w, -h, -w,new Color(0,0.6f,0.0f));

		// LCD black
		h = 40f/2;
		drawRectangle(gl2, h, w, -h, -w,new Color(0,0,0));

		// LCD blue
		h = 25f/2;
		w = 75f/2;
		drawRectangle(gl2, h, w, -h, -w,new Color(0,0,0.7f));
		
		gl2.glPopMatrix();

		// clean up
		gl2.glPopMatrix();*/
	}
}
