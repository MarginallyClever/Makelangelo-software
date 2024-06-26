package com.marginallyclever.makelangelo.apps.previewpanel.plotterrenderer;

import com.jogamp.opengl.GL3;
import com.marginallyclever.makelangelo.apps.previewpanel.ShaderProgram;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.texture.TextureFactory;
import com.marginallyclever.makelangelo.texture.TextureWithMetadata;

public class MakelangeloCustom implements PlotterRenderer {
	public final static float PEN_HOLDER_RADIUS_5 = 25; // mm
	public final static double COUNTERWEIGHT_W = 30;
	public final static double COUNTERWEIGHT_H = 60;
	public final static double PULLEY_RADIUS = 1.27;
	public final static double MOTOR_WIDTH = 42;
	private final TextureWithMetadata controlBoard;

	MakelangeloCustom() {
		controlBoard = TextureFactory.loadTexture("/textures/rampsv14.png");
	}

	@Override
	public void render(GL3 gl,Plotter robot, ShaderProgram shaderProgram) {
		PlotterSettings settings = robot.getSettings();

		paintControlBox(gl,settings);
		paintMotors(gl,settings);
		if(robot.getDidFindHome())
			paintPenHolderToCounterweights(gl,robot);
	}

	@Override
	public void updatePlotterSettings(PlotterSettings settings) {

	}

	/**
	 * paint the controller and the LCD panel
	 * @param gl the render context
	 * @param settings plottersettings of the robot
	 */
	private void paintControlBox(GL3 gl, PlotterSettings settings) {
// TODO implement me
/*
		double cy = settings.getDouble(PlotterSettings.LIMIT_TOP);
		double left = settings.getDouble(PlotterSettings.LIMIT_LEFT);
		double right = settings.getDouble(PlotterSettings.LIMIT_RIGHT);
		double top = settings.getDouble(PlotterSettings.LIMIT_TOP);
		double cx = 0;

		gl.glPushMatrix();

		// mounting plate for PCB
		gl.glColor3f(1,0.8f,0.5f);
		// frame
		drawRectangle(gl, top+35f, right+30f, top-35f, left-30f);

		gl.glTranslated(cx, cy, 0);

		// wires to each motor
		gl.glBegin(GL3.GL_LINES);
		final float SPACING=2;
		float y=SPACING*-1.5f;
		gl.glColor3f(1, 0, 0);		gl.glVertex2d(left, y);	gl.glVertex2d(right, y);  y+=SPACING;
		gl.glColor3f(0, 1, 0);		gl.glVertex2d(left, y);	gl.glVertex2d(right, y);  y+=SPACING;
		gl.glColor3f(0, 0, 1);		gl.glVertex2d(left, y);	gl.glVertex2d(right, y);  y+=SPACING;
		gl.glColor3f(1, 1, 0);		gl.glVertex2d(left, y);	gl.glVertex2d(right, y);;
		gl.glEnd();
		
		float shiftX = (float) right / 2;
		if (controlBoard != null) {
			final double scale = 0.1;
			if (shiftX < 100) {
				shiftX = 45;
			}
			paintTexture(gl, controlBoard, shiftX, -72, 1024 * scale, 1024 * scale);
		} else {
			if (shiftX < 100) {
				shiftX = 85;
			}
			// RUMBA in v3 (135mm*75mm)
			float w = 135f / 2;
			float h = 75f / 2;
			gl.glPushMatrix();
			gl.glColor3d(0.9, 0.9, 0.9);
			gl.glTranslated(shiftX, 0, 0);
			drawRectangle(gl, h, w, -h, -w);
			gl.glPopMatrix();
		}

		renderLCD(gl, left);

		gl.glPopMatrix();*/
	}

	// draw left & right motor
	private void paintMotors(GL3 gl, PlotterSettings settings) {
// TODO implement me
/*
		double top = settings.getDouble(PlotterSettings.LIMIT_TOP);
		double right = settings.getDouble(PlotterSettings.LIMIT_RIGHT);
		double left = settings.getDouble(PlotterSettings.LIMIT_LEFT);

		// left motor
		gl.glColor3d(0.3,0.3,0.3);
		drawRectangle(gl, top+MOTOR_WIDTH/2, left+MOTOR_WIDTH/2, top-MOTOR_WIDTH/2, left-MOTOR_WIDTH/2);

		// right motor
		drawRectangle(gl, top+MOTOR_WIDTH/2, right+MOTOR_WIDTH/2, top-MOTOR_WIDTH/2, right-MOTOR_WIDTH/2);*/
	}
	
	private void renderLCD(GL3 gl, double left) {
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

	private void paintPenHolderToCounterweights(GL3 gl, Plotter robot ) {
// TODO implement me
/*
		PlotterSettings settings = robot.getSettings();
		double dx,dy;
		Point2D pos = robot.getPos();
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
