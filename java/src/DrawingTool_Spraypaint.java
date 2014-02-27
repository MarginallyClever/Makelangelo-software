import java.io.BufferedWriter;
import java.io.IOException;


public class DrawingTool_Spraypaint extends DrawingTool {
	boolean is_up;
	float old_x,old_y;
	
	DrawingTool_Spraypaint() {
		diameter=10;
		z_rate=80;
		z_on=50;
		z_off=90;
		tool_number=2;
		name="Spray paint";
		feed_rate=3000;
		
		old_x=0;
		old_y=0;
	}

	public void WriteOn(BufferedWriter out) throws IOException {
		is_up=false;
	}

	public void WriteOff(BufferedWriter out) throws IOException {
		is_up=true;
	}
		
	public void WriteMoveTo(BufferedWriter out,float x,float y) throws IOException {
		if(is_up) {
			out.write("G00 X"+x+" Y"+y+";\n");			
		} else {
			// TODO make this into a set of dots
			//out.write("G00 X"+x+" Y"+y+";\n");
			float dx=x-old_x;
			float dy=y-old_y;
			float len=(float)Math.sqrt(dx*dx+dy*dy);
			
			for(float d=0;d<len;d+=diameter) {
				super.WriteOn(out);
				super.WriteOff(out);
				float px = old_x + dx * d/len;
				float py = old_y + dy * d/len;
				out.write("G00 X"+px+" Y"+py+";\n");				
			}
		}
		old_x=x;
		old_y=y;
	}
}
