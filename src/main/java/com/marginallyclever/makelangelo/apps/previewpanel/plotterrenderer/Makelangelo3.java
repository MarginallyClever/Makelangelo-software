package com.marginallyclever.makelangelo.apps.previewpanel.plotterrenderer;

import com.jogamp.opengl.GL3;
import com.marginallyclever.makelangelo.apps.previewpanel.RenderContext;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;

/**
 * Deprecated because it cannot find home.
 * @author Dan Royer
 */
@Deprecated
public class Makelangelo3 extends Polargraph implements PlotterRenderer {

	@Override
	public void render(RenderContext context, Plotter robot) {
		paintControlBox(context.gl,robot);
		paintMotors(context,robot);
		if(robot.getDidFindHome()) 
			paintPenHolderToCounterweights(context,robot);
	}

	@Override
	public void updatePlotterSettings(PlotterSettings settings) {

	}

	/**
	 * paint the controller and the LCD panel
	 * @param gl
	 * @param plotter
	 */
	private void paintControlBox(GL3 gl,Plotter plotter) {
// TODO implement me
/*
		double cy = plotter.getSettings().getDouble(PlotterSettings.LIMIT_TOP);
		double left = plotter.getSettings().getDouble(PlotterSettings.LIMIT_LEFT);
		double right = plotter.getSettings().getDouble(PlotterSettings.LIMIT_RIGHT);
		double cx = 0;

		gl.glPushMatrix();
		gl.glTranslated(cx, cy, 0);
		
		// mounting plate for PCB
		gl.glColor3f(1,0.8f,0.5f);
		float w =80;
		float h = 50;
		gl.glBegin(GL3.GL_QUADS);
		gl.glVertex2d(-w, h);
		gl.glVertex2d(+w, h);
		gl.glVertex2d(+w, -h);
		gl.glVertex2d(-w, -h);
		gl.glEnd();

		// wires to each motor
		gl.glBegin(GL3.GL_LINES);
		float SPACING=2f;
		float y=SPACING*-1.5f;
		gl.glColor3f(1, 0, 0);		gl.glVertex2d(0, y);	gl.glVertex2d(left, y);  y+=SPACING;
		gl.glColor3f(0, 1, 0);		gl.glVertex2d(0, y);	gl.glVertex2d(left, y);  y+=SPACING;
		gl.glColor3f(0, 0, 1);		gl.glVertex2d(0, y);	gl.glVertex2d(left, y);  y+=SPACING;
		gl.glColor3f(1, 1, 0);		gl.glVertex2d(0, y);	gl.glVertex2d(left, y);  y+=SPACING;

		y=SPACING*-1.5f;
		gl.glColor3f(1, 0, 0);		gl.glVertex2d(0, y);	gl.glVertex2d(right, y);  y+=SPACING;
		gl.glColor3f(0, 1, 0);		gl.glVertex2d(0, y);	gl.glVertex2d(right, y);  y+=SPACING;
		gl.glColor3f(0, 0, 1);		gl.glVertex2d(0, y);	gl.glVertex2d(right, y);  y+=SPACING;
		gl.glColor3f(1, 1, 0);		gl.glVertex2d(0, y);	gl.glVertex2d(right, y);  y+=SPACING;
		gl.glEnd();
		
		// RUMBA in v3 (135mm*75mm)
		h = 75f/2;
		w = 135f/2;
		gl.glColor3d(0.9,0.9,0.9);
		gl.glBegin(GL3.GL_QUADS);
		gl.glVertex2d(-w, h);
		gl.glVertex2d(+w, h);
		gl.glVertex2d(+w, -h);
		gl.glVertex2d(-w, -h);
		gl.glEnd();

		renderLCD(gl);

		gl.glPopMatrix();*/
	}
	
	private void renderLCD(GL3 gl) {
// TODO implement me
/*
		// position
		gl.glPushMatrix();
		gl.glTranslated(-180, 0, 0);
		
		// mounting plate for LCD
		float w = 80f;
		float h = 50f;
		gl.glColor3f(1,0.8f,0.5f);
		gl.glBegin(GL3.GL_QUADS);
		gl.glVertex2d(-w, h);
		gl.glVertex2d(+w, h);
		gl.glVertex2d(+w, -h);
		gl.glVertex2d(-w, -h);
		gl.glEnd();

		// LCD red
		w = 150f/2;
		h = 56f/2;
		gl.glColor3f(0.8f,0.0f,0.0f);
		gl.glBegin(GL3.GL_QUADS);
		gl.glVertex2d(-w, h);
		gl.glVertex2d(+w, h);
		gl.glVertex2d(+w, -h);
		gl.glVertex2d(-w, -h);
		gl.glEnd();

		// LCD green
		gl.glPushMatrix();
		gl.glTranslated(-(2.6)/2, -0.771, 0);
		
		w = 98f/2;
		h = 60f/2;
		gl.glColor3f(0,0.6f,0.0f);
		gl.glBegin(GL3.GL_QUADS);
		gl.glVertex2d(-w, h);
		gl.glVertex2d(+w, h);
		gl.glVertex2d(+w, -h);
		gl.glVertex2d(-w, -h);
		gl.glEnd();

		// LCD black
		h = 40f/2;
		gl.glColor3f(0,0,0);
		gl.glBegin(GL3.GL_QUADS);
		gl.glVertex2d(-w, h);
		gl.glVertex2d(+w, h);
		gl.glVertex2d(+w, -h);
		gl.glVertex2d(-w, -h);
		gl.glEnd();

		// LCD blue
		h = 25f/2;
		w = 75f/2;
		gl.glColor3f(0,0,0.7f);
		gl.glBegin(GL3.GL_QUADS);
		gl.glVertex2d(-w, h);
		gl.glVertex2d(+w, h);
		gl.glVertex2d(+w, -h);
		gl.glVertex2d(-w, -h);
		gl.glEnd();
		
		gl.glPopMatrix();

		// clean up
		gl.glPopMatrix();*/
	}
}
