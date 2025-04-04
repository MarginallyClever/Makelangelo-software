package com.marginallyclever.makelangelo.plotter.plottercontrols;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.marginallyclever.communications.NetworkSessionEvent;
import com.marginallyclever.convenience.ButtonIcon;
import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.makelangelo.CollapsiblePanel;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.PlotterEvent;
import com.marginallyclever.makelangelo.plotter.plottersettings.PlotterSettings;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.util.PreferencesHelper;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.Arrays;

/**
 * {@link PlotterControls} brings together three separate panels and wraps all
 * the lower level features in a human friendly, intuitive interface. - The
 * {@link MarlinPanel}, which manages the two way network connection to a
 * robot running Marlin firmware. - The {@link JogPanel}, which is a
 * human-friendly way to drive a {@link Plotter} - The {@link ProgramPanel},
 * which is a buffer for queueing commands to a {@link Plotter}
 *
 * @author Dan Royer
 * @since 7.28.0
 */
public class PlotterControls extends JPanel {
	public static final int DIMENSION_PANEL_WIDTH = 850;
	public static final int DIMENSION_PANEL_HEIGHT = 510;
	private static final int DIMENSION_COLLAPSIBLE_HEIGHT = 570;
	private final Plotter myPlotter;
	private final Turtle myTurtle;
	private final JogPanel jogPanel;
	private final MarlinPlotterPanel marlinInterface;
	private final ProgramPanel programPanel;

	private final ChooseConnection chooseConnection = new ChooseConnection();
	private ButtonIcon bFindHome;
	private ButtonIcon bRewind;
	private ButtonIcon bStart;
	private ButtonIcon bStep;
	private ButtonIcon bPause;
	private ButtonIcon bEmergencyStop;
	private final JProgressBar progress = new JProgressBar(0, 100);

	private boolean isRunning = false;
	private boolean penIsUpBeforePause = false;
	private boolean isErrorAlreadyDisplayed = false;

	public PlotterControls(Plotter plotter, Turtle turtle, Window parentWindow) {
		super();
		myPlotter = plotter;
		myTurtle = turtle;

		jogPanel = new JogPanel(plotter);
		marlinInterface = new MarlinPlotterPanel(plotter, chooseConnection);
		programPanel = new ProgramPanel(plotter, turtle);

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab(Translator.get("PlotterControls.JogTab"), jogPanel);
		tabbedPane.addTab(Translator.get("PlotterControls.MarlinTab"), marlinInterface);
		tabbedPane.addTab(Translator.get("PlotterControls.ProgramTab"), programPanel);

		CollapsiblePanel collapsiblePanel = new CollapsiblePanel(parentWindow, Translator.get("PlotterControls.AdvancedControls"), DIMENSION_COLLAPSIBLE_HEIGHT, false);
		collapsiblePanel.add(tabbedPane);

		this.setLayout(new BorderLayout());
		this.add(collapsiblePanel, BorderLayout.CENTER);
		this.add(getButtonsPanels(), BorderLayout.NORTH);
		this.add(progress, BorderLayout.SOUTH);

		marlinInterface.addListener(this::onMarlinEvent);

		myPlotter.addPlotterEventListener((e)-> {
			if (e.type == PlotterEvent.HOME_FOUND) {
				updateButtonStatusConnected();
			}
		});
	}
  
	private void onMarlinEvent(MarlinPanelEvent e) {
		switch (e.getID()) {
			case MarlinPanelEvent.IDLE -> {
					if (isRunning) step();
				}
			case MarlinPanelEvent.ERROR,
				MarlinPanelEvent.DID_NOT_FIND,
				MarlinPanelEvent.COMMUNICATION_FAILURE -> {
					if (!isErrorAlreadyDisplayed) {
						String message;

						switch(e.getActionCommand()) {
							case "communicationFailure" -> message = Translator.get("PlotterControls.communicationFailure");
							case "didNotFind" -> message = Translator.get("PlotterControls.didNotFind");
							case "Printed halted" -> message = Translator.get("PlotterControls.halted");
							default -> message = e.getActionCommand();
						}
						/* TODO Source of dialog box titled "Error" that says "PlotterControls.null".
						 *      Caused by robot being turned off while COM port is connected.
						 */
						JOptionPane.showMessageDialog(this,
								message,
								Translator.get("ErrorTitle"),
								JOptionPane.ERROR_MESSAGE);
						isErrorAlreadyDisplayed = true;
					}
				}
			case MarlinPanelEvent.HOME_XY_FIRST ->
					JOptionPane.showMessageDialog(this,
							Translator.get("PlotterControls.homeXYFirst"),
							Translator.get("InfoTitle"),
							JOptionPane.WARNING_MESSAGE);
		}
		updateProgressBar();
	}

