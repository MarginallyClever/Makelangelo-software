package com.marginallyclever.makelangelo.plotter.plotterRenderer;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.plotter.Plotter;

public class Cartesian implements PlotterRenderer {
	final public double ZAR_MOTOR_MOUNT_SIZE=45; //cm
	final public double ZAR_PLOTTER_SIZE=60; //cm
	final public double ZAR_PLOTTER_OUTER_SIZE=70; //cm
	final public double ZAR_PLOTTER_HOLE_SIZE=20; //cm
	final public double ZAR_MOTOR_BODY_SIZE=42; //cm
	
	@Override
	public void render(GL2 gl2,Plotter robot) {
		paintGantryAndHead(gl2,robot);		
		paintMotors(gl2,robot);
		paintControlBox(gl2,robot);
	}
	
	private void paintGantryAndHead(GL2 gl2, Plotter robot) {
		//double dx, dy;
		Point2D pos = robot.getPos();
		double gx = pos.x;
		double gy = pos.y;
		double gz = (robot.getPenIsUp() ? robot.getPenUpAngle() : robot.getPenDownAngle())/10;

		double top = robot.getLimitTop();
		double bottom = robot.getLimitBottom();
		double left = robot.getLimitLeft();
		double right = robot.getLimitRight();

		gl2.glBegin(GL2.GL_QUADS);
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
		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glColor3f(0, 0, 1);
		float f;
		for (f = 0; f < 2.0 * Math.PI; f += 0.3f) {
			gl2.glVertex2d(gx + Math.cos(f) * (4+gz), gy + Math.sin(f) * (4+gz));
		}
		gl2.glEnd();
	}
	
	protected void paintMotors(GL2 gl2,Plotter robot) {
		double top = robot.getLimitTop();
		double right = robot.getLimitRight();
		double left = robot.getLimitLeft();

		gl2.glPushMatrix();
		gl2.glTranslated(left, top, 0);
		gl2.glRotated(90, 0, 0, 1);
		paintOneMotor(gl2);
		gl2.glPopMatrix();
		gl2.glPushMatrix();
		gl2.glTranslated(right, top, 0);
		gl2.glRotated(0, 0, 0, 1);
		paintOneMotor(gl2);
		gl2.glPopMatrix();
	}

	private void paintOneMotor(GL2 gl2) {		
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
	
	/**
	 * paint the controller and the LCD panel
	 * @param gl2
	 * @param settings
	 */
	private void paintControlBox(GL2 gl2,Plotter robot) {
		double cy = robot.getLimitTop();
		double cx = 0;

		gl2.glPushMatrix();
		gl2.glTranslated(cx, cy+50, 0);

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
		return "1";
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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getHeight() {
		// TODO Auto-generated method stub
		return 0;
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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getZAngleOff() {
		// TODO Auto-generated method stub
		return 0;
	}
*/
}
