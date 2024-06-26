package com.marginallyclever.makelangelo.apps.previewpanel.plotterrenderer;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.Mesh;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;

public class Cartesian implements PlotterRenderer {
	public final double CARTESIAN_MOTOR_MOUNT_SIZE =45; //cm
	public final double CARTESIAN_PLOTTER_SIZE=60; //cm
	public final double CARTESIAN_PLOTTER_OUTER_SIZE=70; //cm
	public final double CARTESIAN_PLOTTER_HOLE_SIZE=20; //cm
	public final double CARTESIAN_MOTOR_BODY_SIZE =42; //cm

	public final Mesh meshMotor = new Mesh();
	public final Mesh meshGantry = new Mesh();
	public final Mesh meshControlBox = new Mesh();

	
	@Override
	public void render(GL3 gl,Plotter robot) {
		paintGantryAndHead(gl,robot);		
		paintMotors(gl,robot);
		paintControlBox(gl,robot);
	}

	@Override
	public void updatePlotterSettings(PlotterSettings settings) {

	}

	private void paintGantryAndHead(GL3 gl, Plotter plotter) {
		//double dx, dy;
		Point2D pos = plotter.getPos();
		double gx = pos.x;
		double gy = pos.y;
		double gz = (plotter.getPenIsUp() ? plotter.getSettings().getDouble(PlotterSettings.PEN_ANGLE_UP) : plotter.getSettings().getDouble(PlotterSettings.PEN_ANGLE_DOWN))/10;

		double top = plotter.getSettings().getDouble(PlotterSettings.LIMIT_TOP);
		double bottom = plotter.getSettings().getDouble(PlotterSettings.LIMIT_BOTTOM);
		double left = plotter.getSettings().getDouble(PlotterSettings.LIMIT_LEFT);
		double right = plotter.getSettings().getDouble(PlotterSettings.LIMIT_RIGHT);
// TODO implement me
/*
		gl.glBegin(GL3.GL_QUADS);
		gl.glColor3f(1, 0.8f, 0.5f);
		// Y rail left side
		gl.glVertex2d(left,top);
		gl.glVertex2d(left- CARTESIAN_MOTOR_BODY_SIZE,top);
		gl.glVertex2d(left- CARTESIAN_MOTOR_BODY_SIZE,bottom);
		gl.glVertex2d(left,bottom);
		// Y rail right side
		gl.glVertex2d(right,top);
		gl.glVertex2d(right+ CARTESIAN_MOTOR_BODY_SIZE,top);
		gl.glVertex2d(right+ CARTESIAN_MOTOR_BODY_SIZE,bottom);
		gl.glVertex2d(right,bottom);

		// X rail gantry on top of Y rails
		gl.glColor3f(1, 0.4f, 0.25f);
		gl.glVertex2d(left- CARTESIAN_MOTOR_BODY_SIZE,gy+ CARTESIAN_MOTOR_BODY_SIZE);
		gl.glVertex2d(left- CARTESIAN_MOTOR_BODY_SIZE,gy);
		gl.glVertex2d(right+ CARTESIAN_MOTOR_BODY_SIZE,gy);
		gl.glVertex2d(right+ CARTESIAN_MOTOR_BODY_SIZE,gy+ CARTESIAN_MOTOR_BODY_SIZE);
		gl.glEnd();
		
		gl.glPushMatrix();		gl.glTranslated(right, gy, 0);		gl.glRotated(0, 0, 0, 1);		paintOneMotor(gl);		gl.glPopMatrix();
		gl.glPushMatrix();		gl.glTranslated(gx   , gy, 0);		gl.glRotated(0, 0, 0, 1);		paintOneMotor(gl);		gl.glPopMatrix();

		// gondola on X rail
		gl.glBegin(GL3.GL_LINE_LOOP);
		gl.glColor3f(0, 0, 1);
		float f;
		for (f = 0; f < 2.0 * Math.PI; f += 0.3f) {
			gl.glVertex2d(gx + Math.cos(f) * (4+gz), gy + Math.sin(f) * (4+gz));
		}
		gl.glEnd();*/
	}
	
	protected void paintMotors(GL3 gl,Plotter plotter) {
// TODO implement me
/*
		double top = plotter.getSettings().getDouble(PlotterSettings.LIMIT_TOP);
		double right = plotter.getSettings().getDouble(PlotterSettings.LIMIT_RIGHT);
		double left = plotter.getSettings().getDouble(PlotterSettings.LIMIT_LEFT);

		gl.glPushMatrix();
		gl.glTranslated(left, top, 0);
		gl.glRotated(90, 0, 0, 1);
		paintOneMotor(gl);
		gl.glPopMatrix();
		gl.glPushMatrix();
		gl.glTranslated(right, top, 0);
		gl.glRotated(0, 0, 0, 1);
		paintOneMotor(gl);
		gl.glPopMatrix();*/
	}

	private void paintOneMotor(GL3 gl) {
// TODO implement me
/*
		// motor
		gl.glColor3f(0, 0, 0);
		gl.glBegin(GL3.GL_QUADS);
		gl.glVertex2d(0                        , 0                        );
		gl.glVertex2d(0                        , CARTESIAN_MOTOR_BODY_SIZE);
		gl.glVertex2d(CARTESIAN_MOTOR_BODY_SIZE, CARTESIAN_MOTOR_BODY_SIZE);
		gl.glVertex2d(CARTESIAN_MOTOR_BODY_SIZE, 0                        );
		gl.glVertex2d(0                        , 0                        );
		gl.glEnd();*/
	}
	
	/**
	 * paint the controller and the LCD panel
	 * @param gl the render context
	 * @param plotter the plotter reference for generating the gcode.
	 */
	private void paintControlBox(GL3 gl,Plotter plotter) {
// TODO implement me
/*
		double cy = plotter.getSettings().getDouble(PlotterSettings.LIMIT_TOP);
		double cx = 0;

		gl.glPushMatrix();
		gl.glTranslated(cx, cy+50, 0);

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
