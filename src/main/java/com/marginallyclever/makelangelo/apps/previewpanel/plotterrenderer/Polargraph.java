package com.marginallyclever.makelangelo.apps.previewpanel.plotterrenderer;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.makelangelo.Mesh;
import com.marginallyclever.makelangelo.MeshFactory;
import com.marginallyclever.makelangelo.apps.previewpanel.RenderContext;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point2d;
import javax.vecmath.Vector3d;
import java.awt.*;

/**
 * Common methods for drawing Polargraph machines.
 * @author Dan Royer
 */
public abstract class Polargraph implements PlotterRenderer {
	public static final float PEN_HOLDER_RADIUS_2 = 60f; // cm
	public static final float MOTOR_SIZE = 21f; // cm
	public static final float COUNTERWEIGHT_HALF_WIDTH = 15;
	public static final float COUNTERWEIGHT_HEIGHT = 100;

	private final Mesh meshQuad = MeshFactory.createMesh();
	private final Mesh meshCircle = MeshFactory.createMesh();

	public Polargraph() {
		meshQuad.setRenderStyle(GL3.GL_QUADS);
		meshQuad.addColor(1,1,1,1);	meshQuad.addTexCoord(0,0);	meshQuad.addVertex(-1,-1,0);
		meshQuad.addColor(1,1,1,1);	meshQuad.addTexCoord(1,0);	meshQuad.addVertex( 1,-1,0);
		meshQuad.addColor(1,1,1,1);	meshQuad.addTexCoord(1,1);	meshQuad.addVertex( 1, 1,0);
		meshQuad.addColor(1,1,1,1);	meshQuad.addTexCoord(0,1);	meshQuad.addVertex(-1, 1,0);

		meshCircle.setRenderStyle(GL3.GL_LINE_LOOP);
		for(int i=0;i<60;++i) {
			double angle = Math.toRadians(i);
			meshCircle.addVertex((float)Math.cos(angle),(float)Math.sin(angle),0f);
		}
	}

