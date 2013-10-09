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
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
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
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.prefs.Preferences;

//@TODO: in-app gcode editing with immediate visusal feedback - only while not drawing
//@TODO: image processing options - cutoff, exposure, resolution, voronoi stipples?
//@TODO: vector output?  stl input?

public class DrawbotGUI
		extends JPanel
		implements ActionListener, SerialPortEventListener
{
	// software version
	static final String version="1";
	
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
	private long robot_uid=0;
	private double limit_top=10;
	private double limit_bottom=-10;
	private double limit_left=-10;
	private double limit_right=10;
	private int image_dpi;
	private int startingPositionIndex;
	
	// paper area
	private double paper_top=10;
	private double paper_bottom=-10;
	private double paper_left=-10;
	private double paper_right=10;

	// machine settings
	private boolean m1invert=false;
	private boolean m2invert=false;
	private double bobbin_left_diameter=0.95;
	private double bobbin_right_diameter=0.95;
	private long penUpNumber, penDownNumber;
	private double feed_rate;
	private boolean penIsUp,penIsUpBeforePause;
	
	// GUI elements
	private static JFrame mainframe;
	private JMenuBar menuBar;
    private JMenuItem buttonOpenFile, buttonExit;
    private JMenuItem buttonConfigurePreferences, buttonConfigureLimits, buttonConfigureBobbins, 
    					buttonRescan, buttonDisconnect, buttonAdjustZ, buttonJogMotors;
    private JMenuItem buttonStart, buttonPause, buttonHalt;
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
    
    // panels
    private DrawPanel previewPane;
	private StatusBar statusBar;
	private JPanel drivePane;
	
	// parsing input from Drawbot
	private String line3="";

	// reading file
	private boolean running=false;
	private boolean paused=true;
	
	GCodeFile gcode = new GCodeFile();
	
	
	private String getPenUp() {
		return Long.toString(penUpNumber);
	}
	
	private String getPenDown() {
		return Long.toString(penDownNumber);
	}
	
	public double getScale() {
		return image_dpi/100.0;
	}

	private void RaisePen() {
		SendLineToRobot("G00 Z"+getPenUp());
		penIsUp=true;
	}
	
	private void LowerPen() {
		SendLineToRobot("G00 Z"+getPenDown());
		penIsUp=false;
	}
	
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

	private void PlaySound(String url) {
		if(url.isEmpty()) return;
		
		try {
			Clip clip = AudioSystem.getClip();
			FileInputStream x = new FileInputStream(url);
			AudioInputStream inputStream = AudioSystem.getAudioInputStream(x);
			clip.open(inputStream);
			clip.start(); 
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
		}
	}
	
	private void PlayConnectSound() {
		PlaySound(prefs.get("sound_connect", ""));
	}
	
	private void PlayDisconnectSound() {
		PlaySound(prefs.get("sound_disconnect", ""));
	}
	
	public void PlayConversionFinishedSound() {
		PlaySound(prefs.get("sound_conversion_finished", ""));
	}
	
	private void PlayDawingFinishedSound() {
		PlaySound(prefs.get("sound_drawing_finished", ""));
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
			
			Filter_Resize rs = new Filter_Resize(paper_top,paper_bottom,paper_left,paper_right,getScale(),0.85f); 
			img = rs.Process(img);
/*
			Filter_Translate t = new Filter_Translate(); 
			img = t.Process(img);
*/
			Filter_BlackAndWhite bwc = new Filter_BlackAndWhite(); 
			img = bwc.Process(img);
			
			Filter_DitherFloydSteinberg dither = new Filter_DitherFloydSteinberg();
			img = dither.Process(img);

	        String workingDirectory=System.getProperty("user.dir");
			String ngcPair = workingDirectory+"temp.ngc";//filename.substring(0, filename.lastIndexOf('.')) + ".ngc";
			Filter_TSPGcodeGenerator tsp = new Filter_TSPGcodeGenerator(ngcPair,getScale());
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
			PlayDisconnectSound();
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
		PlayConnectSound();
		
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

			SendConfig();
			previewPane.setMachineLimits(limit_top, limit_bottom, limit_left, limit_right);
			previewPane.setPaperSize(paper_top,paper_bottom,paper_left,paper_right);
			
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

	   	UpdateRecentFiles(filename);
    	previewPane.ZoomToFitPaper();

    	statusBar.Clear();
	}

	// creates a file open dialog. If you don't cancel it opens that file.
	public void OpenFileDialog() {
	    // Note: source for ExampleFileFilter can be found in FileChooserDemo,
	    // under the demo/jfc directory in the Java 2 SDK, Standard Edition.
		String filename = (recentFiles[0].length()>0) ? filename=recentFiles[0] : "";

		FileFilter filterGCODE = new FileNameExtensionFilter("GCODE files (ngc)", "ngc");
		FileFilter filterImage  = new FileNameExtensionFilter("Images (jpg/bmp/png/gif)", "jpg", "jpeg", "png", "wbmp", "bmp", "gif");
		 
		JFileChooser fc = new JFileChooser(new File(filename));
		fc.addChoosableFileFilter(filterGCODE);
		fc.addChoosableFileFilter(filterImage);
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
		SendLineToRobot("G00 F"+feed_rate+" X0 Y0");
	}
	
	private String SelectFile() {
		JFileChooser choose = new JFileChooser();
	    int returnVal = choose.showOpenDialog(this);
	    if (returnVal == JFileChooser.APPROVE_OPTION) {
	        File file = choose.getSelectedFile();
	        return file.getAbsolutePath();
	    } else {
	        System.out.println("File access cancelled by user.");
	    }
	    return "";
	}
	
	/**
	 * Adjust preferences
	 */
	public void ConfigurePreferences() {
		final JDialog driver = new JDialog(mainframe,"Preferences",true);
		driver.setLayout(new GridBagLayout());
		
		final JTextField sound_connect = new JTextField(prefs.get("sound_connect",""),32);
		final JTextField sound_disconnect = new JTextField(prefs.get("sound_disconnect", ""),32);
		final JTextField sound_conversion_finished = new JTextField(prefs.get("sound_conversion_finished", ""),32);
		final JTextField sound_drawing_finished = new JTextField(prefs.get("sound_drawing_finished", ""),32);

		final JButton change_sound_connect = new JButton("Connect sound");
		final JButton change_sound_disconnect = new JButton("Disconnect sound");
		final JButton change_sound_conversion_finished = new JButton("Convert finish sound");
		final JButton change_sound_drawing_finished = new JButton("Draw finish sound");
		
		final JSlider input_image_dpi = new JSlider(JSlider.HORIZONTAL, 50, 300, image_dpi);
		//input_image_dpi.setSize(250,input_image_dpi.getSize().height);
		input_image_dpi.setMajorTickSpacing(100);
		input_image_dpi.setMinorTickSpacing(25);
		input_image_dpi.setPaintTicks(true);
		input_image_dpi.setPaintLabels(true);
		
		final JCheckBox allow_metrics = new JCheckBox(String.valueOf("I want to add the distance drawn to the global total"));
		allow_metrics.setSelected(allowMetrics);

		final JButton cancel = new JButton("Cancel");
		final JButton save = new JButton("Save");
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth=4; 	c.gridx=0;  c.gridy=0;  driver.add(allow_metrics,c);

		c.gridwidth=1;	c.gridx=0;  c.gridy=3;  driver.add(change_sound_connect,c);				c.gridwidth=3;	c.gridx=1;  c.gridy=3;  driver.add(sound_connect,c);
		c.gridwidth=1;	c.gridx=0;  c.gridy=4;  driver.add(change_sound_disconnect,c);			c.gridwidth=3;	c.gridx=1;  c.gridy=4;  driver.add(sound_disconnect,c);
		c.gridwidth=1;	c.gridx=0;  c.gridy=5;  driver.add(change_sound_conversion_finished,c);	c.gridwidth=3;	c.gridx=1;  c.gridy=5;  driver.add(sound_conversion_finished,c);
		c.gridwidth=1;	c.gridx=0;  c.gridy=6;  driver.add(change_sound_drawing_finished,c);	c.gridwidth=3;	c.gridx=1;  c.gridy=6;  driver.add(sound_drawing_finished,c);
		c.gridwidth=1;	c.gridx=0;  c.gridy=7;  driver.add(new JLabel("Image Resolution"),c);	c.gridwidth=3;	c.gridx=1;  c.gridy=7;  driver.add(input_image_dpi,c);

		c.gridwidth=1;	c.gridx=2;  c.gridy=9;  driver.add(cancel,c);
		c.gridwidth=1;	c.gridx=1;  c.gridy=9;  driver.add(save,c);
		
		ActionListener driveButtons = new ActionListener() {
			  public void actionPerformed(ActionEvent e) {
					Object subject = e.getSource();
					if(subject == change_sound_connect) sound_connect.setText(SelectFile());
					if(subject == change_sound_disconnect) sound_disconnect.setText(SelectFile());
					if(subject == change_sound_conversion_finished) sound_conversion_finished.setText(SelectFile());
					if(subject == change_sound_drawing_finished) sound_drawing_finished.setText(SelectFile());

					if(subject == save) {
						image_dpi=input_image_dpi.getValue();
						
						allowMetrics = allow_metrics.isSelected();
						prefs.put("sound_connect",sound_connect.getText());
						prefs.put("sound_disconnect",sound_disconnect.getText());
						prefs.put("sound_conversion_finished",sound_conversion_finished.getText());
						prefs.put("sound_drawing_finished",sound_drawing_finished.getText());
						prefs.put("image_dpi",Integer.toString(image_dpi));
						driver.dispose();
					}
					if(subject == cancel) {
						driver.dispose();
					}
			  }
		};

		change_sound_connect.addActionListener(driveButtons);
		change_sound_disconnect.addActionListener(driveButtons);
		change_sound_conversion_finished.addActionListener(driveButtons);
		change_sound_drawing_finished.addActionListener(driveButtons);
			
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
		
		final JTextField mw = new JTextField(String.valueOf((limit_right-limit_left)*10));
		final JTextField mh = new JTextField(String.valueOf((limit_top-limit_bottom)*10));
		final JTextField pw = new JTextField(String.valueOf((paper_right-paper_left)*10));
		final JTextField ph = new JTextField(String.valueOf((paper_top-paper_bottom)*10));

		String[] startingStrings = { "Top Left", "Top Center", "Top Right", "Left", "Center", "Right", "Bottom Left","Bottom Center","Bottom Right" };
		final JComboBox startPos = new JComboBox(startingStrings);
		startPos.setSelectedIndex(startingPositionIndex);
		
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
		c.gridx=0; c.gridy=0; c.gridwidth=4; c.gridheight=4; c.anchor=GridBagConstraints.CENTER; driver.add( picLabel,c );
		
		c.gridheight=1; c.gridwidth=1; 
		d.anchor=GridBagConstraints.WEST;

		c.gridx=0; c.gridy=5; c.gridwidth=4; c.gridheight=1;
		driver.add(new JLabel("All values in mm."),c);
		c.gridwidth=1;
		
		c.ipadx=3;
		c.anchor=GridBagConstraints.EAST;
		c.gridx=0; c.gridy=6; driver.add(new JLabel("Machine width"),c);	d.gridx=1;	d.gridy=6;	driver.add(mw,d);
		c.gridx=2; c.gridy=6; driver.add(new JLabel("Machine height"),c);	d.gridx=3;	d.gridy=6;	driver.add(mh,d);
		c.gridx=0; c.gridy=7; driver.add(new JLabel("Paper width"),c);		d.gridx=1;	d.gridy=7;	driver.add(pw,d);
		c.gridx=2; c.gridy=7; driver.add(new JLabel("Paper height"),c);		d.gridx=3;	d.gridy=7;	driver.add(ph,d);
		
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
				if(subject == save) {
					float pwf = Math.round( Float.valueOf(pw.getText()) * 100 ) / (100 * 10);
					float phf = Math.round( Float.valueOf(ph.getText()) * 100 ) / (100 * 10);
					float mwf = Math.round( Float.valueOf(mw.getText()) * 100 ) / (100 * 10);
					float mhf = Math.round( Float.valueOf(mh.getText()) * 100 ) / (100 * 10);
					
					boolean data_is_sane=true;
					if( pwf<=0 ) data_is_sane=false;
					if( phf<=0 ) data_is_sane=false;
					if( mwf<=0 ) data_is_sane=false;
					if( mhf<=0 ) data_is_sane=false;
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
		SendLineToRobot("M114"); // "where" command
		driver.pack();
		driver.setVisible(true);
	}

	/**
	 * Open the config dialog, send the config update to the robot, save it for future, and refresh the preview tab.
	 */
	public void ConfigureBobbins() {
		final JDialog driver = new JDialog(mainframe,"Configure Bobbins",true);
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
		bobbin_left_diameter=Double.valueOf(prefs.get(id+"_bobbin_left_diameter", "0.95"));
		bobbin_right_diameter=Double.valueOf(prefs.get(id+"_bobbin_right_diameter", "0.95"));
		penUpNumber=Long.valueOf(prefs.get(id+"_penUp", "90"));
		penDownNumber=Long.valueOf(prefs.get(id+"_penDown", "65"));
		feed_rate=Double.valueOf(prefs.get(id+"_feed_rate","2000"));
		startingPositionIndex=Integer.valueOf(prefs.get(id+"_startingPosIndex","4"));
		image_dpi= Integer.valueOf(prefs.get("image_dpi", "100"));
	}

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
		prefs.put(id+"_penUp", Long.toString(penUpNumber));
		prefs.put(id+"_penDown", Long.toString(penDownNumber));
		prefs.put(id+"_feed_rate", Double.toString(feed_rate));
		prefs.put(id+"_startingPosIndex", Integer.toString(startingPositionIndex));
		prefs.put("image_dpi",Integer.toString(image_dpi));
	}
	
	void SendConfig() {
		if(!portConfirmed) return;
		
		// Send a command to the robot with new configuration values
		SendLineToRobot("CONFIG T"+limit_top
						+" B"+limit_bottom
						+" L"+limit_left
						+" R"+limit_right
						+" I"+(m1invert?"-1":"1")
						+" J"+(m2invert?"-1":"1"));
		SendLineToRobot("D01 L"+bobbin_left_diameter+" R"+bobbin_right_diameter);
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
			PlayDawingFinishedSound();
			Halt();
		}
	}
	
	private void ChangeToTool(String toolName) {
		JOptionPane.showMessageDialog(null,"Please change to "+toolName+" and click OK.");
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
			PlayDawingFinishedSound();
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
		
		// contains a pen-up command?
		index=line.indexOf("Z90");
		if(index!=-1) {
			line=line.replaceAll("Z90", "Z"+getPenUp());
			penIsUp=true;
		}
		
		// contains a pen-down command?
		index=line.indexOf("Z0");
		if(index!=-1) {
			line=line.replaceAll("Z0", "Z"+getPenDown());
			penIsUp=false;
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
		
		if( subject == buttonZoomIn ) {
			previewPane.ZoomIn();
			return;
		}
		if( subject == buttonZoomOut ) {
			previewPane.ZoomOut();
			return;
		}
		if( subject == buttonOpenFile ) {
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
					penIsUpBeforePause=penIsUp;
					RaisePen();
					buttonPause.setText("Pause");
					paused=false;
					// @TODO: if the robot is not ready to unpause, this might fail and the program would appear to hang.
					SendFileCommand();
				} else {
					if(!penIsUpBeforePause) LowerPen();
					buttonPause.setText("Unpause");
					paused=true;
				}
			}
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
		if( subject == buttonDisconnect ) {
			ClosePort();
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
		if( subject == buttonAdjustZ ) {
			AdjustZ();
			return;
		}
		if( subject == buttonJogMotors ) {
			JogMotors();
			return;
		}
		if( subject == buttonAbout ) {
			JOptionPane.showMessageDialog(null,"Makelangelo v"+version+"\n\n"
					+"Created by Dan Royer (dan@marginallyclever.com).\n\n"
					+"Get the latest version and read the documentation @ https://github.com/MarginallyClever/Makelangelo\n"
					+"Find out more at http://www.makelangelo.com/\n");
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

	/**
	 * Open the config dialog, update the paper size, refresh the preview tab.
	 */
	public JPanel DriveManually() {
		JPanel driver = new JPanel();
		driver.setLayout(new GridBagLayout());

		final JButton find = new JButton("FIND HOME");		find.setPreferredSize(new Dimension(100,20));
		final JButton home = new JButton("GO HOME");		home.setPreferredSize(new Dimension(100,20));
		final JButton center = new JButton("THIS IS HOME");	center.setPreferredSize(new Dimension(100,20));
		
		final JButton up1 = new JButton("Y1");  		up1.setPreferredSize(new Dimension(60,20));
		final JButton up10 = new JButton("Y10");  		up10.setPreferredSize(new Dimension(60,20));
		final JButton up100 = new JButton("Y100");  	up100.setPreferredSize(new Dimension(60,20));
		
		final JButton down1 = new JButton("Y-1");		down1.setPreferredSize(new Dimension(60,20));
		final JButton down10 = new JButton("Y-10");		down10.setPreferredSize(new Dimension(60,20));
		final JButton down100 = new JButton("Y-100");	down100.setPreferredSize(new Dimension(60,20));
		
		final JButton left1 = new JButton("X-1");		left1.setPreferredSize(new Dimension(60,20));
		final JButton left10 = new JButton("X-10");		left10.setPreferredSize(new Dimension(60,20));
		final JButton left100 = new JButton("X-100");	left100.setPreferredSize(new Dimension(60,20));
		
		final JButton right1 = new JButton("X1");		right1.setPreferredSize(new Dimension(60,20));
		final JButton right10 = new JButton("X10");		right10.setPreferredSize(new Dimension(60,20));
		final JButton right100 = new JButton("X100");	right100.setPreferredSize(new Dimension(60,20));
		
		final JButton TL = new JButton("TL");			TL.setPreferredSize(new Dimension(60,20));
		final JButton TR = new JButton("TR");			TR.setPreferredSize(new Dimension(60,20));
		final JButton BL = new JButton("BL");			BL.setPreferredSize(new Dimension(60,20));
		final JButton BR = new JButton("BR");			BR.setPreferredSize(new Dimension(60,20));

		final JButton z90 = new JButton("Pen Up");		z90.setPreferredSize(new Dimension(100,20));
		final JButton z0  = new JButton("Pen Down");	z0.setPreferredSize(new Dimension(100,20));
		
		final JFormattedTextField feedRate = new JFormattedTextField(NumberFormat.getInstance());  feedRate.setPreferredSize(new Dimension(60,20));
		feedRate.setText(Double.toString(feed_rate));
		final JButton setFeedRate = new JButton("Set");	setFeedRate.setPreferredSize(new Dimension(60,20));
		
		GridBagConstraints c = new GridBagConstraints();
		//c.fill=GridBagConstraints.BOTH; 
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

		//c.gridx=3;	c.gridy=3;	driver.add(home,c);
		c.gridx=7;  c.gridy=0;  driver.add(center,c);
		c.gridx=7;  c.gridy=1;  driver.add(home,c);  //driver.add(find,c);
		c.gridx=7;  c.gridy=5;  driver.add(z90,c);
		c.gridx=7;  c.gridy=6;  driver.add(z0,c);
		
		
		c.gridx=3;  c.gridy=8;  driver.add(new JLabel("Speed:"),c);
		c.gridx=4;  c.gridy=8;  driver.add(feedRate,c);
		c.gridx=5;  c.gridy=8;  driver.add(new JLabel("mm/min"),c);
		c.gridx=6;  c.gridy=8;  driver.add(setFeedRate,c);
		
		
		ActionListener driveButtons = new ActionListener() {
			  public void actionPerformed(ActionEvent e) {
					Object subject = e.getSource();
					JButton b = (JButton)subject;
					if(running) return;
					if(b==home) {
						GoHome();
						SendLineToRobot("M114");
					} else if(b==TL) { 
						SendLineToRobot("G00 F"+feed_rate+" X"+(paper_left *10)+" Y"+(paper_top*10));
					} else if(b==TR) { 
						SendLineToRobot("G00 F"+feed_rate+" X"+(paper_right*10)+" Y"+(paper_top*10));
					} else if(b==BL) { 
						SendLineToRobot("G00 F"+feed_rate+" X"+(paper_left *10)+" Y"+(paper_bottom*10));
					} else if(b==BR) { 
						SendLineToRobot("G00 F"+feed_rate+" X"+(paper_right*10)+" Y"+(paper_bottom*10));
					} else if(b==find) {
						SendLineToRobot("G28");
					} else if(b==center) {
						SendLineToRobot("TELEPORT X0 Y0");
					} else if(b==z90) {
						RaisePen();
					} else if(b==z0) {
						LowerPen();
					} else if(b==setFeedRate) {
						feed_rate = Double.parseDouble(feedRate.getText());
						if(feed_rate<1) feed_rate=1;
						if(feed_rate>2000) feed_rate=20000;
						feedRate.setText(Double.toString(feed_rate));
						SendLineToRobot("G00 G21 F"+feed_rate);
					} else {
						SendLineToRobot("G91");
						SendLineToRobot("G00 G21 F"+feed_rate+" "+b.getText());
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
		feedRate.addActionListener(driveButtons);
		
		return driver;
	}
	

	/**
	 * dialog to adjust the pen up & pen down values
	 */
	protected void AdjustZ() {
		final JDialog driver = new JDialog(mainframe,"Adjust Z",true);
		driver.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		final JTextField penUp   = new JTextField(Long.toString(penUpNumber),5);
		final JTextField penDown = new JTextField(Long.toString(penDownNumber),5);
		final JButton buttonTestUp = new JButton("Test up");
		final JButton buttonTestDown = new JButton("Test down");
		final JButton buttonSave = new JButton("Save");
		final JButton buttonCancel = new JButton("Cancel");


		c.gridx=0;	c.gridy=0;	driver.add(new JLabel("Up"),c);
		c.gridx=1;	c.gridy=0;	driver.add(new JLabel("Down"),c);

		c.anchor=GridBagConstraints.WEST;
		c.fill=GridBagConstraints.HORIZONTAL;
		c.weightx=50;
		c.gridx=0;	c.gridy=1;	driver.add(penUp,c);
		c.gridx=1;	c.gridy=1;	driver.add(penDown,c);
		
		c.gridx=0;	c.gridy=2;	driver.add(buttonTestUp,c);
		c.gridx=1;	c.gridy=2;	driver.add(buttonTestDown,c);

		c.gridx=0;	c.gridy=3;	driver.add(buttonSave,c);
		c.gridx=1;	c.gridy=3;	driver.add(buttonCancel,c);

		c.gridwidth=2;
		c.insets=new Insets(0,5,5,5);
		c.anchor=GridBagConstraints.WEST;
		
		c.gridheight=4;
		c.gridx=0;  c.gridy=4;
		driver.add(new JTextArea("Adjust the values sent to the servo to\n" +
								 "raise and lower the pen."),c);
		
		
		ActionListener driveButtons = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object subject = e.getSource();
				
				if(subject == buttonTestUp) {
					SendLineToRobot("G00 Z"+Long.valueOf(penUp.getText()));
				}
				if(subject == buttonTestDown) {
					SendLineToRobot("G00 Z"+Long.valueOf(penDown.getText()));
				}
				if(subject == buttonSave) {
					penUpNumber = Long.valueOf(penUp.getText());
					penDownNumber = Long.valueOf(penDown.getText());
					SaveConfig();
					driver.dispose();
				}
				if(subject == buttonCancel) {
					driver.dispose();
				}
			}
		};
		
		buttonTestUp.addActionListener(driveButtons);
		buttonTestDown.addActionListener(driveButtons);
		
		buttonSave.addActionListener(driveButtons);
		buttonCancel.addActionListener(driveButtons);

		SendLineToRobot("M114");
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
	
	public void CheckForUpdate() {
		try {
		    // Get Github info
			URL github = new URL("http://www.marginallyclever.com/other/makelangelo-version.php");
	        BufferedReader in = new BufferedReader(new InputStreamReader(github.openStream()));

	        String inputLine;
	        if((inputLine = in.readLine()) != null) {
	        	if( inputLine.compareTo(version) !=0 ) {
	        		JOptionPane.showMessageDialog(null,"A new version of this software is available.  The latest version is "+inputLine+"\n"
	        											+"Please visit http://bit.ly/13DrlLK to get the new hotness.");
	        	}
	        }
	        in.close();
		} catch (Exception e) {}
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

        buttonDisconnect = new JMenuItem("Disconnect",KeyEvent.VK_D);
        buttonDisconnect.addActionListener(this);
        buttonDisconnect.setEnabled(portOpened);
        subMenu.add(buttonDisconnect);
        
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

        buttonAdjustZ = new JMenuItem("Adjust Z",KeyEvent.VK_B);
        buttonAdjustZ.getAccessibleContext().setAccessibleDescription("Adjust the Z axis.");
        buttonAdjustZ.addActionListener(this);
        buttonAdjustZ.setEnabled(!running);
        menu.add(buttonAdjustZ);
        
        buttonJogMotors = new JMenuItem("Jog Motors",KeyEvent.VK_J);
        buttonJogMotors.addActionListener(this);
        buttonJogMotors.setEnabled(portConfirmed && !running);
        menu.add(buttonJogMotors);

        menu.addSeparator();
        
        buttonConfigurePreferences = new JMenuItem("Preferences");
        buttonConfigurePreferences.getAccessibleContext().setAccessibleDescription("Adjust miscelaneous preferences.");
        buttonConfigurePreferences.addActionListener(this);
        buttonConfigurePreferences.setEnabled(!running);
        menu.add(buttonConfigurePreferences);
        
        menuBar.add(menu);

        // Draw menu
        menu = new JMenu("Draw");
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
        
        // the drive panel
        drivePane = DriveManually();
        
        // status bar
        statusBar = new StatusBar();
        Font f = statusBar.getFont();
        statusBar.setFont(f.deriveFont(Font.BOLD,15));
        Dimension d=statusBar.getMinimumSize();
        d.setSize(d.getWidth(), d.getHeight()+30);
        statusBar.setMinimumSize(d);

        // layout
        Splitter drive_and_preview = new Splitter(JSplitPane.HORIZONTAL_SPLIT);
        drive_and_preview.add(logPane);
        drive_and_preview.add(drivePane);
        drive_and_preview.setDividerSize(8);
        drive_and_preview.setDividerLocation(-100);
        
        Splitter split = new Splitter(JSplitPane.VERTICAL_SPLIT);
        split.add(previewPane);
        split.add(drive_and_preview);
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
        mainframe.setSize(1200,700);
        mainframe.setVisible(true);

        DrawbotGUI s=getSingleton();
        /*
        // if the default file being opened in a g-code file, this is ok.
        // else load time is long and it feels like the app has crashed on load to new users.
        // open the file
		if(s.recentFiles[0].length()>0) {
			s.OpenFileOnDemand(s.recentFiles[0]);
		}
		*/
		
		// connect to the last port
		s.ListSerialPorts();
		if(Arrays.asList(s.portsDetected).contains(s.recentPort)) {
			s.OpenPort(s.recentPort);
		}
		
		s.CheckForUpdate();
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


/**
 * This file is part of DrawbotGUI.
 *
 * DrawbotGUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * DrawbotGUI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */