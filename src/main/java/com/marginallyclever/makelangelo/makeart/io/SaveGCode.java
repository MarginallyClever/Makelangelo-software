package com.marginallyclever.makelangelo.makeart.io;

import com.marginallyclever.convenience.helpers.StringHelper;
import com.marginallyclever.makelangelo.MakelangeloVersion;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeart.turtletool.TrimTurtle;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.plottercontrols.MarlinPlotterPanel;
import com.marginallyclever.makelangelo.plotter.plottercontrols.ProgramPanel;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Line2d;
import com.marginallyclever.makelangelo.turtle.StrokeLayer;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.vecmath.Point2d;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Save the {@link ProgramPanel} instruction buffer to a gcode file of the user's choosing.
 * Relies on {@link MarlinPlotterPanel} to translate the instructions into gcode.
 * @author Dan Royer
 * @since 7.28.0
 */
public class SaveGCode implements TurtleSaver {
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

	// TODO why is this dialog all the way down in the SaveGCode class?
	// it should be in SaveFileAction, maybe.
	public void run(Turtle turtle, Plotter plotter, JFrame parent, int trimHead, int trimTail) throws Exception {
		if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
			String selectedFile = fc.getSelectedFile().getAbsolutePath();
			String fileWithExtension = addExtension(selectedFile,((FileNameExtensionFilter)fc.getFileFilter()).getExtensions());
			logger.debug("File selected by user: {}", fileWithExtension);

			Turtle skinnyTurtle = TrimTurtle.run(turtle, trimHead, trimTail);

			int count = countTurtleToolChanges(skinnyTurtle);
			if(count>1) {
				maybeSaveSeparateFiles(count,fileWithExtension, skinnyTurtle, plotter,parent);
			} else {
				saveOneFile(fileWithExtension, skinnyTurtle, plotter);
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
		return turtle.getLayers().size();
	}

	private String addExtension(String name, String [] extensions) {
		for( String e : extensions ) {
			if(FilenameUtils.getExtension(name).equalsIgnoreCase(e)) return name;
		}
		
		return name + "." + extensions[0];
	}
		
	protected void saveOneFile(String filename, Turtle turtle, Plotter robot) throws Exception {
		logger.debug("saving...");
		try (FileOutputStream stream = new FileOutputStream(filename)) {
			save(stream, turtle, robot.getSettings());
		}
		logger.debug("done.");
	}

	@Override
	public FileNameExtensionFilter getFileNameFilter() {
		return new FileNameExtensionFilter("GCode", "gcode");
	}

	/**
	 * Save a turtle to a stream
	 * @param outputStream destination of path
	 * @param turtle source of path
	 * @param settings plotter settings
	 * @return true if save successful.
	 * @throws Exception if save failed.
	 */
	@Override
	public boolean save(OutputStream outputStream, Turtle turtle, PlotterSettings settings) throws Exception {
		try (Writer out = new OutputStreamWriter(outputStream)) {
			writeHeader(out,settings,turtle.getBounds());
			out.write(settings.getPenUpString() + "\n");

			StrokeLayer previousLayer = null;
			for (var layer : turtle.getLayers()) {
				if (layer.isEmpty()) continue;
				if(previousLayer == null
						|| !previousLayer.getColor().equals(layer.getColor())
						|| previousLayer.getDiameter() != layer.getDiameter()) {
					// this layer is a different color or diameter than the previous layer.
					out.write(settings.getToolChangeString(layer.getColor().hashCode()) + "\n");
					previousLayer = layer;
				}

				StringBuilder sb = new StringBuilder();
				int count = saveLayer(sb, layer, settings);
				if (count > 0) out.write(sb.toString());
			}
			writeFooter(out,settings);
			return true;
		}
	}

	/**
	 * Save a {@link Turtle} layer to a string builder
	 * @param sb the string builder to append to
	 * @param layer the layer to save
	 * @param settings the plotter settings
	 * @return the number of points in the layer
	 */
	private int saveLayer(StringBuilder sb, StrokeLayer layer, PlotterSettings settings) {
		int count = 0;
		for( var line : layer.getAllLines() ) {
			StringBuilder sb2 = new StringBuilder();
			int c2 = saveLine(sb2, line, settings);
			if(c2>0) {
				sb.append(sb2);
				count += c2;
			}
		}
		return count;
	}

	/**
	 * Save a line to a string builder
	 * @param sb the string builder to append to
	 * @param line the line to save
	 * @param settings the plotter settings
	 * @return the number of points in the line
	 */
	private int saveLine(StringBuilder sb, Line2d line, PlotterSettings settings) {
		if(line.isEmpty()) return 0;

		int count=0;
		var iter = line.iterator();
		Point2d p0 = iter.next();
		sb.append(settings.getTravelToString(p0.x, p0.y)).append("\n")
		  .append(settings.getPenDownString()).append("\n");
		while(iter.hasNext()) {
			var p = iter.next();
			sb.append(settings.getDrawToString(p.x, p.y)).append("\n");
			count++;
		}
		sb.append(settings.getPenUpString()).append("\n");
		return count;
	}

	private void writeFooter(Writer out, PlotterSettings settings) throws IOException{
		out.write(";Start of user gcode\n");
		out.write(settings.getString(PlotterSettings.END_GCODE));
		out.write("\n;End of user gcode\n");
		out.write(";End of Gcode\n");
		out.flush();
	}

	private void writeHeader(Writer out, PlotterSettings settings,Rectangle2D.Double bounds) throws IOException{
		out.write(";Generated with " + MakelangeloVersion.getFullOrLiteVersionStringRelativeToSysEnvDevValue() + "\n");
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
		Date date = new Date(System.currentTimeMillis());
		out.write("; " + formatter.format(date) + "\n");

		out.write(";FLAVOR:Marlin-polargraph\n");
		out.write(";MINX:" + StringHelper.formatDouble(bounds.x) + "\n");
		out.write(";MINY:" + StringHelper.formatDouble(bounds.y) + "\n");
		//out.write(";MINZ:0.000\n");
		out.write(";MAXX:" + StringHelper.formatDouble(bounds.width + bounds.x) + "\n");
		out.write(";MAXY:" + StringHelper.formatDouble(bounds.height + bounds.y) + "\n");
		//out.write(";MAXZ:0.000\n");
		out.write(";Start of user gcode\n");
		out.write(settings.getString(PlotterSettings.START_GCODE));
		out.write("\n;End of user gcode\n");
		out.write(settings.getFindHomeString() + "\n");  // go home
	}
}
