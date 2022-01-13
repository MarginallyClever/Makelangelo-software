package com.marginallyclever.makelangelo.plotter.plotterRenderer;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.marginallyclever.convenience.FileAccess;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.settings.PlotterSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Makelangelo5 implements PlotterRenderer {
	private static final Logger logger = LoggerFactory.getLogger(Makelangelo5.class);

	public final static float PEN_HOLDER_RADIUS_5 = 25; // mm
	public final static double COUNTERWEIGHT_W = 30;
	public final static double COUNTERWEIGHT_H = 60;
	public final static double PULLEY_RADIUS = 1.27;
	public final static double MOTOR_WIDTH = 42;
	private static Texture texture1;
	private static Texture texture2;
	private static Texture texture3;

	@Override
	public void render(GL2 gl2, Plotter robot) {
		if (Makelangelo5.texture1 == null) Makelangelo5.texture1 = loadTexture(gl2, "/makelangelo5.png");
		if (Makelangelo5.texture2 == null) Makelangelo5.texture2 = loadTexture(gl2, "/makelangelo5-motors.png");
		if (Makelangelo5.texture3 == null) Makelangelo5.texture3 = loadTexture(gl2, "/logo.png");

		if (Makelangelo5.texture1 == null) {
			paintControlBoxPlain(gl2, robot);
		} else {
			Makelangelo5.texture1.bind(gl2);
			paintControlBoxFancy(gl2, robot);
		}

		paintSafeArea(gl2, robot);

		if (robot.getDidFindHome())
			paintPenHolderToCounterweights(gl2, robot);

		if (Makelangelo5.texture2 == null) {
			Polargraph.paintMotors(gl2, robot);
		} else {
			Makelangelo5.texture2.bind(gl2);
			paintControlBoxFancy(gl2, robot);
		}

		if (Makelangelo5.texture3 == null) {
			// paintLogo(gl2,robot);
		} else {
			Makelangelo5.texture3.bind(gl2);
			paintLogoFancy(gl2, robot);
		}
	}

	private void paintControlBoxFancy(GL2 gl2, Plotter robot) {
		double left = robot.getLimitLeft();
		// double top = robot.getLimitTop();

		final double scale = 650.0 / 811.0; // machine is 650 motor-to-motor. texture is 811. scale accordingly.
		final double TW = 1024 * scale;
		final double TH = 1024 * scale;
		final double ox = left - 106 * scale; // 106 taken from offset in texture map
		final double oy = -15 - 190 * scale; // 109 taken from offset in texture map. TODO why -15 instead of top?

		gl2.glColor4d(1, 1, 1, 1);
		gl2.glEnable(GL2.GL_TEXTURE_2D);

		gl2.glBegin(GL2.GL_QUADS);
		gl2.glTexCoord2d(0, 0);
		gl2.glVertex2d(ox, oy);
		gl2.glTexCoord2d(1, 0);
		gl2.glVertex2d(ox + TW, oy);
		gl2.glTexCoord2d(1, 1);
		gl2.glVertex2d(ox + TW, oy + TH);
		gl2.glTexCoord2d(0, 1);
		gl2.glVertex2d(ox, oy + TH);
		gl2.glEnd();

		gl2.glDisable(GL2.GL_TEXTURE_2D);
	}

	private Texture loadTexture(GL2 gl2, String name) {
		Texture tex = null;
		try {
			tex = TextureIO.newTexture(FileAccess.open(name), false, name.substring(name.lastIndexOf('.') + 1));
		} catch (IOException e) {
			logger.debug("Can't load {}", name, e);
		}
		return tex;
	}

	/**
	 * paint the Marginally Clever Logo
	 * 
	 * @param gl2
	 * @param settings
	 */
	private void paintLogoFancy(GL2 gl2, Plotter robot) {
		double left = robot.getLimitLeft();
		double right = robot.getLimitRight();

		final double scale = 0.5;
		final double TW = 128 * scale;
		final double TH = 128 * scale;

		final float LOGO_X = ((float) left - (float) right) / 2 - 65; // bottom left corner of safe Area
		final float LOGO_Y = 0 - 490;

		// final float LOGO_X= 0 - ((float)left - (float)right)/2 - 160; // different
		// coordinates in the main Wooden Base
		// final float LOGO_Y= 0 + 470;

		gl2.glColor4d(1, 1, 1, 1);
		gl2.glEnable(GL2.GL_TEXTURE_2D);

		gl2.glBegin(GL2.GL_QUADS);
		gl2.glTexCoord2d(0, 0);
		gl2.glVertex2d(LOGO_X, LOGO_Y);
		gl2.glTexCoord2d(1, 0);
		gl2.glVertex2d(LOGO_X + TW, LOGO_Y);
		gl2.glTexCoord2d(1, 1);
		gl2.glVertex2d(LOGO_X + TW, LOGO_Y + TH);
		gl2.glTexCoord2d(0, 1);
		gl2.glVertex2d(LOGO_X, LOGO_Y + TH);
		gl2.glEnd();

		gl2.glDisable(GL2.GL_TEXTURE_2D);
	}

	/**
	 * paint the controller and the LCD panel
	 * 
	 * @param gl2
	 * @param settings
	 */
	private void paintControlBoxPlain(GL2 gl2, Plotter robot) {
		double cy = robot.getLimitTop();
		double left = robot.getLimitLeft();
		double right = robot.getLimitRight();
		double top = robot.getLimitTop();
		double cx = 0;

		gl2.glPushMatrix();

		// mounting plate for PCB
		final float SUCTION_CUP_Y = 35f;
		final float SUCTION_CUP_RADIUS = 32.5f; /// mm
		final float FRAME_SIZE = 50f; // mm

		gl2.glColor3f(1, 1f, 1f); // #color of suction cups
		drawCircle(gl2, (float) left - SUCTION_CUP_Y, (float) top - SUCTION_CUP_Y, SUCTION_CUP_RADIUS);
		drawCircle(gl2, (float) left - SUCTION_CUP_Y, (float) top + SUCTION_CUP_Y, SUCTION_CUP_RADIUS);
		drawCircle(gl2, (float) right + SUCTION_CUP_Y, (float) top - SUCTION_CUP_Y, SUCTION_CUP_RADIUS);
		drawCircle(gl2, (float) right + SUCTION_CUP_Y, (float) top + SUCTION_CUP_Y, SUCTION_CUP_RADIUS);

		gl2.glColor3d(1, 0.8f, 0.5f);
		// frame
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(left - FRAME_SIZE, top + FRAME_SIZE);
		gl2.glVertex2d(right + FRAME_SIZE, top + FRAME_SIZE);
		gl2.glVertex2d(right + FRAME_SIZE, top - FRAME_SIZE);
		gl2.glVertex2d(left - FRAME_SIZE, top - FRAME_SIZE);
		gl2.glEnd();

		gl2.glTranslated(cx, cy, 0);

		// wires to each motor
		gl2.glBegin(GL2.GL_LINES);
		final float SPACING = 2;
		float y = SPACING * -1.5f;
		gl2.glColor3f(1, 0, 0);
		gl2.glVertex2d(0, y);
		gl2.glVertex2d(left, y);
		y += SPACING;
		gl2.glColor3f(0, 1, 0);
		gl2.glVertex2d(0, y);
		gl2.glVertex2d(left, y);
		y += SPACING;
		gl2.glColor3f(0, 0, 1);
		gl2.glVertex2d(0, y);
		gl2.glVertex2d(left, y);
		y += SPACING;
		gl2.glColor3f(1, 1, 0);
		gl2.glVertex2d(0, y);
		gl2.glVertex2d(left, y);
		y += SPACING;

		y = SPACING * -1.5f;
		gl2.glColor3f(1, 0, 0);
		gl2.glVertex2d(0, y);
		gl2.glVertex2d(right, y);
		y += SPACING;
		gl2.glColor3f(0, 1, 0);
		gl2.glVertex2d(0, y);
		gl2.glVertex2d(right, y);
		y += SPACING;
		gl2.glColor3f(0, 0, 1);
		gl2.glVertex2d(0, y);
		gl2.glVertex2d(right, y);
		y += SPACING;
		gl2.glColor3f(1, 1, 0);
		gl2.glVertex2d(0, y);
		gl2.glVertex2d(right, y);
		y += SPACING;
		gl2.glEnd();

		// RUMBA in v3 (135mm*75mm)
		float h = 75f / 2;
		float w = 135f / 2;
		gl2.glColor3d(0.9, 0.9, 0.9); // background #color RUMBA
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(-w, h);
		gl2.glVertex2d(+w, h);
		gl2.glVertex2d(+w, -h);
		gl2.glVertex2d(-w, -h);
		gl2.glEnd();

		/*
		 * border around RUMBA gl2.glLineWidth(1); gl2.glColor3f(0,0,0);
		 * gl2.glBegin(GL2.GL_LINE_LOOP); gl2.glVertex2d(-w-1, h+1);
		 * gl2.glVertex2d(+w+1, h+1); gl2.glVertex2d(+w+1, -h-1); gl2.glVertex2d(-w-1,
		 * -h-1); gl2.glEnd();
		 */
		renderLCD(gl2);
		gl2.glPopMatrix();
	}

	private void renderLCD(GL2 gl2) {
		// position
		gl2.glPushMatrix();
		gl2.glTranslated(-180, 0, 0);
		/*
		 * // mounting plate for LCD gl2.glColor3f(1,0.8f,0.5f);
		 * gl2.glBegin(GL2.GL_QUADS); gl2.glVertex2d(-8, 5); gl2.glVertex2d(+8, 5);
		 * gl2.glVertex2d(+8, -5); gl2.glVertex2d(-8, -5); gl2.glEnd();
		 */

		// LCD red
		float w = 150f / 2;
		float h = 56f / 2;
		gl2.glColor3f(0.8f, 0.0f, 0.0f);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(-w, h);
		gl2.glVertex2d(+w, h);
		gl2.glVertex2d(+w, -h);
		gl2.glVertex2d(-w, -h);
		gl2.glEnd();

		// LCD green
		gl2.glPushMatrix();
		gl2.glTranslated(-(2.6) / 2, -0.771, 0);

		w = 98f / 2;
		h = 60f / 2;
		gl2.glColor3f(0, 0.6f, 0.0f);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(-w, h);
		gl2.glVertex2d(+w, h);
		gl2.glVertex2d(+w, -h);
		gl2.glVertex2d(-w, -h);
		gl2.glEnd();

		// LCD black
		h = 40f / 2;
		gl2.glColor3f(0, 0, 0);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(-w, h);
		gl2.glVertex2d(+w, h);
		gl2.glVertex2d(+w, -h);
		gl2.glVertex2d(-w, -h);
		gl2.glEnd();

		// LCD blue
		h = 25f / 2;
		w = 75f / 2;
		gl2.glColor3f(0, 0, 0.7f);
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glVertex2d(-w, h);
		gl2.glVertex2d(+w, h);
		gl2.glVertex2d(+w, -h);
		gl2.glVertex2d(-w, -h);
		gl2.glEnd();

		gl2.glPopMatrix();

		// clean up
		gl2.glPopMatrix();
	}

	private void paintPenHolderToCounterweights(GL2 gl2, Plotter robot) {
		PlotterSettings settings = robot.getSettings();
		double dx, dy;
		Point2D pos = robot.getPos();
		double gx = pos.x;
		double gy = pos.y;

		double top = settings.getLimitTop();
		double bottom = settings.getLimitBottom();
		double left = settings.getLimitLeft();
		double right = settings.getLimitRight();

		if (gx < left)
			return;
		if (gx > right)
			return;
		if (gy > top)
			return;
		if (gy < bottom)
			return;

		float bottleCenter = 8f + 7.5f;

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

		paintPlotter(gl2, robot, (float) gx, (float) gy);

		// belts
		gl2.glBegin(GL2.GL_LINES);
		gl2.glColor3d(0.2, 0.2, 0.2);

		// belt from motor to pen holder left
		gl2.glVertex2d(left, top);
		gl2.glVertex2d(gx, gy);
		// belt from motor to pen holder right
		gl2.glVertex2d(right, top);
		gl2.glVertex2d(gx, gy);

		// belt from motor to counterweight left
		gl2.glVertex2d(left - bottleCenter - PULLEY_RADIUS, top - MOTOR_WIDTH / 2);
		gl2.glVertex2d(left - bottleCenter - PULLEY_RADIUS, top - left_b);
		// belt from motor to counterweight right
		gl2.glVertex2d(right + bottleCenter + PULLEY_RADIUS, top - MOTOR_WIDTH / 2);
		gl2.glVertex2d(right + bottleCenter + PULLEY_RADIUS, top - right_b);
		gl2.glEnd();

		// counterweight left
		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glColor3f(0, 0, 1);
		gl2.glVertex2d(left - PULLEY_RADIUS - bottleCenter - COUNTERWEIGHT_W / 2, top - left_b);
		gl2.glVertex2d(left - PULLEY_RADIUS - bottleCenter + COUNTERWEIGHT_W / 2, top - left_b);
		gl2.glVertex2d(left - PULLEY_RADIUS - bottleCenter + COUNTERWEIGHT_W / 2, top - left_b - COUNTERWEIGHT_H);
		gl2.glVertex2d(left - PULLEY_RADIUS - bottleCenter - COUNTERWEIGHT_W / 2, top - left_b - COUNTERWEIGHT_H);
		gl2.glEnd();

		// counterweight right
		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glColor3f(0, 0, 1);
		gl2.glVertex2d(right + PULLEY_RADIUS + bottleCenter - COUNTERWEIGHT_W / 2, top - right_b);
		gl2.glVertex2d(right + PULLEY_RADIUS + bottleCenter + COUNTERWEIGHT_W / 2, top - right_b);
		gl2.glVertex2d(right + PULLEY_RADIUS + bottleCenter + COUNTERWEIGHT_W / 2, top - right_b - COUNTERWEIGHT_H);
		gl2.glVertex2d(right + PULLEY_RADIUS + bottleCenter - COUNTERWEIGHT_W / 2, top - right_b - COUNTERWEIGHT_H);
		gl2.glEnd();
	}

	private void paintPlotter(GL2 gl2, Plotter robot, float gx, float gy) {
		// plotter
		gl2.glColor3f(0, 0, 1);
		drawCircle(gl2, gx, gy, PEN_HOLDER_RADIUS_5);
		if (robot.getPenIsUp()) {
			drawCircle(gl2, gx, gy, PEN_HOLDER_RADIUS_5 + 5);
		}
	}

	private void drawCircle(GL2 gl2, float x, float y, float r) {
		gl2.glTranslatef(x, y, 0);
		gl2.glBegin(GL2.GL_LINE_LOOP);
		float f;
		for (f = 0; f < 2.0 * Math.PI; f += 0.3f) {
			gl2.glVertex2d(Math.cos(f) * r, Math.sin(f) * r);
		}
		gl2.glEnd();
		gl2.glTranslatef(-x, -y, 0);
	}

	private void paintSafeArea(GL2 gl2, Plotter robot) {
		PlotterSettings settings = robot.getSettings();
		double top = settings.getLimitTop();
		// double bottom = settings.getLimitBottom();
		double left = settings.getLimitLeft();
		double right = settings.getLimitRight();

		// gl2.glColor4f(0.5f,0.5f,0.75f,0.75f); // #color Safe area
		gl2.glColor4f(1, 1, 1, 1); // #color Safe area

		gl2.glBegin(GL2.GL_LINE_LOOP);
		gl2.glVertex2d(left - 70f, top + 70f);
		gl2.glVertex2d(right + 70f, top + 70f);
		gl2.glVertex2d(right + 70f, top - 1000);
		gl2.glVertex2d(left - 70f, top - 1000);
		gl2.glEnd();

		/*
		 * filled rectangle for safe area gl2.glColor3d(0.9,0.9,0.9); // #color Safe
		 * area gl2.glBegin(GL2.GL_QUADS); gl2.glVertex2d(left-70f, top+70f);
		 * gl2.glVertex2d(right+70f, top+70f); gl2.glVertex2d(right+70f, top-1000);
		 * gl2.glVertex2d(left-70f, top-1000); gl2.glEnd();
		 */
	}
/*
	@Override
	public Point2D getHome() {
		return new Point2D(0, -482.65); // assumes 1m belts on 650x1000 machine.
	}

	@Override
	public boolean canChangeMachineSize() {
		return false;
	}

	@Override
	public boolean canAccelerate() {
		return true;
	}

	@Override
	public float getWidth() {
		return 650;
	}

	@Override
	public float getHeight() {
		return 1000;
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
		return 12000;
	}

	@Override
	public float getFeedrateDefault() {
		return 9000;
	}

	@Override
	public float getAccelerationMax() {
		return 2000;
	}

	@Override
	public float getPenLiftTime() {
		return 300;
	}

	@Override
	public float getZAngleOn() {
		return 30;
	}

	public String getGCodeConfig(PlotterSettings settings) {
		return "";
	}*/
}
