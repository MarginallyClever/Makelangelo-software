package com.marginallyclever.makelangeloRobot;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class PaperBorderAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	MakelangeloRobot robot;
	
	public PaperBorderAction(MakelangeloRobot robot,String text) {
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
