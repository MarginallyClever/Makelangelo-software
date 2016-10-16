package com.marginallyclever.communications;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;

public class TCPConnectionReader extends InputStreamReader implements Runnable {
	protected InputStream in;
	protected ArrayList<TCPConnectionEventListener> listeners;
	protected boolean keepGoing;
	
	public TCPConnectionReader(InputStream in) {
		super(in);
	    this.listeners = new ArrayList<TCPConnectionEventListener>();
	    keepGoing=true;
	}
	
	public TCPConnectionReader(InputStream in, Charset cs) {
		super(in, cs);
	    this.listeners = new ArrayList<TCPConnectionEventListener>();
	    keepGoing=true;
	}

	public void addListener( TCPConnectionEventListener l ) {
		this.listeners.add( l );
	}

	public void removeListener( TCPConnectionEventListener l ) {
		this.listeners.remove( l );
	}

	protected void notifyAll(int numBytes,byte [] buffer) {
		Iterator<TCPConnectionEventListener> i = listeners.iterator();
		while(i.hasNext()) {
			TCPConnectionEventListener l = i.next();
			l.dataAvailable(numBytes,buffer);
		}
	}

	public void stop() {
		keepGoing=false;
	}
	
	@Override
	public void run() {
		byte [] bytes = new byte[1024];

		while(keepGoing) {
			try {
				int bytesRead = in.read(bytes);
				
				notifyAll(bytesRead,bytes);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
}
