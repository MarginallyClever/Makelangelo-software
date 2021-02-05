package com.marginallyclever.artPipeline;

import javax.swing.ProgressMonitor;

import com.marginallyclever.convenience.turtle.Turtle;


/**
 * shared methods for image manipulation (generating, converting, or filtering)
 * @author Dan
 */
public abstract class TurtleNode {	
	// threading
	protected ProgressMonitor pm;
	protected TurtleSwingWorker threadWorker;
	
	/**
	 * When this {@code TurtleNode} has finished it's task, the result (if any) is stored in turtleResult.
	 */
	protected Turtle turtleResult;
	
	/**
	 * Get the results of running this {@code TurtleNode}.
	 * @return
	 */
	public Turtle getTurtleResult() {
		return turtleResult;
	}

	protected void setTurtleResult(Turtle turtle2) {
		turtleResult = turtle2;
	}

	public void setThreadWorker(TurtleSwingWorker p) {
		threadWorker = p;
	}

	public void setProgressMonitor(ProgressMonitor p) {
		pm = p;
	}
	
	/**
	 * @return the translated name of the {@code TurtleNode}.
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
