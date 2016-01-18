package com.marginallyclever.makelangelo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
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
import java.text.NumberFormat;
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
import javax.swing.JFormattedTextField;
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
 * @author dan royer
 * @author Peter Colapietro
 * @since 7.1.4
 */
public class MakelangeloRobotPanel extends JScrollPane implements ActionListener, ChangeListener, MouseListener, MouseMotionListener {
	/**
	 *
	 */
	private static final long serialVersionUID = -4703402918904039337L;

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

	// god objects ?
	protected Translator translator;
	protected MakelangeloRobot robot;
	protected Makelangelo gui;

	// machine options
	protected String lastFileIn = "";
	protected String lastFileOut = "";

	private String[] machineConfigurations;
	private JComboBox<String> machineChoices;
	private JButton openConfig;
	private JSlider paperMargin;
	private JPanel machineNumberPanel;
	private JButton buttonOpenFile, buttonNewFile, buttonGenerate, buttonSaveFile;
	protected JButton buttonStart, buttonStartAt, buttonPause, buttonHalt;

	// driving controls
	private JButton down100,down10,down1,up1,up10,up100;
	private JButton left100,left10,left1,right1,right10,right100;
	private JButton goHome,setHome;
	private JButton goTop,goBottom,goLeft,goRight,penUp,penDown;

	// speed
	private JFormattedTextField feedRate;
	private JButton setFeedRate;
	private JButton disengageMotors;

	// etch-a-sketch test
	private JLabel coordinates;
	private JPanel dragAndDrive;
	private boolean mouseInside,mouseOn;
	double last_x,last_y;

	// status bar
	public StatusBar statusBar;


