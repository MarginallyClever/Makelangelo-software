package com.marginallyclever.makelangelo.plotter.plotterRenderer;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.Texture;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.settings.PlotterSettings;

import static com.marginallyclever.convenience.DrawingHelper.*;

public class MakelangeloCustom implements PlotterRenderer {
	public final static float PEN_HOLDER_RADIUS_5 = 25; // mm
	public final static double COUNTERWEIGHT_W = 30;
	public final static double COUNTERWEIGHT_H = 60;
	public final static double PULLEY_RADIUS = 1.27;
	public final static double MOTOR_WIDTH = 42;
	private static Texture controlBoard;

	@Override
	public void render(GL2 gl2,Plotter robot) {
		PlotterSettings settings = robot.getSettings();

		paintControlBox(gl2,settings);
		paintMotors(gl2,settings);
		if(robot.getDidFindHome())
			paintPenHolderToCounterweights(gl2,robot);
	}

	/**
	 * paint the controller and the LCD panel
	 * @param gl2 the render context
	 * @param settings settings of the robot
	 */
	private void paintControlBox(GL2 gl2, PlotterSettings settings) {
		double cy = settings.getLimitTop();
		double left = settings.getLimitLeft();
		double right = settings.getLimitRight();
		double top = settings.getLimitTop();
		double cx = 0;

		gl2.glPushMatrix();

		// mounting plate for PCB
		final float FRAME_SIZE=50f; //mm

		gl2.glColor3f(1,0.8f,0.5f);
		// frame
		drawRectangle(gl2, top+FRAME_SIZE, left-FRAME_SIZE, top-FRAME_SIZE, right+FRAME_SIZE);

		gl2.glTranslated(cx, cy, 0);

		// wires to each motor
		gl2.glBegin(GL2.GL_LINES);
		final float SPACING=2;
		float y=SPACING*-1.5f;
		gl2.glColor3f(1, 0, 0);		gl2.glVertex2d(left, y);	gl2.glVertex2d(right, y);  y+=SPACING;
		gl2.glColor3f(0, 1, 0);		gl2.glVertex2d(left, y);	gl2.glVertex2d(right, y);  y+=SPACING;
		gl2.glColor3f(0, 0, 1);		gl2.glVertex2d(left, y);	gl2.glVertex2d(right, y);  y+=SPACING;
		gl2.glColor3f(1, 1, 0);		gl2.glVertex2d(left, y);	gl2.glVertex2d(right, y);;
		gl2.glEnd();
		
		float shiftX = (float) right / 2;
		if (controlBoard == null) controlBoard = loadTexture("/textures/rampsv14.png");
		if (controlBoard != null) {
			final double scale = 0.1;
			if (shiftX < 100) {
				shiftX = 45;
			}
			paintTexture(gl2, controlBoard, shiftX, -72, 1024 * scale, 1024 * scale);
		} else {
			if (shiftX < 100) {
				shiftX = 85;
			}
			// RUMBA in v3 (135mm*75mm)
			float w = 135f / 2;
			float h = 75f / 2;
			gl2.glPushMatrix();
			gl2.glColor3d(0.9, 0.9, 0.9);
			gl2.glTranslated(shiftX, 0, 0);
			drawRectangle(gl2, h, w, -h, -w);
			gl2.glPopMatrix();
		}

		renderLCD(gl2, left);

		gl2.glPopMatrix();
	}

	// draw left & right motor
	private void paintMotors(GL2 gl2, PlotterSettings settings) {
		double top = settings.getLimitTop();
		double right = settings.getLimitRight();
		double left = settings.getLimitLeft();

		// left motor
		gl2.glColor3d(0.3,0.3,0.3);
		drawRectangle(gl2, top+MOTOR_WIDTH/2, left+MOTOR_WIDTH/2, top-MOTOR_WIDTH/2, left-MOTOR_WIDTH/2);

		// right motor
		drawRectangle(gl2, top+MOTOR_WIDTH/2, right+MOTOR_WIDTH/2, top-MOTOR_WIDTH/2, right-MOTOR_WIDTH/2);
	}
	
	private void renderLCD(GL2 gl2, double left) {
		// position
		float shiftX = (float) left / 2;
		if (shiftX > -100) {
			shiftX = -75;
		}
		gl2.glPushMatrix();
		gl2.glTranslated(shiftX, 0, 0);

		// LCD red
		float w = 150f/2;
		float h = 56f/2;
		gl2.glColor3f(0.8f,0.0f,0.0f);
		drawRectangle(gl2, h, w, -h, -w);

		// LCD green
		gl2.glPushMatrix();
		gl2.glTranslated(-(2.6)/2, -0.771, 0);
		
		w = 98f/2;
		h = 60f/2;
		gl2.glColor3f(0,0.6f,0.0f);
		drawRectangle(gl2, h, w, -h, -w);

		// LCD black
		h = 40f/2;
		gl2.glColor3f(0,0,0);
		drawRectangle(gl2, h, w, -h, -w);

		// LCD blue
		h = 25f/2;
		w = 75f/2;
		gl2.glColor3f(0,0,0.7f);
		drawRectangle(gl2, h, w, -h, -w);
		
		gl2.glPopMatrix();

		// clean up
		gl2.glPopMatrix();
	}

