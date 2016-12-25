package com.marginallyclever.makelangeloRobot.converters;

import java.io.IOException;
import java.io.Writer;

import javax.swing.JPanel;

import com.jogamp.opengl.GL2;
import com.marginallyclever.makelangeloRobot.TransformedImage;
import com.marginallyclever.makelangeloRobot.loadAndSave.LoadAndSaveImage;
import com.marginallyclever.makelangeloRobot.ImageManipulator;
import com.marginallyclever.makelangeloRobot.MakelangeloRobotDecorator;
import com.marginallyclever.makelangeloRobot.settings.MakelangeloRobotSettings;

/**
 * Converts a BufferedImage to gcode
 * 
 * Image converters have to be listed in 
 * src/main/resources/META-INF/services/com.marginallyclever.makelangeloRobot.generators.ImageConverter
 * in order to be found by the ServiceLoader.  This is so that you could write an independent plugin and 
 * drop it in the same folder as makelangelo software to be "found" by the software.
 * 
 * Don't forget http://www.reverb-marketing.com/wiki/index.php/When_a_new_style_has_been_added_to_the_Makelangelo_software
 * @author Dan Royer
 *
 */
public abstract class ImageConverter extends ImageManipulator implements MakelangeloRobotDecorator {
	TransformedImage sourceImage;
	LoadAndSaveImage loadAndSave;
	boolean keepIterating=false;
	
	public void setLoadAndSave(LoadAndSaveImage arg0) {
		loadAndSave = arg0;
	}
	
	/**
	 * set the image to be transformed.
	 * @param img the <code>java.awt.image.BufferedImage</code> this filter is using as source material.
	 */
	public void setImage(TransformedImage img) {
		sourceImage=img;
	}
	
	/**
	 * iterative and non-iterative solvers use this method to restart the conversion process.
	 */
	public void reconvert() {
		loadAndSave.reconvert();
	}
	
	/**
	 * run one "step" of an iterative image conversion process.
	 * @return true if conversion should iterate again.
	 */
	public boolean iterate() {
		return false;
	}
	
	public void stopIterating() {
		keepIterating=false;
	}
	
	/**
	 * for "run once" converters, return do the entire conversion and write to disk.
	 * for iterative solvers, the iteration is now done, write to disk.
	 * @param out the Writer to receive the generated gcode.
	 */
	public void finish(Writer out) throws IOException {}
	
	/**
	 * @return the gui panel with options for this manipulator
	 */
	public JPanel getPanel() {
		return null;
	}

	/**
	 * live preview as the system is converting pictures.
	 * draw the results as the calculation is being performed.
	 */
	public void render(GL2 gl2, MakelangeloRobotSettings settings) {}
}
