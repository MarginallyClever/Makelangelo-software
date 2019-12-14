package com.marginallyclever.convenience;

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import com.marginallyclever.makelangeloRobot.settings.MakelangeloRobotSettings;

public class TurtleGCode extends Turtle {
	private Writer writer;
	private MakelangeloRobotSettings settings;
	private DecimalFormat df;
	
	TurtleGCode(Writer _writer,MakelangeloRobotSettings _settings) {
		writer=_writer;
		settings=_settings;
		
		// set up number format
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
		otherSymbols.setDecimalSeparator('.');
		df = new DecimalFormat("#.###",otherSymbols);
		df.setGroupingUsed(false);
	}

	public void moveTo(double x,double y) {
		try {
			if(!isUp()) {
				settings.writeMoveTo(writer, getX(), getY(), isUp());
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		super.moveTo(x,y);
	}

	public void raisePen() {
		try {
			settings.writePenUp(writer);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		super.lowerPen();
		
	}
	public void lowerPen() {
		try {
			if(isUp()) {
				// we may have moved a bunch of times while the pen was up.
				// we only care about the last move.
				settings.writeMoveTo(writer, getX(), getY(), isUp());
			}
			settings.writePenDown(writer);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		super.lowerPen();
	}
}
