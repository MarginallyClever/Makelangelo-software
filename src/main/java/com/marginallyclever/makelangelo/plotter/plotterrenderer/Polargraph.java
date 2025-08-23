package com.marginallyclever.makelangelo.plotter.plotterrenderer;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.DrawingHelper;
import com.marginallyclever.makelangelo.Mesh;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.preview.ShaderProgram;

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
	public void render(ShaderProgram shader, GL3 gl, Plotter robot) {
		paintMotors(shader, gl, robot);
		paintControlBox(shader, gl, robot);
		if(robot.getDidFindHome()) {
			paintPenHolderToCounterweights(shader, gl, robot);
		}
	}

	static public void paintMotors(ShaderProgram shader, GL3 gl,Plotter robot) {
		float top = (float)robot.getSettings().getDouble(PlotterSettings.LIMIT_TOP);
		float right = (float)robot.getSettings().getDouble(PlotterSettings.LIMIT_RIGHT);
		float left = (float)robot.getSettings().getDouble(PlotterSettings.LIMIT_LEFT);

		// left motor
		var c = new Color(0, 0, 0);
		DrawingHelper.drawRectangle(gl,top+MOTOR_SIZE, left+MOTOR_SIZE, top-MOTOR_SIZE,left-MOTOR_SIZE, c);
		// right motor
		DrawingHelper.drawRectangle(gl,top+MOTOR_SIZE, right+MOTOR_SIZE, top-MOTOR_SIZE,right-MOTOR_SIZE, c);
	}

	private void paintControlBox(ShaderProgram shader, GL3 gl, Plotter robot) {
		float cy = (float)robot.getSettings().getDouble(PlotterSettings.LIMIT_TOP);
		float left = (float)robot.getSettings().getDouble(PlotterSettings.LIMIT_LEFT);
		float right = (float)robot.getSettings().getDouble(PlotterSettings.LIMIT_RIGHT);
		float cx = 0;

		// mounting plate for PCB
		var c = new Color(1, 0.8f, 0.5f);
		DrawingHelper.drawRectangle(gl, cy+50, cx+80, cy-50, cx-80, c);

		// wires to each motor
		Mesh wires = new Mesh();
		wires.setRenderStyle(GL3.GL_LINES);
		float SPACING=2f;
		float y=SPACING*-1.5f;
		wires.addColor(1, 0, 0,1);  wires.addVertex(cx, cy+y,0);	wires.addColor(1, 0, 0,1);  wires.addVertex(cx+left, cy+y,0);  y+=SPACING;
		wires.addColor(0, 1, 0,1);  wires.addVertex(cx, cy+y,0);	wires.addColor(0, 1, 0,1);  wires.addVertex(cx+left, cy+y,0);  y+=SPACING;
		wires.addColor(0, 0, 1,1);  wires.addVertex(cx, cy+y,0);	wires.addColor(0, 0, 1,1);  wires.addVertex(cx+left, cy+y,0);  y+=SPACING;
		wires.addColor(1, 1, 0,1);  wires.addVertex(cx, cy+y,0);	wires.addColor(1, 1, 0,1);  wires.addVertex(cx+left, cy+y,0);  y+=SPACING;

		y=SPACING*-1.5f;
		wires.addColor(1, 0, 0,1);  wires.addVertex(cx, cy+y,0);	wires.addColor(1, 0, 0,1);  wires.addVertex(cx+right, cy+y,0);  y+=SPACING;
		wires.addColor(0, 1, 0,1);  wires.addVertex(cx, cy+y,0);	wires.addColor(0, 1, 0,1);  wires.addVertex(cx+right, cy+y,0);  y+=SPACING;
		wires.addColor(0, 0, 1,1);  wires.addVertex(cx, cy+y,0);	wires.addColor(0, 0, 1,1);  wires.addVertex(cx+right, cy+y,0);  y+=SPACING;
		wires.addColor(1, 1, 0,1);  wires.addVertex(cx, cy+y,0);	wires.addColor(1, 1, 0,1);  wires.addVertex(cx+right, cy+y,0);  y+=SPACING;
		wires.render(gl);

		// UNO
		var c2 = new Color(0, 0, 0.6f);
		DrawingHelper.drawRectangle(gl,cy+30,cx+40,cy-30,cx-40,c2);
	}

	static public void paintPenHolderToCounterweights(ShaderProgram shader, GL3 gl, Plotter robot) {
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

		Mesh belts = new Mesh();
		belts.setRenderStyle(GL3.GL_LINES);
		var c = new Color(0.2f, 0.2f, 0.2f, 1.0f);

		// belt from motor to pen holder left
		belts.addVertex(left, top,0);
		belts.addVertex(gx, gy,0);
		// belt from motor to pen holder right
		belts.addVertex(right, top,0);
		belts.addVertex(gx, gy,0);
/*
		// belt from motor to counterweight left
		paintBeltSide(belts,left,top,left_b);
		// belt from motor to counterweight right
		paintBeltSide(belts,right,top,right_b);
*/
		paintGondola(gl,gx,gy,robot,c);

		// left
		paintCounterweight(shader,left,top-left_b);
		// right
		paintCounterweight(shader,right,top-right_b);
	}

	private static void paintBeltSide(Mesh mesh,float x, float y, float length) {
		mesh.addVertex(x - 2, y, 0);
		mesh.addVertex(x - 2, y - length, 0);
		mesh.addVertex(x + 2, y, 0);
		mesh.addVertex(x + 2, y - length, 0);
	}

	private static void paintGondola(GL3 gl, float gx, float gy,Plotter robot,Color color) {
		DrawingHelper.drawCircle(gl, gx, gy, PEN_HOLDER_RADIUS_2, color);
		if (robot.getPenIsUp()) {
			DrawingHelper.drawCircle(gl, gx, gy, PEN_HOLDER_RADIUS_2 + 5, color);
		}
	}

	static public void paintCounterweight(ShaderProgram shader,float x,float y) {/*
		gl2.glBegin(GL3.GL_LINE_LOOP);
		gl2.glColor3f(0, 0, 1);
		gl2.glVertex2d(x - COUNTERWEIGHT_HALF_WIDTH, y);
		gl2.glVertex2d(x + COUNTERWEIGHT_HALF_WIDTH, y);
		gl2.glVertex2d(x + COUNTERWEIGHT_HALF_WIDTH, y - COUNTERWEIGHT_HEIGHT);
		gl2.glVertex2d(x - COUNTERWEIGHT_HALF_WIDTH, y - COUNTERWEIGHT_HEIGHT);
		gl2.glEnd();*/
	}

	static public void paintBottomClearanceArea(GL3 gl2, Plotter machine) {/*
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

	public static void paintSafeArea(ShaderProgram shader, GL3 gl, Plotter robot) {/*
		PlotterSettings settings = robot.getSettings();
		float top = (float)settings.getDouble(PlotterSettings.LIMIT_TOP);
		float bottom = (float)settings.getDouble(PlotterSettings.LIMIT_BOTTOM);
		float left = (float)settings.getDouble(PlotterSettings.LIMIT_LEFT);
		float right = (float)settings.getDouble(PlotterSettings.LIMIT_RIGHT);

		// gl2.glColor4f(0.5f,0.5f,0.75f,0.75f); // #color Safe area
		gl2.glColor4f(1, 1, 1, 1); // #color Safe area

		gl2.glBegin(GL3.GL_LINE_LOOP);
		gl2.glVertex2d(left - 70f, top + 70f);
		gl2.glVertex2d(right + 70f, top + 70f);
		gl2.glVertex2d(right + 70f, bottom);
		gl2.glVertex2d(left - 70f, bottom);
		gl2.glEnd();*/
	}
}
