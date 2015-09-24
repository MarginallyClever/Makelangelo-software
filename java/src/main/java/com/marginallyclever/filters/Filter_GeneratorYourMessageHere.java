package com.marginallyclever.filters;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.marginallyclever.makelangelo.MakelangeloRobot;
import com.marginallyclever.makelangelo.MainGUI;
import com.marginallyclever.makelangelo.MultilingualSupport;
//import java.awt.Shape;
//import java.awt.font.FontRenderContext;
//import java.awt.font.TextLayout;
//import java.awt.geom.PathIterator;


public class Filter_GeneratorYourMessageHere extends Filter {
  protected float kerning = -0.50f;
  protected float letter_width = 2.0f;
  protected float letter_height = 2.0f;
  protected float line_spacing = 0.5f;
  protected float margin = 1.0f;
  static final String alphabetFolder = "ALPHABET/";
  protected int chars_per_line = 35;
  protected static String lastMessage = "";

  private final Logger logger = LoggerFactory.getLogger(Filter_GeneratorYourMessageHere.class);

  public Filter_GeneratorYourMessageHere(MainGUI gui,
                                         MakelangeloRobot mc, MultilingualSupport ms) {
    super(gui, mc, ms);
    logFonts();
  }

  private void logFonts() {
    final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    final Font[] fonts = ge.getAllFonts();
    logger.info("Now printing all fonts from java.awt.GraphicsEnvironment#getAllFonts in the form of java.awt.Font#getFontName : java.awt.Font#getFamily");
    for (Font font : fonts) {
      logger.info("{} : {}", font.getFontName(), font.getFamily());
    }
  }

  @Override
  public String getName() {
    return translator.get("YourMsgHereName");
  }

  public void generate(String dest) {
    final JTextArea text = new JTextArea(lastMessage, 6, 60);

    JPanel panel = new JPanel(new GridLayout(0, 1));
    panel.add(new JScrollPane(text));

    int result = JOptionPane.showConfirmDialog(null, panel, getName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (result == JOptionPane.OK_OPTION) {
      lastMessage = text.getText();
      createMessage(lastMessage, dest);

      //renderFont(gl2,"TimesRoman","مرحبا بالعالم",18);
      //renderFont(gl2,"TimesRoman","Makelangelo",36);

      // TODO Move to GUI?
      mainGUI.log("<font color='green'>Completed.</font>\n");
    }
  }

	/*
    void renderFont(GL2 gl2, String font_name,String text,int size) {
      gl2.glPushMatrix();
        gl2.glScalef(0.1f, -0.1f, 1);
        gl2.glLineWidth(3);
        gl2.glPointSize(4);

    Font font = new Font(font_name, Font.PLAIN, size);
    FontRenderContext frc = new FontRenderContext(null,true,true);
    TextLayout textLayout = new TextLayout(text,font,frc);
    Shape s = textLayout.getOutline(null);
      PathIterator pi = s.getPathIterator(null);
      float [] coords = new float[6];
      float [] coords2 = new float[6];
      float [] start = new float[6];
      while(pi.isDone() == false ) {
        int type = pi.currentSegment(coords);
        switch(type) {
        case PathIterator.SEG_CLOSE:
            gl2.glVertex2f(start[0], start[1]);
            gl2.glEnd();
          break;
        case PathIterator.SEG_LINETO:
            gl2.glVertex2f(coords[0], coords[1]);
          coords2[0] = coords[0];
          coords2[1] = coords[1];
          break;
        case PathIterator.SEG_MOVETO:
          // move without drawing
          start[0] = coords2[0] = coords[0];
          start[1] = coords2[1] = coords[1];
            gl2.glBegin(GL2.GL_LINE_STRIP);
            gl2.glVertex2f(start[0], start[1]);
          break;
        case PathIterator.SEG_CUBICTO:
          for(int i=0;i<10;++i) {
            float t = (float)i/10.0f;
          // p = a0 + a1*t + a2 * tt + a3*ttt;
          float tt=t*t;
          float ttt=tt*t;
          float x = coords2[0] + (coords[0]*t) + (coords[2]*tt) + (coords[4]*ttt);
          float y = coords2[1] + (coords[1]*t) + (coords[3]*tt) + (coords[5]*ttt);
          gl2.glVertex2f(x,y);
          }
        gl2.glVertex2f(coords[4],coords[5]);
          coords2[0] = coords[4];
          coords2[1] = coords[5];
          break;
        case PathIterator.SEG_QUADTO:
          for(int i=0;i<10;++i) {
            float t = (float)i/10.0f;
            //(1-t)²*P0 + 2t*(1-t)*P1 + t²*P2
            float u = (1.0f-t);
          float tt=u*u;
          float ttt=2.0f*t*u;
          float tttt=t*t;
          float x = coords2[0]*tt + (coords[0]*ttt) + (coords[2]*tttt);
          float y = coords2[1]*tt + (coords[1]*ttt) + (coords[3]*tttt);
          gl2.glVertex2f(x,y);
          }
        gl2.glVertex2f(coords[2],coords[3]);
          coords2[0] = coords[2];
          coords2[1] = coords[3];
          break;
        }
        pi.next();
      }
      gl2.glPopMatrix();
    }
	 */

  protected void createMessage(String str, String dest) {

    try (final OutputStream fileOutputStream = new FileOutputStream(dest);
         final Writer output = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)) {

      tool = machine.getCurrentTool();
      setupTransform();
      output.write(machine.getConfigLine() + ";\n");
      output.write(machine.getBobbinLine() + ";\n");
      tool.writeChangeTo(output);

      textSetAlign(Align.CENTER);
      textSetVAlign(VAlign.MIDDLE);
      textCreateMessageNow(lastMessage, output);

      textSetAlign(Align.RIGHT);
      textSetVAlign(VAlign.TOP);
      textSetPosition(image_width, image_height);
      textCreateMessageNow("Makelangelo #" + Long.toString(machine.getUID()), output);
    } catch (IOException e) {
      logger.error("{}", e);
    }
  }
}
