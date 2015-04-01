package DrawingTools;

import java.io.BufferedWriter;
import java.io.IOException;


public class DrawingTool_LED extends DrawingTool {
	public DrawingTool_LED() {
		diameter=4;
		name="LED";
		z_on=180;
		z_off=0;
		feed_rate=5000;
	}
	
	public void WriteChangeTo(BufferedWriter out) throws IOException {
		out.write("M06 T1;\n");
	}

	public void WriteOn(BufferedWriter out) throws IOException {
		out.write("G00 Z180 F500;\n");  // lower the pen.
		out.write("G00 F"+feed_rate+";\n");
	}

	public void WriteOff(BufferedWriter out) throws IOException {
		out.write("G00 Z0 F500;\n");  // lower the pen..
		out.write("G00 F"+feed_rate+";\n");
	}
	
	public void WriteMoveTo(BufferedWriter out,float x,float y) throws IOException {
		out.write("G00 X"+x+" Y"+y+";\n");
	}
}
