package com.marginallyclever.makelangelo.plotter.plotterrenderer;

import com.jogamp.opengl.GL2;

import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;

import javax.vecmath.Point2d;

/**
 * @author Dan Royer
 */
public abstract class Polargraph implements PlotterRenderer {
	public static final float PEN_HOLDER_RADIUS_2= 60f; // cm
	public static final float MOTOR_SIZE= 21f; // cm
	public static final float COUNTERWEIGHT_HALF_WIDTH = 15;
	public static final float COUNTERWEIGHT_HEIGHT = 100;
	
	/**
	 * convert from belt length mm to cartesian position.
	 * @param beltL length of belt (mm)
	 * @param beltR length of belt (mm)
	 * @return cartesian coordinate 
	 */
	public Point2d FK(Plotter plotter, double beltL, double beltR) {
		double top = plotter.getSettings().getDouble(PlotterSettings.LIMIT_TOP);
		double right = plotter.getSettings().getDouble(PlotterSettings.LIMIT_RIGHT);
		double left = plotter.getSettings().getDouble(PlotterSettings.LIMIT_LEFT);

		// use law of cosines: theta = acos((a*a+b*b-c*c)/(2*a*b));
		double a = beltL;
		double b = right-left;
		double c = beltR;

		double theta = (a*a + b*b - c*c) / (2.0*a*b);

		double x = theta * a - b/2;
		double y = top - Math.sqrt(1.0 - theta * theta) * a;

		return new Point2d(x, y);
	}
		
	/**
	 * convert from cartesian space to belt lengths.
	 * @param plotter the plotter
	 * @param x cartesian x
	 * @param y cartesian y
	 * @return Point2d with x=belt left and y=belt right.
	 */
	public Point2d IK(Plotter plotter,double x,double y) {
		double right = plotter.getSettings().getDouble(PlotterSettings.LIMIT_RIGHT);
		double left = plotter.getSettings().getDouble(PlotterSettings.LIMIT_LEFT);
		double top = plotter.getSettings().getDouble(PlotterSettings.LIMIT_TOP);
		
		double dy = top-y;
		double dx = left-x;
		double b1 = Math.sqrt(dx*dx+dy*dy);
		dx = right-x;
		double b2 = Math.sqrt(dx*dx+dy*dy);
		
		return new Point2d(b1,b2);
	}

	@Override
	public void render(GL2 gl2, Plotter robot) {
		paintMotors(gl2, robot);
		paintControlBox(gl2, robot);
		if(robot.getDidFindHome()) {
			paintPenHolderToCounterweights(gl2, robot);
		}
	}

	static public void paintMotors(GL2 gl2,Plotter robot) {
		double top = robot.getSettings().getDouble(PlotterSettings.LIMIT_TOP);
		double right = robot.getSettings().getDouble(PlotterSettings.LIMIT_RIGHT);
		double left = robot.getSettings().getDouble(PlotterSettings.LIMIT_LEFT);

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
		double cy = robot.getSettings().getDouble(PlotterSettings.LIMIT_TOP);
		double left = robot.getSettings().getDouble(PlotterSettings.LIMIT_LEFT);
		double right = robot.getSettings().getDouble(PlotterSettings.LIMIT_RIGHT);
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
		Point2d pos = robot.getPos();
		double gx = pos.x;
		double gy = pos.y;

		double top = robot.getSettings().getDouble(PlotterSettings.LIMIT_TOP);
		double bottom = robot.getSettings().getDouble(PlotterSettings.LIMIT_BOTTOM);
		double left = robot.getSettings().getDouble(PlotterSettings.LIMIT_LEFT);
		double right = robot.getSettings().getDouble(PlotterSettings.LIMIT_RIGHT);

		if (gx < left || gx > right) return;
		if (gy > top || gy < bottom) return;

		double mw = right - left;
		double mh = top - bottom;
		double beltLength = Math.sqrt(mw * mw + mh * mh) + 50;  // TODO replace with robot.getBeltLength()

		double dx = gx - left;
		double dy = gy - top;
		double left_a = Math.sqrt(dx * dx + dy * dy);
		double left_b = (beltLength - left_a) / 2 - 55;

		dx = gx - right;
		double right_a = Math.sqrt(dx * dx + dy * dy);
		double right_b = (beltLength - right_a) / 2 + 55;

		gl2.glBegin(GL2.GL_LINES);
		gl2.glColor3d(0.2, 0.2, 0.2);

		// belt from motor to pen holder left
		gl2.glVertex2d(left, top);
		gl2.glVertex2d(gx, gy);
		// belt from motor to pen holder right
		gl2.glVertex2d(right, top);
		gl2.glVertex2d(gx, gy);
/*
		// belt from motor to counterweight left
		paintBeltSide(gl2,left,top,left_b);
		// belt from motor to counterweight right
		paintBeltSide(gl2,right,top,right_b);
*/
		paintGondola(gl2,gx,gy,robot);

		// left
		paintCounterweight(gl2,left,top-left_b);
		// right
		paintCounterweight(gl2,right,top-right_b);
	}

