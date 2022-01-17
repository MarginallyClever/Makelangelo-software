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

/**
 * {@link JogInterface} provides cartesian driving controls for a {@link Plotter}.
 * It also includes buttons to engage/disengage the motors; find home; and raise/lower the pen.
 * Cartesian driving is disabled until the "find home" action has completed.
 *
 * @author Dan Royer
 * @since 7.28.0
 */
public class JogInterface extends JPanel {
	private static final Logger logger = LoggerFactory.getLogger(JogInterface.class);
	
	private static final long serialVersionUID = -7408469373702327861L;
	private Plotter myPlotter;
	private CartesianButtons bCartesian = new CartesianButtons();
	private JButton toggleEngageMotor;
	private JButton findHome;
	private JButton penUp;
	private JButton penDown;

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
	    	plotter.setPos(p.x,p.y);
	    });

		myPlotter.addPlotterEventListener((e)-> {
			if(e.type == PlotterEvent.MOTORS_ENGAGED
			|| e.type == PlotterEvent.HOME_FOUND) {
				updateButtonStatusWithPlotter();
			}
		});
		updateButtonStatusWithPlotter();
		updateButtonsStatus(false);
	}
	
	public void findHome() {
		myPlotter.findHome();
		updateButtonStatusWithPlotter();
	}

	public void onNetworkConnect() {
		updateButtonsStatus(true);
	}

	public void onNetworkDisconnect() {
		updateButtonsStatus(false);
	}
	
	private JToolBar getToolBar() {
		JToolBar bar = new JToolBar();
		bar.setFloatable(false);
		findHome = new ButtonIcon("JogInterface.FindHome", "/images/house.png");
		penUp = new ButtonIcon("JogInterface.PenUp", "/images/arrow_up.png");
		penDown  = new ButtonIcon("JogInterface.PenDown", "/images/arrow_down.png");
		toggleEngageMotor = new ButtonIcon("JogInterface.DisengageMotors", "/images/lock_open.png");

		bar.add(findHome);
		bar.addSeparator();
		bar.add(penUp);
		bar.add(penDown);
		bar.addSeparator();
		bar.add(toggleEngageMotor);

		findHome.addActionListener(e -> myPlotter.findHome());
		penUp.addActionListener(e-> myPlotter.raisePen());
		penDown.addActionListener(e-> myPlotter.lowerPen());
		toggleEngageMotor.addActionListener(this::onToggleEngageMotorAction);
		
		return bar;
	}

	private void onToggleEngageMotorAction(ActionEvent e) {
		if (myPlotter.getMotorsEngaged()) {
			((ButtonIcon) e.getSource()).replaceIcon("/images/lock.png");
			myPlotter.setMotorsEngaged(false);
		} else {
			((ButtonIcon) e.getSource()).replaceIcon("/images/lock_open.png");
			myPlotter.setMotorsEngaged(true);
		}
	}

	private void updateButtonStatusWithPlotter() {
		toggleEngageMotor.setText(Translator.get(myPlotter.getMotorsEngaged() ? "JogInterface.DisengageMotors" : "JogInterface.EngageMotors"));
		boolean isHomed = myPlotter.getDidFindHome();
		bCartesian.setEnabled(isHomed);
	}

	private void updateButtonsStatus(boolean isConnected) {
		findHome.setEnabled(isConnected);
		penUp.setEnabled(isConnected);
		penDown.setEnabled(isConnected);
		toggleEngageMotor.setEnabled(isConnected);
		if(!isConnected) bCartesian.setEnabled(false);
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
