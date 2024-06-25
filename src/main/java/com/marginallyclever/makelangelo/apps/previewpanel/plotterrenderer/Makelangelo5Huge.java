package com.marginallyclever.makelangelo.apps.previewpanel.plotterrenderer;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.texture.TextureFactory;
import com.marginallyclever.makelangelo.texture.TextureWithMetadata;

import static com.marginallyclever.convenience.helpers.DrawingHelper.drawCircle;
import static com.marginallyclever.convenience.helpers.DrawingHelper.paintTexture;

public class Makelangelo5Huge implements PlotterRenderer {
	private static TextureWithMetadata textureMainBody;
	private static TextureWithMetadata textureMotorMounts;
	private static TextureWithMetadata textureLogo;
	private static TextureWithMetadata textureWeight;
	private static TextureWithMetadata textureGondola;
	private static TextureWithMetadata textureArm;

	@Override
	public void render(GL3 gl, Plotter robot) {
		if (textureMainBody == null) textureMainBody = TextureFactory.loadTexture("/textures/huge.png");
		if (textureMotorMounts == null) textureMotorMounts = TextureFactory.loadTexture("/textures/huge-motors.png");
		if (textureLogo == null) textureLogo = TextureFactory.loadTexture("/logo.png");
		if (textureWeight == null) textureWeight = TextureFactory.loadTexture("/textures/weight.png");
		if (textureGondola == null) textureGondola = TextureFactory.loadTexture("/textures/phBody.png");
		if (textureArm == null) textureArm = TextureFactory.loadTexture("/textures/phArm2.png");

		if (textureMainBody == null) {
			paintControlBoxPlain(gl, robot);
		} else {
			paintControlBoxFancy(gl, robot, textureMainBody);
		}

		Polargraph.paintSafeArea(gl, robot);

		if (robot.getDidFindHome())
			paintPenHolderToCounterweights(gl, robot);

		if (textureMotorMounts == null) {
			Polargraph.paintMotors(gl, robot);
		} else {
			paintControlBoxFancy(gl, robot, textureMotorMounts);
		}

		if (textureLogo == null) {
			// paintLogo(gl,robot);
		} else {
			paintLogoFancy(gl, robot);
		}
	}

	private void paintControlBoxFancy(GL3 gl, Plotter robot,TextureWithMetadata texture) {
		double left = robot.getSettings().getDouble(PlotterSettings.LIMIT_LEFT);

		final double scaleX = 1366 / 943.0; // machine is 1366 motor-to-motor. texture is 922. scaleX accordingly.
		final double width = 1024 * scaleX;
		final double height = 1024 * scaleX;
		final double ox = left - 51 * scaleX; // 106 taken from offset in texture map
		final double oy = -280 * scaleX; // 109 taken from offset in texture map. TODO why -15 instead of top?

		paintTexture(gl, texture, ox, oy, width, height);
	}

	public void paintPenHolderToCounterweights(GL3 gl, Plotter robot) {
		Point2D pos = robot.getPos();
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
		double right_b = (beltLength - right_a) / 2 - 55;


		// belt from motor to pen holder left
		drawBeltMinus10(gl,left,top,gx,gy);
		// belt from motor to pen holder right
		drawBeltMinus10(gl,right,top,gx,gy);

		// belt from motor to counterweight left
		paintBeltSide(gl,left,top,left_b);
		// belt from motor to counterweight right
		paintBeltSide(gl,right,top,right_b);

		paintGondola(gl,gx,gy,robot);

		// left
		paintCounterweight(gl,left,top-left_b);
		// right
		paintCounterweight(gl,right,top-right_b);
	}

	private void drawBeltMinus10(GL3 gl, double cornerX, double cornerY, double penX, double penY) {
		double dx = penX - cornerX;
		double dy = penY - cornerY;
		double len = Math.sqrt(dx * dx + dy * dy);
		penX = cornerX + dx * (len-100) / len;
		penY = cornerY + dy * (len-100) / len;

		gl.glBegin(GL3.GL_LINES);
		gl.glColor3d(0.2, 0.2, 0.2);
		gl.glVertex2d(cornerX, cornerY);
		gl.glVertex2d(penX, penY);
		gl.glEnd();
	}

	private static void paintBeltSide(GL3 gl,double x, double y, double length) {
		gl.glBegin(GL3.GL_LINES);
		gl.glVertex2d(x , y);
		gl.glVertex2d(x, y - length);
		gl.glEnd();
	}

