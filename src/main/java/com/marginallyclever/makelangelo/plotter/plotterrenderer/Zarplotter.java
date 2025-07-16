package com.marginallyclever.makelangelo.plotter.plotterrenderer;

import com.jogamp.opengl.GL3;
import com.marginallyclever.makelangelo.Mesh;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.preview.ShaderProgram;

import javax.vecmath.Point2d;

/**
 * @author Dan Royer
 */
public class Zarplotter implements PlotterRenderer {
	final public float ZAR_MOTOR_MOUNT_SIZE=45; //cm
	final public float ZAR_PLOTTER_SIZE=60; //cm
	final public float ZAR_PLOTTER_OUTER_SIZE=70; //cm
	final public float ZAR_PLOTTER_HOLE_SIZE=20; //cm
	final public float ZAR_MOTOR_BODY_SIZE=42; //cm
	
	@Override
	public void render(ShaderProgram shader, Plotter robot) {
		paintMotors(shader,robot);
		paintControlBox(shader,robot);
		if(robot.getDidFindHome()) 
			paintPenHolderToCounterweights(shader,robot);
	}

	private void paintPenHolderToCounterweights(ShaderProgram shader, Plotter robot) {
		PlotterSettings settings = robot.getSettings();
		//double dx, dy;
		Point2d pos = robot.getPos();
		float gx = (float)pos.x;
		float gy = (float)pos.y;

		float top = (float)settings.getDouble(PlotterSettings.LIMIT_TOP);
		float bottom = (float)settings.getDouble(PlotterSettings.LIMIT_BOTTOM);
		float left = (float)settings.getDouble(PlotterSettings.LIMIT_LEFT);
		float right = (float)settings.getDouble(PlotterSettings.LIMIT_RIGHT);

		var gl = shader.getContext();
		gl.glEnable(GL3.GL_BLEND);
		gl.glBlendFunc(GL3.GL_SRC_ALPHA,GL3.GL_ONE_MINUS_SRC_ALPHA);

		// plotter
		Mesh plotter = new Mesh();
		plotter.setRenderStyle(GL3.GL_QUADS);
		plotter.addColor(0,0,0,0.5f);

		plotter.addVertex(gx-ZAR_PLOTTER_OUTER_SIZE/2, gy-ZAR_PLOTTER_OUTER_SIZE/2, 0);
		plotter.addVertex(gx-ZAR_PLOTTER_OUTER_SIZE/2, gy-ZAR_PLOTTER_HOLE_SIZE/2, 0);
		plotter.addVertex(gx+ZAR_PLOTTER_OUTER_SIZE/2, gy-ZAR_PLOTTER_HOLE_SIZE/2, 0);
		plotter.addVertex(gx+ZAR_PLOTTER_OUTER_SIZE/2, gy-ZAR_PLOTTER_OUTER_SIZE/2, 0);
		plotter.addVertex(gx+ZAR_PLOTTER_HOLE_SIZE/2, gy-ZAR_PLOTTER_HOLE_SIZE/2, 0);
		plotter.addVertex(gx+ZAR_PLOTTER_HOLE_SIZE/2, gy+ZAR_PLOTTER_HOLE_SIZE/2, 0);
		plotter.addVertex(gx+ZAR_PLOTTER_OUTER_SIZE/2, gy+ZAR_PLOTTER_HOLE_SIZE/2, 0);
		plotter.addVertex(gx+ZAR_PLOTTER_OUTER_SIZE/2, gy-ZAR_PLOTTER_HOLE_SIZE/2, 0);
		plotter.addVertex(gx-ZAR_PLOTTER_OUTER_SIZE/2, gy-ZAR_PLOTTER_HOLE_SIZE/2, 0);
		plotter.addVertex(gx-ZAR_PLOTTER_OUTER_SIZE/2, gy+ZAR_PLOTTER_HOLE_SIZE/2, 0);
		plotter.addVertex(gx-ZAR_PLOTTER_HOLE_SIZE/2, gy+ZAR_PLOTTER_HOLE_SIZE/2, 0);
		plotter.addVertex(gx-ZAR_PLOTTER_HOLE_SIZE/2, gy-ZAR_PLOTTER_HOLE_SIZE/2, 0);
		plotter.addVertex(gx-ZAR_PLOTTER_OUTER_SIZE/2, gy+ZAR_PLOTTER_HOLE_SIZE/2, 0);
		plotter.addVertex(gx-ZAR_PLOTTER_OUTER_SIZE/2, gy+ZAR_PLOTTER_OUTER_SIZE/2, 0);
		plotter.addVertex(gx+ZAR_PLOTTER_OUTER_SIZE/2, gy+ZAR_PLOTTER_OUTER_SIZE/2, 0);
		plotter.addVertex(gx+ZAR_PLOTTER_OUTER_SIZE/2, gy+ZAR_PLOTTER_HOLE_SIZE/2, 0);
		plotter.render(gl);

		// belt from motors to plotter
		Mesh belt = new Mesh();
		belt.setRenderStyle(GL3.GL_LINES);

		belt.addVertex(gx+left +ZAR_MOTOR_MOUNT_SIZE, gy+top   -ZAR_MOTOR_MOUNT_SIZE,0);	belt.addVertex(gx-ZAR_PLOTTER_SIZE/2, gy+ZAR_PLOTTER_SIZE/2,0);
		belt.addVertex(gx+right-ZAR_MOTOR_MOUNT_SIZE, gy+top   -ZAR_MOTOR_MOUNT_SIZE,0);	belt.addVertex(gx+ZAR_PLOTTER_SIZE/2, gy+ZAR_PLOTTER_SIZE/2,0);
		belt.addVertex(gx+left +ZAR_MOTOR_MOUNT_SIZE, gy+bottom+ZAR_MOTOR_MOUNT_SIZE,0);	belt.addVertex(gx-ZAR_PLOTTER_SIZE/2, gy-ZAR_PLOTTER_SIZE/2,0);
		belt.addVertex(gx+right-ZAR_MOTOR_MOUNT_SIZE, gy+bottom+ZAR_MOTOR_MOUNT_SIZE,0);	belt.addVertex(gx+ZAR_PLOTTER_SIZE/2, gy-ZAR_PLOTTER_SIZE/2,0);
		belt.render(gl);
	}

