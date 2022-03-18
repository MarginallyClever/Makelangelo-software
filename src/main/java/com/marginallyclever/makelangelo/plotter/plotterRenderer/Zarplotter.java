package com.marginallyclever.makelangelo.plotter.plotterRenderer;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.settings.PlotterSettings;

/**
 * @author Dan Royer
 */
public class Zarplotter implements PlotterRenderer {
	final public double ZAR_MOTOR_MOUNT_SIZE=45; //cm
	final public double ZAR_PLOTTER_SIZE=60; //cm
	final public double ZAR_PLOTTER_OUTER_SIZE=70; //cm
	final public double ZAR_PLOTTER_HOLE_SIZE=20; //cm
	final public double ZAR_MOTOR_BODY_SIZE=42; //cm
	
	@Override
	public void render(GL2 gl2,Plotter robot) {
		paintMotors(gl2,robot);
		paintControlBox(gl2,robot);
		if(robot.getDidFindHome()) 
			paintPenHolderToCounterweights(gl2,robot);		
	}

	private void paintPenHolderToCounterweights(GL2 gl2, Plotter robot) {
		PlotterSettings settings = robot.getSettings();
		//double dx, dy;
		Point2D pos = robot.getPos();
		double gx = pos.x;
		double gy = pos.y;

		double top = settings.getLimitTop();
		double bottom = settings.getLimitBottom();
		double left = settings.getLimitLeft();
		double right = settings.getLimitRight();

		gl2.glEnable(GL2.GL_BLEND);
		gl2.glBlendFunc(GL2.GL_SRC_ALPHA,GL2.GL_ONE_MINUS_SRC_ALPHA);
		
		gl2.glColor4d(0, 0, 0,0.5);
		// plotter
		gl2.glPushMatrix();
		gl2.glTranslated(gx, gy, 0);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(-ZAR_PLOTTER_OUTER_SIZE/2, -ZAR_PLOTTER_OUTER_SIZE/2);
		gl2.glVertex2d(-ZAR_PLOTTER_OUTER_SIZE/2, -ZAR_PLOTTER_HOLE_SIZE/2);
		gl2.glVertex2d(+ZAR_PLOTTER_OUTER_SIZE/2, -ZAR_PLOTTER_HOLE_SIZE/2);
		gl2.glVertex2d(+ZAR_PLOTTER_OUTER_SIZE/2, -ZAR_PLOTTER_OUTER_SIZE/2);

		gl2.glVertex2d( ZAR_PLOTTER_HOLE_SIZE/2, -ZAR_PLOTTER_HOLE_SIZE/2);
		gl2.glVertex2d( ZAR_PLOTTER_HOLE_SIZE/2, ZAR_PLOTTER_HOLE_SIZE/2);
		gl2.glVertex2d(+ZAR_PLOTTER_OUTER_SIZE/2, ZAR_PLOTTER_HOLE_SIZE/2);
		gl2.glVertex2d(+ZAR_PLOTTER_OUTER_SIZE/2, -ZAR_PLOTTER_HOLE_SIZE/2);

		gl2.glVertex2d(-ZAR_PLOTTER_OUTER_SIZE/2, -ZAR_PLOTTER_HOLE_SIZE/2);
		gl2.glVertex2d(-ZAR_PLOTTER_OUTER_SIZE/2, ZAR_PLOTTER_HOLE_SIZE/2);
		gl2.glVertex2d(-ZAR_PLOTTER_HOLE_SIZE/2,  ZAR_PLOTTER_HOLE_SIZE/2);
		gl2.glVertex2d(-ZAR_PLOTTER_HOLE_SIZE/2, -ZAR_PLOTTER_HOLE_SIZE/2);

		gl2.glVertex2d(-ZAR_PLOTTER_OUTER_SIZE/2, +ZAR_PLOTTER_HOLE_SIZE/2);
		gl2.glVertex2d(-ZAR_PLOTTER_OUTER_SIZE/2, +ZAR_PLOTTER_OUTER_SIZE/2);
		gl2.glVertex2d(+ZAR_PLOTTER_OUTER_SIZE/2, +ZAR_PLOTTER_OUTER_SIZE/2);
		gl2.glVertex2d(+ZAR_PLOTTER_OUTER_SIZE/2, +ZAR_PLOTTER_HOLE_SIZE/2);

		gl2.glEnd();
		gl2.glPopMatrix();

		// belt from motors to plotter
		gl2.glBegin(GL2.GL_LINES);
		gl2.glVertex2d(left +ZAR_MOTOR_MOUNT_SIZE, top   -ZAR_MOTOR_MOUNT_SIZE);	gl2.glVertex2d(gx-ZAR_PLOTTER_SIZE/2, gy+ZAR_PLOTTER_SIZE/2);
		gl2.glVertex2d(right-ZAR_MOTOR_MOUNT_SIZE, top   -ZAR_MOTOR_MOUNT_SIZE);	gl2.glVertex2d(gx+ZAR_PLOTTER_SIZE/2, gy+ZAR_PLOTTER_SIZE/2);
		gl2.glVertex2d(left +ZAR_MOTOR_MOUNT_SIZE, bottom+ZAR_MOTOR_MOUNT_SIZE);	gl2.glVertex2d(gx-ZAR_PLOTTER_SIZE/2, gy-ZAR_PLOTTER_SIZE/2);
		gl2.glVertex2d(right-ZAR_MOTOR_MOUNT_SIZE, bottom+ZAR_MOTOR_MOUNT_SIZE);	gl2.glVertex2d(gx+ZAR_PLOTTER_SIZE/2, gy-ZAR_PLOTTER_SIZE/2);
		gl2.glEnd();
	}

