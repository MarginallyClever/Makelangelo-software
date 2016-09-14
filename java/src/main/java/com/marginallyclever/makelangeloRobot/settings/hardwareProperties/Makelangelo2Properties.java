package com.marginallyclever.makelangeloRobot.settings.hardwareProperties;

import com.jogamp.opengl.GL2;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;
import com.marginallyclever.makelangeloRobot.settings.MakelangeloRobotSettings;

public class Makelangelo2Properties implements MakelangeloHardwareProperties {
	public final static float PEN_HOLDER_RADIUS=6; //cm

	@Override
	public int getVersion() {
		return 2;
	}

	@Override
	public String getName() {
		return "Makelangelo 2+";
	}

	@Override
	public boolean canInvertMotors() {
		return true;
	}
	
	@Override
	public boolean canChangeMachineSize() {
		return true;
	}

	@Override
	public boolean canAccelerate() {
		return false;
	}

	@Override
	public boolean canChangePulleySize() {
		return false;
	}

	@Override
	public boolean canAutoHome() {
		return false;
	}

	public float getWidth() { return 3*12*25.4f; }
	public float getHeight() { return 4*12*25.4f; }
	
	@Override
	public void render(GL2 gl2,MakelangeloRobot robot) {
		MakelangeloRobotSettings settings = robot.getSettings();

		paintCalibrationPoint(gl2,settings);
		paintMotors(gl2,settings);
		paintControlBox(gl2,settings);
		paintPenHolderAndCounterweights(gl2,robot);		
	}
	

