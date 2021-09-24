package com.marginallyclever.makelangelo.makeArt.io.gcode;

import java.awt.Component;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.convenience.turtle.TurtleMove;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeArt.io.SaveResource;
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
			out.write(machine.getProgramStart());
			out.write(machine.getAbsoluteMode());
			out.write(machine.getPenUpString());
			boolean isUp=true;
			
			TurtleMove previousMovement=null;
			for(int i=0;i<turtle.history.size();++i) {
				TurtleMove m = turtle.history.get(i);
				boolean zMoved=false;
				
				switch(m.type) {
				case TurtleMove.TRAVEL:
					if(!isUp) {
						// lift pen up
						out.write(machine.getPenUpString());
						isUp=true;
						zMoved=true;
					}
					previousMovement=m;
					break;
				case TurtleMove.DRAW:
					if(isUp) {
						// go to m and put pen down
						if(previousMovement==null) previousMovement=m;
						out.write(machine.getMoveTo(previousMovement.x, previousMovement.y, true,true));
						out.write(machine.getPenDownString());
						machine.getMoveTo(previousMovement.x, previousMovement.y, true,true);
						isUp=false;
						zMoved=true;
					}
					machine.getMoveTo(m.x, m.y,false,zMoved);
					previousMovement=m;
					break;
				case TurtleMove.TOOL_CHANGE:
					out.write(machine.getChangeTool(m.getColor()));
					machine.getChangeTool(m.getColor());
					break;
				}
			}
			if(!isUp) out.write(machine.getPenUpString());
			out.write(machine.getProgramEnd());
			
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
