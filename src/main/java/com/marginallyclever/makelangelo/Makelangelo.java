package com.marginallyclever.makelangelo;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.donatello.FileHelper;
import com.marginallyclever.makelangelo.applicationsettings.LanguagePreferences;
import com.marginallyclever.nodegraphcore.DAO4JSONFactory;
import com.marginallyclever.nodegraphcore.NodeFactory;
import com.marginallyclever.nodegraphcore.ServiceLoaderHelper;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * <p>The Makelangelo app is a tool for programming CNC robots, typically plotters.  It converts lines (made of
 * segments made of points) into instructions in GCODE format, as described in <a
 * href="https://github.com/MarginallyClever/Makelangelo-firmware/wiki/gcode-description">the wiki</a>.</p>
 * <p>In order to do this the app also provides convenient methods to load vectors (like DXF or SVG), create vectors
 * ({@link com.marginallyclever.makelangelo.makeart.turtlegenerator.TurtleGenerator}s), or
 * interpret bitmaps (like BMP,JPEG,PNG,GIF,TGA,PIO) into vectors
 * ({@link com.marginallyclever.makelangelo.makeart.imageconverter.ImageConverter}s).</p>
 * <p>The app must also know some details about the machine, the surface onto which drawings will be made, and the
 * drawing tool making the mark on the paper.  This knowledge helps the app to create better gcode.</p>
 *
 * @author Dan Royer (dan@marginallyclever.com)
 * @since 1.00 2012/2/28
 */
public final class Makelangelo {
	private static final Logger logger = LoggerFactory.getLogger(Makelangelo.class);

	// GUI elements
	private MainFrame mainFrame;

	public static void main(String[] args) {
		CommandLineOptions.setFromMain(args);
		Log.start();

		FileHelper.createDirectoryIfMissing(FileHelper.getExtensionPath());
		ServiceLoaderHelper.addAllPathFiles(FileHelper.getExtensionPath());
		NodeFactory.loadRegistries();
		DAO4JSONFactory.loadRegistries();

		PreferencesHelper.start();
		Translator.start();

		if(Translator.isThisTheFirstTimeLoadingLanguageFiles()) {
			LanguagePreferences.chooseLanguage();
		}
		
		setLookAndFeel();
		javax.swing.SwingUtilities.invokeLater(()->{
			(new MainFrame()).setVisible(true);
		});
	}

	private static void setLookAndFeel() {
		if(CommandLineOptions.hasOption("-nolf")) return;

		logger.info("Setting look and feel...");
		FlatLaf.registerCustomDefaultsSource( Makelangelo.class.getPackageName() );
		try {
			UIManager.setLookAndFeel(new FlatLightLaf());
			// option 2: UIManager.setLookAndFeel(new FlatDarkLaf());
			// option 3: UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ex) {
			logger.warn("failed to set native look and feel.", ex);
		}
	}
}
