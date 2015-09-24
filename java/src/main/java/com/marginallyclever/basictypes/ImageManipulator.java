package com.marginallyclever.basictypes;


import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.marginallyclever.drawingtools.DrawingTool;
import com.marginallyclever.makelangelo.MainGUI;
import com.marginallyclever.makelangelo.MakelangeloRobot;
import com.marginallyclever.makelangelo.MultilingualSupport;


/**
 * shared methods for image manipulation (generating, converting, or filtering)
 * @author Dan
 */
public abstract class ImageManipulator {
  
  // helpers
  protected float w2, h2, scale;
  
  protected DrawingTool tool;

  protected int colorChannel = 0;

  // text properties
  protected float kerning = 5.0f;
  protected float letterWidth = 10.0f;
  protected float letterHeight = 20.0f;
  protected float lineSpacing = 5.0f;
  protected float padding = 5.0f;
  static final String ALPHABET_FOLDER = "ALPHABET/";
  protected int charsPerLine = 25;
  protected boolean draw_bounding_box = false;

  // text position and alignment
  public enum VAlign {
    TOP, MIDDLE, BOTTOM
  }

  public enum Align {LEFT, CENTER, RIGHT}

  protected VAlign align_vertical = VAlign.MIDDLE;
  protected Align align_horizontal = Align.CENTER;
  protected float posx = 0;
  protected float posy = 0;

  // file properties
  protected String dest;
  // pen position optimizing
  protected boolean lastup;
  protected float previousX, previousY;
  // threading
  protected ProgressMonitor pm;
  protected SwingWorker<Void, Void> parent;

  protected MainGUI mainGUI;
  protected MultilingualSupport translator;
  protected MakelangeloRobot machine;

  protected float sampleValue;
  protected float sampleSum;

  private final Logger log = LoggerFactory.getLogger(ImageManipulator.class);

  public ImageManipulator(MainGUI gui, MakelangeloRobot mc, MultilingualSupport ms) {
    mainGUI = gui;
    translator = ms;
    machine = mc;
  }

  public void setParent(SwingWorker<Void, Void> p) {
    parent = p;
  }

  public void setProgressMonitor(ProgressMonitor p) {
    pm = p;
  }

  
  /**
   * @return the translation key of your derived class.
   */
  public String getName() {
    return "Unnamed";
  }


  protected int decode(int pixel) {
    int r = ((pixel >> 16) & 0xff);
    int g = ((pixel >> 8) & 0xff);
    int b = ((pixel) & 0xff);
    return (r + g + b) / 3;
  }

  protected int decode(Color c) {
    int r = c.getRed();
    int g = c.getGreen();
    int b = c.getBlue();
    return (r + g + b) / 3;
  }


  protected int encode(int i) {
    return (0xff << 24) | (i << 16) | (i << 8) | i;
  }


  protected void liftPen(Writer out) throws IOException {
    tool.writeOff(out);
    lastup = true;
  }


  protected void lowerPen(Writer out) throws IOException {
    tool.writeOn(out);
    lastup = false;
  }

  protected void setAbsoluteMode(Writer out) throws IOException {
    out.write("G90;\n");
  }

  protected void setRelativeMode(Writer out) throws IOException {
    out.write("G91;\n");
  }

  protected void setupTransform() {
    double imageHeight = machine.getPaperHeight()*machine.paperMargin;
    double imageWidth = machine.getPaperWidth()*machine.paperMargin;
    h2 = (float)imageHeight / 2.0f;
    w2 = (float)imageWidth / 2.0f;

    scale = 1;  // 10mm = 1cm

    double newWidth = imageWidth;
    double newHeight = imageHeight;

    if (imageWidth > machine.getPaperWidth()) {
      float resize = (float) machine.getPaperWidth() / (float) imageWidth;
      scale *= resize;
      newHeight *= resize;
      newWidth = machine.getPaperWidth();
    }
    if (newHeight > machine.getPaperHeight()) {
      float resize = (float) machine.getPaperHeight() / (float) newHeight;
      scale *= resize;
      newWidth *= resize;
      newHeight = machine.getPaperHeight();
    }
    newWidth *= machine.paperMargin;
    newHeight *= machine.paperMargin;

    textFindCharsPerLine(newWidth);

    posx = w2;
    posy = h2;
  }

  protected float SX(float x) {
    return x * scale;
  }

  protected float SY(float y) {
    return y * scale;
  }

  protected float PX(float x) {
    return x - w2;
  }

  protected float PY(float y) {
    return h2 - y;
  }

  protected float TX(float x) {
    return SX(PX(x));
  }

  protected float TY(float y) {
    return SY(PY(y));
  }


  protected void moveTo(Writer out, float x, float y, boolean up) throws IOException {
    float x2 = TX(x);
    float y2 = TY(y);

    if (up == lastup) {
      previousX = x2;
      previousY = y2;
    } else {
      tool.writeMoveTo(out, previousX, previousY);
      tool.writeMoveTo(out, x2, y2);
      if (up) liftPen(out);
      else lowerPen(out);
    }
  }

