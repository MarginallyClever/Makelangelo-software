package com.marginallyclever.makelangelo.nodes;

import java.io.InputStream;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Interface for the service handler
 * @author Dan Royer
 *
 */
public abstract interface LoadFile {
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
	 * @param robotController machine hardware settings to use in loading process
	 * @return true if load successful.
	 */
	public boolean load(InputStream inputStream);
}
