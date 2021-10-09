package com.marginallyclever.makelangelo;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.makeArt.io.vector.TurtleFactory;
import com.marginallyclever.makelangelo.turtle.Turtle;

public class SaveDialog {
	private JFileChooser fc = new JFileChooser();
	
	public SaveDialog() {
		for( FileNameExtensionFilter ff : TurtleFactory.getSaveExtensions() ) {
			fc.addChoosableFileFilter(ff);
		}
		// do not allow wild card (*.*) file extensions
		fc.setAcceptAllFileFilterUsed(false);
	}
	
	public SaveDialog(String lastDir) {
		// remember the last path used, if any
		fc.setCurrentDirectory((lastDir==null?null : new File(lastDir)));
	}
	
	public void run(Turtle t,JFrame parent) throws Exception {
		if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
			String selectedFile = fc.getSelectedFile().getAbsolutePath();
			String withExtension = addExtension(selectedFile,((FileNameExtensionFilter)fc.getFileFilter()).getExtensions());
			Log.message("File selected by user: "+withExtension);
			TurtleFactory.save(t,withExtension);
		}
	}

	private String addExtension(String name, String [] extensions) {
		for( String e : extensions ) {
			if(FilenameUtils.getExtension(name).equalsIgnoreCase(e)) return name;
		}
		
		return name + "." + extensions[0];
	}
}
