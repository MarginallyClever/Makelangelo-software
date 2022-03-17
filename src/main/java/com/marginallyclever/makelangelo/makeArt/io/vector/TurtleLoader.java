package com.marginallyclever.makelangelo.makeArt.io.vector;

import java.io.InputStream;

import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyClever.makelangelo.turtle.Turtle;

public interface TurtleLoader {		
	/**
	 * @return returns a FileNameExtensionFilter with the extensions supported by this filter.
	 */
	public FileNameExtensionFilter getFileNameFilter();

	/**
	 * Checks a string's filename, which includes the file extension, (e.g. foo.jpg).
	 *
	 * @param filename absolute path of file to load.
	 * @return true if this plugin can load this file.
	 */
	public boolean canLoad(String filename);

	/**
	 * attempt to load a file into the system from a given stream
	 * @param inputStream source of image
	 * @return true if load successful.
	 */
	public Turtle load(InputStream inputStream) throws Exception;
}
