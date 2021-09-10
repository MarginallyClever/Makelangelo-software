package com.marginallyclever.artPipeline.io.gcode;

import java.awt.Component;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.artPipeline.io.SaveResource;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.convenience.turtle.TurtleMove;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;
import com.marginallyclever.makelangeloRobot.settings.MakelangeloRobotSettings;

/**
 * @author Dan Royer
 *
 */
public class SaveGCode implements SaveResource {
	private FileNameExtensionFilter filter = new FileNameExtensionFilter(Translator.get("FileTypeGCode"), "gcode");
	
	@Override
	public FileNameExtensionFilter getFileNameFilter() {
		return filter;
	}

	@Override
	public boolean canSave(String filename) {
		String ext = filename.substring(filename.lastIndexOf('.'));
		return ext.equalsIgnoreCase(".gcode");
	}
	
	@Override
	public boolean save(OutputStream outputStream,MakelangeloRobot robot, Component parentComponent) {
		Log.message("saving...");
		Turtle turtle = robot.getTurtle();
		MakelangeloRobotSettings machine = robot.getSettings();
		
		try(OutputStreamWriter out = new OutputStreamWriter(outputStream)) {
			machine.writeProgramStart(out);
			machine.writeAbsoluteMode(out);
			machine.writePenUp(out);
			boolean isUp=true;
			
			TurtleMove previousMovement=null;
			for(int i=0;i<turtle.history.size();++i) {
				TurtleMove m = turtle.history.get(i);
				boolean zMoved=false;
				
				switch(m.type) {
				case TRAVEL:
					if(!isUp) {
						// lift pen up
						machine.writePenUp(out);
						isUp=true;
						zMoved=true;
					}
					previousMovement=m;
					break;
				case DRAW:
					if(isUp) {
						// go to m and put pen down
						if(previousMovement==null) previousMovement=m;
						machine.writeMoveTo(out, previousMovement.x, previousMovement.y, true,true);
						machine.writePenDown(out);
						isUp=false;
						zMoved=true;
					}
					machine.writeMoveTo(out,m.x, m.y,false,zMoved);
					previousMovement=m;
					break;
				case TOOL_CHANGE:
					machine.writeChangeTo(out, m.getColor());
					break;
				}
			}
			if(!isUp) machine.writePenUp(out);
			machine.writeProgramEnd(out);
			
			out.flush();
			out.close();
		}
		catch(IOException e) {
			Log.error(Translator.get("SaveError") +" "+ e.getLocalizedMessage());
			return false;
		}
		
		Log.message("done.");
		return true;
	}
}
