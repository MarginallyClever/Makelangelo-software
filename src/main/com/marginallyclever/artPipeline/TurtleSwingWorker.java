package com.marginallyclever.artPipeline;

import java.util.ArrayList;

import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import com.marginallyclever.artPipeline.converters.ImageConverter;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.MakelangeloRobotPanel;

public class TurtleSwingWorker extends SwingWorker<ArrayList<Turtle>,Void> {
	public int loopCount;
	protected ImageConverter chosenConverter;
	protected ProgressMonitor progressMonitor;
	
	public TurtleSwingWorker(ImageConverter c,ProgressMonitor pm) {
		super();
		chosenConverter = c;
		progressMonitor = pm;
	}

	@Override
	public ArrayList<Turtle> doInBackground() {
		Log.message("Starting thread 2");
		
		loopCount=0;
		chosenConverter.setThreadWorker(this);

		boolean keepIterating=false;
		
		do {
			loopCount++;
			keepIterating = chosenConverter.iterate();
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				Log.message("swingWorker interrupted.");
				break;
			}
		} while(!progressMonitor.isCanceled() && keepIterating);

		ArrayList<Turtle> turtleList = null;
		
		if(!progressMonitor.isCanceled()) {
			turtleList = chosenConverter.finish();
		} else {
			Log.message("Thread cancelled.");
		}
		
		return turtleList;
	}
	
	@Override
	public void done() {
		if(progressMonitor!=null) progressMonitor.close();

		ArrayList<Turtle> list = get();

		Log.message("Thread ended after "+loopCount+" iteration(s).");
		workerList.remove(threadWorker);
		workerCount--;
		Log.message("Removed worker.  "+workerCount+"/"+workerList.size()+" workers now.");
		threadWorker=null;
		
		MakelangeloRobotPanel panel = chosenRobot.getControlPanel();
		if(panel!=null) panel.updateButtonAccess();
	}
}
