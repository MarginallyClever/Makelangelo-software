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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import javax.swing.event.MouseInputListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.TooManyListenersException;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;



public class DrawbotGUI
		extends JPanel
		implements ActionListener, SerialPortEventListener
{
	static private final String cue = "> ";
	static private final String eol = ";";
	static private final String NL = System.getProperty("line.separator");;
	static final long serialVersionUID=1;

	Preferences prefs = Preferences.userRoot().node("DrawBot");
	
	// Serial connection stuff
	CommPortIdentifier portIdentifier;
	CommPort commPort;
	SerialPort serialPort;
	InputStream in;
	OutputStream out;
	String[] portsDetected;
	boolean portOpened=false;
	boolean portConfirmed=false;
	
	// stored in preferences
	String[] recentFiles = {"","","","","","","","","",""};
	String recentPort;
	
	// GUI elements
    JMenuBar menuBar;
    JMenuItem buttonOpenFile, buttonExit, buttonStart, buttonPause, buttonHalt, buttonDrive, buttonAbout, buttonConfig, buttonRescan;
    JMenuItem [] buttonRecent = new JMenuItem[10];
    JMenuItem [] buttonPorts;
    
    
	JTextArea log,ngcfile;
	JScrollPane logPane,filePane;

	// status bar junk
	StatusBar statusBar;
	DecimalFormat fmt = new DecimalFormat("#.##");
	
	// progress & run control
	boolean paused=true;
	boolean running=false;
	boolean drawing=false;

	// config
	double limit_top, limit_bottom, limit_left, limit_right;
	
	// reading file
    Scanner scanner;
	long linesTotal=0;
	long linesProcessed=0;
	boolean fileOpened=false;

	// parsing input from drawbot
	String line3="";

	// preview pane
	DrawPanel previewPane;
	
	
	
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
	  
	

	// Custom drawing panel written as an inner class to access the instance variables.
	public class DrawPanel extends JPanel implements MouseListener, MouseInputListener  {
		static final long serialVersionUID=2;
		float steps_per_degree=10;
		boolean mouseIn=false;
		int buttonPressed=MouseEvent.NOBUTTON;
		int oldx, oldy;
		int offsetx=0,offsety=0;
		float previewScale=20;

		
		public DrawPanel() {
			super();
	        addMouseMotionListener(this);
	        addMouseListener(this);
		}
		
		
		public void mousePressed(MouseEvent e) {
			buttonPressed=e.getButton();
	    	oldx=e.getX();
	    	oldy=e.getY();
		}
	    public void mouseReleased(MouseEvent e) {
	    	buttonPressed=MouseEvent.NOBUTTON;
	    }
	    public void mouseClicked(MouseEvent e) {}
	    public void mouseEntered(MouseEvent e) {}
	    public void mouseExited(MouseEvent e) {}
	    public void mouseDragged(MouseEvent e) {
	    	int x=e.getX();
	    	int y=e.getY();
	    	if(buttonPressed==MouseEvent.BUTTON1) {
		    	offsetx+=x-oldx;
		    	offsety+=y-oldy;
	    	} else if(buttonPressed==MouseEvent.BUTTON3) {
	    		float amnt = (y-oldy)*0.1f;
	    		previewScale += amnt;
	    		if(previewScale<0.1) previewScale=0.1f;
	    	}
	    	oldx=x;
	    	oldy=y;
	    	repaint();
	    }
	    public void mouseMoved(MouseEvent e) {}
	    
		
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);    // paint background
			setBackground(Color.WHITE);
			Graphics2D g2d = (Graphics2D)g;
		   
			int w = this.getWidth();
			int h = this.getHeight();
			int cx = offsetx + w/2;
			int cy = offsety + h/2;

			g2d.setColor(Color.BLACK);
			g2d.drawLine(cx+(int)(limit_left*previewScale),cy-(int)(limit_top*previewScale),cx+(int)(limit_right*previewScale),cy-(int)(limit_top*previewScale));
			g2d.drawLine(cx+(int)(limit_left*previewScale),cy-(int)(limit_bottom*previewScale),cx+(int)(limit_right*previewScale),cy-(int)(limit_bottom*previewScale));

			g2d.drawLine(cx+(int)(limit_left*previewScale),cy-(int)(limit_top*previewScale),cx+(int)(limit_left*previewScale),cy-(int)(limit_bottom*previewScale));
			g2d.drawLine(cx+(int)(limit_right*previewScale),cy-(int)(limit_top*previewScale),cx+(int)(limit_right*previewScale),cy-(int)(limit_bottom*previewScale));
			
			
			String[] instructions = ngcfile.getText().split("\\r?\\n");
			float px=0,py=0,pz=90;
			int i,j;

			for(i=0;i<instructions.length;++i) {
				if(instructions[i].startsWith("G00 ") || instructions[i].startsWith("G0 ") || 
					instructions[i].startsWith("G01 ") || instructions[i].startsWith("G1 ")) {
					// draw a line
					float x=px;
					float y=py;
					float z=pz;
					String[] tokens = instructions[i].split("\\s");
					for(j=0;j<tokens.length;++j) {
						if(tokens[j].startsWith("X")) x = Float.valueOf(tokens[j].substring(1));
						if(tokens[j].startsWith("Y")) y = Float.valueOf(tokens[j].substring(1));
						if(tokens[j].startsWith("Z")) z = Float.valueOf(tokens[j].substring(1));
					}

					g2d.setColor( z<45 ? Color.BLUE : Color.PINK);
					g2d.drawLine(cx+(int)(px*previewScale),cy-(int)(py*previewScale),cx+(int)(x*previewScale),cy-(int)(y*previewScale));
					px=x;
					py=y;
					pz=z;
				} else if(instructions[i].startsWith("G02 ") || instructions[i].startsWith("G2 ") ||
					instructions[i].startsWith("G03 ") || instructions[i].startsWith("G3 ")) {
					// draw an arc
					int dir = (instructions[i].startsWith("G02") || instructions[i].startsWith("G2")) ? -1 : 1;
					float x=px;
					float y=py;
					float z=pz;
					float ai=px;
					float aj=py;
					String[] tokens = instructions[i].split("\\s");
					for(j=0;j<tokens.length;++j) {
						if(tokens[j].startsWith("X")) x = Float.valueOf(tokens[j].substring(1));
						if(tokens[j].startsWith("Y")) y = Float.valueOf(tokens[j].substring(1));
						if(tokens[j].startsWith("Z")) z = Float.valueOf(tokens[j].substring(1));
						if(tokens[j].startsWith("I")) ai = px + Float.valueOf(tokens[j].substring(1));
						if(tokens[j].startsWith("J")) aj = py + Float.valueOf(tokens[j].substring(1));
					}

					g2d.setColor( z<45 ? Color.GREEN : Color.PINK);

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

					for(int k=0;k<=theta*steps_per_degree;++k) {
						double angle3 = (angle2-angle1) * ((double)k/(theta*steps_per_degree)) + angle1;
						float nx = (float)(ai + Math.cos(angle3) * radius);
					    float ny = (float)(aj + Math.sin(angle3) * radius);

					    g2d.drawLine(cx+(int)(px*previewScale),cy-(int)(py*previewScale),cx+(int)(nx*previewScale),cy-(int)(ny*previewScale));
						px=nx;
						py=ny;
					}
				    g2d.drawLine(cx+(int)(px*previewScale),cy-(int)(py*previewScale),cx+(int)(x*previewScale),cy-(int)(y*previewScale));
					px=x;
					py=y;
					pz=z;
				}
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
		portOpened=false;
		portConfirmed=false;
		log.setText("");
		
	    if (serialPort != null) {
	        try {
		        // Close the port.
		        serialPort.removeEventListener();
		        serialPort.close();
	            // Close the I/O streams.
	            out.close();
	            in.close();
	        } catch (IOException e) {
	            // Don't care
	        }
	    }
	    
		UpdateMenuBar();
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
			serialPort.setSerialPortParams(57600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
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
		int i;
		for(i=0;i<recentFiles.length-1;++i) {
			if(recentFiles[i]==filename) {
				break;
			}
		}
		
		for(--i;i>=0;--i) {
			recentFiles[i+1]=recentFiles[i];
		}
		recentFiles[0]=filename;

		// update prefs
		for(i=0;i<recentFiles.length;++i) {
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
	
	

	// creates a file open dialog. If you don't cancel it opens that file.
	public void OpenFileDialog() {
	    // Note: source for ExampleFileFilter can be found in FileChooserDemo,
	    // under the demo/jfc directory in the Java 2 SDK, Standard Edition.
		String filename = (recentFiles[0].length()>0) ? filename=recentFiles[0] : "";

		JFileChooser fc = new JFileChooser(new File(filename));
	    if(fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
	    	Log("Opening file "+filename+"..."+NL);
	    	OpenFile(fc.getSelectedFile().getAbsolutePath());
	    }
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
			Log(line);

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
	public void SendCommand() {
		if(paused==true) return;
		if(fileOpened==false) return;
		if(portConfirmed==false) return;
		if(linesProcessed>=linesTotal) return;
		
		do {			
			// are there any more commands?
			String line=scanner.nextLine();
			++linesProcessed;
			if(linesProcessed==linesTotal) {
				paused=true;
				CloseFile();
			}
	
			line.trim();
			statusBar.SetProgress(linesProcessed, linesTotal, line+NL);
			Log(line);
			
			// tool change request?
			String [] tokens = line.split("\\s");
			if(Arrays.asList(tokens).contains("M06") || 
			   Arrays.asList(tokens).contains("M6")) {
				// tool change
				for(int i=0;i<tokens.length;++i) {
					if(tokens[i].startsWith("T")) {
						JOptionPane.showMessageDialog(null,"Please change to tool #"+tokens[i].substring(1)+" and click OK.");
					}
				}
				// still ready to send
				continue;
			} else if(tokens[0]=="M02" || tokens[0]=="M2") {
				// end of program?
				running=false;
				CloseFile();
			} else if(tokens[0].startsWith("M")) {
				// other machine code to ignore?
				continue;
			} else {
				int index=line.indexOf('(');
				if(index!=-1) {
					String comment=line.substring(index+1,line.lastIndexOf(')'));
					line=line.substring(0,index).trim();
					Log("* "+comment);
					if(line.length()==0) continue;  // still ready to send
				}
				// send the command to the robot
				line+=eol;
				try {
					out.write(line.getBytes());
				}
				catch(IOException e) {}
				break;
			}
		} while(true);
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
			paused=false;
			running=true;
			drawing=false;
			ClosePort();
			OpenPort(recentPort);
			UpdateMenuBar();
			return;
		}
		if( subject == buttonPause ) {
			if(running) {
				if(paused==true) {
					paused=false;
					SendCommand();
				} else {
					paused=true;
				}
			}
			return;
		}
		if( subject == buttonDrive ) {
			CloseFile();
			OpenPort(recentPort);
			running=false;
			paused=true;
			drawing=true;
			UpdateMenuBar();
			return;
		}
		if( subject == buttonHalt ) {
			CloseFile();
			running=false;
			paused=true;
			UpdateMenuBar();
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
				OpenFile(recentFiles[i]);
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
								SendCommand();
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

        //a group of radio button menu items
        menu = new JMenu("Settings");
        menu.setMnemonic(KeyEvent.VK_T);
        menu.getAccessibleContext().setAccessibleDescription("Adjust the robot settings.");
        
        JMenu subMenu = new JMenu("Port");
        subMenu.setMnemonic(KeyEvent.VK_P);
        subMenu.getAccessibleContext().setAccessibleDescription("What port to connect to?");
        subMenu.setEnabled(!running && !drawing);
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
        buttonConfig.setEnabled(portConfirmed && !running && !drawing);
        menu.add(buttonConfig);

        menuBar.add(menu);

        // run menu
        menu = new JMenu("Draw");
        menu.setMnemonic(KeyEvent.VK_D);
        menu.getAccessibleContext().setAccessibleDescription("Start & Stop progress");
        menu.setEnabled(portConfirmed);

        buttonStart = new JMenuItem("Start",KeyEvent.VK_S);
        buttonStart.getAccessibleContext().setAccessibleDescription("Start sending g-code");
        buttonStart.addActionListener(this);
    	buttonStart.setEnabled(portConfirmed && !running && !drawing);
        menu.add(buttonStart);

        buttonPause = new JMenuItem("Pause",KeyEvent.VK_P);
        buttonPause.getAccessibleContext().setAccessibleDescription("Pause sending g-code");
        buttonPause.addActionListener(this);
        buttonPause.setEnabled(portConfirmed && running && !drawing);
        menu.add(buttonPause);

        buttonHalt = new JMenuItem("Halt",KeyEvent.VK_H);
        buttonHalt.getAccessibleContext().setAccessibleDescription("Halt sending g-code");
        buttonHalt.addActionListener(this);
        buttonHalt.setEnabled(portConfirmed && running && !drawing);
        menu.add(buttonHalt);

        menu.addSeparator();

        buttonDrive = new JMenuItem("Drive",KeyEvent.VK_R);
        buttonDrive.getAccessibleContext().setAccessibleDescription("Etch-a-sketch style driving");
        buttonDrive.addActionListener(this);
        buttonDrive.setEnabled(portConfirmed && !running);
        menu.add(buttonDrive);

        menuBar.add(menu);
        
        //Build second menu in the menu bar.
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
 
        JTabbedPane tabs = new JTabbedPane();

        //Create a scrolled text area.
        log = new JTextArea();
        log.setEditable(false);
        logPane = new JScrollPane(log);
        ngcfile = new JTextArea();
        ngcfile.setEditable(false);
        filePane = new JScrollPane(ngcfile);
        previewPane = new DrawPanel();
        
        tabs.add("Preview",previewPane);
        tabs.add("File",filePane);
        tabs.add("Log",logPane);
        
        contentPane.add(tabs, BorderLayout.CENTER);

        // status bar
        statusBar = new StatusBar();
        contentPane.add(statusBar, BorderLayout.SOUTH);

        // open the file
		GetRecentFiles();
		if(recentFiles[0].length()>0) {
			Log("Opening file "+recentFiles[0]+"..."+NL);
			OpenFile(recentFiles[0]);
		}
		
		// connect to the last port
		GetRecentPort();
		ListSerialPorts();
		if(Arrays.asList(portsDetected).contains(recentPort)) {
			OpenPort(recentPort);
		}
		
        return contentPane;
    }
    
    
    
    // Create the GUI and show it.  For thread safety, this method should be invoked from the event-dispatching thread.
    private static void CreateAndShowGUI() {
        //Create and set up the window.
    	JFrame frame = new JFrame("Drawbot GUI v2012-03-25");
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
