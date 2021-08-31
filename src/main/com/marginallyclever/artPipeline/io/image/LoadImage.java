package com.marginallyclever.artPipeline.io.image;

import java.awt.Component;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.ProgressMonitor;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.artPipeline.TransformedImage;
import com.marginallyclever.artPipeline.converters.ImageConverter;
import com.marginallyclever.artPipeline.io.LoadResource;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;
import com.marginallyclever.makelangeloRobot.settings.MakelangeloRobotSettings;

/**
 * LoadImage uses an InputStream of data to create gcode. 
 * @author Dan Royer
 *
 */
public class LoadImage implements LoadResource {
	private ImageConverter chosenConverter;
	private TransformedImage img;
	private MakelangeloRobot myRobot;
	
	private ImageConverterThread imageConverterThread; 
	private ArrayList<ImageConverterThread> workerList = new ArrayList<ImageConverterThread>();
	private int workerCount = 0;
	
	// Set of image file extensions.
	private static final Set<String> IMAGE_FILE_EXTENSIONS = new HashSet<String>(Arrays.asList("jpg","jpeg","png","wbmp","bmp","gif"));
	private static FileNameExtensionFilter filter = new FileNameExtensionFilter(Translator.get("FileTypeImage"),
			IMAGE_FILE_EXTENSIONS.toArray(new String[IMAGE_FILE_EXTENSIONS.size()]));
	private ProgressMonitor pm;
	
	
	public LoadImage() {}
	
	@Override
	public FileNameExtensionFilter getFileNameFilter() {
		return filter;
	}

	@Override
	public boolean canLoad(String filename) {
		final String filenameExtension = filename.substring(filename.lastIndexOf('.') + 1);
		return IMAGE_FILE_EXTENSIONS.contains(filenameExtension.toLowerCase());
	}

	public void stopConversion() {
		Log.message("Stop conversion");
		if(imageConverterThread!=null) imageConverterThread.cancel(true);
		stopWorker();
	}
	
	public void changeConverter(ImageConverter converter) {
		if( converter == chosenConverter ) return;
		
		Log.message("Changing converter");
		stopConversion();
		startConversion(converter);
	}
	
	private void startConversion(ImageConverter converter) {
		if(converter==null) return;  // TODO silent quit is ugly.
		
		ImageConverter.loadImage = this;
		chosenConverter = converter;
		Log.message("Converter="+chosenConverter.getName());
		
		startWorker();
	}
	
	public void reconvert() {
		stopConversion();
		startConversion(chosenConverter);
	}
	
	public ImageConverter getChosenConverter() {
		return chosenConverter;
	}
	
	/**
	 * Load and convert the image in the chosen style
	 * @return false if loading cancelled or failed.
	 */
	@Override
	public Turtle load(InputStream in,MakelangeloRobot robot, Component parentComponent) throws Exception {
		myRobot = robot;

		img = new TransformedImage( ImageIO.read(in) );
		
		return null;
	}

	// adjust image to fill the paper
	public void scaleToFillPaper() {
		MakelangeloRobotSettings s = myRobot.getSettings();
		double width  = s.getMarginWidth();
		double height = s.getMarginHeight();

		float f;
		if( width > height ) {
			f = (float)( width / (double)img.getSourceImage().getWidth() );
		} else {
			f = (float)( height / (double)img.getSourceImage().getHeight() );
		}
		img.setScale(f,-f);
	}
	
	public void scaleToFitPaper() {
		MakelangeloRobotSettings s = myRobot.getSettings();
		double width  = s.getMarginWidth();
		double height = s.getMarginHeight();
		
		float f;
		if( width < height ) {
			f = (float)( width / (double)img.getSourceImage().getWidth() );
		} else {
			f = (float)( height / (double)img.getSourceImage().getHeight() );
		}
		img.setScale(f,-f);
	}
	
	private void stopWorker() {
		if(chosenConverter!=null) chosenConverter.stopIterating();
		if(imageConverterThread!=null) {
			Log.message("Stopping worker");
			if(imageConverterThread.cancel(true)) {
				Log.message("stop OK");
			} else {
				Log.message("stop FAILED");
			}
		}
	}

	private void startWorker() {
		Log.message("Starting thread "+workerCount);

		ProgressMonitor pm = new ProgressMonitor(null, Translator.get("Converting"), "", 0, 100);
		pm.setProgress(0);
		pm.setMillisToPopup(0);
		
		chosenConverter.setProgressMonitor(pm);
		chosenConverter.setRobot(myRobot);
		chosenConverter.setImage(img);
		
		myRobot.setDecorator(chosenConverter);
		
		imageConverterThread = getNewWorker(chosenConverter,workerCount);
		addWorker(imageConverterThread);
		imageConverterThread.execute();
	}
	
	private ImageConverterThread getNewWorker(ImageConverter chosenConverter2, int workerCount2) {
		ImageConverterThread thread = new ImageConverterThread(this,chosenConverter2,Integer.toString(workerCount2));
		
		thread.addPropertyChangeListener((evt) -> {
			if(!Objects.equals("progress", evt.getPropertyName())) return;
			
			int progress = (Integer) evt.getNewValue();
			pm.setProgress(progress);
			String message = String.format("%d%%.\n", progress);
			pm.setNote(message);
			if (imageConverterThread.isDone()) {
				Log.message(Translator.get("Finished"));
			} else if (imageConverterThread.isCancelled() || pm.isCanceled()) {
				if(pm.isCanceled()) imageConverterThread.cancel(true);
				Log.message(Translator.get("Cancelled"));
			}
		});

		return thread;
	}

	// called when thread is finished, no matter how the thread is finished.
	public void done(ImageConverterThread thread) {
		if(thread.isDone()) {
			if(myRobot!=null) myRobot.setTurtle(chosenConverter.turtle);
			if(pm!=null) pm.setProgress(100);
		}
		if(myRobot!=null) myRobot.setDecorator(null);
		if(pm!=null) pm.close();
		removeWorker(thread);
	}
	
	private void addWorker(ImageConverterThread thread) {
		workerList.add(thread);
		workerCount++;
		Log.message("Added worker.  "+workerList.size()+" workers now.");
	}
	
	private void removeWorker(ImageConverterThread thread) {
		workerList.remove(thread);
		Log.message("Removed worker.  "+workerList.size()+" workers now.");
		if(imageConverterThread==thread)
			imageConverterThread=null;
	}

}
