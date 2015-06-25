package com.marginallyclever.drawingtools;

import com.jogamp.opengl.GL2;
import com.marginallyclever.makelangelo.MachineConfiguration;
import com.marginallyclever.makelangelo.MainGUI;
import com.marginallyclever.makelangelo.MultilingualSupport;

import java.awt.*;
import java.io.IOException;
import java.io.Writer;
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

	protected MainGUI mainGUI;
	protected MultilingualSupport translator;
	protected MachineConfiguration machine;
	

	public DrawingTool(MainGUI gui,MultilingualSupport ms,MachineConfiguration mc) {
		mainGUI = gui;
		translator = ms;
		machine = mc;
	}
	
	public float getZOn() { return z_on; }
	public float getZOff() { return z_off; }
	
	// Load a configure menu and let people adjust the tool settings
	public void adjust() {
		//final JDialog driver = new JDialog(DrawbotGUI.getSingleton().getParentFrame(),"Adjust pulley size",true);		
	}
	
	public void setDiameter(float d) {
		diameter = d;
	}
	
	public float getDiameter() {
		return diameter;
	}

	public String getName() { return name; }
	public float getFeedRate() { return feed_rate; }
	
	public void writeChangeTo(Writer out) throws IOException {
		out.write("M06 T"+tool_number+";\n");
	}

	public void writeOn(Writer out) throws IOException {
		out.write("G00 Z"+z_on+" F"+z_rate+";\n");  // lower the pen.
		out.write("G04 P50;\n");
		out.write("G00 F"+getFeedRate()+";\n");
		drawZ(z_on);
	}

	public void writeOff(Writer out) throws IOException {
		out.write("G00 Z"+z_off+" F"+z_rate+";\n");  // lift the pen.
		out.write("G04 P50;\n");
		out.write("G00 F"+getFeedRate()+";\n");
		drawZ(z_off);
	}
	
	public void writeMoveTo(Writer out,float x,float y) throws IOException {
		out.write("G00 X"+x+" Y"+y+";\n");
	}
	
	public BasicStroke getStroke() {
		return new BasicStroke(diameter*10,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);
	}
	
	public void drawZ(float z) { draw_z=z; }
	public boolean isDrawOn() { return z_on==draw_z; }
	public boolean isDrawOff() { return z_off==draw_z; }

	public void drawLine(GL2 gl2,double x1,double y1,double x2,double y2) {
		gl2.glBegin(GL2.GL_LINES);
		gl2.glVertex2d(x1,y1);
		gl2.glVertex2d(x2, y2);
		gl2.glEnd();
	}
	

	public void loadConfig(Preferences prefs) {
		prefs = prefs.node(name);
		setDiameter(Float.parseFloat(prefs.get("diameter",Float.toString(diameter))));
		z_rate = Float.parseFloat(prefs.get("z_rate",Float.toString(z_rate)));
		z_on = Float.parseFloat(prefs.get("z_on",Float.toString(z_on)));
		z_off = Float.parseFloat(prefs.get("z_off",Float.toString(z_off)));
		//tool_number = Integer.parseInt(prefs.get("tool_number",Integer.toString(tool_number)));
		feed_rate = Float.parseFloat(prefs.get("feed_rate",Float.toString(feed_rate)));		
	}

	public void saveConfig(Preferences prefs) {
		prefs = prefs.node(name);
		prefs.put("diameter", Float.toString(getDiameter()));
		prefs.put("z_rate", Float.toString(z_rate));
		prefs.put("z_on", Float.toString(z_on));
		prefs.put("z_off", Float.toString(z_off));
		prefs.put("tool_number", Integer.toString(tool_number));
		prefs.put("feed_rate", Float.toString(feed_rate));
	}
}
