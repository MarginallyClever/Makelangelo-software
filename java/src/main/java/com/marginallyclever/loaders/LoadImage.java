package com.marginallyclever.loaders;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.basictypes.ImageManipulator;
import com.marginallyclever.converters.Converter_Boxes;
import com.marginallyclever.converters.Converter_Crosshatch;
import com.marginallyclever.converters.Converter_Pulse;
import com.marginallyclever.converters.Converter_Sandy;
import com.marginallyclever.converters.Converter_Scanline;
import com.marginallyclever.converters.Converter_Spiral;
import com.marginallyclever.converters.Converter_VoronoiStippling;
import com.marginallyclever.converters.Converter_VoronoiZigZag;
import com.marginallyclever.converters.Converter_ZigZag;
import com.marginallyclever.converters.ImageConverter;
import com.marginallyclever.generators.Generator_YourMessageHere;
import com.marginallyclever.makelangelo.Log;
import com.marginallyclever.makelangelo.Makelangelo;
import com.marginallyclever.makelangelo.MakelangeloRobot;
import com.marginallyclever.makelangelo.PreferencesHelper;
import com.marginallyclever.makelangelo.Translator;

public class LoadImage implements LoadFileType {
	
	@SuppressWarnings("deprecation")
	private Preferences prefs = PreferencesHelper
			.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.LEGACY_MAKELANGELO_ROOT);

	
	private List<ImageConverter> imageConverters;
	
	/**
	 * Set of image file extensions.
	 */
	private static final Set<String> IMAGE_FILE_EXTENSIONS;

	static {
		IMAGE_FILE_EXTENSIONS = new HashSet<>();
		IMAGE_FILE_EXTENSIONS.add("jpg");
		IMAGE_FILE_EXTENSIONS.add("jpeg");
		IMAGE_FILE_EXTENSIONS.add("png");
		IMAGE_FILE_EXTENSIONS.add("wbmp");
		IMAGE_FILE_EXTENSIONS.add("bmp");
		IMAGE_FILE_EXTENSIONS.add("gif");
	}

	@Override
	public FileNameExtensionFilter getFileNameFilter() {
		return new FileNameExtensionFilter(Translator.get("FileTypeImage"),
				IMAGE_FILE_EXTENSIONS.toArray(new String[IMAGE_FILE_EXTENSIONS.size()]));
	}

	@Override
	public boolean canLoad(String filename) {
		final String filenameExtension = filename.substring(filename.lastIndexOf('.') + 1);
		return IMAGE_FILE_EXTENSIONS.contains(filenameExtension.toLowerCase());
	}


	// TODO see https://github.com/MarginallyClever/Makelangelo/issues/139
	protected void loadImageConverters() {
		imageConverters = new ArrayList<ImageConverter>();
		imageConverters.add(new Converter_Boxes());
		// imageConverters.add(new Converter_ColorBoxes());
		imageConverters.add(new Converter_Crosshatch());
		// imageConverters.add(new Filter_GeneratorColorFloodFill()); // not ready for public consumption
		imageConverters.add(new Converter_Pulse());
		imageConverters.add(new Converter_Sandy());
		imageConverters.add(new Converter_Scanline());
		imageConverters.add(new Converter_Spiral());
		imageConverters.add(new Converter_VoronoiStippling());
		imageConverters.add(new Converter_VoronoiZigZag());
		imageConverters.add(new Converter_ZigZag());
	}

	protected boolean chooseImageConversionOptions(MakelangeloRobot robot,Makelangelo gui) {
		final JPanel panel = new JPanel(new GridBagLayout());

		String[] imageConverterNames = new String[imageConverters.size()];
		Iterator<ImageConverter> ici = imageConverters.iterator();
		int i = 0;
		while (ici.hasNext()) {
			ImageManipulator f = ici.next();
			imageConverterNames[i++] = f.getName();
		}

		final JComboBox<String> inputDrawStyle = new JComboBox<String>(imageConverterNames);
		inputDrawStyle.setSelectedIndex(getPreferredDrawStyle());

		GridBagConstraints c = new GridBagConstraints();

		int y = 0;
		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = y;
		panel.add(new JLabel(Translator.get("ConversionStyle")), c);
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 3;
		c.gridx = 1;
		c.gridy = y++;
		panel.add(inputDrawStyle, c);

		int result = JOptionPane.showConfirmDialog(null, panel, Translator.get("ConversionOptions"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			setPreferredDrawStyle(inputDrawStyle.getSelectedIndex());
			robot.settings.saveConfig();

			// Force update of graphics layout.
			gui.updateMachineConfig();

			return true;
		}

		return false;
	}
	



	public boolean load(String filename,MakelangeloRobot robot,Makelangelo gui) {
		// where to save temp output file?
		final String sourceFile = filename;
		final String destinationFile = gui.getTempDestinationFile();

		loadImageConverters();
		if (chooseImageConversionOptions(robot,gui) == false)
			return false;

		final ProgressMonitor pm = new ProgressMonitor(null, Translator.get("Converting"), "", 0, 100);
		pm.setProgress(0);
		pm.setMillisToPopup(0);

		final SwingWorker<Void, Void> s = new SwingWorker<Void, Void>() {
			@Override
			public Void doInBackground() {
				try (OutputStream fileOutputStream = new FileOutputStream(destinationFile);
						Writer out = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)) {
					// read in image
					Log.message(Translator.get("Converting") + " " + destinationFile);
					// convert with style
					final BufferedImage img = ImageIO.read(new File(sourceFile));

					ImageConverter converter = imageConverters.get(getPreferredDrawStyle());
					converter.setParent(this);
					converter.setProgressMonitor(pm);

					converter.setDrawPanel(gui.getDrawPanel());
					converter.setMachine(robot);
					gui.getDrawPanel().setDecorator(converter);
					converter.convert(img, out);
					converter.setDrawPanel(null);
					gui.getDrawPanel().setDecorator(null);

					if (robot.settings.shouldSignName()) {
						// Sign name
						Generator_YourMessageHere ymh = new Generator_YourMessageHere();
						ymh.setMachine(robot);
						ymh.signName(out);
					}
					gui.updateMachineConfig();
				} catch (IOException e) {
					Log.error(Translator.get("Failed") + e.getLocalizedMessage());
					gui.updateMenuBar();
				}

				// out closed when scope of try() ended.

				pm.setProgress(100);
				return null;
			}

			@Override
			public void done() {
				pm.close();
				Log.message(Translator.get("Finished"));
				LoadGCode loader = new LoadGCode();
				loader.load(destinationFile, robot, gui);
				gui.soundSystem.playConversionFinishedSound();
			}
		};

		s.addPropertyChangeListener(new PropertyChangeListener() {
			// Invoked when task's progress property changes.
			public void propertyChange(PropertyChangeEvent evt) {
				if (Objects.equals("progress", evt.getPropertyName())) {
					int progress = (Integer) evt.getNewValue();
					pm.setProgress(progress);
					String message = String.format("%d%%.\n", progress);
					pm.setNote(message);
					if (s.isDone()) {
						Log.message(Translator.get("Finished"));
					} else if (s.isCancelled() || pm.isCanceled()) {
						if (pm.isCanceled()) {
							s.cancel(true);
						}
						Log.message(Translator.get("Cancelled"));
					}
				}
			}
		});

		s.execute();

		return true;
	}
	
	private void setPreferredDrawStyle(int style) {
		prefs.putInt("Draw Style", style);
	}

	private int getPreferredDrawStyle() {
		return prefs.getInt("Draw Style", 0);
	}
}
