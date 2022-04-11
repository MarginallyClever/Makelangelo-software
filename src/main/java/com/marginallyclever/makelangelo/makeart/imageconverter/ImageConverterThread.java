package com.marginallyclever.makelangelo.makeart.imageconverter;

import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

public class ImageConverterThread extends SwingWorker<Turtle, Void> {
	private static final Logger logger = LoggerFactory.getLogger(ImageConverterThread.class);
	private IterativeImageConverter chosenConverter;
	private String name;

	public ImageConverterThread(IterativeImageConverter converter) {
		super();
		chosenConverter = converter;
		this.name = converter.getName();
		
		chosenConverter.setThread(this);
	}
	
	@Override
	protected Turtle doInBackground() throws Exception {
		logger.debug("Starting {}", name);
		
		Turtle turtle = new Turtle();
		
		int loopCount=0;
		while(!isCancelled() && chosenConverter.iterate()) {
			loopCount++;
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				logger.debug("Thread interrupted.");
				break;
			}
		}

		logger.debug("Ending {} after {} loop(s).", name, loopCount);
		
		return turtle;
	}
	
	@Override
	public void done() {
		if(!isCancelled()) chosenConverter.finish();

		String state = isCancelled() ? "cancelled" : "finished";
		logger.debug("{} thread {}", state, name);
	}
}
