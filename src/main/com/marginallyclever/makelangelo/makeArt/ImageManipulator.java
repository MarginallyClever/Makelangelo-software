package com.marginallyclever.makelangelo.makeArt;

import javax.swing.ProgressMonitor;

import com.marginallyclever.makelangelo.Paper;
import com.marginallyclever.makelangelo.makeArt.io.image.ImageConverterThread;
import com.marginallyclever.makelangelo.turtle.Turtle;

/**
 * shared methods for image manipulation (generating, converting, or filtering)
 * @author Dan
 */
public abstract class ImageManipulator {
	public Turtle turtle = new Turtle();
	
	// threading
	private ProgressMonitor pm;
	private ImageConverterThread thread;

	// helpers
	protected Paper myPaper;
		
	public void setThread(ImageConverterThread p) {
		thread = p;
	}

	public void setProgressMonitor(ProgressMonitor p) {
		pm = p;
	}
	
	public void setProgress(int d) {
		if(pm==null) return;
		pm.setProgress(d);
	}
	
	public void setPaper(Paper p) {
		myPaper = p;
	}

	public boolean isThreadCancelled() {
		if(thread!=null && thread.isCancelled()) return true;
		if(pm!=null && !pm.isCanceled()) return true;
		return false;
	}
	
	/**
	 * @return the translated name of the manipulator.
	 */
	public String getName() {
		return "Unnamed";
	}

	protected boolean isInsidePaperMargins(double x,double y) {
		return myPaper.isInsidePaperMargins(x,y);
	}
}