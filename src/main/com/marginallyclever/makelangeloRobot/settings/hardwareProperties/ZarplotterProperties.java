package com.marginallyclever.makelangeloRobot.settings.hardwareProperties;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;
import com.marginallyclever.makelangeloRobot.settings.MakelangeloRobotSettings;

public class ZarplotterProperties extends Makelangelo2Properties {
	final public double ZAR_MOTOR_MOUNT_SIZE=45; //cm
	final public double ZAR_PLOTTER_SIZE=60; //cm
	final public double ZAR_PLOTTER_OUTER_SIZE=70; //cm
	final public double ZAR_PLOTTER_HOLE_SIZE=20; //cm
	final public double ZAR_MOTOR_BODY_SIZE=42; //cm
	
	@Override
	public Point2D getHome(MakelangeloRobotSettings settings) {
		return new Point2D(0,0);
	}
	
	@Override
	public String getVersion() {
		return "6";
	}

	@Override
	public String getName() {
		return "Zarplotter";
	}

	@Override
	public boolean canInvertMotors() {
		return false;
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
	public void render(GL2 gl2,MakelangeloRobot robot) {
		MakelangeloRobotSettings settings = robot.getSettings();

		paintCalibrationPoint(gl2,settings);
		paintMotors(gl2,settings);
		paintControlBox(gl2,settings);
		paintPenHolderToCounterweights(gl2,robot);		
	}

	
	@Override
	protected void paintPenHolderToCounterweights(GL2 gl2, MakelangeloRobot robot) {
		MakelangeloRobotSettings settings = robot.getSettings();
		//double dx, dy;
		double gx = robot.getPenX();
		double gy = robot.getPenY();

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
	
	@Override
	protected void paintCalibrationPoint(GL2 gl2, MakelangeloRobotSettings settings) {
		gl2.glPushMatrix();
		gl2.glTranslated(settings.getHomeX(), settings.getHomeY(), 0);

		gl2.glColor3f(0.8f, 0.8f, 0.8f);
		gl2.glBegin(GL2.GL_LINES);
		gl2.glVertex2f(-0.25f, 0.0f);
		gl2.glVertex2f(0.25f, 0.0f);
		gl2.glVertex2f(0.0f, -0.25f);
		gl2.glVertex2f(0.0f, 0.25f);
		gl2.glEnd();

		gl2.glPopMatrix();
	}

	@Override
	protected void paintMotors(GL2 gl2,MakelangeloRobotSettings settings) {
		double top = settings.getLimitTop();
		double bottom = settings.getLimitBottom();
		double right = settings.getLimitRight();
		double left = settings.getLimitLeft();


		gl2.glPushMatrix();		gl2.glTranslated(left , top   , 0);		gl2.glRotated(270, 0, 0, 1);		paintOneMotor(gl2);		gl2.glPopMatrix();
		gl2.glPushMatrix();		gl2.glTranslated(right, top   , 0);		gl2.glRotated(180, 0, 0, 1);		paintOneMotor(gl2);		gl2.glPopMatrix();
		gl2.glPushMatrix();		gl2.glTranslated(right, bottom, 0);		gl2.glRotated( 90, 0, 0, 1);		paintOneMotor(gl2);		gl2.glPopMatrix();
		gl2.glPushMatrix();		gl2.glTranslated(left , bottom, 0);		gl2.glRotated(  0, 0, 0, 1);		paintOneMotor(gl2);		gl2.glPopMatrix();
	}

	protected void paintOneMotor(GL2 gl2) {
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
	
	/**
	 * paint the controller and the LCD panel
	 * @param gl2
	 * @param settings
	 */
	protected void paintControlBox(GL2 gl2,MakelangeloRobotSettings settings) {
		double cy = settings.getLimitTop();
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

	/**
	 * @return home X coordinate in mm
	 */ 
	public double getHomeX() {
		return 0;
	}
	
	/**
	 * @return home Y coordinate in mm
	 */
	public double getHomeY() {
		return 0;
	}

	@Override
	public void writeProgramStart(Writer out) throws IOException {
		SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");  
		Date date = new Date(System.currentTimeMillis());  
		out.write("; Zarplotter\n");
		out.write("; "+formatter.format(date)+"\n");
		out.write("D0 R-500 L-500 U-500 V-500\n");  // tighten belts a little
		out.write("M203 W500");  // raise top speed of servo (z axis)
	}

	@Override
	public void writeProgramEnd(Writer out) throws IOException {
		super.writeProgramEnd(out);
	}
}
