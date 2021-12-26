package com.marginallyclever.communications.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.marginallyclever.communications.NetworkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Encapsulate all TCP/IP receive/transmit implementation
 *
 * @author Peter Colapietro
 * @since v7 (4/12/15)
 */
public final class TCPConnection extends NetworkSession implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(TCPConnection.class);

	public static final int DEFAULT_TCP_PORT = 9999;
	
	private SocketChannel socket;
	private boolean portOpened = false;
	private Thread thread;
	private boolean keepPolling;

	// parsing input from Makelangelo
	private String inputBuffer = "";


	public TCPConnection() {}

	@Override
	public void closeConnection() {
		if (!portOpened) return;
		if (socket != null) {
			keepPolling=false;
			
			try {
				socket.close();
			} catch (IOException e) {
				// Nothing to do
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
					logger.error("Failed to read TCP Connection", e);
					closeConnection();
				}
			}
		}
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
			notifyDataReceived(oneLine);
		}
	}

	@Override
	public void sendMessage(String msg) throws Exception {
		if(!portOpened) return;

		byte[] lineBytes = msg.getBytes();
		ByteBuffer buf = ByteBuffer.allocate(lineBytes.length);
		buf.clear();
		buf.put(lineBytes);
		buf.rewind();
		socket.write(buf);
		
		notifyDataSent(msg);
	}

	/**
	 * @return the port open for this serial connection.
	 */
	@Override
	public boolean isOpen() {
		return portOpened;
	}
}
