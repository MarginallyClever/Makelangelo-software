package com.marginallyclever.makelangelo.plotter.plotterrenderer;

import com.marginallyclever.convenience.helpers.DrawingHelper;
import com.marginallyclever.makelangelo.texture.TextureFactory;
import com.marginallyclever.makelangelo.texture.TextureWithMetadata;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;

import javax.vecmath.Point2d;

import java.awt.*;


public class Makelangelo5 implements PlotterRenderer {
	private final TextureWithMetadata textureMainBody;
	private final TextureWithMetadata textureMotors;
	private final TextureWithMetadata textureLogo;
	private final TextureWithMetadata textureWeight;
	private final TextureWithMetadata textureGondola;
	private final TextureWithMetadata textureArm;

    public Makelangelo5() {
        textureMainBody = TextureFactory.loadTexture("/textures/makelangelo5.png");
        textureMotors = TextureFactory.loadTexture("/textures/makelangelo5-motors.png");
        textureLogo = TextureFactory.loadTexture("/logo.png");
        textureWeight = TextureFactory.loadTexture("/textures/weight.png");
        textureGondola = TextureFactory.loadTexture("/textures/phBody.png");
        textureArm = TextureFactory.loadTexture("/textures/phArm2.png");
    }

	@Override
	public void render(Graphics graphics, Plotter robot) {
        var g2d = (Graphics2D) graphics;

        paintControlBoxFancy(g2d, robot, textureMainBody);

		Polargraph.paintSafeArea(g2d, robot);

		if (robot.getDidFindHome())
			paintPenHolderToCounterweights(g2d, robot);

		paintControlBoxFancy(g2d, robot, textureMotors);
		//paintLogoFancy(g2d, robot);
	}

	public void paintPenHolderToCounterweights(Graphics2D graphics, Plotter robot) {
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
		double beltLength = Math.sqrt(mw * mw + mh * mh) + 50;  // TODO replace with robot.getBeltLength()?

		double dx = gx - left;
		double dy = gy - top;
		double left_a = Math.sqrt(dx * dx + dy * dy);
		double left_b = (beltLength - left_a) / 2 - 55;

		dx = gx - right;
		double right_a = Math.sqrt(dx * dx + dy * dy);
		double right_b = (beltLength - right_a) / 2 - 55;

		// belt from motor to pen holder left
		drawBeltMinus10(graphics,left,top,gx,gy);
		// belt from motor to pen holder right
		drawBeltMinus10(graphics,right,top,gx,gy);

		// belt from motor to counterweight left
		paintBeltSide(graphics,left,top,left_b);
		// belt from motor to counterweight right
		paintBeltSide(graphics,right,top,right_b);

		paintGondola(graphics,gx,gy,robot);

		// left
		paintCounterweight(graphics, left,top-left_b);
		// right
		paintCounterweight(graphics, right,top-right_b);
	}

	private void drawBeltMinus10(Graphics graphics, double cornerX, double cornerY, double penX, double penY) {/*
		double dx = penX - cornerX;
		double dy = penY - cornerY;
		double len = Math.sqrt(dx * dx + dy * dy);
		penX = cornerX + dx * (len-100) / len;
		penY = cornerY + dy * (len-100) / len;

		gl2.glBegin(GL3.GL_LINES);
		gl2.glColor3d(0.2, 0.2, 0.2);
		gl2.glVertex2d(cornerX, cornerY);
		gl2.glVertex2d(penX, penY);
		gl2.glEnd();*/
	}

	private static void paintBeltSide(Graphics graphics,double x, double y, double length) {/*
		gl2.glBegin(GL3.GL_LINES);
		gl2.glVertex2d(x, y);
		gl2.glVertex2d(x, y - length);
		gl2.glEnd();*/
	}

	private void paintGondola(Graphics graphics, double gx, double gy,Plotter robot) {
		if(textureGondola!=null && textureArm!=null) {
			paintGondolaFancy(graphics,gx,gy,robot);
			return;
		}
		DrawingHelper.drawCircle(graphics, (float)gx, (float)gy, Polargraph.PEN_HOLDER_RADIUS_2, Color.WHITE);
		if (robot.getPenIsUp()) {
			DrawingHelper.drawCircle(graphics, (float)gx, (float)gy, Polargraph.PEN_HOLDER_RADIUS_2 + 5, Color.WHITE);
		}
	}

