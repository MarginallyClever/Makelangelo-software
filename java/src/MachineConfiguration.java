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
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;


public class MachineConfiguration {
	private Preferences prefs = Preferences.userRoot().node("DrawBot");
	
	// GUID
	public long robot_uid=0;
	
	// machine physical limits
	public double limit_top=18*2.45;
	public double limit_bottom=-18*2.45;
	public double limit_left=-18*2.45;
	public double limit_right=18*2.45;
	
	// paper area
	public double paper_top=9*2.45;
	public double paper_bottom=-9*2.45;
	public double paper_left=-12*2.45;
	public double paper_right=13*2.45;
	public double paper_margin=0.9;
	
	// pulleys turning backwards?
	public boolean m1invert=false;
	public boolean m2invert=false;

	// pulley diameter
	private double bobbin_left_diameter=16;
	private double bobbin_right_diameter=16;

	private double default_feed_rate=2000;  // etch-a-sketch speed
	
	public boolean reverseForGlass=false;
	public boolean motors_backwards=false;
	protected int current_style;


	// top left, bottom center, etc...
	private int startingPositionIndex;
	
	protected DrawingTool tools[];
	protected int current_tool=0;
		
	
	// singleton
	private static MachineConfiguration singletonObject;
	
	public static MachineConfiguration getSingleton() {
		if(singletonObject==null) {
			singletonObject = new MachineConfiguration();
		}
		return singletonObject;
	}
	
	protected MachineConfiguration() {
		tools = new DrawingTool[3];
		tools[0]=new DrawingTool_Pen();
		tools[1]=new DrawingTool_LED();
		tools[2]=new DrawingTool_Spraypaint();
	}
	
