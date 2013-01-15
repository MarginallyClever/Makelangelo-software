/**@(#)drawbotGUI.java
 *
 * drawbot application with GUI
 *
 * @author Dan Royer (dan@marginallyclever.com)
 * @version 1.00 2012/2/28
 */


// io functions
import gnu.io.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.prefs.Preferences;

//@TODO: in-app gcode editing with immediate visusal feedback - only while not drawing
//@TODO: image processing options - cutoff, exposure, resolution
//@TODO: vector output?

public class DrawbotGUI
		extends JPanel
		implements ActionListener, SerialPortEventListener
{
	static final long serialVersionUID=1;
	
	private static DrawbotGUI singletonObject;
	   
	// Serial connection
	private static int BAUD_RATE = 57600;
	private CommPortIdentifier portIdentifier;
	private CommPort commPort;
	private SerialPort serialPort;
	private InputStream in;
	private OutputStream out;
	private String[] portsDetected;
	private boolean portOpened=false;
	private boolean portConfirmed=false;
	
	// Serial communication
	static private final String cue = "> ";
	static private final String eol = ";";
	static private final String NL = System.getProperty("line.separator");
	static private final String hello = "HELLO WORLD! I AM DRAWBOT #";
	
	// Preferences
	private Preferences prefs = Preferences.userRoot().node("DrawBot");
	private String[] recentFiles;
	private String recentPort;
	private boolean allowMetrics=true;
	
	// Metrics
	PublishImage reportImage = new PublishImage();
	DistanceMetric reportDistance = new DistanceMetric();
	
	// Robot config
	private long   robot_uid=0;
	
	private double limit_top=10;
	private double limit_bottom=-10;
	private double limit_left=-10;
	private double limit_right=10;
	
	private int image_dpi;
	
	// paper area (stock material
	private double paper_top=10;
	private double paper_bottom=-10;
	private double paper_left=-10;
	private double paper_right=10;
	private boolean m1invert=false;
	private boolean m2invert=false;
	
	private double bobbin1_diameter=0.95;
	private double bobbin2_diameter=0.95;
	
	// GUI elements
	private static JFrame mainframe;
	private JMenuBar menuBar;
    private JMenuItem buttonOpenFile, buttonExit;
    private JMenuItem buttonConfigurePreferences, buttonConfigureLimits, buttonConfigureBobbins, buttonRescan, buttonJogMotors, buttonImageProcessing;
    private JMenuItem buttonStart, buttonPause, buttonHalt, buttonDriveManually;
    private JMenuItem buttonZoomIn,buttonZoomOut;
    private JMenuItem buttonAbout,buttonCheckForUpdate;
    
    private JMenuItem [] buttonRecent = new JMenuItem[10];
    private JMenuItem [] buttonPorts;

    // logging
    private JTextPane log;
    private JScrollPane logPane;
    HTMLEditorKit kit;
    HTMLDocument doc;
    PrintWriter logToFile;
    
    private DrawPanel previewPane;
	private StatusBar statusBar;
	
	// parsing input from Drawbot
	private String line3="";

	// reading file
	private boolean running=false;
	private boolean paused=true;
	
	GCodeFile gcode = new GCodeFile();
	
	private DrawbotGUI() {
		StartLog();
		LoadConfig();
        GetRecentFiles();
		GetRecentPaperSize();
        GetRecentPort();
	}
	
	protected void finalize() throws Throwable
	{
		//do finalization here
		EndLog();
		super.finalize(); //not necessary if extending Object.
	} 

	private void StartLog() {
		try {
			logToFile = new PrintWriter(new FileWriter("log.html"));
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			logToFile.write("<h3>"+sdf.format(cal.getTime())+"</h3>\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void EndLog() {
		logToFile.close();
	}
	
	public static DrawbotGUI getSingleton() {
		if(singletonObject==null) {
			singletonObject = new DrawbotGUI();
		}
		return singletonObject;
	}
	
	//  data access
	public ArrayList<String> getGcode() {
		return gcode.lines;
	}

	// manages the vertical split in the GUI
	public class Splitter extends JSplitPane {
		static final long serialVersionUID=1;
		
		public Splitter(int split_direction) {
			super(split_direction);
			setResizeWeight(0.9);
			setDividerLocation(0.9);
		}
	}
	
	public void LoadImage(String filename) {
		BufferedImage img;
		
		try {
			img = ImageIO.read(new File(filename));

			Filter_Resize rs = new Filter_Resize(paper_top,paper_bottom,paper_left,paper_right,(double)image_dpi/100.0,0.9f); 
			img = rs.Process(img);
			
			Filter_BlackAndWhite bwc = new Filter_BlackAndWhite(); 
			img = bwc.Process(img);
			
			Filter_DitherFloydSteinberg dither = new Filter_DitherFloydSteinberg();
			img = dither.Process(img);
	
			String ngcPair = filename.substring(0, filename.lastIndexOf('.')) + ".ngc";
			Filter_TSPGcodeGenerator tsp = new Filter_TSPGcodeGenerator(ngcPair);
			tsp.Process(img);
		}
		catch(IOException e) {}
	}

	// appends a message to the log tab and system out.
	public void Log(String msg) {
		try {
			msg=msg.replace("\n", "<br>\n");
			logToFile.write(msg);
			logToFile.flush();
			kit.insertHTML(doc, doc.getLength(), msg, 0, 0, null);
			int over_length = doc.getLength() - msg.length() - 5000;
			doc.remove(0, over_length);
			logPane.getVerticalScrollBar().setValue(logPane.getVerticalScrollBar().getMaximum());
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}
	
	public void ClearLog() {
		try {
			doc.replace(0, doc.getLength(), "", null);
			kit.insertHTML(doc, 0, "", 0,0,null);
			logPane.getVerticalScrollBar().setValue(logPane.getVerticalScrollBar().getMaximum());
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}
	
	public void ClosePort() {
		if(portOpened) {
		    if (serialPort != null) {
		        try {
		            // Close the I/O streams.
		            out.close();
		            in.close();
			        // Close the port.
			        serialPort.removeEventListener();
			        serialPort.close();
		        } catch (IOException e) {
		            // Don't care
		        }
		    }

		    ClearLog();
			portOpened=false;
			portConfirmed=false;
			previewPane.setConnected(false);
			UpdateMenuBar();
		}
	}
	
	// open a serial connection to a device.  We won't know it's the robot until  
	public int OpenPort(String portName) {
		if(portOpened && portName.equals(recentPort)) return 0;
		
		ClosePort();
		
		Log("<font color='green'>Connecting to "+portName+"...</font>\n");
		
		// find the port
		try {
			portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
		}
		catch(Exception e) {
			Log("<span style='color:red'>Ports could not be identified:"+e.getMessage()+"</span>\n");
			e.printStackTrace();
			return 1;
		}

		if ( portIdentifier.isCurrentlyOwned() ) {
    	    Log("<span style='color:red'>Error: Another program is currently using this port."+"</span>\n");
			return 2;
		}

		// open the port
		try {
		    commPort = portIdentifier.open("DrawbotGUI",2000);
		}
		catch(Exception e) {
			Log("Port could not be opened:"+e.getMessage()+NL);
			e.printStackTrace();
			return 3;
		}

	    if( ( commPort instanceof SerialPort ) == false ) {
			Log("<span style='color:red'>Only serial ports are handled by this example."+"</span>\n");
			return 4;
		}

		// set the port parameters (like baud rate)
		serialPort = (SerialPort)commPort;
		try {
			serialPort.setSerialPortParams(BAUD_RATE,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
		}
		catch(Exception e) {
			Log("<span style='color:red'>Port could not be configured:"+e.getMessage()+"</span>\n");
			return 5;
		}

		try {
			in = serialPort.getInputStream();
			out = serialPort.getOutputStream();
		}
		catch(Exception e) {
			Log("<span style='color:red'>Streams could not be opened:"+e.getMessage()+"</span>\n");
			return 6;
		}
		
		try {
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
		}
		catch(TooManyListenersException e) {
			Log("<span style='color:red'>Streams could not be opened:"+e.getMessage()+"</span>\n");
			return 7;
		}

		Log("<span style='green'>Opened.</span>\n");
		SetRecentPort(portName);
		portOpened=true;
		UpdateMenuBar();
		
		return 0;
	}

	/**
	 * Complete the handshake, load robot-specific configuration, update the menu, repaint the preview with the limits.
	 * @return true if handshake succeeds.
	 */
	public boolean ConfirmPort() {
		if(portConfirmed==true) return true;
		int found=line3.lastIndexOf(hello);
		if(found >= 0) {
			portConfirmed=true;
			
			GetRobotUID(line3);
			
			mainframe.setTitle("Makelangelo #"+Long.toString(robot_uid)+" connected");

			// load machine specific config
			GetRecentPaperSize();
			LoadConfig();
			if(limit_top==0 && limit_bottom==0 && limit_left==0 && limit_right==0) {
				ConfigureLimits();
			}

			previewPane.setMachineLimits(limit_top, limit_bottom, limit_left, limit_right);
			SendConfig();
			
			UpdateMenuBar();
			previewPane.setConnected(true);
		}
		return portConfirmed;
	}
	
	private void GetRobotUID(String line3) {
		int found=line3.lastIndexOf(hello);
		
		// get the UID reported by the robot
		String[] lines = line3.substring(found+hello.length()).split("\\r?\\n");
		if(lines.length>0) {
			try {
				robot_uid = Long.parseLong(lines[0]);
			}
			catch(NumberFormatException e) {}
		}
		
		// new robots have UID=0
		if(robot_uid==0) GetNewRobotUID();

		// did read go ok?
		if(robot_uid!=0) {
			reportDistance.SetUID(robot_uid);
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
			SendLineToRobot("UID "+robot_uid);
		}
	}
	
	// find all available serial ports for the settings->ports menu.
	public String[] ListSerialPorts() {
		@SuppressWarnings("unchecked")
	    Enumeration<CommPortIdentifier> ports = (Enumeration<CommPortIdentifier>)CommPortIdentifier.getPortIdentifiers();
	    ArrayList<String> portList = new ArrayList<String>();
	    while (ports.hasMoreElements()) {
	        CommPortIdentifier port = (CommPortIdentifier) ports.nextElement();
	        if (port.getPortType() == CommPortIdentifier.PORT_SERIAL) {
	        	portList.add(port.getName());
	        }
	    }
	    portsDetected = (String[]) portList.toArray(new String[0]);
	    return portsDetected;
	}
	
	// pull the last connected port from prefs
	public void GetRecentPort() {
		recentPort = prefs.get("recent-port", "");
	}
	
	// update the prefs with the last port connected and refreshes the menus.
	// @TODO: only update when the port is confirmed?
	public void SetRecentPort(String portName) {
		prefs.put("recent-port", portName);
		recentPort=portName;
		UpdateMenuBar();
	}
	
	// save paper limits
	public void SetRecentPaperSize() {
		String id=Long.toString(robot_uid);
		prefs.putDouble(id+"_paper_left", paper_left);
		prefs.putDouble(id+"_paper_right", paper_right);
		prefs.putDouble(id+"_paper_top", paper_top);
		prefs.putDouble(id+"_paper_bottom", paper_bottom);
		previewPane.setPaperSize(paper_top,paper_bottom,paper_left,paper_right);
	}
	
	public void GetRecentPaperSize() {
		String id = Long.toString(robot_uid);
		paper_left=Double.parseDouble(prefs.get(id+"_paper_left","0"));
		paper_right=Double.parseDouble(prefs.get(id+"_paper_right","0"));
		paper_top=Double.parseDouble(prefs.get(id+"_paper_top","0"));
		paper_bottom=Double.parseDouble(prefs.get(id+"_paper_bottom","0"));
	}
	
	/**
	 * Opens a file.  If the file can be opened, get a drawing time estimate, update recent files list, and repaint the preview tab.
	 * @param filename what file to open
	 */
	public void LoadGCode(String filename) {
		try {
			gcode.Load(filename);
			gcode.EstimateDrawTime();
		   	Log("<font color='green'>"+gcode.estimate_count + " line segments.\n"+gcode.estimated_length+ "cm of line.\n" +
			   		"Estimated "+statusBar.formatTime((long)(gcode.estimated_time))+"s to draw.</font>\n");
			
	    }
	    catch(IOException e) {
	    	Log("<span style='color:red'>File "+filename+" could not be opened.</span>\n");
	    	RemoveRecentFile(filename);
	    	return;
	    }
	    
	    previewPane.setGCode(gcode.lines);
	   	UpdateRecentFiles(filename);
	    Halt();
	}
	
	/**
	 * changes the order of the recent files list in the File submenu, saves the updated prefs, and refreshes the menus.
	 * @param filename the file to push to the top of the list.
	 */
	public void UpdateRecentFiles(String filename) {
		int cnt = recentFiles.length;
		String [] newFiles = new String[cnt];
		
		newFiles[0]=filename;
		
		int i,j=1;
		for(i=0;i<cnt;++i) {
			if(!filename.equals(recentFiles[i]) && recentFiles[i] != "") {
				newFiles[j++] = recentFiles[i];
				if(j == cnt ) break;
			}
		}

		recentFiles=newFiles;

		// update prefs
		for(i=0;i<cnt;++i) {
			if( recentFiles[i]!=null && !recentFiles[i].isEmpty()) {
				prefs.put("recent-files-"+i, recentFiles[i]);
			}
		}
		
		UpdateMenuBar();
	}
	
	// A file failed to load.  Remove it from recent files, refresh the menu bar.
	public void RemoveRecentFile(String filename) {
		int i;
		for(i=0;i<recentFiles.length-1;++i) {
			if(recentFiles[i]==filename) {
				break;
			}
		}
		for(;i<recentFiles.length-1;++i) {
			recentFiles[i]=recentFiles[i+1];
		}
		recentFiles[recentFiles.length-1]="";

		// update prefs
		for(i=0;i<recentFiles.length;++i) {
			if(recentFiles[i]!=null && !recentFiles[i].isEmpty()) {
				prefs.put("recent-files-"+i, recentFiles[i]);
			}
		}
		
		UpdateMenuBar();
	}
	
	// Load recent files from prefs
	public void GetRecentFiles() {
		recentFiles = new String[10];
		
		int i;
		for(i=0;i<recentFiles.length;++i) {
			recentFiles[i] = prefs.get("recent-files-"+i, "");
		}
	}	
	
	public boolean IsFileGcode(String filename) {
		String ext=filename.substring(filename.lastIndexOf('.'));
    	return (ext.equalsIgnoreCase(".ngc") || ext.equalsIgnoreCase(".gc"));
	}
	
	// User has asked that a file be opened.
	public void OpenFileOnDemand(String filename) {
		Log("<font color='green'>Opening file "+recentFiles[0]+"...</font>\n");
		
		if(IsFileGcode(filename)) {
			LoadGCode(filename);
    	} else {
    		LoadImage(filename);
    	}
    	
    	previewPane.ZoomToFitPaper();

    	statusBar.Clear();
	}

	// creates a file open dialog. If you don't cancel it opens that file.
	public void OpenFileDialog() {
	    // Note: source for ExampleFileFilter can be found in FileChooserDemo,
	    // under the demo/jfc directory in the Java 2 SDK, Standard Edition.
		String filename = (recentFiles[0].length()>0) ? filename=recentFiles[0] : "";

		FileFilter filterImage  = new FileNameExtensionFilter("Images (jpg/bmp/png/gif)", "jpg", "jpeg", "png", "wbmp", "bmp", "gif");
		FileFilter filterGCODE = new FileNameExtensionFilter("GCODE files (ngc)", "ngc");
		 
		JFileChooser fc = new JFileChooser(new File(filename));
		fc.addChoosableFileFilter(filterImage);
		fc.addChoosableFileFilter(filterGCODE);
	    if(fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
	    	String selectedFile=fc.getSelectedFile().getAbsolutePath();
	    	if(!IsFileGcode(selectedFile)) {
	    		// if machine is not yet calibrated
	    		if(paper_top<=paper_bottom || paper_right<=paper_left) {
	    			JOptionPane.showMessageDialog(null,"Please set a paper size before importing an image.  Paper size is set in Settings > Configure Limits.");
	    			return;
	    		}
	    	}
	    	OpenFileOnDemand(selectedFile);
	    }
	}
	
	public void GoHome() {
		SendLineToRobot("G00 X0 Y0 Z90");
	}
	
	/**
	 * Adjust preferences
	 */
	public void ConfigurePreferences() {
		final JDialog driver = new JDialog(mainframe,"Preferences",true);
		driver.setLayout(new GridBagLayout());
		
		final JTextField allow_metrics = new JTextField(String.valueOf(allowMetrics));
		final JTextField twitter_name = new JTextField(String.valueOf(reportImage.GetName()));
		final JPasswordField twitter_pass = new JPasswordField(String.valueOf(reportImage.GetPass()));

		final JButton cancel = new JButton("Cancel");
		final JButton save = new JButton("Save");
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth=3;  
		c.gridx=0;  c.gridy=0;  driver.add(new JLabel("Can I add the distance drawn to the global total?"),c);
		c.gridwidth=1;
		c.gridx=0;  c.gridy=1;  driver.add(allow_metrics);
		c.gridx=1;  c.gridy=1;  driver.add(new JLabel("Yes, please!"),c);
		
		c.gridx=0;  c.gridy=2;  driver.add(new JLabel("Can I tweet the converted pictures?"),c);
		
		c.gridx=0;  c.gridy=2;  driver.add(new JLabel("Twitter account"),c);
		c.gridx=0;  c.gridy=3;  driver.add(new JLabel("Twitter password"),c);
		c.gridx=2;  c.gridy=4; driver.add(cancel,c);
		c.gridx=1;  c.gridy=4; driver.add(save,c);

		ActionListener driveButtons = new ActionListener() {
			  public void actionPerformed(ActionEvent e) {
					Object subject = e.getSource();
					if(subject == save) {
						allowMetrics = Boolean.parseBoolean(allow_metrics.getText());
						reportImage.SetTwitter(twitter_name.getText(),new String(twitter_pass.getPassword()));
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
	
	/**
	 * Open the config dialog, send the config update to the robot, save it for future, and refresh the preview tab.
	 */
	public void ConfigureLimits() {
		final JDialog driver = new JDialog(mainframe,"Configure Limits",true);
		driver.setLayout(new GridBagLayout());

		final JTextField mtop = new JTextField(String.valueOf(limit_top));
		final JTextField mbottom = new JTextField(String.valueOf(limit_bottom));
		final JTextField mleft = new JTextField(String.valueOf(limit_left));
		final JTextField mright = new JTextField(String.valueOf(limit_right));
		
		final JTextField ptop = new JTextField(String.valueOf(paper_top));
		final JTextField pbottom = new JTextField(String.valueOf(paper_bottom));
		final JTextField pleft = new JTextField(String.valueOf(paper_left));
		final JTextField pright = new JTextField(String.valueOf(paper_right));

		final JButton cancel = new JButton("Cancel");
		final JButton save = new JButton("Save");

		BufferedImage myPicture = null;
		try {
			
			myPicture = ImageIO.read(DrawbotGUI.class.getResourceAsStream("limits.png"));
		}
		catch(IOException e) {}
		JLabel picLabel = new JLabel(new ImageIcon( myPicture ));
		
		GridBagConstraints c = new GridBagConstraints();
		GridBagConstraints d = new GridBagConstraints();

		c.weightx=0.25;
		c.gridx=0;  c.gridy=0;  c.gridwidth=4;  c.gridheight=4; c.anchor=GridBagConstraints.CENTER;  driver.add( picLabel,c );
		
		c.gridheight=1; c.gridwidth=1;  c.anchor=GridBagConstraints.EAST;
		d.anchor=GridBagConstraints.WEST;
		
		c.gridx=0;  c.gridy=5;  driver.add(new JLabel("A"),c);		d.gridx=1;	d.gridy=5;	driver.add(mtop,d);
		c.gridx=0;  c.gridy=6;  driver.add(new JLabel("B"),c);		d.gridx=1;	d.gridy=6;	driver.add(mright,d);
		c.gridx=0;  c.gridy=7;  driver.add(new JLabel("C"),c);		d.gridx=1;	d.gridy=7;	driver.add(mbottom,d);
		c.gridx=0;  c.gridy=8;  driver.add(new JLabel("D"),c);		d.gridx=1;	d.gridy=8;	driver.add(mleft,d);
		c.gridx=2;  c.gridy=5;  driver.add(new JLabel("E"),c);		d.gridx=3;	d.gridy=5;	driver.add(ptop,d);
		c.gridx=2;  c.gridy=6;  driver.add(new JLabel("F"),c);		d.gridx=3;	d.gridy=6;	driver.add(pright,d);
		c.gridx=2;  c.gridy=7;  driver.add(new JLabel("G"),c);		d.gridx=3;	d.gridy=7;	driver.add(pbottom,d);
		c.gridx=2;  c.gridy=8;  driver.add(new JLabel("H"),c);		d.gridx=3;	d.gridy=8;	driver.add(pleft,d);

		c.anchor=GridBagConstraints.WEST;
		c.gridx=0;  c.gridy=9;  c.gridwidth=4;  c.gridheight=1;
		driver.add(new JLabel("For more info see http://bit.ly/fix-this-link."),c);
		c.gridx=0;  c.gridy=10;  c.gridwidth=4;  c.gridheight=1;
		driver.add(new JLabel("C, D, G, and H should probably be negative."),c);
		c.gridx=0;  c.gridy=11;  c.gridwidth=4;  c.gridheight=1;
		driver.add(new JLabel("All values in cm."),c);

		
		c.anchor=GridBagConstraints.EAST;
		c.gridy=12;
		c.gridx=3;  c.gridwidth=1;  driver.add(cancel,c);
		c.gridx=2;  c.gridwidth=1;  driver.add(save,c);

		Dimension s=ptop.getPreferredSize();
		s.width=80;
		ptop.setPreferredSize(s);
		pbottom.setPreferredSize(s);
		pleft.setPreferredSize(s);
		pright.setPreferredSize(s);
		mtop.setPreferredSize(s);
		mbottom.setPreferredSize(s);
		mleft.setPreferredSize(s);
		mright.setPreferredSize(s);
		
		ActionListener driveButtons = new ActionListener() {
			  public void actionPerformed(ActionEvent e) {
					Object subject = e.getSource();
					if(subject == save) {
						paper_top = Float.valueOf(ptop.getText());
						paper_bottom = Float.valueOf(pbottom.getText());
						paper_right = Float.valueOf(pright.getText());
						paper_left = Float.valueOf(pleft.getText());
						limit_top = Float.valueOf(mtop.getText());
						limit_bottom = Float.valueOf(mbottom.getText());
						limit_right = Float.valueOf(mright.getText());
						limit_left = Float.valueOf(mleft.getText());
						boolean data_is_sane=true;
						if( limit_right <= limit_left ) data_is_sane=false;
						if( limit_top <= limit_bottom ) data_is_sane=false;
						if( paper_right <= paper_left ) data_is_sane=false;
						if( paper_top <= paper_bottom ) data_is_sane=false;
						if(data_is_sane) {
							previewPane.setMachineLimits(limit_top, limit_bottom, limit_left, limit_right);
							previewPane.setPaperSize(paper_top,paper_bottom,paper_left,paper_right);
							SetRecentPaperSize();
							SaveConfig();
							SendConfig();
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
		SendLineToRobot("M114");  // "where" command
		driver.pack();
		driver.setVisible(true);
	}
	
	/**
	 * Open the config dialog, send the config update to the robot, and save it for future.
	 */
	public void ConfigureBobbins() {
		final JDialog driver = new JDialog(mainframe,"Configure Bobbins",true);
		driver.setLayout(new GridBagLayout());

		final JTextField mBobbin1 = new JTextField(String.valueOf(bobbin1_diameter));
		final JTextField mBobbin2 = new JTextField(String.valueOf(bobbin2_diameter));

		final JButton cancel = new JButton("Cancel");
		final JButton save = new JButton("Save");

		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;  c.gridy=1;  driver.add(new JLabel("Left"),c);
		c.gridx=0;  c.gridy=2;  driver.add(new JLabel("Right"),c);
		
		c.gridx=1;  c.gridy=0;  driver.add(new JLabel("Diameter"),c);
		c.gridx=1;	c.gridy=1;	driver.add(mBobbin1,c);
		c.gridx=1;	c.gridy=2;	driver.add(mBobbin2,c);

		c.gridx=0;  c.gridy=3;  c.gridwidth=2;
		driver.add(new JLabel("All values in cm."),c);

		c.gridx=1;  c.gridy=4;  driver.add(save,c);
		c.gridx=2;  c.gridy=4;  driver.add(cancel,c);

		Dimension s=mBobbin1.getPreferredSize();
		s.width=80;
		mBobbin1.setPreferredSize(s);
		mBobbin2.setPreferredSize(s);
		
		ActionListener driveButtons = new ActionListener() {
			  public void actionPerformed(ActionEvent e) {
					Object subject = e.getSource();
					if(subject == save) {
						bobbin1_diameter = Float.valueOf(mBobbin1.getText());
						bobbin2_diameter = Float.valueOf(mBobbin2.getText());
						boolean data_is_sane=true;
						if( bobbin1_diameter <= 0 ) data_is_sane=false;
						if( bobbin2_diameter <= 0 ) data_is_sane=false;
						if(data_is_sane) {
							SaveConfig();
							SendConfig();
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

	void LoadConfig() {
		String id=Long.toString(robot_uid);
		limit_top = Double.valueOf(prefs.get(id+"_limit_top", "0"));
		limit_bottom = Double.valueOf(prefs.get(id+"_limit_bottom", "0"));
		limit_left = Double.valueOf(prefs.get(id+"_limit_left", "0"));
		limit_right = Double.valueOf(prefs.get(id+"_limit_right", "0"));
		m1invert=Boolean.parseBoolean(prefs.get(id+"_m1invert", "false"));
		m2invert=Boolean.parseBoolean(prefs.get(id+"_m2invert", "false"));
		image_dpi=Integer.parseInt(prefs.get(id+"_image_dpi","100"));
		bobbin1_diameter=Double.valueOf(prefs.get(id+"_bobbin1_diameter", "0.95"));
		bobbin2_diameter=Double.valueOf(prefs.get(id+"_bobbin2_diameter", "0.95"));
	}

	void SaveConfig() {
		String id=Long.toString(robot_uid);
		prefs.put(id+"_limit_top", Double.toString(limit_top));
		prefs.put(id+"_limit_bottom", Double.toString(limit_bottom));
		prefs.put(id+"_limit_right", Double.toString(limit_right));
		prefs.put(id+"_limit_left", Double.toString(limit_left));
		prefs.put(id+"_m1invert",Boolean.toString(m1invert));
		prefs.put(id+"_m2invert",Boolean.toString(m2invert));
		prefs.put(id+"_bobbin1_diameter", Double.toString(bobbin1_diameter));
		prefs.put(id+"_bobbin2_diameter", Double.toString(bobbin2_diameter));
	}
	
	void SendConfig() {
		if(!portConfirmed) return;
		
		// Send a command to the robot with new configuration values
		String line="CONFIG T"+limit_top
				   +" B"+limit_bottom
				   +" L"+limit_left
				   +" R"+limit_right
                   +" I"+(m1invert?"-1":"1")
                   +" J"+(m2invert?"-1":"1");
		SendLineToRobot(line);
		SendLineToRobot("TELEPORT X0 Y0 Z0");
	}

	// Take the next line from the file and send it to the robot, if permitted. 
	public void SendFileCommand() {
		if(running==false || paused==true || gcode.fileOpened==false || portConfirmed==false || gcode.linesProcessed>=gcode.linesTotal) return;
		
		String line;
		do {			
			// are there any more commands?
			// @TODO: find out how far the pen moved each line and add it to the distance total.
			line=gcode.lines.get((int)gcode.linesProcessed++).trim();
			previewPane.setLinesProcessed(gcode.linesProcessed);
			statusBar.SetProgress(gcode.linesProcessed, gcode.linesTotal);
			// loop until we find a line that gets sent to the robot, at which point we'll
			// pause for the robot to respond.  Also stop at end of file.
		} while(ProcessLine(line) && gcode.linesProcessed<gcode.linesTotal);
		
		if(gcode.linesProcessed==gcode.linesTotal) {
			// end of file
			Halt();
		}
	}
	
	private void ChangeToTool(String toolName) {
		JOptionPane.showMessageDialog(null,"Please change to tool "+toolName+" and click OK.");
	}
	
	/**
	 * removes comments, processes commands drawbot shouldn't have to handle.
	 * @param line command to send
	 * @return true if the robot is ready for another command to be sent.
	 */
	public boolean ProcessLine(String line) {
		// tool change request?
		String [] tokens = line.split("\\s");

		// tool change?
		if(Arrays.asList(tokens).contains("M06") || Arrays.asList(tokens).contains("M6")) {
			for(int i=0;i<tokens.length;++i) {
				if(tokens[i].startsWith("T")) {
					ChangeToTool(tokens[i].substring(1));
				}
			}
			// still ready to send
			return true;
		}
		
		// end of program?
		if(tokens[0]=="M02" || tokens[0]=="M2" || tokens[0]=="M30") {
			Halt();
			return false;
		}

		if(Arrays.asList(tokens).contains("M18")) {
		  // Handle M18 (disable motors)
		  SendLineToRobot(line);
		  return true;
        }
		
		// other machine code to ignore?
		if(tokens[0].startsWith("M")) {
			Log("<font color='pink'>"+line+"</font>\n");
			return true;
		} 

		// contains a comment?  if so remove it
		int index=line.indexOf('(');
		if(index!=-1) {
			String comment=line.substring(index+1,line.lastIndexOf(')'));
			line=line.substring(0,index).trim();
			Log("<font color='grey'>* "+comment+"</font\n");
			if(line.length()==0) {
				// entire line was a comment.
				return true;  // still ready to send
			}
		}

		// send relevant part of line to the robot

		SendLineToRobot(line);
		
		return false;
	}
	/**
	 * Sends a single command the robot.  Could be anything.
	 * @param line command to send.
	 * @return true means the command is sent.  false means it was not.
	 */
	public void SendLineToRobot(String line) {
		if(!portConfirmed) return;
		
		line+=eol;
		Log("<font color='white'>"+line+"</font>");
		try {
			out.write(line.getBytes());
		}
		catch(IOException e) {}
	}
	
	/**
	 * stop sending file commands to the robot.
	 * @todo add an e-stop command?
	 */
	public void Halt() {
		running=false;
		paused=false;
		gcode.linesProcessed=0;
	    previewPane.setLinesProcessed(0);
		previewPane.setRunning(running);
		UpdateMenuBar();
	}
	
	// The user has done something.  respond to it.
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();
		
		if(subject == buttonZoomIn) {
			previewPane.ZoomIn();
			return;
		}
		if(subject == buttonZoomOut) {
			previewPane.ZoomOut();
			return;
		}
		if(subject == buttonOpenFile) {
			OpenFileDialog();
			return;
		}

		if( subject == buttonStart ) {
			if(gcode.fileOpened) {
				paused=false;
				running=true;
				UpdateMenuBar();
				gcode.linesProcessed=0;
				previewPane.setRunning(running);
				previewPane.setLinesProcessed(gcode.linesProcessed);
				statusBar.Start();
				SendFileCommand();
			}
			return;
		}
		if( subject == buttonPause ) {
			if(running) {
				if(paused==true) {
					buttonPause.setText("Pause");
					paused=false;
					// @TODO: if the robot is not ready to unpause, this might fail and the program would appear to hang.
					SendFileCommand();
				} else {
					buttonPause.setText("Unpause");
					paused=true;
				}
			}
			return;
		}
		if( subject == buttonDriveManually ) {
			DriveManually();
			return;
		}
		if( subject == buttonImageProcessing ) {
			ImageProcessing();
			return;
		}
		if( subject == buttonHalt ) {
			Halt();
			return;
		}
		if( subject == buttonRescan ) {
			ListSerialPorts();
			UpdateMenuBar();
			return;
			
		}
		if( subject == buttonConfigurePreferences ) {
			ConfigurePreferences();
			return;
		}
		if( subject == buttonConfigureLimits ) {
			ConfigureLimits();
			return;
		}
		if( subject == buttonConfigureBobbins ) {
			ConfigureBobbins();
			return;
		}
		if( subject == buttonJogMotors ) {
			JogMotors();
			return;
		}
		if( subject == buttonAbout ) {
			JOptionPane.showMessageDialog(null,"Makelangelo v149\n\n"
					+"Created by Dan Royer (dan@marginallyclever.com).\n\n"
					+"Get the latest version and read the documentation @ http://github.com/i-make-robots/DrawBot/\n"
					+"Find out more at http://www.marginallyclever.com/\n");
			return;
		}
		if( subject == buttonCheckForUpdate ) {
			CheckForUpdate();
			return;
		}
		if( subject == buttonExit ) {
			System.exit(0);  // @TODO: be more graceful?
			return;
		}
		
		int i;
		for(i=0;i<10;++i) {
			if(subject == buttonRecent[i]) {
				OpenFileOnDemand(recentFiles[i]);
				return;
			}
		}

		for(i=0;i<portsDetected.length;++i) {
			if(subject == buttonPorts[i]) {
				OpenPort(portsDetected[i]);
				return;
			}
		}
	}
	
	// Deal with something robot has sent.
	public void serialEvent(SerialPortEvent events) {
        switch (events.getEventType()) {
            case SerialPortEvent.DATA_AVAILABLE:
	            try {
	            	final byte[] buffer = new byte[1024];
					int len = in.read(buffer);
					if( len>0 ) {
						String line2 = new String(buffer,0,len);
						Log("<span style='color:#FFA500'>"+line2.replace("\n","<br>")+"</span>");
						line3+=line2;
						// wait for the cue ("> ") to send another command
						if(line3.lastIndexOf(cue)!=-1) {
							if(ConfirmPort()) {
								line3="";
								SendFileCommand();
							}
						}
					}
	            } catch (IOException e) {}
                break;
        }
    }

	public void CheckForUpdate() {
		/*
		try {
		    // Ping github for the latest version.
			URL url = new URL("http://marginallyclever.com/drawbot_get.php");
		    URLConnection conn = url.openConnection();
		    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		    robot_uid = Long.parseLong(rd.readLine());
		    rd.close();
		} catch (Exception e) {}
		 */
		// TODO Get latest version
		// TODO Offer to download latest version?
	}
	
	/**
	 * Open the config dialog, update the paper size, refresh the preview tab.
	 */
	public void DriveManually() {
		JDialog driver = new JDialog(mainframe,"Manual Control",true);
		driver.setLayout(new GridBagLayout());

		JButton find = new JButton("FIND HOME");
		JButton home = new JButton("GO HOME");
		JButton center = new JButton("THIS IS HOME");
		
		JButton up1 = new JButton("Y1");
		JButton up10 = new JButton("Y10");
		JButton up100 = new JButton("Y100");
		
		JButton down1 = new JButton("Y-1");
		JButton down10 = new JButton("Y-10");
		JButton down100 = new JButton("Y-100");
		
		JButton left1 = new JButton("X-1");
		JButton left10 = new JButton("X-10");
		JButton left100 = new JButton("X-100");
		
		JButton right1 = new JButton("X1");
		JButton right10 = new JButton("X10");
		JButton right100 = new JButton("X100");
		
		JButton TL = new JButton("GO TL");
		JButton TR = new JButton("GO TR");
		JButton BL = new JButton("GO BL");
		JButton BR = new JButton("GO BR");

		JButton z90 = new JButton("Z90");
		JButton z0  = new JButton("Z0");
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=1;  c.gridy=1;  driver.add(TL,c);
		c.gridx=5;  c.gridy=1;  driver.add(TR,c);
		c.gridx=1;  c.gridy=5;  driver.add(BL,c);
		c.gridx=5;  c.gridy=5;  driver.add(BR,c);
		
		c.gridx=3;	c.gridy=0;	driver.add(up100,c);
		c.gridx=3;	c.gridy=1;	driver.add(up10,c);
		c.gridx=3;	c.gridy=2;	driver.add(up1,c);
		c.gridx=3;	c.gridy=4;	driver.add(down1,c);
		c.gridx=3;	c.gridy=5;	driver.add(down10,c);
		c.gridx=3;	c.gridy=6;	driver.add(down100,c);

		c.gridx=0;	c.gridy=3;	driver.add(left100,c);
		c.gridx=1;	c.gridy=3;	driver.add(left10,c);
		c.gridx=2;	c.gridy=3;	driver.add(left1,c);
		c.gridx=4;	c.gridy=3;	driver.add(right1,c);
		c.gridx=5;	c.gridy=3;	driver.add(right10,c);
		c.gridx=6;	c.gridy=3;	driver.add(right100,c);

		c.gridx=3;	c.gridy=3;	driver.add(home,c);
		c.gridx=7;  c.gridy=0;  driver.add(center,c);
		c.gridx=7;  c.gridy=1;  driver.add(find,c);
		c.gridx=7;  c.gridy=3;  driver.add(z90,c);
		c.gridx=7;  c.gridy=4;  driver.add(z0,c);
		
		ActionListener driveButtons = new ActionListener() {
			  public void actionPerformed(ActionEvent e) {
					Object subject = e.getSource();
					JButton b = (JButton)subject;
					String t=b.getText();
					if(t=="GO HOME") {
						GoHome();
						SendLineToRobot("M114");
					} else if(t=="GO TL") { 
						SendLineToRobot("G00 X"+(paper_left *10)+" Y"+(paper_top*10));
					} else if(t=="GO TR") { 
						SendLineToRobot("G00 X"+(paper_right*10)+" Y"+(paper_top*10));
					} else if(t=="GO BL") { 
						SendLineToRobot("G00 X"+(paper_left *10)+" Y"+(paper_bottom*10));
					} else if(t=="GO BR") { 
						SendLineToRobot("G00 X"+(paper_right*10)+" Y"+(paper_bottom*10));
					} else if(t=="FIND HOME") {
						SendLineToRobot("G28");
					} else if(t=="THIS IS HOME") {
						SendLineToRobot("TELEPORT XO YO");
					} else if(t=="Z90" || t=="Z0") {
						SendLineToRobot("G00 "+b.getText());
					} else {
						SendLineToRobot("G91");
						SendLineToRobot("G00 G21 "+b.getText());
						SendLineToRobot("G90");
						SendLineToRobot("M114");
					}
			  }
			};
		
		up1.addActionListener(driveButtons);
		up10.addActionListener(driveButtons);
		up100.addActionListener(driveButtons);
		down1.addActionListener(driveButtons);
		down10.addActionListener(driveButtons);
		down100.addActionListener(driveButtons);
		left1.addActionListener(driveButtons);
		left10.addActionListener(driveButtons);
		left100.addActionListener(driveButtons);
		right1.addActionListener(driveButtons);
		right10.addActionListener(driveButtons);
		right100.addActionListener(driveButtons);
		z90.addActionListener(driveButtons);
		z0.addActionListener(driveButtons);
		center.addActionListener(driveButtons);
		home.addActionListener(driveButtons);
		find.addActionListener(driveButtons);
		TL.addActionListener(driveButtons);
		TR.addActionListener(driveButtons);
		BL.addActionListener(driveButtons);
		BR.addActionListener(driveButtons);
		SendLineToRobot("M114");
		driver.pack();
		driver.setVisible(true);
	}
	
	/**
	 * open the Image Processing dialog, update automatic image processing.  Could be expanded into a live preview of the dithered image?
	 */
	public void ImageProcessing() {
		final JDialog driver = new JDialog(mainframe,"Image Processing",true);
		driver.setLayout(new GridBagLayout());

		final JSlider input_image_dpi = new JSlider(JSlider.HORIZONTAL, 1, 100, image_dpi);
		input_image_dpi.setMajorTickSpacing(25);
		input_image_dpi.setMinorTickSpacing(5);
		input_image_dpi.setPaintTicks(true);
		input_image_dpi.setPaintLabels(true);
		final JButton cancel = new JButton("Cancel");
		final JButton save = new JButton("Save");

		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;  c.gridy=0;  driver.add(new JLabel("Image conversion DPI (Default: xxx)"));
		c.gridx=1;	c.gridy=0;	driver.add(input_image_dpi,c);
		c.gridx=2;  c.gridy=2;  driver.add(save,c);
		c.gridx=3;  c.gridy=2;  driver.add(cancel,c);
		
		ActionListener driveButtons = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Object subject = e.getSource();
					if(subject == save) {
						int image_dpi2 = input_image_dpi.getValue();
						boolean data_is_sane=true;
						if( image_dpi2 < 1 || image_dpi2 > 64 ) data_is_sane=false;
						if(data_is_sane) {
							image_dpi=image_dpi2;
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
	
	protected void JogMotors() {
		JDialog driver = new JDialog(mainframe,"Jog Motors",true);
		driver.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		final JButton buttonAneg = new JButton("IN");
		final JButton buttonApos = new JButton("OUT");
		final JCheckBox m1i = new JCheckBox("Invert",m1invert);
		
		final JButton buttonBneg = new JButton("IN");
		final JButton buttonBpos = new JButton("OUT");
		final JCheckBox m2i = new JCheckBox("Invert",m2invert);

		c.gridx=0;	c.gridy=0;	driver.add(new JLabel("L"),c);
		c.gridx=0;	c.gridy=1;	driver.add(new JLabel("R"),c);
		
		c.gridx=1;	c.gridy=0;	driver.add(buttonAneg,c);
		c.gridx=1;	c.gridy=1;	driver.add(buttonBneg,c);
		
		c.gridx=2;	c.gridy=0;	driver.add(buttonApos,c);
		c.gridx=2;	c.gridy=1;	driver.add(buttonBpos,c);

		c.gridx=3;	c.gridy=0;	driver.add(m1i,c);
		c.gridx=3;	c.gridy=1;	driver.add(m2i,c);
		
		ActionListener driveButtons = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object subject = e.getSource();
				if(subject == buttonApos) SendLineToRobot("D00 L100");
				if(subject == buttonAneg) SendLineToRobot("D00 L-100");
				if(subject == buttonBpos) SendLineToRobot("D00 R100");
				if(subject == buttonBneg) SendLineToRobot("D00 R-100");
				SendLineToRobot("M114");
			}
		};

		ActionListener invertButtons = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m1invert = m1i.isSelected();
				m2invert = m2i.isSelected();
				
				SaveConfig();
				SendConfig();
			}
		};
		
		buttonApos.addActionListener(driveButtons);
		buttonAneg.addActionListener(driveButtons);
		
		buttonBpos.addActionListener(driveButtons);
		buttonBneg.addActionListener(driveButtons);
		
		m1i.addActionListener(invertButtons);
		m2i.addActionListener(invertButtons);

		SendLineToRobot("M114");
		driver.pack();
		driver.setVisible(true);
	}
	
	public JMenuBar CreateMenuBar() {
        // If the menu bar exists, empty it.  If it doesn't exist, create it.
        menuBar = new JMenuBar();

        UpdateMenuBar();
        
        return menuBar;
	}
	
	// Rebuild the contents of the menu based on current program state
	public void UpdateMenuBar() {
		JMenu menu;
        int i;
        
        menuBar.removeAll();
        
        //Build the first menu.
        menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menu);
 
        buttonOpenFile = new JMenuItem("Open File...",KeyEvent.VK_O);
        buttonOpenFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.ALT_MASK));
        buttonOpenFile.getAccessibleContext().setAccessibleDescription("Open a g-code file...");
        buttonOpenFile.addActionListener(this);
        menu.add(buttonOpenFile);

        menu.addSeparator();

        // list recent files
        if(recentFiles != null && recentFiles.length>0) {
        	// list files here
        	for(i=0;i<recentFiles.length;++i) {
        		if(recentFiles[i] == null || recentFiles[i].length()==0) break;
            	buttonRecent[i] = new JMenuItem((1+i) + " "+recentFiles[i],KeyEvent.VK_1+i);
            	if(buttonRecent[i]!=null) {
            		buttonRecent[i].addActionListener(this);
            		menu.add(buttonRecent[i]);
            	}
        	}
        	if(i!=0) menu.addSeparator();
        }

        buttonExit = new JMenuItem("Exit",KeyEvent.VK_Q);
        buttonExit.getAccessibleContext().setAccessibleDescription("Goodbye...");
        buttonExit.addActionListener(this);
        menu.add(buttonExit);

        menuBar.add(menu);

        // settings menu
        menu = new JMenu("Settings");
        menu.setMnemonic(KeyEvent.VK_T);
        menu.getAccessibleContext().setAccessibleDescription("Adjust the robot settings.");

        JMenu subMenu = new JMenu("Port");
        subMenu.getAccessibleContext().setAccessibleDescription("What port to connect to?");
        subMenu.setEnabled(!running);
        ButtonGroup group = new ButtonGroup();

        ListSerialPorts();
        buttonPorts = new JRadioButtonMenuItem[portsDetected.length];
        for(i=0;i<portsDetected.length;++i) {
        	buttonPorts[i] = new JRadioButtonMenuItem(portsDetected[i]);
            if(recentPort.equals(portsDetected[i]) && portOpened) {
            	buttonPorts[i].setSelected(true);
            }
            buttonPorts[i].addActionListener(this);
            group.add(buttonPorts[i]);
            subMenu.add(buttonPorts[i]);
        }
 
        subMenu.addSeparator();

        buttonRescan = new JMenuItem("Rescan",KeyEvent.VK_N);
        buttonRescan.getAccessibleContext().setAccessibleDescription("Rescan the available ports.");
        buttonRescan.addActionListener(this);
        subMenu.add(buttonRescan);
        
        menu.add(subMenu);

        buttonConfigureLimits = new JMenuItem("Configure limits",KeyEvent.VK_L);
        buttonConfigureLimits.getAccessibleContext().setAccessibleDescription("Adjust the robot & paper shape.");
        buttonConfigureLimits.addActionListener(this);
        buttonConfigureLimits.setEnabled(!running);
        menu.add(buttonConfigureLimits);

        buttonConfigureBobbins = new JMenuItem("Configure bobbins",KeyEvent.VK_B);
        buttonConfigureBobbins.getAccessibleContext().setAccessibleDescription("Adjust the bobbin sizes.");
        buttonConfigureBobbins.addActionListener(this);
        buttonConfigureBobbins.setEnabled(!running);
        menu.add(buttonConfigureBobbins);

        
        buttonJogMotors = new JMenuItem("Jog Motors",KeyEvent.VK_J);
        buttonJogMotors.addActionListener(this);
        buttonJogMotors.setEnabled(portConfirmed && !running);
        menu.add(buttonJogMotors);

        buttonDriveManually = new JMenuItem("Drive Manually",KeyEvent.VK_R);
        buttonDriveManually.getAccessibleContext().setAccessibleDescription("Etch-a-sketch style driving");
        buttonDriveManually.addActionListener(this);
        buttonDriveManually.setEnabled(portConfirmed && !running);
        menu.add(buttonDriveManually);
/*
        buttonConfigurePreferences = new JMenuItem("Preferences");
        buttonConfigurePreferences.getAccessibleContext().setAccessibleDescription("Adjust miscelaneous preferences.");
        buttonConfigurePreferences.addActionListener(this);
        buttonConfigurePreferences.setEnabled(!running);
        menu.add(buttonConfigurePreferences);
*/
/*
        menu.addSeparator();

        buttonImageProcessing = new JMenuItem("Image Processing");
        buttonImageProcessing.addActionListener(this);
        menu.add(buttonImageProcessing);
*/
        menuBar.add(menu);

        // Draw menu
        menu = new JMenu("Draw");
        menu.setMnemonic(KeyEvent.VK_D);
        menu.getAccessibleContext().setAccessibleDescription("Start & Stop progress");

        buttonStart = new JMenuItem("Start",KeyEvent.VK_S);
        buttonStart.getAccessibleContext().setAccessibleDescription("Start sending g-code");
        buttonStart.addActionListener(this);
    	buttonStart.setEnabled(portConfirmed && !running);
        menu.add(buttonStart);

        buttonPause = new JMenuItem("Pause",KeyEvent.VK_P);
        buttonPause.getAccessibleContext().setAccessibleDescription("Pause sending g-code");
        buttonPause.addActionListener(this);
        buttonPause.setEnabled(portConfirmed && running);
        menu.add(buttonPause);

        buttonHalt = new JMenuItem("Halt",KeyEvent.VK_H);
        buttonHalt.getAccessibleContext().setAccessibleDescription("Halt sending g-code");
        buttonHalt.addActionListener(this);
        buttonHalt.setEnabled(portConfirmed && running);
        menu.add(buttonHalt);

        menuBar.add(menu);
        
        // tools menu
        menu = new JMenu("Tools");
        buttonZoomOut = new JMenuItem("Zoom -");
        buttonZoomOut.addActionListener(this);
        buttonZoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS,ActionEvent.ALT_MASK));
        menu.add(buttonZoomOut);
        
        buttonZoomIn = new JMenuItem("Zoom +",KeyEvent.VK_EQUALS);
        buttonZoomIn.addActionListener(this);
        buttonZoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS,ActionEvent.ALT_MASK));
        menu.add(buttonZoomIn);
        
        menuBar.add(menu);
        
        // Help menu
        menu = new JMenu("Help");
        menu.setMnemonic(KeyEvent.VK_H);
        menu.getAccessibleContext().setAccessibleDescription("Get help");

        buttonAbout = new JMenuItem("About",KeyEvent.VK_A);
        menu.getAccessibleContext().setAccessibleDescription("Find out about this program");
        buttonAbout.addActionListener(this);
        menu.add(buttonAbout);

        buttonCheckForUpdate = new JMenuItem("Check for updates",KeyEvent.VK_U);
        menu.getAccessibleContext().setAccessibleDescription("Is there a newer version available?");
        buttonCheckForUpdate.addActionListener(this);
        buttonCheckForUpdate.setEnabled(false);
        menu.add(buttonCheckForUpdate);

        menuBar.add(menu);

        // finish
        menuBar.updateUI();
    }
	
    public Container CreateContentPane() {
        //Create the content-pane-to-be.
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setOpaque(true);
        
        // the log panel
        log = new JTextPane();
        log.setEditable(false);
        log.setBackground(Color.BLACK);
        logPane = new JScrollPane(log);
        kit = new HTMLEditorKit();
        doc = new HTMLDocument();
        log.setEditorKit(kit);
        log.setDocument(doc);
        DefaultCaret c = (DefaultCaret)log.getCaret();
        c.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        ClearLog();
        
        // the preview panel
        previewPane = new DrawPanel();
		previewPane.setMachineLimits(limit_top, limit_bottom, limit_left, limit_right);
        previewPane.setPaperSize(paper_top,paper_bottom,paper_left,paper_right);
        
        // status bar
        statusBar = new StatusBar();
        Font f = statusBar.getFont();
        statusBar.setFont(f.deriveFont(Font.BOLD,15));
        Dimension d=statusBar.getMinimumSize();
        d.setSize(d.getWidth(), d.getHeight()+30);
        statusBar.setMinimumSize(d);

        // layout
        Splitter split = new Splitter(JSplitPane.VERTICAL_SPLIT);
        split.add(previewPane);
        split.add(logPane);
        split.setDividerSize(8);

        contentPane.add(statusBar,BorderLayout.SOUTH);
        contentPane.add(split,BorderLayout.CENTER);
		
        return contentPane;
    }
    
    // Create the GUI and show it.  For thread safety, this method should be invoked from the event-dispatching thread.
    private static void CreateAndShowGUI() {
        //Create and set up the window.
    	mainframe = new JFrame("Makelangelo not connected");
        mainframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        //Create and set up the content pane.
        DrawbotGUI demo = DrawbotGUI.getSingleton();
        mainframe.setJMenuBar(demo.CreateMenuBar());
        mainframe.setContentPane(demo.CreateContentPane());
 
        //Display the window.
        mainframe.setSize(800,700);
        mainframe.setVisible(true);

        DrawbotGUI s=getSingleton();
        // open the file
		if(s.recentFiles[0].length()>0) {
			s.OpenFileOnDemand(s.recentFiles[0]);
		}
		
		// connect to the last port
		s.ListSerialPorts();
		if(Arrays.asList(s.portsDetected).contains(s.recentPort)) {
			s.OpenPort(s.recentPort);
		}
    }
    
    public static void main(String[] args) {
	    //Schedule a job for the event-dispatching thread:
	    //creating and showing this application's GUI.
	    javax.swing.SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	        	/*
	        	String OS = System.getProperty("os.name").toLowerCase();
	            String workingDirectory=System.getProperty("user.dir");
	            System.out.println(workingDirectory);
	            
	            System.out.println(OS);
	            // is this Windows?
	            if(OS.indexOf("win") >= 0) {
	            	// is 64 bit?
	            	if(System.getenv("ProgramFiles(x86)") != null) {
	            		// 64 bit
	            		System.load(workingDirectory+"/64/rxtxSerial.dll");
	            	} else {
	            		// 32 bit
	            		System.load(workingDirectory+"/32/rxtxSerial.dll");
	            	}
	            } else {
	            	// is this OSX?
	    	        if(OS.indexOf("mac") >= 0) {
	    	    		System.load(workingDirectory+"/librxtxSerial.jnilib");
	    	        }
	            }
	    		*/
	            CreateAndShowGUI();
	        }
	    });
    }
}
