package com.marginallyclever.makelangelo.makeArt.io.vector;

import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.convenience.turtle.TurtleMove;
import com.marginallyclever.makelangelo.makeArt.ImageManipulator;
import com.marginallyclever.makelangeloRobot.Plotter;
import com.marginallyclever.makelangeloRobot.settings.PlotterSettings;

/**
 * @author Dan Royer
 *
 */
public class SaveDXF extends ImageManipulator implements TurtleSaver {
	private static FileNameExtensionFilter filter = new FileNameExtensionFilter("DXF R12", "dxf");
	
	@Override
	public String getName() {
		return "DXF";
	}
	
	@Override
	public FileNameExtensionFilter getFileNameFilter() {
		return filter;
	}

	/**
	 * see http://paulbourke.net/dataformats/dxf/min3d.html for details
	 * @param outputStream where to write the data
	 * @param robot the robot from which the data is obtained
	 * @return true if save succeeded.
	 */
	@Override
	public boolean save(OutputStream outputStream, Plotter robot) throws Exception {
		Log.message("saving...");
		Turtle turtle = robot.getTurtle();
		PlotterSettings settings = robot.getSettings();
		
		OutputStreamWriter out = new OutputStreamWriter(outputStream);
		// header
		out.write("999\nDXF created by Makelangelo software (http://makelangelo.com)\n");
		out.write("0\nSECTION\n");
		out.write("2\nHEADER\n");
		out.write("9\n$ACADVER\n1\nAC1006\n");
		out.write("9\n$INSBASE\n");
		out.write("10\n"+settings.getPaperLeft()+"\n");
		out.write("20\n"+settings.getPaperBottom()+"\n");
		out.write("30\n0.0\n");
		out.write("9\n$EXTMIN\n");
		out.write("10\n"+settings.getPaperLeft()+"\n");
		out.write("20\n"+settings.getPaperBottom()+"\n");
		out.write("30\n0.0\n");
		out.write("9\n$EXTMAX\n");
		out.write("10\n"+settings.getPaperRight()+"\n");
		out.write("20\n"+settings.getPaperTop()+"\n");
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
		double x0 = settings.getHomeX();
		double y0 = settings.getHomeY();
		
		String matchUp = settings.getPenUpString();
		String matchDown = settings.getPenDownString();
		
		if(matchUp.contains(";")) {
			matchUp = matchUp.substring(0, matchUp.indexOf(";"));
		}
		matchUp = matchUp.replaceAll("\n", "");

		if(matchDown.contains(";")) {
			matchDown = matchDown.substring(0, matchDown.indexOf(";"));
		}
		matchDown = matchDown.replaceAll("\n", "");
		
		
		for( TurtleMove m : turtle.history ) {
			switch(m.type) {
			case TurtleMove.TRAVEL:
				isUp=true;
				x0=m.x;
				y0=m.y;
				break;
			case TurtleMove.DRAW:
				if(isUp) isUp=false;
				else {
					out.write("0\nLINE\n");
					out.write("8\n1\n");  // layer 1
					out.write("10\n"+MathHelper.roundOff3(x0)+"\n");
					out.write("20\n"+MathHelper.roundOff3(y0)+"\n");
					out.write("11\n"+MathHelper.roundOff3(m.x)+"\n");
					out.write("21\n"+MathHelper.roundOff3(m.y)+"\n");
				}
				x0=m.x;
				y0=m.y;
				
				break;
			case TurtleMove.TOOL_CHANGE:
				// TODO write out DXF layer using  m.getColor()
				break;
			}
		}
		// wrap it up
		out.write("0\nENDSEC\n");
		out.write("0\nEOF\n");
		out.flush();
		
		Log.message("done.");
		return true;
	}

}
