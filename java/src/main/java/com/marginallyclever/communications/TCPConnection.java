package com.marginallyclever.communications;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;


import com.marginallyclever.communications.MarginallyCleverConnectionReadyListener;
import com.marginallyclever.makelangelo.Log;


/**
 * Created on 4/12/15.  Encapsulate all jssc serial receive/transmit implementation
 *
 * @author Peter Colapietro
 * @since v7
 */
public final class TCPConnection implements NetworkConnection, TCPConnectionEventListener {
	private Socket socket;
	private TransportLayer transportLayer;
	private String connectionName = "";
	private boolean portOpened = false;
	private boolean waitingForCue = false;


	static final String CUE = "> ";
	static final String NOCHECKSUM = "NOCHECKSUM ";
	static final String BADCHECKSUM = "BADCHECKSUM ";
	static final String BADLINENUM = "BADLINENUM ";
	static final String NEWLINE = "\n";
	static final String COMMENT_START = ";";

	protected DataOutputStream outToServer;
	protected TCPConnectionReader inFromServer;
	
	// parsing input from Makelangelo
	private String inputBuffer = "";
	ArrayList<String> commandQueue = new ArrayList<String>();

	// Listeners which should be notified of a change to the percentage.
	private ArrayList<MarginallyCleverConnectionReadyListener> listeners = new ArrayList<MarginallyCleverConnectionReadyListener>();


	public TCPConnection(TransportLayer layer) {
		transportLayer = layer;
	}

	@Override
	public void sendMessage(String msg) throws Exception {
		commandQueue.add(msg);
		sendQueuedCommand();
	}


	@Override
	public void closeConnection() {
		if (portOpened) {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				try {
					outToServer.flush();
					outToServer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				try {
					inFromServer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			portOpened = false;
		}
	}

	// open a serial connection to a device.  We won't know it's the robot until
	@Override
	public void openConnection(String URL) throws Exception {
		if (portOpened) return;

		closeConnection();

		URL a = new URL(URL);
		String host = a.getHost();
		int port = a.getPort();
		socket = new Socket(host,port);
		socket.setKeepAlive(true);
		socket.setTcpNoDelay(true);
		socket.setSoTimeout(200);  // maybe only use this on closing connection.

		outToServer = new DataOutputStream(socket.getOutputStream());
		inFromServer = new TCPConnectionReader(socket.getInputStream());
		inFromServer.addListener(this);
		Thread t = new Thread(inFromServer);
		
		connectionName = URL;
		portOpened = true;
		waitingForCue = true;

		t.run();
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


	// Deal with something robot has sent.
	@Override
	public void dataAvailable(int len,byte[]message) {
		if(!portOpened) return;
		String rawInput = new String(message).substring(0,len);
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
					notifyDataAvailable(oneLine);
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

		String command;
		try {
			command=commandQueue.remove(0);
			String line = command;
			if(line.contains(COMMENT_START)) {
				String [] lines = line.split(COMMENT_START);
				command = lines[0];
			}
			if(line.endsWith("\n") == false) {
				line+=NEWLINE;
			}
			outToServer.writeBytes(line);
			waitingForCue=true;
		}
		catch(IndexOutOfBoundsException e1) {}
		catch(IOException e1) {}
	}

	public void deleteAllQueuedCommands() {
		commandQueue.clear();
	}

	// connect to the last port
	@Override
	public void reconnect() throws Exception {
		openConnection(connectionName);
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

	@Override
	public String getRecentConnection() {
		return connectionName;
	}

	@Override
	public void addListener(MarginallyCleverConnectionReadyListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(MarginallyCleverConnectionReadyListener listener) {
		listeners.remove(listener);
	}

	private void notifyLineError(int lineNumber) {
		for (MarginallyCleverConnectionReadyListener listener : listeners) {
			listener.lineError(this,lineNumber);
		}
	}

	private void notifySendBufferEmpty() {
		for (MarginallyCleverConnectionReadyListener listener : listeners) {
			listener.sendBufferEmpty(this);
		}
	}

	// tell all listeners data has arrived
	private void notifyDataAvailable(String line) {
		for (MarginallyCleverConnectionReadyListener listener : listeners) {
			listener.dataAvailable(this,line);
		}
	}

	@Override
	public TransportLayer getTransportLayer() {
		return this.transportLayer;
	}
}
