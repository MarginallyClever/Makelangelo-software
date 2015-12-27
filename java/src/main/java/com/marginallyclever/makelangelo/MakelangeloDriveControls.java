package com.marginallyclever.makelangelo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

/**
 * The GUI for the live driving controls, the start/pause/stop buttons, and the "send gcode" dialog.
 *
 * @author danroyer
 * @since 7.1.4
 */
public class MakelangeloDriveControls
  extends JScrollPane
  implements ActionListener, MouseListener, MouseMotionListener {
  protected JButton down100,down10,down1,up1,up10,up100;
  protected JButton left100,left10,left1,right1,right10,right100;
  protected JButton goHome,setHome;
  protected JButton goTop,goBottom,goLeft,goRight,penUp,penDown;

  JFormattedTextField feedRate;
  JButton setFeedRate;

  private JButton disengageMotors;
  private JPanel dragAndDrive;
  private JLabel coordinates;
  
  protected Translator translator;
  protected MakelangeloRobotSettings machineConfiguration;
  protected Makelangelo gui;

  private boolean mouseInside,mouseOn;
  double last_x,last_y;

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public MakelangeloDriveControls() {
	  mouseInside=false;
	  mouseOn=false;
	  last_x=last_y=0;
  }

  public void updateButtonAccess(boolean isConnected,boolean isRunning) {
	  
  }
  
  public JButton tightJButton(String label) {
	  JButton b = new JButton(label);
	  b.setMargin(new Insets(0,0,0,0));
	  b.setPreferredSize(new Dimension(60,20));
	  return b;
  }

  public void createPanel(Makelangelo _gui, Translator _translator, MakelangeloRobotSettings _machineConfiguration) {
    translator = _translator;
    gui = _gui;
    machineConfiguration = _machineConfiguration;

    this.setBorder(BorderFactory.createEmptyBorder());
    
    GridBagConstraints c = new GridBagConstraints();

    JPanel axisControl = new JPanel(new GridBagLayout());
//    int w=60, h=20;
//      final JLabel yAxis = new JLabel("Y");     yAxis.setPreferredSize(new Dimension(w,h));     yAxis.setHorizontalAlignment(SwingConstants.CENTER);
      down100 = tightJButton("-100");
      down10 = tightJButton("-10");
      down1 = tightJButton("-1");
      up1 = tightJButton("1");
      up10 = tightJButton("10");
      up100 = tightJButton("100");
      
//      final JLabel xAxis = new JLabel("X");     xAxis.setPreferredSize(new Dimension(w,h));   xAxis.setHorizontalAlignment(SwingConstants.CENTER);
      left100 = tightJButton("-100");
      left10 = tightJButton("-10");
      left1 = tightJButton("-1");
      right1 = tightJButton("1");
      right10 = tightJButton("10");
      right100 = tightJButton("100");

      c.fill=GridBagConstraints.BOTH; 
      //c.gridx=4;  c.gridy=0;  axisControl.add(yAxis,c);
      c.gridx=4;  c.gridy=7;  axisControl.add(down100,c);
      c.gridx=4;  c.gridy=6;  axisControl.add(down10,c);
      c.gridx=4;  c.gridy=5;  axisControl.add(down1,c);
      c.gridx=4;  c.gridy=3;  axisControl.add(up1,c);
      c.gridx=4;  c.gridy=2;  axisControl.add(up10,c);
      c.gridx=4;  c.gridy=1;  axisControl.add(up100,c);
      
      //c.gridx=0;  c.gridy=4;  axisControl.add(xAxis,c);
      c.gridx=1;  c.gridy=4;  axisControl.add(left100,c);
      c.gridx=2;  c.gridy=4;  axisControl.add(left10,c);
      c.gridx=3;  c.gridy=4;  axisControl.add(left1,c);
      c.gridx=5;  c.gridy=4;  axisControl.add(right1,c);
      c.gridx=6;  c.gridy=4;  axisControl.add(right10,c);
      c.gridx=7;  c.gridy=4;  axisControl.add(right100,c);
      up1.addActionListener(this);
      up10.addActionListener(this);
      up100.addActionListener(this);
      down1.addActionListener(this);
      down10.addActionListener(this);
      down100.addActionListener(this);
      left1.addActionListener(this);
      left10.addActionListener(this);
      left100.addActionListener(this);
      right1.addActionListener(this);
      right10.addActionListener(this);
      right100.addActionListener(this);
    
    
    JPanel corners = new JPanel();
      corners.setLayout(new GridBagLayout());
      goTop = new JButton(translator.get("Top"));       goTop.setPreferredSize(new Dimension(80,20));
      goBottom = new JButton(translator.get("Bottom")); goBottom.setPreferredSize(new Dimension(80,20));
      goLeft = new JButton(translator.get("Left"));     goLeft.setPreferredSize(new Dimension(80,20));
      goRight = new JButton(translator.get("Right"));   goRight.setPreferredSize(new Dimension(80,20));
      penUp = new JButton(translator.get("PenUp"));      penUp.setPreferredSize(new Dimension(100,20));
      penDown = new JButton(translator.get("PenDown"));  penDown.setPreferredSize(new Dimension(100,20));
      //final JButton find = new JButton("FIND HOME");    find.setPreferredSize(new Dimension(100,20));
      setHome = new JButton(translator.get("SetHome"));     setHome.setPreferredSize(new Dimension(100,20)); 
      goHome = new JButton(translator.get("GoHome"));     goHome.setPreferredSize(new Dimension(100,20)); 
      c.gridx=2;  c.gridy=0;  corners.add(goTop,c);
      c.gridx=2;  c.gridy=2;  corners.add(goBottom,c);
      c.gridx=1;  c.gridy=1;  corners.add(goLeft,c);
      c.gridx=3;  c.gridy=1;  corners.add(goRight,c);
      c.gridx=5;  c.gridy=0;  corners.add(penUp,c);
      c.gridx=5;  c.gridy=2;  corners.add(penDown,c);
      
      c.gridx=0;  c.gridy=0;  corners.add(setHome,c);
      c.gridx=0;  c.gridy=2;  corners.add(goHome,c);
      c.insets = new Insets(0,0,0,0);
      goTop.addActionListener(this);
      goBottom.addActionListener(this);
      goLeft.addActionListener(this);
      goRight.addActionListener(this);
      penUp.addActionListener(this); 
      penDown.addActionListener(this);
      setHome.addActionListener(this);
      goHome.addActionListener(this);
    
      
    JPanel feedRateControl = new JPanel();
    feedRateControl.setLayout(new GridBagLayout());
      c = new GridBagConstraints();
      feedRate = new JFormattedTextField(NumberFormat.getInstance());  feedRate.setPreferredSize(new Dimension(100,20));
      feedRate.setText(Double.toString(machineConfiguration.getFeedRate()));
      setFeedRate = new JButton(translator.get("Set"));
      setFeedRate.addActionListener(this);
      disengageMotors = new JButton(translator.get("DisengageMotors"));
      disengageMotors.addActionListener(this);
  
      c.gridx=3;  c.gridy=0;  feedRateControl.add(new JLabel(translator.get("Speed")),c);
      c.gridx=4;  c.gridy=0;  feedRateControl.add(feedRate,c);
      c.gridx=5;  c.gridy=0;  feedRateControl.add(new JLabel(translator.get("Rate")),c);
      c.gridx=6;  c.gridy=0;  feedRateControl.add(setFeedRate,c);
      c.gridx=7;  c.gridy=0;  feedRateControl.add(disengageMotors,c);
    
    dragAndDrive = new JPanel(new GridBagLayout());
	    dragAndDrive.setBorder(BorderFactory.createLineBorder(Color.BLACK));
	    dragAndDrive.addMouseListener(this);
	    dragAndDrive.addMouseMotionListener(this);

	    coordinates = new JLabel(translator.get("ClickAndDrag"));
	    c.anchor = GridBagConstraints.CENTER;
	
	    // TODO dimensioning doesn't work right.  The better way would be a pen tool to drag on the 3d view.  That's a lot of work.
	    Dimension dims = new Dimension();
	    dims.setSize( 150,
	    				150 * (double)machineConfiguration.getPaperWidth()/(double)machineConfiguration.getPaperHeight());
	    dragAndDrive.setPreferredSize(dims);
	    dragAndDrive.add(coordinates,c);

    // Now put all the parts together
    JPanel p = new JPanel(new GridBagLayout());
    this.setViewportView(p);
    GridBagConstraints con1 = new GridBagConstraints();
	con1.gridx=0;
	con1.gridy=0;
	con1.weightx=1;
	con1.weighty=0;
	con1.fill=GridBagConstraints.HORIZONTAL;
	con1.anchor=GridBagConstraints.NORTHWEST;

    
    p.add(axisControl,con1);
    con1.gridy++;
    p.add(new JSeparator(),con1);
    con1.gridy++;
    p.add(corners,con1);
    con1.gridy++;
    p.add(new JSeparator(),con1);
    con1.gridy++;
    //con1.weighty=1;
    //p.add(dragAndDrive,con1);
    //con1.weighty=0;
    //con1.gridy++;
    p.add(new JSeparator(),con1);
    con1.gridy++;
    p.add(feedRateControl,con1);
    con1.gridy++;
    p.add(new JSeparator(),con1);
    con1.gridy++;

    con1.weighty=1;
    p.add(new JLabel(),con1);
  }
  
  public void mouseClicked(MouseEvent e) {}
  public void mouseDragged(MouseEvent e) {
	  mouseAction(e);
  }
  public void mouseEntered(MouseEvent e) {
	  mouseInside=true;
  }
  public void mouseExited(MouseEvent e) {
	  mouseInside=false;
	  mouseOn=false;
  }
  public void mouseMoved(MouseEvent e) {
	  mouseAction(e);
  }
  public void mousePressed(MouseEvent e) {
	  mouseOn=true;
	  mouseAction(e);
  }
  public void mouseReleased(MouseEvent e) {
	  mouseOn=false;
  }
  public void mouseWheelMoved(MouseEvent e) {}
  
  public void mouseAction(MouseEvent e) {
	  if(mouseInside && mouseOn) {
		  double x = (double)e.getX();
		  double y = (double)e.getY();
		  Dimension d = dragAndDrive.getSize();
		  double w = d.getWidth();
		  double h = d.getHeight();
		  double cx = w/2.0;
		  double cy = h/2.0;
		  x = x - cx;
		  y = cy - y;
		  x *= 10 * ((machineConfiguration.paperRight-machineConfiguration.paperLeft)/2.0) / (w/2.0);
		  y *= 10 * ((machineConfiguration.paperTop-machineConfiguration.paperBottom)/2.0) / (h/2.0);
		  double dx = x-last_x;
		  double dy = y-last_y;
		  if(Math.sqrt(dx*dx+dy*dy)>=1) {
			  last_x=x;
			  last_y=y;
			  String text = "X"+(Math.round(x*100)/100.0)+" Y"+(Math.round(y*100)/100.0);
			  gui.sendLineToRobot("G00 "+text);
			  coordinates.setText(text);
		  } else {
			  coordinates.setText("");
		  }
	  }
  }


  public void actionPerformed(ActionEvent e) {
    Object subject = e.getSource();
    JButton b = (JButton) subject;

    //if(gui.isRunning()) return;

    if (b == goHome) gui.sendLineToRobot("G00 F" + feedRate.getText() + " X0 Y0");
    else if (b == setHome) gui.sendLineToRobot("G92 X0 Y0");
    else if (b == goLeft)
      gui.sendLineToRobot("G00 F" + feedRate.getText() + " X" + (machineConfiguration.paperLeft * 10));
    else if (b == goRight)
      gui.sendLineToRobot("G00 F" + feedRate.getText() + " X" + (machineConfiguration.paperRight * 10));
    else if (b == goTop)
      gui.sendLineToRobot("G00 F" + feedRate.getText() + " Y" + (machineConfiguration.paperTop * 10));
    else if (b == goBottom)
      gui.sendLineToRobot("G00 F" + feedRate.getText() + " Y" + (machineConfiguration.paperBottom * 10));
      //} else if(b==find) {
      //  gui.SendLineToRobot("G28");
    else if (b == penUp) gui.raisePen();
    else if (b == penDown) gui.lowerPen();
    else if (b == setFeedRate) {
      String fr = feedRate.getText();
      fr = fr.replaceAll("[ ,]", "");
      double feed_rate = Double.parseDouble(fr);
      if (feed_rate < 0.001) feed_rate = 0.001;
      machineConfiguration.setFeedRate(feed_rate);
      feedRate.setText(Double.toString(feed_rate));
      gui.sendLineToRobot("G00 F" + feed_rate);
    } else if (b == disengageMotors) {
    	gui.sendLineToRobot("M18");
    } else {
      gui.sendLineToRobot("G91");  // set relative mode

      if (b == down100) gui.sendLineToRobot("G0 Y-100");
      if (b == down10) gui.sendLineToRobot("G0 Y-10");
      if (b == down1) gui.sendLineToRobot("G0 Y-1");
      if (b == up100) gui.sendLineToRobot("G0 Y100");
      if (b == up10) gui.sendLineToRobot("G0 Y10");
      if (b == up1) gui.sendLineToRobot("G0 Y1");

      if (b == left100) gui.sendLineToRobot("G0 X-100");
      if (b == left10) gui.sendLineToRobot("G0 X-10");
      if (b == left1) gui.sendLineToRobot("G0 X-1");
      if (b == right100) gui.sendLineToRobot("G0 X100");
      if (b == right10) gui.sendLineToRobot("G0 X10");
      if (b == right1) gui.sendLineToRobot("G0 X1");

      gui.sendLineToRobot("G90");  // return to absolute mode
    }
  }
}
