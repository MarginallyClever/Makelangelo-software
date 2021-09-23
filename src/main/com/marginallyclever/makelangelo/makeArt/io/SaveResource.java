package com.marginallyclever.makelangelo.makeArt.io;

import java.awt.Component;
import java.io.OutputStream;

import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.makelangeloRobot.MakelangeloRobot;

public interface SaveResource {
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
	 * @param robot machine hardware settings to use in loading process
	 * @param parentComponent parent component of dialogs, if any.
	 * @return true if save successful.
	 */
	public boolean save(OutputStream outputStream,MakelangeloRobot robot, Component parentComponent);
}
