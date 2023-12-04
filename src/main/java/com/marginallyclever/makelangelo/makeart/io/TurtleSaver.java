package com.marginallyclever.makelangelo.makeart.io;

import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Turtle;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.OutputStream;

public interface TurtleSaver {
	FileNameExtensionFilter getFileNameFilter();

	/**
	 * Save a turtle to a stream
	 * @param outputStream destination of path
	 * @param turtle source of path
	 * @param settings plotter settings
	 * @return true if save successful.
	 * @throws Exception if save failed.
	 */
	boolean save(OutputStream outputStream,Turtle turtle, PlotterSettings settings) throws Exception;
}
