package com.marginallyclever.makelangelo;

import com.marginallyclever.communications.MarginallyCleverConnection;
import com.marginallyclever.communications.MarginallyCleverConnectionReadyListener;

/**
 * @author Admin
 * @since 7.2.10
 *
 */
public class MakelangeloRobot implements MarginallyCleverConnectionReadyListener {
	// constants
	private String robotTypeName = "DRAWBOT";
	private String hello = "HELLO WORLD! I AM " + robotTypeName + " #";

	// god object!
	private final Makelangelo mainGUI=null;
		
	// settings go here
	public MakelangeloRobotSettings settings = null;
	
	// current state goes here
	private MarginallyCleverConnection connection = null;
	private boolean portConfirmed = false;
	
	
	public MarginallyCleverConnection getConnection() {
		return connection;
	}

	public void setConnection(MarginallyCleverConnection c) {
		this.connection = c;
		
		this.connection.addListener(this);
	}

	@Override
	public void serialConnectionReady(MarginallyCleverConnection arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void serialDataAvailable(MarginallyCleverConnection arg0, String data) {
		if (portConfirmed == true) return;
		if (data.lastIndexOf(hello) < 0) return;

		portConfirmed = true;

		String after_hello = data.substring(data.lastIndexOf(hello) + hello.length());
		settings.parseRobotUID(after_hello);

		if(mainGUI != null) mainGUI.confirmConnected();
	}
	
	public boolean isPortConfirmed() {
		return portConfirmed;
	}
}
