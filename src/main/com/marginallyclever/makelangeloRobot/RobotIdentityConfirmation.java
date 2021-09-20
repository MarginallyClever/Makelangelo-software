package com.marginallyclever.makelangeloRobot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ServiceLoader;

import com.marginallyclever.communications.NetworkSessionEvent;
import com.marginallyclever.communications.NetworkSessionListener;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangeloRobot.machineStyles.MachineStyle;

public class RobotIdentityConfirmation implements NetworkSessionListener {
	// Firmware check
	private static final String VERSION_CHECK_MESSAGE = "Firmware v";
	private static final long EXPECTED_FIRMWARE_VERSION = 10; // must match the version in the the firmware EEPROM
	
	private boolean identityConfirmed = false;
	private boolean portConfirmed = false;
	private boolean firmwareVersionChecked = false;
	private boolean hardwareVersionChecked = false;
	private MakelangeloRobot robot; 
	
	public RobotIdentityConfirmation(MakelangeloRobot robot) {
		super();
		this.robot = robot;
		reset();
	} 
	
	public void reset() {
		identityConfirmed = false;
		portConfirmed = false;
		firmwareVersionChecked = false;
		hardwareVersionChecked = false;
	}
	
	public boolean getIdentityConfirmed() {
		return identityConfirmed;
	}

	@Override
	public void networkSessionEvent(NetworkSessionEvent evt) {	
		if(evt.flag == NetworkSessionEvent.DATA_AVAILABLE) {	
			if(!identityConfirmed) {
				identityConfirmed = whenTheIntroductionsFinishOK((String)evt.data);
				if(identityConfirmed) {
					notifyListeners(new RobotIdentityEvent(this,RobotIdentityEvent.IDENTITY_CONFIRMED,null));
				}
			}
		}
	}

	private boolean whenTheIntroductionsFinishOK(String data) {
		if(!portConfirmed) portConfirmed = doISeeAHello(data);
		
		if(!firmwareVersionChecked) {
			boolean isGood=doIseeMyFavoriteFirmwareVersion(data);
			if(isGood) {
				firmwareVersionChecked=true;
				// request the hardware version of this robot
				robot.sendLineToRobot("D10\n");
				robot.sendLineToRobot("M503\n");
			}
		}
		
		if(!hardwareVersionChecked) {
			String version=doISeeAHardwareVersion(data);
			if(version!=null) {
				hardwareVersionChecked=true;
				robot.getSettings().setHardwareVersion(version);
			}
		}
		
		return portConfirmed && firmwareVersionChecked && hardwareVersionChecked;
	}
	
	private String doISeeAHardwareVersion(String data) {
		if(data.lastIndexOf("D10") >= 0) {
			String[] pieces = data.split(" ");
			if (pieces.length > 1) {
				String last = pieces[pieces.length-1];
				Log.message("Heard "+data+", hardware version check "+last);
				last = last.replace("\r\n", "");
				if (last.startsWith("V")) {
					String versionFound = last.substring(1).trim();
					Log.message("OK");
					return versionFound;
				} else {
					Log.message("BAD");
					notifyListeners(new RobotIdentityEvent(this,RobotIdentityEvent.BAD_HARDWARE,last));
				}
			}
		}

		return null;
	}

	private boolean doIseeMyFavoriteFirmwareVersion(String data) {
		if (data.lastIndexOf(VERSION_CHECK_MESSAGE) >= 0) {
			String afterV = data.substring(VERSION_CHECK_MESSAGE.length()).trim();
			Log.message("Heard "+VERSION_CHECK_MESSAGE+", software version check "+afterV);
			
			long versionFound = 0;
			try {
				versionFound = Long.parseLong(afterV);
			} finally {}
			
			if (versionFound == EXPECTED_FIRMWARE_VERSION) {
				Log.message("OK");
				return true;
			} else {
				Log.message("BAD");
				notifyListeners(new RobotIdentityEvent(this,RobotIdentityEvent.BAD_FIRMWARE,Long.toString(versionFound)));
			}
		}
		
		return false;
	}

	private boolean doISeeAHello(String data) {
		ServiceLoader<MachineStyle> knownHardware = ServiceLoader.load(MachineStyle.class);
		Iterator<MachineStyle> i = knownHardware.iterator();
		while (i.hasNext()) {
			MachineStyle ms = i.next();
			String myHello = ms.getHello();
			if (data.lastIndexOf(myHello) >= 0) {
				Log.message("Heard "+myHello+".  Port confirmed.");
				portConfirmed = true;
				// which machine GUID is this?
				String afterHello = data.substring(data.lastIndexOf(myHello) + myHello.length());
				robot.getSettings().saveConfig();
				long id=robot.findOrCreateUID(afterHello);
				robot.getSettings().loadConfig(id);
				return true;
			}
		}
		
		return false;
	}

	public boolean getPortConfirmed() {
		return portConfirmed;
	}
	
	// OBSERVER PATTERN

	private ArrayList<RobotIdentityEventListener> listeners = new ArrayList<RobotIdentityEventListener>();
	public void addRobotIdentityEventListener(RobotIdentityEventListener a) {
		listeners.add(a);
	}
	
	public void removeRobotIdentityEventListener(RobotIdentityEventListener a) {
		listeners.remove(a);
	}
	
	private void notifyListeners(RobotIdentityEvent e) {
		for( RobotIdentityEventListener a : listeners ) {
			a.robotIdentityEvent(e);
		}
	}
}
