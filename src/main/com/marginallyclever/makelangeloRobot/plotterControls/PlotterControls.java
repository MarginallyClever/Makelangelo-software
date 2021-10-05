package com.marginallyclever.makelangeloRobot.plotterControls;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

import java.awt.BorderLayout;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.CommandLineOptions;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.Plotter;
import com.marginallyclever.util.PreferencesHelper;

public class PlotterControls extends JPanel {
	private static final long serialVersionUID = 1L;
	
	public PlotterControls(Plotter plotter) {
		super();
		
		JTabbedPane pane = new JTabbedPane();
		pane.addTab(JogInterface.class.getSimpleName(), new JogInterface(plotter));
		pane.addTab(MarlinInterface.class.getSimpleName(), new MarlinInterface(plotter));
		//pane.addTab(ProgramInterface.class.getSimpleName(), new ProgramInterface(plotter));
		
		this.setLayout(new BorderLayout());
		this.add(pane,BorderLayout.CENTER);
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
		frame.add(new PlotterControls(new Plotter()));
		frame.pack();
		frame.setVisible(true);
	}
}
