package com.marginallyclever.makelangelo.plotter.plotterControls;

import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.makelangelo.turtle.TurtleMove;
import com.marginallyclever.util.PreferencesHelper;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;

/**
 * {@link ProgramInterface} maintains a buffer of commands to be sent to a
 * {@link Plotter}. The currently selected element in the {@link JList} is the
 * "play head" of the recording.
 * 
 * @author Dan Royer
 * @since 7.28.0
 */
public class ProgramInterface extends JPanel {
	private static final long serialVersionUID = -7719350277524271664L;
	private Plotter myPlotter;
	private Turtle myTurtle;
	private DefaultListModel<TurtleMove> listModel = new DefaultListModel<TurtleMove>();
	private JList<TurtleMove> listView = new JList<TurtleMove>(listModel);

	public ProgramInterface(Plotter plotter, Turtle turtle) {
		super();
		myPlotter = plotter;
		myTurtle = turtle;

		createCellRenderingSystem();

		JScrollPane scrollPane = new JScrollPane(listView);
		scrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		listView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listView.setMaximumSize(new Dimension(300, Integer.MAX_VALUE));

		this.setLayout(new BorderLayout());
		// this.add(getToolBar(), BorderLayout.PAGE_START);
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
			public Component getListCellRendererComponent(JList<? extends TurtleMove> list, TurtleMove value, int index,
					boolean isSelected, boolean cellHasFocus) {
				Component c = defaultRenderer.getListCellRendererComponent(list, value, index, isSelected,
						cellHasFocus);

				if (c instanceof JLabel) {
					JLabel jc = (JLabel) c;
					jc.setText(value.toString());
				}
				return c;
			}

		});
	}

	/**
	 * Move the play head to the lineNumber-th instruction.  
	 * Does not tell the {@link Plotter} to do anything.
	 */
	public void setLineNumber(int lineNumber) {
		listView.setSelectedIndex(lineNumber);
	}

	/**
	 * @return the currently selected instruction.
	 */
	public int getLineNumber() {
		return listView.getSelectedIndex();
	}

	/**
	 * @return the total number of instructions in the buffer.
	 */
	public int getMoveCount() {
		return myTurtle.history.size();
	}

	/**
	 * Move the play head to the first item in the list.  
	 * Does not tell the {@link Plotter} to do anything.
	 */
	public void rewind() {
		setLineNumber(0);
	}

	/**
	 * Tell the {@link Plotter} to move to the currently selected instruction and
	 * advance the selected instruction by one. If there are no further instructions
	 * the selection is nullified.
	 */
	public void step() {
		int now = listView.getSelectedIndex();
		if (now == -1)
			return;

		// Increment the line as soon as possible so that step() does not get called
		// twice on the same line.
		listView.setSelectedIndex(now + 1);

		TurtleMove move = listModel.get(now);
		// Log.message("Step to ("+now+"):"+move.toString());
		myPlotter.turtleMove(move);

		int selected = listView.getSelectedIndex();
		listView.ensureIndexIsVisible(selected);
		if (selected == now) {
			// could not advance. reached the end.
			listView.clearSelection();
			myPlotter.raisePen();
		}
	}

	// TEST

	public static void main(String[] args) {
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();

		JFrame frame = new JFrame(ProgramInterface.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new ProgramInterface(new Plotter(), new Turtle()));
		frame.pack();
		frame.setVisible(true);
	}
}
