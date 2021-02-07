package com.marginallyclever.convenience.nodes;

import java.util.ArrayList;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangelo.robot.MakelangeloRobotDecorator;


/**
 * @author Dan Royer
 * @since 7.25.0
 */
public abstract class Node implements MakelangeloRobotDecorator {
	// TODO make this not public
	public ArrayList<NodeConnector<?>> inputs = new ArrayList<NodeConnector<?>>(); 
	// TODO make this not public
	public ArrayList<NodeConnector<?>> outputs = new ArrayList<NodeConnector<?>>(); 

	/**
	 * When this {@link Node} has finished it's task, the result (if any) is stored in turtleResult.
	 */
	@Deprecated
	protected ArrayList<Turtle> turtleResult;
	
	// used internally for iterating on long jobs.  Could be promoted to whomsoever manages the thread for this node.
	private boolean keepIterating=false;

	/**
	 * Get the results of running this {@link Node}.
	 * @return
	 */
	@Deprecated
	public ArrayList<Turtle> getTurtleResult() {
		return turtleResult;
	}

	@Deprecated
	protected void setTurtleResult(ArrayList<Turtle> list) {
		turtleResult = list;
	}
	
	/**
	 * @return the translated name of the {@code TurtleNode}.
	 */
	abstract public String getName();

	/**
	 * @return the gui panel with options for this manipulator
	 */
	public abstract NodePanel getPanel();
	
	
	/**
	 * Inputs have been updated, please start over.
	 */
	public void restart() {}
	
	/**
	 * run one step of an iterative process.
	 * @return true if conversion should iterate again.
	 */
	public boolean iterate() {
		return false;
	}
	
	public void stopIterating() {
		keepIterating=false;
	}
	
	public boolean getKeepIterating() {
		return keepIterating;
	}
	
	public void setKeepIterating(boolean state) {
		keepIterating=state;
	}
	
	@Override
	public void render(GL2 gl2) {}
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