  protected void moveToPaper(Writer out, double x, double y, boolean up) throws IOException {
    tool.writeMoveTo(out, (float) x, (float) y);
    if (up) liftPen(out);
    else lowerPen(out);
  }

  public void textSetPosition(float x, float y) {
    posx = x;
    posy = y;
  }

  public void textSetAlign(Align x) {
    align_horizontal = x;
  }

  public void textSetVAlign(VAlign x) {
    align_vertical = x;
  }


  public void textSetCharsPerLine(int numChars) {
    charsPerLine = numChars;
    //System.out.println("MAX="+numChars);
  }


  public void textFindCharsPerLine(double width) {
    charsPerLine = (int) Math.floor((float) (width * 10.0f - padding * 2.0f) / (float) (letterWidth + kerning));
    //System.out.println("MAX="+chars_per_line);
  }


  // TODO count newlines?
  protected Rectangle2D textCalculateBounds(String text) {
    String[] lines = textWrapToLength(text);
    int len = textLongestLine(lines);

    int num_lines = lines.length;
    float h = padding * 2 + (letterHeight + lineSpacing) * num_lines;//- line_spacing; removed because of letters that hang below the line
    float w = padding * 2 + (letterWidth + kerning) * len - kerning;
    float xmax = 0, xmin = 0, ymax = 0, ymin = 0;

    switch (align_horizontal) {
      case LEFT:
        xmax = posx + w;
        xmin = posx;
        break;
      case CENTER:
        xmax = posx + w / 2;
        xmin = posx - w / 2;
        break;
      case RIGHT:
        xmax = posx;
        xmin = posx - w;
        break;
    }

    switch (align_vertical) {
      case BOTTOM:
        ymax = posy + h;
        ymin = posy;
        break;
      case MIDDLE:
        ymax = posy + h / 2;
        ymin = posy - h / 2;
        break;
      case TOP:
        ymax = posy;
        ymin = posy - h;
        break;
    }
    /*
    System.out.println(num_lines + " lines");
    System.out.println("longest "+len+" chars");
    System.out.println("x "+xmin+" to "+xmax);
    System.out.println("y "+ymin+" to "+ymax);
    */
    Rectangle2D r = new Rectangle2D.Float();
    r.setRect(xmin, ymin, xmax - xmin, ymax - ymin);

    return r;
  }


  protected void textCreateMessageNow(String text, Writer output) throws IOException {
    if (charsPerLine <= 0) return;

    tool = machine.getCurrentTool();

    // find size of text block
    Rectangle2D r = textCalculateBounds(text);

    output.write("G90;\n");
    liftPen(output);

    if (draw_bounding_box) {
      // draw bounding box
      output.write("G0 X" + TX((float) r.getMinX()) + " Y" + TY((float) r.getMaxY()) + ";\n");
      lowerPen(output);
      output.write("G0 X" + TX((float) r.getMaxX()) + " Y" + TY((float) r.getMaxY()) + ";\n");
      output.write("G0 X" + TX((float) r.getMaxX()) + " Y" + TY((float) r.getMinY()) + ";\n");
      output.write("G0 X" + TX((float) r.getMinX()) + " Y" + TY((float) r.getMinY()) + ";\n");
      output.write("G0 X" + TX((float) r.getMinX()) + " Y" + TY((float) r.getMaxY()) + ";\n");
      liftPen(output);
    }

    // move to first line height
    // assumes we are still G90
    float message_start = TX((float) r.getMinX()) + SX(padding);
    float firstline = TY((float) r.getMinY()) - SY(padding + letterHeight);
    float interline = -SY(letterHeight + lineSpacing);

    output.write("G0 X" + message_start + " Y" + firstline + ";\n");
    output.write("G91;\n");

    // draw line of text
    String[] lines = textWrapToLength(text);
    for (int i = 0; i < lines.length; i++) {
      if (i > 0) {
        // newline
        output.write("G0 Y" + interline + ";\n");

        // carriage return
        output.write("G90;\n");
        output.write("G0 X" + message_start + ";\n");
        output.write("G91;\n");
      }

      textDrawLine(lines[i], output);
    }

    output.write("G90;\n");
    liftPen(output);
  }


  // break the text into an array of strings.  each string is one line of text made to fit into the chars_per_line limit.
  protected String[] textWrapToLength(String src) {
    String[] test_lines = src.split("\n");
    int i, j;

    int num_lines = 0;
    for (i = 0; i < test_lines.length; ++i) {
      if (test_lines[i].length() > charsPerLine) {
        int x = (int) Math.ceil((double) test_lines[i].length() / (double) charsPerLine);
        num_lines += x;
      } else {
        num_lines++;
      }
    }

    String[] lines = new String[num_lines];
    j = 0;
    for (i = 0; i < test_lines.length; ++i) {
      if (test_lines[i].length() <= charsPerLine) {
        lines[j++] = test_lines[i];
      } else {
        String[] temp = test_lines[i].split("(?<=\\G.{" + charsPerLine + "})");
        for (String aTemp : temp) {
          lines[j++] = aTemp;
        }
      }
    }

    return lines;
  }

