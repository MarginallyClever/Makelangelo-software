package com.marginallyclever.makelangelo.makeArt.io.vector;

import java.io.OutputStream;

import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.makelangeloRobot.Plotter;

public interface TurtleSaver {
	/**
	 * @return returns a FileNameExtensionFilter with the extensions supported by this filter.
	 */
	public FileNameExtensionFilter getFileNameFilter();
	
	/**
	 * attempt to save makelangelo instructions to a given stream
	 * @param outputStream destination
	 * @param robot machine hardware settings to use in loading process
	 * @param parentComponent parent component of dialogs, if any.
	 * @return true if save successful.
	 */
	public boolean save(OutputStream outputStream,Plotter robot) throws Exception;
}
