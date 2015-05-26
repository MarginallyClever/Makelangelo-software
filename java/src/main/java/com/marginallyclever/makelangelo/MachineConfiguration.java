package com.marginallyclever.makelangelo;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
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

/**
 * FIXME Add Javadoc.
 */
public class MachineConfiguration {

	/**
	 *
	 */
	private Preferences makelangeloPreferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MAKELANGELO_ROOT);

	/**
	 *
	 */
	private Preferences machinePreferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.MACHINES);
	
	static final String CURRENT_VERSION = "1";
	// GUID
	protected long robot_uid=0;
	
	protected double INCH_TO_CM = 2.54;
	
	// machine physical limits
	public double limit_top=18*INCH_TO_CM;
	public double limit_bottom=-18*INCH_TO_CM;
	public double limit_left=-18*INCH_TO_CM;
	public double limit_right=18*INCH_TO_CM;
	
	// paper area
	public double paper_top=12*INCH_TO_CM;
	public double paper_bottom=-12*INCH_TO_CM;
	public double paper_left=-9*INCH_TO_CM;
	public double paper_right=9*INCH_TO_CM;
	public double paperMargin=0.9;
	
	// pulleys turning backwards?
	public boolean m1invert=false;
	public boolean m2invert=false;

	// pulley diameter
	private double bobbin_left_diameter=1.5;
	private double bobbin_right_diameter=1.5;

	private double max_feed_rate=3500;  // etch-a-sketch speed
	
	public boolean reverseForGlass=false;
	public boolean motors_backwards=false;
	protected int current_style;

