package com.marginallyclever.makelangelo.apps.previewpanel.plotterrenderer;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.makelangelo.Mesh;
import com.marginallyclever.makelangelo.apps.previewpanel.RenderContext;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.texture.TextureFactory;
import com.marginallyclever.makelangelo.texture.TextureWithMetadata;

import javax.vecmath.Matrix4d;
import java.awt.*;

public class MakelangeloCustom extends Polargraph implements PlotterRenderer {
	public final static float PEN_HOLDER_RADIUS_5 = 25; // mm
	public final static double COUNTERWEIGHT_W = 30;
	public final static double COUNTERWEIGHT_H = 60;
	public final static double PULLEY_RADIUS = 1.27;

	private final TextureWithMetadata controlBoard = TextureFactory.loadTexture("/textures/rampsv14.png");
	private final Mesh wire = new Mesh();

	public MakelangeloCustom() {
		float SPACING = 2;

		wire.setRenderStyle(GL3.GL_LINES);
		float y=0;
		wire.addColor(1,0,0,1);		wire.addVertex(0,0,0);
		wire.addColor(1,0,0,1);		wire.addVertex(1,y,0);
		y += SPACING;
		wire.addColor(0,1,0,1);		wire.addVertex(0,0,0);
		wire.addColor(0,1,0,1);		wire.addVertex(1,y,0);
		y += SPACING;
		wire.addColor(0,0,1,1);		wire.addVertex(0,0,0);
		wire.addColor(0,0,1,1);		wire.addVertex(1,y,0);
		y += SPACING;
		wire.addColor(1,1,0,1);		wire.addVertex(0,0,0);
		wire.addColor(1,1,0,1);		wire.addVertex(1,y,0);
	}

	@Override
	public void render(RenderContext context, Plotter robot) {
		paintSafeArea(context,robot);
		paintControlBox(context,robot.getSettings());
		paintMotors(context,robot);
		if(robot.getDidFindHome())
			paintPenHolderToCounterweights(context,robot);
	}

	@Override
	public void updatePlotterSettings(PlotterSettings settings) {

	}

	/**
	 * paint the controller and the LCD panel
	 * @param context the render context
	 * @param settings plottersettings of the robot
	 */
	private void paintControlBox(RenderContext context, PlotterSettings settings) {
		double left = settings.getDouble(PlotterSettings.LIMIT_LEFT);
		double right = settings.getDouble(PlotterSettings.LIMIT_RIGHT);
		double top = settings.getDouble(PlotterSettings.LIMIT_TOP);
		double cx = 0;
		double cy = top;

		// mounting plate for PCB
		context.shader.setColor(context.gl,"diffuseColor", new Color(255,204,127,255));
		drawRectangle(context, top+35f, right+30f, top-35f, left-30f);
		context.shader.setColor(context.gl,"diffuseColor", Color.WHITE);

		// wires to each motor
		Matrix4d m = new Matrix4d();
		m.setIdentity();
		m.m22 = right-left;
		m.setTranslation(new javax.vecmath.Vector3d(cx+left, cy, 0));
		m.transpose();
		context.shader.setMatrix4d(context.gl,"modelMatrix",m);
		wire.render(context.gl);

		float shiftX = (float)right / 2;
		final double scale = 0.1;
		if (shiftX < 100) {
			shiftX = 45;
		}
		paintTexture(context, controlBoard, cx+shiftX, cy-21, 1024 * scale, 1024 * scale);
		renderLCD(context, left, cx, cy);

		context.shader.setMatrix4d(context.gl,"modelMatrix", MatrixHelper.createIdentityMatrix4());
	}

	private void renderLCD(RenderContext context, double left,double cx,double cy) {
// TODO implement me
/*
		// position
		float shiftX = (float) left / 2;
		if (shiftX > -100) {
			shiftX = -75;
		}
		gl.glPushMatrix();
		gl.glTranslated(shiftX, 0, 0);

		// LCD red
		float w = 150f/2;
		float h = 56f/2;
		gl.glColor3f(0.8f,0.0f,0.0f);
		drawRectangle(gl, h, w, -h, -w);

		// LCD green
		gl.glPushMatrix();
		gl.glTranslated(-(2.6)/2, -0.771, 0);
		
		w = 98f/2;
		h = 60f/2;
		gl.glColor3f(0,0.6f,0.0f);
		drawRectangle(gl, h, w, -h, -w);

		// LCD black
		h = 40f/2;
		gl.glColor3f(0,0,0);
		drawRectangle(gl, h, w, -h, -w);

		// LCD blue
		h = 25f/2;
		w = 75f/2;
		gl.glColor3f(0,0,0.7f);
		drawRectangle(gl, h, w, -h, -w);
		
		gl.glPopMatrix();

		// clean up
		gl.glPopMatrix();*/
	}

