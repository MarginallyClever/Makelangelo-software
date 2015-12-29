package com.marginallyclever.makelangelo;


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.kabeja.dxf.Bounds;
import org.kabeja.dxf.DXFConstants;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFEntity;
import org.kabeja.dxf.DXFLayer;
import org.kabeja.dxf.DXFLine;
import org.kabeja.dxf.DXFPolyline;
import org.kabeja.dxf.DXFSpline;
import org.kabeja.dxf.DXFVertex;
import org.kabeja.dxf.helpers.DXFSplineConverter;
import org.kabeja.dxf.helpers.Point;
import org.kabeja.parser.DXFParser;
import org.kabeja.parser.ParseException;
import org.kabeja.parser.Parser;
import org.kabeja.parser.ParserBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.marginallyclever.drawingtools.DrawingTool;
import com.marginallyclever.generators.Generator_Dragon;
import com.marginallyclever.generators.Generator_HilbertCurve;
import com.marginallyclever.generators.Generator_KochCurve;
import com.marginallyclever.generators.Generator_LSystemTree;
import com.marginallyclever.generators.Generator_Maze;
import com.marginallyclever.generators.Generator_YourMessageHere;
import com.marginallyclever.generators.ImageGenerator;
import com.marginallyclever.makelangelo.settings.MakelangeloSettingsDialog;


/**
 * Controls related to converting an image to gcode
 *
 * @author danroyer
 * @author Peter Colapietro
 * @since 7.1.4
 */
