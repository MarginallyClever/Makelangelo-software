package com.marginallyclever.makelangelo;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

/**
 * contains the text for a gcode file.
 * also provides methods for estimating the total length of lines drawn
 * also provides methods for "walking" a file and remembering certain states
 * also provides bounding information?
 * also provides file scaling?
 *
 * @author danroyer
 */
public class GCodeFile {
  public int linesTotal = 0;
  public int linesProcessed = 0;
  public boolean fileOpened = false;
  public ArrayList<String> lines;
  public float estimated_time = 0;
  public float estimated_length = 0;
  public int estimate_count = 0;
  public float scale = 1.0f;
  public float feed_rate = 1.0f;
  public boolean changed = false;


  // returns angle of dy/dx as a value from 0...2PI
  private double atan3(double dy, double dx) {
    double a = Math.atan2(dy, dx);
    if (a < 0) a = (Math.PI * 2.0) + a;
    return a;
  }


  void estimateDrawTime() {
    int j;

    double px = 0, py = 0, pz = 0, length = 0, x, y, z, ai, aj;
    feed_rate = 1.0f;
    scale = 0.1f;
    estimated_time = 0;
    estimated_length = 0;
    estimate_count = 0;

    Iterator<String> iLine = lines.iterator();
    while (iLine.hasNext()) {
      String line = iLine.next();
      String[] pieces = line.split(";");  // comments come after a semicolon.
      if (pieces.length == 0) continue;

      String[] tokens = pieces[0].split("\\s");

      for (j = 0; j < tokens.length; ++j) {
        if (tokens[j].equals("G20")) scale = 2.54f;  // in->cm
        if (tokens[j].equals("G21")) scale = 0.10f;  // mm->cm
        if (tokens[j].startsWith("F")) {
        	try {
          feed_rate = Float.valueOf(tokens[j].substring(1)) * scale;
        	}
        	catch(Exception e) {
        		e.printStackTrace(); 
        	}
          assert (!Float.isNaN(feed_rate) && feed_rate != 0);
        }
      }

      x = px;
      y = py;
      z = pz;
      ai = px;
      aj = py;
      for (j = 1; j < tokens.length; ++j) {
        if (tokens[j].startsWith("X")) x = Float.valueOf(tokens[j].substring(1)) * scale;
        if (tokens[j].startsWith("Y")) y = Float.valueOf(tokens[j].substring(1)) * scale;
        if (tokens[j].startsWith("Z")) z = Float.valueOf(tokens[j].substring(1)) * scale;
        if (tokens[j].startsWith("I")) ai = px + Float.valueOf(tokens[j].substring(1)) * scale;
        if (tokens[j].startsWith("J")) aj = py + Float.valueOf(tokens[j].substring(1)) * scale;
      }

      if (z != pz) {
        // pen up/down action
        estimated_time += (z - pz) / feed_rate;  // seconds?
        assert (!Float.isNaN(estimated_time));
      }

      if (tokens[0].equals("G00") || tokens[0].equals("G0") ||
          tokens[0].equals("G01") || tokens[0].equals("G1")) {
        // draw a line
        double ddx = x - px;
        double ddy = y - py;
        length = Math.sqrt(ddx * ddx + ddy * ddy);
        estimated_time += length / feed_rate;
        assert (!Float.isNaN(estimated_time));
        estimated_length += length;
        ++estimate_count;
        px = x;
        py = y;
        pz = z;
      } else if (tokens[0].equals("G02") || tokens[0].equals("G2") ||
          tokens[0].equals("G03") || tokens[0].equals("G3")) {
        // draw an arc
        int dir = (tokens[0].equals("G02") || tokens[0].equals("G2")) ? -1 : 1;
        double dx = px - ai;
        double dy = py - aj;
        double radius = Math.sqrt(dx * dx + dy * dy);

        // find angle of arc (sweep)
        double angle1 = atan3(dy, dx);
        double angle2 = atan3(y - aj, x - ai);
        double theta = angle2 - angle1;

        if (dir > 0 && theta < 0) angle2 += 2.0 * Math.PI;
        else if (dir < 0 && theta > 0) angle1 += 2.0 * Math.PI;

        theta = Math.abs(angle2 - angle1);
        // length of arc=theta*r (http://math.about.com/od/formulas/ss/surfaceareavol_9.htm)
        length = theta * radius;
        estimated_time += length / feed_rate;
        assert (!Float.isNaN(estimated_time));
        estimated_length += length;
        ++estimate_count;
        px = x;
        py = y;
        pz = z;
      }
    }  // for ( each instruction )
    assert (!Float.isNaN(estimated_time));
    // processing time for each instruction
    estimated_time += estimate_count * 0.007617845117845f;
    // conversion to ms?
    estimated_time *= 10000;
  }


  // close the file, clear the preview tab
  public void closeFile() {
    if (fileOpened == true) {
      fileOpened = false;
    }
  }


  public void load(String filename) throws IOException {
    closeFile();

    Scanner scanner = new Scanner(new FileInputStream(filename));

    linesTotal = 0;
    lines = new ArrayList<String>();
    try {
      while (scanner.hasNextLine()) {
        lines.add(scanner.nextLine());
        ++linesTotal;
      }
    } finally {
      scanner.close();
    }
    fileOpened = true;
    estimateDrawTime();
  }


  public void save(String filename) throws IOException {
    FileOutputStream out = new FileOutputStream(filename);
    String temp;

    for (int i = 0; i < linesTotal; ++i) {
      temp = lines.get(i);
      if (!temp.endsWith(";") && !temp.endsWith(";\n")) {
        temp += ";";
      }
      if (!temp.endsWith("\n")) temp += "\n";
      out.write(temp.getBytes());
    }

    out.flush();
    out.close();
  }
}


/**
 * This file is part of DrawbotGUI.
 * <p>
 * DrawbotGUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * DrawbotGUI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with DrawbotGUI.  If not, see <http://www.gnu.org/licenses/>.
 */