	@SuppressWarnings("deprecation")
	private Preferences prefs = PreferencesHelper
			.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.LEGACY_MAKELANGELO_ROOT);

	/**
	 * Image processing
	 */
	private List<ImageConverter> imageConverters;
	private List<ImageGenerator> imageGenerators;

	// TODO see https://github.com/MarginallyClever/Makelangelo/issues/139
	protected void loadImageConverters() {
		imageConverters = new ArrayList<ImageConverter>();
		imageConverters.add(new Converter_Boxes(robot.settings));
		// imageConverters.add(new Converter_ColorBoxes(robot.settings,
		// translator));
		imageConverters.add(new Converter_Crosshatch(robot.settings));
		// imageConverters.add(new Filter_GeneratorColorFloodFill(gui,
		// robot.settings)); // not ready for public consumption
		imageConverters.add(new Converter_Pulse(robot.settings));
		imageConverters.add(new Converter_Sandy(robot.settings));
		imageConverters.add(new Converter_Scanline(robot.settings));
		imageConverters.add(new Converter_Spiral(robot.settings));
		imageConverters.add(new Converter_VoronoiStippling(robot.settings));
		imageConverters.add(new Converter_VoronoiZigZag(robot.settings));
		imageConverters.add(new Converter_ZigZag(robot.settings));
	}

	// TODO see https://github.com/MarginallyClever/Makelangelo/issues/139
	protected void loadImageGenerators() {
		imageGenerators = new ArrayList<ImageGenerator>();
		imageGenerators.add(new Generator_Dragon(robot.settings));
		imageGenerators.add(new Generator_HilbertCurve(robot.settings));
		imageGenerators.add(new Generator_KochCurve(robot.settings));
		imageGenerators.add(new Generator_LSystemTree(robot.settings));
		imageGenerators.add(new Generator_Maze(robot.settings));
		imageGenerators.add(new Generator_YourMessageHere(robot.settings));
	}


	public JButton tightJButton(String label) {
		JButton b = new JButton(label);
		b.setMargin(new Insets(0,0,0,0));
		b.setPreferredSize(new Dimension(60,20));
		return b;
	}

	public MakelangeloRobotPanel(Makelangelo gui, Translator translator, MakelangeloRobot robot) {
		this.translator = translator;
		this.gui = gui;
		this.robot = robot;

		this.setBorder(BorderFactory.createEmptyBorder());

		JPanel panel = new JPanel(new GridBagLayout());
		this.setViewportView(panel);

		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx = 0;
		con1.gridy = 0;
		con1.weightx = 1;
		con1.weighty = 0;
		con1.fill = GridBagConstraints.HORIZONTAL;
		con1.anchor = GridBagConstraints.NORTHWEST;



		// settings
		machineNumberPanel = new JPanel(new GridLayout(1, 0));
		updateMachineNumberPanel();
		panel.add(machineNumberPanel, con1);
		con1.gridy++;

		JPanel marginPanel = new JPanel(new GridLayout(1, 0));
		paperMargin = new JSlider(JSlider.HORIZONTAL, 0, 50, 100 - (int) (robot.settings.getPaperMargin() * 100));
		paperMargin.setMajorTickSpacing(10);
		paperMargin.setMinorTickSpacing(5);
		paperMargin.setPaintTicks(false);
		paperMargin.setPaintLabels(true);
		paperMargin.addChangeListener(this);
		marginPanel.add(new JLabel(Translator.get("PaperMargin")));
		marginPanel.add(paperMargin);
		panel.add(marginPanel, con1);
		con1.gridy++;

		panel.add(new JSeparator(), con1);
		con1.gridy++;



		// Driving controls
		mouseInside=false;
		mouseOn=false;
		last_x=last_y=0;

		GridBagConstraints c = new GridBagConstraints();

		JPanel axisControl = new JPanel(new GridBagLayout());
		down100 = tightJButton("-100");
		down10 = tightJButton("-10");
		down1 = tightJButton("-1");

		setHome = tightJButton(Translator.get("SetHome"));

		up1 = tightJButton("1");
		up10 = tightJButton("10");
		up100 = tightJButton("100");

		left100 = tightJButton("-100");
		left10 = tightJButton("-10");
		left1 = tightJButton("-1");
		right1 = tightJButton("1");
		right10 = tightJButton("10");
		right100 = tightJButton("100");

		c.fill=GridBagConstraints.BOTH;
		c.gridx=3;  c.gridy=6;  axisControl.add(down100,c);
		c.gridx=3;  c.gridy=5;  axisControl.add(down10,c);
		c.gridx=3;  c.gridy=4;  axisControl.add(down1,c);
		c.gridx=3;  c.gridy=3;  axisControl.add(setHome,c);     setHome.setPreferredSize(new Dimension(100,20));
		c.gridx=3;  c.gridy=2;  axisControl.add(up1,c);
		c.gridx=3;  c.gridy=1;  axisControl.add(up10,c);
		c.gridx=3;  c.gridy=0;  axisControl.add(up100,c);

		c.gridx=0;  c.gridy=3;  axisControl.add(left100,c);
		c.gridx=1;  c.gridy=3;  axisControl.add(left10,c);
		c.gridx=2;  c.gridy=3;  axisControl.add(left1,c);
		c.gridx=4;  c.gridy=3;  axisControl.add(right1,c);
		c.gridx=5;  c.gridy=3;  axisControl.add(right10,c);
		c.gridx=6;  c.gridy=3;  axisControl.add(right100,c);
		up1.addActionListener(this);
		up10.addActionListener(this);
		up100.addActionListener(this);
		down1.addActionListener(this);
		down10.addActionListener(this);
		down100.addActionListener(this);
		left1.addActionListener(this);
		left10.addActionListener(this);
		left100.addActionListener(this);
		right1.addActionListener(this);
		right10.addActionListener(this);
		right100.addActionListener(this);

		JPanel corners = new JPanel();
		corners.setLayout(new GridBagLayout());
		goTop = new JButton(Translator.get("Top"));       goTop.setPreferredSize(new Dimension(80,20));
		goBottom = new JButton(Translator.get("Bottom")); goBottom.setPreferredSize(new Dimension(80,20));
		goLeft = new JButton(Translator.get("Left"));     goLeft.setPreferredSize(new Dimension(80,20));
		goRight = new JButton(Translator.get("Right"));   goRight.setPreferredSize(new Dimension(80,20));
		penUp = new JButton(Translator.get("PenUp"));      penUp.setPreferredSize(new Dimension(100,20));
		penDown = new JButton(Translator.get("PenDown"));  penDown.setPreferredSize(new Dimension(100,20));
		//final JButton find = new JButton("FIND HOME");    find.setPreferredSize(new Dimension(100,20));
		//setHome = new JButton(translator.get("SetHome"));     setHome.setPreferredSize(new Dimension(100,20));
		goHome = new JButton(Translator.get("GoHome"));     goHome.setPreferredSize(new Dimension(100,20));
		JLabel horizontalFiller = new JLabel(" ");
		c.gridx=2;  c.gridy=0;  corners.add(goTop,c);
		c.gridx=2;  c.gridy=1;  corners.add(goHome,c);
		c.gridx=2;  c.gridy=2;  corners.add(goBottom,c);
		c.gridx=1;  c.gridy=1;  corners.add(goLeft,c);
		c.gridx=3;  c.gridy=1;  corners.add(goRight,c);
		c.weightx=1;
		c.gridx=4;  c.gridy=0;  corners.add(horizontalFiller,c);
		c.weightx=0;
		c.gridx=5;  c.gridy=0;  corners.add(penUp,c);
		c.gridx=5;  c.gridy=2;  corners.add(penDown,c);

		//c.gridx=0;  c.gridy=0;  corners.add(setHome,c);
		c.insets = new Insets(0,0,0,0);
		goTop.addActionListener(this);
		goBottom.addActionListener(this);
		goLeft.addActionListener(this);
		goRight.addActionListener(this);
		penUp.addActionListener(this);
		penDown.addActionListener(this);
		setHome.addActionListener(this);
		goHome.addActionListener(this);


		JPanel feedRateControl = new JPanel();
		feedRateControl.setLayout(new GridBagLayout());
		c = new GridBagConstraints();
		feedRate = new JFormattedTextField(NumberFormat.getInstance());  feedRate.setPreferredSize(new Dimension(100,20));
		feedRate.setText(Double.toString(robot.settings.getFeedRate()));
		setFeedRate = new JButton(Translator.get("Set"));
		setFeedRate.addActionListener(this);
		disengageMotors = new JButton(Translator.get("DisengageMotors"));
		disengageMotors.addActionListener(this);

		c.gridx=3;  c.gridy=0;  feedRateControl.add(new JLabel(Translator.get("Speed")),c);
		c.gridx=4;  c.gridy=0;  feedRateControl.add(feedRate,c);
		c.gridx=5;  c.gridy=0;  feedRateControl.add(new JLabel(Translator.get("Rate")),c);
		c.gridx=6;  c.gridy=0;  feedRateControl.add(setFeedRate,c);
		c.gridx=7;  c.gridy=0;  feedRateControl.add(disengageMotors,c);

		dragAndDrive = new JPanel(new GridBagLayout());
		dragAndDrive.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		dragAndDrive.addMouseListener(this);
		dragAndDrive.addMouseMotionListener(this);

		coordinates = new JLabel(Translator.get("ClickAndDrag"));
		c.anchor = GridBagConstraints.CENTER;

		// TODO dimensioning doesn't work right.  The better way would be a pen tool to drag on the 3d view.  That's a lot of work.
		Dimension dims = new Dimension();
		dims.setSize( 150, 150 * (double)robot.settings.getPaperWidth()/(double)robot.settings.getPaperHeight());
		dragAndDrive.setPreferredSize(dims);
		dragAndDrive.add(coordinates,c);

		con1.weightx=1;
		con1.weighty=0;
		con1.fill=GridBagConstraints.HORIZONTAL;
		con1.anchor=GridBagConstraints.NORTHWEST;


		panel.add(axisControl,con1);
		con1.gridy++;
		panel.add(new JSeparator(),con1);
		con1.gridy++;
		panel.add(corners,con1);
		con1.gridy++;
		//panel.add(new JSeparator(),con1);
		//con1.gridy++;
		//con1.weighty=1;
		//p.add(dragAndDrive,con1);
		//con1.weighty=0;
		//con1.gridy++;
		panel.add(new JSeparator(),con1);
		con1.gridy++;
		panel.add(feedRateControl,con1);
		con1.gridy++;
		panel.add(new JSeparator(),con1);
		con1.gridy++;



		// File conversion
		buttonNewFile = new JButton(Translator.get("MenuNewFile"));
		buttonNewFile.addActionListener(this);
		panel.add(buttonNewFile, con1);
		con1.gridy++;

		buttonOpenFile = new JButton(Translator.get("MenuOpenFile"));
		buttonOpenFile.addActionListener(this);
		panel.add(buttonOpenFile, con1);
		con1.gridy++;

		buttonGenerate = new JButton(Translator.get("MenuGenerate"));
		buttonGenerate.addActionListener(this);
		panel.add(buttonGenerate, con1);
		con1.gridy++;

		buttonSaveFile = new JButton(Translator.get("MenuSaveGCODEAs"));
		buttonSaveFile.addActionListener(this);
		panel.add(buttonSaveFile, con1);
		con1.gridy++;

		panel.add(new JSeparator(), con1);
		con1.gridy++;

		// drive menu
		JPanel drivePanel = new JPanel(new GridLayout(1,4));
		buttonStart = new JButton(Translator.get("Start"));
		buttonStartAt = new JButton(Translator.get("StartAtLine"));
		buttonPause = new JButton(Translator.get("Pause"));
		buttonHalt = new JButton(Translator.get("Halt"));
		drivePanel.add(buttonStart);
		drivePanel.add(buttonStartAt);
		drivePanel.add(buttonPause);
		drivePanel.add(buttonHalt);
		buttonStart.addActionListener(this);
		buttonStartAt.addActionListener(this);
		buttonPause.addActionListener(this);
		buttonHalt.addActionListener(this);
		panel.add(drivePanel, con1);
		con1.gridy++;

		panel.add(new JSeparator(), con1);
		con1.gridy++;



		statusBar = new StatusBar(translator);
		panel.add(statusBar, con1);
		con1.gridy++;


		// always have one extra empty at the end to push everything up.
		con1.weighty = 1;
		panel.add(new JLabel(), con1);
	}

	public void updateMachineNumberPanel() {
		machineNumberPanel.removeAll();
		machineConfigurations = robot.settings.getKnownMachineNames();
		if( machineConfigurations.length>0 ) {
			machineChoices = new JComboBox<>(machineConfigurations);
			machineNumberPanel.add(new JLabel(Translator.get("MachineNumber")));
			machineNumberPanel.add(machineChoices);

			int index = robot.settings.getKnownMachineIndex();
			if( index<0 ) index=0;
			machineChoices.setSelectedIndex(index);
			
			// if we're connected to a confirmed machine, don't let the user change the number panel or settings could get...weird.
			boolean state=false;
			if( robot.getConnection() == null ) state=true;
			else if( robot.getConnection().isOpen() == false ) state=true;
			else if( robot.isPortConfirmed() == false ) state=true;
			
			machineChoices.setEnabled( state );
		}

		openConfig = new JButton(Translator.get("configureMachine"));
		openConfig.addActionListener(this);
		openConfig.setPreferredSize(openConfig.getPreferredSize());
		machineNumberPanel.add(openConfig);
	}

	public void stateChanged(ChangeEvent e) {
		e.getSource();
		double pm = (100 - paperMargin.getValue()) * 0.01;
		if (Double.compare(robot.settings.getPaperMargin(), pm) != 0) {
			robot.settings.setPaperMargin(pm);
			robot.settings.saveConfig();
		}
	}

	// The user has done something. respond to it.
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();

		if( machineChoices != null ) {
			// TODO: maybe only run this when the machineChoices comboBox changes to a new value
			int selectedIndex = machineChoices.getSelectedIndex();
			long newUID = Long.parseLong(machineChoices.getItemAt(selectedIndex));
			robot.settings.loadConfig(newUID);
		}

		if (subject == openConfig) {
			Frame frame = (Frame)this.getRootPane().getParent();
			MakelangeloSettingsDialog m = new MakelangeloSettingsDialog(frame, translator, robot);
			m.run();
			return;
		}

		if (subject == buttonNewFile) {
			newFile();
			return;
		}
		if (subject == buttonOpenFile) {
			openFileDialog();
			return;
		}
		if (subject == buttonGenerate) {
			generateImage();
			return;
		}
		if (subject == buttonSaveFile) {
			saveFileDialog();
			return;
		}

		if (subject == buttonStart) {
			gui.startAt(0);
			updateButtonAccess(robot.isPortConfirmed(), true);
			return;
		}
		if (subject == buttonStartAt) {
			Long lineNumber = getStartingLineNumber();
			if (lineNumber != -1) {
				gui.startAt(lineNumber);
			}
			updateButtonAccess(robot.isPortConfirmed(), true);
			return;
		}
		if (subject == buttonPause) {
			// toggle pause
			if (robot.isPaused() == true) {
				// we were paused.
				// update button text
				buttonPause.setText(Translator.get("Pause"));
				// ready
				robot.unPause();
				// TODO: if the robot is not ready to unpause, this
				// might fail and the program would appear to hang until
				// a dis- and re-connect.
				gui.sendFileCommand();
			} else {
				robot.pause();
				// change button text
				buttonPause.setText(Translator.get("Unpause"));
			}
			return;
		}
		if (subject == buttonHalt) {
			gui.halt();
			return;
		}
		
		if      (subject == goHome  ) robot.sendLineToRobot("G00 X0 Y0");
		else if (subject == setHome ) robot.sendLineToRobot("G92 X0 Y0");
		else if (subject == goLeft  ) robot.sendLineToRobot("G00 X" + (robot.settings.getPaperLeft() * 10));
		else if (subject == goRight ) robot.sendLineToRobot("G00 X" + (robot.settings.getPaperRight() * 10));
		else if (subject == goTop   ) robot.sendLineToRobot("G00 Y" + (robot.settings.getPaperTop() * 10));
		else if (subject == goBottom) robot.sendLineToRobot("G00 Y" + (robot.settings.getPaperBottom() * 10));
