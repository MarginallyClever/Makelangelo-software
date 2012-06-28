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

import java.io.*;
import java.util.*;
import java.util.prefs.Preferences;



public class DrawbotGUI
		extends JPanel
		implements ActionListener, SerialPortEventListener
{
	static final long serialVersionUID=1;
	static private final String cue = "> ";
	static private final String eol = ";";
	static private final String NL = System.getProperty("line.separator");

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
	
	// Preferences
	private Preferences prefs = Preferences.userRoot().node("DrawBot");
	private String[] recentFiles = {"","","","","","","","","",""};
	private String recentPort;
	
	// Robot config
	private double limit_top=10;
	private double limit_bottom=-10;
	private double limit_left=-10;
	private double limit_right=10;
	
	// paper area (stock material
	private double paper_top=10;
	private double paper_bottom=-10;
	private double paper_left=-10;
	private double paper_right=10;
	
	// GUI elements
	private static JFrame mainframe;
	private JMenuBar menuBar;
    private JMenuItem buttonOpenFile, buttonExit;
    private JMenuItem buttonConfig, buttonPaper, buttonRescan, buttonLoad, buttonHome;
    private JMenuItem buttonStart, buttonPause, buttonHalt, buttonDrive;
    private JMenuItem buttonAbout;
    
    private JMenuItem [] buttonRecent = new JMenuItem[10];
    private JMenuItem [] buttonPorts;

    private JTextArea log;
    private JScrollPane logPane;
    private DrawPanel previewPane;
	private StatusBar statusBar;
	
	// parsing input from Drawbot
	private String line3="";

	// reading file
	private boolean running=false;
	private boolean paused=true;
	private Scanner scanner;
    private long linesTotal=0;
	private long linesProcessed=0;
	private boolean fileOpened=false;
	private ArrayList<String> gcode;
	
	
	// Singleton stuff
	private DrawbotGUI() {}
	
	public static DrawbotGUI getSingleton() {
		if(singletonObject==null) {
			singletonObject = new DrawbotGUI();
		}
		return singletonObject;
	}
	
	
	//  data access
	public ArrayList<String> getGcode() {
		return gcode;
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

			Filter_Resize rs = new Filter_Resize(paper_top,paper_bottom,paper_left,paper_right,5,0.9f); 
			img = rs.Process(img);
			
			Filter_BlackAndWhite bwc = new Filter_BlackAndWhite(); 
			img = bwc.Process(img);
			
			Filter_DitherFloydSteinberg dither = new Filter_DitherFloydSteinberg();
			img = dither.Process(img);
	
			String ngcPair = filename.substring(0, filename.lastIndexOf('.')) + ".ngc";
			Filter_TSPGcodeGenerator tsp = new Filter_TSPGcodeGenerator(ngcPair);
			tsp.Process(img);
	
			OpenFile(ngcPair);
		}
		catch(IOException e) {}
	}
	
	
	
	// appends a message to the log tab and system out.
	public void Log(String msg) {
		log.append(msg);
		log.setCaretPosition(log.getText().length());
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

			log.setText("");
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
		
		//Log("Connecting to "+portName+"..."+NL);
		
		// find the port
		try {
			portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
		}
		catch(Exception e) {
			Log("Ports could not be identified:"+e.getMessage()+NL);
			e.printStackTrace();
			return 1;
		}

		if ( portIdentifier.isCurrentlyOwned() ) {
    	    Log("Error: Another program is currently using this port."+NL);
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
			Log("Error: Only serial ports are handled by this example."+NL);
			return 4;
		}

		// set the port parameters (like baud rate)
		serialPort = (SerialPort)commPort;
		try {
			serialPort.setSerialPortParams(BAUD_RATE,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
		}
		catch(Exception e) {
			Log("Port could not be configured:"+e.getMessage()+NL);
			return 5;
		}

		try {
			in = serialPort.getInputStream();
			out = serialPort.getOutputStream();
		}
		catch(Exception e) {
			Log("Streams could not be opened:"+e.getMessage()+NL);
			return 6;
		}
		
		try {
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
		}
		catch(TooManyListenersException e) {
			Log("Streams could not be opened:"+e.getMessage()+NL);
			return 7;
		}

		Log("Opened.\n");
		SetRecentPort(portName);
		portOpened=true;
		UpdateMenuBar();
		
		return 0;
	}

	

	// complete the handshake, update the menu, repaint the preview with the limits.
	public boolean ConfirmPort() {
		if(portConfirmed==true) return true;
		int found=line3.lastIndexOf("== HELLO WORLD ==");
		if(found >= 0) {
			String[] lines = line3.substring(found).split("\\r?\\n");
			try {
				limit_top = Float.parseFloat(lines[1].substring(1));
				limit_bottom = Float.parseFloat(lines[2].substring(1));
				limit_left = Float.parseFloat(lines[3].substring(1));
				limit_right = Float.parseFloat(lines[4].substring(1));
				portConfirmed=true;
				UpdateMenuBar();
				previewPane.setConnected(true);
				previewPane.setMachineLimits(limit_top, limit_bottom, limit_left, limit_right);
			}
			catch(NumberFormatException e) {}
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
		recentPort = prefs.get("recent-port", portsDetected[0]);
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
		prefs.putDouble("paper_left", paper_left);
		prefs.putDouble("paper_right", paper_right);
		prefs.putDouble("paper_top", paper_top);
		prefs.putDouble("paper_bottom", paper_bottom);
		previewPane.setPaperSize(paper_top,paper_bottom,paper_left,paper_right);
	}

	
	
	public void GetRecentPaperSize() {
		paper_left=Double.parseDouble(prefs.get("paper_left","-10"));
		paper_right=Double.parseDouble(prefs.get("paper_right","10"));
		paper_top=Double.parseDouble(prefs.get("paper_top","10"));
		paper_bottom=Double.parseDouble(prefs.get("paper_bottom","-10"));
		previewPane.setPaperSize(paper_top,paper_bottom,paper_left,paper_right);
	}

	
	
	// close the file, clear the preview tab
	public void CloseFile() {
		if(fileOpened==true && scanner != null) scanner.close();
	   	fileOpened=false;
	}
	
	
	
	// Opens the file.  If the file can be opened, repaint the preview tab.
	public void OpenFile(String filename) {
		CloseFile();

	    try {
	    	scanner = new Scanner(new FileInputStream(filename));
	    	linesTotal=0;
	    	gcode = new ArrayList<String>();
		    try {
		      while (scanner.hasNextLine()) {
		    	  gcode.add(scanner.nextLine());
		    	  ++linesTotal;
		      }
		    }
		    finally{
		      scanner.close();
		    }

    		scanner = new Scanner(new FileInputStream(filename));
	    }
	    catch(IOException e) {
	    	Log("File could not be opened."+NL);
	    	RemoveRecentFile(filename);
	    	return;
	    }
		
	   	UpdateRecentFiles(filename);

	   	Log(linesTotal + " lines.\n");
	    fileOpened=true;
	    paused=true;
	    linesProcessed=0;

	    previewPane.repaint();
	}
	
	
	
	// changes the order of the recent files list in the File submenu,
	// saves the updated prefs, and refreshes the menus.
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
			if(recentFiles[i] != null) prefs.put("recent-files-"+i, recentFiles[i]);
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
			if(recentFiles[i] != null) prefs.put("recent-files-"+i, recentFiles[i]);
		}
		
		UpdateMenuBar();
	}
	
	
	
	// Load recent files from prefs
	public void GetRecentFiles() {
		int i;
		for(i=0;i<recentFiles.length;++i) {
			recentFiles[i] = prefs.get("recent-files-"+i, recentFiles[i]);
		}
	}	
	
	

	// User has asked that a file be opened.
	public void OpenFileOnDemand(String filename) {
		Log("Opening file "+recentFiles[0]+"..."+NL);
		
		String ext=filename.substring(filename.lastIndexOf('.'));
    	if(!ext.equalsIgnoreCase(".ngc")) {
//    		String ngcPair = filename.substring(0, filename.lastIndexOf('.')) + ".ngc";
//    		if(!(new File(ngcPair)).exists()) {
    			LoadImage(filename);
//    		}
    	} else {
    		OpenFile(filename);
    	}

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
	    	OpenFileOnDemand(fc.getSelectedFile().getAbsolutePath());
	    }
	}
	
	
	
	public void GoHome() {
		String line="HOME;";
		Log(line+NL);
		try {
			out.write(line.getBytes());
		}
		catch(IOException e) {}
	}
	
	
	
	/**
	 * Open the load dialog, load the spools.
	 */
	public void UpdateLoad() {
		JTextField left = new JTextField("500");
		JTextField right = new JTextField("500");
		final JComponent[] inputs = new JComponent[] {
						new JLabel("Measurements are in cm.  Positive value winds in."),
		                new JLabel("Left"), 	left,
		                new JLabel("Right"), 	right
		};
		JOptionPane.showMessageDialog(null, inputs, "Load Bobbins", JOptionPane.PLAIN_MESSAGE);

		String line="LOAD";
		if(left.getText().trim() !="") line+=" L"+left.getText().trim();
		if(right.getText().trim()!="") line+=" R"+right.getText().trim();
		line+=";";
		Log(line+NL);
		try {
			out.write(line.getBytes());
		}
		catch(IOException e) {}
	}

	
	
	/**
	 * Open the config dialog, send the config update to the robot, refresh the preview tab.
	 */
	public void UpdateConfig() {
		JTextField top = new JTextField(String.valueOf(limit_top));
		JTextField bottom = new JTextField(String.valueOf(limit_bottom));
		JTextField left = new JTextField(String.valueOf(limit_left));
		JTextField right = new JTextField(String.valueOf(limit_right));
		final JComponent[] inputs = new JComponent[] {
						new JLabel("Measurements are from your calibration point, in cm.  Left and Bottom should be negative."),
		                new JLabel("Top"), 		top,
		                new JLabel("Bottom"),	bottom,
		                new JLabel("Left"), 	left,
		                new JLabel("Right"), 	right
		};
		JOptionPane.showMessageDialog(null, inputs, "Config machine limits", JOptionPane.PLAIN_MESSAGE);

		if(left.getText().trim()!="" || right.getText().trim()!="" ||
			top.getText().trim()!="" || bottom.getText().trim()!="") {
			// Send a command to the robot with new configuration values
			String line="CONFIG T"+top.getText()+" B"+bottom.getText()+" L"+left.getText()+" R"+right.getText()+";";
			Log(line+NL);

			try {
				out.write(line.getBytes());
			}
			catch(IOException e) {}
			
			limit_top = Float.valueOf(top.getText());
			limit_bottom = Float.valueOf(bottom.getText());
			limit_right = Float.valueOf(right.getText());
			limit_left = Float.valueOf(left.getText());
			previewPane.setMachineLimits(limit_top, limit_bottom, limit_left, limit_right);
		}
	}

	
	
	/**
	 * Open the config dialog, update the paper size, refresh the preview tab.
	 */
	public void UpdatePaper() {
		JTextField top = new JTextField(String.valueOf(paper_top));
		JTextField bottom = new JTextField(String.valueOf(paper_bottom));
		JTextField left = new JTextField(String.valueOf(paper_left));
		JTextField right = new JTextField(String.valueOf(paper_right));
		final JComponent[] inputs = new JComponent[] {
						new JLabel("Measurements are from your calibration point, in cm.  Left and Bottom should be negative."),
		                new JLabel("Top"), 		top,
		                new JLabel("Bottom"),	bottom,
		                new JLabel("Left"), 	left,
		                new JLabel("Right"), 	right
		};
		JOptionPane.showMessageDialog(null, inputs, "Config paper limits", JOptionPane.PLAIN_MESSAGE);

		if(left.getText().trim()!="" || right.getText().trim()!="" ||
			top.getText().trim()!="" || bottom.getText().trim()!="") {
			paper_top = Float.valueOf(top.getText());
			paper_bottom = Float.valueOf(bottom.getText());
			paper_right = Float.valueOf(right.getText());
			paper_left = Float.valueOf(left.getText());
			SetRecentPaperSize();
		}
	}

	
	
	// Take the next line from the file and send it to the robot, if permitted. 
	public void SendFileCommand() {
		if(paused==true || fileOpened==false || portConfirmed==false || linesProcessed>=linesTotal) return;
		
		String line;
		do {			
			// are there any more commands?
			line=scanner.nextLine().trim();
			++linesProcessed;
			previewPane.setLinesProcessed(linesProcessed);
			statusBar.SetProgress(linesProcessed, linesTotal);
			// loop until we find a line that gets sent to the robot, at which point we'll
			// pause for the robot to respond.  Also stop at end of file.
		} while(!SendLineToRobot(line) && linesProcessed<linesTotal);
		
		if(linesProcessed==linesTotal) {
			// end of file
			Halt();
		}
	}
	
	
	
	// last minute scale & translate the image 
	public String ProcessLine(String line) {
/*
 		String newLine = "";
		String first="";
		
		double f;
		String[] tokens = line.split("\\s");
		int j;
		for(j=0;j<tokens.length;++j) {
			newLine+=first;
			     if(tokens[j].startsWith("X")) {  f = (Float.valueOf(tokens[j].substring(1))*imageScale)-imageOffsetX;  newLine+="X"+f;  }
			else if(tokens[j].startsWith("Y")) {  f = (Float.valueOf(tokens[j].substring(1))*imageScale)-imageOffsetY;  newLine+="Y"+f;  }
			else if(tokens[j].startsWith("Z")) {  f =  Float.valueOf(tokens[j].substring(1));                           newLine+="Z"+f;  }
			else if(tokens[j].startsWith("I")) {  f = (Float.valueOf(tokens[j].substring(1))*imageScale);               newLine+="I"+f;  }
			else if(tokens[j].startsWith("J")) {  f = (Float.valueOf(tokens[j].substring(1))*imageScale);               newLine+="J"+f;  }
			else newLine+=tokens[j];
			first=" ";
		}
		return newLine;
*/
		return line;
	}
	
	
	
	// processes a single instruction meant for the robot.  Could be anything.
	// return true if the command is sent to the robot.
	// return false if it is not.
	public boolean SendLineToRobot(String line) {
		// tool change request?
		String [] tokens = line.split("\\s");

		// tool change?
		if(Arrays.asList(tokens).contains("M06") || Arrays.asList(tokens).contains("M6")) {
			for(int i=0;i<tokens.length;++i) {
				if(tokens[i].startsWith("T")) {
					JOptionPane.showMessageDialog(null,"Please change to tool #"+tokens[i].substring(1)+" and click OK.");
				}
			}
			// still ready to send
			return false;
		}
		
		// end of program?
		if(tokens[0]=="M02" || tokens[0]=="M2") {
			running=false;
			CloseFile();
			Log(line+NL);
			return false;
		}
		
		// other machine code to ignore?
		if(tokens[0].startsWith("M")) {
			Log(line+NL);
			return false;
		} 

		// contains a comment?  if so remove it
		int index=line.indexOf('(');
		if(index!=-1) {
			String comment=line.substring(index+1,line.lastIndexOf(')'));
			line=line.substring(0,index).trim();
			Log("* "+comment+NL);
			if(line.length()==0) {
				// entire line was a comment.
				return false;  // still ready to send
			}
		}

		// send relevant part of line to the robot
		line=ProcessLine(line)+eol;
		Log(line+NL);
		try {
			out.write(line.getBytes());
		}
		catch(IOException e) {}
		
		return true;
	}
	
	
	
	
	/**
	 * stop sending commands to the robot.
	 * @todo add an e-stop command?
	 */
	public void Halt() {
		CloseFile();
		OpenFile(recentFiles[0]);
		running=false;
		paused=true;
		UpdateMenuBar();
	}
	
	
	
	// The user has done something.  respond to it.
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();
		
		if(subject == buttonOpenFile) {
			OpenFileDialog();
			return;
		}

		if( subject == buttonStart ) {
			if(fileOpened) OpenFile(recentFiles[0]);
			if(fileOpened) {
				paused=false;
				running=true;
				UpdateMenuBar();
				linesProcessed=0;
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
		if( subject == buttonDrive ) {
			Drive();
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
		if( subject == buttonConfig ) {
			UpdateConfig();
			return;
		}
		if( subject == buttonPaper ) {
			UpdatePaper();
			return;
		}
		if( subject == buttonLoad ) {
			UpdateLoad();
			return;
		}	
		if( subject == buttonHome ) {
			GoHome();
			return;
		}		
		if(subject == buttonAbout ) {
			JOptionPane.showMessageDialog(null,"Created by Dan Royer (dan@marginallyclever.com)."+NL+NL
					+"Find out more at http://www.marginallyclever.com/"+NL
					+"Get the latest version and read the documentation online @ http://github.com/i-make-robots/DrawBot/");
			return;
		}
		if(subject == buttonExit) {
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
						Log(line2);
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
	
	

	public JMenuBar CreateMenuBar() {
        // If the menu bar exists, empty it.  If it doesn't exist, create it.
        menuBar = new JMenuBar();

        UpdateMenuBar();
        
        return menuBar;
	}

	
	
	/**
	 * Open the config dialog, update the paper size, refresh the preview tab.
	 */
	public void Drive() {
		JDialog driver = new JDialog(mainframe,"Manual Control",true);
		driver.setLayout(new GridBagLayout());
		
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
		
		JButton center = new JButton("CENTERED");
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=3;	c.gridy=0;	driver.add(up100,c);
		c.gridx=3;	c.gridy=1;	driver.add(up10,c);
		c.gridx=3;	c.gridy=2;	driver.add(up1,c);
		c.gridx=3;	c.gridy=4;	driver.add(down1,c);
		c.gridx=3;	c.gridy=5;	driver.add(down10,c);
		c.gridx=3;	c.gridy=6;	driver.add(down100,c);

		c.gridx=3;	c.gridy=3;	driver.add(center,c);
		
		c.gridx=0;	c.gridy=3;	driver.add(left100,c);
		c.gridx=1;	c.gridy=3;	driver.add(left10,c);
		c.gridx=2;	c.gridy=3;	driver.add(left1,c);
		c.gridx=4;	c.gridy=3;	driver.add(right1,c);
		c.gridx=5;	c.gridy=3;	driver.add(right10,c);
		c.gridx=6;	c.gridy=3;	driver.add(right100,c);

		ActionListener driveButtons = new ActionListener() {
			  public void actionPerformed(ActionEvent e) {
					Object subject = e.getSource();
					JButton b = (JButton)subject;
					String t=b.getText();
					if(t=="CENTERED") {
						SendLineToRobot("TELEPORT XO YO");
					} else {
						SendLineToRobot("G91");
						SendLineToRobot("G00 "+b.getText());
						SendLineToRobot("G90");
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
		center.addActionListener(driveButtons);
		driver.pack();
		driver.setVisible(true);
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
        GetRecentFiles();
        if(recentFiles.length>0) {
        	// list files here
        	for(i=0;i<recentFiles.length;++i) {
        		if(recentFiles[i].length()==0) break;
            	buttonRecent[i] = new JMenuItem((1+i) + " "+recentFiles[i],KeyEvent.VK_1+i);
            	buttonRecent[i].addActionListener(this);
            	menu.add(buttonRecent[i]);
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
        subMenu.setMnemonic(KeyEvent.VK_P);
        subMenu.getAccessibleContext().setAccessibleDescription("What port to connect to?");
        subMenu.setEnabled(!running);
        ButtonGroup group = new ButtonGroup();

        ListSerialPorts();
        GetRecentPort();
        buttonPorts = new JRadioButtonMenuItem[portsDetected.length];
        for(i=0;i<portsDetected.length;++i) {
        	buttonPorts[i] = new JRadioButtonMenuItem(portsDetected[i]);
            if(recentPort.equals(portsDetected[i])) buttonPorts[i].setSelected(true);
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

        buttonConfig = new JMenuItem("Configure machine limits",KeyEvent.VK_C);
        buttonConfig.getAccessibleContext().setAccessibleDescription("Adjust the robot shape.");
        buttonConfig.addActionListener(this);
        buttonConfig.setEnabled(portConfirmed && !running);
        menu.add(buttonConfig);

        buttonPaper = new JMenuItem("Configure paper limits",KeyEvent.VK_C);
        buttonPaper.getAccessibleContext().setAccessibleDescription("Adjust the paper shape.");
        buttonPaper.addActionListener(this);
        buttonPaper.setEnabled(!running);
        menu.add(buttonPaper);

        buttonLoad = new JMenuItem("Load bobbins");
        buttonLoad.getAccessibleContext().setAccessibleDescription("Load string onto the bobbin.");
        buttonLoad.addActionListener(this);
        buttonLoad.setEnabled(portConfirmed && !running);
        menu.add(buttonLoad);

        buttonHome = new JMenuItem("Home",KeyEvent.VK_O);
        buttonHome.getAccessibleContext().setAccessibleDescription("Recenter the plotter");
        buttonHome.addActionListener(this);
        buttonHome.setEnabled(portConfirmed && !running);
        menu.add(buttonHome);

        menuBar.add(menu);

        // Draw menu
        menu = new JMenu("Draw");
        menu.setMnemonic(KeyEvent.VK_D);
        menu.getAccessibleContext().setAccessibleDescription("Start & Stop progress");
        menu.setEnabled(portConfirmed);

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

        menu.addSeparator();

        buttonDrive = new JMenuItem("Drive Manually",KeyEvent.VK_R);
        buttonDrive.getAccessibleContext().setAccessibleDescription("Etch-a-sketch style driving");
        buttonDrive.addActionListener(this);
        buttonDrive.setEnabled(portConfirmed && !running);
        menu.add(buttonDrive);

        menuBar.add(menu);

        //Build in the menu bar.
        menu = new JMenu("Help");
        menu.setMnemonic(KeyEvent.VK_H);
        menu.getAccessibleContext().setAccessibleDescription("Get help");

        buttonAbout = new JMenuItem("About",KeyEvent.VK_A);
        menu.getAccessibleContext().setAccessibleDescription("Find out about this program");
        buttonAbout.addActionListener(this);
        menu.add(buttonAbout);

        menuBar.add(menu);

        // finish
        menuBar.updateUI();
    }
	
	
	
    public Container CreateContentPane() {
        //Create the content-pane-to-be.
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setOpaque(true);
        
        // the log panel
        log = new JTextArea();
        log.setEditable(false);
        log.setForeground(Color.GREEN);
        log.setBackground(Color.BLACK);
        logPane = new JScrollPane(log);
        
        // the preview panel
        previewPane = new DrawPanel();
        
        // status bar
        statusBar = new StatusBar();

        // layout
        Splitter split = new Splitter(JSplitPane.VERTICAL_SPLIT);
        //split.add(tabs);
        split.add(previewPane);
        split.add(logPane);
        split.setDividerSize(2);
        
        contentPane.add(split,BorderLayout.CENTER);
        contentPane.add(statusBar,BorderLayout.SOUTH);

        // open the file
		GetRecentFiles();
		if(recentFiles[0].length()>0) {
			OpenFileOnDemand(recentFiles[0]);
		}
		
		// connect to the last port
		ListSerialPorts();
		GetRecentPort();
		if(Arrays.asList(portsDetected).contains(recentPort)) {
			OpenPort(recentPort);
		}
		
		GetRecentPaperSize();
		
        return contentPane;
    }
    
    
    
    // Create the GUI and show it.  For thread safety, this method should be invoked from the event-dispatching thread.
    private static void CreateAndShowGUI() {
        //Create and set up the window.
    	mainframe = new JFrame("Drawbot GUI");
        mainframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        //Create and set up the content pane.
        DrawbotGUI demo = DrawbotGUI.getSingleton();
        mainframe.setJMenuBar(demo.CreateMenuBar());
        mainframe.setContentPane(demo.CreateContentPane());
 
        //Display the window.
        mainframe.setSize(800,700);
        mainframe.setVisible(true);
    }
    
    
    
    public static void main(String[] args) {
	    //Schedule a job for the event-dispatching thread:
	    //creating and showing this application's GUI.
	    javax.swing.SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	            CreateAndShowGUI();
	        }
	    });
    }
}
