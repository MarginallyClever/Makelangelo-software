package com.marginallyclever.makelangelo.makeart.io;

import com.marginallyclever.convenience.helpers.MathHelper;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * @author Dan Royer
 * see http://paulbourke.net/dataformats/dxf/min3d.html for details
 */
public class SaveDXF implements TurtleSaver {
	private static final Logger logger = LoggerFactory.getLogger(SaveDXF.class);
	private static final FileNameExtensionFilter filter = new FileNameExtensionFilter("DXF R12", "dxf");
		
	@Override
	public FileNameExtensionFilter getFileNameFilter() {
		return filter;
	}

	@Override
	public boolean save(OutputStream outputStream,Turtle turtle, PlotterSettings settings) throws Exception {
		logger.debug("saving...");

		OutputStreamWriter out = new OutputStreamWriter(outputStream);
		outputHeader(out,turtle.getBounds());

		for( var layer : turtle.getLayers() ) {
            if(!layer.isVisible()) continue;
			// TODO write out color change using layer.getColor()
			for( var line : layer.getAllLines() ) {
				if(line.isEmpty()) continue;
				var iter = line.getAllPoints().iterator();
				var p0 = iter.next();
				while(iter.hasNext()) {
					var p1 = iter.next();
					out.write("0\nLINE\n");
					out.write("8\n1\n");  // layer 1
					out.write("10\n"+MathHelper.roundOff3(p0.x)+"\n");
					out.write("20\n"+MathHelper.roundOff3(p0.y)+"\n");
					out.write("11\n"+MathHelper.roundOff3(p1.x)+"\n");
					out.write("21\n"+MathHelper.roundOff3(p1.y)+"\n");
					p0=p1;
				}
			}
		}
		outputFooter(out);
		logger.debug("done.");
		return true;
	}

	private void outputFooter(OutputStreamWriter out) throws IOException{
		// wrap it up
		out.write("0\nENDSEC\n");
		out.write("0\nEOF\n");
		out.flush();
	}

	private void outputHeader(OutputStreamWriter out,Rectangle2D.Double box) throws IOException {		// header
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
	}

}
