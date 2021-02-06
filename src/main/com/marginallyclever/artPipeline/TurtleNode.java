package com.marginallyclever.artPipeline;

import java.util.ArrayList;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangeloRobot.MakelangeloRobotDecorator;


/**
 * shared methods for image manipulation (generating, converting, or filtering)
 * @author Dan
 */
public abstract class TurtleNode implements MakelangeloRobotDecorator {
	// used internally for iterating on long jobs.  Could be promoted to whomsoever manages the thread for this node.
	private boolean keepIterating=false;

	/**
	 * When this {@code TurtleNode} starts it might need a sourceImage.
	 */
	protected TransformedImage sourceImage;
	
	/**
	 * When this {@code TurtleNode} has finished it's task, the result (if any) is stored in turtleResult.
	 */
	protected ArrayList<Turtle> turtleResult;

	/**
	 * Get the results of running this {@code TurtleNode}.
	 * @return
	 */
	public ArrayList<Turtle> getTurtleResult() {
		return turtleResult;
	}

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
	public abstract TurtleNodePanel getPanel();
	
	
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
	
	/**
	 * set the image to be transformed.
	 * @param turtle the <code>com.marginallyclever.convenience.turtle.Turtle</code>
	 * @param img the <code>java.awt.image.BufferedImage</code> this filter is using as source material.
	 */
	public void setImage(TransformedImage img) {
		sourceImage=img;
	}
	
	@Override
	public void render(GL2 gl2) {
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
