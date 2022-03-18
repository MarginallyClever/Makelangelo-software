package com.marginallyclever.makelangelo.makeart.io.vector;

import java.io.OutputStream;

import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.makelangelo.turtle.Turtle;

public interface TurtleSaver {
	public FileNameExtensionFilter getFileNameFilter();
	
	public boolean save(OutputStream outputStream,Turtle turtle) throws Exception;
}