//		else if (b == find    ) robot.sendLineToRobot("G28");
		else if (subject == penUp   ) robot.raisePen();
		else if (subject == penDown ) robot.lowerPen();
		else if (subject == setFeedRate) {
			// get the feedrate value
			String fr = feedRate.getText();
			fr = fr.replaceAll("[ ,]", "");
			// trim it to 3 decimal places
			double parsedFeedRate = 0;
			try {
				parsedFeedRate = Double.parseDouble(fr);

				if (parsedFeedRate < 0.001) parsedFeedRate = 0.001;
				// update the input field
				feedRate.setText(Double.toString(parsedFeedRate));
				robot.setFeedRate(parsedFeedRate);
			} catch(NumberFormatException e1) {}
		} else if (subject == disengageMotors) {
			robot.sendLineToRobot("M18");
		} else {
			String command="";

			if (subject == down100) command = "G0 Y-100";
			if (subject == down10) command = "G0 Y-10";
			if (subject == down1) command = "G0 Y-1";
			if (subject == up100) command = "G0 Y100";
			if (subject == up10) command = "G0 Y10";
			if (subject == up1) command = "G0 Y1";

			if (subject == left100) command = "G0 X-100";
			if (subject == left10) command = "G0 X-10";
			if (subject == left1) command = "G0 X-1";
			if (subject == right100) command = "G0 X100";
			if (subject == right10) command = "G0 X10";
			if (subject == right1) command = "G0 X1";

			if(command != "") {
				robot.sendLineToRobot("G91");  // set relative mode
				robot.sendLineToRobot(command);
				robot.sendLineToRobot("G90");  // return to absolute mode
			}
		}
	}

	/**
	 * open a dialog to ask for the line number.
	 *
	 * @return <code>lineNumber</code> greater than or equal to zero if user hit
	 *         ok.
	 */
	private long getStartingLineNumber() {
		final JPanel panel = new JPanel(new GridBagLayout());
		final JTextField starting_line = new JTextField("0", 8);
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy = 0;
		panel.add(new JLabel(Translator.get("StartAtLine")), c);
		c.gridwidth = 2;
		c.gridx = 2;
		c.gridy = 0;
		panel.add(starting_line, c);

		int result = JOptionPane.showConfirmDialog(null, panel, Translator.get("StartAt"), JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
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

	void updateButtonAccess(boolean isConfirmed, boolean isRunning) {
		if (buttonGenerate != null)
			buttonGenerate.setEnabled(!isRunning);

		openConfig.setEnabled(!isRunning);

		buttonStart.setEnabled(isConfirmed && !isRunning);
		buttonStartAt.setEnabled(isConfirmed && !isRunning);
		buttonPause.setEnabled(isConfirmed && isRunning);
		buttonHalt.setEnabled(isConfirmed && isRunning);

		if (!isConfirmed) {
			buttonPause.setText(Translator.get("Pause"));
		}

		disengageMotors.setEnabled(isConfirmed && !isRunning);
		buttonNewFile.setEnabled(!isRunning);
		buttonOpenFile.setEnabled(!isRunning);
		buttonGenerate.setEnabled(!isRunning);

		down100.setEnabled(isConfirmed && !isRunning);
		down10.setEnabled(isConfirmed && !isRunning);
		down1.setEnabled(isConfirmed && !isRunning);
		up1.setEnabled(isConfirmed && !isRunning);
		up10.setEnabled(isConfirmed && !isRunning);
		up100.setEnabled(isConfirmed && !isRunning);

		left100.setEnabled(isConfirmed && !isRunning);
		left10.setEnabled(isConfirmed && !isRunning);
		left1.setEnabled(isConfirmed && !isRunning);
		right1.setEnabled(isConfirmed && !isRunning);
		right10.setEnabled(isConfirmed && !isRunning);
		right100.setEnabled(isConfirmed && !isRunning);

		goTop.setEnabled(isConfirmed && !isRunning);
		goBottom.setEnabled(isConfirmed && !isRunning);
		goLeft.setEnabled(isConfirmed && !isRunning);
		goRight.setEnabled(isConfirmed && !isRunning);

		setHome.setEnabled(isConfirmed && !isRunning);
		goHome.setEnabled(isConfirmed && !isRunning);

		penUp.setEnabled(isConfirmed && !isRunning);
		penDown.setEnabled(isConfirmed && !isRunning);

		setFeedRate.setEnabled(isConfirmed && !isRunning);
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

		FileFilter filterGCODE = new FileNameExtensionFilter(Translator.get("FileTypeGCode"), "ngc");
		final FileFilter filterImage = new FileNameExtensionFilter(Translator.get("FileTypeImage"),
		IMAGE_FILE_EXTENSIONS.toArray(new String[IMAGE_FILE_EXTENSIONS.size()]));
		FileFilter filterDXF = new FileNameExtensionFilter(Translator.get("FileTypeDXF"), "dxf");

		JFileChooser fc = new JFileChooser(new File(filename));
		fc.addChoosableFileFilter(filterImage);
		fc.addChoosableFileFilter(filterDXF);
		fc.addChoosableFileFilter(filterGCODE);
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String selectedFile = fc.getSelectedFile().getAbsolutePath();

			// if machine is not yet calibrated
			if (robot.settings.isPaperConfigured() == false) {
				JOptionPane.showMessageDialog(null, Translator.get("SetPaperSize"));
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
		panel.add(new JLabel(Translator.get("ConversionStyle")), c);
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = 3;
		c.gridx = 1;
		c.gridy = y++;
		panel.add(options, c);

		int result = JOptionPane.showConfirmDialog(null, panel, Translator.get("ConversionOptions"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			int choice = options.getSelectedIndex();

			ImageGenerator chosenGenerator = imageGenerators.get(choice);
			robot.settings.saveConfig();

			String destinationFile = gui.getTempDestinationFile();
			chosenGenerator.setDrawPanel(gui.getDrawPanel());
			gui.getDrawPanel().setDecorator(chosenGenerator);
			chosenGenerator.generate(destinationFile);
			chosenGenerator.setDrawPanel(null);
			gui.getDrawPanel().setDecorator(null);

			loadGCode(destinationFile);
			gui.soundSystem.playConversionFinishedSound();

			// Force update of graphics layout.
			gui.updateMachineConfig();
			gui.getDrawPanel().repaintNow();
		}
	}

	public void saveFileDialog() {
		// Note: source for ExampleFileFilter can be found in FileChooserDemo,
		// under the demo/jfc directory in the Java 2 SDK, Standard Edition.
		String filename = lastFileOut;

		FileFilter filterGCODE = new FileNameExtensionFilter(Translator.get("FileTypeGCode"), "ngc");

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
				Log.error(Translator.get("Failed") + e.getMessage());
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

	/**
	 * Checks a string's filename, which includes the file extension, (e.g. foo.jpg).
	 *
	 * @param filename - image filename.
	 * @return if the file is one of the acceptable image types.
	 * @see PanelPrepareImage#IMAGE_FILE_EXTENSIONS
	 * @see String#toLowerCase()
	 */
	public boolean isFileImage(final String filename) {
		final String filenameExtension = filename.substring(filename.lastIndexOf('.') + 1);
		return IMAGE_FILE_EXTENSIONS.contains(filenameExtension.toLowerCase());
	}

	// User has asked that a file be opened.
	public void openFileOnDemand(String filename) {
		Log.message(Translator.get("OpeningFile") + filename + "...");
		boolean success = false;

		if (isFileGcode(filename)) {
			success = loadGCode(filename);
		} else if (isFileDXF(filename)) {
			success = loadDXF(filename);
		} else if (isFileImage(filename)) {
			success = loadImage(filename);
		} else {
			Log.error(Translator.get("UnknownFileType"));
		}

		if (success == true) {
			lastFileIn = filename;
			gui.updateMenuBar();
		}

		statusBar.clear();
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

	/**
	 * Opens a file. If the file can be opened, get a drawing time estimate,
	 * update recent files list, and repaint the preview tab.
	 *
	 * @param filename
	 *            what file to open
	 */
	public boolean loadGCode(String filename) {
		try {
			gui.gCode.load(filename);
			Log.message(gui.gCode.estimateCount + Translator.get("LineSegments") + "\n" + gui.gCode.estimatedLength
					+ Translator.get("Centimeters") + "\n" + Translator.get("EstimatedTime")
					+ statusBar.formatTime((long) (gui.gCode.estimatedTime)) + "s.");
		} catch (IOException e) {
			Log.error(Translator.get("FileNotOpened") + e.getLocalizedMessage());
			gui.updateMenuBar();
			return false;
		}

		gui.gCode.changed = true;
		//gui.halt();
		return true;
	}

	protected boolean loadDXF(String filename) {
		// where to save temp output file?
		final String destinationFile = gui.getTempDestinationFile();
		final String srcFile = filename;

		final ProgressMonitor pm = new ProgressMonitor(null, Translator.get("Converting"), "", 0, 100);
		pm.setProgress(0);
		pm.setMillisToPopup(0);

		final SwingWorker<Void, Void> s = new SwingWorker<Void, Void>() {
			public boolean ok = false;

			@SuppressWarnings("unchecked")
			@Override
			public Void doInBackground() {
				Log.message(Translator.get("Converting") + " " + destinationFile);

				Parser parser = ParserBuilder.createDefaultParser();

				double dxf_x2 = 0;
				double dxf_y2 = 0;

				try (FileOutputStream fileOutputStream = new FileOutputStream(destinationFile);
						Writer out = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)) {
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

					// find the scale to fit the image on the paper without
					// altering the aspect ratio
					double imageWidth = (b.getMaximumX() - b.getMinimumX());
					double imageHeight = (b.getMaximumY() - b.getMinimumY());
					double paperHeight = robot.settings.getPaperHeight() * 10 * robot.settings.getPaperMargin();
					double paperWidth = robot.settings.getPaperWidth() * 10 * robot.settings.getPaperMargin();
					// double scale = Math.min( scaleX/imageWidth,
					// scaleY/imageHeight);
					// double scale = (scaleX / imageWidth);
					// if (imageHeight * scale > scaleX) scale = scaleY /
					// imageHeight;

					double innerAspectRatio = imageWidth / imageHeight;
					double outerAspectRatio = paperWidth / paperHeight;
					double scale = (innerAspectRatio >= outerAspectRatio) ? (paperWidth / imageWidth)
							: (paperHeight / imageHeight);
					scale *= (robot.settings.isReverseForGlass() ? -1 : 1);
					// double scaleX = imageWidth * scale;
					// double scaleY = imageHeight * scale;

					// count all entities in all layers
					Iterator<DXFLayer> layer_iter = (Iterator<DXFLayer>) doc.getDXFLayerIterator();
					int entity_total = 0;
					int entity_count = 0;
					while (layer_iter.hasNext()) {
						DXFLayer layer = (DXFLayer) layer_iter.next();
						Log.message("Found layer " + layer.getName());
						Iterator<String> entity_iter = (Iterator<String>) layer.getDXFEntityTypeIterator();
						while (entity_iter.hasNext()) {
							String entity_type = (String) entity_iter.next();
							List<DXFEntity> entity_list = (List<DXFEntity>) layer.getDXFEntities(entity_type);
							Log.message("Found " + entity_list.size() + " of type " + entity_type);
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
									// *
									// is it worth drawing this line?
									dx = x2 - x;
									dy = y2 - y;
									if (dx * dx + dy * dy < tool.getDiameter() / 2.0) {
										continue;
									}
									// */
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
												// line does not start at last
												// tool location, lift and move.
												if (tool.isDrawOn()) {
													tool.writeOff(out);
												}
												tool.writeMoveTo(out, (float) x, (float) y);
											}
											// else line starts right here, do
											// nothing.
										} else {
											// not the first point, draw.
											if (tool.isDrawOff())
												tool.writeOn(out);
											if (j < polyLine.getVertexCount() - 1
													&& dx * dx + dy * dy < tool.getDiameter() / 2.0)
												continue; // less than 1mm
															// movement? Skip
															// it.
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
												// line does not start at last
												// tool location, lift and move.
												if (tool.isDrawOn()) {
													tool.writeOff(out);
												}
												tool.writeMoveTo(out, (float) x, (float) y);
											}
											// else line starts right here, do
											// nothing.
										} else {
											// not the first point, draw.
											if (tool.isDrawOff())
												tool.writeOn(out);
											if (j < entity.getVertexCount() - 1
													&& dx * dx + dy * dy < tool.getDiameter() / 2.0)
												continue; // less than 1mm
															// movement? Skip
															// it.
											tool.writeMoveTo(out, (float) x, (float) y);
										}
										dxf_x2 = x;
										dxf_y2 = y;
									}
								}
							}
						}
					}

					// entities finished. Close up file.
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
				Log.message(Translator.get("Finished"));
				gui.soundSystem.playConversionFinishedSound();
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

	public boolean loadImage(String filename) {
		// where to save temp output file?
		final String sourceFile = filename;
		final String destinationFile = gui.getTempDestinationFile();

		loadImageConverters();
		if (chooseImageConversionOptions() == false)
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
					gui.getDrawPanel().setDecorator(converter);
					converter.convert(img, out);
					converter.setDrawPanel(null);
					gui.getDrawPanel().setDecorator(null);

					if (robot.settings.shouldSignName()) {
						// Sign name
						Generator_YourMessageHere ymh = new Generator_YourMessageHere(robot.settings);
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
				loadGCode(destinationFile);
				gui.soundSystem.playConversionFinishedSound();
				gui.getDrawPanel().repaintNow();
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

	public void mouseClicked(MouseEvent e) {}
	public void mouseDragged(MouseEvent e) {
		mouseAction(e);
	}
	public void mouseEntered(MouseEvent e) {
		mouseInside=true;
	}
	public void mouseExited(MouseEvent e) {
		mouseInside=false;
		mouseOn=false;
	}
	public void mouseMoved(MouseEvent e) {
		mouseAction(e);
	}
	public void mousePressed(MouseEvent e) {
		mouseOn=true;
		mouseAction(e);
	}
	public void mouseReleased(MouseEvent e) {
		mouseOn=false;
	}
	public void mouseWheelMoved(MouseEvent e) {}

	public void mouseAction(MouseEvent e) {
		if(mouseInside && mouseOn) {
			double x = (double)e.getX();
			double y = (double)e.getY();
			Dimension d = dragAndDrive.getSize();
			double w = d.getWidth();
			double h = d.getHeight();
			double cx = w/2.0;
			double cy = h/2.0;
			x = x - cx;
			y = cy - y;
			x *= 10 * robot.settings.getPaperWidth()  / w;
			y *= 10 * robot.settings.getPaperHeight() / h;
			double dx = x-last_x;
			double dy = y-last_y;
			if(Math.sqrt(dx*dx+dy*dy)>=1) {
				last_x=x;
				last_y=y;
				String text = "X"+(Math.round(x*100)/100.0)+" Y"+(Math.round(y*100)/100.0);
				robot.sendLineToRobot("G00 "+text);
				coordinates.setText(text);
			} else {
				coordinates.setText("");
			}
		}
	}
}
