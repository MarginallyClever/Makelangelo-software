package com.marginallyclever.makelangelo;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.marginallyclever.makelangelo.paper.Paper;

public class ActionPaperBorder extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private RobotController myController;
	
	public ActionPaperBorder(RobotController robotController,String name) {
		super(name);
		this.myController=robotController;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		Paper paper = myController.getPaper();
		myController.myPlotter.movePenAbsolute(paper.getLeft(),paper.getTop());
		myController.myPlotter.lowerPen();
		myController.myPlotter.movePenAbsolute(paper.getRight(),paper.getTop());
		myController.myPlotter.movePenAbsolute(paper.getRight(),paper.getBottom());
		myController.myPlotter.movePenAbsolute(paper.getLeft(),paper.getBottom());
		myController.myPlotter.movePenAbsolute(paper.getLeft(),paper.getTop());
		myController.myPlotter.raisePen();
		myController.myPlotter.goHome();
	}
}