public class PanelPrepareImage
extends JScrollPane
implements ActionListener, ChangeListener {
	/**
	 *
	 */
	private static final long serialVersionUID = -4703402918904039337L;

	private static final Set<String> imageFileExtentions;

	static {
		imageFileExtentions = new HashSet<>();
		imageFileExtentions.add("jpg");
		imageFileExtentions.add("jpeg");
		imageFileExtentions.add("png");
		imageFileExtentions.add("wbmp");
		imageFileExtentions.add("bmp");
		imageFileExtentions.add("gif");
	}

	protected Translator translator;
	protected MakelangeloRobot robot;
	protected Makelangelo gui;

	protected String lastFileIn = "";
	protected String lastFileOut = "";

	private String[] machineConfigurations;
	private JComboBox<String> machineChoices;
	private JButton openConfig;
	private JSlider paperMargin;
	private JPanel machineNumberPanel;
	private JButton buttonOpenFile, buttonNewFile, buttonGenerate, buttonSaveFile;
	protected JButton buttonStart, buttonStartAt, buttonPause, buttonHalt;

	@SuppressWarnings("deprecation")
	private Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.LEGACY_MAKELANGELO_ROOT);

	// to make sure pen isn't on the paper while the machine is paused
	private boolean penIsUp;
	private boolean penIsUpBeforePause;

	/**
	 * @see org.slf4j.Logger
	 */
	private final Logger logger = LoggerFactory.getLogger(PanelPrepareImage.class);

	/**
	 * Image processing
	 */
	private List<ImageConverter> imageConverters;
	private List<ImageGenerator> imageGenerators;

	public void raisePen() {
		penIsUp = true;
	}

	public void lowerPen() {
		penIsUp = false;
	}

	// TODO use a ServiceLoader and find plugins in nearby folders
	protected void loadImageConverters() {
		imageConverters = new ArrayList<ImageConverter>();
		imageConverters.add(new Converter_ZigZag(gui, robot.settings, translator));
		imageConverters.add(new Converter_Spiral(gui, robot.settings, translator));
		imageConverters.add(new Converter_Crosshatch(gui, robot.settings, translator));
		imageConverters.add(new Converter_Scanline(gui, robot.settings, translator));
		imageConverters.add(new Converter_Pulse(gui, robot.settings, translator));
		imageConverters.add(new Converter_Boxes(gui, robot.settings, translator));
		//imageConverters.add(new Converter_ColorBoxes(gui, robot.settings, translator));
		imageConverters.add(new Converter_VoronoiStippling(gui, robot.settings, translator));
		imageConverters.add(new Converter_VoronoiZigZag(gui,robot.settings,translator));
		imageConverters.add(new Converter_Sandy(gui,robot.settings,translator));
		//imageConverters.add(new Filter_GeneratorColorFloodFill(gui, robot.settings, translator));  // not ready for public consumption
	}

	// TODO use a ServiceLoader and find plugins in nearby folders
	protected void loadImageGenerators() {
		imageGenerators = new ArrayList<ImageGenerator>();
		imageGenerators.add(new Generator_Maze(gui,robot.settings,translator));
		imageGenerators.add(new Generator_YourMessageHere(gui,robot.settings,translator));
		imageGenerators.add(new Generator_HilbertCurve(gui,robot.settings,translator));
		imageGenerators.add(new Generator_LSystemTree(gui,robot.settings,translator));
		imageGenerators.add(new Generator_KochCurve(gui,robot.settings,translator));
		imageGenerators.add(new Generator_Dragon(gui,robot.settings,translator));
	}

	/**
	 * @return
	 */
	private String[] getAnyMachineConfigurations() {
		String[] machineNames = robot.settings.getKnownMachineNames();
		if (machineNames.length == 0) {
			machineNames = robot.settings.getAvailableConfigurations();
		}
		return machineNames;
	}


	public void createPanel(Makelangelo _gui, Translator translator, MakelangeloRobot robot) {
		this.translator = translator;
		gui = _gui;
		this.robot = robot;

		this.setBorder(BorderFactory.createEmptyBorder());

		JPanel panel = new JPanel(new GridBagLayout());  
		this.setViewportView(panel);
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx=0;
		con1.gridy=0;
		con1.weightx=1;
		con1.weighty=0;
		con1.fill=GridBagConstraints.HORIZONTAL;
		con1.anchor=GridBagConstraints.NORTHWEST;


		machineNumberPanel = new JPanel(new GridLayout(1,0));
		updateMachineNumberPanel();
		panel.add(machineNumberPanel,con1);
		con1.gridy++;

		JPanel marginPanel = new JPanel(new GridLayout(1,0));
		paperMargin = new JSlider(JSlider.HORIZONTAL, 0, 50, 100 - (int) (robot.settings.paperMargin * 100));
		paperMargin.setMajorTickSpacing(10);
		paperMargin.setMinorTickSpacing(5);
		paperMargin.setPaintTicks(false);
		paperMargin.setPaintLabels(true);
		paperMargin.addChangeListener(this);
		marginPanel.add(new JLabel(translator.get("PaperMargin")));
		marginPanel.add(paperMargin);
		panel.add(marginPanel,con1);
		con1.gridy++;

		panel.add(new JSeparator(),con1);
		con1.gridy++;

		// File conversion
		buttonNewFile = new JButton(translator.get("MenuNewFile"));
		buttonNewFile.addActionListener(this);
		panel.add(buttonNewFile,con1);
		con1.gridy++;

		buttonOpenFile = new JButton(translator.get("MenuOpenFile"));
		buttonOpenFile.addActionListener(this);
		panel.add(buttonOpenFile,con1);
		con1.gridy++;

		buttonGenerate = new JButton(translator.get("MenuGenerate"));
		buttonGenerate.addActionListener(this);
		panel.add(buttonGenerate,con1);
		con1.gridy++;

		buttonSaveFile = new JButton(translator.get("MenuSaveGCODEAs"));
		buttonSaveFile.addActionListener(this);
		panel.add(buttonSaveFile,con1);
		con1.gridy++;

		panel.add(new JSeparator(),con1);
		con1.gridy++;

		// drive menu
		JPanel drivePanel = new JPanel(new GridLayout(2,2));
		buttonStart = new JButton(translator.get("Start"));
		buttonStartAt = new JButton(translator.get("StartAtLine"));
		buttonPause = new JButton(translator.get("Pause"));
		buttonHalt = new JButton(translator.get("Halt"));
		drivePanel.add(buttonStart);
		drivePanel.add(buttonStartAt);
		drivePanel.add(buttonPause);
		drivePanel.add(buttonHalt);
		buttonStart.addActionListener(this);
		buttonStartAt.addActionListener(this);
		buttonPause.addActionListener(this);
		buttonHalt.addActionListener(this);
		panel.add(drivePanel,con1);
		con1.gridy++;

		con1.weighty=1;
		panel.add(new JLabel(),con1);
	}

	public void updateMachineNumberPanel() {
		machineNumberPanel.removeAll();
		machineConfigurations = getAnyMachineConfigurations();
		machineChoices = new JComboBox<>(machineConfigurations);
		try {
			int index = robot.settings.getCurrentMachineIndex();
			machineChoices.setSelectedIndex(index);
		} catch (IllegalArgumentException e) {
			// TODO FIXME Do RCA and patch this at the source so that an illegal argument never occurs at this state.
			logger.info("This only happens for the times Makelangelo GUI runs and there is no known machine configuration. {}", e.getMessage());
		}

		openConfig = new JButton(translator.get("configureMachine"));
		openConfig.addActionListener(this);
		openConfig.setPreferredSize(openConfig.getPreferredSize());

		machineNumberPanel.add(new JLabel(translator.get("MachineNumber")));
		machineNumberPanel.add(machineChoices);
		machineNumberPanel.add(openConfig);
	}

	public void stateChanged(ChangeEvent e) {
		e.getSource();
		double pm = (100 - paperMargin.getValue()) * 0.01;
		if ( Double.compare(robot.settings.paperMargin , pm) != 0) {
			robot.settings.paperMargin = pm;
			robot.settings.saveConfig();
		}
	}


	// The user has done something.  respond to it.
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();

		int selectedIndex = machineChoices.getSelectedIndex();
		long newUID = Long.parseLong(machineChoices.getItemAt(selectedIndex));
		robot.settings.loadConfig(newUID);

		if( subject == openConfig ) {
			MakelangeloSettingsDialog m = new MakelangeloSettingsDialog(gui, translator, robot);
			m.run();
			return;
		}

		if (subject == buttonNewFile ) {
			newFile();
			return;
		}
		if (subject == buttonOpenFile) {
			openFileDialog();
			return;
		}
		if(subject == buttonGenerate) {
			generateImage();
			return;
		}
		if (subject == buttonSaveFile) {
			saveFileDialog();
			return;
		}


		if (gui.gCode.isLoaded() ) {
			if(!robot.isRunning()) {
				if (subject == buttonStart) {
					gui.startAt(0);
					return;
				}
				if (subject == buttonStartAt) {
					Long lineNumber = getStartingLineNumber();
					if (lineNumber != -1) {
						gui.startAt(lineNumber);
					}
					return;
				}
			} else {
				if (subject == buttonPause) {
					if (robot.isPaused() == true) {
						if (!penIsUpBeforePause) {
							gui.lowerPen();
						}
						buttonPause.setText(translator.get("Pause"));
						robot.unPause();
						// TODO: if the robot is not ready to unpause, this might fail and the program would appear to hang until a dis- and re-connect.
						gui.sendFileCommand();
					} else {
						penIsUpBeforePause = penIsUp;
						gui.raisePen();
						buttonPause.setText(translator.get("Unpause"));
						robot.pause();
					}
					return;
				}
				if (subject == buttonHalt) {
					gui.halt();
					return;
				}
			}
		}
	}


	/**
	 * open a dialog to ask for the line number.
	 *
	 * @return <code>lineNumber</code> greater than or equal to zero if user hit ok.
	 */
	private long getStartingLineNumber() {
		final JPanel panel = new JPanel(new GridBagLayout());
		final JTextField starting_line = new JTextField("0", 8);
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy = 0;
		panel.add(new JLabel(translator.get("StartAtLine")), c);
		c.gridwidth = 2;
		c.gridx = 2;
		c.gridy = 0;
		panel.add(starting_line, c);

		int result = JOptionPane.showConfirmDialog(null, panel, translator.get("StartAt"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			long lineNumber;
			try {
				lineNumber = Long.decode(starting_line.getText());
			} catch (Exception e) {
				lineNumber = -1;
			}

			return lineNumber;
		}
		return -1;
	}

	void updateButtonAccess(boolean isConnected,boolean isRunning) {
		if (buttonGenerate != null) buttonGenerate.setEnabled(!isRunning);

		openConfig.setEnabled(!isRunning);

		buttonStart.setEnabled(isConnected && !isRunning);
		buttonStartAt.setEnabled(isConnected && !isRunning);
		buttonPause.setEnabled(isConnected && isRunning);
		buttonHalt.setEnabled(isConnected && isRunning);

		if(!isConnected) {
			buttonPause.setText(translator.get("Pause"));
		}
	}

	public void newFile() {
		gui.gCode.reset();
		gui.updateMenuBar();
		gui.updateMachineConfig();
	}

	// creates a file open dialog. If you don't cancel it opens that file.
	public void openFileDialog() {
		// Note: source for ExampleFileFilter can be found in FileChooserDemo,
		// under the demo/jfc directory in the Java 2 SDK, Standard Edition.

		String filename = lastFileIn;

		FileFilter filterGCODE = new FileNameExtensionFilter(translator.get("FileTypeGCode"), "ngc");
		final FileFilter filterImage = new FileNameExtensionFilter(translator.get("FileTypeImage"), imageFileExtentions.toArray(new String[imageFileExtentions.size()]));
		FileFilter filterDXF = new FileNameExtensionFilter(translator.get("FileTypeDXF"), "dxf");

		JFileChooser fc = new JFileChooser(new File(filename));
		fc.addChoosableFileFilter(filterImage);
		fc.addChoosableFileFilter(filterDXF);
		fc.addChoosableFileFilter(filterGCODE);
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String selectedFile = fc.getSelectedFile().getAbsolutePath();

			// if machine is not yet calibrated
			if (robot.settings.isPaperConfigured() == false) {
				JOptionPane.showMessageDialog(null, translator.get("SetPaperSize"));
				return;
			}
			openFileOnDemand(selectedFile);
		}
	}

	public void generateImage() {
		final JPanel panel = new JPanel(new GridBagLayout());

		loadImageGenerators();
		String[] imageGeneratorNames = new String[imageGenerators.size()];
		Iterator<ImageGenerator> ici = imageGenerators.iterator();
		int i = 0;
		while (ici.hasNext()) {
			ImageManipulator f = ici.next();
			imageGeneratorNames[i++] = f.getName();
		}

		final JComboBox<String> options = new JComboBox<String>(imageGeneratorNames);

		GridBagConstraints c = new GridBagConstraints();

		int y = 0;
		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = y;
		panel.add(new JLabel(translator.get("ConversionStyle")), c);
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 3;
		c.gridx = 1;
		c.gridy = y++;
		panel.add(options, c);

		int result = JOptionPane.showConfirmDialog(null, panel, translator.get("ConversionOptions"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			int choice = options.getSelectedIndex();

			ImageGenerator chosenGenerator = imageGenerators.get(choice);
			robot.settings.saveConfig();

			String destinationFile = gui.getTempDestinationFile();
			gui.getDrawPanel().setDecorator(chosenGenerator);
			chosenGenerator.generate(destinationFile);
			gui.getDrawPanel().setDecorator(null);

			loadGCode(destinationFile);
			gui.playConversionFinishedSound();

			// Force update of graphics layout.
			gui.updateMachineConfig();
		}
	}

	public void saveFileDialog() {
		// Note: source for ExampleFileFilter can be found in FileChooserDemo,
		// under the demo/jfc directory in the Java 2 SDK, Standard Edition.
		String filename = lastFileOut;

		FileFilter filterGCODE = new FileNameExtensionFilter(translator.get("FileTypeGCode"), "ngc");

		JFileChooser fc = new JFileChooser(new File(filename));
		fc.addChoosableFileFilter(filterGCODE);
		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			String selectedFile = fc.getSelectedFile().getAbsolutePath();

			if (!selectedFile.toLowerCase().endsWith(".ngc")) {
				selectedFile += ".ngc";
			}

			try {
				gui.gCode.save(selectedFile);
			} catch (IOException e) {
				gui.log("<span style='color:red'>" + translator.get("Failed") + e.getMessage() + "</span>\n");
				return;
			}
		}
	}

	public boolean isFileGcode(String filename) {
		String ext = filename.substring(filename.lastIndexOf('.'));
		return (ext.equalsIgnoreCase(".ngc") || ext.equalsIgnoreCase(".gc"));
	}

	public boolean isFileDXF(String filename) {
		String ext = filename.substring(filename.lastIndexOf('.'));
		return (ext.equalsIgnoreCase(".dxf"));
	}

	public boolean isFileImage(String filename) {
		String ext = filename.substring(filename.lastIndexOf('.') + 1);
		return imageFileExtentions.contains(ext);
	}

	// User has asked that a file be opened.
	public void openFileOnDemand(String filename) {
		gui.log("<font color='green'>" + translator.get("OpeningFile") + filename + "...</font>\n");
		boolean success = false;

		if (isFileGcode(filename)) {
			success = loadGCode(filename);
		} else if (isFileDXF(filename)) {
			success = loadDXF(filename);
		} else if (isFileImage(filename)) {
			success = loadImage(filename);
		} else {
			gui.log("<font color='red'>" + translator.get("UnknownFileType") + "</font>\n");
		}

		if (success == true) {
			lastFileIn = filename;
			gui.updateMenuBar();
		}

		gui.statusBar.clear();
	}


	protected boolean chooseImageConversionOptions() {
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
		panel.add(new JLabel(translator.get("ConversionStyle")), c);
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 3;
		c.gridx = 1;
		c.gridy = y++;
		panel.add(inputDrawStyle, c);

		int result = JOptionPane.showConfirmDialog(null, panel, translator.get("ConversionOptions"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			setPreferredDrawStyle(inputDrawStyle.getSelectedIndex());
			robot.settings.saveConfig();

			// Force update of graphics layout.
			gui.updateMachineConfig();

			return true;
		}

		return false;
	}

	/**
	 * Opens a file.  If the file can be opened, get a drawing time estimate, update recent files list, and repaint the preview tab.
	 *
	 * @param filename what file to open
	 */
	public boolean loadGCode(String filename) {
		try {
			gui.gCode.load(filename);
			gui.log("<font color='green'>" + gui.gCode.estimateCount + translator.get("LineSegments")
			+ "\n" + gui.gCode.estimatedLength + translator.get("Centimeters") + "\n"
			+ translator.get("EstimatedTime") + gui.statusBar.formatTime((long) (gui.gCode.estimatedTime)) + "s.</font>\n");
		} catch (IOException e) {
			gui.log("<span style='color:red'>" + translator.get("FileNotOpened") + e.getLocalizedMessage() + "</span>\n");
			gui.updateMenuBar();
			return false;
		}

		gui.gCode.changed = true;
		gui.halt();
		return true;
	}


	protected boolean loadDXF(String filename) {
		// where to save temp output file?
		final String destinationFile = gui.getTempDestinationFile();
		final String srcFile = filename;

		final ProgressMonitor pm = new ProgressMonitor(null, translator.get("Converting"), "", 0, 100);
		pm.setProgress(0);
		pm.setMillisToPopup(0);

		final SwingWorker<Void, Void> s = new SwingWorker<Void, Void>() {
			public boolean ok = false;

			@SuppressWarnings("unchecked")
			@Override
			public Void doInBackground() {
				gui.log("<font color='green'>" + translator.get("Converting") + " " + destinationFile + "</font>\n");

				Parser parser = ParserBuilder.createDefaultParser();

				double dxf_x2 = 0;
				double dxf_y2 = 0;

				try (
						FileOutputStream fileOutputStream = new FileOutputStream(destinationFile);
						Writer out = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)
						) {
					DrawingTool tool = robot.settings.getCurrentTool();
					out.write(robot.settings.getConfigLine() + ";\n");
					out.write(robot.settings.getBobbinLine() + ";\n");
					out.write("G00 G90;\n");
					tool.writeChangeTo(out);
					tool.writeOff(out);

					parser.parse(srcFile, DXFParser.DEFAULT_ENCODING);
					DXFDocument doc = parser.getDocument();
					Bounds b = doc.getBounds();
					double imageCenterX = (b.getMaximumX() + b.getMinimumX()) / 2.0f;
					double imageCenterY = (b.getMaximumY() + b.getMinimumY()) / 2.0f;

					// find the scale to fit the image on the paper without altering the aspect ratio
					double imageWidth = (b.getMaximumX() - b.getMinimumX());
					double imageHeight = (b.getMaximumY() - b.getMinimumY());
					double paperHeight = robot.settings.getPaperHeight()*10 * robot.settings.paperMargin;
					double paperWidth = robot.settings.getPaperWidth() *10* robot.settings.paperMargin;
					//double scale = Math.min( scaleX/imageWidth, scaleY/imageHeight);
					//double scale = (scaleX / imageWidth);
					//if (imageHeight * scale > scaleX) scale = scaleY / imageHeight;

					double innerAspectRatio = imageWidth / imageHeight;
					double outerAspectRatio = paperWidth / paperHeight;
					double scale = (innerAspectRatio >= outerAspectRatio) ? (paperWidth / imageWidth) : (paperHeight / imageHeight);

					scale *= (robot.settings.reverseForGlass ? -1 : 1);
					//double scaleX = imageWidth * scale;
					//double scaleY = imageHeight * scale;

					// count all entities in all layers
					Iterator<DXFLayer> layer_iter = (Iterator<DXFLayer>) doc.getDXFLayerIterator();
					int entity_total = 0;
					int entity_count = 0;
					while (layer_iter.hasNext()) {
						DXFLayer layer = (DXFLayer) layer_iter.next();
						gui.log("<font color='yellow'>Found layer " + layer.getName() + "</font>\n");
						Iterator<String> entity_iter = (Iterator<String>) layer.getDXFEntityTypeIterator();
						while (entity_iter.hasNext()) {
							String entity_type = (String) entity_iter.next();
							List<DXFEntity> entity_list = (List<DXFEntity>) layer.getDXFEntities(entity_type);
							gui.log("<font color='yellow'>+ Found " + entity_list.size() + " of type " + entity_type + "</font>\n");
							entity_total += entity_list.size();
						}
					}
					// set the progress meter
					pm.setMinimum(0);
					pm.setMaximum(entity_total);

					// convert each entity
					layer_iter = doc.getDXFLayerIterator();
					while (layer_iter.hasNext()) {
						DXFLayer layer = (DXFLayer) layer_iter.next();

						Iterator<String> entity_type_iter = (Iterator<String>) layer.getDXFEntityTypeIterator();
						while (entity_type_iter.hasNext()) {
							String entity_type = (String) entity_type_iter.next();
							List<DXFEntity> entity_list = layer.getDXFEntities(entity_type);

							if (entity_type.equals(DXFConstants.ENTITY_TYPE_LINE)) {
								Iterator<DXFEntity> iter = entity_list.iterator();
								while (iter.hasNext()) {
									pm.setProgress(entity_count++);
									DXFLine entity = (DXFLine) iter.next();
									Point start = entity.getStartPoint();
									Point end = entity.getEndPoint();

									double x = (start.getX() - imageCenterX) * scale;
									double y = (start.getY() - imageCenterY) * scale;
									double x2 = (end.getX() - imageCenterX) * scale;
									double y2 = (end.getY() - imageCenterY) * scale;
									double dx, dy;
									//*
									// is it worth drawing this line?
											dx = x2 - x;
											dy = y2 - y;
											if (dx * dx + dy * dy < tool.getDiameter() / 2.0) {
												continue;
											}
											//*/
											dx = dxf_x2 - x;
											dy = dxf_y2 - y;

											if (dx * dx + dy * dy > tool.getDiameter() / 2.0) {
												if (tool.isDrawOn()) {
													tool.writeOff(out);
												}
												tool.writeMoveTo(out, (float) x, (float) y);
											}
											if (tool.isDrawOff()) {
												tool.writeOn(out);
											}
											tool.writeMoveTo(out, (float) x2, (float) y2);
											dxf_x2 = x2;
											dxf_y2 = y2;
								}
							} else if (entity_type.equals(DXFConstants.ENTITY_TYPE_SPLINE)) {
								Iterator<DXFEntity> iter = entity_list.iterator();
								while (iter.hasNext()) {
									pm.setProgress(entity_count++);
									DXFSpline entity = (DXFSpline) iter.next();
									entity.setLineWeight(30);
									DXFPolyline polyLine = DXFSplineConverter.toDXFPolyline(entity);
									boolean first = true;
									for (int j = 0; j < polyLine.getVertexCount(); ++j) {
										DXFVertex v = polyLine.getVertex(j);
										double x = (v.getX() - imageCenterX) * scale;
										double y = (v.getY() - imageCenterY) * scale;
										double dx = dxf_x2 - x;
										double dy = dxf_y2 - y;

										if (first == true) {
											first = false;
											if (dx * dx + dy * dy > tool.getDiameter() / 2.0) {
												// line does not start at last tool location, lift and move.
												if (tool.isDrawOn()) {
													tool.writeOff(out);
												}
												tool.writeMoveTo(out, (float) x, (float) y);
											}
											// else line starts right here, do nothing.
										} else {
											// not the first point, draw.
											if (tool.isDrawOff()) tool.writeOn(out);
											if (j < polyLine.getVertexCount() - 1 && dx * dx + dy * dy < tool.getDiameter() / 2.0)
												continue;  // less than 1mm movement?  Skip it.
											tool.writeMoveTo(out, (float) x, (float) y);
										}
										dxf_x2 = x;
										dxf_y2 = y;
									}
								}
							} else if (entity_type.equals(DXFConstants.ENTITY_TYPE_POLYLINE)) {
								Iterator<DXFEntity> iter = entity_list.iterator();
								while (iter.hasNext()) {
									pm.setProgress(entity_count++);
									DXFPolyline entity = (DXFPolyline) iter.next();
									boolean first = true;
									for (int j = 0; j < entity.getVertexCount(); ++j) {
										DXFVertex v = entity.getVertex(j);
										double x = (v.getX() - imageCenterX) * scale;
										double y = (v.getY() - imageCenterY) * scale;
										double dx = dxf_x2 - x;
										double dy = dxf_y2 - y;

										if (first == true) {
											first = false;
											if (dx * dx + dy * dy > tool.getDiameter() / 2.0) {
												// line does not start at last tool location, lift and move.
												if (tool.isDrawOn()) {
													tool.writeOff(out);
												}
												tool.writeMoveTo(out, (float) x, (float) y);
											}
											// else line starts right here, do nothing.
										} else {
											// not the first point, draw.
											if (tool.isDrawOff()) tool.writeOn(out);
											if (j < entity.getVertexCount() - 1 && dx * dx + dy * dy < tool.getDiameter() / 2.0)
												continue;  // less than 1mm movement?  Skip it.
											tool.writeMoveTo(out, (float) x, (float) y);
										}
										dxf_x2 = x;
										dxf_y2 = y;
									}
								}
							}
						}
					}

					// entities finished.  Close up file.
					tool.writeOff(out);
					tool.writeMoveTo(out, 0, 0);

					ok = true;
				} catch (IOException | ParseException e) {
					e.printStackTrace();
				}

				pm.setProgress(100);
				return null;
			}

			@Override
			public void done() {
				pm.close();
				gui.log("<font color='green'>" + translator.get("Finished") + "</font>\n");
				gui.playConversionFinishedSound();
				if (ok) {
					loadGCode(destinationFile);
				}
				gui.halt();
			}
		};

		s.addPropertyChangeListener(new PropertyChangeListener() {
			// Invoked when task's progress property changes.
			public void propertyChange(PropertyChangeEvent evt) {
				if (Objects.equals("progress", evt.getPropertyName())) {
					int progress = (Integer) evt.getNewValue();
					pm.setProgress(progress);
					String message = String.format("%d%%\n", progress);
					pm.setNote(message);
					if (s.isDone()) {
						gui.log("<font color='green'>" + translator.get("Finished") + "</font>\n");
					} else if (s.isCancelled() || pm.isCanceled()) {
						if (pm.isCanceled()) {
							s.cancel(true);
						}
						gui.log("<font color='green'>" + translator.get("Cancelled") + "</font>\n");
					}
				}
			}
		});

		s.execute();

		return true;
	}


	public boolean loadImage(String filename) {
		// where to save temp output file?
		final String sourceFile = filename;
		final String destinationFile = gui.getTempDestinationFile();

		loadImageConverters();
		if (chooseImageConversionOptions() == false) return false;

		final ProgressMonitor pm = new ProgressMonitor(null, translator.get("Converting"), "", 0, 100);
		pm.setProgress(0);
		pm.setMillisToPopup(0);

		final SwingWorker<Void, Void> s = new SwingWorker<Void, Void>() {
			@Override
			public Void doInBackground() {
				try (OutputStream fileOutputStream = new FileOutputStream(destinationFile);
						Writer out = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)){
					// read in image
					gui.log("<font color='green'>" + translator.get("Converting") + " " + destinationFile + "</font>\n");
					// convert with style
					final BufferedImage img = ImageIO.read(new File(sourceFile));

					ImageConverter converter = imageConverters.get(getPreferredDrawStyle());
					converter.setParent(this);
					converter.setProgressMonitor(pm);

					gui.getDrawPanel().setDecorator(converter);
					converter.convert(img, out);
					gui.getDrawPanel().setDecorator(null);

					if(robot.settings.shouldSignName()) {
						// Sign name
						Generator_YourMessageHere ymh = new Generator_YourMessageHere(gui, robot.settings, translator);
						ymh.signName(out);
					}
					gui.updateMachineConfig();
				} catch (IOException e) {
					gui.log("<font color='red'>" + translator.get("Failed") + e.getLocalizedMessage() + "</font>\n");
					gui.updateMenuBar();
				}

				// out closed when scope of try() ended.

				pm.setProgress(100);
				return null;
			}

			@Override
			public void done() {
				pm.close();
				gui.log("<font color='green'>" + translator.get("Finished") + "</font>\n");
				loadGCode(destinationFile);
				gui.playConversionFinishedSound();
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
						gui.log("<font color='green'>" + translator.get("Finished") + "</font>\n");
					} else if (s.isCancelled() || pm.isCanceled()) {
						if (pm.isCanceled()) {
							s.cancel(true);
						}
						gui.log("<font color='green'>" + translator.get("Cancelled") + "</font>\n");
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
