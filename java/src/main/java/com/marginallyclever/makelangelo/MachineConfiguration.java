package com.marginallyclever.makelangelo;

import com.marginallyclever.drawingtools.DrawingTool;
import com.marginallyclever.drawingtools.DrawingTool_LED;
import com.marginallyclever.drawingtools.DrawingTool_Pen;
import com.marginallyclever.drawingtools.DrawingTool_Spraypaint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

import com.marginallyclever.drawingtools.DrawingTool;
import com.marginallyclever.drawingtools.DrawingTool_LED;
import com.marginallyclever.drawingtools.DrawingTool_Pen;
import com.marginallyclever.drawingtools.DrawingTool_Spraypaint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dan royer
 */
public final class MachineConfiguration {
  /**
   *
   */
  private final Preferences topLevelMachinesPreferenceNode = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);

  /**
   * Each robot has a global unique identifier
   */
  private long robot_uid = 0;

  protected final static double INCH_TO_CM = 2.54;

  // machine physical limits
  public double limit_top = 18 * INCH_TO_CM;
  public double limit_bottom = -18 * INCH_TO_CM;
  public double limit_left = -18 * INCH_TO_CM;
  public double limit_right = 18 * INCH_TO_CM;

  // paper area
  public double paper_top = 12 * INCH_TO_CM;
  public double paper_bottom = -12 * INCH_TO_CM;
  public double paper_left = -9 * INCH_TO_CM;
  public double paper_right = 9 * INCH_TO_CM;
  public double paperMargin = 0.9;

  // pulleys turning backwards?
  public boolean m1invert = false;
  public boolean m2invert = false;

  // pulley diameter
  private double bobbin_left_diameter=1.5;
  private double bobbin_right_diameter=1.5;

  private double max_feed_rate=11000;  // etch-a-sketch speed

  private String commonPaperSizes [] = { "",
		"4A0 (1682 x 2378)",
		"2A0 (1189 x 1682)",
		"A0 (841 x 1189)",
		"A1 (594 x 841)",
		"A2 (420 x 594)",
		"A3 (297 x 420)",
		"A4 (210 x 297)",
		"A5 (148 x 210)",
		"A6 (105 x 148)",
		"A7 (74 x 105)",};
  
  public boolean reverseForGlass=false;
  public boolean motors_backwards=false;

    /**
     * top left, bottom center, etc...
     *
     * <pre>
     * {@code private String[] startingStrings =  {
     *       "Top Left",
     *       "Top Center",
     *       "Top Right",
     *       "Left",
     *       "Center",
     *       "Right",
     *       "Bottom Left",
     *       "Bottom Center",
     *       "Bottom Right"
     *   };}
     * </pre>
=======
  private double bobbin_left_diameter = 1.5;
  private double bobbin_right_diameter = 1.5;

  private double max_feed_rate = 11000;  // etch-a-sketch speed

  public boolean reverseForGlass = false;
  public boolean motors_backwards = false;

  /**
   * top left, bottom center, etc...
   * <p>
   * <pre>
   * {@code private String[] startingStrings =  {
   *       "Top Left",
   *       "Top Center",
   *       "Top Right",
   *       "Left",
   *       "Center",
   *       "Right",
   *       "Bottom Left",
   *       "Bottom Center",
   *       "Bottom Right"
   *   };}
   * </pre>
>>>>>>> origin/dev
   */
  private int startingPositionIndex = 4;

  // TODO a way for users to create different tools for each machine
  private List<DrawingTool> tools;

  private int current_tool = 0;

  private String[] machineConfigurationsAvailable = null;

  private MainGUI mainGUI = null;

  private MultilingualSupport translator;

  private final Logger logger = LoggerFactory.getLogger(MachineConfiguration.class);

  /**
   * TODO move tool names into translations & add a color palette system for quantizing colors
   *
   * @param gui
   * @param ms
   */
  protected MachineConfiguration(MainGUI gui, MultilingualSupport ms) {
    mainGUI = gui;
    translator = ms;

    commonPaperSizes[0] = ms.get("Other");
    
    tools = new ArrayList<>();
    tools.add(new DrawingTool_Pen("Pen (black)", 0, gui, ms, this));
    tools.add(new DrawingTool_Pen("Pen (red)", 1, gui, ms, this));
    tools.add(new DrawingTool_Pen("Pen (green)", 2, gui, ms, this));
    tools.add(new DrawingTool_Pen("Pen (blue)", 3, gui, ms, this));
    tools.add(new DrawingTool_LED(gui, ms, this));
    tools.add(new DrawingTool_Spraypaint(gui, ms, this));

    // which configurations are available?
    try {
      machineConfigurationsAvailable = topLevelMachinesPreferenceNode.childrenNames();
    } catch (Exception e) {
      logger.error("{}", e);
      machineConfigurationsAvailable = new String[1];
    }
    // TODO load most recent config?
    loadConfig(0);
  }
  
  /**
   * Must match commonPaperSizes
   * @return
   */
  private int getCurrentPaperSizeChoice(double pw,double ph) {
	    if( pw == 1682 && ph == 2378 ) return 1;
	    if( pw == 1189 && ph == 1682 ) return 2;
	    if( pw == 841 && ph == 1189 ) return 3;
	    if( pw == 594 && ph == 841 ) return 4;
	    if( pw == 420 && ph == 594 ) return 5;
	    if( pw == 297 && ph == 420 ) return 6;
	    if( pw == 210 && ph == 297 ) return 7;
	    if( pw == 148 && ph == 210 ) return 8;
	    if( pw == 105 && ph == 148 ) return 9;
	    if( pw == 74 && ph == 105 ) return 10;
	    
	    return 0;
  }

  /**
   * Open the config dialog, send the config update to the robot, save it for future, and refresh the preview tab.
   */
  public void adjustMachineSize() {
    final JDialog driver = new JDialog(mainGUI.getParentFrame(), translator.get("MenuSettingsMachine"), true);
    JPanel container = new JPanel();

    container.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
    driver.add(container);
    //container.setLayout(new GridLayout(0,1,8,8));
    container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));


    GridBagConstraints c = new GridBagConstraints();
    GridBagConstraints d = new GridBagConstraints();

    int y = 0;
    
    final JButton cancel = new JButton(translator.get("Cancel"));
    final JButton save = new JButton(translator.get("Save"));
