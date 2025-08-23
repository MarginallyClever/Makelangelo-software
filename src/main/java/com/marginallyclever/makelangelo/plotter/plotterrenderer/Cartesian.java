package com.marginallyclever.makelangelo.plotter.plotterrenderer;

import com.jogamp.opengl.GL3;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.preview.ShaderProgram;

public class Cartesian implements PlotterRenderer {
	final public double ZAR_MOTOR_MOUNT_SIZE=45; //cm
	final public double ZAR_PLOTTER_SIZE=60; //cm
	final public double ZAR_PLOTTER_OUTER_SIZE=70; //cm
	final public double ZAR_PLOTTER_HOLE_SIZE=20; //cm
	final public double ZAR_MOTOR_BODY_SIZE=42; //cm
	
	@Override
	public void render(ShaderProgram shader, GL3 gl, Plotter robot) {
		paintGantryAndHead(shader,gl,robot);
		paintMotors(shader,gl,robot);
		paintControlBox(shader,gl,robot);
	}
	
	private void paintGantryAndHead(ShaderProgram shader, GL3 gl, Plotter plotter) {/*
		//double dx, dy;
		Point2d pos = plotter.getPos();
		double gx = pos.x;
		double gy = pos.y;
		double gz = (plotter.getPenIsUp() ? plotter.getSettings().getDouble(PlotterSettings.PEN_ANGLE_UP) : plotter.getSettings().getDouble(PlotterSettings.PEN_ANGLE_DOWN))/10;

		double top = plotter.getSettings().getDouble(PlotterSettings.LIMIT_TOP);
		double bottom = plotter.getSettings().getDouble(PlotterSettings.LIMIT_BOTTOM);
		double left = plotter.getSettings().getDouble(PlotterSettings.LIMIT_LEFT);
		double right = plotter.getSettings().getDouble(PlotterSettings.LIMIT_RIGHT);

		gl2.glBegin(GL3.GL_QUADS);
		gl2.glColor3f(1, 0.8f, 0.5f);
		// left side Y
		gl2.glVertex2d(left,top);
		gl2.glVertex2d(left-ZAR_MOTOR_BODY_SIZE,top);
		gl2.glVertex2d(left-ZAR_MOTOR_BODY_SIZE,bottom);
		gl2.glVertex2d(left,bottom);
		// right side Y
		gl2.glVertex2d(right,top);
		gl2.glVertex2d(right+ZAR_MOTOR_BODY_SIZE,top);
		gl2.glVertex2d(right+ZAR_MOTOR_BODY_SIZE,bottom);
		gl2.glVertex2d(right,bottom);

		// gantry X
		gl2.glColor3f(1, 0.4f, 0.25f);
		gl2.glVertex2d(left-ZAR_MOTOR_BODY_SIZE,gy+ZAR_MOTOR_BODY_SIZE);
		gl2.glVertex2d(left-ZAR_MOTOR_BODY_SIZE,gy);
		gl2.glVertex2d(right+ZAR_MOTOR_BODY_SIZE,gy);
		gl2.glVertex2d(right+ZAR_MOTOR_BODY_SIZE,gy+ZAR_MOTOR_BODY_SIZE);
		gl2.glEnd();
		
		gl2.glPushMatrix();		gl2.glTranslated(right, gy, 0);		gl2.glRotated(0, 0, 0, 1);		paintOneMotor(gl2);		gl2.glPopMatrix();
		gl2.glPushMatrix();		gl2.glTranslated(gx   , gy, 0);		gl2.glRotated(0, 0, 0, 1);		paintOneMotor(gl2);		gl2.glPopMatrix();

		// gondola
		gl2.glBegin(GL3.GL_LINE_LOOP);
		gl2.glColor3f(0, 0, 1);
		float f;
		for (f = 0; f < 2.0 * Math.PI; f += 0.3f) {
			gl2.glVertex2d(gx + Math.cos(f) * (4+gz), gy + Math.sin(f) * (4+gz));
		}
		gl2.glEnd();*/
	}
	
	protected void paintMotors(ShaderProgram shader, GL3 gl,Plotter plotter) {/*
		double top = plotter.getSettings().getDouble(PlotterSettings.LIMIT_TOP);
		double right = plotter.getSettings().getDouble(PlotterSettings.LIMIT_RIGHT);
		double left = plotter.getSettings().getDouble(PlotterSettings.LIMIT_LEFT);

		gl2.glPushMatrix();
		gl2.glTranslated(left, top, 0);
		gl2.glRotated(90, 0, 0, 1);
		paintOneMotor(gl2);
		gl2.glPopMatrix();
		gl2.glPushMatrix();
		gl2.glTranslated(right, top, 0);
		gl2.glRotated(0, 0, 0, 1);
		paintOneMotor(gl2);
		gl2.glPopMatrix();*/
	}

	private void paintOneMotor(GL3 gl2) {/*
		// motor
		gl2.glColor3f(0, 0, 0);
		gl2.glBegin(GL3.GL_QUADS);
		gl2.glVertex2d(0                  , 0                  );
		gl2.glVertex2d(0                  , ZAR_MOTOR_BODY_SIZE);
		gl2.glVertex2d(ZAR_MOTOR_BODY_SIZE, ZAR_MOTOR_BODY_SIZE);
		gl2.glVertex2d(ZAR_MOTOR_BODY_SIZE, 0                  );
		gl2.glVertex2d(0                  , 0                  );
		gl2.glEnd();*/
	}
	
	/**
	 * paint the controller and the LCD panel
	 * @param shader the render context
	 * @param plotter the plotter reference for generating the gcode.
	 */
	private void paintControlBox(ShaderProgram shader, GL3 gl,Plotter plotter) {/*
		double cy = plotter.getSettings().getDouble(PlotterSettings.LIMIT_TOP);
		double cx = 0;

		gl2.glPushMatrix();
		gl2.glTranslated(cx, cy+50, 0);

		gl2.glScaled(10, 10, 1);
		
		// mounting plate for PCB
		gl2.glColor3f(1,0.8f,0.5f);
		gl2.glBegin(GL3.GL_QUADS);
		gl2.glVertex2d(-8, 5);
		gl2.glVertex2d(+8, 5);
		gl2.glVertex2d(+8, -5);
		gl2.glVertex2d(-8, -5);
		gl2.glEnd();
		
		// RUMBA in v3 (135mm*75mm)
		float h = 7.5f/2;
		float w = 13.5f/2;
		gl2.glColor3d(0.9,0.9,0.9);
		gl2.glBegin(GL3.GL_QUADS);
		gl2.glVertex2d(-w, h);
		gl2.glVertex2d(+w, h);
		gl2.glVertex2d(+w, -h);
		gl2.glVertex2d(-w, -h);
		gl2.glEnd();

		gl2.glPopMatrix();*/
	}
}
