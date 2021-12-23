package com.marginallyclever.makelangelo.makeArt.io.image;

import javax.swing.SwingWorker;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.makeArt.imageConverter.ImageConverter;
import com.marginallyclever.makelangelo.turtle.Turtle;

public class ImageConverterThread extends SwingWorker<Turtle, Void> {
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
		Log.message("Starting "+name);
		
		Turtle turtle = new Turtle();
		
		int loopCount=0;
		while(!isCancelled() && chosenConverter.iterate()) {
			loopCount++;
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				Log.message("Thread interrupted.");
				break;
			}
		}

		Log.message("Ending "+name+" after "+loopCount+" loop(s).");
		
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
		
		Log.message(state+" thread "+name);
	}
}