	public void paintPenHolderToCounterweights(RenderContext context, Plotter robot) {
// TODO implement me
/*
		PlotterSettings settings = robot.getSettings();
		double dx,dy;
		Point2d pos = robot.getPos();
		double gx = pos.x;
		double gy = pos.y;
		
		double top = settings.getDouble(PlotterSettings.LIMIT_TOP);
		double bottom = settings.getDouble(PlotterSettings.LIMIT_BOTTOM);
		double left = settings.getDouble(PlotterSettings.LIMIT_LEFT);
		double right = settings.getDouble(PlotterSettings.LIMIT_RIGHT);
		
		if(gx<left  ) return;
		if(gx>right ) return;
		if(gy>top   ) return;
		if(gy<bottom) return;
		
		float bottleCenter = 8f+7.5f;
		
		double mw = right-left;
		double mh = top-settings.getDouble(PlotterSettings.LIMIT_BOTTOM);
		double suggestedLength = Math.sqrt(mw*mw+mh*mh)+50;

		dx = gx - left;
		dy = gy - top;
		double left_a = Math.sqrt(dx*dx+dy*dy);
		double left_b = (suggestedLength - left_a)/2;

		dx = gx - right;
		double right_a = Math.sqrt(dx*dx+dy*dy);
		double right_b = (suggestedLength - right_a)/2;

		paintPlotter(gl,(float)gx,(float)gy);

		// belts
		gl.glBegin(GL3.GL_LINES);
		gl.glColor3d(0.2,0.2,0.2);
		
		// belt from motor to pen holder left
		gl.glVertex2d(left, top);
		gl.glVertex2d(gx,gy);
		// belt from motor to pen holder right
		gl.glVertex2d(right, top);
		gl.glVertex2d(gx,gy);
		
		// belt from motor to counterweight left
		gl.glVertex2d(left-bottleCenter-PULLEY_RADIUS, top-MOTOR_WIDTH/2);
		gl.glVertex2d(left-bottleCenter-PULLEY_RADIUS, top-left_b);
		// belt from motor to counterweight right
		gl.glVertex2d(right+bottleCenter+PULLEY_RADIUS, top-MOTOR_WIDTH/2);
		gl.glVertex2d(right+bottleCenter+PULLEY_RADIUS, top-right_b);
		gl.glEnd();
		
		// counterweight left
		gl.glBegin(GL3.GL_LINE_LOOP);
		gl.glColor3f(0, 0, 1);
		gl.glVertex2d(left-PULLEY_RADIUS-bottleCenter-COUNTERWEIGHT_W/2,top-left_b);
		gl.glVertex2d(left-PULLEY_RADIUS-bottleCenter+COUNTERWEIGHT_W/2,top-left_b);
		gl.glVertex2d(left-PULLEY_RADIUS-bottleCenter+COUNTERWEIGHT_W/2,top-left_b-COUNTERWEIGHT_H);
		gl.glVertex2d(left-PULLEY_RADIUS-bottleCenter-COUNTERWEIGHT_W/2,top-left_b-COUNTERWEIGHT_H);
		gl.glEnd();
		
		// counterweight right
		gl.glBegin(GL3.GL_LINE_LOOP);
		gl.glColor3f(0, 0, 1);
		gl.glVertex2d(right+PULLEY_RADIUS+bottleCenter-COUNTERWEIGHT_W/2,top-right_b);
		gl.glVertex2d(right+PULLEY_RADIUS+bottleCenter+COUNTERWEIGHT_W/2,top-right_b);
		gl.glVertex2d(right+PULLEY_RADIUS+bottleCenter+COUNTERWEIGHT_W/2,top-right_b-COUNTERWEIGHT_H);
		gl.glVertex2d(right+PULLEY_RADIUS+bottleCenter-COUNTERWEIGHT_W/2,top-right_b-COUNTERWEIGHT_H);
		gl.glEnd();*/
	}

	private void paintPlotter(GL3 gl, float gx, float gy) {
// TODO implement me
/*
		// plotter
		gl.glColor3f(0, 0, 1);
		drawCircle(gl, gx, gy, PEN_HOLDER_RADIUS_5);*/
	}
}