	private JPanel getButtonsPanels() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(getConnectPanel(), BorderLayout.NORTH);
		panel.add(getDrawPanel(), BorderLayout.CENTER);
		return panel;
	}

	private JPanel getConnectPanel() {
		JPanel panel = new JPanel();
		Border border = BorderFactory.createTitledBorder(Translator.get("PlotterControls.ConnectControls"));
		panel.setBorder(border);
		panel.add(chooseConnection);
		chooseConnection.addListener(e -> {
			switch (e.flag) {
				case NetworkSessionEvent.CONNECTION_OPENED -> onConnect();
				case NetworkSessionEvent.CONNECTION_CLOSED -> onDisconnect();
			}
		});

		return panel;
	}

	private JPanel getDrawPanel() {
		JPanel panel = new JPanel();
		Border border = BorderFactory.createTitledBorder(Translator.get("PlotterControls.DrawControls"));
		panel.setBorder(border);

		bFindHome = new ButtonIcon( Translator.get("PlotterControls.FindHome"), "/images/house.png");
		bRewind = new ButtonIcon( Translator.get("PlotterControls.Rewind"), "/images/control_start_blue.png");
		bStart = new ButtonIcon( Translator.get("PlotterControls.Play"), "/images/control_play_blue.png");
		bStep = new ButtonIcon( Translator.get("PlotterControls.Step"), "/images/control_fastforward_blue.png");
		bPause = new ButtonIcon( Translator.get("PlotterControls.Pause"), "/images/control_pause_blue.png");
		bEmergencyStop = new ButtonIcon( Translator.get("PlotterControls.EmergencyStop"), "/images/stop.png");
		bEmergencyStop.setForeground(Color.RED);

		panel.add(bFindHome);
		panel.add(bRewind);
		panel.add(bStart);
		panel.add(bPause);
		panel.add(bStep);
		panel.add(bEmergencyStop);

		bFindHome.addActionListener(e -> findHome());
		bRewind.addActionListener(e -> rewind());
		bStart.addActionListener(e -> play());
		bPause.addActionListener(e -> pause());
		bStep.addActionListener(e -> step());
		bEmergencyStop.addActionListener(e ->
		{
			marlinInterface.sendESTOP();
			chooseConnection.closeConnection();
		});

		onDisconnect();

		return panel;
	}

	private void findHome() {
		jogPanel.findHome();
		updateButtonStatusConnected();
	}

	private void step() {
		programPanel.step();
		if (programPanel.getLineNumber() == -1) {
			// done
			addUserEndGCODE();
			pause();
		}
	}

	public void startAt(int lineNumber) {
		int count = programPanel.getMoveCount();
		if (lineNumber >= count)
			lineNumber = count;
		if (lineNumber < 0)
			lineNumber = 0;

		programPanel.setLineNumber(lineNumber);
		play();
	}

	private void updateProgressBar() {
		progress.setValue((int) (100.0 * programPanel.getLineNumber() / programPanel.getMoveCount()));
	}

	private void rewind() {
		programPanel.rewind();
		progress.setValue(0);
	}

	private void play() {
		isRunning = true;
		updateButtonStatusConnected();
		if (!penIsUpBeforePause)
			myPlotter.lowerPen();
		rewindIfNoProgramLineSelected();
		addUserStartGCODE();
		step();
	}

	private void rewindIfNoProgramLineSelected() {
		if (programPanel.getLineNumber() == -1) {
			programPanel.rewind();
		}
	}

	private void pause() {
		isRunning = false;
		updateButtonStatusConnected();
		penIsUpBeforePause = myPlotter.getPenIsUp();
		if (!penIsUpBeforePause)
			myPlotter.raisePen();
	}

	public boolean isRunning() {
		return isRunning;
	}

	private void updateButtonStatusConnected() {
		boolean isHomed = myPlotter.getDidFindHome();
		bRewind.setEnabled(isHomed && !isRunning);
		bStart.setEnabled(isHomed && !isRunning);
		bPause.setEnabled(isHomed && isRunning);
		bStep.setEnabled(isHomed && !isRunning);
	}

	private void onConnect() {
		myPlotter.reInit();
		bFindHome.setEnabled(true);
		bEmergencyStop.setEnabled(true);
		isRunning = false;
		updateButtonStatusConnected();
		jogPanel.onNetworkConnect();
		isErrorAlreadyDisplayed = false;
	}

	private void onDisconnect() {
		myPlotter.reInit();
		bFindHome.setEnabled(false);
		bEmergencyStop.setEnabled(false);
		bRewind.setEnabled(false);
		bStart.setEnabled(false);
		bPause.setEnabled(false);
		bStep.setEnabled(false);
		jogPanel.onNetworkDisconnect();
	}

	@SuppressWarnings("unused")
	private int findLastPenUpBefore(int startAtLine) {
		int sum=0;
		for( var strokeLayer : myTurtle.strokeLayers ) {
			for (var line : strokeLayer.getAllLines()) {
				int size = line.getAllPoints().size();
				if (sum <= startAtLine && startAtLine < sum + size) {
					// the start of this line (sum) is the right value.
					return sum;
				}
			}
		}
		// all layers are empty?
		return 0;
	}

	private void addUserStartGCODE() {
		String gcode = myPlotter.getSettings().getString(PlotterSettings.START_GCODE);
		Arrays.asList(gcode.split(System.getProperty("line.separator")).clone())
				.forEach(marlinInterface::queueAndSendCommand);
	}

	private void addUserEndGCODE() {
		String gcode = myPlotter.getSettings().getString(PlotterSettings.END_GCODE);
		Arrays.asList(gcode.split(System.getProperty("line.separator")).clone())
				.forEach(marlinInterface::queueAndSendCommand);
	}

	/**
	 * Called from windowAdapter::windowClosing() to clean up resources.
	 */
	public void onDialogClosing() {
		// make sure to close the connection when the dialog is closed.
		chooseConnection.closeConnection();
		// make sure to unregister listeners
		marlinInterface.stopListeningToPlotter();
	}

	// TEST

	public static void main(String[] args) throws UnsupportedLookAndFeelException {
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();
		FlatLaf.registerCustomDefaultsSource( "com.marginallyclever.makelangelo" );
		UIManager.setLookAndFeel( new FlatLightLaf() );

		JFrame frame = new JFrame(Translator.get("PlotterControls.Title"));
		frame.setPreferredSize(new Dimension(DIMENSION_PANEL_WIDTH, DIMENSION_PANEL_HEIGHT));
		frame.setMinimumSize(new Dimension(DIMENSION_PANEL_WIDTH, DIMENSION_PANEL_HEIGHT));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new PlotterControls(new Plotter(), new Turtle(), frame));
		frame.pack();
		frame.setVisible(true);
	}
}