	private void paintGondolaFancy(Graphics graphics, double gx, double gy,Plotter robot) {/*
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
		DrawingHelper.paintTexture(gl2,textureArm,-100,-100,200,200);
		gl2.glPopMatrix();

		gl2.glPushMatrix();
		gl2.glTranslated(gx,gy,0);
		gl2.glRotated(Math.toDegrees(angleRight)+90,0,0,1);
		DrawingHelper.paintTexture(gl2,textureArm,-100,-100,200,200);
		gl2.glPopMatrix();

		// paint body last so it's on top
		DrawingHelper.paintTexture(gl2,textureGondola,gx-50,gy-50,100,100);*/
	}

	private void paintCounterweight(Graphics2D graphics,double x,double y) {
		if(textureWeight==null) {
			Polargraph.paintCounterweight(graphics,(float)x,(float)y);
			return;
		}
		DrawingHelper.paintTexture(graphics, textureWeight, x-20, y-74, 40,80);
	}

	private void paintControlBoxFancy(Graphics2D graphics, Plotter robot,TextureWithMetadata texture) {
		double left = robot.getSettings().getDouble(PlotterSettings.LIMIT_LEFT);
		double top = robot.getSettings().getDouble(PlotterSettings.LIMIT_TOP);

		final double scale = 650.0 / 811.0; // machine is 650 motor-to-motor. texture is 811. scale accordingly.
		final double TW = 1024 * scale;
		final double TH = 1024 * scale;
		final double ox = left - 106 * scale; // 106 taken from offset in texture map
		final double oy = -15 - 190 * scale; // 109 taken from offset in texture map. TODO why -15 instead of top?

		DrawingHelper.paintTexture(graphics, texture, ox, oy, TW, TH);
	}

	/**
	 * paint the Marginally Clever Logo
	 *
	 * @param graphics the render context
	 * @param robot the machine to draw.
	 */
	private void paintLogoFancy(Graphics2D graphics, Plotter robot) {
		final double scale = 0.5;
		final double TW = 128 * scale;
		final double TH = 128 * scale;

		final float LOGO_X = (float)robot.getSettings().getDouble(PlotterSettings.LIMIT_LEFT) - 65; // bottom left corner of safe Area
		final float LOGO_Y = (float)robot.getSettings().getDouble(PlotterSettings.LIMIT_BOTTOM) - 10;

		DrawingHelper.paintTexture(graphics, textureLogo, LOGO_X, -LOGO_Y, TW, TH);
	}

	// RUMBA in v3 (135mm*75mm)
	private void drawRUMBA(Graphics graphics, double left, double right) {/*
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

	private void drawWires(Graphics graphics, double left, double right) {/*
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

	private void drawFrame(Graphics2D g2d, double left, double right, double top) {/*
		final float FRAME_SIZE = 50f; // mm
		gl2.glColor3d(1, 0.8f, 0.5f);
		gl2.glBegin(GL3.GL_TRIANGLE_FAN);
		gl2.glVertex2d(left - FRAME_SIZE, top + FRAME_SIZE);
		gl2.glVertex2d(right + FRAME_SIZE, top + FRAME_SIZE);
		gl2.glVertex2d(right + FRAME_SIZE, top - FRAME_SIZE);
		gl2.glVertex2d(left - FRAME_SIZE, top - FRAME_SIZE);
		gl2.glEnd();*/
	}

	private void drawSuctionCups(Graphics graphics,double left,double right,double top) {
		final float SUCTION_CUP_Y = 35f;
		final float SUCTION_CUP_RADIUS = 32.5f; /// mm
		var c = new Color(1, 1f, 1f); // #color of suction cups
		DrawingHelper.drawCircle(graphics, (float) left - SUCTION_CUP_Y, (float) top - SUCTION_CUP_Y, SUCTION_CUP_RADIUS, c);
		DrawingHelper.drawCircle(graphics, (float) left - SUCTION_CUP_Y, (float) top + SUCTION_CUP_Y, SUCTION_CUP_RADIUS, c);
		DrawingHelper.drawCircle(graphics, (float) right + SUCTION_CUP_Y, (float) top - SUCTION_CUP_Y, SUCTION_CUP_RADIUS, c);
		DrawingHelper.drawCircle(graphics, (float) right + SUCTION_CUP_Y, (float) top + SUCTION_CUP_Y, SUCTION_CUP_RADIUS, c);
	}

	private void renderLCD(Graphics2D g2d, double left, double right) {/*
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
