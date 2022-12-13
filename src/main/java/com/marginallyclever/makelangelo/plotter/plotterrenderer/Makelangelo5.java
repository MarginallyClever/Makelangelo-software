package com.marginallyclever.makelangelo.plotter.plotterrenderer;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.Texture;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.settings.PlotterSettings;

import static com.marginallyclever.convenience.DrawingHelper.*;

public class Makelangelo5 implements PlotterRenderer {

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
		if (texture1 == null) texture1 = loadTexture("/textures/makelangelo5.png");
		if (texture2 == null) texture2 = loadTexture("/textures/makelangelo5-motors.png");
		if (texture3 == null) texture3 = loadTexture("/logo.png");

		if (texture1 == null) {
			paintControlBoxPlain(gl2, robot);
		} else {
			paintControlBoxFancy(gl2, robot,texture1);
		}

		Polargraph.paintSafeArea(gl2, robot);

		if (robot.getDidFindHome())
			Polargraph.paintPenHolderToCounterweights(gl2, robot);

		if (texture1 == null || texture2 == null) {
			Polargraph.paintMotors(gl2, robot);
		} else {
			paintControlBoxFancy(gl2, robot,texture2);
		}

		if (texture3 == null) {
			// paintLogo(gl2,robot);
		} else {
			paintLogoFancy(gl2, robot);
		}
	}

	private void paintControlBoxFancy(GL2 gl2, Plotter robot,Texture texture) {
		double left = robot.getLimitLeft();
		// double top = robot.getLimitTop();

		final double scale = 650.0 / 811.0; // machine is 650 motor-to-motor. texture is 811. scale accordingly.
		final double TW = 1024 * scale;
		final double TH = 1024 * scale;
		final double ox = left - 106 * scale; // 106 taken from offset in texture map
		final double oy = -15 - 190 * scale; // 109 taken from offset in texture map. TODO why -15 instead of top?

		paintTexture(gl2, texture, ox, oy, TW, TH);
	}

	/**
	 * paint the Marginally Clever Logo
	 *
	 * @param gl2   the render context
	 * @param robot the machine to draw.
	 */
	private void paintLogoFancy(GL2 gl2, Plotter robot) {
		final double scale = 0.5;
		final double TW = 128 * scale;
		final double TH = 128 * scale;

		final float LOGO_X = (float)robot.getLimitLeft() - 65; // bottom left corner of safe Area
		final float LOGO_Y = (float)robot.getLimitBottom()+10;

		paintTexture(gl2, texture3, LOGO_X, LOGO_Y, TW, TH);
	}

	/**
	 * paint the controller and the LCD panel
	 *
	 * @param gl2   the render context
	 * @param robot the machine to draw.
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

		renderLCD(gl2);
		gl2.glPopMatrix();
	}

	private void renderLCD(GL2 gl2) {
		// position
		gl2.glPushMatrix();
		gl2.glTranslated(-180, 0, 0);

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
}