	private void paintPenHolderToCounterweights(GL2 gl2, Plotter robot ) {
		PlotterSettings settings = robot.getSettings();
		double dx,dy;
		Point2D pos = robot.getPos();
		double gx = pos.x;
		double gy = pos.y;
		
		double top = settings.getLimitTop();
		double bottom = settings.getLimitBottom();
		double left = settings.getLimitLeft();
		double right = settings.getLimitRight();
		
		if(gx<left  ) return;
		if(gx>right ) return;
		if(gy>top   ) return;
		if(gy<bottom) return;
		
		float bottleCenter = 8f+7.5f;
		
		double mw = right-left;
		double mh = top-settings.getLimitBottom();
		double suggestedLength = Math.sqrt(mw*mw+mh*mh)+50;

		dx = gx - left;
		dy = gy - top;
		double left_a = Math.sqrt(dx*dx+dy*dy);
		double left_b = (suggestedLength - left_a)/2;

		dx = gx - right;
		double right_a = Math.sqrt(dx*dx+dy*dy);
		double right_b = (suggestedLength - right_a)/2;

		paintPlotter(gl2,(float)gx,(float)gy);

		// belts
		gl2.glBegin(GL2.GL_LINES);
		gl2.glColor3d(0.2,0.2,0.2);
		
		// belt from motor to pen holder left
		gl2.glVertex2d(left, top);
		gl2.glVertex2d(gx,gy);
		// belt from motor to pen holder right
		gl2.glVertex2d(right, top);
		gl2.glVertex2d(gx,gy);
		
		// belt from motor to counterweight left
		gl2.glVertex2d(left-bottleCenter-PULLEY_RADIUS, top-MOTOR_WIDTH/2);
		gl2.glVertex2d(left-bottleCenter-PULLEY_RADIUS, top-left_b);
		// belt from motor to counterweight right
		gl2.glVertex2d(right+bottleCenter+PULLEY_RADIUS, top-MOTOR_WIDTH/2);
		gl2.glVertex2d(right+bottleCenter+PULLEY_RADIUS, top-right_b);
		gl2.glEnd();
		
		// counterweight left
		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glColor3f(0, 0, 1);
		gl2.glVertex2d(left-PULLEY_RADIUS-bottleCenter-COUNTERWEIGHT_W/2,top-left_b);
		gl2.glVertex2d(left-PULLEY_RADIUS-bottleCenter+COUNTERWEIGHT_W/2,top-left_b);
		gl2.glVertex2d(left-PULLEY_RADIUS-bottleCenter+COUNTERWEIGHT_W/2,top-left_b-COUNTERWEIGHT_H);
		gl2.glVertex2d(left-PULLEY_RADIUS-bottleCenter-COUNTERWEIGHT_W/2,top-left_b-COUNTERWEIGHT_H);
		gl2.glEnd();
		
		// counterweight right
		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glColor3f(0, 0, 1);
		gl2.glVertex2d(right+PULLEY_RADIUS+bottleCenter-COUNTERWEIGHT_W/2,top-right_b);
		gl2.glVertex2d(right+PULLEY_RADIUS+bottleCenter+COUNTERWEIGHT_W/2,top-right_b);
		gl2.glVertex2d(right+PULLEY_RADIUS+bottleCenter+COUNTERWEIGHT_W/2,top-right_b-COUNTERWEIGHT_H);
		gl2.glVertex2d(right+PULLEY_RADIUS+bottleCenter-COUNTERWEIGHT_W/2,top-right_b-COUNTERWEIGHT_H);
		gl2.glEnd();
	}

	private void paintPlotter(GL2 gl2, float gx, float gy) {
		// plotter
		gl2.glColor3f(0, 0, 1);
		drawCircle(gl2, gx, gy, PEN_HOLDER_RADIUS_5);
	}

/*
	@Override
	public Point2D getHome() {
		return new Point2D(0,0);
	}

	@Override
	public String getVersion() {
		return "0";
	}

	@Override
	public boolean canChangeMachineSize() {
		return true;
	}

	@Override
	public boolean canAccelerate() {
		return true;
	}

	@Override
	public float getWidth() {
		return 3*12*25.4f;
	}

	@Override
	public float getHeight() {
		return 4*12*25.4f;
	}

	@Override
	public boolean canAutoHome() {
		return true;
	}

	@Override
	public boolean canChangeHome() {
		return false;
	}

	@Override
	public float getFeedrateMax() {
		return 100;
	}

	@Override
	public float getFeedrateDefault() {
		return 100;
	}

	@Override
	public float getAccelerationMax() {
		return 150;
	}

	@Override
	public float getPenLiftTime() {
		return 80;
	}

	@Override
	public float getZAngleOn() {
		return 30;
	}*/
}
