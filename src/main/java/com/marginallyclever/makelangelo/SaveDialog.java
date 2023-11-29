package com.marginallyclever.makelangelo;

import com.marginallyclever.makelangelo.makeart.io.TurtleFactory;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class SaveDialog {
	private static final Logger logger = LoggerFactory.getLogger(SaveDialog.class);
	
	private static final JFileChooser fc = new JFileChooser();
	
	public SaveDialog() {
		for( FileNameExtensionFilter ff : TurtleFactory.getSaveExtensions() ) {
			fc.addChoosableFileFilter(ff);
		}
		// do not allow wild card (*.*) file extensions
		fc.setAcceptAllFileFilterUsed(false);
	}
	
	public void run(Turtle t, Window parent, PlotterSettings settings) throws Exception {
		if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
			String selectedFile = fc.getSelectedFile().getAbsolutePath();
			String withExtension = addExtension(selectedFile,((FileNameExtensionFilter)fc.getFileFilter()).getExtensions());
			logger.debug("File selected by user: {}", withExtension);
			TurtleFactory.save(t,withExtension,settings);
		}
	}

	private String addExtension(String name, String [] extensions) {
		for( String e : extensions ) {
			if(FilenameUtils.getExtension(name).equalsIgnoreCase(e)) return name;
		}
		
		return name + "." + extensions[0];
	}

	public static String getLastPath() {
		return fc.getCurrentDirectory().toString();
	}
	
	public static void setLastPath(String lastPath) {
		fc.setCurrentDirectory((lastPath==null?null : new File(lastPath)));
	}
}
