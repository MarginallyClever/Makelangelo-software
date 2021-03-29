package com.marginallyclever.makelangelo.nodes;

import java.io.OutputStream;
import java.util.ArrayList;

import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.core.turtle.Turtle;
import com.marginallyclever.makelangelo.plotter.Plotter;

/**
 * Interface for the service handler
 * @author Dan Royer
 *
 */
public abstract interface SaveFile {
	/**
	 * @return returns a FileNameExtensionFilter with the extensions supported by this filter.
	 */
	public FileNameExtensionFilter getFileNameFilter();

	/**
	 * Checks a string's filename, which includes the file extension, (e.g. foo.jpg).
	 *
	 * @param filename absolute path of file to save.
	 * @return true if this plugin can save this file.
	 */
	public boolean canSave(String filename);
	
	/**
	 * attempt to save makelangelo instructions to a given stream
	 * @param outputStream destination
	 * @param turtles the agnostic set of instructions
	 * @param robot machine hardware settings to use in loading process
	 * @return true if save successful.
	 */
	public boolean save(OutputStream outputStream,ArrayList<Turtle> turtles, Plotter robot);
}