	private void paintMotors(GL2 gl2,Plotter robot) {
		double top = robot.getLimitTop();
		double bottom = robot.getLimitBottom();
		double right = robot.getLimitRight();
		double left = robot.getLimitLeft();


		gl2.glPushMatrix();		gl2.glTranslated(left , top   , 0);		gl2.glRotated(270, 0, 0, 1);		paintOneMotor(gl2);		gl2.glPopMatrix();
		gl2.glPushMatrix();		gl2.glTranslated(right, top   , 0);		gl2.glRotated(180, 0, 0, 1);		paintOneMotor(gl2);		gl2.glPopMatrix();
		gl2.glPushMatrix();		gl2.glTranslated(right, bottom, 0);		gl2.glRotated( 90, 0, 0, 1);		paintOneMotor(gl2);		gl2.glPopMatrix();
		gl2.glPushMatrix();		gl2.glTranslated(left , bottom, 0);		gl2.glRotated(  0, 0, 0, 1);		paintOneMotor(gl2);		gl2.glPopMatrix();
	}

	private void paintOneMotor(GL2 gl2) {
		// frame
		gl2.glColor3f(1, 0.8f, 0.5f);
		gl2.glBegin(GL2.GL_TRIANGLE_FAN);
		gl2.glVertex2d(0                   , 0                   );
		gl2.glVertex2d(0                   , ZAR_MOTOR_MOUNT_SIZE);
		gl2.glVertex2d(ZAR_MOTOR_MOUNT_SIZE, ZAR_MOTOR_MOUNT_SIZE);
		gl2.glVertex2d(ZAR_MOTOR_MOUNT_SIZE, 0                   );
		gl2.glVertex2d(0                   , 0                   );
		gl2.glEnd();
		
		// motor
		gl2.glColor3f(0, 0, 0);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(0                  , 0                  );
		gl2.glVertex2d(0                  , ZAR_MOTOR_BODY_SIZE);
		gl2.glVertex2d(ZAR_MOTOR_BODY_SIZE, ZAR_MOTOR_BODY_SIZE);
		gl2.glVertex2d(ZAR_MOTOR_BODY_SIZE, 0                  );
		gl2.glVertex2d(0                  , 0                  );
		gl2.glEnd();
	}
	
	private void paintControlBox(GL2 gl2,Plotter robot) {
		double cy = robot.getLimitTop();
		double cx = 0;

		gl2.glPushMatrix();
		gl2.glTranslated(cx, cy, 0);

		gl2.glScaled(10, 10, 1);
		
		// mounting plate for PCB
		gl2.glColor3f(1,0.8f,0.5f);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(-8, 5);
		gl2.glVertex2d(+8, 5);
		gl2.glVertex2d(+8, -5);
		gl2.glVertex2d(-8, -5);
		gl2.glEnd();
		
		// RUMBA in v3 (135mm*75mm)
		float h = 7.5f/2;
		float w = 13.5f/2;
		gl2.glColor3d(0.9,0.9,0.9);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(-w, h);
		gl2.glVertex2d(+w, h);
		gl2.glVertex2d(+w, -h);
		gl2.glVertex2d(-w, -h);
		gl2.glEnd();

		gl2.glPopMatrix();
	}
/*
	@Override
	public Point2D getHome() {
		return new Point2D(0,0);
	}
	
	@Override
	public String getVersion() {
		return "6";
	}

	@Override
	public boolean canAutoHome() {
		return false;
	}

	@Override
	public boolean canChangeMachineSize() {
		return true;
	}

	@Override
	public boolean canAccelerate() {
		return true;
	}

	@Override
	public boolean canChangeHome() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public float getWidth() {
		return 3 * 12 * 25.4f;  // 3'
	}

	@Override
	public float getHeight() {
		return 3 * 12 * 25.4f;  // 3'
	}

	@Override
	public float getFeedrateMax() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getFeedrateDefault() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getAccelerationMax() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getPenLiftTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getZAngleOn() {
		return 0;
	}

	@Override
	public float getZAngleOff() {
		return 90;
	}*/
}
