/**
 * @(#)drawbot.java
 *
 * drawbot application
 *
 * C:\Users\Dan\Documents\designs\drawbot\v2\reddit alien.ngc
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
  static BufferedReader commandLine;
  static BufferedReader inFile;
  static CommPortIdentifier portIdentifier;
  static CommPort commPort;
  static SerialPort serialPort;
  static InputStream in;
  static OutputStream out;
  static String fileName, portName;



  public static void main(String[] args) {
    // start listening to the command line
    commandLine = new BufferedReader(new InputStreamReader(System.in));
    fileName="";
    portName="COM18";

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


		try {
			// read filename
			inFile =  new BufferedReader(new FileReader(fileName));
			System.out.println("File "+fileName+" opened...");

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
			} else {
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
        } else {
          // set the port parameters (like baud rate)
          serialPort = (SerialPort) commPort;
          try {
            serialPort.setSerialPortParams(57600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
          }
          catch(Exception e) {
            System.err.println("Port could not be configured:"+e.getMessage());
            e.printStackTrace();
          }

          in = serialPort.getInputStream();
          out = serialPort.getOutputStream();

          String line,line2,line3="";
          boolean eof_reached=false;
          byte[] buffer = new byte[1024];
          int len = -1;
          String cue=new String("> ");
          String eol=new String(";");

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

          out.close();
          in.close();

        }
			}

			inFile.close();
		}
		catch (IOException e){
			System.err.println("File operation failed:"+e.getMessage());
			e.printStackTrace();
		}

		System.out.println("\n** FINISHED **");
	}
}
