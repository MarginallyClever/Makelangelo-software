package com.marginallyclever.makelangelo;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.marginallyclever.makelangelo.paper.Paper;
import com.marginallyclever.makelangelo.plotter.Plotter;

public class ActionPaperBorder extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Plotter myPlotter;
	private Paper myPaper;
	
	public ActionPaperBorder(Plotter plotter,Paper paper,String name) {
		super(name);
		myPlotter=plotter;
		myPaper=paper;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		myPlotter.movePenAbsolute(myPaper.getLeft(),myPaper.getTop());
		myPlotter.lowerPen();
		myPlotter.movePenAbsolute(myPaper.getRight(),myPaper.getTop());
		myPlotter.movePenAbsolute(myPaper.getRight(),myPaper.getBottom());
		myPlotter.movePenAbsolute(myPaper.getLeft(),myPaper.getBottom());
		myPlotter.movePenAbsolute(myPaper.getLeft(),myPaper.getTop());
		myPlotter.raisePen();
		myPlotter.goHome();
	}
}
