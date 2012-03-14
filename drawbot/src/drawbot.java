/**
 * @(#)drawbot.java
 *
 * drawbot application
 *
 * C:\Users\Dan\Documents\designs\drawbot\v2\vhs_0001.ngc
 *
 * @author
 * @version 1.00 2012/2/28
 */
import java.io.*;
import java.util.*;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.*;
import javax.swing.*;

public class drawbot extends JFrame implements KeyListener, ActionListener {
	static BufferedReader commandLine;
	static BufferedReader inFile;
	static CommPortIdentifier portIdentifier;
	static CommPort commPort;
	static SerialPort serialPort;
	static InputStream in;
	static OutputStream out;
	static String portName="COM9";
	static String cue=new String("> ");
	static String eol=new String(";");

	static int k_up=0,k_down=0,k_left=0,k_right=0, old_state=0;

	JTextArea displayArea;
    JTextField typingArea;
    static final String newline = System.getProperty("line.separator");


	public static void setup() {
		/* Use an appropriate Look and Feel */
        try {
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        }
        catch (UnsupportedLookAndFeelException ex) {   ex.printStackTrace();        }
        catch (IllegalAccessException ex) {            ex.printStackTrace();        }
        catch (InstantiationException ex) {            ex.printStackTrace();        }
        catch (ClassNotFoundException ex) {            ex.printStackTrace();        }
        /* Turn off metal's use of bold fonts */
        UIManager.put("swing.boldMetal", Boolean.FALSE);

        //Schedule a job for event dispatch thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });

        OpenPort();
	}


    // Create the GUI and show it.  For thread safety,
    // this method should be invoked from the
    // event-dispatching thread.
    private static void createAndShowGUI() {
        //Create and set up the window.
        drawbot frame = new drawbot("drawbot");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Set up the content pane.
        frame.addComponentsToPane();
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }


    private void addComponentsToPane() {
        JButton button = new JButton("Clear");
        button.addActionListener(this);

        typingArea = new JTextField(20);
        typingArea.addKeyListener(this);

        //Uncomment this if you wish to turn off focus
        //traversal.  The focus subsystem consumes
        //focus traversal keys, such as Tab and Shift Tab.
        //If you uncomment the following line of code, this
        //disables focus traversal and the Tab events will
        //become available to the key event listener.
        //typingArea.setFocusTraversalKeysEnabled(false);

        displayArea = new JTextArea();
        displayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(displayArea);
        scrollPane.setPreferredSize(new Dimension(375, 125));

        getContentPane().add(typingArea, BorderLayout.PAGE_START);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(button, BorderLayout.PAGE_END);
    }


    public drawbot(String name) {
        super(name);
    }


    // Handle the button click.
    public void actionPerformed(ActionEvent e) {
        //Clear the text components.
        displayArea.setText("");
        typingArea.setText("");
        //Return the focus to the typing area.
        typingArea.requestFocusInWindow();
    }


    // Handle the key typed event from the text field.
    public void keyTyped(KeyEvent e) {
        keyAction(e);
    }


    // Handle the key pressed event from the text field.
    public void keyPressed(KeyEvent e) {
        keyAction(e);
    }


    // Handle the key released event from the text field.
    public void keyReleased(KeyEvent e) {
        keyAction(e);
    }


    private void keyAction(KeyEvent k){
    	// what action occurred?
        int pressed = ( k.getID() == KeyEvent.KEY_PRESSED ) ? 1 : 0;

		switch(k.getKeyCode()) {
		case 37: 	k_left=pressed; 	break; // left
		case 38:	k_up=pressed;		break; // up
		case 39:	k_right=pressed;	break; // right
		case 40:	k_down=pressed;		break; // down
		default: break;  // do nothing
		}

		// has anything changed?
		int state=k_left<<0
				| k_up<<1
				| k_right<<2
				| k_down<<3;
		if(old_state==state) return;
		old_state=state;

		// compose the instruction
		float x=k_right-k_left;
		float y=k_down-k_up;
		String line="J00 X"+x+" Y"+y+";";

		if(line.length()>0) {
			// send the instruction
			try {
				out.write(line.getBytes());
			}
			catch(IOException e) {
				System.err.println("Send failed:"+e.getMessage());
			}
			// echo to the user
	        displayArea.append(line + newline);
	        displayArea.setCaretPosition(displayArea.getDocument().getLength());
		}
    }


	public static void OpenPort() {
		// find the port
		try {
			portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
		}
		catch(Exception e) {
			System.err.println("Ports could not be identified:"+e.getMessage());
			e.printStackTrace();
		}

		if ( portIdentifier.isCurrentlyOwned() ) {
    	    System.out.println("Error: Port is currently in use");
    	    return;
		}

		// open the port
		try {
		    commPort = portIdentifier.open("drawbot",2000);
		}
		catch(Exception e) {
			System.err.println("Port could not be opened:"+e.getMessage());
			e.printStackTrace();
		}

	    if ( ( commPort instanceof SerialPort ) == false ) {
			System.out.println("Error: Only serial ports are handled by this example.");
			return;
		}

		// set the port parameters (like baud rate)
		serialPort = (SerialPort) commPort;
		try {
			serialPort.setSerialPortParams(57600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
		}
		catch(Exception e) {
			System.err.println("Port could not be configured:"+e.getMessage());
			e.printStackTrace();
		}

		try {
			in = serialPort.getInputStream();
			out = serialPort.getOutputStream();
		}
		catch(Exception e) {
			System.err.println("Streams could not be opened:"+e.getMessage());
			e.printStackTrace();
		}
	}


	public static void main(String[] args) {
	  	//setup();

	    // start listening to the command line
	    commandLine = new BufferedReader(new InputStreamReader(System.in));
	    String fileName="";

	    // Read in the file & port names
	    try {
			System.out.println("Enter the filename:");
			fileName = commandLine.readLine();
			System.out.println("Enter the port:");
			portName = commandLine.readLine();
	    }
	    catch (IOException e){
			System.err.println("User input read failed:"+e.getMessage());
			e.printStackTrace();
	    }

		// read filename
		try {
			inFile =  new BufferedReader(new FileReader(fileName));
			System.out.println("File "+fileName+" opened...");
		}
		catch (IOException e){
			System.err.println("File operation failed:"+e.getMessage());
			e.printStackTrace();
		}

		OpenPort();

		String line,line2,line3="";
		boolean eof_reached=false;
		byte[] buffer = new byte[1024];
		int len = -1;

		try {
			// as long as there are lines in the file to read
			do {
				// get any messages from the robot
				len = in.read(buffer);
				if( len>0 ) {
					line2 = new String(buffer,0,len);
					System.out.print(line2);
					line3+=line2;
					// wait for the cue ("> ") to send another command
					if(line3.lastIndexOf(cue)!=-1) {
						line3="";
						// are there any more commands?
						line=inFile.readLine();
						if(line==null) {
							// no more lines to read
							eof_reached=true;
						} else {
							// send the command to the robot
							line+=eol;
							System.out.println(line);
							out.write(line.getBytes());
						}
					}
				}
			} while(eof_reached!=true);
		}
		catch(IOException e) {
			System.err.println("IO error:"+e.getMessage());
			e.printStackTrace();
		}

		System.out.println("\n** FINISHED **");
	}
}
