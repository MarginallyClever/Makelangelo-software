package com.marginallyclever.makelangeloRobot.settings.hardwareProperties;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;
import com.marginallyclever.makelangeloRobot.settings.MakelangeloRobotSettings;

public class Makelangelo2Properties implements MakelangeloHardwareProperties {
	public final static float PEN_HOLDER_RADIUS_2= 60f; // cm
	public final static float MOTOR_SIZE= 21f; // cm
	public final static float PLOTTER_SIZE= 21f; // cm
	public final static float FRAME_SIZE= 50f; // cm
	
	/**
	 * convert from belt length mm to cartesian position.
	 * @param beltL length of belt (mm)
	 * @param beltR length of belt (mm)
	 * @return cartesian coordinate 
	 */
	public Point2D FK(double beltL, double beltR) {
		  double limit_ymax = getHeight()/2;
		  
		  // use law of cosines: theta = acos((a*a+b*b-c*c)/(2*a*b));
		  double a = beltL;
		  double b = getWidth();
		  double c = beltR;

		  // slow, uses trig
		  // we know law of cosines:   cc = aa + bb -2ab * cos( theta )
		  // or cc - aa - bb = -2ab * cos( theta )
		  // or ( aa + bb - cc ) / ( 2ab ) = cos( theta );
		  // or theta = acos((aa+bb-cc)/(2ab));
		  // so  x = cos(theta)*l1 + limit_xmin;
		  // and y = sin(theta)*l1 + limit_ymax;
		  // and we know that cos(acos(i)) = i
		  // and we know that sin(acos(i)) = sqrt(1-i*i)
		  // so y = sin(  acos((aa+bb-cc)/(2ab))  )*l1 + limit_ymax;
		  double theta = ((a*a+b*b-c*c)/(2.0*a*b));
		  
		  double x = theta * a - getWidth()/2;  // theta*a + limit_xmin;
		  /*
		  Serial.print("ymax=");   Serial.println(limit_ymax);
		  Serial.print("theta=");  Serial.println(theta);
		  Serial.print("a=");      Serial.println(a);
		  Serial.print("b=");      Serial.println(b);
		  Serial.print("c=");      Serial.println(c);
		  Serial.print("S0=");     Serial.println(motorStepArray[0]);
		  Serial.print("S1=");     Serial.println(motorStepArray[1]);
		  */
		  double y = limit_ymax - Math.sqrt( 1.0 - theta * theta ) * a;
		  
		  return new Point2D(x,y);
	}
	
	@Override
	public Point2D getHome(MakelangeloRobotSettings settings) {
		return FK(1025,1025);  // default calibration length for M2 belts.
	}
	
