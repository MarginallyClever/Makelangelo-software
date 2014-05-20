/**@(#)drawbotGUI.java
 *
 * drawbot application with GUI
 *
 * @author Dan Royer (dan@marginallyclever.com)
 * @version 1.00 2012/2/28
 */


// io functions
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.TooManyListenersException;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

// TODO while not drawing, in-app gcode editing with immediate visusal feedback 
// TODO image processing options - cutoff, exposure, resolution, voronoi stippling
// TODO vector output

public class Makelangelo
		extends JPanel
		implements ActionListener, SerialPortEventListener
{
	// software version
	static final String version="3";
	
	static final long serialVersionUID=1;
	
	private static Makelangelo singletonObject;
	
	// TODO put all serial stuff in a Serial class, hide it inside Robot class?
	// Serial connection
	private static final int BAUD_RATE = 57600;
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
	static private final String hello = "HELLO WORLD! I AM DRAWBOT #";
	
	// Image processing preferences
	private static final int IMAGE_TSP=0;
	private static final int IMAGE_SPIRAL=1;
	private static final int IMAGE_4LEVEL=2;
	private static final int IMAGE_SCANLINE=3;
	private static final int IMAGE_RGB=4;
	
	private Preferences prefs = Preferences.userRoot().node("DrawBot");
	private String[] recentFiles;
	private String recentPort;
	//private boolean allowMetrics=true;
	
	// Metrics?
	PublishImage reportImage = new PublishImage();
	DistanceMetric reportDistance = new DistanceMetric();
	
	// machine settings while running
	private double feed_rate;
	private boolean penIsUp,penIsUpBeforePause;
	
	// GUI elements
	private static JFrame mainframe;
	private JMenuBar menuBar;
    private JMenuItem buttonOpenFile, buttonText2GCODE, buttonSaveFile, buttonExit;
    private JMenuItem buttonConfigurePreferences, buttonAdjustMachineSize, buttonAdjustPulleySize, buttonChangeTool, buttonAdjustTool, buttonRescan, buttonDisconnect, buttonJogMotors;
    private JMenuItem buttonStart, buttonStartAt, buttonPause, buttonHalt;
    private JMenuItem buttonZoomIn,buttonZoomOut,buttonZoomToFit;
    private JMenuItem buttonAbout,buttonCheckForUpdate;
    
    private JMenuItem [] buttonRecent = new JMenuItem[10];
    private JMenuItem [] buttonPorts;

    public boolean dialog_result=false;
    
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
	

	private void RaisePen() {
		SendLineToRobot("G00 Z"+MachineConfiguration.getSingleton().getPenUpString());
		penIsUp=true;
	}
	
	private void LowerPen() {
		SendLineToRobot("G00 Z"+MachineConfiguration.getSingleton().getPenDownString());
		penIsUp=false;
	}
	
	private Makelangelo() {
		StartLog();
		MachineConfiguration.getSingleton().LoadConfig();
        GetRecentFiles();
        GetRecentPort();
	}
	
	protected void finalize() throws Throwable {
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
	
	public static Makelangelo getSingleton() {
		if(singletonObject==null) {
			singletonObject = new Makelangelo();
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
		
	private void SetDrawStyle(int style) {
		prefs.putInt("Draw Style", style);
	}
	private int GetDrawStyle() {
		return prefs.getInt("Draw Style", IMAGE_SPIRAL);
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
        // where to save temp output file?
		String destinationFile = System.getProperty("user.dir")+"/temp.ngc";
		
		// read in image
		BufferedImage img;
		try {
			img = ImageIO.read(new File(filename));
			
			// resize & flip as needed
			Filter_Resize rs = new Filter_Resize(); 
			img = rs.Process(img);
			
			// convert with style
			switch(GetDrawStyle()) {
			case Makelangelo.IMAGE_TSP:			LoadImageTSP(img,destinationFile);		break;
			case Makelangelo.IMAGE_SPIRAL:		LoadImageSpiral(img,destinationFile);	break;
			case Makelangelo.IMAGE_4LEVEL:		LoadImage4Level(img,destinationFile);	break;
			case Makelangelo.IMAGE_SCANLINE:	LoadImageScanLine(img,destinationFile);	break;
			case Makelangelo.IMAGE_RGB:         LoadImageRGB(img,destinationFile);		break;
			}
		}
		catch(IOException e) {
	    	Log("<span style='color:red'>File could not be opened: "+e.getLocalizedMessage()+"</span>\n");
	    	RemoveRecentFile(filename);
	    	return;
		}
	}

	
	private void LoadImageTSP(BufferedImage img,String destinationFile) throws IOException {
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		img = bw.Process(img);
		
		Filter_DitherFloydSteinberg dither = new Filter_DitherFloydSteinberg();
		img = dither.Process(img);

		Filter_TSPGcodeGenerator generator = new Filter_TSPGcodeGenerator(destinationFile);
		generator.Process(img);
	}
	
	
	private void LoadImage4Level(BufferedImage img,String destinationFile) throws IOException {
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255); 
		img = bw.Process(img);

		Filter_CrosshatchGenerator generator = new Filter_CrosshatchGenerator(destinationFile);
		generator.Process(img);
	}
	
	
	private void LoadImageRGB(BufferedImage img,String destinationFile) throws IOException {
		Filter_RGBCircleGenerator generator = new Filter_RGBCircleGenerator(destinationFile);
		generator.Process(img);
	}
	
	
	private void LoadImageSpiral(BufferedImage img,String destinationFile) throws IOException {
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255); 
		img = bw.Process(img);

		Filter_Spiral generator = new Filter_Spiral(destinationFile);
		generator.Process(img);
	}

	
	private void LoadImageScanLine(BufferedImage img,String destinationFile) throws IOException {
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		img = bw.Process(img);
		
		Filter_DitherFloydSteinberg dither = new Filter_DitherFloydSteinberg();
		img = dither.Process(img);

		Filter_ScanlineGenerator generator = new Filter_ScanlineGenerator(destinationFile);
		generator.Process(img);
	}
	
	
	private void TextToGCODE() {
		Filter_YourMessageHere msg = new Filter_YourMessageHere();

		msg.Generate( System.getProperty("user.dir")+"/temp.ngc");

    	previewPane.ZoomToFitPaper();
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
			Log("<span style='color:red'>Port could not be opened:"+e.getMessage()+"</span>\n");
			e.printStackTrace();
			return 3;
		}

	    if( ( commPort instanceof SerialPort ) == false ) {
			Log("<span style='color:red'>This is not a SerialPort.</span>\n");
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

		Log("<span style='color:green'>Opened.</span>\n");
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
		if(line3.lastIndexOf(hello) >= 0) {
			portConfirmed=true;
			
			String after_hello = line3.substring(line3.lastIndexOf(hello) + hello.length());
			MachineConfiguration.getSingleton().ParseRobotUID(after_hello);
			
			mainframe.setTitle("Makelangelo #"+Long.toString(MachineConfiguration.getSingleton().robot_uid)+" connected");

			// did read go ok?
			if(MachineConfiguration.getSingleton().robot_uid!=0) {
				reportDistance.SetUID(MachineConfiguration.getSingleton().robot_uid);
			}

			SendConfig();
			previewPane.updateMachineConfig();

			UpdateMenuBar();
			previewPane.setConnected(true);
		}
		return portConfirmed;
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
	    	Log("<span style='color:red'>File could not be opened: "+e.getLocalizedMessage()+"</span>\n");
	    	RemoveRecentFile(filename);
	    	return;
	    }
	    
	    previewPane.setGCode(gcode.lines);
	    Halt();
	}
	
	public boolean IsFileLoaded() {
		return ( gcode.fileOpened && gcode.lines != null && gcode.lines.size() > 0 );
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
		prefs.remove("recent-files-"+(i-1));
		
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
		Log("<font color='green'>Opening file "+filename+"...</font>\n");

	   	UpdateRecentFiles(filename);
	   	
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

		FileFilter filterGCODE = new FileNameExtensionFilter("GCODE files (ngc)", "ngc");
		FileFilter filterImage  = new FileNameExtensionFilter("Images (jpg/bmp/png/gif)", "jpg", "jpeg", "png", "wbmp", "bmp", "gif");
		 
		JFileChooser fc = new JFileChooser(new File(filename));
		fc.addChoosableFileFilter(filterGCODE);
		fc.addChoosableFileFilter(filterImage);
	    if(fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
	    	String selectedFile=fc.getSelectedFile().getAbsolutePath();
	    	if(!IsFileGcode(selectedFile)) {
	    		// if machine is not yet calibrated
	    		if(MachineConfiguration.getSingleton().IsPaperConfigured() == false) {
	    			JOptionPane.showMessageDialog(null,"Please set a paper size before importing an image.  Paper size is set in Settings > Adjust machine size.");
	    			return;
	    		}
	    	}
	    	OpenFileOnDemand(selectedFile);
	    }
	}
	
	private void SaveFileDialog() {
	    // Note: source for ExampleFileFilter can be found in FileChooserDemo,
	    // under the demo/jfc directory in the Java 2 SDK, Standard Edition.
		String filename = (recentFiles[0].length()>0) ? filename=recentFiles[0] : "";

		FileFilter filterGCODE = new FileNameExtensionFilter("GCODE files (ngc)", "ngc");
		
		JFileChooser fc = new JFileChooser(new File(filename));
		fc.addChoosableFileFilter(filterGCODE);
	    if(fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
	    	String selectedFile=fc.getSelectedFile().getAbsolutePath();

			if(!selectedFile.toLowerCase().endsWith(".ngc")) {
				selectedFile+=".ngc";
			}

	    	try {
	    		gcode.Save(selectedFile);
	    	}
		    catch(IOException e) {
		    	Log("<span style='color:red'>File "+filename+" could not be saved: "+e.getMessage()+"</span>\n");
		    	return;
		    }
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
	public void AdjustPreferences() {
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

		final JSlider input_paper_margin = new JSlider(JSlider.HORIZONTAL, 0, 100, 100-(int)(MachineConfiguration.getSingleton().paper_margin*100));
		input_paper_margin.setMajorTickSpacing(20);
		input_paper_margin.setMinorTickSpacing(5);
		input_paper_margin.setPaintTicks(false);
		input_paper_margin.setPaintLabels(true);
		
		//final JCheckBox allow_metrics = new JCheckBox(String.valueOf("I want to add the distance drawn to the // total"));
		//allow_metrics.setSelected(allowMetrics);
		
		final JCheckBox show_pen_up = new JCheckBox("Show pen up moves");
		show_pen_up.setSelected(previewPane.getShowPenUp());

		final JCheckBox reverse_h = new JCheckBox("Flip for glass");
		reverse_h.setSelected(MachineConfiguration.getSingleton().reverseForGlass);

		String [] styles= { "Single Line Zigzag", "Spiral", "Cross hatching", "Scanlines", "RGB" };

		final JComboBox input_draw_style = new JComboBox(styles);
		input_draw_style.setSelectedIndex(GetDrawStyle());
		
		final JButton cancel = new JButton("Cancel");
		final JButton save = new JButton("Save");
		
		GridBagConstraints c = new GridBagConstraints();
		//c.gridwidth=4; 	c.gridx=0;  c.gridy=0;  driver.add(allow_metrics,c);

		c.anchor=GridBagConstraints.EAST;	c.gridwidth=1;	c.gridx=0;  c.gridy=3;  driver.add(change_sound_connect,c);								c.anchor=GridBagConstraints.WEST;	c.gridwidth=3;	c.gridx=1;  c.gridy=3;  driver.add(sound_connect,c);
		c.anchor=GridBagConstraints.EAST;	c.gridwidth=1;	c.gridx=0;  c.gridy=4;  driver.add(change_sound_disconnect,c);							c.anchor=GridBagConstraints.WEST;	c.gridwidth=3;	c.gridx=1;  c.gridy=4;  driver.add(sound_disconnect,c);
		c.anchor=GridBagConstraints.EAST;	c.gridwidth=1;	c.gridx=0;  c.gridy=5;  driver.add(change_sound_conversion_finished,c);					c.anchor=GridBagConstraints.WEST;	c.gridwidth=3;	c.gridx=1;  c.gridy=5;  driver.add(sound_conversion_finished,c);
		c.anchor=GridBagConstraints.EAST;	c.gridwidth=1;	c.gridx=0;  c.gridy=6;  driver.add(change_sound_drawing_finished,c);					c.anchor=GridBagConstraints.WEST;	c.gridwidth=3;	c.gridx=1;  c.gridy=6;  driver.add(sound_drawing_finished,c);
		c.anchor=GridBagConstraints.EAST;	c.gridwidth=1;	c.gridx=0;  c.gridy=8;  driver.add(new JLabel("Margin at paper edge (%)"),c);			c.anchor=GridBagConstraints.WEST;	c.gridwidth=3;	c.gridx=1;  c.gridy=8;  driver.add(input_paper_margin,c);
		c.anchor=GridBagConstraints.EAST;	c.gridwidth=1;	c.gridx=0;  c.gridy=9;  driver.add(new JLabel("Conversion style"),c);					c.anchor=GridBagConstraints.WEST;	c.gridwidth=3;	c.gridx=1;	c.gridy=9;	driver.add(input_draw_style,c);
		c.anchor=GridBagConstraints.WEST;	c.gridwidth=1;	c.gridx=1;  c.gridy=10;  driver.add(show_pen_up,c);
		c.anchor=GridBagConstraints.WEST;	c.gridwidth=1;  c.gridx=1;  c.gridy=11; driver.add(reverse_h,c);

		c.anchor=GridBagConstraints.EAST;	c.gridwidth=1;	c.gridx=2;  c.gridy=12;  driver.add(save,c);
		c.anchor=GridBagConstraints.WEST;	c.gridwidth=1;	c.gridx=3;  c.gridy=12;  driver.add(cancel,c);
		
		ActionListener driveButtons = new ActionListener() {
			  public void actionPerformed(ActionEvent e) {
					Object subject = e.getSource();
					if(subject == change_sound_connect) sound_connect.setText(SelectFile());
					if(subject == change_sound_disconnect) sound_disconnect.setText(SelectFile());
					if(subject == change_sound_conversion_finished) sound_conversion_finished.setText(SelectFile());
					if(subject == change_sound_drawing_finished) sound_drawing_finished.setText(SelectFile());

					if(subject == save) {
						MachineConfiguration.getSingleton().paper_margin=(100-input_paper_margin.getValue())*0.01;
						
						//allowMetrics = allow_metrics.isSelected();
						previewPane.setShowPenUp(show_pen_up.isSelected());
						MachineConfiguration.getSingleton().reverseForGlass=reverse_h.isSelected();
						
						SetDrawStyle(input_draw_style.getSelectedIndex());
						prefs.put("sound_connect",sound_connect.getText());
						prefs.put("sound_disconnect",sound_disconnect.getText());
						prefs.put("sound_conversion_finished",sound_conversion_finished.getText());
						prefs.put("sound_drawing_finished",sound_drawing_finished.getText());
						MachineConfiguration.getSingleton().SaveConfig();
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
	 * Send the machine configuration to the robot
	 */
	void SendConfig() {
		if(!portConfirmed) return;
		
		// Send a command to the robot with new configuration values
		SendLineToRobot(MachineConfiguration.getSingleton().GetConfigLine());
		SendLineToRobot(MachineConfiguration.getSingleton().GetBobbinLine());
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
	
	
	private void ChangeToTool(String changeToolString) {
		int i=Integer.parseInt(changeToolString.replace(";",""));
		
		MachineConfiguration mc = MachineConfiguration.getSingleton();
		String [] toolNames = mc.getToolNames();
		
		if(i>toolNames.length) {
			Log("<span style='color:red'>Invalid tool "+i+" requested.</span>");
			i=0;
		}
		JOptionPane.showMessageDialog(null,"Please prepare "+toolNames[i]+", then click any button to begin.");
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
			Log("<span style='color:pink'>"+line+"</span>\n");
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
		
		if(!line.endsWith(";") && !line.endsWith(";\n")) {
			line+=eol;
		}
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

	/**
	 * open a dialog to ask for the line number.
	 * @return true if "ok" is pressed, false if the window is closed any other way.
	 */
	private boolean getStartingLineNumber() {
		dialog_result=false;
		
		final JDialog driver = new JDialog(mainframe,"Start at",true);
		driver.setLayout(new GridBagLayout());		
		final JTextField starting_line = new JTextField("0",8);
		final JButton cancel = new JButton("Cancel");
		final JButton start = new JButton("Start");
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth=2;	c.gridx=0;  c.gridy=0;  driver.add(new JLabel("Skip to line #"),c);
		c.gridwidth=2;	c.gridx=2;  c.gridy=0;  driver.add(starting_line,c);
		c.gridwidth=1;	c.gridx=0;  c.gridy=1;  driver.add(cancel,c);
		c.gridwidth=1;	c.gridx=2;  c.gridy=1;  driver.add(start,c);
		
		ActionListener driveButtons = new ActionListener() {
			  public void actionPerformed(ActionEvent e) {
					Object subject = e.getSource();
					
					if(subject == start) {
						gcode.linesProcessed=Long.parseLong(starting_line.getText());
						dialog_result=true;
						driver.dispose();
					}
					if(subject == cancel) {
						dialog_result=false;
						driver.dispose();
					}
			  }
		};

		start.addActionListener(driveButtons);
		cancel.addActionListener(driveButtons);
		driver.pack();
		driver.setVisible(true);  // modal
		
		return dialog_result;
	}

	private void StartDrawing() {
		paused=false;
		running=true;
		UpdateMenuBar();
		previewPane.setRunning(running);
		previewPane.setLinesProcessed(gcode.linesProcessed);
		statusBar.Start();
		SendFileCommand();
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
		if( subject == buttonZoomToFit ) {
			previewPane.ZoomToFitPaper();
			return;
		}
		if( subject == buttonOpenFile ) {
			OpenFileDialog();
			return;
		}
		if( subject == buttonText2GCODE ) {
			TextToGCODE();
			return;
		}

		if( subject == buttonStart ) {
			if(gcode.fileOpened && !running) {
				gcode.linesProcessed=0;
				StartDrawing();
			}
			return;
		}
		if( subject == buttonStartAt ) {
			if(gcode.fileOpened && !running) {
				gcode.linesProcessed=0;
				if(getStartingLineNumber()) {
					StartDrawing();
				}
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
			AdjustPreferences();
			return;
		}
		if( subject == buttonAdjustMachineSize ) {
			MachineConfiguration.getSingleton().AdjustMachineSize();
			previewPane.updateMachineConfig();
			return;
		}
		if( subject == buttonAdjustPulleySize ) {
			MachineConfiguration.getSingleton().AdjustPulleySize();
			previewPane.updateMachineConfig();
			return;
		}
		if( subject == buttonChangeTool ) {
			MachineConfiguration.getSingleton().ChangeTool();
			previewPane.updateMachineConfig();
			return;
		}
		if( subject == buttonAdjustTool ) {
			MachineConfiguration.getSingleton().AdjustTool();
			previewPane.updateMachineConfig();
			return;
		}
		if( subject == buttonJogMotors ) {
			JogMotors();
			return;
		}
		if( subject == buttonAbout ) {
			JOptionPane.showMessageDialog(null,"<html><body>"
					+"<h1>Makelangelo v"+version+"</h1>"
					+"<h3><a href='http://www.marginallyclever.com/'>http://www.marginallyclever.com/</a></h3>"
					+"<p>Created by Dan Royer (dan@marginallyclever.com).<br>Additional contributions by Joseph Cottam.</p><br>"
					+"<p>To get the latest version please visit<br>"
					+"<a href='https://github.com/MarginallyClever/Makelangelo'>https://github.com/MarginallyClever/Makelangelo</a></p><br>"
					+"<p>This program is open source and free.  If this was helpful<br>to you, please buy me a thank you beer through Paypal.</p>"
					+"</body></html>");
			return;
		}
		if( subject == buttonCheckForUpdate ) {
			CheckForUpdate();
			return;
		}
		
		if( subject == buttonSaveFile ) {
			SaveFileDialog();
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
		
		feed_rate = MachineConfiguration.getSingleton().GetFeedRate();
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
						SendLineToRobot("G00 F"+feed_rate+" X"+(MachineConfiguration.getSingleton().paper_left *10)+" Y"+(MachineConfiguration.getSingleton().paper_top*10));
					} else if(b==TR) { 
						SendLineToRobot("G00 F"+feed_rate+" X"+(MachineConfiguration.getSingleton().paper_right*10)+" Y"+(MachineConfiguration.getSingleton().paper_top*10));
					} else if(b==BL) { 
						SendLineToRobot("G00 F"+feed_rate+" X"+(MachineConfiguration.getSingleton().paper_left *10)+" Y"+(MachineConfiguration.getSingleton().paper_bottom*10));
					} else if(b==BR) { 
						SendLineToRobot("G00 F"+feed_rate+" X"+(MachineConfiguration.getSingleton().paper_right*10)+" Y"+(MachineConfiguration.getSingleton().paper_bottom*10));
					} else if(b==find) {
						SendLineToRobot("G28");
					} else if(b==center) {
						SendLineToRobot("TELEPORT X0 Y0");
					} else if(b==z90) {
						RaisePen();
					} else if(b==z0) {
						LowerPen();
					} else if(b==setFeedRate) {
						String fr=feedRate.getText();
						fr=fr.replaceAll("[ ,]","");
						feed_rate = Double.parseDouble(fr);
						if(feed_rate<0.001) feed_rate=0.001;
						MachineConfiguration.getSingleton().SetFeedRate(feed_rate);
						feedRate.setText(Double.toString(feed_rate));
						SendLineToRobot("G00 G21 F"+feed_rate);
					} else {
						SendLineToRobot("G91");  // set relative mode
						SendLineToRobot("G00 G21 F"+feed_rate+" "+b.getText());
						SendLineToRobot("G90");  // return to absolute mode
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
		setFeedRate.addActionListener(driveButtons);
		
		return driver;
	}
	
	protected void JogMotors() {
		JDialog driver = new JDialog(mainframe,"Jog Motors",true);
		driver.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		final JButton buttonAneg = new JButton("IN");
		final JButton buttonApos = new JButton("OUT");
		final JCheckBox m1i = new JCheckBox("Invert",MachineConfiguration.getSingleton().m1invert);
		
		final JButton buttonBneg = new JButton("IN");
		final JButton buttonBpos = new JButton("OUT");
		final JCheckBox m2i = new JCheckBox("Invert",MachineConfiguration.getSingleton().m2invert);

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
				MachineConfiguration.getSingleton().m1invert = m1i.isSelected();
				MachineConfiguration.getSingleton().m2invert = m2i.isSelected();
				
				MachineConfiguration.getSingleton().SaveConfig();
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
			URL github = new URL("https://www.marginallyclever.com/other/software-update-check.php?id=1");
	        BufferedReader in = new BufferedReader(new InputStreamReader(github.openStream()));

	        String inputLine;
	        if((inputLine = in.readLine()) != null) {
	        	if( inputLine.compareTo(version) !=0 ) {
	        		JOptionPane.showMessageDialog(null,"A new version of this software is available.  The latest version is "+inputLine+"\n"
	        											+"Please visit http://makelangelo.com/ to get the new hotness.");
	        	} else {
	        		JOptionPane.showMessageDialog(null,"This version is up to date.");
	        	}
	        } else {
	        	throw new Exception();
	        }
	        in.close();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,"Sorry, I failed.  Please visit http://www.marginallyclever.com/ to check yourself.");
		}
	}

	// Rebuild the contents of the menu based on current program state
	public void UpdateMenuBar() {
		JMenu menu, subMenu;
		ButtonGroup group;
        int i;
        
        menuBar.removeAll();
        
        // File menu
        menu = new JMenu("Makelangelo");
        menu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menu);
        
        buttonAbout = new JMenuItem("About",KeyEvent.VK_A);
        menu.getAccessibleContext().setAccessibleDescription("Find out about this program");
        buttonAbout.addActionListener(this);
        menu.add(buttonAbout);

        buttonCheckForUpdate = new JMenuItem("Check for updates",KeyEvent.VK_U);
        menu.getAccessibleContext().setAccessibleDescription("Is there a newer version available?");
        buttonCheckForUpdate.addActionListener(this);
        buttonCheckForUpdate.setEnabled(true);
        menu.add(buttonCheckForUpdate);

        menu.addSeparator();
        
        buttonExit = new JMenuItem("Exit",KeyEvent.VK_Q);
        buttonExit.getAccessibleContext().setAccessibleDescription("Goodbye...");
        buttonExit.addActionListener(this);
        menu.add(buttonExit);
        
        

        // settings menu
        menu = new JMenu("Settings");
        menu.setMnemonic(KeyEvent.VK_T);
        menu.getAccessibleContext().setAccessibleDescription("Adjust the robot settings.");

        subMenu = new JMenu("Port");
        subMenu.getAccessibleContext().setAccessibleDescription("What port to connect to?");
        subMenu.setEnabled(!running);
        group = new ButtonGroup();

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

        buttonAdjustMachineSize = new JMenuItem("Adjust machine size",KeyEvent.VK_L);
        buttonAdjustMachineSize.addActionListener(this);
        buttonAdjustMachineSize.setEnabled(!running);
        menu.add(buttonAdjustMachineSize);

        buttonAdjustPulleySize = new JMenuItem("Adjust pulley size",KeyEvent.VK_B);
        buttonAdjustPulleySize.addActionListener(this);
        buttonAdjustPulleySize.setEnabled(!running);
        menu.add(buttonAdjustPulleySize);
        
        buttonJogMotors = new JMenuItem("Jog Motors",KeyEvent.VK_J);
        buttonJogMotors.addActionListener(this);
        buttonJogMotors.setEnabled(portConfirmed && !running);
        menu.add(buttonJogMotors);

        menu.addSeparator();
        
        buttonChangeTool = new JMenuItem("Select Tool",KeyEvent.VK_T);
        buttonChangeTool.getAccessibleContext().setAccessibleDescription("Change the tool.");
        buttonChangeTool.addActionListener(this);
        buttonChangeTool.setEnabled(!running);
        menu.add(buttonChangeTool);

        buttonAdjustTool = new JMenuItem("Adjust Current Tool",KeyEvent.VK_B);
        buttonAdjustTool.getAccessibleContext().setAccessibleDescription("Adjust the tool.");
        buttonAdjustTool.addActionListener(this);
        buttonAdjustTool.setEnabled(!running);
        menu.add(buttonAdjustTool);

        menu.addSeparator();
        
        buttonConfigurePreferences = new JMenuItem("Preferences");
        buttonConfigurePreferences.getAccessibleContext().setAccessibleDescription("Adjust miscelaneous preferences.");
        buttonConfigurePreferences.addActionListener(this);
        buttonConfigurePreferences.setEnabled(!running);
        menu.add(buttonConfigurePreferences);
        
        menuBar.add(menu);

        

        // File conversion menu
        menu = new JMenu("GCODE");
        menu.setMnemonic(KeyEvent.VK_H);
        menu.getAccessibleContext().setAccessibleDescription("Get help");

        buttonText2GCODE = new JMenuItem("Text to GCODE");
        buttonText2GCODE.setEnabled(!running);
        buttonText2GCODE.addActionListener(this);
        menu.add(buttonText2GCODE);
        
        subMenu = new JMenu("Open/Convert File...");
        subMenu.getAccessibleContext().setAccessibleDescription("Open a g-code file");
        subMenu.setEnabled(!running);
        group = new ButtonGroup();

	        // list recent files
	        if(recentFiles != null && recentFiles.length>0) {
	        	// list files here
	        	for(i=0;i<recentFiles.length;++i) {
	        		if(recentFiles[i] == null || recentFiles[i].length()==0) break;
	            	buttonRecent[i] = new JMenuItem((1+i) + " "+recentFiles[i],KeyEvent.VK_1+i);
	            	if(buttonRecent[i]!=null) {
	            		buttonRecent[i].addActionListener(this);
	            		subMenu.add(buttonRecent[i]);
	            	}
	        	}
	        	if(i!=0) subMenu.addSeparator();
	        }
        
	        buttonOpenFile = new JMenuItem("Open File...",KeyEvent.VK_O);
	        buttonOpenFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.ALT_MASK));
	        buttonOpenFile.getAccessibleContext().setAccessibleDescription("Open a g-code file...");
	        buttonOpenFile.addActionListener(this);
	        subMenu.add(buttonOpenFile);
        
        menu.add(subMenu);

        buttonSaveFile = new JMenuItem("Save GCODE as...");
        buttonSaveFile.getAccessibleContext().setAccessibleDescription("Save the current g-code file...");
        buttonSaveFile.addActionListener(this);
        menu.add(buttonSaveFile);

        menuBar.add(menu);
        
        
        
        // Draw menu
        menu = new JMenu("Draw");
        menu.getAccessibleContext().setAccessibleDescription("Start & Stop progress");

        buttonStart = new JMenuItem("Start",KeyEvent.VK_S);
        buttonStart.getAccessibleContext().setAccessibleDescription("Start sending g-code");
        buttonStart.addActionListener(this);
    	buttonStart.setEnabled(portConfirmed && !running);
        menu.add(buttonStart);

        buttonStartAt = new JMenuItem("Start at line...");
        buttonStartAt.getAccessibleContext().setAccessibleDescription("Start sending g-code");
        buttonStartAt.addActionListener(this);
        buttonStartAt.setEnabled(portConfirmed && !running);
        menu.add(buttonStartAt);

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
        
        // view menu
        menu = new JMenu("Preview");
        buttonZoomOut = new JMenuItem("Zoom -");
        buttonZoomOut.addActionListener(this);
        buttonZoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS,ActionEvent.ALT_MASK));
        menu.add(buttonZoomOut);
        
        buttonZoomIn = new JMenuItem("Zoom +",KeyEvent.VK_EQUALS);
        buttonZoomIn.addActionListener(this);
        buttonZoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS,ActionEvent.ALT_MASK));
        menu.add(buttonZoomIn);
        
        buttonZoomToFit = new JMenuItem("Zoom to fit");
        buttonZoomToFit.addActionListener(this);
        menu.add(buttonZoomToFit);
        
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

    /*
	// connect to the last port
    private void reconnectToLastPort() {
	    ListSerialPorts();
		if(Arrays.asList(portsDetected).contains(recentPort)) {
			OpenPort(recentPort);
		}
	}
    
    // if the default file being opened in a g-code file, this is ok.  Otherwise it may take too long and look like a crash/hang.
    private void reopenLastFile() {
		if(recentFiles[0].length()>0) {
			OpenFileOnDemand(recentFiles[0]);
		}
    }
    */
    
    public JFrame getParentFrame() {
    	return mainframe;
    }
    
    // Create the GUI and show it.  For thread safety, this method should be invoked from the event-dispatching thread.
    private static void CreateAndShowGUI() {
        //Create and set up the window.
    	mainframe = new JFrame("Makelangelo not connected");
        mainframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        //Create and set up the content pane.
        Makelangelo demo = Makelangelo.getSingleton();
        mainframe.setJMenuBar(demo.CreateMenuBar());
        mainframe.setContentPane(demo.CreateContentPane());
 
        //Display the window.
        // TODO remember preferences for window size
        mainframe.setSize(1200,700);
        mainframe.setVisible(true);
        
        demo.previewPane.ZoomToFitPaper();
        //demo.reconnectToLastPort();
        //demo.reopenLastFile();
        //demo.CheckForUpdate();
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