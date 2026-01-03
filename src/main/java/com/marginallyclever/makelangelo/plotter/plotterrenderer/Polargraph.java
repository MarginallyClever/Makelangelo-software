package com.marginallyclever.makelangelo.plotter.plotterrenderer;

import com.marginallyclever.convenience.helpers.DrawingHelper;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;

import javax.vecmath.Point2d;
import java.awt.*;

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
	public Point2d FK(Plotter plotter, float beltL, float beltR) {
		float top = (float)plotter.getSettings().getDouble(PlotterSettings.LIMIT_TOP);
		float right = (float)plotter.getSettings().getDouble(PlotterSettings.LIMIT_RIGHT);
		float left = (float)plotter.getSettings().getDouble(PlotterSettings.LIMIT_LEFT);

		// use law of cosines: theta = acos((a*a+b*b-c*c)/(2*a*b));
		float a = beltL;
		float b = right-left;
		float c = beltR;

		float theta = (a*a + b*b - c*c) / (2.0f*a*b);

		float x = theta * a - b/2;
		float y = top - (float)Math.sqrt(1.0 - theta * theta) * a;

		return new Point2d(x, y);
	}
		
	/**
	 * convert from cartesian space to belt lengths.
	 * @param plotter the plotter
	 * @param x cartesian x
	 * @param y cartesian y
	 * @return Point2d with x=belt left and y=belt right.
	 */
	public Point2d IK(Plotter plotter,float x,float y) {
		float right = (float)plotter.getSettings().getDouble(PlotterSettings.LIMIT_RIGHT);
		float left = (float)plotter.getSettings().getDouble(PlotterSettings.LIMIT_LEFT);
		float top = (float)plotter.getSettings().getDouble(PlotterSettings.LIMIT_TOP);
		
		float dy = top-y;
		float dx = left-x;
		float b1 = (float)Math.sqrt(dx*dx+dy*dy);
		dx = right-x;
		float b2 = (float)Math.sqrt(dx*dx+dy*dy);
		
		return new Point2d(b1,b2);
	}

	@Override
	public void render(Graphics graphics, Plotter robot) {
		paintMotors(graphics, robot);
		paintControlBox(graphics, robot);
		if(robot.getDidFindHome()) {
			paintPenHolderToCounterweights(graphics, robot);
		}
	}

	static public void paintMotors(Graphics graphics,Plotter robot) {
		float top = (float)robot.getSettings().getDouble(PlotterSettings.LIMIT_TOP);
		float right = (float)robot.getSettings().getDouble(PlotterSettings.LIMIT_RIGHT);
		float left = (float)robot.getSettings().getDouble(PlotterSettings.LIMIT_LEFT);

		// left motor
		var c = new Color(0, 0, 0);
		DrawingHelper.drawRectangle(graphics,top+MOTOR_SIZE, left+MOTOR_SIZE, top-MOTOR_SIZE,left-MOTOR_SIZE, c);
		// right motor
		DrawingHelper.drawRectangle(graphics,top+MOTOR_SIZE, right+MOTOR_SIZE, top-MOTOR_SIZE,right-MOTOR_SIZE, c);
	}

	private void paintControlBox(Graphics graphics, Plotter robot) {
		float cy = (float)robot.getSettings().getDouble(PlotterSettings.LIMIT_TOP);
		float left = (float)robot.getSettings().getDouble(PlotterSettings.LIMIT_LEFT);
		float right = (float)robot.getSettings().getDouble(PlotterSettings.LIMIT_RIGHT);
		float cx = 0;

		// mounting plate for PCB
		var c = new Color(1, 0.8f, 0.5f);
		DrawingHelper.drawRectangle(graphics, cy+50, cx+80, cy-50, cx-80, c);

		// wires to each motor
        Graphics2D g2d = (Graphics2D) graphics;

		float SPACING=2f;
		float y=SPACING*-1.5f;
        g2d.setColor(Color.RED   );    g2d.drawLine((int)cx, (int)(cy+y), (int)(cx+left), (int)(cy+y) );  y+=SPACING;
        g2d.setColor(Color.GREEN );    g2d.drawLine((int)cx, (int)(cy+y), (int)(cx+left), (int)(cy+y) );  y+=SPACING;
        g2d.setColor(Color.BLUE  );    g2d.drawLine((int)cx, (int)(cy+y), (int)(cx+left), (int)(cy+y) );  y+=SPACING;
        g2d.setColor(Color.YELLOW);    g2d.drawLine((int)cx, (int)(cy+y), (int)(cx+left), (int)(cy+y) );  y+=SPACING;

		y=SPACING*-1.5f;
        g2d.setColor(Color.RED   );    g2d.drawLine((int)cx, (int)(cy+y), (int)(cx+right), (int)(cy+y) );  y+=SPACING;
        g2d.setColor(Color.GREEN );    g2d.drawLine((int)cx, (int)(cy+y), (int)(cx+right), (int)(cy+y) );  y+=SPACING;
        g2d.setColor(Color.BLUE  );    g2d.drawLine((int)cx, (int)(cy+y), (int)(cx+right), (int)(cy+y) );  y+=SPACING;
        g2d.setColor(Color.YELLOW);    g2d.drawLine((int)cx, (int)(cy+y), (int)(cx+right), (int)(cy+y) );  y+=SPACING;

		// UNO
		var c2 = new Color(0, 0, 0.6f);
		DrawingHelper.drawRectangle(graphics,cy+30,cx+40,cy-30,cx-40,c2);
	}

	static public void paintPenHolderToCounterweights(Graphics graphics, Plotter robot) {
		Point2d pos = robot.getPos();
		float gx = (float)pos.x;
		float gy = (float)pos.y;

		float top = (float)robot.getSettings().getDouble(PlotterSettings.LIMIT_TOP);
		float bottom = (float)robot.getSettings().getDouble(PlotterSettings.LIMIT_BOTTOM);
		float left = (float)robot.getSettings().getDouble(PlotterSettings.LIMIT_LEFT);
		float right = (float)robot.getSettings().getDouble(PlotterSettings.LIMIT_RIGHT);

		if (gx < left || gx > right) return;
		if (gy > top || gy < bottom) return;

		float mw = right - left;
		float mh = top - bottom;
		float beltLength = (float)Math.sqrt(mw * mw + mh * mh) + 50;  // TODO replace with robot.getBeltLength()

		float dx = gx - left;
		float dy = gy - top;
		float left_a = (float)Math.sqrt(dx * dx + dy * dy);
		float left_b = (beltLength - left_a) / 2 - 55;

		dx = gx - right;
		float right_a = (float)Math.sqrt(dx * dx + dy * dy);
		float right_b = (beltLength - right_a) / 2 + 55;

		var c = new Color(0.2f, 0.2f, 0.2f, 1.0f);


        Graphics2D g2d = (Graphics2D) graphics;
        g2d.drawLine((int)left ,(int)top,(int)gx,(int)gy);  // belt from motor to pen holder left
        g2d.drawLine((int)right,(int)top,(int)gx,(int)gy);  // belt from motor to pen holder right
/*
		// belt from motor to counterweight left
		paintBeltSide(g2d, left,top,left_b);
		// belt from motor to counterweight right
		paintBeltSide(g2d, right,top,right_b);
*/
		paintGondola(graphics,gx,gy,robot,c);

		// left
		paintCounterweight(graphics,left,top-left_b);
		// right
		paintCounterweight(graphics,right,top-right_b);
	}

	private static void paintBeltSide(Graphics2D g2d, float x, float y, float length) {
		g2d.drawLine((int)(x - 2), (int)y,(int)(x - 2), (int)(y - length));
		g2d.drawLine((int)(x + 2), (int)y,(int)(x + 2), (int)(y - length));
	}

	private static void paintGondola(Graphics graphics, float gx, float gy, Plotter robot, Color color) {
		DrawingHelper.drawCircle(graphics, gx, gy, PEN_HOLDER_RADIUS_2, color);
		if (robot.getPenIsUp()) {
			DrawingHelper.drawCircle(graphics, gx, gy, PEN_HOLDER_RADIUS_2 + 5, color);
		}
	}

	static public void paintCounterweight(Graphics graphics,float x,float y) {/*
		gl2.glBegin(GL3.GL_LINE_LOOP);
		gl2.glColor3f(0, 0, 1);
		gl2.glVertex2d(x - COUNTERWEIGHT_HALF_WIDTH, y);
		gl2.glVertex2d(x + COUNTERWEIGHT_HALF_WIDTH, y);
		gl2.glVertex2d(x + COUNTERWEIGHT_HALF_WIDTH, y - COUNTERWEIGHT_HEIGHT);
		gl2.glVertex2d(x - COUNTERWEIGHT_HALF_WIDTH, y - COUNTERWEIGHT_HEIGHT);
		gl2.glEnd();*/
	}

	static public void paintBottomClearanceArea(Graphics graphics, Plotter machine) {/*
		// bottom clearance arcs
		// right
		float w = (float)machine.getSettings().getDouble(PlotterSettings.LIMIT_RIGHT) - (float)machine.getSettings().getDouble(PlotterSettings.LIMIT_LEFT) + 2.1f;
		float h = (float)machine.getSettings().getDouble(PlotterSettings.LIMIT_TOP) - (float)machine.getSettings().getDouble(PlotterSettings.LIMIT_BOTTOM) + 2.1f;
		float r = (float)Math.sqrt(h*h + w*w); // circle radius
		float gy = (float)machine.getSettings().getDouble(PlotterSettings.LIMIT_TOP) + 2.1f;
		float v;
		gl2.glColor3d(0.6, 0.6, 0.6);

		float gx = (float)machine.getSettings().getDouble(PlotterSettings.LIMIT_LEFT) - 2.1f;
		float start = (float)1.5*(float)Math.PI;
		float end = (2*(float)Math.PI-(float)Math.atan(h/w));
		gl2.glBegin(GL3.GL_LINE_STRIP);
		for(v=0;v<=1.0;v+=0.1) {
		  float vi = (end-start)*v + start;
		  gl2.glVertex2d(gx+Math.cos(vi)*r,gy+Math.sin(vi)*r);
		}
		gl2.glEnd();

		// left
		gx = (float)machine.getSettings().getDouble(PlotterSettings.LIMIT_RIGHT) + 2.1f;
		start = (float)(1*Math.PI+Math.atan(h/w));
		end = (float)1.5*(float)Math.PI;
		gl2.glBegin(GL3.GL_LINE_STRIP);
		for(v=0;v<=1.0;v+=0.1) {
		  float vi = (end-start)*v + start;
		  gl2.glVertex2d(gx+Math.cos(vi)*r, gy+Math.sin(vi)*r);
		}
		gl2.glEnd();*/
	}

	public static void paintSafeArea(Graphics graphics, Plotter robot) {
        var g2d = (Graphics2D) graphics;

		PlotterSettings settings = robot.getSettings();
		float top = -(float)settings.getDouble(PlotterSettings.LIMIT_TOP);
        float left = (float)settings.getDouble(PlotterSettings.LIMIT_LEFT);

		float bottom = -(float)settings.getDouble(PlotterSettings.LIMIT_BOTTOM);
		float right = (float)settings.getDouble(PlotterSettings.LIMIT_RIGHT);

        g2d.setColor(Color.WHITE);
        g2d.drawRect(
                (int)left,
                (int)top,
                (int)Math.abs(right-left),
                (int)Math.abs(top-bottom));
	}
}
