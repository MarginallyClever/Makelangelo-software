package com.marginallyclever.makelangelo.makeArt.io.vector;

import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleMove;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.geom.Rectangle2D;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * @author Dan Royer
 *
 */
public class SaveDXF implements TurtleSaver {
	private static final Logger logger = LoggerFactory.getLogger(SaveDXF.class);
	private static FileNameExtensionFilter filter = new FileNameExtensionFilter("DXF R12", "dxf");
		
	@Override
	public FileNameExtensionFilter getFileNameFilter() {
		return filter;
	}

	@Override
	public boolean save(OutputStream outputStream,Turtle turtle) throws Exception {
		logger.debug("saving...");
		
		Rectangle2D.Double box = turtle.getBounds();
		OutputStreamWriter out = new OutputStreamWriter(outputStream);
		// header
		out.write("999\nDXF created by Makelangelo software (http://makelangelo.com)\n");
		out.write("0\nSECTION\n");
		out.write("2\nHEADER\n");
		out.write("9\n$ACADVER\n1\nAC1006\n");
		out.write("9\n$INSBASE\n");
		out.write("10\n"+box.x+"\n");
		out.write("20\n"+box.y+"\n");
		out.write("30\n0.0\n");
		out.write("9\n$EXTMIN\n");
		out.write("10\n"+box.x+"\n");
		out.write("20\n"+box.y+"\n");
		out.write("30\n0.0\n");
		out.write("9\n$EXTMAX\n");
		out.write("10\n"+(box.x+box.width)+"\n");
		out.write("20\n"+(box.y+box.height)+"\n");
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
		double x0 = turtle.history.get(0).x;
		double y0 = turtle.history.get(0).y;
				
		for( TurtleMove m : turtle.history ) {
			switch(m.type) {
			case TurtleMove.TRAVEL:
				isUp=true;
				x0=m.x;
				y0=m.y;
				break;
			case TurtleMove.DRAW_LINE:
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
		
		logger.debug("done.");
		return true;
	}

}
