package com.marginallyclever.makelangeloRobot;

import java.util.Iterator;
import java.util.ServiceLoader;

import com.marginallyclever.communications.NetworkSessionEvent;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangeloRobot.machineStyles.MachineStyle;

public class RobotIdentityConfirmationBeforeMarlin extends RobotIdentityConfirmation {
	// Firmware check
	private static final String VERSION_CHECK_MESSAGE = "Firmware v";
	private static final long EXPECTED_FIRMWARE_VERSION = 10; // must match the version in the the firmware EEPROM
	
	private boolean firmwareVersionChecked = false;
	private boolean hardwareVersionChecked = false;
	private MakelangeloRobot robot; 
	
	public RobotIdentityConfirmationBeforeMarlin(MakelangeloRobot robot) {
		super();
		this.robot = robot;
		reset();
	} 
	
	public void reset() {
		setIdentityConfirmed(false);
		setPortConfirmed(false);
		firmwareVersionChecked = false;
		hardwareVersionChecked = false;
	}
	
	public void start() {
		try {
			robot.sendLineToRobot("M100\n");
		} catch (Exception e) {
			Log.error(e.getMessage());
		}
	}

	@Override
	public void networkSessionEvent(NetworkSessionEvent evt) {	
		if(evt.flag == NetworkSessionEvent.DATA_AVAILABLE) {	
			if(!getIdentityConfirmed()) {
				if(whenTheIntroductionsFinishOK((String)evt.data)) {
					setIdentityConfirmed(true);
					notifyListeners(new RobotIdentityEvent(this,RobotIdentityEvent.IDENTITY_CONFIRMED,null));
				}
			}
		}
	}

	private boolean whenTheIntroductionsFinishOK(String data) {
		if(!getPortConfirmed()) {
			if(doISeeAHello(data)) {
				setPortConfirmed(true);
			}
		}
		
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
		
		return getPortConfirmed() && firmwareVersionChecked && hardwareVersionChecked;
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
				notifyListeners(new RobotIdentityEvent(this,RobotIdentityEvent.BAD_FIRMWARE,afterV));
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

}
