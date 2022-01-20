package com.marginallyclever.makelangelo.makeArt.io.vector;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;

import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.makelangelo.turtle.Turtle;

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
		throw new Exception("TurtleFactory could not load '"+filename+"'.");
	}
	
	private static boolean isValidExtension(String filename, FileNameExtensionFilter filter) {
		String [] extensions = filter.getExtensions();
		for( String e : extensions ) {
			if(filename.endsWith(e)) return true;
		}
		return false;
	}

	public static ArrayList<FileNameExtensionFilter> getLoadExtensions() {
		ArrayList<FileNameExtensionFilter> filters = new ArrayList<FileNameExtensionFilter>();
		for( TurtleLoader loader : loaders ) {
			filters.add( loader.getFileNameFilter() );
		}
		return filters;
	}

	public static ArrayList<FileNameExtensionFilter> getSaveExtensions() {
		ArrayList<FileNameExtensionFilter> filters = new ArrayList<FileNameExtensionFilter>();
		for( TurtleSaver saver : savers ) {
			filters.add( saver.getFileNameFilter() );
		}
		return filters;
	}

	public static void save(Turtle turtle,String filename) throws Exception {
		if(filename == null || filename.trim().length()==0) throw new InvalidParameterException("filename cannot be empty");

		for( TurtleSaver saver : savers ) {
			if(isValidExtension(filename,saver.getFileNameFilter())) {
				FileOutputStream out = new FileOutputStream(filename); 
				saver.save(out,turtle);
				out.close();
				return;
			}
		}
		throw new Exception("TurtleFactory could not save '"+filename+"'.");
	}
}
