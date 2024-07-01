package com.marginallyclever.makelangelo.apps.previewpanel.plotterrenderer;

import com.jogamp.opengl.GL3;
import com.marginallyclever.makelangelo.Mesh;
import com.marginallyclever.makelangelo.MeshFactory;
import com.marginallyclever.makelangelo.apps.previewpanel.RenderContext;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;

/**
 * @author Dan Royer
 */
public class Zarplotter implements PlotterRenderer {
	public static final int ZAR_MOTOR_MOUNT_SIZE=45; //cm
	public static final int ZAR_PLOTTER_SIZE=60; //cm
	public static final int ZAR_PLOTTER_OUTER_SIZE=70; //cm
	public static final int ZAR_PLOTTER_HOLE_SIZE=20; //cm
	public static final int NEMA17_SIZE = 42; //cm

	public final Mesh meshQuad = MeshFactory.createMesh();

	public Zarplotter() {
		setupMesh();
	}

	private void setupMesh() {
		meshQuad.setRenderStyle(GL3.GL_QUADS);
		meshQuad.addColor(1,1,1, 1);
		meshQuad.addColor(1,1,1, 1);
		meshQuad.addColor(1,1,1, 1);
		meshQuad.addColor(1,1,1, 1);
		meshQuad.addVertex(0,0,0);
		meshQuad.addVertex(0,1,0);
		meshQuad.addVertex(1,1,0);
		meshQuad.addVertex(1,0,0);
	}

	@Override
	public void render(RenderContext context, Plotter robot) {
		paintMotors(context,robot);
		paintControlBox(context,robot);
		if(robot.getDidFindHome()) 
			paintPenHolderToCounterweights(context,robot);
	}

	@Override
	public void updatePlotterSettings(PlotterSettings settings) {

	}

	private void paintPenHolderToCounterweights(RenderContext context, Plotter robot) {
// TODO implement me
/*
		PlotterSettings settings = robot.getSettings();
		//double dx, dy;
		Point2d pos = robot.getPos();
		double gx = pos.x;
		double gy = pos.y;

		double top = settings.getDouble(PlotterSettings.LIMIT_TOP);
		double bottom = settings.getDouble(PlotterSettings.LIMIT_BOTTOM);
		double left = settings.getDouble(PlotterSettings.LIMIT_LEFT);
		double right = settings.getDouble(PlotterSettings.LIMIT_RIGHT);

		gl.glEnable(GL3.GL_BLEND);
		gl.glBlendFunc(GL3.GL_SRC_ALPHA,GL3.GL_ONE_MINUS_SRC_ALPHA);
		
		gl.glColor4d(0, 0, 0,0.5);
		// plotter
		gl.glPushMatrix();
		gl.glTranslated(gx, gy, 0);
		gl.glBegin(GL3.GL_QUADS);
		gl.glVertex2d(-ZAR_PLOTTER_OUTER_SIZE/2, -ZAR_PLOTTER_OUTER_SIZE/2);
		gl.glVertex2d(-ZAR_PLOTTER_OUTER_SIZE/2, -ZAR_PLOTTER_HOLE_SIZE/2);
		gl.glVertex2d(+ZAR_PLOTTER_OUTER_SIZE/2, -ZAR_PLOTTER_HOLE_SIZE/2);
		gl.glVertex2d(+ZAR_PLOTTER_OUTER_SIZE/2, -ZAR_PLOTTER_OUTER_SIZE/2);

		gl.glVertex2d( ZAR_PLOTTER_HOLE_SIZE/2, -ZAR_PLOTTER_HOLE_SIZE/2);
		gl.glVertex2d( ZAR_PLOTTER_HOLE_SIZE/2, ZAR_PLOTTER_HOLE_SIZE/2);
		gl.glVertex2d(+ZAR_PLOTTER_OUTER_SIZE/2, ZAR_PLOTTER_HOLE_SIZE/2);
		gl.glVertex2d(+ZAR_PLOTTER_OUTER_SIZE/2, -ZAR_PLOTTER_HOLE_SIZE/2);

		gl.glVertex2d(-ZAR_PLOTTER_OUTER_SIZE/2, -ZAR_PLOTTER_HOLE_SIZE/2);
		gl.glVertex2d(-ZAR_PLOTTER_OUTER_SIZE/2, ZAR_PLOTTER_HOLE_SIZE/2);
		gl.glVertex2d(-ZAR_PLOTTER_HOLE_SIZE/2,  ZAR_PLOTTER_HOLE_SIZE/2);
		gl.glVertex2d(-ZAR_PLOTTER_HOLE_SIZE/2, -ZAR_PLOTTER_HOLE_SIZE/2);

		gl.glVertex2d(-ZAR_PLOTTER_OUTER_SIZE/2, +ZAR_PLOTTER_HOLE_SIZE/2);
		gl.glVertex2d(-ZAR_PLOTTER_OUTER_SIZE/2, +ZAR_PLOTTER_OUTER_SIZE/2);
		gl.glVertex2d(+ZAR_PLOTTER_OUTER_SIZE/2, +ZAR_PLOTTER_OUTER_SIZE/2);
		gl.glVertex2d(+ZAR_PLOTTER_OUTER_SIZE/2, +ZAR_PLOTTER_HOLE_SIZE/2);

		gl.glEnd();
		gl.glPopMatrix();

		// belt from motors to plotter
		gl.glBegin(GL3.GL_LINES);
		gl.glVertex2d(left +ZAR_MOTOR_MOUNT_SIZE, top   -ZAR_MOTOR_MOUNT_SIZE);	gl.glVertex2d(gx-ZAR_PLOTTER_SIZE/2, gy+ZAR_PLOTTER_SIZE/2);
		gl.glVertex2d(right-ZAR_MOTOR_MOUNT_SIZE, top   -ZAR_MOTOR_MOUNT_SIZE);	gl.glVertex2d(gx+ZAR_PLOTTER_SIZE/2, gy+ZAR_PLOTTER_SIZE/2);
		gl.glVertex2d(left +ZAR_MOTOR_MOUNT_SIZE, bottom+ZAR_MOTOR_MOUNT_SIZE);	gl.glVertex2d(gx-ZAR_PLOTTER_SIZE/2, gy-ZAR_PLOTTER_SIZE/2);
		gl.glVertex2d(right-ZAR_MOTOR_MOUNT_SIZE, bottom+ZAR_MOTOR_MOUNT_SIZE);	gl.glVertex2d(gx+ZAR_PLOTTER_SIZE/2, gy-ZAR_PLOTTER_SIZE/2);
		gl.glEnd();*/
	}

