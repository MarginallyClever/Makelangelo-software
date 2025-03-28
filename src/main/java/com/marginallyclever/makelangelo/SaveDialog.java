package com.marginallyclever.makelangelo;

import com.marginallyclever.makelangelo.makeart.io.TurtleFactory;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class SaveDialog {
	private static final Logger logger = LoggerFactory.getLogger(SaveDialog.class);
	
	private static final JFileChooser fc = TurtleFactory.getSaveFileChooser();
	
	public SaveDialog() {}
	
	public void run(Turtle t, Window parent, PlotterSettings settings) throws Exception {
		if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
			String selectedFile = fc.getSelectedFile().getAbsolutePath();
			logger.debug("File selected by user: {}", selectedFile);
			TurtleFactory.save(t,selectedFile,settings);
		}
	}

	public static String getLastPath() {
		return fc.getCurrentDirectory().toString();
	}
	
	public static void setLastPath(String lastPath) {
		fc.setCurrentDirectory((lastPath==null?null : new File(lastPath)));
	}
}
