package com.marginallyclever.communications.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

import com.marginallyclever.communications.NetworkSession;
import com.marginallyclever.convenience.log.Log;


/**
 * Encapsulate all TCP/IP receive/transmit implementation
 *
 * @author Peter Colapietro
 * @since v7 (4/12/15)
 */
public final class TCPConnection extends NetworkSession implements Runnable {
	public static final String CUE = "> ";
	public static final String NOCHECKSUM = "NOCHECKSUM ";
	public static final String BADCHECKSUM = "BADCHECKSUM ";
	public static final String BADLINENUM = "BADLINENUM ";
	public static final String NEWLINE = "\n";
	public static final String COMMENT_START = ";";
	public static final int DEFAULT_TCP_PORT = 9999;
	
	private SocketChannel socket;
	private boolean portOpened = false;
	private boolean waitingForCue = false;
	private Thread thread;
	private boolean keepPolling;

	// parsing input from Makelangelo
	private String inputBuffer = "";
	private ArrayList<String> commandQueue = new ArrayList<String>();


	public TCPConnection() {}

	@Override
	public void sendMessage(String msg) throws Exception {
		commandQueue.add(msg);
		sendQueuedCommand();
	}

	@Override
	public void closeConnection() {
		if (!portOpened) return;
		if (socket != null) {
			keepPolling=false;
			
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		portOpened = false;
	}

	/** 
	 * Open a connection to a device on the net.
	 * @param ipAddress the network address of the device
	 */
	@Override
	public void openConnection(String ipAddress) throws Exception {
		if (portOpened) return;

		closeConnection();
		
		if(ipAddress.startsWith("http://")) {
			ipAddress = ipAddress.substring(7);
		}

		URL a = new URL("http://"+ipAddress);
		String host = a.getHost();
		int port = a.getPort();
		if(port==-1) port = DEFAULT_TCP_PORT;
		socket = SocketChannel.open();
		socket.connect(new InetSocketAddress(host,port));
		thread = new Thread(this);
		
		setName(ipAddress);
		portOpened = true;
		waitingForCue = true;
		keepPolling=true;
		thread.start();
	}
	
	public void run() {
		ByteBuffer buf = ByteBuffer.allocate(256);
		while(keepPolling) {
			try {
				int bytesRead = socket.read(buf);
				if(bytesRead>0) {
					String line = new String(buf.array());
					dataAvailable(bytesRead,line);
					buf.rewind();
				}
			}
			catch (IOException e) {
				if(keepPolling) {
					e.printStackTrace();
					closeConnection();
				}
			}
		}
	}
		
	/**
	 * Check if the robot reports an error and if so what line number.
	 *
	 * @return -1 if there was no error, otherwise the line number containing the error.
	 */
	protected int errorReported(String line) {
		if (line.lastIndexOf(NOCHECKSUM) != -1) {
			String after_error = line.substring(line.lastIndexOf(NOCHECKSUM) + NOCHECKSUM.length());
			String x = getNumberPortion(after_error);
			int err = 0;
			try {
				err = Integer.decode(x);
				Log.error("NOCHECKSUM "+err);
			} catch (Exception e) {}

			return err;
		}
		if (line.lastIndexOf(BADCHECKSUM) != -1) {
			String after_error = line.substring(line.lastIndexOf(BADCHECKSUM) + BADCHECKSUM.length());
			String x = getNumberPortion(after_error);
			int err = 0;
			try {
				err = Integer.decode(x);
				Log.error("BADCHECKSUM "+err);
			} catch (Exception e) {}

			return err;
		}
		if (line.lastIndexOf(BADLINENUM) != -1) {
			String after_error = line.substring(line.lastIndexOf(BADLINENUM) + BADLINENUM.length());
			String x = getNumberPortion(after_error);
			int err = 0;
			try {
				err = Integer.decode(x);
				Log.error("BADLINENUM "+err);
			} catch (Exception e) {}

			return err;
		}

		return -1;
	}

	public void dataAvailable(int len,String message) {
		if(!portOpened) return;
		String rawInput = message.substring(0,len);
		if( len==0 ) return;
		
		inputBuffer+=rawInput;
		// each line ends with a \n.
		int x;

		for( x=inputBuffer.indexOf("\n"); x!=-1; x=inputBuffer.indexOf("\n") ) {
			x=x+1;
			String oneLine = inputBuffer.substring(0,x);
			inputBuffer = inputBuffer.substring(x);

			// check for error
			int error_line = errorReported(oneLine);
			if(error_line != -1) {
				notifyLineError(error_line);
			} else {
				// no error
				if(!oneLine.trim().equals(CUE.trim())) 
				{
					notifyDataReceived(oneLine);
				}
			}

			// wait for the cue to send another command
			if(oneLine.indexOf(CUE)==0) {
				waitingForCue=false;
			}
		}
		if(waitingForCue==false) {
			sendQueuedCommand();
		}
	}

	protected void sendQueuedCommand() {
		if(!portOpened || waitingForCue) return;

		if(commandQueue.isEmpty()==true) {
			notifySendBufferEmpty();
			return;
		}

		try {
			String command=commandQueue.remove(0);
			String line = command;
			if(line.contains(COMMENT_START)) {
				String [] lines = line.split(COMMENT_START);
				command = lines[0];
			}
			if(!command.endsWith("\n")) command+=NEWLINE;

			byte[] lineBytes = line.getBytes();
			ByteBuffer buf = ByteBuffer.allocate(lineBytes.length);
			buf.clear();
			buf.put(lineBytes);
			buf.rewind();
			socket.write(buf);
			
			waitingForCue=true;

			notifyDataSent(command);
		}
		catch(Exception e1) {
			Log.error(e1.getLocalizedMessage());
		}
	}

	public void deleteAllQueuedCommands() {
		commandQueue.clear();
	}

	/**
	 * Java string to int is very picky.  this method is slightly less picky.  Only works with positive whole numbers.
	 *
	 * @param src
	 * @return the portion of the string that is actually a number
	 */
	private String getNumberPortion(String src) {
		src = src.trim();
		int length = src.length();
		String result = "";
		for (int i = 0; i < length; i++) {
			Character character = src.charAt(i);
			if (Character.isDigit(character)) {
				result += character;
			}
		}
		return result;
	}

	/**
	 * @return the port open for this serial connection.
	 */
	@Override
	public boolean isOpen() {
		return portOpened;
	}
}