  protected int textLongestLine(String[] lines) {
    int len = 0;
    for (String line : lines) {
      if (len < line.length()) len = line.length();
    }

    return len;
  }

  protected void textDrawLine(String a1, Writer output) throws IOException {
    String ud = ALPHABET_FOLDER;

    log.info("{} ({})", a1, a1.length());

    int i = 0;
    for (i = 0; i < a1.length(); ++i) {
      char letter = a1.charAt(i);

      if (letter == '\n' || letter == '\r') continue;

      String name;

      // find the file that goes with this character
      if ('a' <= letter && letter <= 'z') {
        name = "SMALL_" + Character.toUpperCase(letter);
      } else {
        switch (letter) {
          case ' ':
            name = "SPACE";
            break;
          case '!':
            name = "EXCLAMATION";
            break;
          case '"':
            name = "DOUBLEQ";
            break;
          case '$':
            name = "DOLLAR";
            break;
          case '#':
            name = "POUND";
            break;
          case '%':
            name = "PERCENT";
            break;
          case '&':
            name = "AMPERSAND";
            break;
          case '\'':
            name = "SINGLEQ";
            break;
          case '(':
            name = "B1OPEN";
            break;
          case ')':
            name = "B1CLOSE";
            break;
          case '*':
            name = "ASTERIX";
            break;
          case '+':
            name = "PLUS";
            break;
          case ',':
            name = "COMMA";
            break;
          case '-':
            name = "HYPHEN";
            break;
          case '.':
            name = "PERIOD";
            break;
          case '/':
            name = "FSLASH";
            break;
          case ':':
            name = "COLON";
            break;
          case ';':
            name = "SEMICOLON";
            break;
          case '<':
            name = "GREATERTHAN";
            break;
          case '=':
            name = "EQUAL";
            break;
          case '>':
            name = "LESSTHAN";
            break;
          case '?':
            name = "QUESTION";
            break;
          case '@':
            name = "AT";
            break;
          case '[':
            name = "B2OPEN";
            break;
          case ']':
            name = "B2CLOSE";
            break;
          case '^':
            name = "CARET";
            break;
          case '_':
            name = "UNDERSCORE";
            break;
          case '`':
            name = "GRAVE";
            break;
          case '{':
            name = "B3OPEN";
            break;
          case '|':
            name = "BAR";
            break;
          case '}':
            name = "B3CLOSE";
            break;
          case '~':
            name = "TILDE";
            break;
          case '\\':
            name = "BSLASH";
            break;
          case '…':
            name = "SPACE";
            break;
          default:
            name = Character.toString(letter);
            break;
        }
      }
      String fn = ud + name + ".NGC";
      final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(fn);
      if (inputStream != null) {
        if (i > 0 && kerning != 0) {
          output.write("G0 X" + SX(kerning) + ";\n");
        }
        try (final InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             final BufferedReader in = new BufferedReader(inputStreamReader)) {

          String b;
          while ((b = in.readLine()) != null) {
            if (b.trim().length() == 0)
              continue;
            switch (b) {
              case "UP":
                output.write("G90;\n");
                liftPen(output);
                output.write("G91;\n");
                break;
              case "DOWN":
                output.write("G90;\n");
                lowerPen(output);
                output.write("G91;\n");
                break;
              default:
                StringTokenizer st = new StringTokenizer(b);
                String gap = "";
                while (st.hasMoreTokens()) {
                  String c = st.nextToken();
                  if (c.startsWith("G")) {
                    output.write(gap + c);
                  } else if (c.startsWith("X")) {
                    // translate coordinates
                    final float x = Float.parseFloat(c.substring(1)) * 10; // cm to mm
                    output.write(gap + "X" + SX(x));
                  } else if (c.startsWith("Y")) {
                    // translate coordinates
                    final float y = Float.parseFloat(c.substring(1)) * 10; // cm to mm
                    output.write(gap + "Y" + SY(y));
                  } else {
                    output.write(gap + c);
                  }
                  gap = " ";
                }
                output.write(";\n");
                break;
            }
          }
        }
      } else {
        // file not found
        System.out.println("file not found. Making best guess as to where it is.");
        System.out.print(fn);
        System.out.println(" NOK");
      }
    }
  }

  protected void signName(Writer out) throws IOException {
    float desired_scale = 0.5f;  // changes the size of the font.  large number = larger font

    textSetAlign(Align.RIGHT);
    textSetVAlign(VAlign.BOTTOM);
    textSetPosition((float)(machine.getPaperWidth() *10.0f*machine.paperMargin),
    				(float)(machine.getPaperHeight()*10.0f*machine.paperMargin));

    float xx = w2;
    float yy = h2;
    float old_scale = scale;
    h2 = 0;
    w2 = 0;
    scale = desired_scale;

    textSetCharsPerLine(25);

    textCreateMessageNow("Makelangelo #" + Long.toString(machine.getUID()), out);
    //TextCreateMessageNow("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890<>,?/\"':;[]!@#$%^&*()_+-=\\|~`{}.",out);
    h2 = yy;
    w2 = xx;
    scale = old_scale;
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
