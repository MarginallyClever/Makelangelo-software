package com.marginallyclever.makelangelo.makeArt.io.vector;

import java.io.OutputStream;

import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangeloRobot.Plotter;

public interface TurtleSaver {
	public FileNameExtensionFilter getFileNameFilter();
	
	public boolean save(OutputStream outputStream,Turtle turtle) throws Exception;
}
