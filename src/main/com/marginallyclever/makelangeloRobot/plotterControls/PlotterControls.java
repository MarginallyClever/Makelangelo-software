package com.marginallyclever.makelangeloRobot.plotterControls;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import java.awt.BorderLayout;
import java.util.ArrayList;

import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleMove;
import com.marginallyclever.makelangeloRobot.Plotter;
import com.marginallyclever.util.PreferencesHelper;

public class PlotterControls extends JPanel {
	private static final long serialVersionUID = 1L;
	private Plotter myPlotter;
	private Turtle myTurtle;
	private JogInterface jogInterface;
	private MarlinInterface marlinInterface;
	private ProgramInterface programInterface;

	private JButton bSaveGCode = new JButton(Translator.get("SaveGCode"));
	private JButton bRewind = new JButton(Translator.get("Rewind"));
	private JButton bStart = new JButton(Translator.get("Play"));
	private JButton bStep = new JButton(Translator.get("Step"));
	private JButton bPause = new JButton(Translator.get("Pause"));
	private JProgressBar progress = new JProgressBar(0,100); 
	
	private boolean isRunning=false;
	private boolean penIsUpBeforePause=false;
		
	public PlotterControls(Plotter plotter,Turtle turtle) {
		super();
		myPlotter=plotter;
		myTurtle=turtle;
		
		jogInterface = new JogInterface(plotter);
		marlinInterface = new MarlinInterface(plotter);
		programInterface = new ProgramInterface(plotter,turtle);
		
		JTabbedPane pane = new JTabbedPane();
		pane.addTab(JogInterface.class.getSimpleName(), jogInterface);
		pane.addTab(MarlinInterface.class.getSimpleName(), marlinInterface);
		pane.addTab(ProgramInterface.class.getSimpleName(), programInterface);
		
		this.setLayout(new BorderLayout());
		this.add(pane,BorderLayout.CENTER);
		this.add(getToolBar(),BorderLayout.NORTH);
		this.add(progress,BorderLayout.SOUTH);
		
		marlinInterface.addListener((e)->{
			if(e.getActionCommand().contentEquals(MarlinInterface.IDLE) ) {
				System.out.println("PlotterControls heard idle");
				if(isRunning) {
					System.out.println("PlotterControls is running");
					programInterface.step();
				}
			}
			updateProgressBar();
		});
	}
	
	private JToolBar getToolBar() {
		JToolBar bar = new JToolBar();
		bar.add(bSaveGCode);
		bar.add(bRewind);
		bar.add(bStart);
		bar.add(bPause);
		bar.add(bStep);
		
		bSaveGCode.addActionListener((e)-> saveGCode());
		bRewind.addActionListener((e)-> rewind());
		bStart.addActionListener((e)-> play());
		bPause.addActionListener((e)-> pause());
		bStep.addActionListener((e)-> step());
		
		updateButtonStatus();
		
		return bar;
	}
	
	private void step() {
		programInterface.step();
	}

	private void saveGCode() {
		Log.message("Saving to gcode...");
		SaveGCode save = new SaveGCode();
		try {
			save.run(myTurtle, myPlotter, this);
		} catch(Exception e) {
			Log.error("Export error: "+e.getLocalizedMessage()); 
			JOptionPane.showMessageDialog(this, e.getLocalizedMessage(), Translator.get("Error"), JOptionPane.ERROR_MESSAGE);
		}
	}

	public void startAt(int lineNumber) {
		int count = programInterface.getMoveCount();
		if(lineNumber>=count) lineNumber = count;
		if(lineNumber<0) lineNumber=0;

		programInterface.setLineNumber(lineNumber);
		play();
	}
	
	private void updateProgressBar() {
		progress.setValue((int)(100.0*programInterface.getLineNumber()/programInterface.getMoveCount()));
	}

	private void rewind() {
		programInterface.rewind();
		progress.setValue(0);
	}

	private void play() {
		isRunning = true;
		updateButtonStatus();
		if(!penIsUpBeforePause) myPlotter.lowerPen();
		programInterface.step();
	}

	private void pause() {
		isRunning = false;
		updateButtonStatus();
		penIsUpBeforePause = myPlotter.getPenIsUp();
		if(!penIsUpBeforePause) myPlotter.raisePen();
	}

	public boolean isRunning() {
		return isRunning;
	}

	private void updateButtonStatus() {
		bRewind.setEnabled(!isRunning);
		bStart.setEnabled(!isRunning);
		bPause.setEnabled(isRunning);
		bStep.setEnabled(!isRunning);
	}
	
	@SuppressWarnings("unused")
	private int findLastPenUpBefore(int startAtLine) {
		ArrayList<TurtleMove> history = myTurtle.history;
		int total = history.size();
		int x = startAtLine;
		if(x >= total) x = total-1;
		if(x<0) return 0;

		while (x > 1) {
			TurtleMove m = history.get(x);
			if(m.type == TurtleMove.TRAVEL) return x;
			--x;
		}

		return x;
	}

	// TEST
	
	public static void main(String[] args) {
		Log.start();
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {}
		
		JFrame frame = new JFrame(PlotterControls.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new PlotterControls(new Plotter(),new Turtle()));
		frame.pack();
		frame.setVisible(true);
	}

	public void closeConnection() {
		marlinInterface.closeConnection();
	}
}
