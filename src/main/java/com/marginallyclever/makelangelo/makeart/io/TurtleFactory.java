package com.marginallyclever.makelangelo.makeart.io;

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
	private static TurtleLoader [] loaders = {
			new LoadDXF(),
			new LoadGCode(),
			new LoadScratch2(),
			new LoadScratch3(),
			new LoadSVG(),
	};
	
	private static TurtleSaver [] savers = {
			new SaveDXF(),
			new SaveSVG(),
			new SaveBitmap("BMP",false),
			new SaveBitmap("GIF",false),
			new SaveBitmap("JPG",false),
			new SaveBitmap("PIO",false),
			new SaveBitmap("PNG",true),
			// TODO no SaveGCode?
	};
	
	public static Turtle load(String filename) throws Exception {
		if(filename == null || filename.trim().length()==0) throw new InvalidParameterException("filename cannot be empty");

		for( TurtleLoader loader : loaders ) {
			if(isValidExtension(filename,loader.getFileNameFilter())) {
				FileInputStream in = new FileInputStream(filename);
				Turtle result = loader.load(in);
				in.close();
				return result;
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

	public static void save(Turtle turtle,String filename) throws Exception {
		if(filename == null || filename.trim().length()==0) throw new InvalidParameterException("filename cannot be empty");

		for (TurtleSaver saver : savers) {
			if(isValidExtension(filename,saver.getFileNameFilter())) {
				try (FileOutputStream out = new FileOutputStream(filename)) {
					saver.save(out, turtle);
				}
				return;
			}
		}
		String extension = filename.substring(filename.lastIndexOf("."));
		throw new Exception("TurtleFactory could not save '"+filename+"' : invalid file format '" + extension + "'");
	}
}
