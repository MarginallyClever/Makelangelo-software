package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class ImageConverterThread extends SwingWorker<Turtle, Void> {
	private static final Logger logger = LoggerFactory.getLogger(ImageConverterThread.class);
	private final ImageConverterIterative myConverter;
	private final String name;
	private boolean paused=false;
	private boolean enough=false;

	public ImageConverterThread(ImageConverterIterative converter) {
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

	/**
	 * Will cause the thread to end gracefully at the start of the next iteration.
	 */
	public void endThreadGracefully() {
		logger.debug("endThreadGracefully()");
		this.enough = true;
	}

	@Override
	protected Turtle doInBackground() throws Exception {
		logger.debug("doInBackground() start {}", name);
		
		Turtle turtle = new Turtle();
		turtle.setStroke(Color.BLACK,myConverter.getPlotterSettings().getDouble(PlotterSettings.DIAMETER));

		int iterations = 0;
		int pauseCount=-1;
		try {
			while (!enough && !isCancelled()) {
				if (!paused) {
					if (pauseCount == iterations) {
						myConverter.resume();
					}
					myConverter.iterate();
					iterations++;
				} else {
					if (pauseCount != iterations) {
						pauseCount = iterations;
						myConverter.generateOutput();
					}
				}
				try {
					Thread.sleep(5);
				} catch (Exception ignored) {
				}
			}
		} catch(Exception e) {
			logger.error("caught exception",e);
		}

		logger.debug("doInBackground() ending {} after {} loop(s).", name, iterations);
		
		return turtle;
	}
	
	@Override
	public void done() {
		logger.debug("{} thread {}", isCancelled() ? "cancelled" : "finished", name);
	}
}
