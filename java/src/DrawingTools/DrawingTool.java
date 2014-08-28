package DrawingTools;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.util.prefs.Preferences;


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
	
	
	public float GetZOn() { return z_on; }
	public float GetZOff() { return z_off; }
	
	// Load a configure menu and let people adjust the tool settings
	public void Adjust() {
		//final JDialog driver = new JDialog(DrawbotGUI.getSingleton().getParentFrame(),"Adjust pulley size",true);		
	}
	
	public void SetDiameter(float d) {
		diameter = d;
	}
	
	public float GetDiameter() {
		return diameter;
	}

	public String GetName() { return name; }
	public float GetFeedRate() { return feed_rate; }
	
	public void WriteChangeTo(OutputStreamWriter out) throws IOException {
		out.write("M06 T"+tool_number+";\n");
	}

	public void WriteOn(OutputStreamWriter out) throws IOException {
		out.write("G00 Z"+z_on+" F"+z_rate+";\n");  // lower the pen.
		out.write("G04 P50;\n");
		out.write("G00 F"+GetFeedRate()+";\n");
		DrawZ(z_on);
	}

	public void WriteOff(OutputStreamWriter out) throws IOException {
		out.write("G00 Z"+z_off+" F"+z_rate+";\n");  // lift the pen.
		out.write("G04 P50;\n");
		out.write("G00 F"+GetFeedRate()+";\n");
		DrawZ(z_off);
	}
	
	public void WriteMoveTo(OutputStreamWriter out,float x,float y) throws IOException {
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
	

	public void LoadConfig(Preferences prefs) {
		prefs = prefs.node(name);
		SetDiameter(Float.parseFloat(prefs.get("diameter",Float.toString(diameter))));
		z_rate = Float.parseFloat(prefs.get("z_rate",Float.toString(z_rate)));
		z_on = Float.parseFloat(prefs.get("z_on",Float.toString(z_on)));
		z_off = Float.parseFloat(prefs.get("z_off",Float.toString(z_off)));
		//tool_number = Integer.parseInt(prefs.get("tool_number",Integer.toString(tool_number)));
		feed_rate = Float.parseFloat(prefs.get("feed_rate",Float.toString(feed_rate)));		
	}

	public void SaveConfig(Preferences prefs) {
		prefs = prefs.node(name);
		prefs.put("diameter", Float.toString(GetDiameter()));
		prefs.put("z_rate", Float.toString(z_rate));
		prefs.put("z_on", Float.toString(z_on));
		prefs.put("z_off", Float.toString(z_off));
		prefs.put("tool_number", Integer.toString(tool_number));
		prefs.put("feed_rate", Float.toString(feed_rate));
	}
}
