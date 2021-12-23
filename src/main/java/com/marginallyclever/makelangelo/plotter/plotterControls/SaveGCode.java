package com.marginallyclever.makelangelo.plotter.plotterControls;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleMove;

public class SaveGCode {
	private final JFileChooser fc = new JFileChooser();

	public SaveGCode() {
		FileNameExtensionFilter filter = new FileNameExtensionFilter("GCode", "gcode");
		fc.addChoosableFileFilter(filter);
		// do not allow wild card (*.*) file extensions
		fc.setAcceptAllFileFilterUsed(false);
	}

	public SaveGCode(String lastDir) {
		// remember the last path used, if any
		fc.setCurrentDirectory((lastDir==null?null : new File(lastDir)));
	}

	public void run(Turtle turtle, Plotter robot, JFrame parent) throws Exception {
		if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
			String selectedFile = fc.getSelectedFile().getAbsolutePath();
			String fileWithExtension = addExtension(selectedFile,((FileNameExtensionFilter)fc.getFileFilter()).getExtensions());
			Log.message("File selected by user: "+fileWithExtension);
			save(fileWithExtension,turtle,robot);
		}
	}

	private String addExtension(String name, String [] extensions) {
		for( String e : extensions ) {
			if(FilenameUtils.getExtension(name).equalsIgnoreCase(e)) return name;
		}
		
		return name + "." + extensions[0];
	}
	
	private void save(String filename, Turtle turtle, Plotter robot) throws Exception {
		Log.message("saving...");
		
		try (Writer out = new OutputStreamWriter(new FileOutputStream(filename))) {

			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
			Date date = new Date(System.currentTimeMillis());
			out.write("; " + formatter.format(date) + "\n");
			out.write("G28\n");  // go home

			boolean isUp = true;

			TurtleMove previousMovement = null;
			for (int i = 0; i < turtle.history.size(); ++i) {
				TurtleMove m = turtle.history.get(i);

				switch (m.type) {
					case TurtleMove.TRAVEL -> {
						if (!isUp) {
							// lift pen up
							out.write(MarlinPlotterInterface.getPenUpString(robot) + "\n");
							isUp = true;
						}
						previousMovement = m;
					}
					case TurtleMove.DRAW -> {
						if (isUp) {
							// go to m and put pen down
							if (previousMovement == null) previousMovement = m;
							out.write(MarlinPlotterInterface.getTravelToString(previousMovement.x, previousMovement.y) + "\n");
							out.write(MarlinPlotterInterface.getPenDownString(robot) + "\n");
							isUp = false;
						}
						out.write(MarlinPlotterInterface.getDrawToString(m.x, m.y) + "\n");
						previousMovement = m;
					}
					case TurtleMove.TOOL_CHANGE -> out.write(MarlinPlotterInterface.getToolChangeString(m.getColor().toInt()) + "\n");
				}
			}
			if (!isUp) out.write(MarlinPlotterInterface.getPenUpString(robot) + "\n");
		}

		Log.message("done.");
	}
}
