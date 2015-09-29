package com.marginallyclever.drawingtools;

import java.awt.BasicStroke;
import java.io.IOException;
import java.io.Writer;
import java.util.prefs.Preferences;

import com.jogamp.opengl.GL2;
import com.marginallyclever.makelangelo.MakelangeloRobot;
import com.marginallyclever.makelangelo.MainGUI;
import com.marginallyclever.makelangelo.MultilingualSupport;


public class DrawingTool {
  // Every tool must have a unique number.
  protected int toolNumber;

  protected float diameter; // mm
  protected float feedRate;
  protected float zOn;
  protected float zOff;
  protected float zRate;
  protected String name;

  // used while drawing to the GUI
  protected float draw_z = 0;

  protected MainGUI mainGUI;
  protected MultilingualSupport translator;
  protected MakelangeloRobot machine;


  public DrawingTool(MainGUI gui, MultilingualSupport ms, MakelangeloRobot mc) {
    mainGUI = gui;
    translator = ms;
    machine = mc;
    diameter = 1;
  }

  public float getZOn() {
    return zOn;
  }

  public float getZOff() {
    return zOff;
  }

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

  public String getName() {
    return name;
  }

  public float getFeedRate() {
    return feedRate;
  }

  public void writeChangeTo(Writer out) throws IOException {
    out.write("M06 T" + toolNumber + ";\n");
  }

  public void writeOn(Writer out) throws IOException {
    out.write("G00 Z" + zOn + " F" + zRate + ";\n");  // lower the pen.
    out.write("G04 P50;\n");
    out.write("G00 F" + getFeedRate() + ";\n");
    drawZ(zOn);
  }

  public void writeOff(Writer out) throws IOException {
    out.write("G00 Z" + zOff + " F" + zRate + ";\n");  // lift the pen.
    out.write("G04 P50;\n");
    out.write("G00 F" + getFeedRate() + ";\n");
    drawZ(zOff);
  }

  public void writeMoveTo(Writer out, float x, float y) throws IOException {
    out.write("G00 X" + x + " Y" + y + ";\n");
  }

  public BasicStroke getStroke() {
    return new BasicStroke(diameter * 10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
  }

  public void drawZ(float z) {
    draw_z = z;
  }

  public boolean isDrawOn() {
    return zOn == draw_z;
  }

  public boolean isDrawOff() {
    return zOff == draw_z;
  }

  public void drawLine(GL2 gl2, double x1, double y1, double x2, double y2) {
    gl2.glBegin(GL2.GL_LINES);
    gl2.glVertex2d(x1, y1);
    gl2.glVertex2d(x2, y2);
    gl2.glEnd();
  }


  public void loadConfig(Preferences prefs) {
    prefs = prefs.node(name);
    setDiameter(Float.parseFloat(prefs.get("diameter", Float.toString(diameter))));
    zRate = Float.parseFloat(prefs.get("z_rate", Float.toString(zRate)));
    zOn = Float.parseFloat(prefs.get("z_on", Float.toString(zOn)));
    zOff = Float.parseFloat(prefs.get("z_off", Float.toString(zOff)));
    //tool_number = Integer.parseInt(prefs.get("tool_number",Integer.toString(tool_number)));
    feedRate = Float.parseFloat(prefs.get("feed_rate", Float.toString(feedRate)));
  }

  public void saveConfig(Preferences prefs) {
    prefs = prefs.node(name);
    prefs.put("diameter", Float.toString(getDiameter()));
    prefs.put("z_rate", Float.toString(zRate));
    prefs.put("z_on", Float.toString(zOn));
    prefs.put("z_off", Float.toString(zOff));
    prefs.put("tool_number", Integer.toString(toolNumber));
    prefs.put("feed_rate", Float.toString(feedRate));
  }
}
