package com.marginallyclever.artPipeline.io.image;

import javax.swing.SwingWorker;

import com.marginallyclever.artPipeline.converters.ImageConverter;
import com.marginallyclever.convenience.log.Log;

public class ImageConverterThread extends SwingWorker<Void, Void> {
	private ImageConverter chosenConverter;
	private String name;
	private LoadImage listener;

	public ImageConverterThread(LoadImage createdBy,ImageConverter converter,String name) {
		listener = createdBy;
		chosenConverter = converter;
		this.name = name;
		
		chosenConverter.setThread(this);
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		Log.message("Starting "+name);

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
		
		return null;
	}
	
	@Override
	public void done() {
		String state;

		if(isCancelled()) {
			state = "Cancelled";
		} else {
			chosenConverter.finish();
			state = "Finished";
		}
		
		Log.message(state+" thread "+name);

		listener.done(this);
	}
}
