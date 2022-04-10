package com.marginallyclever.makelangelo.plotter.plottercontrols;

import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.makelangelo.MakelangeloVersion;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.turtle.MovementType;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleMove;
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
import java.util.ArrayList;
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

	public void run(Turtle turtle, Plotter plotter, JFrame parent, int trimHead, int trimTail) throws Exception {
		if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
			String selectedFile = fc.getSelectedFile().getAbsolutePath();
			String fileWithExtension = addExtension(selectedFile,((FileNameExtensionFilter)fc.getFileFilter()).getExtensions());
			logger.debug("File selected by user: {}", fileWithExtension);

			logger.debug("turtle.history.size={} trimHead={} trimTail={}", turtle.history.size(), trimHead, trimTail);			
			Turtle skinnyTurtle = trimTurtle(turtle, trimHead, trimTail);
			logger.debug("skinnyTurtle.history.size={} ?=(trimTail-trimHead)={}", skinnyTurtle.history.size(),trimTail-trimHead);

			int count = countTurtleToolChanges(skinnyTurtle);
			if(count>1) {
				maybeSaveSeparateFiles(count,fileWithExtension, skinnyTurtle, plotter,parent);
			} else {
				saveOneFile(fileWithExtension, skinnyTurtle, plotter);
			}
		}
	}

	/**
	 * remove trimHead commands from the start of the turtle history.
	 * remove trimTail commands from the end of the turtle history.
	 * Returns the {@link Turtle} with the trimmed history.
	 * @param turtle the source turtle.
	 * @return the {@link Turtle} with the trimmed history.
	 */
	protected Turtle trimTurtle(Turtle turtle, int trimHead, int trimTail) {
		Turtle skinny = new Turtle();
		skinny.history.clear();

		TurtleMove lastTC = null;

		int i=0;
		for( TurtleMove m : turtle.history ) {
			if(i<trimHead && m.type == MovementType.TOOL_CHANGE) {
				// watch for the last tool change
				lastTC = m;
			}
			if(i==trimHead && m.type != MovementType.TOOL_CHANGE) {
				// we've reached the trimHead point, so start adding commands, starting with the last tool change.
				skinny.history.add(lastTC);
			}
			if(i>=trimHead && i<trimTail) {
				// between trimHead and trimTail, add all commands.
				skinny.history.add(m);
			}
			++i;
		}
		// insurance?
		skinny.penUp();

		return skinny;
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
			saveSeparateFiles(fileWithExtension, turtle, plotter);
		}
	}

	/**
	 * Splits a {@link Turtle} by tool changes and saves to one file per change.  Given a FILE.EXT it will generate
	 * FILE-1.EXT, FILE-2.EXT, ..., FILE-N.EXT as required.
	 * @param fileWithExtension the absolute path of the file to save
	 * @param turtle the turtle to split and save
	 * @param plotter the plotter reference for generating the gcode.
	 * @return a list of the names of each file created.
	 * @throws Exception
	 */
	protected List<String> saveSeparateFiles(String fileWithExtension, Turtle turtle, Plotter plotter) throws Exception {
		// split filename.ext.  New format will be filename-n.ext
		int last = fileWithExtension.lastIndexOf(".");
		String ext = fileWithExtension.substring(last);
		String fileWithoutExtension = fileWithExtension.substring(0,last);
		// now save each file
		List<Turtle> list = turtle.splitByToolChange();
		List<String> filesCreated = new ArrayList<>();
		int i=0;
		for( Turtle split : list ) {
			++i;
			String newFileName = fileWithoutExtension+"-"+Integer.toString(i)+ext;
			saveOneFile(newFileName,split,plotter);
			filesCreated.add(newFileName);
		}
		return filesCreated;
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