	/**
	 * convert from belt length mm to cartesian position.
	 * @param beltL length of belt (mm)
	 * @param beltR length of belt (mm)
	 * @return cartesian coordinate 
	 */
	public Point2d FK(Plotter plotter,double beltL, double beltR) {
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
	public void render(RenderContext context, Plotter robot) {
		drawPhysicalLimits(context,robot);

		paintMotors(context, robot);
		paintControlBox(context, robot);
		if(robot.getDidFindHome()) {
			paintPenHolderToCounterweights(context, robot);
		}
	}

	/**
	 * Outline the drawing limits
	 * @param context the rendering context
	 */
	private void drawPhysicalLimits(RenderContext context,Plotter robot) {
// TODO implement me
/*
		mesh.addColor(0.9f, 0.9f, 0.9f,1.0f); // #color

		gl.glBegin(GL3.GL_LINE_LOOP);
		var settings = robot.getSettings();
		gl.glVertex2d(settings.getDouble(PlotterSettings.LIMIT_LEFT), settings.getDouble(PlotterSettings.LIMIT_TOP));
		gl.glVertex2d(settings.getDouble(PlotterSettings.LIMIT_RIGHT), settings.getDouble(PlotterSettings.LIMIT_TOP));
		gl.glVertex2d(settings.getDouble(PlotterSettings.LIMIT_RIGHT), settings.getDouble(PlotterSettings.LIMIT_BOTTOM));
		gl.glVertex2d(settings.getDouble(PlotterSettings.LIMIT_LEFT), settings.getDouble(PlotterSettings.LIMIT_BOTTOM));
		gl.glEnd();*/
	}

	public void paintMotors(RenderContext context,Plotter robot) {
		double top = robot.getSettings().getDouble(PlotterSettings.LIMIT_TOP);
		double right = robot.getSettings().getDouble(PlotterSettings.LIMIT_RIGHT);
		double left = robot.getSettings().getDouble(PlotterSettings.LIMIT_LEFT);

		context.shader.setColor(context.gl,"diffuseColor", Color.BLACK);

		Matrix4d m = new Matrix4d();

		// left motor
		m.setIdentity();
		m.setTranslation(new Vector3d(left,top,0));
		m.transpose();
		context.shader.setMatrix4d(context.gl,"modelMatrix", m);
		meshQuad.render(context.gl);

		// right motor
		m.setIdentity();
		m.setTranslation(new Vector3d(right,top,0));
		m.transpose();
		context.shader.setMatrix4d(context.gl,"modelMatrix", m);
		meshQuad.render(context.gl);

		context.shader.setMatrix4d(context.gl,"modelMatrix", MatrixHelper.createIdentityMatrix4());
		context.shader.setColor(context.gl,"diffuseColor", Color.WHITE);
	}

	private void paintControlBox(RenderContext context, Plotter robot) {
// TODO implement me
/*
		double cy = robot.getSettings().getDouble(PlotterSettings.LIMIT_TOP);
		double left = robot.getSettings().getDouble(PlotterSettings.LIMIT_LEFT);
		double right = robot.getSettings().getDouble(PlotterSettings.LIMIT_RIGHT);
		double cx = 0;

		gl.glPushMatrix();
		gl.glTranslated(cx, cy, 0);

		// mounting plate for PCB
		gl.glColor3f(1, 0.8f, 0.5f);
		gl.glBegin(GL3.GL_QUADS);
		gl.glVertex2d(-80, 50);
		gl.glVertex2d(+80, 50);
		gl.glVertex2d(+80, -50);
		gl.glVertex2d(-80, -50);
		gl.glEnd();

		// wires to each motor
		gl.glBegin(GL3.GL_LINES);
		float SPACING=2f;
		float y=SPACING*-1.5f;
		gl.glColor3f(1, 0, 0);		gl.glVertex2d(0, y);	gl.glVertex2d(left, y);  y+=SPACING;
		gl.glColor3f(0, 1, 0);		gl.glVertex2d(0, y);	gl.glVertex2d(left, y);  y+=SPACING;
		gl.glColor3f(0, 0, 1);		gl.glVertex2d(0, y);	gl.glVertex2d(left, y);  y+=SPACING;
		gl.glColor3f(1, 1, 0);		gl.glVertex2d(0, y);	gl.glVertex2d(left, y);  y+=SPACING;

		y=SPACING*-1.5f;
		gl.glColor3f(1, 0, 0);		gl.glVertex2d(0, y);	gl.glVertex2d(right, y);  y+=SPACING;
		gl.glColor3f(0, 1, 0);		gl.glVertex2d(0, y);	gl.glVertex2d(right, y);  y+=SPACING;
		gl.glColor3f(0, 0, 1);		gl.glVertex2d(0, y);	gl.glVertex2d(right, y);  y+=SPACING;
		gl.glColor3f(1, 1, 0);		gl.glVertex2d(0, y);	gl.glVertex2d(right, y);  y+=SPACING;
		gl.glEnd();

		// UNO
		gl.glColor3d(0, 0, 0.6);
		gl.glBegin(GL3.GL_QUADS);
		gl.glVertex2d(-40, 30);
		gl.glVertex2d(+40, 30);
		gl.glVertex2d(+40, -30);
		gl.glVertex2d(-40, -30);
		gl.glEnd();

		gl.glPopMatrix();*/
	}

	public void paintPenHolderToCounterweights(RenderContext context, Plotter robot) {
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
// TODO implement me
/*
		gl.glBegin(GL3.GL_LINES);
		gl.glColor3d(0.2, 0.2, 0.2);

		// belt from motor to pen holder left
		gl.glVertex2d(left, top);
		gl.glVertex2d(gx, gy);
		// belt from motor to pen holder right
		gl.glVertex2d(right, top);
		gl.glVertex2d(gx, gy);
*/
		// belt from motor to counterweight left
		//paintBeltSide(gl,left,top,left_b);
		// belt from motor to counterweight right
		//paintBeltSide(gl,right,top,right_b);

		paintGondola(context,gx,gy,robot);

		// left
		paintCounterweight(context,left,top-left_b);
		// right
		paintCounterweight(context,right,top-right_b);
	}

	private void paintBeltSide(GL3 gl,double x, double y, double length) {
// TODO implement me
/*
		gl.glVertex2d(x - 2, y);
		gl.glVertex2d(x - 2, y - length);
		gl.glVertex2d(x + 2, y);
		gl.glVertex2d(x + 2, y - length);*/
	}

	private void paintGondola(RenderContext context, double gx, double gy,Plotter robot) {
		drawCircle(context, gx, gy, PEN_HOLDER_RADIUS_2);
		if (robot.getPenIsUp()) {
			drawCircle(context, gx, gy, PEN_HOLDER_RADIUS_2 + 5);
		}
	}

	public void paintCounterweight(RenderContext context,double x,double y) {
// TODO implement me
/*
		gl.glBegin(GL3.GL_LINE_LOOP);
		gl.glColor3f(0, 0, 1);
		gl.glVertex2d(x - COUNTERWEIGHT_HALF_WIDTH, y);
		gl.glVertex2d(x + COUNTERWEIGHT_HALF_WIDTH, y);
		gl.glVertex2d(x + COUNTERWEIGHT_HALF_WIDTH, y - COUNTERWEIGHT_HEIGHT);
		gl.glVertex2d(x - COUNTERWEIGHT_HALF_WIDTH, y - COUNTERWEIGHT_HEIGHT);
		gl.glEnd();*/
	}

	public void paintBottomClearanceArea(RenderContext context, Plotter machine) {
// TODO implement me
/*
		// bottom clearance arcs
		// right
		double w = machine.getSettings().getDouble(PlotterSettings.LIMIT_RIGHT) - machine.getSettings().getDouble(PlotterSettings.LIMIT_LEFT) + 2.1;
		double h = machine.getSettings().getDouble(PlotterSettings.LIMIT_TOP) - machine.getSettings().getDouble(PlotterSettings.LIMIT_BOTTOM) + 2.1;
		float r=(float)Math.sqrt(h*h + w*w); // circle radius
		double gy = machine.getSettings().getDouble(PlotterSettings.LIMIT_TOP) + 2.1;
		double v;
		gl.glColor3d(0.6, 0.6, 0.6);

		double gx = machine.getSettings().getDouble(PlotterSettings.LIMIT_LEFT) - 2.1;
		double start = (float)1.5*(float)Math.PI;
		double end = (2*Math.PI-Math.atan(h/w));
		gl.glBegin(GL3.GL_LINE_STRIP);
		for(v=0;v<=1.0;v+=0.1) {
		  double vi = (end-start)*v + start;
		  gl.glVertex2d(gx+Math.cos(vi)*r,gy+Math.sin(vi)*r);
		}
		gl.glEnd();

		// left
		gx = machine.getSettings().getDouble(PlotterSettings.LIMIT_RIGHT) + 2.1;
		start = (float)(1*Math.PI+Math.atan(h/w));
		end = (float)1.5*(float)Math.PI;
		gl.glBegin(GL3.GL_LINE_STRIP);
		for(v=0;v<=1.0;v+=0.1) {
		  double vi = (end-start)*v + start;
		  gl.glVertex2d(gx+Math.cos(vi)*r, gy+Math.sin(vi)*r);
		}
		gl.glEnd();*/
	}

	public void drawCircle(RenderContext context, double gx, double gy, float radius) {
		Matrix4d m = new Matrix4d();
		m.setIdentity();
		m.setScale(radius);
		m.setTranslation(new Vector3d(gx,gy,0));
		m.transpose();
		context.shader.setColor(context.gl,"diffuseColor", Color.BLUE);
		context.shader.setMatrix4d(context.gl,"modelMatrix", m);
		meshCircle.render(context.gl);
		context.shader.setMatrix4d(context.gl,"modelMatrix", MatrixHelper.createIdentityMatrix4());
		context.shader.setColor(context.gl,"diffuseColor", Color.WHITE);
	}

	public void paintSafeArea(RenderContext context, Plotter robot) {
		PlotterSettings settings = robot.getSettings();
		double top = settings.getDouble(PlotterSettings.LIMIT_TOP) + 70;
		double bottom = settings.getDouble(PlotterSettings.LIMIT_BOTTOM);
		double left = settings.getDouble(PlotterSettings.LIMIT_LEFT) - 70;
		double right = settings.getDouble(PlotterSettings.LIMIT_RIGHT) + 70;

		Matrix4d m = new Matrix4d();
		m.setIdentity();
		m.m00 = right-left;
		m.m11 = top-bottom;
		m.setTranslation(new Vector3d((left+right)/2,(top+bottom)/2,0));
		m.transpose();
		context.shader.setColor(context.gl,"diffuseColor", Color.WHITE);
		context.shader.setMatrix4d(context.gl,"modelMatrix", m);
		meshQuad.setRenderStyle(GL3.GL_LINE_LOOP);
		meshQuad.render(context.gl);
		meshQuad.setRenderStyle(GL3.GL_QUADS);
		context.shader.setMatrix4d(context.gl,"modelMatrix", MatrixHelper.createIdentityMatrix4());
	}
}