/*
	// top left, bottom center, etc...
	private String[] startingStrings = { 
			"Top Left", 
			"Top Center",
			"Top Right", 
			"Left", 
			"Center", 
			"Right", 
			"Bottom Left",
			"Bottom Center",
			"Bottom Right" };*/
	private int startingPositionIndex=4;
	
	// TODO a way for users to create different tools for each machine 
	protected DrawingTool tools[];
	protected int current_tool=0;	
	
	protected String [] configurations_available = null;
	private MainGUI mainGUI = null;
	private MultilingualSupport translator;
	
	
	// constructor
	protected MachineConfiguration(MainGUI gui,MultilingualSupport ms) {
		mainGUI = gui;
		translator = ms;
		
		tools = new DrawingTool[6];
		int i=0;
		tools[i++]=new DrawingTool_Pen("Pen (black)",0,gui,ms,this);
		tools[i++]=new DrawingTool_Pen("Pen (red)",1,gui,ms,this);
		tools[i++]=new DrawingTool_Pen("Pen (green)",2,gui,ms,this);
		tools[i++]=new DrawingTool_Pen("Pen (blue)",3,gui,ms,this);
		tools[i++]=new DrawingTool_LED(gui,ms,this);
		tools[i++]=new DrawingTool_Spraypaint(gui,ms,this);
		
		versionCheck();
		
		// which configurations are available?
		try {
			configurations_available = machinePreferences.childrenNames();
			
			// remove from configurations_available all names which are not parsable as a number.
			// only numbers are machine names, the rest are tool names.
			Vector<String> valid = new Vector<String>();
			for(i=0;i<configurations_available.length;++i) {
				try {
					Integer.parseInt(configurations_available[i]);
					// if parseInt throws a NumberFormatException, the next line will not happen.
					// thus only names that fit our format (numbers only) will make it into the valid list.
					valid.addElement(configurations_available[i]);
				} catch(NumberFormatException e) {
					// invalid, do nothing.
				}
			}
			
			configurations_available = new String[valid.size()];
			valid.copyInto(configurations_available);
		}
		catch(Exception e) {
			configurations_available = new String[0];
		}
		
		// TODO load most recent config?
		loadConfig(0);
	}
	
	/**
	* Open the config dialog, send the config update to the robot, save it for future, and refresh the preview tab.
	*/
	public void adjustMachineSize() {
		final JDialog driver = new JDialog(mainGUI.getParentFrame(),"Adjust machine & paper size",true);
		driver.setLayout(new GridBagLayout());
		
		final JTextField mw = new JTextField(String.valueOf((limit_right-limit_left)*10));
		final JTextField mh = new JTextField(String.valueOf((limit_top-limit_bottom)*10));
		final JTextField pw = new JTextField(String.valueOf((paper_right-paper_left)*10));
		final JTextField ph = new JTextField(String.valueOf((paper_top-paper_bottom)*10));
		final JCheckBox m1i = new JCheckBox(translator.get("Invert"),this.m1invert);
		final JCheckBox m2i = new JCheckBox(translator.get("Invert"),this.m2invert);

		//final JComboBox<String> startPos = new JComboBox<String>(startingStrings);
		//startPos.setSelectedIndex(startingPositionIndex);
		
		final JButton cancel = new JButton(translator.get("Cancel"));
		final JButton save = new JButton(translator.get("Save"));
		
		String limit_file = "limits.png";
		
		BufferedImage myPicture = null;
		try {
			InputStream s = MainGUI.class.getResourceAsStream("/"+limit_file);
			myPicture = ImageIO.read(s);
		}
		catch(IOException e) {
			e.printStackTrace();
			
		}
		if (myPicture == null) {System.err.println(translator.get("CouldNotFind")+limit_file); return;}
		
		JLabel picLabel = new JLabel(new ImageIcon( myPicture ));
		
		GridBagConstraints c = new GridBagConstraints();
		GridBagConstraints d = new GridBagConstraints();
		
		int y=0;
		
		c.weightx=0.25;
		c.gridx=0; c.gridy=y; c.gridwidth=4; c.gridheight=4; c.anchor=GridBagConstraints.CENTER; driver.add( picLabel,c );
		y+=5;
		
		c.gridheight=1; c.gridwidth=1; 
		c.gridx=0; c.gridy=y; c.gridwidth=4; c.gridheight=1;
		driver.add(new JLabel(translator.get("mmNotice")),c);
		c.gridwidth=1;
		y++;

		c.ipadx=3;
		c.anchor=GridBagConstraints.EAST;
		d.anchor=GridBagConstraints.WEST;
		
		c.gridx=0; c.gridy=y; driver.add(new JLabel(translator.get("MachineWidth")),c);		d.gridx=1;	d.gridy=y;	driver.add(mw,d);
		c.gridx=2; c.gridy=y; driver.add(new JLabel(translator.get("MachineHeight")),c);	d.gridx=3;	d.gridy=y;	driver.add(mh,d);
		y++;
		c.gridx=0; c.gridy=y; driver.add(new JLabel(translator.get("PaperWidth")),c);		d.gridx=1;	d.gridy=y;	driver.add(pw,d);
		c.gridx=2; c.gridy=y; driver.add(new JLabel(translator.get("PaperHeight")),c);		d.gridx=3;	d.gridy=y;	driver.add(ph,d);
		y++;

		c.gridx=0; c.gridy=y; driver.add(new JLabel(translator.get("InvertLeft")),c);		d.gridx=1;	d.gridy=y;	driver.add(m1i,d);
		c.gridx=2; c.gridy=y; driver.add(new JLabel(translator.get("InvertRight")),c);		d.gridx=3;	d.gridy=y;	driver.add(m2i,d);
		y++;
		
		//c.gridx=0; c.gridy=9; c.gridwidth=4; c.gridheight=1;
		//driver.add(new JLabel("For more info see http://bit.ly/fix-this-link."),c);
		//c.gridx=0; c.gridy=11; c.gridwidth=2; c.gridheight=1;  driver.add(new JLabel("Pen starts at paper"),c);
		//c.anchor=GridBagConstraints.WEST;
		//c.gridx=2; c.gridy=11; c.gridwidth=2; c.gridheight=1;  driver.add(startPos,c);

		
		c.anchor=GridBagConstraints.EAST;
		c.gridy=13;
		c.gridx=3; c.gridwidth=1; driver.add(cancel,c);
		c.gridx=2; c.gridwidth=1; driver.add(save,c);
		
		Dimension s=ph.getPreferredSize();
		s.width=80;
		mw.setPreferredSize(s);
		mh.setPreferredSize(s);
		pw.setPreferredSize(s);
		ph.setPreferredSize(s);
	
		ActionListener driveButtons = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object subject = e.getSource();

				double pwf = Double.valueOf(pw.getText()) / 10.0;
				double phf = Double.valueOf(ph.getText()) / 10.0;
				double mwf = Double.valueOf(mw.getText()) / 10.0;
				double mhf = Double.valueOf(mh.getText()) / 10.0;
				boolean data_is_sane=true;
				if( pwf<=0 ) data_is_sane=false;
				if( phf<=0 ) data_is_sane=false;
				if( mwf<=0 ) data_is_sane=false;
				if( mhf<=0 ) data_is_sane=false;
				
				if(subject == save) {
					
					if(data_is_sane) {
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
						startingPositionIndex=4;
						// relative to paper limits
						switch(startingPositionIndex%3) {
						case 0:
							paper_left=0;
							paper_right=pwf;
							limit_left=-(mwf-pwf)/2.0f;
							limit_right=(mwf-pwf)/2.0f + pwf;
							break;
						case 1:
							paper_left = -pwf/2.0f;
							paper_right = pwf/2.0f;
							limit_left = -mwf/2.0f;
							limit_right = mwf/2.0f;
							break;
						case 2:
							paper_right=0;
							paper_left=-pwf;
							limit_left=-pwf - (mwf-pwf)/2.0f;
							limit_right=(mwf-pwf)/2.0f;
							break;
						}
						switch(startingPositionIndex/3) {
						case 0:
							paper_top=0;
							paper_bottom=-phf;
							limit_top=(mhf-phf)/2.0f;
							limit_bottom=-phf - (mhf-phf)/2.0f;
							break;
						case 1:
							paper_top=phf/2.0f;
							paper_bottom=-phf/2.0f;
							limit_top=mhf/2.0f;
							limit_bottom=-mhf/2.0f;
							break;
						case 2:
							paper_bottom=0;
							paper_top=phf;
							limit_top=phf + (mhf-phf)/2.0f;
							limit_bottom= - (mhf-phf)/2.0f;
							break;
						}

						m1invert = m1i.isSelected();
						m2invert = m2i.isSelected();
						
						saveConfig();
						mainGUI.sendConfig();
						driver.dispose();
					}
				}
				if(subject == cancel) {
					driver.dispose();
				}
			}
		};
	
		save.addActionListener(driveButtons);
		cancel.addActionListener(driveButtons);
		driver.getRootPane().setDefaultButton(save);
		mainGUI.sendLineToRobot("M114"); // "where" command
		driver.pack();
		driver.setVisible(true);
	}
	

	public String [] getToolNames() {
		String[] toolNames = new String[tools.length];
		for(int i=0;i<tools.length;++i) {
			toolNames[i] = tools[i].getName();
		}
		return toolNames;
	}
	
	
	// dialog to adjust the pen up & pen down values
	protected void changeTool() {
		final JDialog driver = new JDialog(mainGUI.getParentFrame(),translator.get("AdjustMachineSize"),true);
		driver.setLayout(new GridBagLayout());
		
		final JComboBox<String> toolCombo = new JComboBox<String>(getToolNames());
		toolCombo.setSelectedIndex(current_tool);
		
		final JButton cancel = new JButton(translator.get("Cancel"));
		final JButton save = new JButton(translator.get("Save"));
		
		GridBagConstraints c = new GridBagConstraints();
		
		c.gridheight=1; c.gridwidth=1; 
		
		c.gridx=0; c.gridy=1; c.gridwidth=2; c.gridheight=1;  driver.add(new JLabel(translator.get("ToolType")),c);
		c.anchor=GridBagConstraints.WEST;
		c.gridx=2; c.gridy=1; c.gridwidth=2; c.gridheight=1;  driver.add(toolCombo,c);

		
		c.anchor=GridBagConstraints.EAST;
		c.gridy=3;
		c.gridx=3; c.gridwidth=1; driver.add(cancel,c);
		c.gridx=2; c.gridwidth=1; driver.add(save,c);
			
		ActionListener driveButtons = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object subject = e.getSource();
				if(subject == save) {
					current_tool = toolCombo.getSelectedIndex();
					
					saveConfig();
					mainGUI.sendConfig();
					driver.dispose();
				}
				if(subject == cancel) {
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
		return tools[tool_id];
	}
	
	
	public DrawingTool getCurrentTool() {
		return getTool(current_tool);
	}
	
	
	// Open the config dialog, send the config update to the robot, save it for future, and refresh the preview tab.
	public void adjustPulleySize() {
		final JDialog driver = new JDialog(mainGUI.getParentFrame(),translator.get("AdjustPulleySize"),true);
		driver.setLayout(new GridBagLayout());

		final JTextField mBobbin1 = new JTextField(String.valueOf(bobbin_left_diameter*10));
		final JTextField mBobbin2 = new JTextField(String.valueOf(bobbin_right_diameter*10));

		final JButton cancel = new JButton(translator.get("Cancel"));
		final JButton save = new JButton(translator.get("Save"));

		GridBagConstraints c = new GridBagConstraints();
		c.weightx=50;
		c.gridx=0;  c.gridy=1;  driver.add(new JLabel(translator.get("Left")),c);
		c.gridx=0;  c.gridy=2;  driver.add(new JLabel(translator.get("Right")),c);
		c.gridx=1;  c.gridy=0;  driver.add(new JLabel(translator.get("Diameter")),c);
		c.gridx=1;	c.gridy=1;	driver.add(mBobbin1,c);
		c.gridx=1;	c.gridy=2;	driver.add(mBobbin2,c);
		c.gridx=2;  c.gridy=1;  driver.add(new JLabel(translator.get("Millimeters")),c);
		c.gridx=2;  c.gridy=2;  driver.add(new JLabel(translator.get("Millimeters")),c);
		c.gridx=0;  c.gridy=3;  driver.add(save,c);
		c.gridx=1;  c.gridy=3;  driver.add(cancel,c);
		
		Dimension s=mBobbin1.getPreferredSize();
		s.width=80;
		mBobbin1.setPreferredSize(s);
		mBobbin2.setPreferredSize(s);
		
		ActionListener driveButtons = new ActionListener() {
			  public void actionPerformed(ActionEvent e) {
					Object subject = e.getSource();
					if(subject == save) {
						bobbin_left_diameter = Double.valueOf(mBobbin1.getText())/10.0;
						bobbin_right_diameter = Double.valueOf(mBobbin2.getText())/10.0;
						boolean data_is_sane=true;
						if( bobbin_left_diameter <= 0 ) data_is_sane=false;
						if( bobbin_right_diameter <= 0 ) data_is_sane=false;
						if(data_is_sane ) {
							saveConfig();
							mainGUI.sendConfig();
							driver.dispose();
						}
					}
					if(subject == cancel) {
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

	
	protected void versionCheck() {
		String version = makelangeloPreferences.get("version", CURRENT_VERSION);
		if( version.equals(CURRENT_VERSION) == false ) {
			makelangeloPreferences.put("version", CURRENT_VERSION);
		}
	}
	
	
	// Load the machine configuration
	protected void loadConfig(long uid) {
		robot_uid = uid;
		//if( GetCanUseCloud() && LoadConfigFromCloud() ) return; FIXME once cloud logic is finished.
		loadConfigFromLocal();
	}
	
	protected void loadConfigFromLocal() {
		machinePreferences.node(Long.toString(robot_uid));
		limit_top = Double.valueOf(machinePreferences.get("limit_top", Double.toString(limit_top)));
		limit_bottom = Double.valueOf(machinePreferences.get("limit_bottom", Double.toString(limit_bottom)));
		limit_left = Double.valueOf(machinePreferences.get("limit_left", Double.toString(limit_left)));
		limit_right = Double.valueOf(machinePreferences.get("limit_right", Double.toString(limit_right)));
		m1invert=Boolean.parseBoolean(machinePreferences.get("m1invert", m1invert?"true":"false"));
		m2invert=Boolean.parseBoolean(machinePreferences.get("m2invert", m2invert?"true":"false"));
		bobbin_left_diameter=Double.valueOf(machinePreferences.get("bobbin_left_diameter", Double.toString(bobbin_left_diameter)));
		bobbin_right_diameter=Double.valueOf(machinePreferences.get("bobbin_right_diameter", Double.toString(bobbin_right_diameter)));
		max_feed_rate=Double.valueOf(machinePreferences.get("feed_rate",Double.toString(max_feed_rate)));
		startingPositionIndex=Integer.valueOf(machinePreferences.get("startingPosIndex",Integer.toString(startingPositionIndex)));

		paper_left=Double.parseDouble(machinePreferences.get("paper_left",Double.toString(paper_left)));
		paper_right=Double.parseDouble(machinePreferences.get("paper_right",Double.toString(paper_right)));
		paper_top=Double.parseDouble(machinePreferences.get("paper_top",Double.toString(paper_top)));
		paper_bottom=Double.parseDouble(machinePreferences.get("paper_bottom",Double.toString(paper_bottom)));
		
		// load each tool's settings
		for(int i=0;i<tools.length;++i) {
			tools[i].loadConfig(machinePreferences);
		}

		// TODO move these values to image filter preferences
		paperMargin = Double.valueOf(machinePreferences.get("paper_margin",Double.toString(paperMargin)));
		reverseForGlass = Boolean.parseBoolean(machinePreferences.get("reverseForGlass",reverseForGlass?"true":"false"));
		current_tool = Integer.valueOf(machinePreferences.get("current_tool",Integer.toString(current_tool)));
	}

	
	// Save the machine configuration
	public void saveConfig() {
		//if(GetCanUseCloud() && SaveConfigToCloud() ) return; FIXME once cloud logic is finished.
		saveConfigToLocal();
	}

	/** 		TODO finish these methods.

	 public boolean GetCanUseCloud() {
		return machinePreferences.getBoolean("can_use_cloud", false);
	}

	
	public void SetCanUseCloud(boolean b) {
		machinePreferences.putBoolean("can_use_cloud", b);
	}

	protected boolean SaveConfigToCloud() {
		return false;
	}



	 protected boolean LoadConfigFromCloud() {
	 // Ask for credentials: MC login, password.  auto-remember login name.
	 //String login = new String();
	 //String password = new String();
	 // TODO finish this section


	 //try {
	 // Send query
	 //URL url = new URL("https://marginallyclever.com/drawbot_getmachineconfig.php?name="+login+"pass="+password+"&id="+robot_uid);
	 //URLConnection conn = url.openConnection();
	 //BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	 // read data
	 // TODO finish this section

	 // close connection
	 //rd.close();
	 //} catch (Exception e) {}

	return false;
}
	 */
	
	
	protected void saveConfigToLocal() {
		machinePreferences.node(Long.toString(robot_uid));
		machinePreferences.put("limit_top", Double.toString(limit_top));
		machinePreferences.put("limit_bottom", Double.toString(limit_bottom));
		machinePreferences.put("limit_right", Double.toString(limit_right));
		machinePreferences.put("limit_left", Double.toString(limit_left));
		machinePreferences.put("m1invert", Boolean.toString(m1invert));
		machinePreferences.put("m2invert", Boolean.toString(m2invert));
		machinePreferences.put("bobbin_left_diameter", Double.toString(bobbin_left_diameter));
		machinePreferences.put("bobbin_right_diameter", Double.toString(bobbin_right_diameter));
		machinePreferences.put("feed_rate", Double.toString(max_feed_rate));
		machinePreferences.put("startingPosIndex", Integer.toString(startingPositionIndex));

		machinePreferences.putDouble("paper_left", paper_left);
		machinePreferences.putDouble("paper_right", paper_right);
		machinePreferences.putDouble("paper_top", paper_top);
		machinePreferences.putDouble("paper_bottom", paper_bottom);

		// save each tool's settings
		for(int i=0;i<tools.length;++i) {
			tools[i].saveConfig(machinePreferences);
		}

		// TODO move these values to image filter preferences
		machinePreferences.put("paper_margin", Double.toString(paperMargin));
		machinePreferences.put("reverseForGlass", Boolean.toString(reverseForGlass));
		machinePreferences.put("current_tool", Integer.toString(current_tool));
	}


	
	public String getBobbinLine() {
		return new String("D1 L"+bobbin_left_diameter+" R"+bobbin_right_diameter);
	}

	
	public String getConfigLine() {
		return new String("M101 T"+limit_top
		+" B"+limit_bottom
		+" L"+limit_left
		+" R"+limit_right
		+" I"+(m1invert?"-1":"1")
		+" J"+(m2invert?"-1":"1"));
	}
	
	
	public String getPenUpString() {
		return Float.toString(tools[current_tool].getZOff());
	}
	
	public String getPenDownString() {
		return Float.toString(tools[current_tool].getZOn());
	}
	
	public boolean isPaperConfigured() {
		return (paper_top>paper_bottom && paper_right>paper_left);
	}
	
	public void parseRobotUID(String line) {
		saveConfig();
		
		// get the UID reported by the robot
		String[] lines = line.split("\\r?\\n");
		long new_uid=0;
		if(lines.length>0) {
			try {
				new_uid = Long.parseLong(lines[0]);
			}
			catch(NumberFormatException e) {}
		}
		
		// new robots have UID=0
		if(new_uid==0) {
			new_uid=getNewRobotUID();
		}
		
		// load machine specific config
		loadConfig(new_uid);
		
		if(limit_top==0 && limit_bottom==0 && limit_left==0 && limit_right==0) {
			// probably first time turning on, adjust the machine size
			adjustMachineSize();
		}
	}
	
	/**
	 * based on http://www.exampledepot.com/egs/java.net/Post.html
	 */
	private long getNewRobotUID() {
		long new_uid=0;
		try {
		    // Send data
			URL url = new URL("https://marginallyclever.com/drawbot_getuid.php");
		    URLConnection conn = url.openConnection();
		    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream())); //FIXME use try with resources
		    String line = rd.readLine();
		    new_uid = Long.parseLong(line);
		    rd.close();
		} catch (Exception e) {}

		// did read go ok?
		if(new_uid!=0) {
			// make sure a machinePreferences node is created
			machinePreferences.node(Long.toString(new_uid));
			// tell the robot it's new UID.
			mainGUI.sendLineToRobot("UID "+new_uid);

			// if this is a new robot UID, update the list of available configurations
			String [] new_list = new String[configurations_available.length+1];
			for(int i=0;i<configurations_available.length;++i) {
				new_list[i] = configurations_available[i];
			}
			new_list[configurations_available.length] = Long.toString(new_uid);
		}
		return new_uid;
	}
	
	
	/**
	 * 
	 * @return the number of machine configurations that exist on this computer
	 */
	public int getMachineCount() {
		return configurations_available.length;
	}
	
	
	/**
	 * Get the UID of every machine this computer recognizes EXCEPT machine 0, which is only assigned temporarily when a machine is new or before the first software connect.
	 * @return an array of strings, each string is a machine UID.
	 */
	public String[] getKnownMachineNames() {
		assert(getMachineCount()>0);
		String [] choices = new String[configurations_available.length-1];

		int j=0;
		for(int i=0;i<configurations_available.length;++i) {
			if(configurations_available[i].equals("0")) continue;
			choices[j++] = configurations_available[i];
		}
		
		return choices;
	}
	
	
	public int getCurrentMachineIndex() {
		assert(getMachineCount()>0);
		int j=0;
		for(int i=0;i<configurations_available.length;++i) {
			if(configurations_available[i].equals("0")) continue;
			if(configurations_available[i].equals(Long.toString(robot_uid))) {
				return j;
			}
			++j;
		}
		
		return 0;
	}
	
	
	public double getPaperWidth() {
		return paper_right - paper_left;
	}
	
	
	public double getPaperHeight() {
		return paper_top -paper_bottom;
	}
	
	
	public double getPaperScale() {
		double paper_w=getPaperWidth();
		double paper_h=getPaperHeight();
		
		if(paper_w>paper_h) {
			return paper_h/paper_w;
		} else {
			return paper_w/paper_h;
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