	/**
	* Open the config dialog, send the config update to the robot, save it for future, and refresh the preview tab.
	*/
	public void AdjustMachineSize() {
		final JDialog driver = new JDialog(Makelangelo.getSingleton().getParentFrame(),"Adjust machine size",true);
		driver.setLayout(new GridBagLayout());
		
		final JTextField mw = new JTextField(String.valueOf((limit_right-limit_left)*10));
		final JTextField mh = new JTextField(String.valueOf((limit_top-limit_bottom)*10));
		final JTextField pw = new JTextField(String.valueOf((paper_right-paper_left)*10));
		final JTextField ph = new JTextField(String.valueOf((paper_top-paper_bottom)*10));
		final JCheckBox m1i = new JCheckBox("Invert",MachineConfiguration.getSingleton().m1invert);
		final JCheckBox m2i = new JCheckBox("Invert",MachineConfiguration.getSingleton().m2invert);

		String[] startingStrings = { "Top Left", "Top Center", "Top Right", "Left", "Center", "Right", "Bottom Left","Bottom Center","Bottom Right" };
		final JComboBox startPos = new JComboBox(startingStrings);
		startPos.setSelectedIndex(startingPositionIndex);
		
		final JButton cancel = new JButton("Cancel");
		final JButton save = new JButton("Save");
		
		BufferedImage myPicture = null;
		try {
			InputStream s = Makelangelo.class.getResourceAsStream("limits.png");
			myPicture = ImageIO.read(s);
		}
		catch(IOException e) {
			e.printStackTrace();
			
		}
		if (myPicture == null) {System.err.println("Could not find limits image."); return;}
		
		JLabel picLabel = new JLabel(new ImageIcon( myPicture ));
		
		GridBagConstraints c = new GridBagConstraints();
		GridBagConstraints d = new GridBagConstraints();
		
		int y=0;
		
		c.weightx=0.25;
		c.gridx=0; c.gridy=y; c.gridwidth=4; c.gridheight=4; c.anchor=GridBagConstraints.CENTER; driver.add( picLabel,c );
		y+=5;
		
		c.gridheight=1; c.gridwidth=1; 
		c.gridx=0; c.gridy=y; c.gridwidth=4; c.gridheight=1;
		driver.add(new JLabel("All values in mm."),c);
		c.gridwidth=1;
		y++;

		c.ipadx=3;
		c.anchor=GridBagConstraints.EAST;
		d.anchor=GridBagConstraints.WEST;
		c.gridx=0; c.gridy=y; driver.add(new JLabel("Machine width"),c);	d.gridx=1;	d.gridy=y;	driver.add(mw,d);
		c.gridx=2; c.gridy=y; driver.add(new JLabel("Machine height"),c);	d.gridx=3;	d.gridy=y;	driver.add(mh,d);
		y++;
		c.gridx=0; c.gridy=y; driver.add(new JLabel("Paper width"),c);		d.gridx=1;	d.gridy=y;	driver.add(pw,d);
		c.gridx=2; c.gridy=y; driver.add(new JLabel("Paper height"),c);		d.gridx=3;	d.gridy=y;	driver.add(ph,d);
		y++;

		c.gridx=0; c.gridy=y; driver.add(new JLabel("Invert left"),c);		d.gridx=1;	d.gridy=y;	driver.add(m1i,d);
		c.gridx=2; c.gridy=y; driver.add(new JLabel("Invert right"),c);		d.gridx=3;	d.gridy=y;	driver.add(m2i,d);
		y++;
		
		//c.gridx=0; c.gridy=9; c.gridwidth=4; c.gridheight=1;
		//driver.add(new JLabel("For more info see http://bit.ly/fix-this-link."),c);
		c.gridx=0; c.gridy=11; c.gridwidth=2; c.gridheight=1;  driver.add(new JLabel("Pen starts at paper"),c);
		c.anchor=GridBagConstraints.WEST;
		c.gridx=2; c.gridy=11; c.gridwidth=2; c.gridheight=1;  driver.add(startPos,c);

		
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
				
				float pwf = Math.round( Float.valueOf(pw.getText()) * 100 ) / (100 * 10);
				float phf = Math.round( Float.valueOf(ph.getText()) * 100 ) / (100 * 10);
				float mwf = Math.round( Float.valueOf(mw.getText()) * 100 ) / (100 * 10);
				float mhf = Math.round( Float.valueOf(mh.getText()) * 100 ) / (100 * 10);
				boolean data_is_sane=true;
				if( pwf<=0 ) data_is_sane=false;
				if( phf<=0 ) data_is_sane=false;
				if( mwf<=0 ) data_is_sane=false;
				if( mhf<=0 ) data_is_sane=false;
				
				if(subject == save) {
					
					if(data_is_sane) {
						startingPositionIndex = startPos.getSelectedIndex();
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
						// relative to paper limits
						switch(startingPositionIndex%3) {
						case 0:
							paper_left=0;
							paper_right=pwf;
							limit_left=-(mwf-pwf)/2.0;
							limit_right=(mwf-pwf)/2.0 + pwf;
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
							limit_right=(mwf-pwf)/2;
							break;
						}
						switch(startingPositionIndex/3) {
						case 0:
							paper_top=0;
							paper_bottom=-phf;
							limit_top=(mhf-phf)/2;
							limit_bottom=-phf - (mhf-phf)/2;
							break;
						case 1:
							paper_top=phf/2;
							paper_bottom=-phf/2;
							limit_top=mhf/2;
							limit_bottom=-mhf/2;
							break;
						case 2:
							paper_bottom=0;
							paper_top=phf;
							limit_top=phf + (mhf-phf)/2;
							limit_bottom= - (mhf-phf)/2;
							break;
						}

						m1invert = m1i.isSelected();
						m2invert = m2i.isSelected();
						
						SetRecentPaperSize();
						SaveConfig();
						Makelangelo.getSingleton().SendConfig();
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
		Makelangelo.getSingleton().SendLineToRobot("M114"); // "where" command
		driver.pack();
		driver.setVisible(true);
	}
	

	public String [] getToolNames() {
		String[] toolNames = new String[tools.length];
		for(int i=0;i<tools.length;++i) {
			toolNames[i] = tools[i].GetName();
		}
		return toolNames;
	}
	
	
	// dialog to adjust the pen up & pen down values
	protected void ChangeTool() {
		final JDialog driver = new JDialog(Makelangelo.getSingleton().getParentFrame(),"Adjust machine size",true);
		driver.setLayout(new GridBagLayout());
		
		final JComboBox toolCombo = new JComboBox(getToolNames());
		toolCombo.setSelectedIndex(current_tool);
		
		final JButton cancel = new JButton("Cancel");
		final JButton save = new JButton("Save");
		
		GridBagConstraints c = new GridBagConstraints();
		
		c.gridheight=1; c.gridwidth=1; 
		
		c.gridx=0; c.gridy=1; c.gridwidth=2; c.gridheight=1;  driver.add(new JLabel("Tool type"),c);
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
					
					SaveConfig();
					Makelangelo.getSingleton().SendConfig();
					driver.dispose();
				}
				if(subject == cancel) {
					driver.dispose();
				}
			}
		};
	
