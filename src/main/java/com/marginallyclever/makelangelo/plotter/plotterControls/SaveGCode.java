package com.marginallyclever.makelangelo.plotter.plotterControls;

import com.marginallyclever.makelangelo.MakelangeloVersion;
import com.marginallyclever.convenience.StringHelper;
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
		
	protected void save(String filename, Turtle turtle, Plotter robot) throws Exception {
		logger.debug("saving...");
		
		try (Writer out = new OutputStreamWriter(new FileOutputStream(filename))) {
			out.write(";FLAVOR:Marlin-polargraph\n");
			Rectangle2D.Double bounds = turtle.getBounds();
			out.write(";MINX:" + StringHelper.formatDouble(bounds.x) + "\n");
			out.write(";MINY:" + StringHelper.formatDouble(bounds.y) + "\n");
			//out.write(";MINZ:0.000\n");
			out.write(";MAXX:" + StringHelper.formatDouble(bounds.width + bounds.x) + "\n");
			out.write(";MAXY:" + StringHelper.formatDouble(bounds.height + bounds.y) + "\n");
			//out.write(";MAXZ:0.000\n");

			out.write(";Generated with " + MakelangeloVersion.getFullOrLiteVersionStringRelativeToSysEnvDevValue() + "\n");
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
			Date date = new Date(System.currentTimeMillis());
			out.write("; " + formatter.format(date) + "\n");
			
			if ( !robot.getSettings().getUserGcode().doUserStartGCodeContaineG28(robot.getSettings())){
				out.write("G28\n");  // go home // TODO MarlinPlotterInterface.getFindHomeString()?
			}						
			out.write("; User General Start-Gcode - BEGIN\n");
			out.write(robot.getSettings().getUserGcode().resolvePlaceHolderAndEvalExpression(robot.getSettings().getUserGcode().getUserGeneralStartGcode(),robot.getSettings()) +"\n");
			out.write("; User General Start-Gcode - END\n");
			//

			boolean isUp = true;

			TurtleMove previousMovement = null;
			for (int i = 0; i < turtle.history.size(); ++i) {
				TurtleMove m = turtle.history.get(i);

				switch (m.type) {
					case TRAVEL -> {
						if (!isUp) {
							// lift pen up
							out.write(MarlinPlotterInterface.getPenUpString(robot) + "\n");
							isUp = true;
						}
						previousMovement = m;
					}
					case DRAW_LINE -> {
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
					case TOOL_CHANGE -> {
						out.write("; User ToolChange Star-Gcode - BEGIN\n");
						out.write(robot.getSettings().getUserGcode().resolvePlaceHolderAndEvalExpression(robot.getSettings().getUserGcode().getUserToolChangeStartGcode(),robot.getSettings()) + "\n");
						out.write("; User ToolChange Star-Gcode - END\n");
						out.write(MarlinPlotterInterface.getPenUpString(robot) + "\n");
						out.write(MarlinPlotterInterface.getToolChangeString(m.getColor().toInt()) + "\n");
						out.write("; User ToolChange End-Gcode - BEGIN\n");
						out.write(robot.getSettings().getUserGcode().resolvePlaceHolderAndEvalExpression(robot.getSettings().getUserGcode().getUserToolChangeEndGcode(),robot.getSettings()) + "\n");
						out.write("; User ToolChange End-Gcode - END\n");
					}
				}
			}
			if (!isUp) out.write(MarlinPlotterInterface.getPenUpString(robot) + "\n");
 
			out.write("; User General End-Gcode - BEGIN\n");
			out.write(robot.getSettings().getUserGcode().resolvePlaceHolderAndEvalExpression(robot.getSettings().getUserGcode().getUserGeneralEndGcode(),robot.getSettings()) +"\n");
			out.write("; User General End-Gcode - END\n");
			
			out.write(";End of Gcode\n");
				
			out.flush();
		}
		logger.debug("done.");
	}
}
