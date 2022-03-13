package com.marginallyClever.makelangelo.makeArt.io.vector;

import java.io.OutputStream;

import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyClever.makelangelo.turtle.Turtle;

public interface TurtleSaver {
	public FileNameExtensionFilter getFileNameFilter();
	
	public boolean save(OutputStream outputStream, Turtle turtle) throws Exception;
}
