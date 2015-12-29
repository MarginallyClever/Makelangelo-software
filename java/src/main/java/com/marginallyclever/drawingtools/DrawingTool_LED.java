package com.marginallyclever.drawingtools;

import java.io.IOException;
import java.io.Writer;

import com.marginallyclever.makelangelo.MakelangeloRobot;
import com.marginallyclever.makelangelo.Translator;


public class DrawingTool_LED extends DrawingTool {
  public DrawingTool_LED(Translator ms, MakelangeloRobot robot) {
    super(ms, robot);

    diameter = 4;
    name = "LED";
    zOn = 180;
    zOff = 0;
    feedRate = 5000;
  }

  public void writeChangeTo(Writer out) throws IOException {
    out.write("M06 T1;\n");
  }

  public void writeOn(Writer out) throws IOException {
    out.write("G00 Z180 F500;\n");  // lower the pen.
    out.write("G00 F" + feedRate + ";\n");
  }

  public void writeOff(Writer out) throws IOException {
    out.write("G00 Z0 F500;\n");  // lower the pen..
    out.write("G00 F" + feedRate + ";\n");
  }

  public void writeMoveTo(Writer out, float x, float y) throws IOException {
    out.write("G00 X" + x + " Y" + y + ";\n");
  }
}
