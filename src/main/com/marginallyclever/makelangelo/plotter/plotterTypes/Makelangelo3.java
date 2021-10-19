package com.marginallyclever.makelangelo.plotter.plotterTypes;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.plotter.Plotter;

/**
 * Deprecated because it cannot find home.
 * @author Dan Royer
 */
@Deprecated
public class Makelangelo3 extends Polargraph {
	@Override
	public String getVersion() {
		return "3";
	}

	@Override
	public String getName() {
		return "Makelangelo 3.0-3.2";
	}

	@Override
	public Point2D getHome() {
		return new Point2D(0,0);
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
	public boolean canAutoHome() {
		return false;
	}

	@Override
	public boolean canChangeHome() {
		return true;
	}

	@Override
	public void render(GL2 gl2,Plotter robot) {
		paintControlBox(gl2,robot);
		paintMotors(gl2,robot);
		if(robot.getDidFindHome()) 
			paintPenHolderToCounterweights(gl2,robot);		
	}

	protected void paintMotors(GL2 gl2,Plotter robot) {
		double top = robot.getLimitTop();
		double right = robot.getLimitRight();
		double left = robot.getLimitLeft();

		gl2.glColor3f(1, 0.8f, 0.5f);
		// left frame
		gl2.glPushMatrix();
		// gl2.glTranslatef(-2.1f, 2.1f, 0);
		gl2.glBegin(GL2.GL_TRIANGLE_FAN);
		gl2.glVertex2d(left - FRAME_SIZE, top + FRAME_SIZE);
		gl2.glVertex2d(left + FRAME_SIZE, top + FRAME_SIZE);
		gl2.glVertex2d(left + FRAME_SIZE, top             );
		gl2.glVertex2d(left             , top - FRAME_SIZE);
		gl2.glVertex2d(left - FRAME_SIZE, top - FRAME_SIZE);
		gl2.glEnd();
		gl2.glPopMatrix();

		// right frame
		gl2.glPushMatrix();
		// gl2.glTranslatef(2.1f, 2.1f, 0);
		gl2.glBegin(GL2.GL_TRIANGLE_FAN);
		gl2.glVertex2d(right + FRAME_SIZE, top + FRAME_SIZE);
		gl2.glVertex2d(right - FRAME_SIZE, top + FRAME_SIZE);
		gl2.glVertex2d(right - FRAME_SIZE, top             );
		gl2.glVertex2d(right             , top - FRAME_SIZE);
		gl2.glVertex2d(right + FRAME_SIZE, top - FRAME_SIZE);
		gl2.glEnd();
		gl2.glPopMatrix();

		// left motor
		gl2.glColor3f(0, 0, 0);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(left - MOTOR_SIZE, top + MOTOR_SIZE);
		gl2.glVertex2d(left + MOTOR_SIZE, top + MOTOR_SIZE);
		gl2.glVertex2d(left + MOTOR_SIZE, top - MOTOR_SIZE);
		gl2.glVertex2d(left - MOTOR_SIZE, top - MOTOR_SIZE);
		// right motor
		gl2.glVertex2d(right - MOTOR_SIZE, top + MOTOR_SIZE);
		gl2.glVertex2d(right + MOTOR_SIZE, top + MOTOR_SIZE);
		gl2.glVertex2d(right + MOTOR_SIZE, top - MOTOR_SIZE);
		gl2.glVertex2d(right - MOTOR_SIZE, top - MOTOR_SIZE);
		gl2.glEnd();
	}
	
	/**
	 * paint the controller and the LCD panel
	 * @param gl2
	 * @param settings
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
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(-w, h);
		gl2.glVertex2d(+w, h);
		gl2.glVertex2d(+w, -h);
		gl2.glVertex2d(-w, -h);
		gl2.glEnd();

		// wires to each motor
		gl2.glBegin(GL2.GL_LINES);
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
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(-w, h);
		gl2.glVertex2d(+w, h);
		gl2.glVertex2d(+w, -h);
		gl2.glVertex2d(-w, -h);
		gl2.glEnd();

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
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(-w, h);
		gl2.glVertex2d(+w, h);
		gl2.glVertex2d(+w, -h);
		gl2.glVertex2d(-w, -h);
		gl2.glEnd();

		// LCD red
		w = 150f/2;
		h = 56f/2;
		gl2.glColor3f(0.8f,0.0f,0.0f);
		gl2.glBegin(GL2.GL_QUADS);
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
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(-w, h);
		gl2.glVertex2d(+w, h);
		gl2.glVertex2d(+w, -h);
		gl2.glVertex2d(-w, -h);
		gl2.glEnd();

		// LCD black
		h = 40f/2;
		gl2.glColor3f(0,0,0);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(-w, h);
		gl2.glVertex2d(+w, h);
		gl2.glVertex2d(+w, -h);
		gl2.glVertex2d(-w, -h);
		gl2.glEnd();

		// LCD blue
		h = 25f/2;
		w = 75f/2;
		gl2.glColor3f(0,0,0.7f);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(-w, h);
		gl2.glVertex2d(+w, h);
		gl2.glVertex2d(+w, -h);
		gl2.glVertex2d(-w, -h);
		gl2.glEnd();
		
		gl2.glPopMatrix();

		// clean up
		gl2.glPopMatrix();
	}

	/**
	 * @since software 7.22.6
	 * @return mm/s [>0]
	 */
	@Override
	public float getFeedrateMax() {
		return 400;
	}
	/**
	 * @since software 7.22.6
	 * @return mm/s [>0]
	 */
	@Override
	public float getFeedrateDefault() {
		return 100;
	}
	
	/**
	 * @since software 7.22.6
	 * @return mm/s^2 [>0]
	 */
	@Override
	public float getAccelerationMax() {
		return 50;
	}

	/**
	 * @since software 7.22.6
	 * @return deg/s [>0]
	 */
	@Override
	public float getPenLiftTime() {
		return 50;
	}
	
	/**
	 * @since software 7.22.6
	 * @return deg [0...90] largest angle less than 90 when pen is touching drawing.
	 */
	@Override
	public float getZAngleOn() {
		return 40;
	}
}
