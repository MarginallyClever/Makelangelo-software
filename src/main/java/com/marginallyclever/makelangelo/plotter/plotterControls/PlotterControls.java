package com.marginallyclever.makelangelo.plotter.plotterControls;

import com.marginallyclever.communications.NetworkSessionEvent;
import com.marginallyclever.convenience.ButtonIcon;
import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.makelangelo.CollapsiblePanel;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.PlotterEvent;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleMove;
import com.marginallyclever.util.PreferencesHelper;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.List;

/**
 * {@link PlotterControls} brings together three separate panels and wraps all
 * the lower level features in a human friendly, intuitive interface. - The
 * {@link MarlinInterface}, which manages the two way network connection to a
 * robot running Marlin firmware. - The {@link JogInterface}, which is a
 * human-friendly way to drive a {@link Plotter} - The {@link ProgramInterface},
 * which is a buffer for queueing commands to a {@link Plotter}
 *
 * @author Dan Royer
 * @since 7.28.0
 */
public class PlotterControls extends JPanel {
    private static final long serialVersionUID = -1201865024705737250L;
	private final Plotter myPlotter;
	private final Turtle myTurtle;
	private final JogInterface jogInterface;
	private final MarlinInterface marlinInterface;
	private final ProgramInterface programInterface;


	private ChooseConnection chooseConnection = new ChooseConnection();
	private ButtonIcon bFindHome;
	private ButtonIcon bRewind;
	private ButtonIcon bStart;
	private ButtonIcon bStep;
	private ButtonIcon bPause;
	private ButtonIcon bEmergencyStop;
	private final JProgressBar progress = new JProgressBar(0, 100);

	private boolean isRunning = false;
	private boolean penIsUpBeforePause = false;

	public PlotterControls(Plotter plotter, Turtle turtle, Window parentWindow) {
		super();
		myPlotter = plotter;
		myTurtle = turtle;

		jogInterface = new JogInterface(plotter);
		marlinInterface = new MarlinPlotterInterface(plotter, chooseConnection);
		programInterface = new ProgramInterface(plotter, turtle);

		JTabbedPane tabbedPane = new JTabbedPane();
		jogInterface.setPreferredSize(new Dimension(780, 300));
		tabbedPane.addTab(Translator.get("PlotterControls.JogTab"), jogInterface);
		tabbedPane.addTab(Translator.get("PlotterControls.MarlinTab"), marlinInterface);
		tabbedPane.addTab(Translator.get("PlotterControls.ProgramTab"), programInterface);

		CollapsiblePanel collapsiblePanel = new CollapsiblePanel(parentWindow, Translator.get("PlotterControls.AdvancedControls"), 570);
		collapsiblePanel.add(tabbedPane);

		this.setLayout(new BorderLayout());
		this.add(collapsiblePanel, BorderLayout.CENTER);
		this.add(getButtonsPanels(), BorderLayout.NORTH);
		this.add(progress, BorderLayout.SOUTH);

		marlinInterface.addListener(e -> {
			if (e.getActionCommand().equals(MarlinInterface.IDLE) && isRunning) {
				step();
			} else if (e.getActionCommand().equals(MarlinInterface.ERROR)) {
				JOptionPane.showMessageDialog(this,  Translator.get("PlotterControls.FatalError"), Translator.get("PlotterControls.FatalErrorTitle"), JOptionPane.ERROR_MESSAGE);
			}
			updateProgressBar();
		});
		
		chooseConnection.addListener(e -> {
			switch (e.flag) {
				case NetworkSessionEvent.CONNECTION_OPENED -> onConnect();
				case NetworkSessionEvent.CONNECTION_CLOSED -> onDisconnect();
			}
		});

		myPlotter.addPlotterEventListener((e)-> {
			if (e.type == PlotterEvent.HOME_FOUND) {
				updateButtonStatusConnected();
			}
		});
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

		return panel;
	}

	private JPanel getDrawPanel() {

		JPanel panel = new JPanel();
		Border border = BorderFactory.createTitledBorder(Translator.get("PlotterControls.DrawControls"));
		panel.setBorder(border);

		bFindHome = new ButtonIcon("PlotterControls.FindHome", "/images/house.png");
		bRewind = new ButtonIcon("PlotterControls.Rewind", "/images/control_start_blue.png");
		bStart = new ButtonIcon("PlotterControls.Play", "/images/control_play_blue.png");
		bStep = new ButtonIcon("PlotterControls.Step", "/images/control_fastforward_blue.png");
		bPause = new ButtonIcon("PlotterControls.Pause", "/images/control_pause_blue.png");
		bEmergencyStop = new ButtonIcon("PlotterControls.EmergencyStop", "/images/stop.png");
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
		jogInterface.findHome();
		updateButtonStatusConnected();
	}

	private void step() {
		programInterface.step();
		if (programInterface.getLineNumber() == -1) {
			// done
			pause();
		}
	}

	public void startAt(int lineNumber) {
		int count = programInterface.getMoveCount();
		if (lineNumber >= count)
			lineNumber = count;
		if (lineNumber < 0)
			lineNumber = 0;

		programInterface.setLineNumber(lineNumber);
		play();
	}

	private void updateProgressBar() {
		progress.setValue((int) (100.0 * programInterface.getLineNumber() / programInterface.getMoveCount()));
	}

	private void rewind() {
		programInterface.rewind();
		progress.setValue(0);
	}

	private void play() {
		isRunning = true;
		updateButtonStatusConnected();
		if (!penIsUpBeforePause)
			myPlotter.lowerPen();
		rewindIfNoProgramLineSelected();
		step();
	}

	private void rewindIfNoProgramLineSelected() {
		if (programInterface.getLineNumber() == -1) {
			programInterface.rewind();
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
		updateButtonStatusConnected();
		jogInterface.onNetworkConnect();
	}

	private void onDisconnect() {
		myPlotter.reInit();
		bFindHome.setEnabled(false);
		bEmergencyStop.setEnabled(false);
		bRewind.setEnabled(false);
		bStart.setEnabled(false);
		bPause.setEnabled(false);
		bStep.setEnabled(false);
		jogInterface.onNetworkDisconnect();
	}

	@SuppressWarnings("unused")
	private int findLastPenUpBefore(int startAtLine) {
		List<TurtleMove> history = myTurtle.history;
		int total = history.size();
		int x = startAtLine;
		if (x >= total)
			x = total - 1;
		if (x < 0)
			return 0;

		while (x > 1) {
			TurtleMove m = history.get(x);
			if (m.type == TurtleMove.TRAVEL)
				return x;
			--x;
		}

		return x;
	}

	public void closeConnection() {
		chooseConnection.closeConnection();
	}

	// TEST

	public static void main(String[] args) {
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();

		JFrame frame = new JFrame(Translator.get("PlotterControls.Title"));
		frame.setPreferredSize(new Dimension(850, 220));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new PlotterControls(new Plotter(), new Turtle(), frame));
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(false);
	}

}
