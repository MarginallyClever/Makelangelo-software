package com.marginallyclever.makelangelo.apps.previewpanel.plotterrenderer;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.makelangelo.Mesh;
import com.marginallyclever.makelangelo.MeshFactory;
import com.marginallyclever.makelangelo.apps.previewpanel.RenderContext;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.*;

/**
 * @author Dan Royer
 */
public class Zarplotter implements PlotterRenderer {
	public static final int ZAR_MOTOR_MOUNT_SIZE=45; //cm
	public static final int ZAR_PLOTTER_SIZE=60; //cm
	public static final int ZAR_PLOTTER_OUTER_SIZE=70; //cm
	public static final int ZAR_PLOTTER_HOLE_SIZE=20; //cm
	public static final int NEMA17_SIZE = 42; //cm

	public final Mesh meshQuad = MeshFactory.createMesh();

	public Zarplotter() {
		setupMesh();
	}

	private void setupMesh() {
		meshQuad.clear();
		meshQuad.setRenderStyle(GL3.GL_QUADS);
		meshQuad.addColor(1,1,1, 1);
		meshQuad.addColor(1,1,1, 1);
		meshQuad.addColor(1,1,1, 1);
		meshQuad.addColor(1,1,1, 1);
		meshQuad.addVertex(0,0,0);
		meshQuad.addVertex(0,1,0);
		meshQuad.addVertex(1,1,0);
		meshQuad.addVertex(1,0,0);
	}

	@Override
	public void render(RenderContext context, Plotter robot) {
		paintMotors(context,robot);
		paintControlBox(context,robot);
		if(robot.getDidFindHome()) 
			paintPenHolderToCounterweights(context,robot);
	}

	@Override
	public void updatePlotterSettings(PlotterSettings settings) {

	}

	private void paintPenHolderToCounterweights(RenderContext context, Plotter robot) {
// TODO implement me
/*
		PlotterSettings settings = robot.getSettings();
		//double dx, dy;
		Point2d pos = robot.getPos();
		double gx = pos.x;
		double gy = pos.y;

		double top = settings.getDouble(PlotterSettings.LIMIT_TOP);
		double bottom = settings.getDouble(PlotterSettings.LIMIT_BOTTOM);
		double left = settings.getDouble(PlotterSettings.LIMIT_LEFT);
		double right = settings.getDouble(PlotterSettings.LIMIT_RIGHT);

		gl.glColor4d(0, 0, 0,0.5);
		// plotter
		gl.glPushMatrix();
			gl.glTranslated(gx, gy, 0);
			gl.glBegin(GL3.GL_QUADS);
			gl.glVertex2d(-ZAR_PLOTTER_OUTER_SIZE/2, -ZAR_PLOTTER_OUTER_SIZE/2);
			gl.glVertex2d(-ZAR_PLOTTER_OUTER_SIZE/2, -ZAR_PLOTTER_HOLE_SIZE/2);
			gl.glVertex2d(+ZAR_PLOTTER_OUTER_SIZE/2, -ZAR_PLOTTER_HOLE_SIZE/2);
			gl.glVertex2d(+ZAR_PLOTTER_OUTER_SIZE/2, -ZAR_PLOTTER_OUTER_SIZE/2);

			gl.glVertex2d( ZAR_PLOTTER_HOLE_SIZE/2, -ZAR_PLOTTER_HOLE_SIZE/2);
			gl.glVertex2d( ZAR_PLOTTER_HOLE_SIZE/2, ZAR_PLOTTER_HOLE_SIZE/2);
			gl.glVertex2d(+ZAR_PLOTTER_OUTER_SIZE/2, ZAR_PLOTTER_HOLE_SIZE/2);
			gl.glVertex2d(+ZAR_PLOTTER_OUTER_SIZE/2, -ZAR_PLOTTER_HOLE_SIZE/2);

			gl.glVertex2d(-ZAR_PLOTTER_OUTER_SIZE/2, -ZAR_PLOTTER_HOLE_SIZE/2);
			gl.glVertex2d(-ZAR_PLOTTER_OUTER_SIZE/2, ZAR_PLOTTER_HOLE_SIZE/2);
			gl.glVertex2d(-ZAR_PLOTTER_HOLE_SIZE/2,  ZAR_PLOTTER_HOLE_SIZE/2);
			gl.glVertex2d(-ZAR_PLOTTER_HOLE_SIZE/2, -ZAR_PLOTTER_HOLE_SIZE/2);

			gl.glVertex2d(-ZAR_PLOTTER_OUTER_SIZE/2, +ZAR_PLOTTER_HOLE_SIZE/2);
			gl.glVertex2d(-ZAR_PLOTTER_OUTER_SIZE/2, +ZAR_PLOTTER_OUTER_SIZE/2);
			gl.glVertex2d(+ZAR_PLOTTER_OUTER_SIZE/2, +ZAR_PLOTTER_OUTER_SIZE/2);
			gl.glVertex2d(+ZAR_PLOTTER_OUTER_SIZE/2, +ZAR_PLOTTER_HOLE_SIZE/2);
			gl.glEnd();
		gl.glPopMatrix();

		// belt from motors to plotter
		gl.glBegin(GL3.GL_LINES);
		gl.glVertex2d(left +ZAR_MOTOR_MOUNT_SIZE, top   -ZAR_MOTOR_MOUNT_SIZE);	gl.glVertex2d(gx-ZAR_PLOTTER_SIZE/2, gy+ZAR_PLOTTER_SIZE/2);
		gl.glVertex2d(right-ZAR_MOTOR_MOUNT_SIZE, top   -ZAR_MOTOR_MOUNT_SIZE);	gl.glVertex2d(gx+ZAR_PLOTTER_SIZE/2, gy+ZAR_PLOTTER_SIZE/2);
		gl.glVertex2d(left +ZAR_MOTOR_MOUNT_SIZE, bottom+ZAR_MOTOR_MOUNT_SIZE);	gl.glVertex2d(gx-ZAR_PLOTTER_SIZE/2, gy-ZAR_PLOTTER_SIZE/2);
		gl.glVertex2d(right-ZAR_MOTOR_MOUNT_SIZE, bottom+ZAR_MOTOR_MOUNT_SIZE);	gl.glVertex2d(gx+ZAR_PLOTTER_SIZE/2, gy-ZAR_PLOTTER_SIZE/2);
		gl.glEnd();*/
	}