	private static void paintBeltSide(GL2 gl2,double x, double y, double length) {
		gl2.glVertex2d(x - 2, y);
		gl2.glVertex2d(x - 2, y - length);
		gl2.glVertex2d(x + 2, y);
		gl2.glVertex2d(x + 2, y - length);
	}

	private static void paintGondola(GL2 gl2, double gx, double gy,Plotter robot) {
		drawCircle(gl2, gx, gy, PEN_HOLDER_RADIUS_2, 20);
		if (robot.getPenIsUp()) {
			drawCircle(gl2, gx, gy, PEN_HOLDER_RADIUS_2 + 5, 20);
		}
	}

	static public void paintCounterweight(GL2 gl2,double x,double y) {
		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glColor3f(0, 0, 1);
		gl2.glVertex2d(x - COUNTERWEIGHT_HALF_WIDTH, y);
		gl2.glVertex2d(x + COUNTERWEIGHT_HALF_WIDTH, y);
		gl2.glVertex2d(x + COUNTERWEIGHT_HALF_WIDTH, y - COUNTERWEIGHT_HEIGHT);
		gl2.glVertex2d(x - COUNTERWEIGHT_HALF_WIDTH, y - COUNTERWEIGHT_HEIGHT);
		gl2.glEnd();
	}

	static public void paintBottomClearanceArea(GL2 gl2, Plotter machine) {

		// bottom clearance arcs
		// right
		double w = machine.getSettings().getDouble(PlotterSettings.LIMIT_RIGHT) - machine.getSettings().getDouble(PlotterSettings.LIMIT_LEFT) + 2.1;
		double h = machine.getSettings().getDouble(PlotterSettings.LIMIT_TOP) - machine.getSettings().getDouble(PlotterSettings.LIMIT_BOTTOM) + 2.1;
		float r=(float)Math.sqrt(h*h + w*w); // circle radius
		double gy = machine.getSettings().getDouble(PlotterSettings.LIMIT_TOP) + 2.1;
		double v;
		gl2.glColor3d(0.6, 0.6, 0.6);

		double gx = machine.getSettings().getDouble(PlotterSettings.LIMIT_LEFT) - 2.1;
		double start = (float)1.5*(float)Math.PI;
		double end = (2*Math.PI-Math.atan(h/w));
		gl2.glBegin(GL2.GL_LINE_STRIP);
		for(v=0;v<=1.0;v+=0.1) {
		  double vi = (end-start)*v + start;
		  gl2.glVertex2d(gx+Math.cos(vi)*r,gy+Math.sin(vi)*r);
		}
		gl2.glEnd();

		// left
		gx = machine.getSettings().getDouble(PlotterSettings.LIMIT_RIGHT) + 2.1;
		start = (float)(1*Math.PI+Math.atan(h/w));
		end = (float)1.5*(float)Math.PI;
		gl2.glBegin(GL2.GL_LINE_STRIP);
		for(v=0;v<=1.0;v+=0.1) {
		  double vi = (end-start)*v + start;
		  gl2.glVertex2d(gx+Math.cos(vi)*r, gy+Math.sin(vi)*r);
		}
		gl2.glEnd();

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

	public static void paintSafeArea(GL2 gl2, Plotter robot) {
		PlotterSettings settings = robot.getSettings();
		double top = settings.getDouble(PlotterSettings.LIMIT_TOP);
		double bottom = settings.getDouble(PlotterSettings.LIMIT_BOTTOM);
		double left = settings.getDouble(PlotterSettings.LIMIT_LEFT);
		double right = settings.getDouble(PlotterSettings.LIMIT_RIGHT);

		// gl2.glColor4f(0.5f,0.5f,0.75f,0.75f); // #color Safe area
		gl2.glColor4f(1, 1, 1, 1); // #color Safe area

		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glVertex2d(left - 70f, top + 70f);
		gl2.glVertex2d(right + 70f, top + 70f);
		gl2.glVertex2d(right + 70f, bottom);
		gl2.glVertex2d(left - 70f, bottom);
		gl2.glEnd();
	}
}
