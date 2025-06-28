package com.marginallyclever.makelangelo.plotter.plotterrenderer;

import com.jogamp.opengl.GL3;
import com.marginallyclever.makelangelo.Mesh;
import com.marginallyclever.makelangelo.preview.ShaderProgram;
import com.marginallyclever.makelangelo.texture.TextureFactory;
import com.marginallyclever.makelangelo.texture.TextureWithMetadata;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;

import javax.vecmath.Point2d;

import java.awt.*;

import static com.marginallyclever.convenience.helpers.DrawingHelper.*;

public class MakelangeloCustom implements PlotterRenderer {
	public final static float PEN_HOLDER_RADIUS_5 = 25; // mm
	public final static float COUNTERWEIGHT_W = 30;
	public final static float COUNTERWEIGHT_H = 60;
	public final static float PULLEY_RADIUS = 1.27f;
	public final static float MOTOR_WIDTH = 42;
	private static TextureWithMetadata controlBoard;

	@Override
	public void render(ShaderProgram shader, Plotter robot) {
		PlotterSettings settings = robot.getSettings();

		paintControlBox(shader,settings);
		paintMotors(shader,settings);
		if(robot.getDidFindHome())
			paintPenHolderToCounterweights(shader,robot);
	}

	/**
	 * paint the controller and the LCD panel
	 * @param shader the render context
	 * @param settings plottersettings of the robot
	 */
	private void paintControlBox(ShaderProgram shader, PlotterSettings settings) {
		var gl = shader.getContext();
		float cy = (float)settings.getDouble(PlotterSettings.LIMIT_TOP);
		float left = (float)settings.getDouble(PlotterSettings.LIMIT_LEFT);
		float right = (float)settings.getDouble(PlotterSettings.LIMIT_RIGHT);
		float top = (float)settings.getDouble(PlotterSettings.LIMIT_TOP);
		float cx = 0;

		// mounting plate for PCB
		drawRectangle(gl, top+35f, right+30f, top-35f, left-30f,new Color(1,0.8f,0.5f));

		// wires to each motor
		Mesh wires = new Mesh();
		wires.setRenderStyle(GL3.GL_LINES);
		final float SPACING=2;
		float y=SPACING*-1.5f;
		wires.addColor(1, 0, 0,1);	wires.addVertex(cx+left , cy+y, 0);
		wires.addColor(1, 0, 0,1);	wires.addVertex(cx+right, cy+y, 0);  y+=SPACING;
		wires.addColor(0, 1, 0,1);	wires.addVertex(cx+left , cy+y, 0);
		wires.addColor(0, 1, 0,1);	wires.addVertex(cx+right, cy+y, 0);  y+=SPACING;
		wires.addColor(0, 0, 1,1);	wires.addVertex(cx+left , cy+y, 0);
		wires.addColor(0, 0, 1,1);	wires.addVertex(cx+right, cy+y, 0);  y+=SPACING;
		wires.addColor(1, 1, 0,1);	wires.addVertex(cx+left , cy+y, 0);
		wires.addColor(1, 1, 0,1);	wires.addVertex(cx+right, cy+y, 0);
		wires.render(gl);
		
		float shiftX = (float) right / 2;
		if (controlBoard == null) controlBoard = TextureFactory.loadTexture("/textures/rampsv14.png");
        final double scale = 0.1;
        if (shiftX < 100) {
            shiftX = 45;
        }

		paintTexture(shader, controlBoard, cx+shiftX, cy-72, 1024 * scale, 1024 * scale);

        renderLCD(shader, settings);
	}

	// draw left & right motor
	private void paintMotors(ShaderProgram shader, PlotterSettings settings) {
		double top = settings.getDouble(PlotterSettings.LIMIT_TOP);
		double right = settings.getDouble(PlotterSettings.LIMIT_RIGHT);
		double left = settings.getDouble(PlotterSettings.LIMIT_LEFT);
		var c = new Color(0.3f,0.3f,0.3f);
		var gl = shader.getContext();
		// left motor
		drawRectangle(gl, top+MOTOR_WIDTH/2, left+MOTOR_WIDTH/2, top-MOTOR_WIDTH/2, left-MOTOR_WIDTH/2, c);
		// right motor
		drawRectangle(gl, top+MOTOR_WIDTH/2, right+MOTOR_WIDTH/2, top-MOTOR_WIDTH/2, right-MOTOR_WIDTH/2, c);
	}
	
	private void renderLCD(ShaderProgram shader, PlotterSettings settings) {
		float left = (float)settings.getDouble(PlotterSettings.LIMIT_LEFT);
		float right = (float)settings.getDouble(PlotterSettings.LIMIT_RIGHT);
		float top = (float)settings.getDouble(PlotterSettings.LIMIT_TOP);

		// position
		float shiftX = (float) left / 2;
		if (shiftX > -100) {
			shiftX = -75;
		}

		var gl = shader.getContext();

		// LCD red
		float w = 150f/2;
		float h = 56f/2;
		drawRectangle(gl, top+h, shiftX+w, top-h, shiftX-w, new Color(1,0.8f,0.5f));

		// LCD green
		shiftX += -2.6f/2;
		float shiftY = -0.771f;
		w = 98f/2;
		h = 60f/2;
		drawRectangle(gl, top+shiftY+h, shiftX+w, top+shiftY-h, shiftX-w, new Color(0,0.6f,0.0f));

		// LCD black
		h = 40f/2;
		drawRectangle(gl, top+shiftY+h, shiftX+w, top+shiftY-h, shiftX-w, new Color(0,0,0));

		// LCD blue
		h = 25f/2;
		w = 75f/2;
		drawRectangle(gl, top+shiftY+h, shiftX+w, top+shiftY-h, shiftX-w, new Color(0,0,0.7f));
	}

