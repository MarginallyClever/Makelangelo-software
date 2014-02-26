import java.io.BufferedWriter;
import java.io.IOException;


public class DrawingTool_Spraypaint extends DrawingTool {
	boolean is_up;
	
	DrawingTool_Spraypaint() {
		diameter=10;
		z_rate=80;
		z_on=50;
		z_off=90;
		tool_number=2;
		name="Spray paint";
	}

	public void WriteOn(BufferedWriter out) throws IOException {
		is_up=true;
		super.WriteOn(out);
	}

	public void WriteOff(BufferedWriter out) throws IOException {
		is_up=false;
		super.WriteOff(out);
	}
		
	public void WriteMoveTo(BufferedWriter out,float x,float y) throws IOException {
		if(is_up) {
			out.write("G00 X"+x+" Y"+y+";\n");			
		} else {
			// TODO make this into a set of dots
			out.write("G00 X"+x+" Y"+y+";\n");			
		}
	}
}
