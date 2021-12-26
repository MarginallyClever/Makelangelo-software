package com.marginallyclever.makelangelo.plotter.plotterControls;

import com.marginallyclever.convenience.ButtonIcon;
import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.convenience.Point2D;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.plotter.PlotterEvent;
import com.marginallyclever.util.PreferencesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class JogInterface extends JPanel {
	private static final Logger logger = LoggerFactory.getLogger(JogInterface.class);
	
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
	    	logger.debug("Move {},{}", x, y);
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
		bar.setFloatable(false);
		JButton penUp    = new ButtonIcon("JogInterface.PenUp", "/images/arrow_up.png");
		JButton penDown  = new ButtonIcon("JogInterface.PenDown", "/images/arrow_down.png");
		toggleEngageMotor = new ButtonIcon("JogInterface.DisengageMotors", "/images/lock_open.png");

		bar.add(penUp);
		bar.add(penDown);
		bar.addSeparator();
		bar.add(toggleEngageMotor);
		
		penUp.addActionListener((e)-> myPlotter.raisePen());
		penDown.addActionListener((e)-> myPlotter.lowerPen());
		toggleEngageMotor.addActionListener(this::onToggleEngageMotorAction);
		
		return bar;
	}

	private void onToggleEngageMotorAction(ActionEvent e) {
		if (myPlotter.getAreMotorsEngaged()) {
			((ButtonIcon) e.getSource()).replaceIcon("/images/lock.png");
			myPlotter.disengageMotors();
		} else {
			((ButtonIcon) e.getSource()).replaceIcon("/images/lock_open.png");
			myPlotter.engageMotors();
		}
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