/*
    JLabel picLabel = null;
    BufferedImage myPicture = null;
    final String limit_file = "limits.png";
    try (final InputStream s = getClass().getClassLoader().getResourceAsStream(limit_file)) {
      myPicture = ImageIO.read(s);
    }
    catch(IOException e) {
      logger.error("{}", e);
      myPicture=null;
    }
    
    if (myPicture != null) {
      picLabel = new JLabel(new ImageIcon(myPicture));
    } else {
      logger.error("{}", translator.get("CouldNotFind")+limit_file);
    }*/

/*
    if (myPicture != null) {
      c.weightx = 0.25;
      c.gridx = 0;
      c.gridy = y;
      c.gridwidth = 4;
      c.gridheight = 4;
      c.anchor = GridBagConstraints.CENTER;
      container.add(picLabel, c);
      y += 5;
    }
*/    
    JPanel p = new JPanel(new GridBagLayout());
    container.add(p);
    
    c.gridwidth=3;
    p.add(new JLabel("1\" = 25.4mm",SwingConstants.CENTER),c);
    c.gridwidth=1;
    
    y=1;

    c.anchor=GridBagConstraints.EAST;
    d.anchor=GridBagConstraints.WEST;

    final JTextField mw = new JTextField(String.valueOf((limit_right-limit_left)*10));
    final JTextField mh = new JTextField(String.valueOf((limit_top-limit_bottom)*10));
    c.gridx=0;  c.gridy=y;  p.add(new JLabel(translator.get("MachineWidth")),c);
    d.gridx=1;  d.gridy=y;  p.add(mw,d);
    d.gridx=2;  d.gridy=y;  p.add(new JLabel("mm"),d);
    y++;
    c.gridx=0;  c.gridy=y;  p.add(new JLabel(translator.get("MachineHeight")),c);
    d.gridx=1;  d.gridy=y;  p.add(mh,d);
    d.gridx=2;  d.gridy=y;  p.add(new JLabel("mm"),d);
    y++;

    container.add(new JSeparator(SwingConstants.HORIZONTAL));
    p = new JPanel(new GridBagLayout());
    container.add(p);
    y=0;
    final JComboBox<String> paperSizes = new JComboBox<>(commonPaperSizes);
    paperSizes.setSelectedIndex(getCurrentPaperSizeChoice( (paper_right-paper_left)*10, (paper_top-paper_bottom)*10) );
    
    final JTextField pw = new JTextField(Integer.toString((int)((paper_right-paper_left)*10)));
    final JTextField ph = new JTextField(Integer.toString((int)((paper_top-paper_bottom)*10)));
    
    c.gridx=0;  c.gridy=y;  p.add(new JLabel(translator.get("PaperSize")),c);
    d.gridx=1;  d.gridy=y;  d.gridwidth=2;  p.add(paperSizes,d);
    y=1;
    d.gridwidth=1;

    c.gridx=0;  c.gridy=y;  p.add(Box.createGlue(),c);
    d.gridx=1;  d.gridy=y;  p.add(pw,d); 
    d.gridx=2;  d.gridy=y;  p.add(new JLabel(translator.get("Millimeters")),d);
    y++;
    c.gridx=0;  c.gridy=y;  p.add(new JLabel(" x "),c);
    d.gridx=1;  d.gridy=y;  p.add(ph,d);
    d.gridx=2;  d.gridy=y;  p.add(new JLabel(translator.get("Millimeters")),d);
    y++;
    
    //c.gridx=0; c.gridy=9; c.gridwidth=4; c.gridheight=1;
    //container.add(new JLabel("For more info see http://bit.ly/fix-this-link."),c);
    //c.gridx=0; c.gridy=11; c.gridwidth=2; c.gridheight=1;  container.add(new JLabel("Pen starts at paper"),c);
    //c.anchor=GridBagConstraints.WEST;
    //c.gridx=2; c.gridy=11; c.gridwidth=2; c.gridheight=1;  container.add(startPos,c);

    //final JComboBox<String> startPos = new JComboBox<String>(startingStrings);
    //startPos.setSelectedIndex(startingPositionIndex);

    
    container.add(new JSeparator());
    p = new JPanel(new GridBagLayout());
    container.add(p);
    
    c = new GridBagConstraints();
    c.gridwidth=3;
    p.add(new JLabel(translator.get("AdjustPulleySize"),SwingConstants.CENTER),c);
    c.gridwidth=1;
    
    final JTextField mBobbin1 = new JTextField(String.valueOf(bobbin_left_diameter * 10));
    final JTextField mBobbin2 = new JTextField(String.valueOf(bobbin_right_diameter * 10));
    y=2;
    c.weightx = 0;
    c.anchor=GridBagConstraints.EAST;
    d.anchor=GridBagConstraints.WEST;
    c.gridx = 0;    c.gridy = y;    p.add(new JLabel(translator.get("Left")), c);
    d.gridx = 1;    d.gridy = y;    p.add(mBobbin1, d);
    d.gridx = 2;    d.gridy = y;    p.add(new JLabel(translator.get("Millimeters")), d);
    y++;
    c.gridx = 0;    c.gridy = y;    p.add(new JLabel(translator.get("Right")), c);
    d.gridx = 1;    d.gridy = y;    p.add(mBobbin2, d);
    d.gridx = 2;    d.gridy = y;    p.add(new JLabel(translator.get("Millimeters")), d);

    Dimension s = mBobbin1.getPreferredSize();
    s.width = 80;
    mBobbin1.setPreferredSize(s);
    mBobbin2.setPreferredSize(s);


    p = new JPanel(new GridBagLayout());
    container.add(p);
    
    c.anchor=GridBagConstraints.EAST;
    c.gridy=0;
    c.weightx=0;
    c.weighty=1;
    c.gridx=1; c.gridwidth=1; p.add(save,c);
    c.gridx=2; c.gridwidth=1; p.add(cancel,c);
    c.weightx=1;
    c.gridx=0; c.gridwidth=1; p.add(Box.createGlue(),c);

    s = ph.getPreferredSize();
    s.width = 80;
    mw.setPreferredSize(s);
    mh.setPreferredSize(s);
    pw.setPreferredSize(s);
    ph.setPreferredSize(s);

    KeyListener driveKeys = new KeyListener() {
    	public void keyPressed(KeyEvent e) {}
    	public void keyReleased(KeyEvent e) { Event(e); }
    	public void keyTyped(KeyEvent e) { Event(e); }
    	
    	private void Event(KeyEvent e) {
        	double w=0;
        	double h=0;
        	try {
        		w = Double.parseDouble(pw.getText());
        		h = Double.parseDouble(ph.getText());
        	} catch(Exception err) {
        		err.getMessage();
        	}
        	paperSizes.setSelectedIndex(getCurrentPaperSizeChoice(w,h));	
    	}
    };
    
    ActionListener driveButtons = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Object subject = e.getSource();

        if(subject == paperSizes) {
            final int selectedIndex = paperSizes.getSelectedIndex();
            if(selectedIndex!= 0) {
            	String str = paperSizes.getItemAt(selectedIndex);
            	String sw = str.substring(str.indexOf('(')+1, str.indexOf('x')).trim();
            	String sh = str.substring(str.indexOf('x')+1, str.indexOf(')')).trim();
            	pw.setText(sw);
            	ph.setText(sh);
            }
        }
        
        if(subject == save) {
            double pwf = Double.valueOf(pw.getText()) / 10.0;
            double phf = Double.valueOf(ph.getText()) / 10.0;
            double mwf = Double.valueOf(mw.getText()) / 10.0;
            double mhf = Double.valueOf(mh.getText()) / 10.0;
            boolean data_is_sane=true;
            if( pwf<=0 ) data_is_sane=false;
            if( phf<=0 ) data_is_sane=false;
            if( mwf<=0 ) data_is_sane=false;
            if( mhf<=0 ) data_is_sane=false;

            double bld = Double.valueOf(mBobbin1.getText()) / 10.0;
            double brd = Double.valueOf(mBobbin2.getText()) / 10.0;

            if (bld <= 0) data_is_sane = false;
            if (brd <= 0) data_is_sane = false;

          if (data_is_sane) {
        	  bobbin_left_diameter = bld;
        	  bobbin_right_diameter = brd;
            //startingPositionIndex = startPos.getSelectedIndex();
            /*// relative to machine limits
            switch(startingPositionIndex%3) {
            case 0:
              paper_left=(mwf-pwf)/2.0f;
              paper_right=mwf-paper_left;
              limit_left=0;
              limit_right=mwf;
              break;
            case 1:
              paper_left = -pwf/2.0f;
              paper_right = pwf/2.0f;
              limit_left = -mwf/2.0f;
              limit_right = mwf/2.0f;
              break;
            case 2:
              paper_right=-(mwf-pwf)/2.0f;
              paper_left=-mwf-paper_right;
              limit_left=-mwf;
              limit_right=0;
              break;
            }
            switch(startingPositionIndex/3) {
            case 0:
              paper_top=-(mhf-phf)/2;
              paper_bottom=-mhf-paper_top;
              limit_top=0;
              limit_bottom=-mhf;
              break;
            case 1:
              paper_top=phf/2;
              paper_bottom=-phf/2;
              limit_top=mhf/2;
              limit_bottom=-mhf/2;
              break;
            case 2:
              paper_bottom=(mhf-phf)/2;
              paper_top=mhf-paper_bottom;
              limit_top=mhf;
              limit_bottom=0;
              break;
            }
            */
            startingPositionIndex = 4;
            // relative to paper limits
            switch (startingPositionIndex % 3) {
              case 0:
                paper_left = 0;
                paper_right = pwf;
                limit_left = -(mwf - pwf) / 2.0f;
                limit_right = (mwf - pwf) / 2.0f + pwf;
                break;
              case 1:
                paper_left = -pwf / 2.0f;
                paper_right = pwf / 2.0f;
                limit_left = -mwf / 2.0f;
                limit_right = mwf / 2.0f;
                break;
              case 2:
                paper_right = 0;
                paper_left = -pwf;
                limit_left = -pwf - (mwf - pwf) / 2.0f;
                limit_right = (mwf - pwf) / 2.0f;
                break;
            }
            switch (startingPositionIndex / 3) {
              case 0:
                paper_top = 0;
                paper_bottom = -phf;
                limit_top = (mhf - phf) / 2.0f;
                limit_bottom = -phf - (mhf - phf) / 2.0f;
                break;
              case 1:
                paper_top = phf / 2.0f;
                paper_bottom = -phf / 2.0f;
                limit_top = mhf / 2.0f;
                limit_bottom = -mhf / 2.0f;
                break;
              case 2:
                paper_bottom = 0;
                paper_top = phf;
                limit_top = phf + (mhf - phf) / 2.0f;
                limit_bottom = -(mhf - phf) / 2.0f;
                break;
            }

            saveConfig();
            mainGUI.sendConfig();
            driver.dispose();
          }
        }
        if (subject == cancel) {
          driver.dispose();
        }
      }
    };

    mainGUI.sendLineToRobot("M114"); // "where" command
    save.addActionListener(driveButtons);
    driver.getRootPane().setDefaultButton(save);
    cancel.addActionListener(driveButtons);
    paperSizes.addActionListener(driveButtons);
    pw.addKeyListener(driveKeys);
    ph.addKeyListener(driveKeys);
    driver.pack();
    driver.setVisible(true);
  }


  public String[] getToolNames() {
    String[] toolNames = new String[tools.size()];
    Iterator<DrawingTool> i = tools.iterator();
    int c = 0;
    while (i.hasNext()) {
      DrawingTool t = i.next();
      toolNames[c++] = t.getName();
    }
    return toolNames;
  }


  /**
   * dialog to adjust the pen up & pen down values
   */
  protected void changeTool() {
    final JDialog driver = new JDialog(mainGUI.getParentFrame(), translator.get("AdjustMachineSize"), true);
    driver.setLayout(new GridBagLayout());

    final JComboBox<String> toolCombo = new JComboBox<String>(getToolNames());
    toolCombo.setSelectedIndex(current_tool);

    final JButton cancel = new JButton(translator.get("Cancel"));
    final JButton save = new JButton(translator.get("Save"));

    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 1;
    c.gridwidth = 2;
    c.gridheight = 1;
    driver.add(new JLabel(translator.get("ToolType")), c);
    c.anchor = GridBagConstraints.WEST;
    c.gridx = 2;
    c.gridy = 1;
    c.gridwidth = 2;
    c.gridheight = 1;
    driver.add(toolCombo, c);


    c.anchor = GridBagConstraints.EAST;
    c.gridy = 3;
    c.gridx = 3;
    c.gridwidth = 1;
    driver.add(cancel, c);
    c.gridx = 2;
    c.gridwidth = 1;
    driver.add(save, c);

    ActionListener driveButtons = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Object subject = e.getSource();
        if (subject == save) {
          current_tool = toolCombo.getSelectedIndex();

          saveConfig();
          mainGUI.sendConfig();
          driver.dispose();
        }
        if (subject == cancel) {
          driver.dispose();
        }
      }
    };

    save.addActionListener(driveButtons);
    cancel.addActionListener(driveButtons);
    driver.getRootPane().setDefaultButton(save);
    driver.pack();
    driver.setVisible(true);
  }


  // dialog to adjust the pen up & pen down values
  protected void adjustTool() {
    getCurrentTool().adjust();
  }


  public DrawingTool getTool(int tool_id) {
    return tools.get(tool_id);
  }


  public DrawingTool getCurrentTool() {
    return getTool(current_tool);
  }


  /**
   * Load the machine configuration
   *
   * @param uid the unique id of the robot to be loaded
   */
  protected void loadConfig(long uid) {
    robot_uid = uid;
    // once cloud logic is finished.
    //if( GetCanUseCloud() && LoadConfigFromCloud() ) return;
    loadConfigFromLocal();
  }

  protected void loadConfigFromLocal() {
    final Preferences uniqueMachinePreferencesNode = topLevelMachinesPreferenceNode.node(Long.toString(robot_uid));
    limit_top = Double.valueOf(uniqueMachinePreferencesNode.get("limit_top", Double.toString(limit_top)));
    limit_bottom = Double.valueOf(uniqueMachinePreferencesNode.get("limit_bottom", Double.toString(limit_bottom)));
    limit_left = Double.valueOf(uniqueMachinePreferencesNode.get("limit_left", Double.toString(limit_left)));
    limit_right = Double.valueOf(uniqueMachinePreferencesNode.get("limit_right", Double.toString(limit_right)));
    
    paper_left=Double.parseDouble(uniqueMachinePreferencesNode.get("paper_left",Double.toString(paper_left)));
    paper_right=Double.parseDouble(uniqueMachinePreferencesNode.get("paper_right",Double.toString(paper_right)));
    paper_top=Double.parseDouble(uniqueMachinePreferencesNode.get("paper_top",Double.toString(paper_top)));
    paper_bottom=Double.parseDouble(uniqueMachinePreferencesNode.get("paper_bottom",Double.toString(paper_bottom)));

    m1invert=Boolean.parseBoolean(uniqueMachinePreferencesNode.get("m1invert", m1invert?"true":"false"));
    m2invert=Boolean.parseBoolean(uniqueMachinePreferencesNode.get("m2invert", m2invert?"true":"false"));
    
    bobbin_left_diameter=Double.valueOf(uniqueMachinePreferencesNode.get("bobbin_left_diameter", Double.toString(bobbin_left_diameter)));
    bobbin_right_diameter=Double.valueOf(uniqueMachinePreferencesNode.get("bobbin_right_diameter", Double.toString(bobbin_right_diameter)));
    
    max_feed_rate=Double.valueOf(uniqueMachinePreferencesNode.get("feed_rate",Double.toString(max_feed_rate)));
    
    startingPositionIndex=Integer.valueOf(uniqueMachinePreferencesNode.get("startingPosIndex",Integer.toString(startingPositionIndex)));

    // load each tool's settings
    for (DrawingTool tool : tools) {
      tool.loadConfig(uniqueMachinePreferencesNode);
    }

    paperMargin = Double.valueOf(uniqueMachinePreferencesNode.get("paper_margin", Double.toString(paperMargin)));
    reverseForGlass = Boolean.parseBoolean(uniqueMachinePreferencesNode.get("reverseForGlass", reverseForGlass ? "true" : "false"));
    current_tool = Integer.valueOf(uniqueMachinePreferencesNode.get("current_tool", Integer.toString(current_tool)));
  }


  // Save the machine configuration
  public void saveConfig() {
    // once cloud logic is finished.
    //if(GetCanUseCloud() && SaveConfigToCloud() ) return;
    saveConfigToLocal();
  }

  /*
  // TODO finish these cloud storage methods.  Security will be a problem.

   public boolean GetCanUseCloud() {
    return topLevelMachinesPreferenceNode.getBoolean("can_use_cloud", false);
  }


  public void SetCanUseCloud(boolean b) {
    topLevelMachinesPreferenceNode.putBoolean("can_use_cloud", b);
  }

  protected boolean SaveConfigToCloud() {
    return false;
  }



   protected boolean LoadConfigFromCloud() {
     // Ask for credentials: MC login, password.  auto-remember login name.
     //String login = new String();
     //String password = new String();

     //try {
     // Send query
     //URL url = new URL("https://marginallyclever.com/drawbot_getmachineconfig.php?name="+login+"pass="+password+"&id="+robot_uid);
     //URLConnection conn = url.openConnection();
     //BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
     // read data

     // close connection
     //rd.close();
     //} catch (Exception e) {}

    return false;
  }
   */


  protected void saveConfigToLocal() {
    final Preferences uniqueMachinePreferencesNode = topLevelMachinesPreferenceNode.node(Long.toString(robot_uid));
    uniqueMachinePreferencesNode.put("limit_top", Double.toString(limit_top));
    uniqueMachinePreferencesNode.put("limit_bottom", Double.toString(limit_bottom));
    uniqueMachinePreferencesNode.put("limit_right", Double.toString(limit_right));
    uniqueMachinePreferencesNode.put("limit_left", Double.toString(limit_left));
    uniqueMachinePreferencesNode.put("m1invert", Boolean.toString(m1invert));
    uniqueMachinePreferencesNode.put("m2invert", Boolean.toString(m2invert));
    uniqueMachinePreferencesNode.put("bobbin_left_diameter", Double.toString(bobbin_left_diameter));
    uniqueMachinePreferencesNode.put("bobbin_right_diameter", Double.toString(bobbin_right_diameter));
    uniqueMachinePreferencesNode.put("feed_rate", Double.toString(max_feed_rate));
    uniqueMachinePreferencesNode.put("startingPosIndex", Integer.toString(startingPositionIndex));

    uniqueMachinePreferencesNode.putDouble("paper_left", paper_left);
    uniqueMachinePreferencesNode.putDouble("paper_right", paper_right);
    uniqueMachinePreferencesNode.putDouble("paper_top", paper_top);
    uniqueMachinePreferencesNode.putDouble("paper_bottom", paper_bottom);

    // save each tool's settings
    for (DrawingTool tool : tools) {
      tool.saveConfig(uniqueMachinePreferencesNode);
    }

    // TODO move these values to image filter preferences?
    uniqueMachinePreferencesNode.put("paper_margin", Double.toString(paperMargin));
    uniqueMachinePreferencesNode.put("reverseForGlass", Boolean.toString(reverseForGlass));
    uniqueMachinePreferencesNode.put("current_tool", Integer.toString(current_tool));
  }


  public String getBobbinLine() {
    return "D1 L" + bobbin_left_diameter + " R" + bobbin_right_diameter;
  }


  public String getConfigLine() {
    return "M101 T" + limit_top
        + " B" + limit_bottom
        + " L" + limit_left
        + " R" + limit_right
        + " I" + (m1invert ? "-1" : "1")
        + " J" + (m2invert ? "-1" : "1");
  }


  public String getPenUpString() {
    return Float.toString(tools.get(current_tool).getZOff());
  }

  public String getPenDownString() {
    return Float.toString(tools.get(current_tool).getZOn());
  }

  public boolean isPaperConfigured() {
    return (paper_top > paper_bottom && paper_right > paper_left);
  }

  public void parseRobotUID(String line) {
    saveConfig();

    // get the UID reported by the robot
    String[] lines = line.split("\\r?\\n");
    long new_uid = 0;
    if (lines.length > 0) {
      try {
        new_uid = Long.parseLong(lines[0]);
      } catch (NumberFormatException e) {
        logger.error("{}", e);
      }
    }

    // new robots have UID=0
    if (new_uid == 0) {
      new_uid = getNewRobotUID();
    }

    // load machine specific config
    loadConfig(new_uid);

    if (limit_top == 0 && limit_bottom == 0 && limit_left == 0 && limit_right == 0) {
      // probably first time turning on, adjust the machine size
      adjustMachineSize();
    }
  }

  /**
   * based on http://www.exampledepot.com/egs/java.net/Post.html
   */
  private long getNewRobotUID() {
    long new_uid = 0;

    try {
      // Send data
      URL url = new URL("https://marginallyclever.com/drawbot_getuid.php");
      URLConnection conn = url.openConnection();
      try (
          final InputStream connectionInputStream = conn.getInputStream();
          final Reader inputStreamReader = new InputStreamReader(connectionInputStream);
          final BufferedReader rd = new BufferedReader(inputStreamReader)
      ) {
        String line = rd.readLine();
        new_uid = Long.parseLong(line);
      }
    } catch (Exception e) {
      logger.error("{}", e);
      return 0;
    }

    // did read go ok?
    if (new_uid != 0) {
      // make sure a topLevelMachinesPreferenceNode node is created
      topLevelMachinesPreferenceNode.node(Long.toString(new_uid));
      // tell the robot it's new UID.
      mainGUI.sendLineToRobot("UID " + new_uid);

      // if this is a new robot UID, update the list of available configurations
      final String[] new_list = new String[machineConfigurationsAvailable.length + 1];
      System.arraycopy(machineConfigurationsAvailable, 0, new_list, 0, machineConfigurationsAvailable.length);
      new_list[machineConfigurationsAvailable.length] = Long.toString(new_uid);
      machineConfigurationsAvailable = new_list;
    }
    return new_uid;
  }


  /**
   * @return the number of machine configurations that exist on this computer
   */
  public int getMachineCount() {
    return machineConfigurationsAvailable.length;
  }


  /**
   * Get the UID of every machine this computer recognizes EXCEPT machine 0, which is only assigned temporarily when a machine is new or before the first software connect.
   *
   * @return an array of strings, each string is a machine UID.
   */
  public String[] getKnownMachineNames() {
    final List<String> machineConfigurationsAvailableArrayAsList = new LinkedList<>(Arrays.asList(machineConfigurationsAvailable));
    if (machineConfigurationsAvailableArrayAsList.contains("0")) {
      machineConfigurationsAvailableArrayAsList.remove("0");
    }
    return Arrays.copyOf(machineConfigurationsAvailableArrayAsList.toArray(), machineConfigurationsAvailableArrayAsList.size(), String[].class);
  }

  /**
   * Get the UID of every machine this computer recognizes INCLUDING machine 0, which is only assigned temporarily when a machine is new or before the first software connect.
   *
   * @return an array of strings, each string is a machine UID.
   */
  public String[] getAvailableConfigurations() {
    return machineConfigurationsAvailable;
  }


  public int getCurrentMachineIndex() {
    for (int i = 0; i < machineConfigurationsAvailable.length; i++) {
      if (machineConfigurationsAvailable[i].equals("0")) continue;
      if (machineConfigurationsAvailable[i].equals(Long.toString(robot_uid))) {
        return i;
      }
    }

    return 0;
  }


  public double getPaperWidth() {
    return paper_right - paper_left;
  }


  public double getPaperHeight() {
    return paper_top - paper_bottom;
  }


  public double getPaperScale() {
    double paper_w = getPaperWidth();
    double paper_h = getPaperHeight();

    if (paper_w > paper_h) {
      return paper_h / paper_w;
    } else {
      return paper_w / paper_h;
    }
  }

  public double getFeedRate() {
    return max_feed_rate;
  }

  public void setFeedRate(double f) {
    max_feed_rate = f;
    saveConfig();
  }

  public long getUID() {
    return robot_uid;
  }
}
