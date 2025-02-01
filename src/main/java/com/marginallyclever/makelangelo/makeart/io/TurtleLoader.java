package com.marginallyclever.makelangelo.makeart.io;

import com.marginallyclever.makelangelo.turtle.Turtle;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.InputStream;

/**
 * A TurtleLoader is a plugin that can load a file into the system as a {@link Turtle}.
 */
public interface TurtleLoader {		
	/**
	 * @return returns a Swing {@link FileNameExtensionFilter} with the extensions supported by this filter.
	 */
	FileNameExtensionFilter getFileNameFilter();

	/**
	 * Checks a string's filename, which includes the file extension, (e.g. foo.jpg).
	 *
	 * @param filename absolute path of file to load.
	 * @return true if this plugin can load this file.
	 */
	boolean canLoad(String filename);

	/**
	 * attempt to load a file into the system from a given stream
	 * @param inputStream source of image
	 * @return true if load successful.
	 */
	Turtle load(InputStream inputStream) throws Exception;
}
