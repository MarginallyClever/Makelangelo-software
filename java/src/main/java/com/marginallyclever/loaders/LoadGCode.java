package com.marginallyclever.loaders;

import java.io.IOException;

import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.makelangelo.GCodeFile;
import com.marginallyclever.makelangelo.Log;
import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;

public class LoadGCode implements LoadFileType {

	@Override
	public FileNameExtensionFilter getFileNameFilter() {
		return new FileNameExtensionFilter(Translator.get("FileTypeGCode"), "ngc");
	}

	@Override
	public boolean canLoad(String filename) {
		String ext = filename.substring(filename.lastIndexOf('.'));
		return (ext.equalsIgnoreCase(".ngc") || ext.equalsIgnoreCase(".gc"));
	}

	@Override
	public boolean load(String filename,MakelangeloRobot robot,Makelangelo gui) {
		if(robot.settings.isReverseForGlass()) {
			Log.message("Flipping for glass...");
		}

		GCodeFile file;
		try {
			file = new GCodeFile(filename,robot.settings.isReverseForGlass());
			
		} catch (IOException e) {
			Log.error(Translator.get("FileNotOpened") + e.getLocalizedMessage());
			return false;
		}

		Log.message(file.estimateCount + Translator.get("LineSegments") + "\n" + file.estimatedLength
				+ Translator.get("Centimeters") + "\n" + Translator.get("EstimatedTime")
				+ Log.millisecondsToHumanReadable((long) (file.estimatedTime)) + ".");

		gui.setGCode(file);
		gui.getDrawPanel().setGCode(file);
		gui.getDrawPanel().repaintNow();
		return true;
	}
}
