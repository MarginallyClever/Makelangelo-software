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


public class drawbot {
	static BufferedReader inFile;
	static CommPortIdentifier portIdentifier;
	static CommPort commPort;
	static SerialPort serialPort;
	static InputStream in;
	static OutputStream out;
	static String portName="COM9";
	static String cue=new String("> ");
	static String eol=new String(";");
	static final long serialVersionUID=1;

	
	public static void OpenPort() {
		// find the port
		try {
			portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
		}
		catch(Exception e) {
			System.err.println("Ports could not be identified:"+e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}

		if ( portIdentifier.isCurrentlyOwned() ) {
    	    System.out.println("Error: Port is currently in use");
			System.exit(1);
		}

		// open the port
		try {
		    commPort = portIdentifier.open("drawbot",2000);
		}
		catch(Exception e) {
			System.err.println("Port could not be opened:"+e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}

	    if ( ( commPort instanceof SerialPort ) == false ) {
			System.out.println("Error: Only serial ports are handled by this example.");
			System.exit(1);
		}

		// set the port parameters (like baud rate)
		serialPort = (SerialPort) commPort;
		try {
			serialPort.setSerialPortParams(57600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
		}
		catch(Exception e) {
			System.err.println("Port could not be configured:"+e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}

		try {
			in = serialPort.getInputStream();
			out = serialPort.getOutputStream();
		}
		catch(Exception e) {
			System.err.println("Streams could not be opened:"+e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}


	public static void main(String[] args) {
	    // start listening to the command line
		Console cons = System.console();
		if(cons==null) {
			System.err.println("Console could not be created?");
			System.exit(1);
		}
		
	    // read filename
		String fileName = cons.readLine("Enter the filename: ");

		try {
			inFile =  new BufferedReader(new FileReader(fileName));
			System.out.println("File "+fileName+" opened...");
		}
		catch (IOException e){
			System.err.println("File operation failed:"+e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}

	    // Read in the port name
		portName = cons.readLine("Enter the port: ");

	    OpenPort();

		String line,line2,line3="";
		boolean eof_reached=false;
		boolean ready_to_send=false;
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
						ready_to_send=true;
					}
				}
				
				if(ready_to_send==true) {
					// are there any more commands?
					line=inFile.readLine();
					if(line==null) {
						// no more lines to read
						eof_reached=true;
					} else {
						String [] tokens = line.split("\\s");
						if(Arrays.asList(tokens).contains("M06") || 
						   Arrays.asList(tokens).contains("M6")) {
							// tool change
							for(int i=0;i<tokens.length;++i) {
								if(tokens[i].startsWith("T")) {
									cons.readLine("Please change to tool #"+tokens[i].substring(1)+" and press enter. ");
								}
							}
							// ready_to_send will still be true here.  We didn't talk to the arduino.
						} else if(tokens[0]=="M02" || tokens[0]=="M2") {
							// end of program
							break;
						} else {
							// send the command to the robot
							line+=eol;
							System.out.println(line);
							out.write(line.getBytes());
							ready_to_send=false;
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
		System.exit(0);
	}
}
