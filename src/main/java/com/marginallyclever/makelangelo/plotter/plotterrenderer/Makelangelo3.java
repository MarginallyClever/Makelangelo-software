package com.marginallyclever.makelangelo.plotter.plotterrenderer;

import com.jogamp.opengl.GL3;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.preview.ShaderProgram;

/**
 * Deprecated because it cannot find home.
 * @author Dan Royer
 */
@Deprecated
public class Makelangelo3 implements PlotterRenderer {

	@Override
	public void render(ShaderProgram shader, Plotter robot) {
		paintControlBox(shader,robot);
		Polargraph.paintMotors(shader,robot);
		if(robot.getDidFindHome()) 
			Polargraph.paintPenHolderToCounterweights(shader,robot);
	}

	/**
	 * paint the controller and the LCD panel
	 * @param shader the shader to use for rendering
	 * @param plotter the plotter to render
	 */
	private void paintControlBox(ShaderProgram shader,Plotter plotter) {/*
		double cy = plotter.getSettings().getDouble(PlotterSettings.LIMIT_TOP);
		double left = plotter.getSettings().getDouble(PlotterSettings.LIMIT_LEFT);
		double right = plotter.getSettings().getDouble(PlotterSettings.LIMIT_RIGHT);
		double cx = 0;

		gl2.glPushMatrix();
		gl2.glTranslated(cx, cy, 0);
		
		// mounting plate for PCB
		gl2.glColor3f(1,0.8f,0.5f);
		float w =80;
		float h = 50;
		gl2.glBegin(GL3.GL_QUADS);
		gl2.glVertex2d(-w, h);
		gl2.glVertex2d(+w, h);
		gl2.glVertex2d(+w, -h);
		gl2.glVertex2d(-w, -h);
		gl2.glEnd();

		// wires to each motor
		gl2.glBegin(GL3.GL_LINES);
		float SPACING=2f;
		float y=SPACING*-1.5f;
		gl2.glColor3f(1, 0, 0);		gl2.glVertex2d(0, y);	gl2.glVertex2d(left, y);  y+=SPACING;
		gl2.glColor3f(0, 1, 0);		gl2.glVertex2d(0, y);	gl2.glVertex2d(left, y);  y+=SPACING;
		gl2.glColor3f(0, 0, 1);		gl2.glVertex2d(0, y);	gl2.glVertex2d(left, y);  y+=SPACING;
		gl2.glColor3f(1, 1, 0);		gl2.glVertex2d(0, y);	gl2.glVertex2d(left, y);  y+=SPACING;

		y=SPACING*-1.5f;
		gl2.glColor3f(1, 0, 0);		gl2.glVertex2d(0, y);	gl2.glVertex2d(right, y);  y+=SPACING;
		gl2.glColor3f(0, 1, 0);		gl2.glVertex2d(0, y);	gl2.glVertex2d(right, y);  y+=SPACING;
		gl2.glColor3f(0, 0, 1);		gl2.glVertex2d(0, y);	gl2.glVertex2d(right, y);  y+=SPACING;
		gl2.glColor3f(1, 1, 0);		gl2.glVertex2d(0, y);	gl2.glVertex2d(right, y);  y+=SPACING;
		gl2.glEnd();
		
		// RUMBA in v3 (135mm*75mm)
		h = 75f/2;
		w = 135f/2;
		gl2.glColor3d(0.9,0.9,0.9);
		gl2.glBegin(GL3.GL_QUADS);
		gl2.glVertex2d(-w, h);
		gl2.glVertex2d(+w, h);
		gl2.glVertex2d(+w, -h);
		gl2.glVertex2d(-w, -h);
		gl2.glEnd();

		renderLCD(gl2);

		gl2.glPopMatrix();*/
	}
	
	private void renderLCD(GL3 gl2) {/*
		// position
		gl2.glPushMatrix();
		gl2.glTranslated(-180, 0, 0);
		
		// mounting plate for LCD
		float w = 80f;
		float h = 50f;
		gl2.glColor3f(1,0.8f,0.5f);
		gl2.glBegin(GL3.GL_QUADS);
		gl2.glVertex2d(-w, h);
		gl2.glVertex2d(+w, h);
		gl2.glVertex2d(+w, -h);
		gl2.glVertex2d(-w, -h);
		gl2.glEnd();

		// LCD red
		w = 150f/2;
		h = 56f/2;
		gl2.glColor3f(0.8f,0.0f,0.0f);
		gl2.glBegin(GL3.GL_QUADS);
		gl2.glVertex2d(-w, h);
		gl2.glVertex2d(+w, h);
		gl2.glVertex2d(+w, -h);
		gl2.glVertex2d(-w, -h);
		gl2.glEnd();

		// LCD green
		gl2.glPushMatrix();
		gl2.glTranslated(-(2.6)/2, -0.771, 0);
		
		w = 98f/2;
		h = 60f/2;
		gl2.glColor3f(0,0.6f,0.0f);
		gl2.glBegin(GL3.GL_QUADS);
		gl2.glVertex2d(-w, h);
		gl2.glVertex2d(+w, h);
		gl2.glVertex2d(+w, -h);
		gl2.glVertex2d(-w, -h);
		gl2.glEnd();

		// LCD black
		h = 40f/2;
		gl2.glColor3f(0,0,0);
		gl2.glBegin(GL3.GL_QUADS);
		gl2.glVertex2d(-w, h);
		gl2.glVertex2d(+w, h);
		gl2.glVertex2d(+w, -h);
		gl2.glVertex2d(-w, -h);
		gl2.glEnd();

		// LCD blue
		h = 25f/2;
		w = 75f/2;
		gl2.glColor3f(0,0,0.7f);
		gl2.glBegin(GL3.GL_QUADS);
		gl2.glVertex2d(-w, h);
		gl2.glVertex2d(+w, h);
		gl2.glVertex2d(+w, -h);
		gl2.glVertex2d(-w, -h);
		gl2.glEnd();
		
		gl2.glPopMatrix();

		// clean up
		gl2.glPopMatrix();*/
	}
}
