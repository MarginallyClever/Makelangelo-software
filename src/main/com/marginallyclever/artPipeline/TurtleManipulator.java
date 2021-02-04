package com.marginallyclever.artPipeline;


import java.util.ArrayList;

import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import com.marginallyclever.convenience.turtle.Turtle;


/**
 * shared methods for image manipulation (generating, converting, or filtering)
 * @author Dan
 */
public abstract class TurtleManipulator {	
	// threading
	protected ProgressMonitor pm;
	protected SwingWorker<ArrayList<Turtle>, Void> threadWorker;
	
	
	public void setThreadWorker(SwingWorker<ArrayList<Turtle>, Void> p) {
		threadWorker = p;
	}

	public void setProgressMonitor(ProgressMonitor p) {
		pm = p;
	}
	
	/**
	 * @return the translated name of the manipulator.
	 */
	abstract public String getName();
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
