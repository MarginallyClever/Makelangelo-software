package com.marginallyclever.makelangeloRobot.plotterControls;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangelo.CommandLineOptions;
import com.marginallyclever.makelangelo.SoundSystem;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.makeArt.io.vector.SaveGCode;
import com.marginallyclever.makelangeloRobot.Plotter;
import com.marginallyclever.makelangeloRobot.PlotterEvent;
import com.marginallyclever.util.PreferencesHelper;

public class ProgramInterface extends JPanel {
	private static final long serialVersionUID = -7719350277524271664L;
	private Plotter myPlotter;
	private boolean isRunning=false;
	private boolean penIsUpBeforePause=false;
	private int nextLineNumber=0;
	// this list of gcode commands is stored separate from the Turtle.
	private ArrayList<String> gcodeCommands = new ArrayList<String>();
	
	public ProgramInterface(Plotter plotter) {
		super();
		myPlotter=plotter;
	}
	
	public void halt() {
		isRunning = false;
		penIsUpBeforePause = myPlotter.getPenIsUp();
		if(!myPlotter.getPenIsUp()) myPlotter.raisePen();
		//notifyListeners(new PlotterEvent(PlotterEvent.STOP,this));
	}

	public void start() {
		isRunning = true;
		if(!penIsUpBeforePause) myPlotter.lowerPen();
		sendFileCommand();
		//notifyListeners(new PlotterEvent(PlotterEvent.START,this));
	}

	public boolean isRunning() {
		return isRunning;
	}

	public int findLastPenUpBefore(int startAtLine) {
		int total = gcodeCommands.size();
		if (total == 0)
			return 0;

		String toMatch = myPlotter.getSettings().getPenUpString();

		int x = startAtLine;
		if (x >= total)
			x = total - 1;

		toMatch = toMatch.trim();
		while (x > 1) {
			String line = gcodeCommands.get(x).trim();
			if (line.equals(toMatch)) {
				return x;
			}
			--x;
		}

		return x;
	}
	
	private void setLineNumber(int count) {
		nextLineNumber=count;
		//notifyListeners(new PlotterEvent(PlotterEvent.PROGRESS_SOFAR, this, nextLineNumber));
	}

	public void startAt(int lineNumber) {
		if(lineNumber>=gcodeCommands.size()) lineNumber = gcodeCommands.size();
		if(lineNumber<0) lineNumber=0;

		setLineNumber(lineNumber);
		sendLineNumber(lineNumber);
		start();
	}

	public void setTurtle(Turtle turtle) {
		try(final OutputStream fileOutputStream = new FileOutputStream("currentDrawing.ngc")) {
			SaveGCode exportForDrawing = new SaveGCode();
			exportForDrawing.save(fileOutputStream, myPlotter);

			gcodeCommands.clear();
			BufferedReader reader = new BufferedReader(new FileReader("currentDrawing.ngc"));
			String line;
			while((line = reader.readLine()) != null) {
				gcodeCommands.add(line.trim());
			}
			reader.close();
		} catch (Exception e) {
			Log.error("setTurtle(): "+e.getMessage());
			e.printStackTrace();
		}
	}
	
	// Take the next line from the file and send it to the robot, if permitted.
	private void sendFileCommand() {
		if( !isRunning() ) return;
		
		// are there any more commands?
		if(nextLineNumber >= gcodeCommands.size()) {
			halt();
			SoundSystem.playDrawingFinishedSound();
		} else {
			String line = gcodeCommands.get(nextLineNumber);
			Log.message("Send: "+line);
			marlin.sendLineWithNumberAndChecksum(line, nextLineNumber);
			setLineNumber(nextLineNumber+1);
		}
	}

	public int getGCodeCommandsCount() {
		return gcodeCommands.size();
	}

	private void sendLineNumber(int newLineNumber) {
		marlin.send("M110 N" + newLineNumber);
	}
	
	// TEST
	
	public static void main(String[] args) {
		Log.start();
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {}
		
		JFrame frame = new JFrame(ProgramInterface.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new ProgramInterface(new Plotter()));
		frame.pack();
		frame.setVisible(true);
	}

}
