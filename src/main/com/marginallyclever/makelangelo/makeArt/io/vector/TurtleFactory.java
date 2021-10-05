package com.marginallyclever.makelangelo.makeArt.io.vector;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.convenience.turtle.Turtle;

public class TurtleFactory {
	private static TurtleLoader [] loaders = {
		new LoadDXF(),
		new LoadGCode(),
		new LoadScratch2(),
		new LoadScratch3(),
		new LoadSVG()
	};
	
	private static TurtleSaver [] savers = {
		new SaveDXF(),
		new SaveSVG(),
		new SaveGCode()
	};
	
	public static Turtle load(String filename) throws Exception {
		if(filename == null || filename.trim().length()==0) return null;

		for( TurtleLoader loader : loaders ) {
			if(isValidExtension(filename,loader)) {			
				return loadTurtleWithLoader(filename,loader);
			}
		}
		throw new Exception("TurtleFactory could not load '"+filename+"'.");
	}
	
	private static boolean isValidExtension(String filename, TurtleLoader loader) {
		String [] extensions = loader.getFileNameFilter().getExtensions();
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
	
	private static Turtle loadTurtleWithLoader(String filename,TurtleLoader loader) throws Exception {
		InputStream fileInputStream = new FileInputStream(filename);
		return loader.load(fileInputStream);
	}

	public static void save(Turtle t,String filename) throws Exception {
		
	}
}
