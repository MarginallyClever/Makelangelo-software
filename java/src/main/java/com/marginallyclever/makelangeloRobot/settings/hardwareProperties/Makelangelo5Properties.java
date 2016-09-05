package com.marginallyclever.makelangeloRobot.settings.hardwareProperties;

import com.jogamp.opengl.GL2;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;
import com.marginallyclever.makelangeloRobot.settings.MakelangeloRobotSettings;

public class Makelangelo5Properties extends Makelangelo3Properties {
	@Override
	public boolean canChangeMachineSize() {
		return false;
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
}
