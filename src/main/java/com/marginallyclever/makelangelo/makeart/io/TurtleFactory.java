package com.marginallyclever.makelangelo.makeart.io;

import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Factory for creating {@link Turtle} objects from vector files and writing them to vector files.
 * @author Dan Royer
 */
public class TurtleFactory {
	private static final TurtleLoader [] loaders = {
			new LoadAudio(),
			new LoadDXF(),
			new LoadFactorioMap(),
			new LoadGCode(),
			new LoadOSM(),
			new LoadScratch2(),
			new LoadScratch3(),
			new LoadSVG(),
			new LoadTHR()
	};
	
	private static final TurtleSaver [] savers = {
			new SaveBitmap("bmp",false),
			new SaveBitmap("gif",false),
			new SaveBitmap("jpg",false),
			new SaveBitmap("pio",false),
			new SaveBitmap("png",true),
			new SaveBitmap("tif",false),
			new SaveBitmap("webp",true),
			new SaveDXF(),
			new SaveGCode(),
			new SaveSVG(),
			new SaveTHR(),
	};

	private static JFileChooser fileChooserLoad = null;
	private static JFileChooser fileChooserSave = null;

	public static JFileChooser getLoadFileChooser() {
		if(fileChooserLoad ==null) {
			fileChooserLoad = new JFileChooser();
			// add formats
			TurtleFactory.getLoadExtensions().forEach(fileChooserLoad::addChoosableFileFilter);
			// no wild card filter, please.
			fileChooserLoad.setAcceptAllFileFilterUsed(false);
		}
		return fileChooserLoad;
	}

	public static JFileChooser getSaveFileChooser() {
		if(fileChooserSave == null) {
			fileChooserSave = new JFileChooser();
			// add formats
			TurtleFactory.getSaveExtensions().forEach(fileChooserSave::addChoosableFileFilter);
			// no wild card filter, please.
			fileChooserSave.setAcceptAllFileFilterUsed(false);

			fileChooserSave.addActionListener(e-> {
				if (!JFileChooser.APPROVE_SELECTION.equals(e.getActionCommand())) return;

				FileNameExtensionFilter filter = (FileNameExtensionFilter) fileChooserSave.getFileFilter();
				String extension = filter.getExtensions()[0];
				String filename = fileChooserSave.getSelectedFile().getName();
				if (!FilenameUtils.getExtension(filename).equalsIgnoreCase(extension)) {
					fileChooserSave.setSelectedFile(new java.io.File(fileChooserSave.getSelectedFile() + "." + extension));
				}
			});
		}
		return fileChooserSave;
	}

	public static @Nonnull Turtle load(String filename) throws Exception {
		return loadInternal(filename, null);
	}

	public static @Nonnull Turtle load(String filename, PlotterSettings settings) throws Exception {
		return loadInternal(filename, Objects.requireNonNull(settings, "settings"), null);
	}

	public static @Nonnull Turtle load(String filename, PlotterSettings settings, Paper paper) throws Exception {
		return loadInternal(filename, Objects.requireNonNull(settings, "settings"), Objects.requireNonNull(paper, "paper"));
	}

	private static @Nonnull Turtle loadInternal(String filename, PlotterSettings settings) throws Exception {
		return loadInternal(filename, settings, null);
	}

	private static @Nonnull Turtle loadInternal(String filename, PlotterSettings settings, Paper paper) throws Exception {
		if(filename == null || filename.trim().isEmpty()) {
			throw new InvalidParameterException("filename cannot be empty");
		}

		for( TurtleLoader loader : loaders ) {
			if(isValidExtension(filename,loader.getFileNameFilter())) {
				try(FileInputStream in = new FileInputStream(filename)) {
					if (settings != null && loader instanceof LoadGCode loadGCode) {
						return loadGCode.load(in, settings, paper);
					}
					return loader.load(in);
				} catch(Exception e) {
					throw new Exception("could not load '" + filename + "'.", e);
				}
			}
		}
		throw new Exception("unrecognized format '" + filename + "'.");
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
		save(turtle, filename, settings, null);
	}

	public static void save(Turtle turtle, String filename, PlotterSettings settings, Paper paper) throws Exception {
		if(filename == null || filename.trim().isEmpty()) throw new InvalidParameterException("filename cannot be empty");

		if(filename.lastIndexOf(".")==-1) throw new InvalidParameterException("filename must have an extension");
		String extension = filename.substring(filename.lastIndexOf("."));
		if(extension.isEmpty()) throw new InvalidParameterException("filename must have an extension");

		for (TurtleSaver saver : savers) {
			if(isValidExtension(filename,saver.getFileNameFilter())) {
				try (FileOutputStream out = new FileOutputStream(filename)) {
					if (saver instanceof SaveGCode saveGCode) {
						saveGCode.save(out, turtle, settings, paper);
					} else {
						saver.save(out, turtle,settings);
					}
					return;
				}
			}
		}

		throw new Exception("TurtleFactory could not save '"+filename+"' : invalid file format '" + extension + "'");
	}
}
