package com.marginallyclever.makelangeloRobot.settings.hardwareProperties;

import com.jogamp.opengl.GL2;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;
import com.marginallyclever.makelangeloRobot.settings.MakelangeloRobotSettings;

public class Makelangelo5Properties extends Makelangelo3Properties {
	@Override
	public int getVersion() {
		return 5;
	}
	
	@Override
	public String getName() {
		return "Makelangelo 5+";
	}
	
	@Override
	public boolean canInvertMotors() {
		return false;
	}

	@Override
	public boolean canChangeMachineSize() {
		return false;
	}

	@Override
	public boolean canAccelerate() {
		return true;
	}

	@Override
	public boolean canChangePulleySize() {
		return false;
	}
	
	public float getWidth() { return 650; }
	public float getHeight() { return 1100; }

	@Override
	public boolean canAutoHome() {
		return true;
	}

	@Override
	public void render(GL2 gl2,MakelangeloRobot robot) {
		MakelangeloRobotSettings settings = robot.getSettings();

		super.paintCalibrationPoint(gl2,settings);
		paintMotors(gl2,settings);
		paintControlBox(gl2,settings);
		super.paintPenHolderAndCounterweights(gl2,robot);		
	}
	

	// draw left & right motor
	protected void paintMotors( GL2 gl2,MakelangeloRobotSettings settings ) {
		double top = settings.getLimitTop();
		double right = settings.getLimitRight();
		double left = settings.getLimitLeft();
		
		gl2.glColor3f(1,0.8f,0.5f);
		// frame
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(left-5f, top+5f);
		gl2.glVertex2d(right+5f, top+5f);
		gl2.glVertex2d(right+5f, top-5f);
		gl2.glVertex2d(left-5f, top-5f);
		gl2.glEnd();

		// left motor
		gl2.glColor3f(0,0,0);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(left-2.1f, top+2.1f);
		gl2.glVertex2d(left+2.1f, top+2.1f);
		gl2.glVertex2d(left+2.1f, top-2.1f);
		gl2.glVertex2d(left-2.1f, top-2.1f);
		
		// right motor
		gl2.glVertex2d(right-2.1f, top+2.1f);
		gl2.glVertex2d(right+2.1f, top+2.1f);
		gl2.glVertex2d(right+2.1f, top-2.1f);
		gl2.glVertex2d(right-2.1f, top-2.1f);
		gl2.glEnd();
	}
	
	protected void renderLCD(GL2 gl2) {
		// position
		gl2.glPushMatrix();
		gl2.glTranslated(-18, 0, 0);
		/*
		// mounting plate for LCD
		gl2.glColor3f(1,0.8f,0.5f);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(-8, 5);
		gl2.glVertex2d(+8, 5);
		gl2.glVertex2d(+8, -5);
		gl2.glVertex2d(-8, -5);
		gl2.glEnd();*/

		// LCD red
		float w = 15.0f/2;
		float h = 5.6f/2;
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
		
		w = 9.8f/2;
		h = 6.0f/2;
		gl2.glColor3f(0,0.6f,0.0f);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(-w, h);
		gl2.glVertex2d(+w, h);
		gl2.glVertex2d(+w, -h);
		gl2.glVertex2d(-w, -h);
		gl2.glEnd();

		// LCD black
		h = 4.0f/2;
		gl2.glColor3f(0,0,0);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(-w, h);
		gl2.glVertex2d(+w, h);
		gl2.glVertex2d(+w, -h);
		gl2.glVertex2d(-w, -h);
		gl2.glEnd();

		// LCD blue
		h = 2.5f/2;
		w = 7.5f/2;
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
}
