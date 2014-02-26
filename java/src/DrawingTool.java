import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.io.BufferedWriter;
import java.io.IOException;


public class DrawingTool {
	// every tool must have a unique number.
	protected int tool_number;
	
	protected float diameter=1; // mm
	protected float feed_rate;
	protected float z_on;
	protected float z_off;
	protected float z_rate;
	protected String name;

	// used while drawing to the GUI
	protected float draw_z=0;
	
	
	// Load a configure menu and let people adjust the tool settings
	public void Adjust() {
		//final JDialog driver = new JDialog(DrawbotGUI.getSingleton().getParentFrame(),"Adjust pulley size",true);		
	}
	
	public float GetDiameter() {
		return diameter;
	}

	public String GetName() { return name; }
	
	public void WriteChangeTo(BufferedWriter out) throws IOException {
		out.write("M06 T"+tool_number+";\n");
	}

	public void WriteOn(BufferedWriter out) throws IOException {
		out.write("G00 Z"+z_on+" F"+z_rate+";\n");  // lower the pen.
		out.write("G00 F"+feed_rate+";\n");
	}

	public void WriteOff(BufferedWriter out) throws IOException {
		out.write("G00 Z"+z_off+" F"+z_rate+";\n");  // lift the pen.
		out.write("G00 F"+feed_rate+";\n");
	}
	
	public void WriteMoveTo(BufferedWriter out,float x,float y) throws IOException {
		out.write("G00 X"+x+" Y"+y+";\n");
	}
	
	public BasicStroke getStroke() {
		return new BasicStroke(diameter*10,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);
	}
	
	public void DrawZ(float z) { draw_z=z; }
	public boolean DrawIsOn() { return z_on==draw_z; }
	public boolean DrawIsOff() { return z_off==draw_z; }

	public void DrawLine(Graphics2D g2d,double x1,double y1,double x2,double y2) {
		g2d.drawLine((int)x1,(int)y1,(int)x2,(int)y2);
	}
	
	public void LoadConfig() {}
	public void SaveConfig() {}
}