	// draw left & right motor
	protected void paintMotors( GL2 gl2,MakelangeloRobotSettings settings ) {
		double top = settings.getLimitTop();
		double right = settings.getLimitRight();
		double left = settings.getLimitLeft();
		
		gl2.glColor3f(1,0.8f,0.5f);
		// left frame
		gl2.glPushMatrix();
		//gl2.glTranslatef(-2.1f, 2.1f, 0);
		gl2.glBegin(GL2.GL_TRIANGLE_FAN);
		gl2.glVertex2d(left-5f, top+5f);
		gl2.glVertex2d(left+5f, top+5f);
		gl2.glVertex2d(left+5f, top);
		gl2.glVertex2d(left   , top-5f);
		gl2.glVertex2d(left-5f, top-5f);
		gl2.glEnd();
		gl2.glPopMatrix();

		// right frame
		gl2.glPushMatrix();
		//gl2.glTranslatef(2.1f, 2.1f, 0);
		gl2.glBegin(GL2.GL_TRIANGLE_FAN);
		gl2.glVertex2d(right+5f, top+5f);
		gl2.glVertex2d(right-5f, top+5f);
		gl2.glVertex2d(right-5f, top);
		gl2.glVertex2d(right   , top-5f);
		gl2.glVertex2d(right+5f, top-5f);
		gl2.glEnd();
		gl2.glPopMatrix();

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

	protected void paintControlBox(GL2 gl2,MakelangeloRobotSettings settings) {
		double cy = settings.getLimitTop();
		double left = settings.getLimitLeft();
		double right = settings.getLimitRight();
		double cx = 0;

		gl2.glPushMatrix();
		gl2.glTranslated(cx, cy, 0);
		
		// mounting plate for PCB
		gl2.glColor3f(1,0.8f,0.5f);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(-8, 5);
		gl2.glVertex2d(+8, 5);
		gl2.glVertex2d(+8, -5);
		gl2.glVertex2d(-8, -5);
		gl2.glEnd();
		
		// wires to each motor
		gl2.glBegin(GL2.GL_LINES);
		gl2.glColor3f(1,0,0); 	gl2.glVertex2d(0,-0.3);	gl2.glVertex2d(left,-0.3);
		gl2.glColor3f(0,1,0); 	gl2.glVertex2d(0,-0.1);	gl2.glVertex2d(left,-0.1);
		gl2.glColor3f(0,0,1); 	gl2.glVertex2d(0, 0.1);	gl2.glVertex2d(left, 0.1);
		gl2.glColor3f(1,1,0); 	gl2.glVertex2d(0, 0.3);	gl2.glVertex2d(left, 0.3);
		

		gl2.glColor3f(1,0,0); 	gl2.glVertex2d(0, 0.3);	gl2.glVertex2d(right, 0.3);
		gl2.glColor3f(0,1,0); 	gl2.glVertex2d(0, 0.1);	gl2.glVertex2d(right, 0.1);
		gl2.glColor3f(0,0,1); 	gl2.glVertex2d(0,-0.1);	gl2.glVertex2d(right,-0.1);
		gl2.glColor3f(1,1,0); 	gl2.glVertex2d(0,-0.3);	gl2.glVertex2d(right,-0.3);
		gl2.glEnd();
		
		// UNO
		gl2.glColor3d(0,0,0.6);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(-4, 3);
		gl2.glVertex2d(+4, 3);
		gl2.glVertex2d(+4, -3);
		gl2.glVertex2d(-4, -3);
		gl2.glEnd();

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

		if(gx<left) return;
		if(gx>right) return;
		if(gy>top) return;
		if(gy<bottom) return;
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
		gl2.glVertex2d(left-bottleCenter-0.2, top);
		gl2.glVertex2d(left-bottleCenter-0.2, top-left_b);
		gl2.glVertex2d(left-bottleCenter+0.2, top);
		gl2.glVertex2d(left-bottleCenter+0.2, top-left_b);
		// belt from motor to counterweight right
		gl2.glVertex2d(right+bottleCenter-0.2, top);
		gl2.glVertex2d(right+bottleCenter-0.2, top-right_b);
		gl2.glVertex2d(right+bottleCenter+0.2, top);
		gl2.glVertex2d(right+bottleCenter+0.2, top-right_b);
		gl2.glEnd();
		
		// gondola
		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glColor3f(0, 0, 1);
		float f;
		for(f=0;f<2.0*Math.PI;f+=0.3f) {
			gl2.glVertex2d(gx+Math.cos(f)*PEN_HOLDER_RADIUS,gy+Math.sin(f)*PEN_HOLDER_RADIUS);
		}
		gl2.glEnd();
		
		// counterweight left
		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glColor3f(0, 0, 1);
		gl2.glVertex2d(left-bottleCenter-1.5,top-left_b);
		gl2.glVertex2d(left-bottleCenter+1.5,top-left_b);
		gl2.glVertex2d(left-bottleCenter+1.5,top-left_b-15);
		gl2.glVertex2d(left-bottleCenter-1.5,top-left_b-15);
		gl2.glEnd();
		
		// counterweight right
		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glColor3f(0, 0, 1);
		gl2.glVertex2d(right+bottleCenter-1.5,top-right_b);
		gl2.glVertex2d(right+bottleCenter+1.5,top-right_b);
		gl2.glVertex2d(right+bottleCenter+1.5,top-right_b-15);
		gl2.glVertex2d(right+bottleCenter-1.5,top-right_b-15);
		gl2.glEnd();
		
		/*
		// bottom clearance arcs
		// right
		gl2.glColor3d(0.6, 0.6, 0.6);
		gl2.glBegin(GL2.GL_LINE_STRIP);
		double w = machine.getSettings().getLimitRight() - machine.getSettings().getLimitLeft()+2.1;
		double h = machine.getSettings().getLimitTop() - machine.getSettings().getLimitBottom() + 2.1;
		r=(float)Math.sqrt(h*h + w*w); // circle radius
		gx = machine.getSettings().getLimitLeft() - 2.1;
		gy = machine.getSettings().getLimitTop() + 2.1;
		double start = (float)1.5*(float)Math.PI;
		double end = (2*Math.PI-Math.atan(h/w));
		double v;
		for(v=0;v<=1.0;v+=0.1) {
			double vi = (end-start)*v + start;
			gl2.glVertex2d(gx+Math.cos(vi)*r,gy+Math.sin(vi)*r);
		}
		gl2.glEnd();
		
		// left
		gl2.glBegin(GL2.GL_LINE_STRIP);
		gx = machine.getSettings().getLimitRight() + 2.1;
		start = (float)(1*Math.PI+Math.atan(h/w));
		end = (float)1.5*(float)Math.PI;
		for(v=0;v<=1.0;v+=0.1) {
			double vi = (end-start)*v + start;
			gl2.glVertex2d(gx+Math.cos(vi)*r,gy+Math.sin(vi)*r);
		}
		gl2.glEnd();
		*/
	}


	/**
	 * draw calibration point
	 * @param gl2
	 */
	protected void paintCalibrationPoint(GL2 gl2,MakelangeloRobotSettings settings) {
		gl2.glColor3f(0.8f,0.8f,0.8f);
		gl2.glPushMatrix();
		gl2.glTranslated(settings.getHomeX(), settings.getHomeY(), 0);

		// gondola
		gl2.glBegin(GL2.GL_LINE_LOOP);
		float f;
		for(f=0;f<2.0*Math.PI;f+=0.3f) {
			gl2.glVertex2d(	Math.cos(f)*(PEN_HOLDER_RADIUS+0.1),
							Math.sin(f)*(PEN_HOLDER_RADIUS+0.1)
							);
		}
		gl2.glEnd();

		gl2.glBegin(GL2.GL_LINES);
		gl2.glVertex2f(-0.25f,0.0f);
		gl2.glVertex2f( 0.25f,0.0f);
		gl2.glVertex2f(0.0f,-0.25f);
		gl2.glVertex2f(0.0f, 0.25f);
		gl2.glEnd();
		
		gl2.glPopMatrix();
	}
}
