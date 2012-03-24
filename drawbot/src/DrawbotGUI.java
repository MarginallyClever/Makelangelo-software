/**@(#)drawbotGUI.java
 *
 * drawbot application with GUI
 *
 * @author Dan Royer (dan@marginallyclever.com)
 * @version 1.00 2012/2/28
 */


// io functions
import java.io.*;
// nicely formated float log
import java.text.DecimalFormat;
// ??
import java.util.*;

// service manager

// Serial communications
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

// Swing (GUI) components
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// preferences
import java.util.prefs.Preferences;



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

	
	
	public String[] ListSerialPorts() {
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
	
	
	
	public void GetRecentPort() {
		recentPort = prefs.get("recent-port", portsDetected[0]);
	}

	
	
	public void SetRecentPort(String portName) {
		prefs.put("recent-port", portName);
		recentPort=portName;
		UpdateMenuBar();
	}
	

	
	public void CloseFile() {
		ngcfile.setText("");
		if(fileOpened==true && scanner != null) scanner.close();
		linesProcessed=0;
	   	fileOpened=false;
	}
	
	
	
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
	}
	
	
	
	/* changes the order of the recent files list in the File submenu 
	 * also refreshes the menus.
	 */
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
	
	
	
	public void GetRecentFiles() {
		int i;
		for(i=0;i<recentFiles.length;++i) {
			recentFiles[i] = prefs.get("recent-files-"+i, recentFiles[i]);
		}
	}	
	
	

	/* creates a file open dialog.  
	 * If you didn't cancel it opens that file.
	 */
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
		}
	}
	
	
	
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
			ClosePort();
			OpenPort(recentPort);
			running=false;
			paused=true;
			drawing=true;
			return;
		}
		if( subject == buttonHalt ) {
			CloseFile();
			ClosePort();
			running=false;
			paused=true;
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
		}
		return portConfirmed;
	}
	
	
	
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
        buttonConfig.setEnabled(portConfirmed);
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
    	buttonStart.setEnabled(portConfirmed);
        menu.add(buttonStart);

        buttonPause = new JMenuItem("Pause",KeyEvent.VK_P);
        buttonPause.getAccessibleContext().setAccessibleDescription("Pause sending g-code");
        buttonPause.addActionListener(this);
        buttonPause.setEnabled(portConfirmed);
        menu.add(buttonPause);

        buttonHalt = new JMenuItem("Halt",KeyEvent.VK_H);
        buttonHalt.getAccessibleContext().setAccessibleDescription("Halt sending g-code");
        buttonHalt.addActionListener(this);
        buttonHalt.setEnabled(portConfirmed);
        menu.add(buttonHalt);

        menu.addSeparator();

        buttonDrive = new JMenuItem("Drive",KeyEvent.VK_R);
        buttonDrive.getAccessibleContext().setAccessibleDescription("Etch-a-sketch style driving");
        buttonDrive.addActionListener(this);
        buttonDrive.setEnabled(portConfirmed);
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
    	JFrame frame = new JFrame("Drawbot GUI v2012-03-20");
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
