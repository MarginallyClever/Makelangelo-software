package com.marginallyclever.makelangelo.plotter.plotterTypes;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.plotter.Plotter;

public class Makelangelo3_3 implements PlotterType {
	@Override
	public String getName() {
		return "Makelangelo 3.3";
	}

	@Override
	public void render(GL2 gl2,Plotter robot) {
		paintControlBox(gl2,robot);
		paintMotors(gl2,robot);
		if(robot.getDidFindHome()) 
			paintPenHolderToCounterweights(gl2,robot);		
	}

	private void paintPenHolderToCounterweights(GL2 gl2, Plotter robot) {
		double dx, dy;
		Point2D pos = robot.getPos();
		double gx = pos.x;
		double gy = pos.y;

		double top = robot.getLimitTop();
		double bottom = robot.getLimitBottom();
		double left = robot.getLimitLeft();
		double right = robot.getLimitRight();

		double mw = right - left;
		double mh = top - bottom;
		double suggestedLength = Math.sqrt(mw * mw + mh * mh) + 50;

		dx = gx - left;
		dy = gy - top;
		double left_a = Math.sqrt(dx * dx + dy * dy);
		double left_b = (suggestedLength - left_a) / 2;

		dx = gx - right;
		double right_a = Math.sqrt(dx * dx + dy * dy);
		double right_b = (suggestedLength - right_a) / 2;

		if (gx < left)
			return;
		if (gx > right)
			return;
		if (gy > top)
			return;
		if (gy < bottom)
			return;
		gl2.glBegin(GL2.GL_LINES);
		gl2.glColor3d(0.2, 0.2, 0.2);

		// belt from motor to pen holder left
		gl2.glVertex2d(left, top);
		gl2.glVertex2d(gx, gy);
		// belt from motor to pen holder right
		gl2.glVertex2d(right, top);
		gl2.glVertex2d(gx, gy);

		float bottleCenter = 8f + 7.5f;

		// belt from motor to counterweight left
		gl2.glVertex2d(left - bottleCenter - 2, top);
		gl2.glVertex2d(left - bottleCenter - 2, top - left_b);
		gl2.glVertex2d(left - bottleCenter + 2, top);
		gl2.glVertex2d(left - bottleCenter + 2, top - left_b);
		// belt from motor to counterweight right
		gl2.glVertex2d(right + bottleCenter - 2, top);
		gl2.glVertex2d(right + bottleCenter - 2, top - right_b);
		gl2.glVertex2d(right + bottleCenter + 2, top);
		gl2.glVertex2d(right + bottleCenter + 2, top - right_b);
		gl2.glEnd();

		// gondola
		Polargraph.drawCircle(gl2,gx,gy,Polargraph.PEN_HOLDER_RADIUS_2,20);
		if(robot.getPenIsUp()) {
			Polargraph.drawCircle(gl2,gx,gy,Polargraph.PEN_HOLDER_RADIUS_2+5,20);
		}

		// counterweight left
		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glColor3f(0, 0, 1);
		gl2.glVertex2d(left - bottleCenter - 15, top - left_b);
		gl2.glVertex2d(left - bottleCenter + 15, top - left_b);
		gl2.glVertex2d(left - bottleCenter + 15, top - left_b - 150);
		gl2.glVertex2d(left - bottleCenter - 15, top - left_b - 150);
		gl2.glEnd();

		// counterweight right
		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glColor3f(0, 0, 1);
		gl2.glVertex2d(right + bottleCenter - 15, top - right_b);
		gl2.glVertex2d(right + bottleCenter + 15, top - right_b);
		gl2.glVertex2d(right + bottleCenter + 15, top - right_b - 150);
		gl2.glVertex2d(right + bottleCenter - 15, top - right_b - 150);
		gl2.glEnd();
	}

	protected void paintMotors(GL2 gl2,Plotter robot) {
		double top = robot.getLimitTop();
		double right = robot.getLimitRight();
		double left = robot.getLimitLeft();

		// left motor
		gl2.glColor3f(0, 0, 0);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(left - Polargraph.MOTOR_SIZE, top + Polargraph.MOTOR_SIZE);
		gl2.glVertex2d(left + Polargraph.MOTOR_SIZE, top + Polargraph.MOTOR_SIZE);
		gl2.glVertex2d(left + Polargraph.MOTOR_SIZE, top - Polargraph.MOTOR_SIZE);
		gl2.glVertex2d(left - Polargraph.MOTOR_SIZE, top - Polargraph.MOTOR_SIZE);
		// right motor
		gl2.glVertex2d(right - Polargraph.MOTOR_SIZE, top + Polargraph.MOTOR_SIZE);
		gl2.glVertex2d(right + Polargraph.MOTOR_SIZE, top + Polargraph.MOTOR_SIZE);
		gl2.glVertex2d(right + Polargraph.MOTOR_SIZE, top - Polargraph.MOTOR_SIZE);
		gl2.glVertex2d(right - Polargraph.MOTOR_SIZE, top - Polargraph.MOTOR_SIZE);
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
/*
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
		return true;
	}

	@Override
	public boolean canChangeHome() {
		return true;
	}

	@Override
	public Point2D getHome() {
		return new Point2D(0,0);
	}
	*/
}
