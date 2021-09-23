package com.marginallyclever.makelangelo.makeArt.io;

import java.awt.Component;
import java.io.InputStream;

import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;

public interface LoadResource {
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
	 * @param robot machine hardware settings to use in loading process
	 * @param parentComponent parent component of dialogs, if any.
	 * @return true if load successful.
	 */
	public Turtle load(InputStream inputStream,MakelangeloRobot robot, Component parentComponent) throws Exception;
}
