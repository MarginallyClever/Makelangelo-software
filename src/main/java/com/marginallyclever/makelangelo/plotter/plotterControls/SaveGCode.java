package com.marginallyclever.makelangelo.plotter.plotterControls;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.makelangelo.MakelangeloVersion;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleMove;
import java.awt.geom.Rectangle2D;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Save the {@link ProgramInterface} instruction buffer to a gcode file of the user's choosing.
 * Relies on {@link MarlinPlotterInterface} to translate the instructions into gcode.
 * @author Dan Royer
 * @since 7.28.0
 */
public class SaveGCode {
	private static final Logger logger = LoggerFactory.getLogger(SaveGCode.class);
	
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
			logger.debug("File selected by user: {}", fileWithExtension);
			save(fileWithExtension,turtle,robot);
		}
	}

	private String addExtension(String name, String [] extensions) {
		for( String e : extensions ) {
			if(FilenameUtils.getExtension(name).equalsIgnoreCase(e)) return name;
		}
		
		return name + "." + extensions[0];
	}
	
	boolean verboseGCodeHeader = true;
	
	private void save(String filename, Turtle turtle, Plotter robot) throws Exception {
		logger.debug("saving...");
		
		try (Writer out = new OutputStreamWriter(new FileOutputStream(filename))) {
			
			if (verboseGCodeHeader) {
				out.write(";FLAVOR:Marlin-polargraph\n");

				//out.write(";TIME:"+ "\n"); // TODO
				
				Rectangle2D.Double bounds = turtle.getBounds();
				out.write(";MINX:" + StringHelper.formatDouble(bounds.x) + "\n");
				out.write(";MINY:" + StringHelper.formatDouble(bounds.y) + "\n");
				//out.write(";MINZ:0.000\n");
				out.write(";MAXX:" + StringHelper.formatDouble(bounds.width + bounds.x) + "\n");
				out.write(";MAXY:" + StringHelper.formatDouble(bounds.height + bounds.y) + "\n");
				//out.write(";MAXZ:0.000\n");

				out.write(";Generated with " + MakelangeloVersion.getFullOrLiteVersionStringRelativeToSysEnvDevValue() + "\n");
			}
			
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
			Date date = new Date(System.currentTimeMillis());
			out.write("; " + formatter.format(date) + "\n");
			
			if ( verboseGCodeHeader &&  "true".equalsIgnoreCase(System.getenv("DEV")) ){ // TODO or to Review
				out.write("; plotter:" + robot.getPlotterSettings().getHardwareName() + "\n");// TODO to review as this is alwayse "Makelangelo 5" currently 
				// TODO Paper Size , plotter type,  plotter config , ...
				// TODO origin/base filename (ex like ;MESH:ellipse.svg or scratch_test.sb3, ... )

				out.write("; turtle.history.size():" + turtle.history.size() + "\n");
				out.write("; turtle.splitByToolChange().size():" + turtle.splitByToolChange().size() + "\n");// todo ssi reordered cf 
				/*
	2022-02-24 05:08:10,768 DEBUG c.m.makelangelo.turtle.Turtle - Turtle.splitByToolChange() into 4 sections. 
	2022-02-24 05:08:10,769 DEBUG c.m.makelangelo.turtle.Turtle - Turtle.splitByToolChange() 2 not-empty sections. 
				 */
			}
			
			// TODO MarlinPlotterInterface.getFindHomeString()?
			out.write("G28\n");  // go home

			// TODO user "start gcode"
			// https://marlinfw.org/docs/gcode/M092.html Set Axis Steps-per-unit
			// https://marlinfw.org/docs/gcode/M117.html Set LCD Message
			// https://marlinfw.org/docs/gcode/M220.html Set Feedrate Percentage
			// ...
			
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
					case TurtleMove.DRAW_LINE -> {
						if (isUp) {
							// go to m and put pen down
							if (previousMovement == null) previousMovement = m;
							out.write(MarlinPlotterInterface.getTravelToString(robot,previousMovement.x, previousMovement.y) + "\n");
							out.write(MarlinPlotterInterface.getPenDownString(robot) + "\n");
							isUp = false;
						}
						out.write(MarlinPlotterInterface.getDrawToString(robot,m.x, m.y) + "\n");
						previousMovement = m;
					}
					case TurtleMove.TOOL_CHANGE -> {
						// TODO ? ";TIME_ELAPSED:"
						// TODO "tool change start gcode" ( if i want a bip M300 and/or something else like a park position ...)
						out.write(MarlinPlotterInterface.getPenUpString(robot) + "\n");
						out.write(MarlinPlotterInterface.getToolChangeString(m.getColor().toInt()) + "\n");
						// todo "tool change end gcode"
					}
				}
			}
			if (!isUp) out.write(MarlinPlotterInterface.getPenUpString(robot) + "\n");
			
			//TODO user "end gcode" ( M300 , park position , disable motors ... )

			out.write(";End of Gcode\n"); 
			
			out.flush();
		}
		logger.debug("done.");
	}
}
