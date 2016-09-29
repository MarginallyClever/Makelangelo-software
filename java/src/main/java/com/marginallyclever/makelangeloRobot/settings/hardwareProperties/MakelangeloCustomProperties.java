package com.marginallyclever.makelangeloRobot.settings.hardwareProperties;

public class MakelangeloCustomProperties extends Makelangelo2Properties {
	public final static float PEN_HOLDER_RADIUS=6; //cm

	@Override
	public int getVersion() {
		return 0;
	}

	@Override
	public String getName() {
		return "Makelangelo (Custom)";
	}

	@Override
	public boolean canInvertMotors() {
		return true;
	}
	
	@Override
	public boolean canChangeMachineSize() {
		return true;
	}

	@Override
	public boolean canAccelerate() {
		return true;
	}

	@Override
	public boolean canChangePulleySize() {
		return true;
	}

	@Override
	public boolean canAutoHome() {
		return false;
	}

	public float getWidth() { return 3*12*25.4f; }
	public float getHeight() { return 4*12*25.4f; }
}
