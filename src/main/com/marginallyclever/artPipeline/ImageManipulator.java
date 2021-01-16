package com.marginallyclever.artPipeline;

import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;
import com.marginallyclever.makelangeloRobot.settings.MakelangeloRobotSettings;


/**
 * shared methods for image manipulation (generating, converting, or filtering)
 * @author Dan
 */
public abstract class ImageManipulator {		
	// pen position optimizing
	public Turtle turtle = new Turtle();
	// threading
	protected ProgressMonitor pm;
	protected SwingWorker<Void, Void> swingWorker;
	// helpers
	protected MakelangeloRobotSettings machine;

	
	public void setSwingWorker(SwingWorker<Void, Void> p) {
		swingWorker = p;
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


	protected boolean isInsidePaperMargins(double x,double y) {
		if( x < machine.getMarginLeft()  ) return false;
		if( x > machine.getMarginRight() ) return false;
		if( y < machine.getMarginBottom()) return false;
		if( y > machine.getMarginTop()   ) return false;
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
