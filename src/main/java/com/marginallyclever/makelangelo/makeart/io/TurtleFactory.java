package com.marginallyclever.makelangelo.makeart.io;

import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Turtle;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory for creating {@link Turtle} objects from vector files and writing them to vector files.
 * @author Dan Royer
 */
public class TurtleFactory {
	private static final TurtleLoader [] loaders = {
			new LoadDXF(),
			new LoadFactorioMap(),
			new LoadGCode(),
			new LoadScratch2(),
			new LoadScratch3(),
			new LoadSVG(),
	};
	
	private static final TurtleSaver [] savers = {
			new SaveDXF(),
			new SaveSVG(),
			new SaveGCode(),
			new SaveBitmap("bmp",false),
			new SaveBitmap("gif",false),
			new SaveBitmap("jpg",false),
			new SaveBitmap("pio",false),
			new SaveBitmap("png",true),
			new SaveBitmap("tif",false),
			new SaveBitmap("webp",true),
	};
	
	public static Turtle load(String filename) throws Exception {
		if(filename == null || filename.trim().isEmpty()) throw new InvalidParameterException("filename cannot be empty");

		for( TurtleLoader loader : loaders ) {
			if(isValidExtension(filename,loader.getFileNameFilter())) {
				try(FileInputStream in = new FileInputStream(filename)) {
					return loader.load(in);
				}
			}
		}
		throw new IllegalStateException("TurtleFactory could not load '"+filename+"'.");
	}
	
	private static boolean isValidExtension(String filename, FileNameExtensionFilter filter) {
		filename = filename.toLowerCase();
		String [] extensions = filter.getExtensions();
		for( String e : extensions ) {
			if(filename.endsWith(e.toLowerCase())) return true;
		}
		return false;
	}

	public static List<FileNameExtensionFilter> getLoadExtensions() {
		List<FileNameExtensionFilter> filters = new ArrayList<>();
		for( TurtleLoader loader : loaders ) {
			filters.add( loader.getFileNameFilter() );
		}
		return filters;
	}

	public static List<FileNameExtensionFilter> getSaveExtensions() {
		List<FileNameExtensionFilter> filters = new ArrayList<>();
		for( TurtleSaver saver : savers ) {
			filters.add( saver.getFileNameFilter() );
		}
		return filters;
	}

	public static void save(Turtle turtle,String filename, PlotterSettings settings) throws Exception {
		if(filename == null || filename.trim().isEmpty()) throw new InvalidParameterException("filename cannot be empty");

		for (TurtleSaver saver : savers) {
			if(isValidExtension(filename,saver.getFileNameFilter())) {
				try (FileOutputStream out = new FileOutputStream(filename)) {
					saver.save(out, turtle,settings);
				}
				return;
			}
		}
		String extension = filename.substring(filename.lastIndexOf("."));
		throw new Exception("TurtleFactory could not save '"+filename+"' : invalid file format '" + extension + "'");
	}
}
