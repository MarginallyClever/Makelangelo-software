package com.marginallyclever.makelangeloRobot.loadAndSave;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.gcode.GCodeFile;
import com.marginallyclever.makelangelo.Log;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;

/**
 * LoadGCode loads gcode into memory. 
 * @author Admin
 *
 */
public class LoadAndSaveGCode implements LoadAndSaveFileType {
	private FileNameExtensionFilter filter = new FileNameExtensionFilter(Translator.get("FileTypeGCode"), "ngc");
	
	@Override
	public FileNameExtensionFilter getFileNameFilter() {
		return filter;
	}

	@Override
	public boolean canLoad(String filename) {
		String ext = filename.substring(filename.lastIndexOf('.'));
		return (ext.equalsIgnoreCase(".ngc") || ext.equalsIgnoreCase(".gc"));
	}

	@Override
	public boolean canSave(String filename) {
		String ext = filename.substring(filename.lastIndexOf('.'));
		return (ext.equalsIgnoreCase(".ngc") || ext.equalsIgnoreCase(".gc"));
	}

	
	@Override
	public boolean load(InputStream in,MakelangeloRobot robot) {
		if(robot.getSettings().isReverseForGlass()) {
			Log.message("Flipping for glass...");
		}

		GCodeFile file;
		try {
			file = new GCodeFile(in,robot.getSettings().isReverseForGlass());
			
		} catch (IOException e) {
			Log.error(Translator.get("LoadError") +" "+ e.getLocalizedMessage());
			return false;
		}

		Log.message(file.estimateCount + Translator.get("LineSegments") + "\n" + file.estimatedLength
				+ Translator.get("Centimeters") + "\n" + Translator.get("EstimatedTime")
				+ Log.millisecondsToHumanReadable((long) (file.estimatedTime)) + ".");

		robot.setGCode(file);
		return true;
	}

	@Override
	public boolean save(OutputStream outputStream,MakelangeloRobot robot) {
		Log.message("saving...");
		GCodeFile sourceMaterial = robot.gCode;
		sourceMaterial.setLinesProcessed(0);
		
		OutputStreamWriter out = new OutputStreamWriter(outputStream);
		try {
			int total=sourceMaterial.getLinesTotal();
			Log.message(total+" total lines to save.");
			for(int i=0;i<total;++i) {
				String str = sourceMaterial.nextLine();
				if(!str.endsWith(";")) str+=";";
				if(!str.endsWith("\n")) str+="\n";
				out.write(str);
			}
		}
		catch(IOException e) {
			Log.error(Translator.get("SaveError") +" "+ e.getLocalizedMessage());
			return false;
		}
		
		Log.message("done.");
		return true;
	}

	@Override
	public boolean canLoad() {
		return true;
	}

	@Override
	public boolean canSave() {
		return true;
	}
}
