package com.marginallyclever.artPipeline.generators;

import java.util.ArrayList;

import com.jogamp.opengl.GL2;
import com.marginallyclever.artPipeline.ImageManipulator;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangeloRobot.MakelangeloRobotDecorator;

/**
 * Generators create gcode from user input.  Fractals might be one example.
 * 
 * Image Generators have to be listed in 
 * src/main/resources/META-INF/services/com.marginallyclever.makelangeloRobot.generators.ImageGenerator
 * in order to be found by the ServiceLoader.  This is so that you could write an independent plugin and 
 * drop it in the same folder as makelangelo software to be "found" by the software.
 * 
 * Don't forget http://www.reverb-marketing.com/wiki/index.php/When_a_new_style_has_been_added_to_the_Makelangelo_software
 * @author dan royer
 *
 */
public abstract class ImageGenerator extends ImageManipulator implements MakelangeloRobotDecorator {
	private ArrayList<ImageGeneratorListener> listeners = new ArrayList<ImageGeneratorListener>();
	public void addListener(ImageGeneratorListener a) {
		listeners.add(a);
	}
	
	public void removeListener(ImageGeneratorListener a) {
		listeners.remove(a);
	}
	
	protected void notifyListeners(Turtle turtle) {
		for( ImageGeneratorListener a : listeners ) a.turtleReady(turtle);
	}
	
	/**
	 * @return true if generate succeeded.
	 */
	abstract public void generate();
	
	/**
	 * @return the gui panel with options for this manipulator
	 */
	public ImageGeneratorPanel getPanel() {
		return null;
	}
	
	/**
	 * live preview as the system is generating.
	 * draw the results as the calculation is being performed.
	 */
	public void render(GL2 gl2) {}
}
