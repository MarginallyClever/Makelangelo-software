package com.marginallyclever.makelangelo.plotter.plotterControls;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;

import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleMove;
import com.marginallyclever.util.PreferencesHelper;

public class ProgramInterface extends JPanel {
	private static final long serialVersionUID = -7719350277524271664L;
	private Plotter myPlotter;
	private Turtle myTurtle;
	private DefaultListModel<TurtleMove> listModel = new DefaultListModel<TurtleMove>();
	private JList<TurtleMove> listView = new JList<TurtleMove>(listModel);

	public ProgramInterface(Plotter plotter,Turtle turtle) {
		super();
		myPlotter=plotter;
		myTurtle=turtle;
		
		createCellRenderingSystem();
		
		JScrollPane scrollPane = new JScrollPane(listView);
		scrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		
		listView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listView.setMaximumSize(new Dimension(300,Integer.MAX_VALUE));
		
		this.setLayout(new BorderLayout());
		//this.add(getToolBar(), BorderLayout.PAGE_START);
		this.add(scrollPane, BorderLayout.CENTER);

		addTurtleToList(turtle);
	}
	
	private void addTurtleToList(Turtle turtle) {
		listModel.addAll(turtle.history);
	}

	private void createCellRenderingSystem() {
		listView.setCellRenderer(new ListCellRenderer<TurtleMove>() {
			private DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer(); 
			
			@Override
			public Component getListCellRendererComponent(JList<? extends TurtleMove> list,
					TurtleMove value, int index, boolean isSelected, boolean cellHasFocus) {
				Component c = defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				
				if(c instanceof JLabel) {
					JLabel jc = (JLabel)c;
					jc.setText(value.toString());
				}
				return c;
			}
			
		});
	}

	public void setLineNumber(int lineNumber) {
		listView.setSelectedIndex(lineNumber);
	}

	public int getMoveCount() {
		return myTurtle.history.size();
	}
	
	public void rewind() {
		listView.setSelectedIndex(0);
	}

	public void step() {
		int now = listView.getSelectedIndex();
		if(now==-1) return;
		
		// Increment the line as soon as possible so that step() does not get called twice on the same line.
		listView.setSelectedIndex(now+1);
		
		TurtleMove move = listModel.get(now);
		//Log.message("Step to ("+now+"):"+move.toString());
		myPlotter.turtleMove(move);
		
		int selected = listView.getSelectedIndex();
		listView.ensureIndexIsVisible(selected);
		if(selected == now) {
			// could not advance. reached the end.
			listView.clearSelection();
			myPlotter.raisePen();
		}
	}
	
	public int getLineNumber() {
		return listView.getSelectedIndex();
	}
	
	// TEST
	
	public static void main(String[] args) {
		Log.start();
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();
		
		JFrame frame = new JFrame(ProgramInterface.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new ProgramInterface(new Plotter(),new Turtle()));
		frame.pack();
		frame.setVisible(true);
	}
}
