package com.marginallyclever.makelangelo.plotter.plotterControls;

import com.marginallyclever.communications.NetworkSessionItem;
import com.marginallyclever.communications.NetworkSessionUIManager;
import com.marginallyclever.convenience.ButtonIcon;
import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.makelangelo.CollapsiblePanel;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleMove;
import com.marginallyclever.util.PreferencesHelper;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.List;

public class PlotterControls extends JPanel {
	private final Plotter myPlotter;
	private final Turtle myTurtle;
	private final JogInterface jogInterface;
	private final MarlinInterface marlinInterface;
	private final ProgramInterface programInterface;

	private JComboBox<NetworkSessionItem> connectionComboBox = new JComboBox<>();
	private ConnectionButton connectionButton = new ConnectionButton(connectionComboBox);
	private ButtonIcon bFindHome;
	private ButtonIcon bRewind;
	private ButtonIcon bStart;
	private ButtonIcon bStep;
	private ButtonIcon bPause;
	private final JProgressBar progress = new JProgressBar(0, 100);

	private boolean isRunning = false;
	private boolean penIsUpBeforePause = false;

	public PlotterControls(Plotter plotter, Turtle turtle, Window parentWindow) {
		super();
		myPlotter = plotter;
		myTurtle = turtle;

		jogInterface = new JogInterface(plotter);
		marlinInterface = new MarlinPlotterInterface(plotter);
		programInterface = new ProgramInterface(plotter, turtle);

		CollapsiblePanel panelDebug = new CollapsiblePanel(parentWindow, Translator.get("PlotterControls.DebugControls"));
		JTabbedPane pane = new JTabbedPane();
		pane.addTab(Translator.get("PlotterControls.JogTab"), jogInterface);
		pane.addTab(Translator.get("PlotterControls.MarlinTab"), marlinInterface);
		pane.addTab(Translator.get("PlotterControls.ProgramTab"), programInterface);
		panelDebug.add(pane);

		this.setLayout(new BorderLayout());
		this.add(panelDebug, BorderLayout.CENTER);
		this.add(getButtonsPanels(), BorderLayout.NORTH);
		this.add(progress, BorderLayout.SOUTH);

		marlinInterface.addListener(e -> {
			if (e.getActionCommand().contentEquals(MarlinInterface.IDLE) && isRunning) {
				step();
			}
			updateProgressBar();
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
		panel.add(connectionComboBox);

		ButtonIcon refresh = new ButtonIcon("", "/images/arrow_refresh.png");
		refresh.addActionListener(e -> addConnectionsItems());
		panel.add(refresh);
		addConnectionsItems();
		panel.add(connectionButton);
		return panel;
	}

	private void addConnectionsItems() {
		connectionComboBox.removeAllItems();
		for (NetworkSessionItem connection: NetworkSessionUIManager.getConnectionsItems()) {
			connectionComboBox.addItem(connection);
		}
	}

	private JPanel getDrawPanel() {

		bFindHome = new ButtonIcon("PlotterControls.FindHome", "/images/house.png");
		bRewind = new ButtonIcon("PlotterControls.Rewind", "/images/control_start_blue.png");
		bStart = new ButtonIcon("PlotterControls.Play", "/images/control_play_blue.png");
		bStep = new ButtonIcon("PlotterControls.Step", "/images/control_fastforward_blue.png");
		bPause = new ButtonIcon("PlotterControls.Pause", "/images/control_pause_blue.png");

		JPanel panel = new JPanel();
		Border border = BorderFactory.createTitledBorder(Translator.get("PlotterControls.DrawControls"));
		panel.setBorder(border);
		panel.add(bFindHome);
		panel.add(bRewind);
		panel.add(bStart);
		panel.add(bPause);
		panel.add(bStep);

		bFindHome.addActionListener(e -> findHome());
		bRewind.addActionListener(e -> rewind());
		bStart.addActionListener(e -> play());
		bPause.addActionListener(e -> pause());
		bStep.addActionListener(e -> step());

		updateButtonStatus();

		return panel;
	}

	private void findHome() {
		jogInterface.findHome();
		updateButtonStatus();
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
		updateButtonStatus();
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
		updateButtonStatus();
		penIsUpBeforePause = myPlotter.getPenIsUp();
		if (!penIsUpBeforePause)
			myPlotter.raisePen();
	}

	public boolean isRunning() {
		return isRunning;
	}

	private void updateButtonStatus() {
		boolean isHomed = myPlotter.getDidFindHome();
		bRewind.setEnabled(isHomed && !isRunning);
		bStart.setEnabled(isHomed && !isRunning);
		bPause.setEnabled(isHomed && isRunning);
		bStep.setEnabled(isHomed && !isRunning);
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
		marlinInterface.closeConnection();
	}

	// TEST

	public static void main(String[] args) {
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();

		JFrame frame = new JFrame(Translator.get("PlotterControls.Title"));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new PlotterControls(new Plotter(), new Turtle(), frame));
		frame.pack();
		frame.setVisible(true);
	}

}
