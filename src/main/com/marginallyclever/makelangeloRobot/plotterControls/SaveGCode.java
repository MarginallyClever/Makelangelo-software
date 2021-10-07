package com.marginallyclever.makelangeloRobot.plotterControls;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleMove;
import com.marginallyclever.makelangeloRobot.Plotter;

public class SaveGCode {
	private JFileChooser fc = new JFileChooser();
	private FileNameExtensionFilter filter = new FileNameExtensionFilter("GCode", "gcode");
	
	public SaveGCode() {
		fc.addChoosableFileFilter(filter);
		// do not allow wild card (*.*) file extensions
		fc.setAcceptAllFileFilterUsed(false);
	}
	
	public SaveGCode(String lastDir) {
		// remember the last path used, if any
		fc.setCurrentDirectory((lastDir==null?null : new File(lastDir)));
	}
	
	public void run(Turtle turtle,Plotter robot,JComponent parent) throws Exception {		
		if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
			String selectedFile = fc.getSelectedFile().getAbsolutePath();
			String withExtension = addExtension(selectedFile,((FileNameExtensionFilter)fc.getFileFilter()).getExtensions());
			Log.message("File selected by user: "+withExtension);
			OutputStream out = new FileOutputStream(new File(withExtension));
			save(out,turtle,robot);
		}
	}

	private String addExtension(String name, String [] extensions) {
		for( String e : extensions ) {
			if(FilenameUtils.getExtension(name).equalsIgnoreCase(e)) return name;
		}
		
		return name + "." + extensions[0];
	}
	
	private void save(OutputStream outputStream,Turtle turtle,Plotter robot) throws Exception {
		Log.message("saving...");
		
		OutputStreamWriter out = new OutputStreamWriter(outputStream);

		SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");  
		Date date = new Date(System.currentTimeMillis());  
		out.write("; "+formatter.format(date)+"\n");
		out.write("G28\n");  // go home
		
		boolean isUp=true;
		
		TurtleMove previousMovement=null;
		for(int i=0;i<turtle.history.size();++i) {
			TurtleMove m = turtle.history.get(i);
			
			switch(m.type) {
			case TurtleMove.TRAVEL:
				if(!isUp) {
					// lift pen up
					out.write(MarlinInterface.getPenUpString(robot));
					isUp=true;
				}
				previousMovement=m;
				break;
			case TurtleMove.DRAW:
				if(isUp) {
					// go to m and put pen down
					if(previousMovement==null) previousMovement=m;
					out.write(MarlinInterface.getTravelTo(previousMovement.x, previousMovement.y));
					out.write(MarlinInterface.getPenDownString(robot));
					isUp=false;
				}
				out.write(MarlinInterface.getDrawTo(m.x, m.y));
				previousMovement=m;
				break;
			case TurtleMove.TOOL_CHANGE:
				out.write(MarlinInterface.getToolChangeString(m.getColor().toInt()));
				break;
			}
		}
		if(!isUp) out.write(MarlinInterface.getPenUpString(robot));
		
		out.flush();
		out.close();
		
		Log.message("done.");
	}
}
