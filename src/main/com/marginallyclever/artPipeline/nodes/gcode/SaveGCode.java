package com.marginallyclever.artPipeline.nodes.gcode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.artPipeline.nodes.LoadAndSaveFile;
import com.marginallyclever.core.log.Log;
import com.marginallyclever.core.node.Node;
import com.marginallyclever.core.turtle.Turtle;
import com.marginallyclever.core.turtle.TurtleMove;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.robot.MakelangeloRobot;
import com.marginallyclever.makelangelo.robot.settings.MakelangeloRobotSettings;

/**
 * LoadGCode loads gcode into memory. 
 * @author Dan Royer
 *
 */
public class SaveGCode extends Node implements LoadAndSaveFile {
	private FileNameExtensionFilter filter = new FileNameExtensionFilter(Translator.get("FileTypeGCode"), "ngc");
	
	@Override
	public FileNameExtensionFilter getFileNameFilter() {
		return filter;
	}
	
	@Override
	public String getName() {
		return "SaveGCode";
	}
	
	@Override
	public boolean canLoad() {
		return false;
	}

	@Override
	public boolean canSave() {
		return true;
	}

	@Override
	public boolean canLoad(String filename) {
		return false;
	}

	@Override
	public boolean canSave(String filename) {
		String ext = filename.substring(filename.lastIndexOf('.'));
		return (ext.equalsIgnoreCase(".ngc") || ext.equalsIgnoreCase(".gc"));
	}
	
	@Override
	public boolean load(InputStream in) {
		return false;
	}

	@Override
	public boolean save(OutputStream outputStream,ArrayList<Turtle> turtles, MakelangeloRobot robot) {
		Log.message("saving...");
		
		try(OutputStreamWriter out = new OutputStreamWriter(outputStream)) {
			MakelangeloRobotSettings machine = robot.getSettings();
			machine.writeProgramStart(out);
			machine.writeAbsoluteMode(out);
			machine.writePenUp(out);
			boolean isUp=true;
			
			for( Turtle t : turtles ) {
				TurtleMove previousMovement=null;
				machine.writeChangeTo(out,t.getColor());
				for( TurtleMove m : t.history ) {
					boolean zMoved=false;
					if(m.isUp) {
						if(!isUp) {
							// lift pen up
							machine.writePenUp(out);
							isUp=true;
							zMoved=true;
						}
					} else {
						if(isUp) {
							// go to m and put pen down
							if(previousMovement!=null) {
								machine.writeMoveTo(out, previousMovement.x, previousMovement.y, true,true);
							} else {
								machine.writeMoveTo(out, m.x, m.y, true,true);
							}
							machine.writePenDown(out);
							isUp=false;
							zMoved=true;
						}
						machine.writeMoveTo(out,m.x, m.y,false,zMoved);
					}
					previousMovement=m;
				}
				if(!isUp) machine.writePenUp(out);
			}
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
