package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

public class ImageConverterThread extends SwingWorker<Turtle, Void> {
	private static final Logger logger = LoggerFactory.getLogger(ImageConverterThread.class);
	private IterativeImageConverter myConverter;
	private String name;
	private boolean paused=false;
	private int iterations=0;

	public ImageConverterThread(IterativeImageConverter converter) {
		super();
		this.myConverter = converter;
		this.name = converter.getName();
		myConverter.setThread(this);
		setPaused(false);
	}

	public void setPaused(boolean paused) {
		logger.debug("{} thread {}", paused ? "pausing" : "resuming", name);
		this.paused = paused;
	}

	public boolean getPaused() {
		return paused;
	}
	
	@Override
	protected Turtle doInBackground() throws Exception {
		logger.debug("Starting {}", name);
		
		Turtle turtle = new Turtle();

		iterations=0;
		int pauseCount=-1;
		while(!isCancelled()) {
			if(!paused) {
				if(pauseCount==iterations) {
					myConverter.resume();
				}
				myConverter.iterate();
				iterations++;
			} else {
				if(pauseCount!=iterations) {
					pauseCount=iterations;
					myConverter.generateOutput();
				}
			}
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {}
		}

		logger.debug("Ending {} after {} loop(s).", name, iterations);
		
		return turtle;
	}
	
	@Override
	public void done() {
		String state = isCancelled() ? "cancelled" : "finished";
		logger.debug("{} thread {}", state, name);
	}
}