		save.addActionListener(driveButtons);
		cancel.addActionListener(driveButtons);
		driver.pack();
		driver.setVisible(true);
	}
	
	
	// dialog to adjust the pen up & pen down values
	protected void AdjustTool() {
		GetCurrentTool().Adjust();
	}
	

	public DrawingTool GetTool(int tool_id) {
		return tools[tool_id];
	}
	
	
	public DrawingTool GetCurrentTool() {
		return GetTool(current_tool);
	}
	
	
	// Open the config dialog, send the config update to the robot, save it for future, and refresh the preview tab.
	public void AdjustPulleySize() {
		final JDialog driver = new JDialog(Makelangelo.getSingleton().getParentFrame(),"Adjust pulley size",true);
		driver.setLayout(new GridBagLayout());

		final JTextField mBobbin1 = new JTextField(String.valueOf(bobbin_left_diameter*10));
		final JTextField mBobbin2 = new JTextField(String.valueOf(bobbin_right_diameter*10));

		final JButton cancel = new JButton("Cancel");
		final JButton save = new JButton("Save");

		GridBagConstraints c = new GridBagConstraints();
		c.weightx=50;
		c.gridx=0;  c.gridy=1;  driver.add(new JLabel("Left"),c);
		c.gridx=0;  c.gridy=2;  driver.add(new JLabel("Right"),c);
		
		c.gridx=1;  c.gridy=0;  driver.add(new JLabel("Diameter"),c);
		c.gridx=1;	c.gridy=1;	driver.add(mBobbin1,c);
		c.gridx=1;	c.gridy=2;	driver.add(mBobbin2,c);

		c.gridx=2;  c.gridy=1;  driver.add(new JLabel("mm"),c);
		c.gridx=2;  c.gridy=2;  driver.add(new JLabel("mm"),c);

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
							SaveConfig();
							Makelangelo.getSingleton().SendConfig();
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
		driver.pack();
		driver.setVisible(true);
	}

	
	// Load the machine configuration
	void LoadConfig() {
		String id=Long.toString(robot_uid);
		limit_top = Double.valueOf(prefs.get(id+"_limit_top", "58.8"));
		limit_bottom = Double.valueOf(prefs.get(id+"_limit_bottom", "-58.8"));
		limit_left = Double.valueOf(prefs.get(id+"_limit_left", "-44.1"));
		limit_right = Double.valueOf(prefs.get(id+"_limit_right", "44.1"));
		m1invert=Boolean.parseBoolean(prefs.get(id+"_m1invert", "false"));
		m2invert=Boolean.parseBoolean(prefs.get(id+"_m2invert", "false"));
		bobbin_left_diameter=Double.valueOf(prefs.get(id+"_bobbin_left_diameter", "1.5"));
		bobbin_right_diameter=Double.valueOf(prefs.get(id+"_bobbin_right_diameter", "1.5"));
		default_feed_rate=Double.valueOf(prefs.get(id+"_feed_rate","2000"));
		startingPositionIndex=Integer.valueOf(prefs.get(id+"_startingPosIndex","4"));
		// TODO move these values to image filter preferences
		paper_margin = Double.valueOf(prefs.get(id+"_paper_margin","0.85"));
		reverseForGlass = Boolean.parseBoolean(prefs.get(id+"_reverseForGlass","false"));
		current_tool = Integer.parseInt(prefs.get(id+"_current_tool","0"),10);
		
		// load each tool's settings
		for(int i=0;i<tools.length;++i) {
			tools[i].LoadConfig(prefs);
		}

		GetRecentPaperSize();
	}

	
	// Save the machine configuration
	void SaveConfig() {
		String id=Long.toString(robot_uid);
		prefs.put(id+"_limit_top", Double.toString(limit_top));
		prefs.put(id+"_limit_bottom", Double.toString(limit_bottom));
		prefs.put(id+"_limit_right", Double.toString(limit_right));
		prefs.put(id+"_limit_left", Double.toString(limit_left));
		prefs.put(id+"_m1invert",Boolean.toString(m1invert));
		prefs.put(id+"_m2invert",Boolean.toString(m2invert));
		prefs.put(id+"_bobbin_left_diameter", Double.toString(bobbin_left_diameter));
		prefs.put(id+"_bobbin_right_diameter", Double.toString(bobbin_right_diameter));
		prefs.put(id+"_feed_rate", Double.toString(default_feed_rate));
		prefs.put(id+"_startingPosIndex", Integer.toString(startingPositionIndex));
		// TODO move these values to image filter preferences
		prefs.put(id+"_paper_margin", Double.toString(paper_margin));
		prefs.put(id+"_reverseForGlass",Boolean.toString(reverseForGlass));
		prefs.put(id+"_current_tool", Integer.toString(current_tool));
		
		// TODO: save each tool's settings
		for(int i=0;i<tools.length;++i) {
			tools[i].SaveConfig(prefs);
		}
		
		SetRecentPaperSize();
	}


	
	String GetBobbinLine() {
		return new String("D01 L"+bobbin_left_diameter+" R"+bobbin_right_diameter);
	}

	
	String GetConfigLine() {
		return new String("CONFIG T"+limit_top
		+" B"+limit_bottom
		+" L"+limit_left
		+" R"+limit_right
		+" I"+(m1invert?"-1":"1")
		+" J"+(m2invert?"-1":"1"));
	}
	
	
	String getPenUpString() {
		return Float.toString(tools[current_tool].z_off);
	}
	
	String getPenDownString() {
		return Float.toString(tools[current_tool].z_on);
	}
	
	// save paper limits
	private void SetRecentPaperSize() {
		String id=Long.toString(robot_uid);
		prefs.putDouble(id+"_paper_left", paper_left);
		prefs.putDouble(id+"_paper_right", paper_right);
		prefs.putDouble(id+"_paper_top", paper_top);
		prefs.putDouble(id+"_paper_bottom", paper_bottom);
	}
	
	private void GetRecentPaperSize() {
		String id = Long.toString(robot_uid);
		paper_left=Double.parseDouble(prefs.get(id+"_paper_left","0"));
		paper_right=Double.parseDouble(prefs.get(id+"_paper_right","0"));
		paper_top=Double.parseDouble(prefs.get(id+"_paper_top","0"));
		paper_bottom=Double.parseDouble(prefs.get(id+"_paper_bottom","0"));
	}
	
	public boolean IsPaperConfigured() {
		return (paper_top>paper_bottom && paper_right>paper_left);
	}
	
	public void ParseRobotUID(String line) {
		// get the UID reported by the robot
		String[] lines = line.split("\\r?\\n");
		if(lines.length>0) {
			try {
				robot_uid = Long.parseLong(lines[0]);
			}
			catch(NumberFormatException e) {}
		}
		
		// new robots have UID=0
		if(robot_uid==0) GetNewRobotUID();

		// load machine specific config
		GetRecentPaperSize();
		LoadConfig();
		if(limit_top==0 && limit_bottom==0 && limit_left==0 && limit_right==0) {
			AdjustMachineSize();
		}
	}
	
	/**
	 * based on http://www.exampledepot.com/egs/java.net/Post.html
	 */
	private void GetNewRobotUID() {
		try {
		    // Send data
			URL url = new URL("http://marginallyclever.com/drawbot_getuid.php");
		    URLConnection conn = url.openConnection();
		    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		    robot_uid = Long.parseLong(rd.readLine());
		    rd.close();
		} catch (Exception e) {}

		// did read go ok?
		if(robot_uid!=0) {
			Makelangelo.getSingleton().SendLineToRobot("UID "+robot_uid);
		}
	}
	
	public double GetPaperWidth() {
		return paper_right - paper_left;
	}
	
	public double GetPaperHeight() {
		return paper_top -paper_bottom;
	}
	
	public double GetPaperScale() {
		double paper_w=GetPaperWidth();
		double paper_h=GetPaperHeight();
		
		if(paper_w>paper_h) {
			return paper_h/paper_w;
		} else {
			return paper_w/paper_h;
		}
	}
	
	public double GetFeedRate() {
		return default_feed_rate;
	}
	
	public void SetFeedRate(double f) {
		default_feed_rate = f;
	}
	
	public long GetUID() {
		return robot_uid;
	}
}
