package com.marginallyclever.makelangelo.robot;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class PaperBorderAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	RobotController robot;
	
	public PaperBorderAction(RobotController robot,String text) {
		super(text);
		this.robot=robot;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		robot.movePenToEdgeTop();
		robot.lowerPen();
		robot.movePenToEdgeRight();
		robot.movePenToEdgeBottom();
		robot.movePenToEdgeLeft();
		robot.movePenToEdgeTop();
		robot.movePenAbsolute(0, robot.getPenY());
		robot.raisePen();
		robot.goHome();
	}

}