	private void paintGondola(GL3 gl, double gx, double gy,Plotter robot) {
		if(textureGondola!=null && textureArm!=null) {
			paintGondolaFancy(gl,gx,gy,robot);
			return;
		}
		Polargraph.drawCircle(gl, gx, gy, Polargraph.PEN_HOLDER_RADIUS_2, 20);
		if (robot.getPenIsUp()) {
			Polargraph.drawCircle(gl, gx, gy, Polargraph.PEN_HOLDER_RADIUS_2 + 5, 20);
		}
	}

	private void paintGondolaFancy(GL3 gl, double gx, double gy,Plotter robot) {
		double top = robot.getSettings().getDouble(PlotterSettings.LIMIT_TOP);
		double left = robot.getSettings().getDouble(PlotterSettings.LIMIT_LEFT);
		double right = robot.getSettings().getDouble(PlotterSettings.LIMIT_RIGHT);
		// get angle from top-left to gx,gy
		double dx = gx - left;
		double dy = gy - top;
		double angleLeft = Math.atan2(dy, dx);
		//get angle from top-right to gx,gy
		dx = gx - right;
		double angleRight = Math.atan2(dy, dx);

		gl.glPushMatrix();
		gl.glTranslated(gx,gy,0);
		gl.glRotated(Math.toDegrees(angleLeft)+90,0,0,1);
		paintTexture(gl,textureArm,-100,-100,200,200);
		gl.glPopMatrix();

		gl.glPushMatrix();
		gl.glTranslated(gx,gy,0);
		gl.glRotated(Math.toDegrees(angleRight)+90,0,0,1);
		paintTexture(gl,textureArm,-100,-100,200,200);
		gl.glPopMatrix();

		// paint body last so it's on top
		paintTexture(gl,textureGondola,gx-50,gy-50,100,100);
	}

	private void paintCounterweight(GL3 gl,double x,double y) {
		if(textureWeight==null) {
			Polargraph.paintCounterweight(gl,x,y);
			return;
		}

		paintTexture(gl, textureWeight, x-20, y-74, 40,80);
	}

	/**
	 * paint the Marginally Clever Logo
	 *
	 * @param gl   the render context
	 * @param robot the machine to draw.
	 */
	private void paintLogoFancy(GL3 gl, Plotter robot) {
		final double scale = 0.5;
		final double TW = 128 * scale;
		final double TH = 128 * scale;

		final float LOGO_X = (float)robot.getSettings().getDouble(PlotterSettings.LIMIT_LEFT) - 65; // bottom left corner of safe Area
		final float LOGO_Y = (float)robot.getSettings().getDouble(PlotterSettings.LIMIT_BOTTOM)+10;

		paintTexture(gl, textureLogo, LOGO_X, LOGO_Y, TW, TH);
	}

	/**
	 * paint the controller and the LCD panel
	 *
	 * @param gl   the render context
	 * @param robot the machine to draw.
	 */
	private void paintControlBoxPlain(GL3 gl, Plotter robot) {
		double cy = robot.getSettings().getDouble(PlotterSettings.LIMIT_TOP);
		double left = robot.getSettings().getDouble(PlotterSettings.LIMIT_LEFT);
		double right = robot.getSettings().getDouble(PlotterSettings.LIMIT_RIGHT);
		double top = robot.getSettings().getDouble(PlotterSettings.LIMIT_TOP);
		double cx = 0;

		gl.glPushMatrix();

		drawSuctionCups(gl,left,right,top);
		drawFrame(gl,left,right,top);
		gl.glTranslated(cx, cy, 0);
		drawWires(gl,left,right);
		drawRUMBA(gl,left,right);
		renderLCD(gl,left,right);
		gl.glPopMatrix();
	}

	// RUMBA in v3 (135mm*75mm)
	private void drawRUMBA(GL3 gl, double left, double right) {
		float h = 75f / 2;
		float w = 135f / 2;
		gl.glPushMatrix();
		gl.glTranslated(right-650.0/2.0,0,0);

			gl.glColor3d(0.9, 0.9, 0.9);
			gl.glBegin(GL3.GL_QUADS);
			gl.glVertex2d(-w, h);
			gl.glVertex2d(+w, h);
			gl.glVertex2d(+w, -h);
			gl.glVertex2d(-w, -h);
			gl.glEnd();
		gl.glPopMatrix();
	}

