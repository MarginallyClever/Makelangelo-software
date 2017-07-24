package com.marginallyclever.makelangeloRobot.settings.hardwareProperties;

import com.jogamp.opengl.GL2;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;
import com.marginallyclever.makelangeloRobot.settings.MakelangeloRobotSettings;

public class Makelangelo5Properties extends Makelangelo3Properties {
	public final static float PEN_HOLDER_RADIUS_5 = 3; // cm
	public final static double COUNTERWEIGHT_W = 3;
	public final static double COUNTERWEIGHT_H = 6;
	public final static double PULLEY_RADIUS = 0.127;
	public final static double MOTOR_WIDTH = 4.2;

	
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
	public float getHeight() { return 1000; }

	@Override
	public boolean canAutoHome() {
		return true;
	}

	@Override
	public void render(GL2 gl2,MakelangeloRobot robot) {
		MakelangeloRobotSettings settings = robot.getSettings();

		paintCalibrationPoint(gl2,settings);
		paintMotors(gl2,settings);
		paintControlBox(gl2,settings);
		paintPenHolderAndCounterweights(gl2,robot);		
		paintSafeArea(gl2,robot);
	}
	

	// draw left & right motor
	protected void paintMotors( GL2 gl2,MakelangeloRobotSettings settings ) {
		double top = settings.getLimitTop();
		double right = settings.getLimitRight();
		double left = settings.getLimitLeft();
		
		final float SUCTION_CUP_Y=3.5f;

		gl2.glColor3f(1,1f,1f);
		drawCircle(gl2,(float)left -SUCTION_CUP_Y,(float)top-SUCTION_CUP_Y,3.25f);
		drawCircle(gl2,(float)left -SUCTION_CUP_Y,(float)top+SUCTION_CUP_Y,3.25f);
		drawCircle(gl2,(float)right+SUCTION_CUP_Y,(float)top-SUCTION_CUP_Y,3.25f);
		drawCircle(gl2,(float)right+SUCTION_CUP_Y,(float)top+SUCTION_CUP_Y,3.25f);
		
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
		gl2.glVertex2d(left-MOTOR_WIDTH/2, top+MOTOR_WIDTH/2);
		gl2.glVertex2d(left+MOTOR_WIDTH/2, top+MOTOR_WIDTH/2);
		gl2.glVertex2d(left+MOTOR_WIDTH/2, top-MOTOR_WIDTH/2);
		gl2.glVertex2d(left-MOTOR_WIDTH/2, top-MOTOR_WIDTH/2);
		
		// right motor
		gl2.glVertex2d(right-MOTOR_WIDTH/2, top+MOTOR_WIDTH/2);
		gl2.glVertex2d(right+MOTOR_WIDTH/2, top+MOTOR_WIDTH/2);
		gl2.glVertex2d(right+MOTOR_WIDTH/2, top-MOTOR_WIDTH/2);
		gl2.glVertex2d(right-MOTOR_WIDTH/2, top-MOTOR_WIDTH/2);
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
	
	protected void paintPenHolderAndCounterweights( GL2 gl2, MakelangeloRobot robot ) {
		MakelangeloRobotSettings settings = robot.getSettings();
		double dx,dy;
		double gx = robot.getGondolaX() / 10;
		double gy = robot.getGondolaY() / 10;
		
		double top = settings.getLimitTop();
		double bottom = settings.getLimitBottom();
		double left = settings.getLimitLeft();
		double right = settings.getLimitRight();

		if(gx<left  ) return;
		if(gx>right ) return;
		if(gy>top   ) return;
		if(gy<bottom) return;
		
		double mw = right-left;
		double mh = top-settings.getLimitBottom();
		double suggestedLength = Math.sqrt(mw*mw+mh*mh)+5;

		dx = gx - left;
		dy = gy - top;
		double left_a = Math.sqrt(dx*dx+dy*dy);
		double left_b = (suggestedLength - left_a)/2;

		dx = gx - right;
		double right_a = Math.sqrt(dx*dx+dy*dy);
		double right_b = (suggestedLength - right_a)/2;

		gl2.glBegin(GL2.GL_LINES);
		gl2.glColor3d(0.2,0.2,0.2);
		
		// belt from motor to pen holder left
		gl2.glVertex2d(left, top);
		gl2.glVertex2d(gx,gy);
		// belt from motor to pen holder right
		gl2.glVertex2d(right, top);
		gl2.glVertex2d(gx,gy);
		
		float bottleCenter = 0.8f+0.75f;
		
		// belt from motor to counterweight left
		gl2.glVertex2d(left-bottleCenter-PULLEY_RADIUS, top-MOTOR_WIDTH/2);
		gl2.glVertex2d(left-bottleCenter-PULLEY_RADIUS, top-left_b);
		// belt from motor to counterweight right
		gl2.glVertex2d(right+bottleCenter+PULLEY_RADIUS, top-MOTOR_WIDTH/2);
		gl2.glVertex2d(right+bottleCenter+PULLEY_RADIUS, top-right_b);
		gl2.glEnd();
		
		// gondola
		gl2.glColor3f(0, 0, 1);
		drawCircle(gl2,(float)gx,(float)gy,PEN_HOLDER_RADIUS_5);
		
		// counterweight left
		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glColor3f(0, 0, 1);
		gl2.glVertex2d(left-PULLEY_RADIUS-bottleCenter-COUNTERWEIGHT_W/2,top-left_b);
		gl2.glVertex2d(left-PULLEY_RADIUS-bottleCenter+COUNTERWEIGHT_W/2,top-left_b);
		gl2.glVertex2d(left-PULLEY_RADIUS-bottleCenter+COUNTERWEIGHT_W/2,top-left_b-COUNTERWEIGHT_H);
		gl2.glVertex2d(left-PULLEY_RADIUS-bottleCenter-COUNTERWEIGHT_W/2,top-left_b-COUNTERWEIGHT_H);
		gl2.glEnd();
		
		// counterweight right
		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glColor3f(0, 0, 1);
		gl2.glVertex2d(right+PULLEY_RADIUS+bottleCenter-COUNTERWEIGHT_W/2,top-right_b);
		gl2.glVertex2d(right+PULLEY_RADIUS+bottleCenter+COUNTERWEIGHT_W/2,top-right_b);
		gl2.glVertex2d(right+PULLEY_RADIUS+bottleCenter+COUNTERWEIGHT_W/2,top-right_b-COUNTERWEIGHT_H);
		gl2.glVertex2d(right+PULLEY_RADIUS+bottleCenter-COUNTERWEIGHT_W/2,top-right_b-COUNTERWEIGHT_H);
		gl2.glEnd();
	}
	
	protected void drawCircle(GL2 gl2,float x,float y,float r) {
		gl2.glBegin(GL2.GL_LINE_LOOP);
		float f;
		for(f=0;f<2.0*Math.PI;f+=0.3f) {
			gl2.glVertex2d(x+Math.cos(f)*r,y+Math.sin(f)*r);
		}
		gl2.glEnd();
	}
	
	protected void paintSafeArea(GL2 gl2,MakelangeloRobot robot) {
		MakelangeloRobotSettings settings = robot.getSettings();
		double top = settings.getLimitTop();
		//double bottom = settings.getLimitBottom();
		double left = settings.getLimitLeft();
		double right = settings.getLimitRight();

		gl2.glColor4f(0.5f,0.5f,0.75f,0.75f);

		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glVertex2d(left-7f, top+7f);
		gl2.glVertex2d(right+9f, top+7f);
		gl2.glVertex2d(right+9f, top-100);
		gl2.glVertex2d(left-7f, top-100);
		gl2.glEnd();
	}
}
