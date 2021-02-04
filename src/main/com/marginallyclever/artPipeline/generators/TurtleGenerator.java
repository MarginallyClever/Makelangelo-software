package com.marginallyclever.artPipeline.generators;

import com.jogamp.opengl.GL2;
import com.marginallyclever.artPipeline.TurtleManipulator;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangeloRobot.MakelangeloRobotDecorator;

/**
 * Generators create gcode from user input.  Fractals might be one example.
 * 
 * TurtleGenerators have to be listed in 
 * src/main/resources/META-INF/services/com.marginallyclever.makelangeloRobot.generators.TurtleGenerator
 * in order to be found by the ServiceLoader.  This is so that you could write an independent plugin and 
 * drop it in the same folder as makelangelo software to be "found" by the software.
 * 
 * Don't forget http://www.reverb-marketing.com/wiki/index.php/When_a_new_style_has_been_added_to_the_Makelangelo_software
 * @author dan royer
 *
 */
public abstract class TurtleGenerator extends TurtleManipulator implements MakelangeloRobotDecorator {
	/**
	 * @return a Turtle containing the path generated.  Null on failure.
	 */
	abstract public Turtle generate();
	
	/**
	 * @return the gui panel with options for this manipulator
	 */
	abstract public ImageGeneratorPanel getPanel();
	
	/**
	 * live preview as the system is generating.
	 * draw the results as the calculation is being performed.
	 */
	@Override
	public void render(GL2 gl2) {}
}
