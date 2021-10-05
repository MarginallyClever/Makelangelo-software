package com.marginallyclever.makelangelo;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangelo.makeArt.io.vector.TurtleFactory;

public class LoadDialog {
	private JFileChooser fc = new JFileChooser();
	private String lastFileIn="";
	
	public String getLastFileIn() {
		return lastFileIn;
	}

	public LoadDialog() {
		for( FileFilter ff : TurtleFactory.getLoadExtensions() ) {
			fc.addChoosableFileFilter(ff);
		}
		// no wild card filter, please.
		fc.setAcceptAllFileFilterUsed(false);
	}
	
	public LoadDialog(String lastFileIn) {
		this();
		// remember the last path used, if any
		fc.setCurrentDirectory((lastFileIn==null?null : new File(lastFileIn)));
	}

	public Turtle run(JFrame mainFrame) throws Exception {
		if (fc.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
			lastFileIn = fc.getSelectedFile().getAbsolutePath();
			Log.message("File selected by user: "+lastFileIn);
			return TurtleFactory.load(lastFileIn);
		}
		return null;
	}
}
