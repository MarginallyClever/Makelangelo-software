package com.marginallyclever.makelangelo.apps.previewpanel.plotterrenderer;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.DrawingHelper;
import com.marginallyclever.makelangelo.Mesh;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.texture.TextureFactory;
import com.marginallyclever.makelangelo.texture.TextureWithMetadata;

import javax.vecmath.Point2d;

public class Makelangelo5 implements PlotterRenderer {
	private final TextureWithMetadata textureMainBody = TextureFactory.loadTexture("/textures/makelangelo5.png");
	private final TextureWithMetadata textureMotors = TextureFactory.loadTexture("/textures/makelangelo5-motors.png");
	private final TextureWithMetadata textureLogo = TextureFactory.loadTexture("/logo.png");
	private final TextureWithMetadata textureWeight = TextureFactory.loadTexture("/textures/weight.png");
	private final TextureWithMetadata textureGondola = TextureFactory.loadTexture("/textures/phBody.png");
	private final TextureWithMetadata textureArm = TextureFactory.loadTexture("/textures/phArm2.png");

	private final Mesh controlBox = new Mesh();

	private void setupControlBoxMesh(PlotterSettings settings) {
		float left = (float)settings.getDouble(PlotterSettings.LIMIT_LEFT);

		float scale = 650.0f / 811.0f; // machine is 650 motor-to-motor. texture is 811. scale accordingly.
		float TW = 1024 * scale;
		float TH = 1024 * scale;
		float ox = left - 106 * scale; // 106 taken from offset in texture map
		float oy = -15 - 190 * scale; // 109 taken from offset in texture map. TODO why -15 instead of top?

		controlBox.clear();
		controlBox.setRenderStyle(GL3.GL_QUADS);
		controlBox.addTexCoord(0, 0);		controlBox.addVertex(ox, oy, 0);
		controlBox.addTexCoord(1, 0);		controlBox.addVertex(ox + TW, oy, 0);
		controlBox.addTexCoord(1, 1);		controlBox.addVertex(ox + TW, oy + TH, 0);
		controlBox.addTexCoord(0, 1);		controlBox.addVertex(ox, oy + TH, 0);

		controlBox.addColor(1, 1, 1, 1);
		controlBox.addColor(1, 1, 1, 1);
		controlBox.addColor(1, 1, 1, 1);
		controlBox.addColor(1, 1, 1, 1);
	}

	@Override
	public void render(GL3 gl, Plotter robot) {
		if (textureMainBody != null) {
			paintControlBoxFancy(gl, textureMainBody);
		}

		Polargraph.paintSafeArea(gl, robot);

		if (robot.getDidFindHome())
			paintPenHolderToCounterweights(gl, robot);

		if (textureMotors != null) {
			paintControlBoxFancy(gl, textureMotors);
		}

		if (textureLogo != null) {
			paintLogoFancy(gl, robot.getSettings());
		}
	}

	@Override
	public void updatePlotterSettings(PlotterSettings settings) {
		setupControlBoxMesh(settings);
	}

	public void paintPenHolderToCounterweights(GL3 gl, Plotter robot) {
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
		paintCounterweight(gl,left,top-left_b);
		// right
		paintCounterweight(gl,right,top-right_b);
	}

	private void drawBeltMinus10(GL3 gl, double cornerX, double cornerY, double penX, double penY) {
// TODO implement me
/*
		double dx = penX - cornerX;
		double dy = penY - cornerY;
		double len = Math.sqrt(dx * dx + dy * dy);
		penX = cornerX + dx * (len-100) / len;
		penY = cornerY + dy * (len-100) / len;

		gl.glBegin(GL3.GL_LINES);
		gl.glColor3d(0.2, 0.2, 0.2);
		gl.glVertex2d(cornerX, cornerY);
		gl.glVertex2d(penX, penY);
		gl.glEnd();*/
	}

	private static void paintBeltSide(GL3 gl,double x, double y, double length) {
// TODO implement me
/*
		gl.glBegin(GL3.GL_LINES);
		gl.glVertex2d(x, y);
		gl.glVertex2d(x, y - length);
		gl.glEnd();*/
	}

	private void paintGondola(GL3 gl, double gx, double gy,Plotter robot) {
		if(textureGondola==null || textureArm==null) return;
// TODO implement me
/*
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
		paintTexture(gl,textureGondola,gx-50,gy-50,100,100);*/
	}

	private void paintCounterweight(GL3 gl,double x,double y) {
		if(textureWeight==null) {
			Polargraph.paintCounterweight(gl,x,y);
			return;
		}
		DrawingHelper.paintTexture(gl, textureWeight, x-20, y-74, 40,80);
	}

	private void paintControlBoxFancy(GL3 gl,TextureWithMetadata texture) {
		//shaderProgram.set1i(gl,"useTexture",1);
		texture.use(gl);
		//controlBox.render(gl);
		//shaderProgram.set1i(gl,"useTexture",0);
	}

	/**
	 * Paint the Marginally Clever Logo
	 *
	 * @param gl   the render context
	 * @param settings the machine settings
	 */
	private void paintLogoFancy(GL3 gl, PlotterSettings settings) {
// TODO implement me
/*
		// bottom left corner of safe area
		final float LOGO_X = (float)settings.getDouble(PlotterSettings.LIMIT_LEFT) - 65;
		final float LOGO_Y = (float)settings.getDouble(PlotterSettings.LIMIT_BOTTOM)+10;
		DrawingHelper.paintTexture(gl, textureLogo, LOGO_X, LOGO_Y, 64, 64);*/
	}
}
