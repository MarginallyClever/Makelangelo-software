package com.marginallyclever.makelangeloRobot.drawingtools;

import java.io.IOException;
import java.io.Writer;

import com.marginallyclever.makelangeloRobot.MakelangeloRobot;


public class DrawingTool_LED extends DrawingTool {
  public DrawingTool_LED(MakelangeloRobot robot) {
    super(robot);

    diameter = 4;
    name = "LED";
    zOn = 180;
    zOff = 0;
    feedRateXY = 5000;
  }

  @Override
  public void writeChangeTo(Writer out) throws IOException {
    out.write("M06 T1;\n");
  }

  @Override
  public void writeOn(Writer out) throws IOException {
    out.write("G00 Z180 F500;\n");  // lower the pen.
    out.write("G00 F" + feedRateXY + ";\n");
  }

  @Override
  public void writeOff(Writer out) throws IOException {
    out.write("G00 Z0 F500;\n");  // lower the pen..
    out.write("G00 F" + feedRateXY + ";\n");
  }

  @Override
  public void writeMoveTo(Writer out, double x, double y) throws IOException {
    out.write("G00 X" + x + " Y" + y + ";\n");
  }
}
