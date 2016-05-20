package com.marginallyclever.loaders;

import java.io.InputStream;

import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.makelangeloRobot.MakelangeloRobot;

public interface LoadFileType {
	/**
	 * Just what it sounds like.
	 * @return
	 */
	public FileNameExtensionFilter getFileNameFilter();

	/**
	 * Checks a string's filename, which includes the file extension, (e.g. foo.jpg).
	 *
	 * @param filename - image filename.
	 * @return if the file is one of the acceptable image types.
	 */
	public boolean canLoad(String filename);
	
	/**
	 * attempt to load a file into the system
	 * @param filename
	 * @return true if load successful.
	 */
	public boolean load(InputStream in,MakelangeloRobot robot);
}
