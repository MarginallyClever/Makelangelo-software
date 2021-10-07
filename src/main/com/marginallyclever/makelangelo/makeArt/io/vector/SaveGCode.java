package com.marginallyclever.makelangelo.makeArt.io.vector;

import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleMove;
import com.marginallyclever.makelangeloRobot.Plotter;
import com.marginallyclever.makelangeloRobot.settings.PlotterSettings;

/**
 * @author Dan Royer
 *
 */
public class SaveGCode implements TurtleSaver {
	private FileNameExtensionFilter filter = new FileNameExtensionFilter("GCode", "gcode");
	
	@Override
	public FileNameExtensionFilter getFileNameFilter() {
		return filter;
	}
	
	@Override
	public boolean save(OutputStream outputStream,Turtle turtle) throws Exception {
		Log.message("saving...");
		PlotterSettings machine = robot.getSettings();
		
		OutputStreamWriter out = new OutputStreamWriter(outputStream);
		out.write(machine.getProgramStart());
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
		
		Log.message("done.");
		return true;
	}
}
