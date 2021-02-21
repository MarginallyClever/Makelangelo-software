package com.marginallyclever.core.node;

import java.util.ArrayList;


/**
 * All {@link Node} should be registered with ./src/main/resources/META-INF/services/{@link Node}
 * @author Dan Royer
 * @since 7.25.0
 */
public abstract class Node {
	public ArrayList<NodeConnector<?>> inputs = new ArrayList<NodeConnector<?>>(); 
	public ArrayList<NodeConnector<?>> outputs = new ArrayList<NodeConnector<?>>(); 

	// used internally for iterating on long jobs.  Could be promoted to whomsoever manages the thread for this node.
	private boolean keepIterating=false;

	/**
	 * @return the translated name of the {@link Node}.
	 */
	abstract public String getName();
	
	/**
	 * Inputs have been updated, please start over.
	 */
	public void restart() {}
	
	/**
	 * Run one step of an iterative process.
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