	private void paintPenHolderToCounterweights(ShaderProgram shader, Plotter robot ) {
		PlotterSettings settings = robot.getSettings();
		Point2d pos = robot.getPos();
		float gx = (float)pos.x;
		float gy = (float)pos.y;
		
		float top = (float)settings.getDouble(PlotterSettings.LIMIT_TOP);
		float bottom = (float)settings.getDouble(PlotterSettings.LIMIT_BOTTOM);
		float left = (float)settings.getDouble(PlotterSettings.LIMIT_LEFT);
		float right = (float)settings.getDouble(PlotterSettings.LIMIT_RIGHT);
		
		if(gx<left  ) return;
		if(gx>right ) return;
		if(gy>top   ) return;
		if(gy<bottom) return;
		
		float bottleCenter = 8f+7.5f;

		float mw = right-left;
		float mh = top-(float)settings.getDouble(PlotterSettings.LIMIT_BOTTOM);
		float suggestedLength = (float)Math.sqrt(mw*mw+mh*mh)+50;

		float dx = gx - left;
		float dy = gy - top;
		float left_a = (float)Math.sqrt(dx*dx+dy*dy);
		float left_b = (suggestedLength - left_a)/2;

		dx = gx - right;
		float right_a = (float)Math.sqrt(dx*dx+dy*dy);
		float right_b = (suggestedLength - right_a)/2;

		var gl = shader.getContext();
		paintPlotter(gl,gx,gy);

		// belts
		Mesh belts = new Mesh();
		belts.setRenderStyle(GL3.GL_LINES);
		// belt from motor to pen holder left
		belts.addColor(0.2f,0.2f,0.2f,1.0f);  belts.addVertex(left, top,0);
		belts.addColor(0.2f,0.2f,0.2f,1.0f);  belts.addVertex(gx,gy,0);
		// belt from motor to pen holder right
		belts.addColor(0.2f,0.2f,0.2f,1.0f);  belts.addVertex(right, top,0);
		belts.addColor(0.2f,0.2f,0.2f,1.0f);  belts.addVertex(gx,gy,0);
		// belt from motor to counterweight left
		belts.addColor(0.2f,0.2f,0.2f,1.0f);  belts.addVertex(left-bottleCenter-PULLEY_RADIUS, top-MOTOR_WIDTH/2,0);
		belts.addColor(0.2f,0.2f,0.2f,1.0f);  belts.addVertex(left-bottleCenter-PULLEY_RADIUS, top-left_b,0);
		// belt from motor to counterweight right
		belts.addColor(0.2f,0.2f,0.2f,1.0f);  belts.addVertex(right+bottleCenter+PULLEY_RADIUS, top-MOTOR_WIDTH/2,0);
		belts.addColor(0.2f,0.2f,0.2f,1.0f);  belts.addVertex(right+bottleCenter+PULLEY_RADIUS, top-right_b,0);
		belts.render(gl);
		
		// counterweight left
		Mesh cwLeft = new Mesh();
		cwLeft.setRenderStyle(GL3.GL_LINE_LOOP);
		cwLeft.addColor(0, 0, 1,1);  cwLeft.addVertex(left-PULLEY_RADIUS-bottleCenter-COUNTERWEIGHT_W/2,top-left_b, 0);
		cwLeft.addColor(0, 0, 1,1);  cwLeft.addVertex(left-PULLEY_RADIUS-bottleCenter+COUNTERWEIGHT_W/2,top-left_b, 0);
		cwLeft.addColor(0, 0, 1,1);  cwLeft.addVertex(left-PULLEY_RADIUS-bottleCenter+COUNTERWEIGHT_W/2,top-left_b-COUNTERWEIGHT_H, 0);
		cwLeft.addColor(0, 0, 1,1);  cwLeft.addVertex(left-PULLEY_RADIUS-bottleCenter-COUNTERWEIGHT_W/2,top-left_b-COUNTERWEIGHT_H, 0);
		cwLeft.render(gl);
		
		// counterweight right
		Mesh cwRight = new Mesh();
		cwRight.setRenderStyle(GL3.GL_LINE_LOOP);
		cwRight.addColor(0, 0, 1,1);  cwRight.addVertex(right+PULLEY_RADIUS+bottleCenter-COUNTERWEIGHT_W/2,top-right_b, 0);
		cwRight.addColor(0, 0, 1,1);  cwRight.addVertex(right+PULLEY_RADIUS+bottleCenter+COUNTERWEIGHT_W/2,top-right_b, 0);
		cwRight.addColor(0, 0, 1,1);  cwRight.addVertex(right+PULLEY_RADIUS+bottleCenter+COUNTERWEIGHT_W/2,top-right_b-COUNTERWEIGHT_H, 0);
		cwRight.addColor(0, 0, 1,1);  cwRight.addVertex(right+PULLEY_RADIUS+bottleCenter-COUNTERWEIGHT_W/2,top-right_b-COUNTERWEIGHT_H, 0);
		cwRight.render(gl);
	}

	private void paintPlotter(GL3 gl2, float gx, float gy) {
		// plotter
		drawCircle(gl2, gx, gy, PEN_HOLDER_RADIUS_5, new Color(0f,0f,1f));
	}
}
