package com.marginallyclever.loaders;

import java.io.IOException;

import javax.swing.filechooser.FileNameExtensionFilter;

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
		try {
			if(robot.settings.isReverseForGlass()) {
				Log.message("Flipping for glass...");
			}
			gui.gCode.load(filename,robot.settings.isReverseForGlass());
			Log.message(gui.gCode.estimateCount + Translator.get("LineSegments") + "\n" + gui.gCode.estimatedLength
					+ Translator.get("Centimeters") + "\n" + Translator.get("EstimatedTime")
					+ Log.millisecondsToHumanReadable((long) (gui.gCode.estimatedTime)) + ".");
		} catch (IOException e) {
			Log.error(Translator.get("FileNotOpened") + e.getLocalizedMessage());
			gui.updateMenuBar();
			return false;
		}

		gui.gCode.changed = true;
		gui.getDrawPanel().setGCode(gui.gCode);
		gui.getDrawPanel().repaintNow();
		return true;
	}
}
