package com.marginallyclever.makelangelo.plotter.plotterrenderer;

import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;

import javax.vecmath.Point2d;
import java.awt.*;

/**
 * Zarplotter is four motors, one on each corner of a rectangle, pulling a belt to a central pen holder.
 * Effectively two makelangelos put together.  This class draws a representation of the Zarplotter in the preview window.
 * @author Dan Royer
 */
public class Zarplotter implements PlotterRenderer {
	final public float ZAR_MOTOR_MOUNT_SIZE=45; //cm
	final public float ZAR_PLOTTER_SIZE=60; //cm
	final public float ZAR_PLOTTER_OUTER_SIZE=70; //cm
	final public float ZAR_PLOTTER_HOLE_SIZE=20; //cm
	final public float ZAR_MOTOR_BODY_SIZE=42; //cm
	
	@Override
	public void render(Graphics graphics, Plotter robot) {
		paintMotors(graphics,robot);
		paintControlBox(graphics,robot);
		if(robot.getDidFindHome()) 
			paintPenHolderToCounterweights(graphics,robot);
	}

	private void paintPenHolderToCounterweights(Graphics graphics, Plotter robot) {
        Graphics2D g2d = (Graphics2D) graphics;

		PlotterSettings settings = robot.getSettings();
		Point2d pos = robot.getPos();
		float gx = (float)pos.x;
		float gy = (float)pos.y;

		float top = (float)settings.getDouble(PlotterSettings.LIMIT_TOP);
		float bottom = (float)settings.getDouble(PlotterSettings.LIMIT_BOTTOM);
		float left = (float)settings.getDouble(PlotterSettings.LIMIT_LEFT);
		float right = (float)settings.getDouble(PlotterSettings.LIMIT_RIGHT);

        g2d.setColor(Color.BLACK);

        /*
        TODO figure out what this zarplotter code was trying to draw
        float a = ZAR_PLOTTER_OUTER_SIZE/2;
        float b = ZAR_PLOTTER_HOLE_SIZE/2;
		plotter.setRenderStyle(GL3.GL_TRIANGLE_FAN);
		plotter.addVertex(gx-a, gy-a, 0);
		plotter.addVertex(gx-a, gy-b, 0);
		plotter.addVertex(gx+a, gy-b, 0);
		plotter.addVertex(gx+a, gy-a, 0);
		plotter.addVertex(gx+b, gy-b, 0);
		plotter.addVertex(gx+b, gy+b, 0);
		plotter.addVertex(gx+a, gy+b, 0);
		plotter.addVertex(gx+a, gy-b, 0);

		plotter.addVertex(gx-a, gy-b, 0);
		plotter.addVertex(gx-a, gy+b, 0);
		plotter.addVertex(gx-b, gy+b, 0);
		plotter.addVertex(gx-b, gy-b, 0);
		plotter.addVertex(gx-a, gy+b, 0);
		plotter.addVertex(gx-a, gy+a, 0);
		plotter.addVertex(gx+a, gy+a, 0);
		plotter.addVertex(gx+a, gy+b, 0);
		*/

		// belt from 4 motors to plotter
		g2d.drawLine((int)(gx+left +ZAR_MOTOR_MOUNT_SIZE), (int)(gy+top   -ZAR_MOTOR_MOUNT_SIZE), (int)(gx-ZAR_PLOTTER_SIZE/2), (int)(gy+ZAR_PLOTTER_SIZE/2));
		g2d.drawLine((int)(gx+right-ZAR_MOTOR_MOUNT_SIZE), (int)(gy+top   -ZAR_MOTOR_MOUNT_SIZE), (int)(gx+ZAR_PLOTTER_SIZE/2), (int)(gy+ZAR_PLOTTER_SIZE/2));
		g2d.drawLine((int)(gx+left +ZAR_MOTOR_MOUNT_SIZE), (int)(gy+bottom+ZAR_MOTOR_MOUNT_SIZE), (int)(gx-ZAR_PLOTTER_SIZE/2), (int)(gy-ZAR_PLOTTER_SIZE/2));
		g2d.drawLine((int)(gx+right-ZAR_MOTOR_MOUNT_SIZE), (int)(gy+bottom+ZAR_MOTOR_MOUNT_SIZE), (int)(gx+ZAR_PLOTTER_SIZE/2), (int)(gy-ZAR_PLOTTER_SIZE/2));
	}

	private void paintMotors(Graphics graphics,Plotter plotter) {
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

	private void paintOneMotor(Graphics graphics) {
		// frame
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.setColor(new Color(255,204,128,1.0f));
        g2d.fillRect(0,0,(int)ZAR_MOTOR_MOUNT_SIZE,(int)ZAR_MOTOR_MOUNT_SIZE);

		// motor
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0,0,(int)ZAR_MOTOR_BODY_SIZE,(int)ZAR_MOTOR_BODY_SIZE);
	}
	
	private void paintControlBox(Graphics graphics,Plotter plotter) {
		float cy = -(float)plotter.getSettings().getDouble(PlotterSettings.LIMIT_TOP);
		float cx = 0;

		// mounting plate for PCB
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.setColor(new Color(255,204,128,255));
        g2d.fillRect((int)(cx-80), (int)(cy-50), 160, 100);
		// RUMBA in v3 (135mm*75mm)
        g2d.setColor(new Color(229,229,229,255));
        g2d.fillRect((int)(cx-67.5f), (int)(cy-37.5f), 135, 75);
	}
}
