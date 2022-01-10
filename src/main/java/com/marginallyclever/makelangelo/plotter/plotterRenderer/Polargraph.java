package com.marginallyclever.makelangelo.plotter.plotterRenderer;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.plotter.Plotter;

/**
 * @author Dan Royer
 */
public abstract class Polargraph implements PlotterRenderer {
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
	public Point2D FK(Plotter plotter,double beltL, double beltR) {
		double limit_ymax = plotter.getLimitTop();
		double right = plotter.getLimitRight();
		double left = plotter.getLimitLeft();

		// use law of cosines: theta = acos((a*a+b*b-c*c)/(2*a*b));
		double a = beltL;
		double b = right-left;
		double c = beltR;

		double theta = (a*a + b*b - c*c) / (2.0*a*b);

		double x = theta * a - b/2;
		double y = limit_ymax - Math.sqrt(1.0 - theta * theta) * a;

		return new Point2D(x, y);
	}
		
	/**
	 * convert from cartesian space to belt lengths.
	 * @param x
	 * @param y
	 * @return Point2D with x=belt left and y=belt right.
	 */
	public Point2D IK(Plotter plotter,double x,double y) {
		double right = plotter.getLimitRight();
		double left = plotter.getLimitLeft();
		double top = plotter.getLimitTop();
		
		double dy = top-y;
		double dx = left-x;
		double b1 = Math.sqrt(dx*dx+dy*dy);
		dx = right-x;
		double b2 = Math.sqrt(dx*dx+dy*dy);
		
		return new Point2D(b1,b2);
	}

	@Override
	public void render(GL2 gl2, Plotter robot) {
		paintMotors(gl2, robot);
		paintControlBox(gl2, robot);
		if(robot.getDidFindHome()) 
			paintPenHolderToCounterweights(gl2, robot);
	}

	static public void paintMotors(GL2 gl2,Plotter robot) {
		double top = robot.getLimitTop();
		double right = robot.getLimitRight();
		double left = robot.getLimitLeft();

		gl2.glColor3f(1, 0.8f, 0.5f);
		// left frame
		gl2.glPushMatrix();
		// gl2.glTranslatef(-2.1f, 2.1f, 0);
		/*gl2.glBegin(GL2.GL_TRIANGLE_FAN);
		gl2.glVertex2d(left - FRAME_SIZE, top + FRAME_SIZE);
		gl2.glVertex2d(left + FRAME_SIZE, top + FRAME_SIZE);
		gl2.glVertex2d(left + FRAME_SIZE, top             );
		gl2.glVertex2d(left             , top - FRAME_SIZE);
		gl2.glVertex2d(left - FRAME_SIZE, top - FRAME_SIZE);
		gl2.glEnd();*/
		gl2.glPopMatrix();

		// right frame
		gl2.glPushMatrix();
		// gl2.glTranslatef(2.1f, 2.1f, 0);
		/*gl2.glBegin(GL2.GL_TRIANGLE_FAN);
		gl2.glVertex2d(right + FRAME_SIZE, top + FRAME_SIZE);
		gl2.glVertex2d(right - FRAME_SIZE, top + FRAME_SIZE);
		gl2.glVertex2d(right - FRAME_SIZE, top             );
		gl2.glVertex2d(right             , top - FRAME_SIZE);
		gl2.glVertex2d(right + FRAME_SIZE, top - FRAME_SIZE);
		gl2.glEnd();*/
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

	private void paintControlBox(GL2 gl2, Plotter robot) {
		double cy = robot.getLimitTop();
		double left = robot.getLimitLeft();
		double right = robot.getLimitRight();
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
		gl2.glVertex2d(-40, 30);
		gl2.glVertex2d(+40, 30);
		gl2.glVertex2d(+40, -30);
		gl2.glVertex2d(-40, -30);
		gl2.glEnd();

		gl2.glPopMatrix();
	}

	static public void paintPenHolderToCounterweights(GL2 gl2, Plotter robot) {
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
		drawCircle(gl2,gx,gy,PEN_HOLDER_RADIUS_2,20);
		if(robot.getPenIsUp()) {
			drawCircle(gl2,gx,gy,PEN_HOLDER_RADIUS_2+5,20);
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

	public static void drawCircle(GL2 gl2, double gx, double gy, float penHolderRadius2, int steps) {
		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glColor3f(0, 0, 1);
		float f;
		for (f = 0; f < steps;++f) {
			double f2 = Math.PI*2.0 * (double)f/(double)steps;
			gl2.glVertex2d(
					gx + Math.cos(f2) * PEN_HOLDER_RADIUS_2, 
					gy + Math.sin(f2) * PEN_HOLDER_RADIUS_2);
		}
		gl2.glEnd();
	}
}
