package com.marginallyclever.makelangelo.plotter.plotterControls;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import java.awt.BorderLayout;

import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.PlotterEvent;
import com.marginallyclever.util.PreferencesHelper;

public class JogInterface extends JPanel {
	private static final long serialVersionUID = -7408469373702327861L;
	private Plotter myPlotter;
	private CartesianButtons bCartesian = new CartesianButtons();
	private JButton toggleEngageMotor;
	
	public JogInterface(Plotter plotter) {
		super();
		myPlotter=plotter;
		
		this.setLayout(new BorderLayout());
		this.add(bCartesian,BorderLayout.CENTER);
		this.add(getToolBar(),BorderLayout.NORTH);
		
		bCartesian.addActionListener((e)->{
	    	int id = e.getID();
	    	if(CartesianButtons.isCenterZone(id)) {
	    		findHome();
	    		return;
	    	}
	    	int q=CartesianButtons.getQuadrant(id);
	    	int z=CartesianButtons.getZone(id);
	    	int x,y;
	    	if((q%2)==1) {
	    		x=0;
	    		y=100;
	    	} else {
	    		x=100;
	    		y=0;
	    	}
	    	if(q>1) {
	    		x=-x;
	    		y=-y;
	    	}
	    	while(z-->0) {
	    		x/=10;
	    		y/=10;
	    	}
	    	Log.message("Move "+x+","+y);
	    	Point2D p = plotter.getPos();
	    	p.x+=x;
	    	p.y+=y;
	    	plotter.moveTo(p);
	    });

		myPlotter.addListener((e)-> {
			if(e.type == PlotterEvent.MOTORS_ENGAGED
			|| e.type == PlotterEvent.HOME_FOUND) {
				updateButtonStatus();
			}
		});
		updateButtonStatus();
	}
	
	public void findHome() {
		myPlotter.findHome();
		updateButtonStatus();
	}
	
	private JToolBar getToolBar() {
		JToolBar bar = new JToolBar();

		JButton penUp    = new JButton(Translator.get("JogInterface.PenUp"));
		JButton penDown  = new JButton(Translator.get("JogInterface.PenDown"));
		JButton findHome = new JButton(Translator.get("JogInterface.FindHome"));
		toggleEngageMotor = new JButton(Translator.get("JogInterface.DisengageMotors"));

		bar.add(findHome);
		bar.addSeparator();
		bar.add(penUp);
		bar.add(penDown);
		bar.addSeparator();
		bar.add(toggleEngageMotor);
		
		penUp.addActionListener((e)-> myPlotter.raisePen());
		penDown.addActionListener((e)-> myPlotter.lowerPen());
		findHome.addActionListener((e)-> myPlotter.findHome());
		toggleEngageMotor.addActionListener((e)-> onToggleEngageMotorAction());
		
		return bar;
	}

	private void onToggleEngageMotorAction() {
		if(myPlotter.getAreMotorsEngaged() )
			myPlotter.disengageMotors();
		else
			myPlotter.engageMotors();
	}

	private void updateButtonStatus() {
		toggleEngageMotor.setText(Translator.get( myPlotter.getAreMotorsEngaged() ? "JogInterface.DisengageMotors" : "JogInterface.EngageMotors" ));
		bCartesian.setEnabled(myPlotter.getDidFindHome());
	}

	public static void main(String[] args) {
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();
		
		JFrame frame = new JFrame(JogInterface.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new JogInterface(new Plotter()));
		frame.pack();
		frame.setVisible(true);
	}
}
