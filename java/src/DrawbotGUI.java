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
import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.prefs.Preferences;




public class DrawbotGUI
		extends JPanel
		implements ActionListener, SerialPortEventListener
{
	static final long serialVersionUID=1;
	static private final String cue = "> ";
	static private final String eol = ";";
	static private final String NL = System.getProperty("line.separator");;

	// Serial connection
	static int BAUD_RATE = 57600;
	CommPortIdentifier portIdentifier;
	CommPort commPort;
	SerialPort serialPort;
	InputStream in;
	OutputStream out;
	String[] portsDetected;
	boolean portOpened=false;
	boolean portConfirmed=false;
	boolean readyToReceive=false;
	
	// Preferences
	Preferences prefs = Preferences.userRoot().node("DrawBot");
	String[] recentFiles = {"","","","","","","","","",""};
	String recentPort;
	
	// Robot config
	double limit_top, limit_bottom, limit_left, limit_right;
	
	// GUI elements
    JMenuBar menuBar;
    JMenuItem buttonOpenFile, buttonExit;
    JMenuItem buttonConfig, buttonRescan, buttonHome;
    JMenuItem buttonStart, buttonPause, buttonHalt, buttonDrive;
    JCheckBoxMenuItem buttonMoveImage;
    JMenuItem buttonAbout;
    
    JMenuItem [] buttonRecent = new JMenuItem[10];
    JMenuItem [] buttonPorts;

    // tabs
	JTextArea log;
	JScrollPane logPane;
	JTextArea ngcfile;
	//JScrollPane filePane;
	DrawPanel previewPane;

	// status bar
	StatusBar statusBar;
	DecimalFormat fmt = new DecimalFormat("#.##");
	
	// parsing input from drawbot
	String line3="";

	// reading file
	boolean running=false;
	boolean paused=true;
    Scanner scanner;
	long linesTotal=0;
	long linesProcessed=0;
	boolean fileOpened=false;

	// movement style
	final int MODE_MOVE_CAMERA = 0; 
	final int MODE_MOVE_IMAGE  = 1;

	int movementMode=MODE_MOVE_CAMERA;
	
	// scale image to fit machine
	double imageScale=1;
	double imageOffsetX=0;
	double imageOffsetY=0;
	
	// driving controls
	boolean driving=false;
	double driveScale=100;
	double deadZone=20;
	double maxZone=100;
	
	
	
	// manages the status bar at the bottom of the application window
	public class StatusBar extends JLabel {
		static final long serialVersionUID=1;
		
	    /** Creates a new instance of StatusBar */
	    public StatusBar() {
	        super();
	        super.setPreferredSize(new Dimension(100, 16));
	        SetMessage("Ready");
	    }
	    
	    public void SetMessage(String message) {
	        setText(" "+message);        
	    }        
	    
	    public void SetProgress(long sofar,long total,String msg) {
	    	float progress=0;
	    	if(total>0) progress = 100.0f*(float)sofar/(float)total;
		   	statusBar.SetMessage(fmt.format(progress)+"% ("+sofar+"/"+total+") "+msg);
	    }
	}
	  
	

	// manages the vertical split in the GUI
	public class Splitter extends JSplitPane {
		static final long serialVersionUID=1;
		
		public Splitter(int split_direction) {
			super(split_direction);
			setResizeWeight(0.8);
			setDividerLocation(0.8);
		}
	}
	
	
	
	// Custom drawing panel written as an inner class to access the instance variables.
	public class DrawPanel extends JPanel implements MouseListener, MouseInputListener  {
		static final long serialVersionUID=2;

		// arc smoothness - increase to make more smooth and run slower.
		double steps_per_degree=10;

		// motion control
		boolean mouseIn=false;
		int buttonPressed=MouseEvent.NOBUTTON;
		int oldx, oldy;

		// scale + position
		int cx,cy;
		double cameraOffsetX=0,cameraOffsetY=0;
		double cameraZoom=20;
		float drawScale=0.1f;

		// driving controls
		public boolean driveOn=false;
		public double drivex=0, drivey=0;
		public double drivexview=0, driveyview=0;
		
		
		
		public DrawPanel() {
			super();
	        addMouseMotionListener(this);
	        addMouseListener(this);
		}

		public void StopDriving() {
	    	drivex=0;
	    	drivey=0;
	    	drivexview=0;    
	    	driveyview=0;
    		driveOn=false;
    		if(driving) SendLineToRobot("J02 X0 Y0");
		}

		public void MoveCamera(int x,int y) {
    		if(driving) {
    			// drive the plotter around
    			drivexview=(x-oldx);
	    		driveyview=(y-oldy);
	    		double len=Math.sqrt(drivexview*drivexview+driveyview*driveyview);
	    		// cap the max velocity
    			if(len>maxZone) {
    				drivexview*=maxZone/len;
    				driveyview*=maxZone/len;
    				len=maxZone;
    			}
    			// add a deadzone in the center
    			double f=len-deadZone;
	    		if(f>0) {
	    			// this scales f to [0....1] so length of (drivex,drivey) will <= 1
	    			f=f/(maxZone-deadZone);
	    			drivex=drivexview*f/len;
	    			drivey=-driveyview*f/len;
	    		} else {
	    			drivex=0;
	    			drivey=0;
	    		}
	    		if(readyToReceive) {
	    			SendLineToRobot("J02 X"+drivex+" Y"+drivey);
	    		}
    		} else {
    			// scroll the gcode preview
	    		double dx=(x-oldx)/cameraZoom;
	    		double dy=(y-oldy)/cameraZoom;
		    	cameraOffsetX-=dx;
		    	cameraOffsetY+=dy;
    		}
		}
		public void MoveImage(int x,int y) {
			// scroll the gcode preview
    		double dx=(x-oldx)/cameraZoom;
    		double dy=(y-oldy)/cameraZoom;
	    	imageOffsetX-=dx;
	    	imageOffsetY+=dy;
		}
		public void ScaleImage(int x,int y) {
			double amnt = (double)(y-oldy)*0.01;
			imageScale += amnt;
			if(imageScale<0.01) imageScale=0.01f;
		}
		public void ZoomCamera(int x,int y) {
			double amnt = (double)(y-oldy)*0.01;
			cameraZoom += amnt;
			if(cameraZoom<0.1) cameraZoom=0.1f;
		}
		public void mousePressed(MouseEvent e) {
			buttonPressed=e.getButton();
	    	oldx=e.getX();
	    	oldy=e.getY();
	    	if(driving && e.getButton()==MouseEvent.BUTTON1) {
	    		driveOn=true;
	    	}
		}
	    public void mouseReleased(MouseEvent e) {
	    	buttonPressed=MouseEvent.NOBUTTON;
	    	if(e.getButton()==MouseEvent.BUTTON1) StopDriving();
	    }
	    public void mouseClicked(MouseEvent e) {}
	    public void mouseEntered(MouseEvent e) {}
	    public void mouseExited(MouseEvent e) {
	    	StopDriving();
	    }
	    public void mouseDragged(MouseEvent e) {
	    	int x=e.getX();
	    	int y=e.getY();
	    	if(buttonPressed==MouseEvent.BUTTON1) {
	    		if(movementMode==MODE_MOVE_IMAGE) MoveImage(x,y);
	    		else MoveCamera(x,y);
	    	} else if(buttonPressed==MouseEvent.BUTTON3) {
	    		if(movementMode==MODE_MOVE_IMAGE) ScaleImage(x,y);
	    		else ZoomCamera(x,y);
	    	}
	    	oldx=x;
	    	oldy=y;
	    	repaint();
	    }
	    public void mouseMoved(MouseEvent e) {}

	    public double TX(double a) {
	    	return cx+(int)((a-cameraOffsetX)*cameraZoom);
	    }
	    public double TY(double a) {
	    	return cy-(int)((a-cameraOffsetY)*cameraZoom);
	    }
	    public double ITX(double a) {
	    	return TX(a*imageScale-imageOffsetX);
	    }
	    public double ITY(double a) {
	    	return TY(a*imageScale-imageOffsetY);
	    }
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);    // paint background
			setBackground(Color.WHITE);
			Graphics2D g2d = (Graphics2D)g;
		   
			cx = this.getWidth()/2;
			cy = this.getHeight()/2;

			if(!portConfirmed) {			
				setBackground(Color.WHITE);
			} else {
				setBackground(Color.GRAY);
				g2d.setColor(Color.WHITE);
				g2d.fillRect((int)TX(limit_left),(int)TY(limit_top),
						(int)((limit_right-limit_left)*cameraZoom),
						(int)((limit_top-limit_bottom)*cameraZoom));

			}

			// draw calibration point
			g2d.setColor(Color.RED);
			g2d.drawLine((int)TX(-0.25),(int)TY( 0.00), (int)TX(0.25),(int)TY(0.00));
			g2d.drawLine((int)TX(0),    (int)TY(-0.25), (int)TX(0.00),(int)TY(0.25));

			// draw image
			g2d.setColor(Color.BLACK);
			
			String[] instructions = ngcfile.getText().split("\\r?\\n");
			double px=TX(0),py=TY(0),pz=90;
			int i,j;

			for(i=0;i<instructions.length;++i) {
				if(instructions[i].contains("G20")) {
					drawScale=0.393700787f;
				} else if(instructions[i].contains("G21")) {
					drawScale=0.1f;
				} else if(instructions[i].startsWith("G00 ") || instructions[i].startsWith("G0 ") || 
					instructions[i].startsWith("G01 ") || instructions[i].startsWith("G1 ")) {
					// draw a line
					double x=px;
					double y=py;
					double z=pz;
					String[] tokens = instructions[i].split("\\s");
					for(j=0;j<tokens.length;++j) {
						if(tokens[j].startsWith("X")) x = Float.valueOf(tokens[j].substring(1)) * drawScale;
						if(tokens[j].startsWith("Y")) y = Float.valueOf(tokens[j].substring(1)) * drawScale;
						if(tokens[j].startsWith("Z")) z = Float.valueOf(tokens[j].substring(1)) * drawScale;
					}

					if(z<5) g2d.drawLine((int)ITX(px),(int)ITY(py),(int)ITX(x),(int)ITY(y));
					px=x;
					py=y;
					pz=z;
				} else if(instructions[i].startsWith("G02 ") || instructions[i].startsWith("G2 ") ||
					instructions[i].startsWith("G03 ") || instructions[i].startsWith("G3 ")) {
					// draw an arc
					int dir = (instructions[i].startsWith("G02") || instructions[i].startsWith("G2")) ? -1 : 1;
					double x=px;
					double y=py;
					double z=pz;
					double ai=px;
					double aj=py;
					String[] tokens = instructions[i].split("\\s");
					for(j=0;j<tokens.length;++j) {
						if(tokens[j].startsWith("X")) x = Float.valueOf(tokens[j].substring(1)) * drawScale;
						if(tokens[j].startsWith("Y")) y = Float.valueOf(tokens[j].substring(1)) * drawScale;
						if(tokens[j].startsWith("Z")) z = Float.valueOf(tokens[j].substring(1)) * drawScale;
						if(tokens[j].startsWith("I")) ai = px + Float.valueOf(tokens[j].substring(1)) * drawScale;
						if(tokens[j].startsWith("J")) aj = py + Float.valueOf(tokens[j].substring(1)) * drawScale;
					}

					if(z<5) {
						double dx=px - ai;
						double dy=py - aj;
						double radius=Math.sqrt(dx*dx+dy*dy);
	
						// find angle of arc (sweep)
						double angle1=atan3(dy,dx);
						double angle2=atan3(y-aj,x-ai);
						double theta=angle2-angle1;
	
						if(dir>0 && theta<0) angle2+=2.0*Math.PI;
						else if(dir<0 && theta>0) angle1+=2.0*Math.PI;
	
						theta=Math.abs(angle2-angle1);
	
						// Draw the arc from a lot of little line segments.
						for(int k=0;k<=theta*steps_per_degree;++k) {
							double angle3 = (angle2-angle1) * ((double)k/(theta*steps_per_degree)) + angle1;
							float nx = (float)(ai + Math.cos(angle3) * radius);
						    float ny = (float)(aj + Math.sin(angle3) * radius);
	
						    g2d.drawLine((int)ITX(px),(int)ITY(py),(int)ITX(nx),(int)ITY(ny));
							px=nx;
							py=ny;
						}
					    g2d.drawLine((int)ITX(px),(int)ITY(py),(int)ITX(x),(int)ITY(y));
					}
					px=x;
					py=y;
					pz=z;
				}
			}  // for ( each instruction )
			
			if(driveOn) {
				double x=oldx;
				double y=oldy;
				g2d.setColor(Color.BLACK);
				// action line
				g2d.drawLine( (int)x, (int)y, (int)(x+drivexview), (int)(y+driveyview) );
				// action limits
				g2d.setColor(Color.RED);
				g2d.drawArc((int)(x-maxZone), 
						(int)(y-maxZone),
						(int)(maxZone*2),
						(int)(maxZone*2),
						0,
						360);
				g2d.setColor(Color.GREEN);
				g2d.drawArc((int)(x-deadZone), 
							(int)(y-deadZone),
							(int)(deadZone*2),
							(int)(deadZone*2),
							0,
							360);
			}
		}
	}
	
	
	
	// returns angle of dy/dx as a value from 0...2PI
	public double atan3(double dy,double dx) {
	  double a=Math.atan2(dy,dx);
	  if(a<0) a=(Math.PI*2.0)+a;
	  return a;
	}
	
	
	
	// spits a message out to the log tab 
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
		    
			portOpened=false;
			portConfirmed=false;
			log.setText("");
			
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

		SetRecentPort(portName);
		portOpened=true;
		UpdateMenuBar();
		
		return 0;
	}


	// complete the handshake, update the menu, repaint the preview with the limits.
	public boolean ConfirmPort() {
		if(portConfirmed==true) return true;
		if(line3.startsWith("== HELLO WORLD ==")==true) {
			String[] lines = line3.split("\\r?\\n");

			limit_top = Float.parseFloat(lines[1].substring(1));
			limit_bottom = Float.parseFloat(lines[2].substring(1));
			limit_left = Float.parseFloat(lines[3].substring(1));
			limit_right = Float.parseFloat(lines[4].substring(1));
			portConfirmed=true;
			UpdateMenuBar();
			previewPane.repaint();
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
	

	
	// close the file, clear the preview tab
	public void CloseFile() {
		ngcfile.setText("");
		if(fileOpened==true && scanner != null) scanner.close();
		linesProcessed=0;
	   	fileOpened=false;
	}
	
	
	
	// Opens the file.  If the file can be opened, repaint the preview tab.
	// @TODO: check this file is gcode?
	public void OpenFile(String filename) {
		CloseFile();

	   	// load contents into file pane
	   	StringBuilder text = new StringBuilder();
	    
	    try {
	    	scanner = new Scanner(new FileInputStream(filename));
	    	linesTotal=0;
		    try {
		      while (scanner.hasNextLine()){
		        text.append(scanner.nextLine() + NL);
		        ++linesTotal;
		      }
		    }
		    finally{
		      scanner.close();
		    }

		    ngcfile.setText(text.toString());

    		scanner = new Scanner(new FileInputStream(filename));
	    }
	    catch(IOException e) {
	    	Log("File could not be opened."+NL);
	    	RemoveRecentFile(filename);
	    	return;
	    }
		
	   	UpdateRecentFiles(filename);

	    fileOpened=true;
	    paused=true;
	    statusBar.SetProgress(linesProcessed,linesTotal,"");

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
		imageScale=1;
		imageOffsetX=0;
		imageOffsetY=0;
		OpenFile(filename);
	}

	
	
	// creates a file open dialog. If you don't cancel it opens that file.
	public void OpenFileDialog() {
	    // Note: source for ExampleFileFilter can be found in FileChooserDemo,
	    // under the demo/jfc directory in the Java 2 SDK, Standard Edition.
		String filename = (recentFiles[0].length()>0) ? filename=recentFiles[0] : "";

		JFileChooser fc = new JFileChooser(new File(filename));
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

		readyToReceive=false;
	}
	
	
	
	// Open the config dialog, send the config update to the robot, refresh the preview tab.
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
		JOptionPane.showMessageDialog(null, inputs, "Config", JOptionPane.PLAIN_MESSAGE);

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
			previewPane.repaint();
		}
	}
	
	
	
	// Take the next line from the file and send it to the robot, if permitted. 
	public void SendFileCommand() {
		if(paused==true) return;
		if(fileOpened==false) return;
		if(portConfirmed==false) return;
		if(linesProcessed>=linesTotal) return;
		
		String line;
		do {			
			// are there any more commands?
			line=scanner.nextLine().trim();
			++linesProcessed;
			statusBar.SetProgress(linesProcessed, linesTotal, line+NL);
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
			readyToReceive=false;
			out.write(line.getBytes());
		}
		catch(IOException e) {}
		
		return true;
	}
	
	
	
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
				driving=false;
				UpdateMenuBar();
				SendFileCommand();
			}
			return;
		}
		if( subject == buttonPause ) {
			if(running) {
				if(paused==true) {
					paused=false;
					// @TODO: if the robot is not ready to unpause, this might fail and the program would appear to hang.
					SendFileCommand();
				} else {
					paused=true;
				}
			}
			return;
		}
		if( subject == buttonDrive ) {
			if(driving==true) {
				driving=false;
				SendLineToRobot("J00");
			} else {
				OpenPort(recentPort);
				running=false;
				paused=true;
				driving=true;
				SendLineToRobot("J01");
			}
			UpdateMenuBar();
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
		if( subject == buttonHome ) {
			GoHome();
			return;
		}		
		if(subject == buttonAbout ) {
			JOptionPane.showMessageDialog(null,"Created by Dan Royer (dan@marginallyclever.com)."+NL+NL
					+"Find out more at http://www.marginallyclever.com/"+NL
					+"Join the project http://github.com/i-make-robots/DrawBot/");
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
		
		if(subject == buttonMoveImage) {
			movementMode  = (movementMode != MODE_MOVE_IMAGE) ? MODE_MOVE_IMAGE : MODE_MOVE_CAMERA;
			
			buttonMoveImage.setState(movementMode==MODE_MOVE_IMAGE);
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
								readyToReceive=true;
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
	


	// Rebuild the contents of the menu based on current program state
	public void UpdateMenuBar() {
		JMenu menu;
        int i;
        
        menuBar.removeAll();
        
        //Build the first menu.
        menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menu.getAccessibleContext().setAccessibleDescription("What g-code to run?");
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

        menu = new JMenu("Settings");
        menu.setMnemonic(KeyEvent.VK_T);
        menu.getAccessibleContext().setAccessibleDescription("Adjust the robot settings.");
        
        JMenu subMenu = new JMenu("Port");
        subMenu.setMnemonic(KeyEvent.VK_P);
        subMenu.getAccessibleContext().setAccessibleDescription("What port to connect to?");
        subMenu.setEnabled(!running && !driving);
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

        buttonConfig = new JMenuItem("Config",KeyEvent.VK_C);
        buttonConfig.getAccessibleContext().setAccessibleDescription("Adjust the robot configuration.");
        buttonConfig.addActionListener(this);
        buttonConfig.setEnabled(portConfirmed && !running && !driving);
        menu.add(buttonConfig);

        buttonHome = new JMenuItem("Home",KeyEvent.VK_O);
        buttonHome.getAccessibleContext().setAccessibleDescription("Recenter the plotter");
        buttonHome.addActionListener(this);
        buttonHome.setEnabled(portConfirmed && !running && !driving);
        menu.add(buttonHome);

        menuBar.add(menu);
/*
        // Image menu
        menu = new JMenu("Image");
        menu.getAccessibleContext().setAccessibleDescription("Change the image");

        buttonMoveImage = new JCheckBoxMenuItem("Move & Scale");
        buttonMoveImage.addActionListener(this);
        buttonMoveImage.setState(movementMode==MODE_MOVE_IMAGE);
        menu.add(buttonMoveImage);
*/
        menuBar.add(menu);

        // Draw menu
        menu = new JMenu("Draw");
        menu.setMnemonic(KeyEvent.VK_D);
        menu.getAccessibleContext().setAccessibleDescription("Start & Stop progress");
        menu.setEnabled(portConfirmed);

        buttonStart = new JMenuItem("Start",KeyEvent.VK_S);
        buttonStart.getAccessibleContext().setAccessibleDescription("Start sending g-code");
        buttonStart.addActionListener(this);
    	buttonStart.setEnabled(portConfirmed && !running && !driving);
        menu.add(buttonStart);

        buttonPause = new JMenuItem("Pause",KeyEvent.VK_P);
        buttonPause.getAccessibleContext().setAccessibleDescription("Pause sending g-code");
        buttonPause.addActionListener(this);
        buttonPause.setEnabled(portConfirmed && running && !driving);
        menu.add(buttonPause);

        buttonHalt = new JMenuItem("Halt",KeyEvent.VK_H);
        buttonHalt.getAccessibleContext().setAccessibleDescription("Halt sending g-code");
        buttonHalt.addActionListener(this);
        buttonHalt.setEnabled(portConfirmed && running && !driving);
        menu.add(buttonHalt);

        menu.addSeparator();

        buttonDrive = new JMenuItem((driving?"Stop":"Start") + " Driving",KeyEvent.VK_R);
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
        // the file panel
        ngcfile = new JTextArea();
        ngcfile.setEditable(false);
        //filePane = new JScrollPane(ngcfile);
        // the preview panel
        previewPane = new DrawPanel();
        
        // the tabs
        //JTabbedPane tabs = new JTabbedPane();
        //tabs.add("Preview",previewPane);
        //tabs.add("File",filePane);
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
		
        return contentPane;
    }
    
    
    
    // Create the GUI and show it.  For thread safety, this method should be invoked from the event-dispatching thread.
    private static void CreateAndShowGUI() {
        //Create and set up the window.
    	JFrame frame = new JFrame("Drawbot GUI v2012-03-26");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        //Create and set up the content pane.
        DrawbotGUI demo = new DrawbotGUI();
        frame.setJMenuBar(demo.CreateMenuBar());
        frame.setContentPane(demo.CreateContentPane());
 
        //Display the window.
        frame.setSize(500,700);
        frame.setVisible(true);
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