	@Override
	public String getVersion() {
		return "2";
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
	public boolean canAutoHome() {
		return false;
	}

	@Override
	public boolean canChangeHome() {
		return false;
	}

	public float getWidth() {
		return 3 * 12 * 25.4f;  // 3'
	}

	public float getHeight() {
		return 4 * 12 * 25.4f;  // 4'
	}

	@Override
	public void render(GL2 gl2, MakelangeloRobot robot) {
		MakelangeloRobotSettings settings = robot.getSettings();

		paintCalibrationPoint(gl2, settings);
		paintMotors(gl2, settings);
		paintControlBox(gl2, settings);
		paintPenHolderToCounterweights(gl2, robot);
	}

	// draw left & right motor
	protected void paintMotors(GL2 gl2, MakelangeloRobotSettings settings) {
		double top = settings.getLimitTop();
		double right = settings.getLimitRight();
		double left = settings.getLimitLeft();

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

	protected void paintControlBox(GL2 gl2, MakelangeloRobotSettings settings) {
		double cy = settings.getLimitTop();
		double left = settings.getLimitLeft();
		double right = settings.getLimitRight();
		double cx = 0;

		gl2.glPushMatrix();
		gl2.glTranslated(cx, cy, 0);

		// mounting plate for PCB
		gl2.glColor3f(1, 0.8f, 0.5f);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(-80, 50);
		gl2.glVertex2d(+80, 50);
		gl2.glVertex2d(+80, -50);
		gl2.glVertex2d(-80, -50);
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

		// UNO
		gl2.glColor3d(0, 0, 0.6);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(-4, 3);
		gl2.glVertex2d(+4, 3);
		gl2.glVertex2d(+4, -3);
		gl2.glVertex2d(-4, -3);
		gl2.glEnd();

		gl2.glPopMatrix();
	}

	protected void paintPenHolderToCounterweights(GL2 gl2, MakelangeloRobot robot) {
		MakelangeloRobotSettings settings = robot.getSettings();
		double dx, dy;
		double gx = robot.getPenX();
		double gy = robot.getPenY();

		double top = settings.getLimitTop();
		double bottom = settings.getLimitBottom();
		double left = settings.getLimitLeft();
		double right = settings.getLimitRight();

		double mw = right - left;
		double mh = top - settings.getLimitBottom();
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
		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glColor3f(0, 0, 1);
		float f;
		for (f = 0; f < 2.0 * Math.PI; f += 0.3f) {
			gl2.glVertex2d(gx + Math.cos(f) * PEN_HOLDER_RADIUS_2, gy + Math.sin(f) * PEN_HOLDER_RADIUS_2);
		}
		gl2.glEnd();

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

		/*
		 * // bottom clearance arcs // right gl2.glColor3d(0.6, 0.6, 0.6);
		 * gl2.glBegin(GL2.GL_LINE_STRIP); double w =
		 * machine.getSettings().getLimitRight() -
		 * machine.getSettings().getLimitLeft()+2.1; double h =
		 * machine.getSettings().getLimitTop() -
		 * machine.getSettings().getLimitBottom() + 2.1; r=(float)Math.sqrt(h*h
		 * + w*w); // circle radius gx = machine.getSettings().getLimitLeft() -
		 * 2.1; gy = machine.getSettings().getLimitTop() + 2.1; double start =
		 * (float)1.5*(float)Math.PI; double end = (2*Math.PI-Math.atan(h/w));
		 * double v; for(v=0;v<=1.0;v+=0.1) { double vi = (end-start)*v + start;
		 * gl2.glVertex2d(gx+Math.cos(vi)*r,gy+Math.sin(vi)*r); } gl2.glEnd();
		 * 
		 * // left gl2.glBegin(GL2.GL_LINE_STRIP); gx =
		 * machine.getSettings().getLimitRight() + 2.1; start =
		 * (float)(1*Math.PI+Math.atan(h/w)); end = (float)1.5*(float)Math.PI;
		 * for(v=0;v<=1.0;v+=0.1) { double vi = (end-start)*v + start;
		 * gl2.glVertex2d(gx+Math.cos(vi)*r,gy+Math.sin(vi)*r); } gl2.glEnd();
		 */
	}

	/**
	 * draw calibration point
	 * 
	 * @param gl2
	 */
	protected void paintCalibrationPoint(GL2 gl2, MakelangeloRobotSettings settings) {
		gl2.glColor3f(0.8f, 0.8f, 0.8f);
		gl2.glPushMatrix();
		gl2.glTranslated(settings.getHomeX(), settings.getHomeY(), 0);

		// gondola
		gl2.glBegin(GL2.GL_LINE_LOOP);
		float f;
		for (f = 0; f < 2.0 * Math.PI; f += 0.3f) {
			gl2.glVertex2d(Math.cos(f) * (PEN_HOLDER_RADIUS_2 + 0.1), Math.sin(f) * (PEN_HOLDER_RADIUS_2 + 0.1));
		}
		gl2.glEnd();

		gl2.glBegin(GL2.GL_LINES);
		gl2.glVertex2f(-0.25f, 0.0f);
		gl2.glVertex2f(0.25f, 0.0f);
		gl2.glVertex2f(0.0f, -0.25f);
		gl2.glVertex2f(0.0f, 0.25f);
		gl2.glEnd();

		gl2.glPopMatrix();
	}

	@Override
	public void writeProgramStart(Writer out) throws IOException {
		SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");  
		Date date = new Date(System.currentTimeMillis());  
		out.write("; Makelangelo 2\n");
		out.write("; "+formatter.format(date)+"\n");
		out.write("M203 U500");  // raise top speed of servo (z axis)
	}

	@Override
	public void writeProgramEnd(Writer out) throws IOException {
		out.write("; Program End\n");
	}

	/**
	 * @since software 7.22.6
	 * @return mm/s [>0]
	 */
	@Override
	public float getFeedrateMax() {
		return 100;
	}
	/**
	 * @since software 7.22.6
	 * @return mm/s [>0]
	 */
	@Override
	public float getFeedrateDefault() {
		return 60;
	}
	
	/**
	 * @since software 7.22.6
	 * @return mm/s^2 [>0]
	 */
	@Override
	public float getAccelerationMax() {
		return 300;
	}

	/**
	 * @since software 7.22.6
	 * @return deg/s [>0]
	 */
	@Override
	public float getZRate() {
		return 500;
	}
	
	/**
	 * @since software 7.22.6
	 * @return deg [0...90] largest angle less than 90 when pen is touching drawing.
	 */
	@Override
	public float getZAngleOn() {
		return 160;
	}
	
	/**
	 * @since software 7.22.6
	 * @return 90 deg.  Middle position on servo. 
	 */
	@Override
	public float getZAngleOff() {
		return 90;
	}

	@Override
	public String getGCodeConfig(MakelangeloRobotSettings settings) {
		String result;
		String xAxis = "M101 A0 T"+StringHelper.formatDouble(settings.getLimitRight())+" B"+StringHelper.formatDouble(settings.getLimitLeft());
		String yAxis = "M101 A1 T"+StringHelper.formatDouble(settings.getLimitTop())+" B"+StringHelper.formatDouble(settings.getLimitBottom());
		String zAxis = "M101 A2 T170 B10";
		result = xAxis+"\n"+yAxis+"\n"+zAxis;
		return result;
	}
}
