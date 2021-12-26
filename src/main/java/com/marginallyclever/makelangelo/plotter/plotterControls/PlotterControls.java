package com.marginallyclever.makelangelo.plotter.plotterControls;

import com.marginallyclever.convenience.ButtonIcon;
import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleMove;
import com.marginallyclever.util.PreferencesHelper;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PlotterControls extends JPanel {
	private static final long serialVersionUID = 1L;
	private Plotter myPlotter;
	private Turtle myTurtle;
	private JogInterface jogInterface;
	private MarlinInterface marlinInterface;
	private ProgramInterface programInterface;

	private JButton bFindHome;
	private JButton bRewind;
	private JButton bStart;
	private JButton bStep;
	private JButton bPause;
	private JProgressBar progress = new JProgressBar(0, 100);

	private boolean isRunning = false;
	private boolean penIsUpBeforePause = false;

	public PlotterControls(Plotter plotter, Turtle turtle) {
		super();
		myPlotter = plotter;
		myTurtle = turtle;

		jogInterface = new JogInterface(plotter);
		marlinInterface = new MarlinPlotterInterface(plotter);
		programInterface = new ProgramInterface(plotter, turtle);

		JTabbedPane pane = new JTabbedPane();
		pane.addTab(JogInterface.class.getSimpleName(), jogInterface);
		pane.addTab(MarlinInterface.class.getSimpleName(), marlinInterface);
		pane.addTab(ProgramInterface.class.getSimpleName(), programInterface);

		this.setLayout(new BorderLayout());
		this.add(pane, BorderLayout.CENTER);
		this.add(getToolBar(), BorderLayout.NORTH);
		this.add(progress, BorderLayout.SOUTH);

		marlinInterface.addListener((e) -> {
			if (e.getActionCommand().contentEquals(MarlinInterface.IDLE)) {
				// logger.debug("PlotterControls heard idle");
				if (isRunning) {
					// logger.debug("PlotterControls is running");
					step();
				}
			}
			updateProgressBar();
		});
	}

	private JPanel getToolBar() {

		bFindHome = new PlotterButton("JogInterface.FindHome", "/images/house.png");
		bRewind = new PlotterButton("PlotterControls.Rewind", "/images/control_start_blue.png");
		bStart = new PlotterButton("PlotterControls.Play", "/images/control_play_blue.png");
		bStep = new PlotterButton("PlotterControls.Step", "/images/control_fastforward_blue.png");
		bPause = new PlotterButton("PlotterControls.Pause", "/images/control_pause_blue.png");

		JPanel panel = new JPanel();
		panel.add(bFindHome);
		panel.add(bRewind);
		panel.add(bStart);
		panel.add(bPause);
		panel.add(bStep);

		bFindHome.addActionListener((e) -> findHome());
		bRewind.addActionListener((e) -> rewind());
		bStart.addActionListener((e) -> play());
		bPause.addActionListener((e) -> pause());
		bStep.addActionListener((e) -> step());

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

	static class PlotterButton extends ButtonIcon{

		PlotterButton(String translationKey, String iconPath) {
			super(translationKey, iconPath);
			setMargin(new Insets(5, 5, 5, 5));
		}
	}

	// TEST

	public static void main(String[] args) {
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();

		JFrame frame = new JFrame(Translator.get("PlotterControls.Title"));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new PlotterControls(new Plotter(), new Turtle()));
		frame.pack();
		frame.setVisible(true);
	}

}
