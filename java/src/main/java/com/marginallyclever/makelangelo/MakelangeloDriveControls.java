package com.marginallyclever.makelangelo;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * The GUI for the live driving controls, the start/pause/stop buttons, and the "send gcode" dialog.
 * @author danroyer
 * @since 7.1.4
 */
public class MakelangeloDriveControls
	extends JPanel
	implements ActionListener, KeyListener {
	protected JButton down100,down10,down1,up1,up10,up100;
	protected JButton left100,left10,left1,right1,right10,right100;
	protected JButton home,center;
	protected JButton buttonStart,buttonStartAt,buttonPause,buttonHalt;
	protected JButton goTop,goBottom,goLeft,goRight,goUp,goDown;

	JFormattedTextField feedRate;
	JButton setFeedRate;
	
	// command line
	private JPanel textInputArea;
	private JTextField commandLineText;
	private JButton commandLineSend;
	private JButton disengageMotors;
	
	// to make sure pen isn't on the paper while the machine is paused
	private boolean penIsUp,penIsUpBeforePause;
	
	protected MultilingualSupport translator;
	protected MachineConfiguration machineConfiguration;
	protected MainGUI gui;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MakelangeloDriveControls() {
	}
	
	
	public void raisePen() {
		penIsUp=true;
	}
	
	public void lowerPen() {
		penIsUp=false;
	}
	
	public void updateButtonAccess(boolean isConnected,boolean isRunning) {
    	buttonStart.setEnabled(isConnected && !isRunning);
        buttonStartAt.setEnabled(isConnected && !isRunning);
        buttonPause.setEnabled(isConnected && isRunning);
        buttonHalt.setEnabled(isConnected && isRunning);
	}
	
	public void createPanel(MainGUI _gui,MultilingualSupport _translator,MachineConfiguration _machineConfiguration) {
		translator=_translator;
		gui=_gui;
		machineConfiguration = _machineConfiguration;
		
		GridBagConstraints c;

		this.setLayout(new GridLayout(0,1));
		this.setPreferredSize(new Dimension(150,100));
		
		this.removeAll();
		
	    // Draw menu
		JPanel go = new JPanel(new GridBagLayout());
	    	buttonStart = new JButton(translator.get("Start"));
	    	go.add(buttonStart);
	        buttonStartAt = new JButton(translator.get("StartAtLine"));
	        go.add(buttonStartAt);
	        buttonPause = new JButton(translator.get("Pause"));
	        go.add(buttonPause);
	        buttonHalt = new JButton(translator.get("Halt"));
	        go.add(buttonHalt);
		    buttonStart.addActionListener(this);
		    buttonStartAt.addActionListener(this);
		    buttonPause.addActionListener(this);
		    buttonHalt.addActionListener(this);
	
		
		JPanel axisControl = new JPanel(new GridBagLayout());
			final JLabel yAxis = new JLabel("Y");			yAxis.setPreferredSize(new Dimension(60,20));			yAxis.setHorizontalAlignment(SwingConstants.CENTER);
			down100 = new JButton("-100");	down100.setPreferredSize(new Dimension(60,20));
			down10 = new JButton("-10");		down10.setPreferredSize(new Dimension(60,20));
			down1 = new JButton("-1");		down1.setPreferredSize(new Dimension(60,20));
			up1 = new JButton("1");  			up1.setPreferredSize(new Dimension(60,20));
			up10 = new JButton("10");  		up10.setPreferredSize(new Dimension(60,20));
			up100 = new JButton("100");  		up100.setPreferredSize(new Dimension(60,20));
			
			final JLabel xAxis = new JLabel("X");			xAxis.setPreferredSize(new Dimension(60,20));		xAxis.setHorizontalAlignment(SwingConstants.CENTER);
			left100 = new JButton("-100");	left100.setPreferredSize(new Dimension(60,20));
			left10 = new JButton("-10");		left10.setPreferredSize(new Dimension(60,20));
			left1 = new JButton("-1");		left1.setPreferredSize(new Dimension(60,20));	
			right1 = new JButton("1");		right1.setPreferredSize(new Dimension(60,20));
			right10 = new JButton("10");		right10.setPreferredSize(new Dimension(60,20));
			right100 = new JButton("100");	right100.setPreferredSize(new Dimension(60,20));
	
			//final JButton find = new JButton("FIND HOME");	find.setPreferredSize(new Dimension(100,20));
			center = new JButton(translator.get("SetHome"));	center.setPreferredSize(new Dimension(100,20));
			home = new JButton(translator.get("GoHome"));		home.setPreferredSize(new Dimension(100,20));
			
			c = new GridBagConstraints();
			//c.fill=GridBagConstraints.BOTH; 
			c.gridx=0;  c.gridy=0;  axisControl.add(yAxis,c);
			c.gridx=1;	c.gridy=0;	axisControl.add(down100,c);
			c.gridx=2;	c.gridy=0;	axisControl.add(down10,c);
			c.gridx=3;	c.gridy=0;	axisControl.add(down1,c);
			c.gridx=4;	c.gridy=0;	axisControl.add(up1,c);
			c.gridx=5;	c.gridy=0;	axisControl.add(up10,c);
			c.gridx=6;	c.gridy=0;	axisControl.add(up100,c);
			
			c.gridx=0;  c.gridy=1;  axisControl.add(xAxis,c);
			c.gridx=1;	c.gridy=1;	axisControl.add(left100,c);
			c.gridx=2;	c.gridy=1;	axisControl.add(left10,c);
			c.gridx=3;	c.gridy=1;	axisControl.add(left1,c);
			c.gridx=4;	c.gridy=1;	axisControl.add(right1,c);
			c.gridx=5;	c.gridy=1;	axisControl.add(right10,c);
			c.gridx=6;	c.gridy=1;	axisControl.add(right100,c);
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
			center.addActionListener(this);
			home.addActionListener(this);
		
		
		JPanel corners = new JPanel();
			corners.setLayout(new GridBagLayout());
			goTop = new JButton(translator.get("Top"));			goTop.setPreferredSize(new Dimension(80,20));
			goBottom = new JButton(translator.get("Bottom"));	goBottom.setPreferredSize(new Dimension(80,20));
			goLeft = new JButton(translator.get("Left"));		goLeft.setPreferredSize(new Dimension(80,20));
			goRight = new JButton(translator.get("Right"));		goRight.setPreferredSize(new Dimension(80,20));
			goUp = new JButton(translator.get("PenUp"));		goUp.setPreferredSize(new Dimension(100,20));
			goDown = new JButton(translator.get("PenDown"));	goDown.setPreferredSize(new Dimension(100,20));
			c = new GridBagConstraints();
			c.gridx=3;  c.gridy=0;  corners.add(goTop,c);
			c.gridx=3;  c.gridy=1;  corners.add(goBottom,c);
			c.gridx=4;  c.gridy=0;  corners.add(goLeft,c);
			c.gridx=4;  c.gridy=1;  corners.add(goRight,c);
			c.insets = new Insets(0,5,0,0);
			c.gridx=5;  c.gridy=0;  corners.add(goUp,c);
			c.gridx=5;  c.gridy=1;  corners.add(goDown,c);
			c.gridx=6;	c.gridy=0;	corners.add(home,c);
			c.gridx=6;	c.gridy=1;	corners.add(center,c);
			c.insets = new Insets(0,0,0,0);
			goTop.addActionListener(this);
			goBottom.addActionListener(this);
			goLeft.addActionListener(this);
			goRight.addActionListener(this);
			goUp.addActionListener(this);
			goDown.addActionListener(this);
		
			
		JPanel feedRateControl = new JPanel();
		feedRateControl.setLayout(new GridBagLayout());
			c = new GridBagConstraints();
			feedRate = new JFormattedTextField(NumberFormat.getInstance());  feedRate.setPreferredSize(new Dimension(100,20));
			feedRate.setText(Double.toString(machineConfiguration.getFeedRate()));
			setFeedRate = new JButton(translator.get("Set"));
			disengageMotors = new JButton(translator.get("DisengageMotors"));
	
			c.gridx=3;  c.gridy=0;  feedRateControl.add(new JLabel(translator.get("Speed")),c);
			c.gridx=4;  c.gridy=0;  feedRateControl.add(feedRate,c);
			c.gridx=5;  c.gridy=0;  feedRateControl.add(new JLabel(translator.get("Rate")),c);
			c.gridx=6;  c.gridy=0;  feedRateControl.add(setFeedRate,c);
			c.gridx=7;  c.gridy=0;  feedRateControl.add(disengageMotors,c);
		
	
		this.add(go);
	    this.add(new JSeparator());
		this.add(axisControl);
		this.add(corners);
		this.add(feedRateControl);
	    this.add(new JSeparator());
	    this.add(getTextInputField());
	    
		setFeedRate.addActionListener(this);
		disengageMotors.addActionListener(this);
	}
    

	  public void actionPerformed(ActionEvent e) {
			Object subject = e.getSource();
			JButton b = (JButton)subject;

			
			if(gui.isFileLoaded() && !gui.isRunning()) {
				if( subject == buttonStart ) {
					gui.startAt(0);
					return;
				}
				if( subject == buttonStartAt ) {
					Long lineNumber = getStartingLineNumber();
					if(lineNumber != -1) {
						gui.startAt(lineNumber);
					}
					return;
					
				}
			}

			if( subject == commandLineSend ) {
				gui.sendLineToRobot(commandLineText.getText());
				commandLineText.setText("");
			}
			
			//if(gui.isRunning()) return;

			if( subject == buttonPause ) {
				if(gui.isPaused()==true) {
					if(!penIsUpBeforePause) {
						gui.lowerPen();
					}
					buttonPause.setText(translator.get("Pause"));
					gui.unPause();
					// TODO: if the robot is not ready to unpause, this might fail and the program would appear to hang.
					gui.sendFileCommand();
				} else {
					penIsUpBeforePause=penIsUp;
					gui.raisePen();
					buttonPause.setText(translator.get("Unpause"));
					gui.pause();
				}
				return;
			}
			if( subject == buttonHalt ) {
				gui.halt();
				return;
			}
			
			if(b==home) gui.sendLineToRobot("G00 F"+feedRate.getText()+" X0 Y0");
			else if(b==center) gui.sendLineToRobot("G92 X0 Y0");
			else if(b==goLeft) gui.sendLineToRobot("G00 F"+feedRate.getText()+" X"+(machineConfiguration.paper_left *10));
			else if(b==goRight) gui.sendLineToRobot("G00 F"+feedRate.getText()+" X"+(machineConfiguration.paper_right*10));
			else if(b==goTop) gui.sendLineToRobot("G00 F"+feedRate.getText()+" Y"+(machineConfiguration.paper_top*10));
			else if(b==goBottom) gui.sendLineToRobot("G00 F"+feedRate.getText()+" Y"+(machineConfiguration.paper_bottom*10));
			//} else if(b==find) {
			//	gui.SendLineToRobot("G28");
			else if(b==goUp) gui.raisePen();
			else if(b==goDown) gui.lowerPen();
			else if(b==setFeedRate) {
				String fr=feedRate.getText();
				fr=fr.replaceAll("[ ,]","");
				double feed_rate = Double.parseDouble(fr);
				if(feed_rate<0.001) feed_rate=0.001;
				machineConfiguration.setFeedRate(feed_rate);
				feedRate.setText(Double.toString(feed_rate));
				gui.sendLineToRobot("G00 G21 F"+feed_rate);
			} 
			else if(b==disengageMotors) gui.sendLineToRobot("M18");
			else {
				gui.sendLineToRobot("G91");  // set relative mode

				if(b==down100) gui.sendLineToRobot("G0 Y-100");
				if(b==down10) gui.sendLineToRobot("G0 Y-10");
				if(b==down1) gui.sendLineToRobot("G0 Y-1");
				if(b==up100) gui.sendLineToRobot("G0 Y100");
				if(b==up10) gui.sendLineToRobot("G0 Y10");
				if(b==up1) gui.sendLineToRobot("G0 Y1");

				if(b==left100) gui.sendLineToRobot("G0 X-100");
				if(b==left10) gui.sendLineToRobot("G0 X-10");
				if(b==left1) gui.sendLineToRobot("G0 X-1");
				if(b==right100) gui.sendLineToRobot("G0 X100");
				if(b==right10) gui.sendLineToRobot("G0 X10");
				if(b==right1) gui.sendLineToRobot("G0 X1");
				
				gui.sendLineToRobot("G90");  // return to absolute mode
			}
	  }

	private JPanel getTextInputField() {
		textInputArea = new JPanel(new GridLayout(0,1));
		commandLineText = new JTextField(0);
		commandLineText.setPreferredSize(new Dimension(10,10));
		commandLineSend = new JButton(translator.get("Send"));
		//commandLineSend.setHorizontalAlignment(SwingConstants.EAST);
		textInputArea.add(commandLineText);
		textInputArea.add(commandLineSend);
		
		commandLineText.addKeyListener(this);
		commandLineSend.addActionListener(this);

	    //textInputArea.setMinimumSize(new Dimension(100,50));
	    //textInputArea.setMaximumSize(new Dimension(10000,50));

		return textInputArea;
	}


	@Override
	public void keyTyped(KeyEvent e) {}

    /** Handle the key-pressed event from the text field. */
	@Override
    public void keyPressed(KeyEvent e) {}

    /** Handle the key-released event from the text field. */
    @Override
	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_ENTER) {
			gui.processLine(commandLineText.getText());
			commandLineText.setText("");
		}
	}
    


	/**
	 * open a dialog to ask for the line number.
	 * @return <code>lineNumber</code> greater than or equal to zero if user hit ok.
	 */
	private long getStartingLineNumber() {
		final JPanel panel = new JPanel(new GridBagLayout());		
		final JTextField starting_line = new JTextField("0",8);
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth=2;	c.gridx=0;  c.gridy=0;  panel.add(new JLabel(translator.get("StartAtLine")),c);
		c.gridwidth=2;	c.gridx=2;  c.gridy=0;  panel.add(starting_line,c);
		
	    int result = JOptionPane.showConfirmDialog(null, panel, translator.get("StartAt"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
	    if (result == JOptionPane.OK_OPTION) {
	    	long lineNumber;
	    	try {
	    		lineNumber = Long.decode(starting_line.getText());
	    	}
	    	catch(Exception e) {
	    		lineNumber = -1;
	    	}
	    	
	    	return lineNumber;
	    }
		return -1;
	}
}