	private void drawWires(GL3 gl, double left, double right) {
		// wires to each motor
		gl.glBegin(GL3.GL_LINES);
		final float SPACING = 2;
		float y = SPACING * -1.5f;
		gl.glColor3f(1, 0, 0);
		gl.glVertex2d(0, y);
		gl.glVertex2d(left, y);
		y += SPACING;
		gl.glColor3f(0, 1, 0);
		gl.glVertex2d(0, y);
		gl.glVertex2d(left, y);
		y += SPACING;
		gl.glColor3f(0, 0, 1);
		gl.glVertex2d(0, y);
		gl.glVertex2d(left, y);
		y += SPACING;
		gl.glColor3f(1, 1, 0);
		gl.glVertex2d(0, y);
		gl.glVertex2d(left, y);
		y += SPACING;

		y = SPACING * -1.5f;
		gl.glColor3f(1, 0, 0);
		gl.glVertex2d(0, y);
		gl.glVertex2d(right, y);
		y += SPACING;
		gl.glColor3f(0, 1, 0);
		gl.glVertex2d(0, y);
		gl.glVertex2d(right, y);
		y += SPACING;
		gl.glColor3f(0, 0, 1);
		gl.glVertex2d(0, y);
		gl.glVertex2d(right, y);
		y += SPACING;
		gl.glColor3f(1, 1, 0);
		gl.glVertex2d(0, y);
		gl.glVertex2d(right, y);
		y += SPACING;
		gl.glEnd();
	}

	private void drawFrame(GL3 gl, double left, double right, double top) {
		final float FRAME_SIZE = 50f; // mm
		gl.glColor3d(1, 0.8f, 0.5f);
		gl.glBegin(GL3.GL_QUADS);
		gl.glVertex2d(left - FRAME_SIZE, top + FRAME_SIZE);
		gl.glVertex2d(right + FRAME_SIZE, top + FRAME_SIZE);
		gl.glVertex2d(right + FRAME_SIZE, top - FRAME_SIZE);
		gl.glVertex2d(left - FRAME_SIZE, top - FRAME_SIZE);
		gl.glEnd();
	}

	private void drawSuctionCups(GL3 gl,double left,double right,double top) {
		final float SUCTION_CUP_Y = 35f;
		final float SUCTION_CUP_RADIUS = 32.5f; /// mm
		gl.glColor3f(1, 1f, 1f); // #color of suction cups
		drawCircle(gl, (float) left - SUCTION_CUP_Y, (float) top - SUCTION_CUP_Y, SUCTION_CUP_RADIUS);
		drawCircle(gl, (float) left - SUCTION_CUP_Y, (float) top + SUCTION_CUP_Y, SUCTION_CUP_RADIUS);
		drawCircle(gl, (float) right + SUCTION_CUP_Y, (float) top - SUCTION_CUP_Y, SUCTION_CUP_RADIUS);
		drawCircle(gl, (float) right + SUCTION_CUP_Y, (float) top + SUCTION_CUP_Y, SUCTION_CUP_RADIUS);
	}

	private void renderLCD(GL3 gl, double left, double right) {
		// position
		gl.glPushMatrix();
		gl.glTranslated(right-(650.0/2.0)-180,0,0);

		// LCD red
		float w = 150f / 2;
		float h = 56f / 2;
		gl.glColor3f(0.8f, 0.0f, 0.0f);
		gl.glBegin(GL3.GL_QUADS);
		gl.glVertex2d(-w, h);
		gl.glVertex2d(+w, h);
		gl.glVertex2d(+w, -h);
		gl.glVertex2d(-w, -h);
		gl.glEnd();

		// LCD green
		gl.glPushMatrix();
		gl.glTranslated(-(2.6) / 2, -0.771, 0);

		w = 98f / 2;
		h = 60f / 2;
		gl.glColor3f(0, 0.6f, 0.0f);
		gl.glBegin(GL3.GL_QUADS);
		gl.glVertex2d(-w, h);
		gl.glVertex2d(+w, h);
		gl.glVertex2d(+w, -h);
		gl.glVertex2d(-w, -h);
		gl.glEnd();

		// LCD black
		h = 40f / 2;
		gl.glColor3f(0, 0, 0);
		gl.glBegin(GL3.GL_QUADS);
		gl.glVertex2d(-w, h);
		gl.glVertex2d(+w, h);
		gl.glVertex2d(+w, -h);
		gl.glVertex2d(-w, -h);
		gl.glEnd();

		// LCD blue
		h = 25f / 2;
		w = 75f / 2;
		gl.glColor3f(0, 0, 0.7f);
		gl.glBegin(GL3.GL_QUADS);
		gl.glVertex2d(-w, h);
		gl.glVertex2d(+w, h);
		gl.glVertex2d(+w, -h);
		gl.glVertex2d(-w, -h);
		gl.glEnd();

		gl.glPopMatrix();

		// clean up
		gl.glPopMatrix();
	}

}