	private void paintMotors(RenderContext context,Plotter plotter) {
// TODO implement me
/*
		double top = plotter.getSettings().getDouble(PlotterSettings.LIMIT_TOP);
		double bottom = plotter.getSettings().getDouble(PlotterSettings.LIMIT_BOTTOM);
		double right = plotter.getSettings().getDouble(PlotterSettings.LIMIT_RIGHT);
		double left = plotter.getSettings().getDouble(PlotterSettings.LIMIT_LEFT);


		gl.glPushMatrix();		gl.glTranslated(left , top   , 0);		gl.glRotated(270, 0, 0, 1);		paintOneMotor(gl);		gl.glPopMatrix();
		gl.glPushMatrix();		gl.glTranslated(right, top   , 0);		gl.glRotated(180, 0, 0, 1);		paintOneMotor(gl);		gl.glPopMatrix();
		gl.glPushMatrix();		gl.glTranslated(right, bottom, 0);		gl.glRotated( 90, 0, 0, 1);		paintOneMotor(gl);		gl.glPopMatrix();
		gl.glPushMatrix();		gl.glTranslated(left , bottom, 0);		gl.glRotated(  0, 0, 0, 1);		paintOneMotor(gl);		gl.glPopMatrix();*/
	}

	private void paintOneMotor(GL3 gl) {
// TODO implement me
/*
		// frame
		gl.glColor3f(1, 0.8f, 0.5f);
		gl.glBegin(GL3.GL_TRIANGLE_FAN);
		gl.glVertex2d(0                   , 0                   );
		gl.glVertex2d(0                   , ZAR_MOTOR_MOUNT_SIZE);
		gl.glVertex2d(ZAR_MOTOR_MOUNT_SIZE, ZAR_MOTOR_MOUNT_SIZE);
		gl.glVertex2d(ZAR_MOTOR_MOUNT_SIZE, 0                   );
		gl.glVertex2d(0                   , 0                   );
		gl.glEnd();
		*/

		// TODO set color to black?  load NEMA17 motor picture?
		meshQuad.render(gl);
	}
	
	private void paintControlBox(RenderContext context,Plotter plotter) {
// TODO implement me
/*
		double cy = plotter.getSettings().getDouble(PlotterSettings.LIMIT_TOP);
		double cx = 0;

		gl.glPushMatrix();
		gl.glTranslated(cx, cy, 0);

		gl.glScaled(10, 10, 1);
		
		// mounting plate for PCB
		gl.glColor3f(1,0.8f,0.5f);
		gl.glBegin(GL3.GL_QUADS);
		gl.glVertex2d(-8, 5);
		gl.glVertex2d(+8, 5);
		gl.glVertex2d(+8, -5);
		gl.glVertex2d(-8, -5);
		gl.glEnd();
		
		// RUMBA in v3 (135mm*75mm)
		float h = 7.5f/2;
		float w = 13.5f/2;
		gl.glColor3d(0.9,0.9,0.9);
		gl.glBegin(GL3.GL_QUADS);
		gl.glVertex2d(-w, h);
		gl.glVertex2d(+w, h);
		gl.glVertex2d(+w, -h);
		gl.glVertex2d(-w, -h);
		gl.glEnd();

		gl.glPopMatrix();*/
	}
}
