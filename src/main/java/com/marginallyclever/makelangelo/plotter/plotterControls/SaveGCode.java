package com.marginallyClever.makelangelo.plotter.plotterControls;

import com.marginallyClever.convenience.StringHelper;
import com.marginallyClever.makelangelo.MakelangeloVersion;
import com.marginallyClever.makelangelo.Translator;
import com.marginallyClever.makelangelo.plotter.Plotter;
import com.marginallyClever.makelangelo.turtle.MovementType;
import com.marginallyClever.makelangelo.turtle.Turtle;
import com.marginallyClever.makelangelo.turtle.TurtleMove;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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

	public void run(Turtle turtle, Plotter plotter, JFrame parent) throws Exception {
		if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
			String selectedFile = fc.getSelectedFile().getAbsolutePath();
			String fileWithExtension = addExtension(selectedFile,((FileNameExtensionFilter)fc.getFileFilter()).getExtensions());
			logger.debug("File selected by user: {}", fileWithExtension);

			int count = countTurtleToolChanges(turtle);
			if(count>1) {
				maybeSaveSeparateFiles(count,fileWithExtension, turtle, plotter,parent);
			} else {
				saveOneFile(fileWithExtension, turtle, plotter);
			}
		}
	}

	/**
	 * Offer to split the gcode file into one file per tool change, which is probably one per color.
	 * @param count number of tool changes detected
	 * @param fileWithExtension the user's desired filename.
	 * @param turtle the source turtle containing many tool changes.
	 * @param plotter the plotter that will translate the turtle to gcode.
	 * @param parent the parent frame for the confirmation dialog.
	 * @throws Exception if saving the file fails.
	 */
	private void maybeSaveSeparateFiles(int count,String fileWithExtension, Turtle turtle, Plotter plotter, JFrame parent) throws Exception {
		String title = Translator.get("SaveGCode.splitGCodeTitle");
		String query = Translator.get("SaveGCode.splitGCodeQuestion",new String[]{Integer.toString(count)});
		int n = JOptionPane.showConfirmDialog(parent, query, title, JOptionPane.YES_NO_OPTION);
		if(n==JOptionPane.NO_OPTION) {
			saveOneFile(fileWithExtension, turtle, plotter);
		} else if(n==JOptionPane.YES_OPTION) {
			// split filename.ext.  New format will be filename-n.ext
			int last = fileWithExtension.lastIndexOf(".");
			String ext = fileWithExtension.substring(last);
			String fileWithoutExtension = fileWithExtension.substring(0,last);
			// now save each file
			List<Turtle> list = turtle.splitByToolChange();
			int i=0;
			for( Turtle t : list ) {
				++i;
				String newFileName = fileWithoutExtension+"-"+Integer.toString(i)+ext;
				saveOneFile(newFileName,turtle,plotter);
			}
		}
	}

	private int countTurtleToolChanges(Turtle turtle) {
		int i=0;
		for( TurtleMove m : turtle.history ) {
			if(m.type == MovementType.TOOL_CHANGE) ++i;
		}
		return i;
	}

	private String addExtension(String name, String [] extensions) {
		for( String e : extensions ) {
			if(FilenameUtils.getExtension(name).equalsIgnoreCase(e)) return name;
		}
		
		return name + "." + extensions[0];
	}
		
	protected void saveOneFile(String filename, Turtle turtle, Plotter robot) throws Exception {
		logger.debug("saving...");
		
		try (Writer out = new OutputStreamWriter(new FileOutputStream(filename))) {			
			out.write(";Generated with " + MakelangeloVersion.getFullOrLiteVersionStringRelativeToSysEnvDevValue() + "\n");
			out.write(";FLAVOR:Marlin-polargraph\n");
			Rectangle2D.Double bounds = turtle.getBounds();
			out.write(";MINX:" + StringHelper.formatDouble(bounds.x) + "\n");
			out.write(";MINY:" + StringHelper.formatDouble(bounds.y) + "\n");
			//out.write(";MINZ:0.000\n");
			out.write(";MAXX:" + StringHelper.formatDouble(bounds.width + bounds.x) + "\n");
			out.write(";MAXY:" + StringHelper.formatDouble(bounds.height + bounds.y) + "\n");
			//out.write(";MAXZ:0.000\n");
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
			Date date = new Date(System.currentTimeMillis());
			out.write("; " + formatter.format(date) + "\n");
			
			// TODO MarlinPlotterInterface.getFindHomeString()?
			out.write("G28\n");  // go home

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
						out.write(MarlinPlotterInterface.getPenUpString(robot) + "\n");
						out.write(MarlinPlotterInterface.getToolChangeString(m.getColor().toInt()) + "\n");
					}
				}
			}
			if (!isUp) out.write(MarlinPlotterInterface.getPenUpString(robot) + "\n");

			out.write(";End of Gcode\n"); 
			
			out.flush();
		}
		logger.debug("done.");
	}
}
