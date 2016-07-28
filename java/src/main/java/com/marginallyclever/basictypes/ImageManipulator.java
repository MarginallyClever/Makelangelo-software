package com.marginallyclever.basictypes;


import java.io.IOException;
import java.io.Writer;

import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import com.marginallyclever.drawingtools.DrawingTool;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;
import com.marginallyclever.makelangeloRobot.MakelangeloRobotSettings;


/**
 * shared methods for image manipulation (generating, converting, or filtering)
 * @author Dan
 */
public abstract class ImageManipulator {	
	// pen position optimizing
	protected boolean lastUp;
	protected double previousX, previousY;
	
	// threading
	protected ProgressMonitor pm;
	protected SwingWorker<Void, Void> parent;

	// helpers
	protected MakelangeloRobotSettings machine;
	protected DrawingTool tool;


	public void setParent(SwingWorker<Void, Void> p) {
		parent = p;
	}

	public void setProgressMonitor(ProgressMonitor p) {
		pm = p;
	}
	
	public void setRobot(MakelangeloRobot robot) {
		machine = robot.getSettings();
	}


	/**
	 * @return the translated name of the manipulator.
	 */
	public String getName() {
		return "Unnamed";
	}


	/**
	 * insert the machine-specific preamble at the start of the gcode file.
	 * @param img
	 * @param out
	 * @throws IOException
	 */
	public void imageStart(Writer out) throws IOException {
		//out.write(machine.getGCodeConfig() + ";\n");
		//out.write(machine.getGCodeBobbin() + ";\n");
		//out.write(machine.getGCodeSetPositionAtHome()+";\n");		
		previousX = machine.getHomeX();
		previousY = machine.getHomeY();
		setAbsoluteMode(out);
	}


	protected void liftPen(Writer out) throws IOException {
		if(tool==null) {
			throw new IOException("Order of operations: Can't raise the tool before setting a tool.");
		}
		if(lastUp) return;
		tool.writeOff(out);
		lastUp = true;
	}


	protected void lowerPen(Writer out) throws IOException {
		if(tool==null) {
			throw new IOException("Order of operations: Can't lower the tool before setting a tool.");
		}
		if(!lastUp) return;
		tool.writeOn(out);
		lastUp = false;
	}

	protected void setAbsoluteMode(Writer out) throws IOException {
		out.write("G90;\n");
	}

	protected void setRelativeMode(Writer out) throws IOException {
		out.write("G91;\n");
	}


	/**
	 * Create the gcode that will move the robot to a new position.  It does not translate from image space to paper space.
	 * @param out where to write the gcode
	 * @param x new coordinate
	 * @param y new coordinate
	 * @param up new pen state
	 * @throws IOException on write failure
	 */
	protected void moveTo(Writer out, double x, double y, boolean up) throws IOException {
		if(isInsidePaperMargins(x,y)) {
			tool.writeMoveTo(out, (float) x, (float) y);
		}
		if(lastUp != up) {
			if (up) liftPen(out);
			else lowerPen(out);
			lastUp = up;
		}
		previousX = x;
		previousY = y;
	}


	/**
	 * This is a special case of moveTo() that only works when every line on the paper is a straight line.
	 * @param out where to write the gcode
	 * @param x new coordinate
	 * @param y new coordinate
	 * @param up new pen state
	 * @throws IOException on write failure
	 */
	protected void lineTo(Writer out, double x, double y, boolean up) throws IOException {
		if(lastUp != up) {
			moveTo(out,x,y,up);
		}
	}


	protected boolean isInsidePaperMargins(double x,double y) {
		final float EPSILON = 0.01f;
		if( x < (machine.getPaperLeft()   * machine.getPaperMargin()*10.0f-EPSILON)) return false;
		if( x > (machine.getPaperRight()  * machine.getPaperMargin()*10.0f+EPSILON)) return false;
		if( y < (machine.getPaperBottom() * machine.getPaperMargin()*10.0f-EPSILON)) return false;
		if( y > (machine.getPaperTop()    * machine.getPaperMargin()*10.0f+EPSILON)) return false;
		return true;
	}
}

/**
 * This file is part of Makelangelo.
 * <p>
 * Makelangelo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Makelangelo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Makelangelo.  If not, see <http://www.gnu.org/licenses/>.
 */
