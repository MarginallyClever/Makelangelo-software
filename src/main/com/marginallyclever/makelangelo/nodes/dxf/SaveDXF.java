package com.marginallyclever.makelangelo.nodes.dxf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.core.MathHelper;
import com.marginallyclever.core.Point2D;
import com.marginallyclever.core.log.Log;
import com.marginallyclever.core.node.Node;
import com.marginallyclever.core.node.NodeConnectorExistingFile;
import com.marginallyclever.core.turtle.Turtle;
import com.marginallyclever.core.turtle.TurtleMove;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.nodes.LoadAndSaveFile;
import com.marginallyclever.makelangelo.robot.MakelangeloRobot;

/**
 * Saves a Turtle to DXF format.
 * @author Dan Royer
 * @since 7.25.0
 *
 */
public class SaveDXF extends Node implements LoadAndSaveFile {
	private static FileNameExtensionFilter filter = new FileNameExtensionFilter(Translator.get("LoadDXF.filter"), "dxf");
	private NodeConnectorExistingFile inputFile = new NodeConnectorExistingFile("LoadDXF.inputFile",filter,""); 
	
	public SaveDXF() {
		super();
		inputs.add(inputFile);
	}
	
	@Override
	public String getName() {
		return Translator.get("SaveDXF.name");
	}
	
	@Override
	public FileNameExtensionFilter getFileNameFilter() {
		return filter;
	}

	@Override
	public boolean canLoad() {
		return false;
	}

	@Override
	public boolean canSave() {
		return true;
	}

	@Override
	public boolean canLoad(String filename) {
		return false;
	}

	@Override
	public boolean canSave(String filename) {
		String ext = filename.substring(filename.lastIndexOf('.'));
		return (ext.equalsIgnoreCase(".dxf"));
	}

	/**
	 * @param in
	 * @return true if load is successful.
	 */
	@Override
	public boolean load(InputStream in) {
		return false;
	}
	
	@Override
	/**
	 * see http://paulbourke.net/dataformats/dxf/min3d.html for details
	 * @param outputStream where to write the data
	 * @param robot the robot from which the data is obtained
	 * @return true if save succeeded.
	 */
	public boolean save(OutputStream outputStream,ArrayList<Turtle> turtles, MakelangeloRobot robot) {
		Log.message("Saving...");

		try(OutputStreamWriter out = new OutputStreamWriter(outputStream)) {
			Turtle firstTurtle = turtles.get(0);
			// find the actual bounds
			Point2D totalBottom = new Point2D();
			Point2D totalTop = new Point2D();
			Turtle.getBounds(turtles,totalTop,totalBottom);
			
			// header
			out.write("999\nDXF created by Makelangelo software (http://makelangelo.com)\n");
			out.write("0\nSECTION\n");
			out.write("2\nHEADER\n");
			out.write("9\n$ACADVER\n1\nAC1006\n");
			out.write("9\n$INSBASE\n");
			out.write("10\n"+totalBottom.x+"\n");
			out.write("20\n"+totalBottom.y+"\n");
			out.write("30\n0.0\n");
			out.write("9\n$EXTMIN\n");
			out.write("10\n"+totalBottom.x+"\n");
			out.write("20\n"+totalBottom.y+"\n");
			out.write("30\n0.0\n");
			out.write("9\n$EXTMAX\n");
			out.write("10\n"+totalTop.x+"\n");
			out.write("20\n"+totalTop.y+"\n");
			out.write("30\n0.0\n");
			out.write("0\nENDSEC\n");

			// tables section
			out.write("0\nSECTION\n");
			out.write("2\nTABLES\n");
			// line type
			out.write("0\nTABLE\n");
			out.write("2\nLTYPE\n");
			out.write("70\n1\n");
			out.write("0\nLTYPE\n");
			out.write("2\nCONTINUOUS\n");
			out.write("70\n64\n");
			out.write("3\nSolid line\n");
			out.write("72\n65\n");
			out.write("73\n0\n");
			out.write("40\n0.000\n");
			out.write("0\nENDTAB\n");
			// layers
			out.write("0\nTABLE\n");
			out.write("2\nLAYER\n");
			out.write("70\n6\n");
			out.write("0\nLAYER\n");
			out.write("2\n1\n");
			out.write("70\n64\n");
			out.write("62\n7\n");
			out.write("6\nCONTINUOUS\n");
			out.write("0\nLAYER\n");
			out.write("2\n2\n");
			out.write("70\n64\n");
			out.write("62\n7\n");
			out.write("6\nCONTINUOUS\n");
			out.write("0\nENDTAB\n");
			out.write("0\nTABLE\n");
			out.write("2\nSTYLE\n");
			out.write("70\n0\n");
			out.write("0\nENDTAB\n");
			// end tables
			out.write("0\nENDSEC\n");

			// empty blocks section (good form?)
			out.write("0\nSECTION\n");
			out.write("0\nBLOCKS\n");
			out.write("0\nENDSEC\n");
			// now the lines
			out.write("0\nSECTION\n");
			out.write("2\nENTITIES\n");

			boolean isUp=true;
			double x0 = firstTurtle.getX();
			double y0 = firstTurtle.getY();
						
			for( Turtle t : turtles ) {
				for( TurtleMove m : t.history ) {
					if(m.isUp) {
						isUp=true;
					} else {
						if(isUp) isUp=false;
						else {
							out.write("0\nLINE\n");
							out.write("8\n1\n");  // layer 1
							out.write("10\n"+MathHelper.roundOff3(x0)+"\n");
							out.write("20\n"+MathHelper.roundOff3(y0)+"\n");
							out.write("11\n"+MathHelper.roundOff3(m.x)+"\n");
							out.write("21\n"+MathHelper.roundOff3(m.y)+"\n");
						}
					}
					x0=m.x;
					y0=m.y;
				}
			}
			// wrap it up
			out.write("0\nENDSEC\n");
			out.write("0\nEOF\n");
			out.flush();
		}
		catch(IOException e) {
			Log.error("Save Error: "+ e.getLocalizedMessage());
			return false;
		}
		
		return true;
	}
}
