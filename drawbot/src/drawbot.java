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
import java.text.DecimalFormat;
import java.util.*;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;


public class Drawbot {
	static BufferedReader inFile;
	static CommPortIdentifier portIdentifier;
	static CommPort commPort;
	static SerialPort serialPort;
	static InputStream in;
	static OutputStream out;
	static String cue=new String("> ");
	static String eol=new String(";");
	static final long serialVersionUID=1;
	static String portName="COM9";

	
	public void OpenPort() {
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

	
	public int countLines(String filename) throws IOException {
	    InputStream is = new BufferedInputStream(new FileInputStream(filename));
	    try {
	        byte[] c = new byte[1024];
	        int count = 0;
	        int readChars = 0;
	        while ((readChars = is.read(c)) != -1) {
	            for (int i = 0; i < readChars; ++i) {
	                if (c[i] == '\n')
	                    ++count;
	            }
	        }
	        return count;
	    } finally {
	        is.close();
	    }
	}
	
	
	public Drawbot() {
	    // start listening to the command line
		Console cons = System.console();
		if(cons==null) {
			System.err.println("Console could not be created?");
			System.exit(1);
		}
		
	    // read filename
		String fileName = cons.readLine("Enter the filename: ");
		float numLines=1;

		try {
			inFile =  new BufferedReader(new FileReader(fileName));
			System.out.println("File "+fileName+" opened...");
			numLines=countLines(fileName);
		}
		catch (IOException e){
			System.err.println("File operation failed:"+e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}

	    // Read in the port name
		portName = cons.readLine("Enter the port: ");

	    OpenPort();

	    long start = System.currentTimeMillis();
	    
		String line,line2,line3="";
		boolean eof_reached=false;
		boolean ready_to_send=false;
		byte[] buffer = new byte[1024];
		int len = -1;
		int numErrors=0;
		float linesRead=0;
		DecimalFormat fmt = new DecimalFormat("#.##");

		try {
			// as long as there are lines in the file to read
			do {
				// get any messages from the robot
				len = in.read(buffer);
				if( len>0 ) {
					line2 = new String(buffer,0,len);
					System.out.print(line2);
					line3+=line2;
					if(line3.contains("error")) {
						++numErrors;
					}
					// wait for the cue ("> ") to send another command
					if(line3.lastIndexOf(cue)!=-1) {
						ready_to_send=true;
						line3="";
					}
				}
				
				if(ready_to_send==true) {
					// are there any more commands?
					line=inFile.readLine();
					++linesRead;
					float percentage = linesRead*100.0f/numLines;
					if(line==null) {
						// no more lines to read
						eof_reached=true;
					} else {
						line.trim();
						String [] tokens = line.split("\\s");
						if(Arrays.asList(tokens).contains("M06") || 
						   Arrays.asList(tokens).contains("M6")) {
							// tool change
							for(int i=0;i<tokens.length;++i) {
								if(tokens[i].startsWith("T")) {
									cons.readLine("Please change to tool #"+tokens[i].substring(1)+" and press enter. ");
								}
							}
							// still ready_to_send
						} else if(tokens[0]=="M02" || tokens[0]=="M2") {
							// end of program
							System.out.println("("+fmt.format(percentage)+"%) "+line);
							break;
						} else if(tokens[0].startsWith("M")) {
							// ignore
							System.out.println("("+fmt.format(percentage)+"%) "+line+" ignored.");
						} else {
 							int index=line.indexOf('(');
							if(index!=-1) {
								String comment=line.substring(index+1,line.lastIndexOf(')'));
								line=line.substring(0,index).trim();
								System.out.println("\n* "+comment);
								if(line.length()==0) continue;  // still ready_to_send
							}
							// send the command to the robot
							ready_to_send=false;
							line+=eol;
							System.out.println("("+fmt.format(percentage)+"%) "+line);
							out.write(line.getBytes());
							//try {
							//	Thread.sleep(50);
							//}
							//catch(InterruptedException e) {}
						}
					}
				}
			} while(eof_reached!=true);
		}
		catch(IOException e) {
			System.err.println("IO error:"+e.getMessage());
			e.printStackTrace();
		}

		long end = System.currentTimeMillis();
		
		System.out.println("\n** FINISHED **");
		System.out.println(((end-start)*0.001)+"s elapsed.");
		if(numErrors>0) System.out.println(numErrors+" errors.");
		System.exit(0);
	}

	
	public static void main(String[] args) {
		new Drawbot();
	}
}
