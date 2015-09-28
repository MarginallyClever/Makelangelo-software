package com.marginallyclever.drawingtools;


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Writer;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

import com.marginallyclever.makelangelo.MakelangeloRobot;
import com.marginallyclever.makelangelo.MainGUI;
import com.marginallyclever.makelangelo.MultilingualSupport;


public class DrawingTool_Spraypaint extends DrawingTool {
  boolean isUp;
  float oldX, oldY;
  float overlap;
  

  public DrawingTool_Spraypaint(MainGUI gui, MultilingualSupport ms, MakelangeloRobot mc) {
    super(gui, ms, mc);

    diameter = 40;
    zRate = 80;
    zOn = 50;
    zOff = 90;
    toolNumber = 2;
    name = "Spray paint";
    feedRate = 3000;
    overlap = 0.3f;

    oldX = 0;
    oldY = 0;
  }

  public void writeOn(Writer out) throws IOException {
    isUp = false;
  }

  public void writeOff(Writer out) throws IOException {
    isUp = true;
  }

  public void writeMoveTo(Writer out, float x, float y) throws IOException {
    if (isUp) {
      out.write("G00 X" + x + " Y" + y + ";\n");
    } else {
      // Make a set of dots in a row, instead of a single continuous line
      //out.write("G00 X"+x+" Y"+y+";\n");
      float dx = x - oldX;
      float dy = y - oldY;
      float len = (float) Math.sqrt(dx * dx + dy * dy);
      float step = diameter * (1 - overlap);
      float r = step / 2;
      float d, px, py;

      for (d = r; d < len - r; d += step) {
        px = oldX + dx * d / len;
        py = oldY + dy * d / len;
        out.write("G00 X" + px + " Y" + py + " F" + feedRate + ";\n");
        super.writeOn(out);
        super.writeOff(out);
      }
      d = len - r;
      px = oldX + dx * d / len;
      py = oldY + dy * d / len;
      out.write("G00 X" + px + " Y" + py + " F" + feedRate + ";\n");
      super.writeOn(out);
      super.writeOff(out);
    }
    oldX = x;
    oldY = y;
  }

  public void adjust() {
    final JDialog driver = new JDialog(mainGUI.getParentFrame(), translator.get("spraypaintToolAdjust"), true);
    driver.setLayout(new GridBagLayout());

    final JTextField spraypaintDiameter = new JTextField(Float.toString(diameter), 5);
    final JTextField spraypaintFeedRate = new JTextField(Float.toString(feedRate), 5);

    final JTextField spraypaintUp = new JTextField(Float.toString(zOff), 5);
    final JTextField spraypaintDown = new JTextField(Float.toString(zOn), 5);
    final JTextField spraypaintZRate = new JTextField(Float.toString(zRate), 5);
    final JButton buttonTestDot = new JButton("Test");
    final JButton buttonSave = new JButton("Save");
    final JButton buttonCancel = new JButton("Cancel");

    GridBagConstraints c = new GridBagConstraints();
    GridBagConstraints d = new GridBagConstraints();

    c.anchor = GridBagConstraints.EAST;
    c.fill = GridBagConstraints.HORIZONTAL;
    d.anchor = GridBagConstraints.WEST;
    d.fill = GridBagConstraints.HORIZONTAL;
    d.weightx = 50;
    int y = 0;

    c.gridx = 0;
    c.gridy = y;
    driver.add(new JLabel(translator.get("spraypaintToolDiameter")), c);
    d.gridx = 1;
    d.gridy = y;
    driver.add(spraypaintDiameter, d);
    ++y;

    c.gridx = 0;
    c.gridy = y;
    driver.add(new JLabel(translator.get("spraypaintToolMaxFeedRate")), c);
    d.gridx = 1;
    d.gridy = y;
    driver.add(spraypaintFeedRate, d);
    ++y;

    c.gridx = 0;
    c.gridy = y;
    driver.add(new JLabel(translator.get("spraypaintToolUp")), c);
    d.gridx = 1;
    d.gridy = y;
    driver.add(spraypaintUp, d);
    ++y;

    c.gridx = 0;
    c.gridy = y;
    driver.add(new JLabel(translator.get("spraypaintToolDown")), c);
    d.gridx = 1;
    d.gridy = y;
    driver.add(spraypaintDown, d);
    ++y;

    c.gridx = 0;
    c.gridy = y;
    driver.add(new JLabel(translator.get("spraypaintToolLiftSpeed")), c);
    d.gridx = 1;
    d.gridy = y;
    driver.add(spraypaintZRate, d);
    ++y;

    c.gridx = 0;
    c.gridy = y;
    driver.add(new JLabel(translator.get("spraypaintToolTest")), c);
    d.gridx = 1;
    d.gridy = y;
    driver.add(buttonTestDot, d);
    ++y;

    c.gridx = 1;
    c.gridy = y;
    driver.add(buttonSave, c);
    c.gridx = 2;
    c.gridy = y;
    driver.add(buttonCancel, c);
    ++y;

    c.gridwidth = 2;
    c.insets = new Insets(0, 5, 5, 5);
    c.anchor = GridBagConstraints.WEST;
    /*
    c.gridheight=4;
    c.gridx=0;  c.gridy=y;
    driver.add(new JTextArea("Adjust the values sent to the servo to\n" +
                 "raise and lower the pen."),c);
    */
    ActionListener driveButtons = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Object subject = e.getSource();

        if (subject == buttonTestDot) {
          mainGUI.sendLineToRobot("G00 Z" + spraypaintUp.getText() + " F" + spraypaintZRate.getText());
          mainGUI.sendLineToRobot("G00 Z" + spraypaintDown.getText() + " F" + spraypaintZRate.getText());
        }
        if (subject == buttonSave) {
          diameter = Float.valueOf(spraypaintDiameter.getText());
          feedRate = Float.valueOf(spraypaintFeedRate.getText());
          zOff = Float.valueOf(spraypaintUp.getText());
          zOn = Float.valueOf(spraypaintDown.getText());
          zRate = Float.valueOf(spraypaintZRate.getText());
          machine.saveConfig();
          driver.dispose();
        }
        if (subject == buttonCancel) {
          driver.dispose();
        }
      }
    };

    buttonTestDot.addActionListener(driveButtons);

    buttonSave.addActionListener(driveButtons);
    buttonCancel.addActionListener(driveButtons);
    driver.getRootPane().setDefaultButton(buttonSave);

    mainGUI.sendLineToRobot("M114");
    driver.pack();
    driver.setVisible(true);
  }
}