	private void paintMotors(RenderContext context,Plotter plotter) {
		double top = plotter.getSettings().getDouble(PlotterSettings.LIMIT_TOP);
		double bottom = plotter.getSettings().getDouble(PlotterSettings.LIMIT_BOTTOM);
		double right = plotter.getSettings().getDouble(PlotterSettings.LIMIT_RIGHT);
		double left = plotter.getSettings().getDouble(PlotterSettings.LIMIT_LEFT);

		setupMesh();

		Matrix4d m = new Matrix4d();
		m.setIdentity();
		m.rotZ(Math.toRadians(270));
		m.setTranslation(new Vector3d(left,top,0));
		paintOneMotor(context,m);

		m.setIdentity();
		m.rotZ(Math.toRadians(180));
		m.setTranslation(new Vector3d(right,top,0));
		paintOneMotor(context,m);

		m.setIdentity();
		m.rotZ(Math.toRadians(90));
		m.setTranslation(new Vector3d(right,bottom,0));
		paintOneMotor(context,m);

		m.setIdentity();
		m.setTranslation(new Vector3d(left,bottom,0));
		paintOneMotor(context,m);

		context.shader.setMatrix4d(context.gl,"modelMatrix", MatrixHelper.createIdentityMatrix4());
		context.shader.setColor(context.gl,"diffuseColor", Color.WHITE);
	}

	private void paintOneMotor(RenderContext context,Matrix4d m) {
		Matrix4d s = new Matrix4d();

		// frame
		s.setIdentity();
		s.setScale(ZAR_MOTOR_MOUNT_SIZE);
		s.mul(m,s);
		s.transpose();
		context.shader.setMatrix4d(context.gl,"modelMatrix", s);
		context.shader.setColor(context.gl,"diffuseColor", new Color(255, 204, 127));
		meshQuad.render(context.gl);

		// motor
		s.setIdentity();
		s.setScale(NEMA17_SIZE);
		s.mul(m,s);
		s.transpose();
		context.shader.setMatrix4d(context.gl,"modelMatrix", s);
		context.shader.setColor(context.gl,"diffuseColor", Color.BLACK);
		meshQuad.render(context.gl);
	}
	
	private void paintControlBox(RenderContext context,Plotter plotter) {
// TODO implement me
/*
		double cy = plotter.getSettings().getDouble(PlotterSettings.LIMIT_TOP);
		double cx = 0;

		gl.glPushMatrix();
		gl.glTranslated(cx, cy, 0);

		gl.glScaled(10, 10, 1);
		
		// mounting plate for PCB
		gl.glColor3f(1,0.8f,0.5f);
		gl.glBegin(GL3.GL_QUADS);
		gl.glVertex2d(-8, 5);
		gl.glVertex2d(+8, 5);
		gl.glVertex2d(+8, -5);
		gl.glVertex2d(-8, -5);
		gl.glEnd();
		
		// RUMBA in v3 (135mm*75mm)
		float h = 7.5f/2;
		float w = 13.5f/2;
		gl.glColor3d(0.9,0.9,0.9);
		gl.glBegin(GL3.GL_QUADS);
		gl.glVertex2d(-w, h);
		gl.glVertex2d(+w, h);
		gl.glVertex2d(+w, -h);
		gl.glVertex2d(-w, -h);
		gl.glEnd();

		gl.glPopMatrix();*/
	}
}
