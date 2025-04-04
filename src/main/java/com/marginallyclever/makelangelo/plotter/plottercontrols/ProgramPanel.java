package com.marginallyclever.makelangelo.plotter.plottercontrols;

import com.marginallyclever.convenience.CommandLineOptions;
import com.marginallyclever.convenience.helpers.StringHelper;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.plotter.Plotter;
import com.marginallyclever.makelangelo.turtle.Line2d;
import com.marginallyclever.makelangelo.turtle.StrokeLayer;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.util.PreferencesHelper;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.vecmath.Point2d;
import java.awt.*;

/**
 * {@link ProgramPanel} maintains a buffer of commands to be sent to a
 * {@link Plotter}. The currently selected element in the {@link JList} is the
 * "play head" of the recording.
 * 
 * @author Dan Royer
 * @since 7.28.0
 */
public class ProgramPanel extends JPanel {
	private final Plotter myPlotter;
	private final Turtle myTurtle;
	private final DefaultListModel<String> listModel = new DefaultListModel<>();
	private final JList<String> listView = new JList<>(listModel);

	public ProgramPanel(Plotter plotter, Turtle turtle) {
		super(new BorderLayout());
		myPlotter = plotter;
		myTurtle = turtle;

		createCellRenderingSystem();

		JScrollPane scrollPane = new JScrollPane(listView);
		scrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		listView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listView.setMaximumSize(new Dimension(300, Integer.MAX_VALUE));

		// this.add(getToolBar(), BorderLayout.PAGE_START);
		this.add(scrollPane, BorderLayout.CENTER);

		addTurtleToList(turtle);
	}

	private void addTurtleToList(@Nonnull Turtle turtle) {
		StrokeLayer iLayer = null;
		Line2d iLine = null;

		var iter = turtle.getIterator();
		while (iter.hasNext()) {
			Point2d p = iter.next();
			if(iLine != iter.getLine()) {
				iLine = iter.getLine();
				if(iLayer != iter.getLayer()) {
					iLayer = iter.getLayer();
					var c = iLayer.getColor();
					var d = iLayer.getDiameter();
					listModel.addElement("TOOL"
									+ " R" + c.getRed()
									+ " G" + c.getGreen()
									+ " B" + c.getBlue()
									+ " A" + c.getAlpha()
									+ " D" + StringHelper.formatDouble(d));
				}
				// travel to new line.
				listModel.addElement("TRAVEL"
						+ " X" + StringHelper.formatDouble(p.x)
						+ " Y" + StringHelper.formatDouble(p.y));
				continue;
			}
			listModel.addElement("DRAW_LINE"
					+ " X" + StringHelper.formatDouble(p.x)
					+ " Y" + StringHelper.formatDouble(p.y));
		}
	}

	private void createCellRenderingSystem() {
		listView.setCellRenderer(new ListCellRenderer<>() {
			private final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

			@Override
			public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
					boolean isSelected, boolean cellHasFocus) {
				Component c = defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (c instanceof JLabel jc) {
					jc.setText(value);
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
		return myTurtle.countPoints();
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

		var move = listModel.get(now);
		if(move.startsWith("TRAVEL")) {
			myPlotter.raisePen();
			moveToCoordinates(move.substring(7));
			myPlotter.lowerPen();
		} else if(move.startsWith("DRAW_LINE")) {
			moveToCoordinates(move.substring(9));
		} else if(move.startsWith("TOOL")) {
			String color = move.substring(5);
			String [] parts = color.split(" ");
			int r = Integer.parseInt(parts[0].substring(2));
			int g = Integer.parseInt(parts[1].substring(2));
			int b = Integer.parseInt(parts[2].substring(2));
			int a = Integer.parseInt(parts[3].substring(2));
			// ignore diameter
			myPlotter.requestUserChangeTool(new Color(r, g, b, a).getRGB());
		}

		int selected = listView.getSelectedIndex();
		listView.ensureIndexIsVisible(selected);
		if (selected == now) {
			// could not advance. reached the end.
			listView.clearSelection();
			myPlotter.raisePen();
		}
	}

	private void moveToCoordinates(String coordinates) {
		String [] parts = coordinates.split(" ");
		double x = Double.parseDouble(parts[0].substring(2));
		double y = Double.parseDouble(parts[1].substring(2));
		myPlotter.setPos(x,y);
	}

	// TEST

	public static void main(String[] args) {
		PreferencesHelper.start();
		CommandLineOptions.setFromMain(args);
		Translator.start();

		JFrame frame = new JFrame(ProgramPanel.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new ProgramPanel(new Plotter(), new Turtle()));
		frame.pack();
		frame.setVisible(true);
	}
}
