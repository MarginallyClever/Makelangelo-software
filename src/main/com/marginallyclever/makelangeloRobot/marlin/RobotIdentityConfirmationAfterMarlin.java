package com.marginallyclever.makelangeloRobot.marlin;

import java.util.Iterator;
import java.util.ServiceLoader;

import com.marginallyclever.communications.NetworkSessionEvent;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangeloRobot.machineStyles.MachineStyle;

public class RobotIdentityConfirmationAfterMarlin extends RobotIdentityConfirmation {
	private static final String EXPECTED_FIRMWARE_VERSION = "2.0.9"; // must match the version in the the firmware EEPROM
	private static final String AUTHOR = "Author: (Marginally Clever,";
	
	private boolean firmwareVersionChecked = false;
	private boolean hardwareVersionChecked = false;
	private String versionFound;
	
	public RobotIdentityConfirmationAfterMarlin() {
		super();
		reset();
	} 
	
	public void reset() {
		setIdentityConfirmed(false);
		setPortConfirmed(false);
		firmwareVersionChecked = false;
		hardwareVersionChecked = false;
	}
	
	public void start() {}

	public String getVersion() {
		return versionFound;
	}
	
	@Override
	public void networkSessionEvent(NetworkSessionEvent evt) {	
		if(evt.flag == NetworkSessionEvent.DATA_RECEIVED) {	
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
		
		if(!hardwareVersionChecked) {
			String version=doISeeAHardwareVersion(data);
			if(version!=null) {
				hardwareVersionChecked = true;
				versionFound = version;
			}
		}
		
		return getPortConfirmed() && firmwareVersionChecked && hardwareVersionChecked;
	}

	private String doISeeAHardwareVersion(String data) {
		int a = data.lastIndexOf(AUTHOR); 
		if(a >= 0) {
			int b = data.lastIndexOf(")");
			String machineType = data.substring(a+AUTHOR.length(),b).trim();
			Log.message("Hardware version check found "+machineType);
			return machineType;
		}

		return null;
	}

	private boolean doISeeAHello(String data) {
		ServiceLoader<MachineStyle> knownHardware = ServiceLoader.load(MachineStyle.class);
		Iterator<MachineStyle> i = knownHardware.iterator();
		while (i.hasNext()) {
			MachineStyle ms = i.next();
			String myHello = ms.getHello();
			if (data.lastIndexOf(myHello) >= 0) {
				Log.message("Heard "+myHello+".  Hello found.");
				// which firmware is this?
				String afterHello = data.substring(data.lastIndexOf(myHello) + myHello.length()).trim();
				if(afterHello.compareTo(EXPECTED_FIRMWARE_VERSION)>=0) {
					Log.message("OK");
					firmwareVersionChecked=true;
					return true;
				} else {
					Log.message("BAD");
					notifyListeners(new RobotIdentityEvent(this,RobotIdentityEvent.BAD_FIRMWARE,afterHello));
				}
			}
		}
		
		return false;
	}

}
