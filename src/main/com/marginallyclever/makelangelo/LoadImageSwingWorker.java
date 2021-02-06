package com.marginallyclever.makelangelo;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Objects;

import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import com.marginallyclever.artPipeline.TurtleNode;
import com.marginallyclever.artPipeline.nodes.ImageConverter;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangeloRobot.MakelangeloRobotPanel;

/**
 * 
 * @author Dan Royer
 *
 */
@Deprecated
public class LoadImageSwingWorker extends SwingWorker<ArrayList<Turtle>,Void> {
	public int loopCount;
	protected TurtleNode chosenConverter;
	protected ProgressMonitor progressMonitor;
	
	public LoadImageSwingWorker(ImageConverter c,ProgressMonitor pm) {
		super();
		chosenConverter = c;
		progressMonitor = pm;
		
		addPropertyChangeListener(new PropertyChangeListener() {
			// Invoked when task's progress property changes.
			public void propertyChange(PropertyChangeEvent evt) {
				if (Objects.equals("progress", evt.getPropertyName())) {
					int progress = (Integer) evt.getNewValue();
					pm.setProgress(progress);
					pm.setNote(String.format("%d%%.\n", progress));
				}
			}
		});
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
		
		if(progressMonitor.isCanceled()) {
			Log.message("Thread cancelled.");
		}
		
		return turtleList;
	}
	
	@Override
	public void done() {
		if(progressMonitor!=null) progressMonitor.close();

		ArrayList<Turtle> list = get();

		Log.message("Thread ended after "+loopCount+" iteration(s).");
		
		MakelangeloRobotPanel panel = chosenRobot.getControlPanel();
		if(panel!=null) panel.updateButtonAccess();
	}
}
