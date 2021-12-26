package com.marginallyclever.makelangelo.makeArt.io.image;

import com.marginallyclever.makelangelo.makeArt.imageConverter.ImageConverter;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

public class ImageConverterThread extends SwingWorker<Turtle, Void> {
	private static final Logger logger = LoggerFactory.getLogger(ImageConverterThread.class);
	private ImageConverter chosenConverter;
	private String name;

	public ImageConverterThread(ImageConverter converter,String name) {
		super();
		chosenConverter = converter;
		this.name = name;
		
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
		String state;

		if(isCancelled()) {
			state = "cancelled";
		} else {
			chosenConverter.finish();
			state = "finished";
		}
		
		logger.debug("{} thread {}", state, name);
	}
}
