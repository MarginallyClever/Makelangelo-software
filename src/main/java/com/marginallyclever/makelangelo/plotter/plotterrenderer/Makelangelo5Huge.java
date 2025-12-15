package com.marginallyclever.makelangelo.plotter.plotterrenderer;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.DrawingHelper;
import com.marginallyclever.makelangelo.Mesh;
import com.marginallyclever.makelangelo.preview.ShaderProgram;
import com.marginallyclever.makelangelo.texture.TextureFactory;
import com.marginallyclever.makelangelo.texture.TextureWithMetadata;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;

import javax.vecmath.Point2d;

import java.awt.*;

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
	public void render(ShaderProgram shader, GL3 gl, Plotter robot) {
		if (textureMainBody == null) textureMainBody = TextureFactory.loadTexture("/textures/huge.png");
		if (textureMotorMounts == null) textureMotorMounts = TextureFactory.loadTexture("/textures/huge-motors.png");
		if (textureLogo == null) textureLogo = TextureFactory.loadTexture("/logo.png");
		if (textureWeight == null) textureWeight = TextureFactory.loadTexture("/textures/weight.png");
		if (textureGondola == null) textureGondola = TextureFactory.loadTexture("/textures/phBody.png");
		if (textureArm == null) textureArm = TextureFactory.loadTexture("/textures/phArm2.png");

		if (textureMainBody == null) {
			paintControlBoxPlain(shader, gl, robot);
		} else {
			paintControlBoxFancy(shader, gl, robot, textureMainBody);
		}

		Polargraph.paintSafeArea(shader, gl, robot);

		if (robot.getDidFindHome())
			paintPenHolderToCounterweights(shader, gl, robot);

		if (textureMotorMounts == null) {
			Polargraph.paintMotors(shader, gl, robot);
		} else {
			paintControlBoxFancy(shader, gl, robot, textureMotorMounts);
		}

		if (textureLogo == null) {
			// paintLogo(gl2,robot);
		} else {
			paintLogoFancy(shader, gl, robot);
		}
	}

	private void paintControlBoxFancy(ShaderProgram shader, GL3 gl, Plotter robot,TextureWithMetadata texture) {
		double left = robot.getSettings().getDouble(PlotterSettings.LIMIT_LEFT);

		final double scaleX = 1366 / 943.0; // machine is 1366 motor-to-motor. texture is 922. scaleX accordingly.
		final double width = 1024 * scaleX;
		final double height = 1024 * scaleX;
		final double ox = left - 51 * scaleX; // 106 taken from offset in texture map
		final double oy = -280 * scaleX; // 109 taken from offset in texture map. TODO why -15 instead of top?

		paintTexture(shader, gl, texture, ox, oy, width, height);
	}

	public void paintPenHolderToCounterweights(ShaderProgram shader, GL3 gl, Plotter robot) {
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
		paintCounterweight(shader, gl, left,top-left_b);
		// right
		paintCounterweight(shader, gl, right,top-right_b);
	}

	private void drawBeltMinus10(GL3 gl2, double cornerX, double cornerY, double penX, double penY) {
		float dx = (float)(penX - cornerX);
		float dy = (float)(penY - cornerY);
		float len = (float)Math.sqrt(dx * dx + dy * dy);
		penX = cornerX + dx * (len-100) / len;
		penY = cornerY + dy * (len-100) / len;

		Mesh mesh = new Mesh();
		mesh.setRenderStyle(GL3.GL_LINES);
		mesh.addColor(0.2f, 0.2f, 0.2f,1.0f);		mesh.addVertex((float)cornerX, (float)cornerY, 0);
		mesh.addColor(0.2f, 0.2f, 0.2f,1.0f);		mesh.addVertex((float)penX, (float)penY, 0);
		mesh.render(gl2);
	}

	private static void paintBeltSide(GL3 gl2,double x, double y, double length) {
		Mesh mesh = new Mesh();
		mesh.setRenderStyle(GL3.GL_LINES);
		mesh.addColor(0.2f, 0.2f, 0.2f,1.0f);		mesh.addVertex((float)x, (float)y, 0);
		mesh.addColor(0.2f, 0.2f, 0.2f,1.0f);		mesh.addVertex((float)x, (float)(y-length), 0);
		mesh.render(gl2);
	}

	private void paintGondola(GL3 gl2, double gx, double gy,Plotter robot) {
		if(textureGondola!=null && textureArm!=null) {
			paintGondolaFancy(gl2,gx,gy,robot);
			return;
		}
		DrawingHelper.drawCircle(gl2, (float)gx, (float)gy, Polargraph.PEN_HOLDER_RADIUS_2, Color.BLACK);
		if (robot.getPenIsUp()) {
			DrawingHelper.drawCircle(gl2, (float)gx, (float)gy, Polargraph.PEN_HOLDER_RADIUS_2 + 5, Color.BLACK);
		}
	}

	private void paintGondolaFancy(GL3 gl2, double gx, double gy,Plotter robot) {/*
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

		gl2.glPushMatrix();
		gl2.glTranslated(gx,gy,0);
		gl2.glRotated(Math.toDegrees(angleLeft)+90,0,0,1);
		paintTexture(gl2,textureArm,-100,-100,200,200);
		gl2.glPopMatrix();

		gl2.glPushMatrix();
		gl2.glTranslated(gx,gy,0);
		gl2.glRotated(Math.toDegrees(angleRight)+90,0,0,1);
		paintTexture(gl2,textureArm,-100,-100,200,200);
		gl2.glPopMatrix();

		// paint body last so it's on top
		paintTexture(gl2,textureGondola,gx-50,gy-50,100,100);*/
	}

	private void paintCounterweight(ShaderProgram shader, GL3 gl,double x,double y) {
		if(textureWeight==null) {
			Polargraph.paintCounterweight(shader,(float)x,(float)y);
			return;
		}

		paintTexture(shader, gl, textureWeight, x-20, y-74, 40,80);
	}

	/**
	 * paint the Marginally Clever Logo
	 *
	 * @param shader the render context
	 * @param robot the machine to draw.
	 */
	private void paintLogoFancy(ShaderProgram shader, GL3 gl, Plotter robot) {
		final double scale = 0.5;
		final double TW = 128 * scale;
		final double TH = 128 * scale;

		final float LOGO_X = (float)robot.getSettings().getDouble(PlotterSettings.LIMIT_LEFT) - 65; // bottom left corner of safe Area
		final float LOGO_Y = (float)robot.getSettings().getDouble(PlotterSettings.LIMIT_BOTTOM)+10;

		paintTexture(shader, gl, textureLogo, LOGO_X, LOGO_Y, TW, TH);
	}

	/**
	 * paint the controller and the LCD panel
	 *
	 * @param shader the render context
	 * @param robot the machine to draw.
	 */
	private void paintControlBoxPlain(ShaderProgram shader, GL3 gl, Plotter robot) {
		double cy = robot.getSettings().getDouble(PlotterSettings.LIMIT_TOP);
		double left = robot.getSettings().getDouble(PlotterSettings.LIMIT_LEFT);
		double right = robot.getSettings().getDouble(PlotterSettings.LIMIT_RIGHT);
		double top = robot.getSettings().getDouble(PlotterSettings.LIMIT_TOP);
		double cx = 0;
/*
		gl2.glPushMatrix();

		drawSuctionCups(gl2,left,right,top);
		drawFrame(gl2,left,right,top);
		gl2.glTranslated(cx, cy, 0);
		drawWires(gl2,left,right);
		drawRUMBA(gl2,left,right);
		renderLCD(gl2,left,right);

		gl2.glPopMatrix();*/
	}

	// RUMBA in v3 (135mm*75mm)
	private void drawRUMBA(GL3 gl2, double left, double right) {/*
		float h = 75f / 2;
		float w = 135f / 2;
		gl2.glPushMatrix();
		gl2.glTranslated(right-650.0/2.0,0,0);

			gl2.glColor3d(0.9, 0.9, 0.9);
			gl2.glBegin(GL3.GL_TRIANGLE_FAN);
			gl2.glVertex2d(-w, h);
			gl2.glVertex2d(+w, h);
			gl2.glVertex2d(+w, -h);
			gl2.glVertex2d(-w, -h);
			gl2.glEnd();
		gl2.glPopMatrix();*/
	}

	private void drawWires(GL3 gl2, double left, double right) {/*
		// wires to each motor
		gl2.glBegin(GL3.GL_LINES);
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
		gl2.glEnd();*/
	}

	private void drawFrame(GL3 gl2, double left, double right, double top) {/*
		final float FRAME_SIZE = 50f; // mm
		gl2.glColor3d(1, 0.8f, 0.5f);
		gl2.glBegin(GL3.GL_TRIANGLE_FAN);
		gl2.glVertex2d(left - FRAME_SIZE, top + FRAME_SIZE);
		gl2.glVertex2d(right + FRAME_SIZE, top + FRAME_SIZE);
		gl2.glVertex2d(right + FRAME_SIZE, top - FRAME_SIZE);
		gl2.glVertex2d(left - FRAME_SIZE, top - FRAME_SIZE);
		gl2.glEnd();*/
	}

	private void drawSuctionCups(GL3 gl2,double left,double right,double top) {
		final float SUCTION_CUP_Y = 35f;
		final float SUCTION_CUP_RADIUS = 32.5f; /// mm
		var c = new Color(1f, 1f, 1f); // #color of suction cups
		drawCircle(gl2, (float) left - SUCTION_CUP_Y, (float) top - SUCTION_CUP_Y, SUCTION_CUP_RADIUS, c);
		drawCircle(gl2, (float) left - SUCTION_CUP_Y, (float) top + SUCTION_CUP_Y, SUCTION_CUP_RADIUS, c);
		drawCircle(gl2, (float) right + SUCTION_CUP_Y, (float) top - SUCTION_CUP_Y, SUCTION_CUP_RADIUS, c);
		drawCircle(gl2, (float) right + SUCTION_CUP_Y, (float) top + SUCTION_CUP_Y, SUCTION_CUP_RADIUS, c);
	}

	private void renderLCD(GL3 gl2, double left, double right) {/*
		// position
		gl2.glPushMatrix();
		gl2.glTranslated(right-(650.0/2.0)-180,0,0);

		// LCD red
		float w = 150f / 2;
		float h = 56f / 2;
		gl2.glColor3f(0.8f, 0.0f, 0.0f);
		gl2.glBegin(GL3.GL_TRIANGLE_FAN);
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
		gl2.glBegin(GL3.GL_TRIANGLE_FAN);
		gl2.glVertex2d(-w, h);
		gl2.glVertex2d(+w, h);
		gl2.glVertex2d(+w, -h);
		gl2.glVertex2d(-w, -h);
		gl2.glEnd();

		// LCD black
		h = 40f / 2;
		gl2.glColor3f(0, 0, 0);
		gl2.glBegin(GL3.GL_TRIANGLE_FAN);
		gl2.glVertex2d(-w, h);
		gl2.glVertex2d(+w, h);
		gl2.glVertex2d(+w, -h);
		gl2.glVertex2d(-w, -h);
		gl2.glEnd();

		// LCD blue
		h = 25f / 2;
		w = 75f / 2;
		gl2.glColor3f(0, 0, 0.7f);
		gl2.glBegin(GL3.GL_TRIANGLE_FAN);
		gl2.glVertex2d(-w, h);
		gl2.glVertex2d(+w, h);
		gl2.glVertex2d(+w, -h);
		gl2.glVertex2d(-w, -h);
		gl2.glEnd();

		gl2.glPopMatrix();

		// clean up
		gl2.glPopMatrix();*/
	}
}