	private void paintMotors(ShaderProgram shader,Plotter plotter) {
		/*
		float top = (float)plotter.getSettings().getDouble(PlotterSettings.LIMIT_TOP);
		float bottom = (float)plotter.getSettings().getDouble(PlotterSettings.LIMIT_BOTTOM);
		float right = (float)plotter.getSettings().getDouble(PlotterSettings.LIMIT_RIGHT);
		float left = (float)plotter.getSettings().getDouble(PlotterSettings.LIMIT_LEFT);

		gl2.glTranslated(left , top   , 0);  gl2.glRotated(270, 0, 0, 1);  paintOneMotor(gl2);
		gl2.glTranslated(right, top   , 0);  gl2.glRotated(180, 0, 0, 1);  paintOneMotor(gl2);
		gl2.glTranslated(right, bottom, 0);  gl2.glRotated( 90, 0, 0, 1);  paintOneMotor(gl2);
		gl2.glTranslated(left , bottom, 0);  gl2.glRotated(  0, 0, 0, 1);  paintOneMotor(gl2);
		*/
	}

	private void paintOneMotor(GL3 gl) {
		// frame
		Mesh frame = new Mesh();
		frame.setRenderStyle(GL3.GL_TRIANGLE_FAN);
		frame.addColor(1, 0.8f, 0.5f,1.0f);  frame.addVertex(0                   , 0                   ,0);
		frame.addColor(1, 0.8f, 0.5f,1.0f);  frame.addVertex(0                   , ZAR_MOTOR_MOUNT_SIZE,0);
		frame.addColor(1, 0.8f, 0.5f,1.0f);  frame.addVertex(ZAR_MOTOR_MOUNT_SIZE, ZAR_MOTOR_MOUNT_SIZE,0);
		frame.addColor(1, 0.8f, 0.5f,1.0f);  frame.addVertex(ZAR_MOTOR_MOUNT_SIZE, 0                   ,0);
		frame.addColor(1, 0.8f, 0.5f,1.0f);  frame.addVertex(0                   , 0                   ,0);
		frame.render(gl);

		// motor
		Mesh motor = new Mesh();
		motor.setRenderStyle(GL3.GL_QUADS);
		motor.addColor(0, 0, 0, 1.0f);  motor.addVertex(0                  , 0                  ,0);
		motor.addColor(0, 0, 0, 1.0f);  motor.addVertex(0                  , ZAR_MOTOR_BODY_SIZE,0);
		motor.addColor(0, 0, 0, 1.0f);  motor.addVertex(ZAR_MOTOR_BODY_SIZE, ZAR_MOTOR_BODY_SIZE,0);
		motor.addColor(0, 0, 0, 1.0f);  motor.addVertex(ZAR_MOTOR_BODY_SIZE, 0                  ,0);
		motor.addColor(0, 0, 0, 1.0f);  motor.addVertex(0                  , 0                  ,0);
		motor.render(gl);
	}
	
	private void paintControlBox(ShaderProgram shader,Plotter plotter) {
		float cy = (float)plotter.getSettings().getDouble(PlotterSettings.LIMIT_TOP);
		float cx = 0;

		Mesh mesh = new Mesh();
		mesh.setRenderStyle(GL3.GL_QUADS);
		// mounting plate for PCB
		mesh.addColor(1,0.8f,0.5f,1.0f);  mesh.addVertex(cx-80, cy+50,0);
		mesh.addColor(1,0.8f,0.5f,1.0f);  mesh.addVertex(cx+80, cy+50,0);
		mesh.addColor(1,0.8f,0.5f,1.0f);  mesh.addVertex(cx+80, cy-50,0);
		mesh.addColor(1,0.8f,0.5f,1.0f);  mesh.addVertex(cx-80, cy-50,0);
		// RUMBA in v3 (135mm*75mm)
		mesh.addColor(0.9f,0.9f,0.9f,1.0f);  mesh.addVertex(cx-67.5f, cy+37.5f, 0);
		mesh.addColor(0.9f,0.9f,0.9f,1.0f);  mesh.addVertex(cx+67.5f, cy+37.5f, 0);
		mesh.addColor(0.9f,0.9f,0.9f,1.0f);  mesh.addVertex(cx+67.5f, cy-37.5f, 0);
		mesh.addColor(0.9f,0.9f,0.9f,1.0f);  mesh.addVertex(cx-67.5f, cy-37.5f, 0);

		mesh.render(shader.getContext());
	}
}
