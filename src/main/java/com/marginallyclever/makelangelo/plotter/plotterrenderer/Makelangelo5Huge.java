package com.marginallyclever.makelangelo.plotter.plotterrenderer;

import com.marginallyclever.convenience.helpers.DrawingHelper;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.texture.TextureFactory;
import com.marginallyclever.makelangelo.texture.TextureWithMetadata;

import javax.vecmath.Point2d;
import java.awt.*;

/**
 * Visual representation of a Makelangelo 5 Huge plotter.
 */
public class Makelangelo5Huge implements PlotterRenderer {
	private static TextureWithMetadata textureMainBody;
	private static TextureWithMetadata textureMotorMounts;
	private static TextureWithMetadata textureLogo;
	private static TextureWithMetadata textureWeight;
	private static TextureWithMetadata textureGondola;
	private static TextureWithMetadata textureArm;

    public Makelangelo5Huge() {
        textureMainBody = TextureFactory.loadTexture("/textures/huge.png");
        textureMotorMounts = TextureFactory.loadTexture("/textures/huge-motors.png");
        textureLogo = TextureFactory.loadTexture("/logo.png");
        textureWeight = TextureFactory.loadTexture("/textures/weight.png");
        textureGondola = TextureFactory.loadTexture("/textures/phBody.png");
        textureArm = TextureFactory.loadTexture("/textures/phArm2.png");
    }
    
	@Override
	public void render(Graphics graphics, Plotter robot) {
        paintControlBoxFancy(graphics, robot, textureMainBody);

		Polargraph.paintSafeArea(graphics, robot);

		if (robot.getDidFindHome())
			paintPenHolderToCounterweights(graphics, robot);

        paintControlBoxFancy(graphics, robot, textureMotorMounts);
        //paintLogoFancy(graphics, robot);
	}

	private void paintControlBoxFancy(Graphics graphics, Plotter robot,TextureWithMetadata texture) {
		double left = robot.getSettings().getDouble(PlotterSettings.LIMIT_LEFT);
        double top = robot.getSettings().getDouble(PlotterSettings.LIMIT_TOP);

		final double scaleX = 1366 / 943.0; // machine is 1366 motor-to-motor. texture is 922. scaleX accordingly.
		final double width = 1024 * scaleX;
		final double height = 1024 * scaleX;
		final double ox = left - 51 * scaleX; // 106 taken from offset in texture map
		final double oy = -280 * scaleX; // 109 taken from offset in texture map. TODO why -15 instead of top?

		DrawingHelper.paintTexture(graphics, texture, ox, oy, width, height);
	}

	public void paintPenHolderToCounterweights(Graphics graphics, Plotter robot) {
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

	private void drawBeltMinus10(Graphics graphics, double cornerX, double cornerY, double penX, double penY) {
		float dx = (float)(penX - cornerX);
		float dy = (float)(penY - cornerY);
		float len = (float)Math.sqrt(dx * dx + dy * dy);
		penX = cornerX + dx * (len-100) / len;
		penY = cornerY + dy * (len-100) / len;

        Graphics2D g2d = (Graphics2D) graphics;
        g2d.setColor(new Color(255*0.2f,255*0.2f,255*0.2f));
        g2d.drawLine((int)cornerX,(int)cornerY,(int)penX,(int)penY);
	}

	private static void paintBeltSide(Graphics graphics,double x, double y, double length) {
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.setColor(new Color(255*0.2f,255*0.2f,255*0.2f));
        g2d.drawLine((int)x,(int)y,(int)x,(int)(y-length));
	}

	private void paintGondola(Graphics graphics, double gx, double gy,Plotter robot) {
		if(textureGondola!=null && textureArm!=null) {
			paintGondolaFancy(graphics,gx,gy,robot);
			return;
		}
		DrawingHelper.drawCircle(graphics, (float)gx, (float)gy, Polargraph.PEN_HOLDER_RADIUS_2, Color.BLACK);
		if (robot.getPenIsUp()) {
			DrawingHelper.drawCircle(graphics, (float)gx, (float)gy, Polargraph.PEN_HOLDER_RADIUS_2 + 5, Color.BLACK);
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

		graphics.glPushMatrix();
		graphics.glTranslated(gx,gy,0);
		graphics.glRotated(Math.toDegrees(angleLeft)+90,0,0,1);
		DrawingHelper.paintTexture(graphics,textureArm,-100,-100,200,200);
		graphics.glPopMatrix();

		graphics.glPushMatrix();
		graphics.glTranslated(gx,gy,0);
		graphics.glRotated(Math.toDegrees(angleRight)+90,0,0,1);
		DrawingHelper.paintTexture(graphics,textureArm,-100,-100,200,200);
		graphics.glPopMatrix();

		// paint body last so it's on top
		DrawingHelper.paintTexture(graphics,textureGondola,gx-50,gy-50,100,100);*/
	}

	private void paintCounterweight(Graphics graphics,double x,double y) {
		if(textureWeight==null) {
			Polargraph.paintCounterweight(graphics,(float)x,(float)y);
			return;
		}

		DrawingHelper.paintTexture(graphics, textureWeight, x-20, y-74, 40,80);
	}

	/**
	 * paint the Marginally Clever Logo
	 *
	 * @param graphics the render context
	 * @param robot the machine to draw.
	 */
	private void paintLogoFancy(Graphics graphics, Plotter robot) {
		final double scale = 0.5;
		final double TW = 128 * scale;
		final double TH = 128 * scale;

		final float LOGO_X = (float)robot.getSettings().getDouble(PlotterSettings.LIMIT_LEFT) - 65; // bottom left corner of safe Area
		final float LOGO_Y = (float)robot.getSettings().getDouble(PlotterSettings.LIMIT_BOTTOM)+10;

		DrawingHelper.paintTexture(graphics, textureLogo, LOGO_X, LOGO_Y, TW, TH);
	}


	private void drawSuctionCups(Graphics graphics,double left,double right,double top) {
		final float SUCTION_CUP_Y = 35f;
		final float SUCTION_CUP_RADIUS = 32.5f; /// mm
		var c = new Color(1f, 1f, 1f); // #color of suction cups
		DrawingHelper.drawCircle(graphics, (float) left - SUCTION_CUP_Y, (float) top - SUCTION_CUP_Y, SUCTION_CUP_RADIUS, c);
		DrawingHelper.drawCircle(graphics, (float) left - SUCTION_CUP_Y, (float) top + SUCTION_CUP_Y, SUCTION_CUP_RADIUS, c);
		DrawingHelper.drawCircle(graphics, (float) right + SUCTION_CUP_Y, (float) top - SUCTION_CUP_Y, SUCTION_CUP_RADIUS, c);
		DrawingHelper.drawCircle(graphics, (float) right + SUCTION_CUP_Y, (float) top + SUCTION_CUP_Y, SUCTION_CUP_RADIUS, c);
	}
}
