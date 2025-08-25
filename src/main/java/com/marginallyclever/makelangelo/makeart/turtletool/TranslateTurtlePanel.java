package com.marginallyclever.makelangelo.makeart.turtletool;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.util.PreferencesHelper;
import org.apache.batik.ext.swing.GridBagConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class TranslateTurtlePanel extends JPanel {
	private static final Logger logger = LoggerFactory.getLogger(TranslateTurtlePanel.class);

	private final Turtle turtleToChange;
	private final Turtle turtleOriginal;
	private final JSpinner dx;
	private final JSpinner dy;
	private final Rectangle2D.Double myOriginalBounds;

	public TranslateTurtlePanel(Turtle t) {
		super();
		turtleToChange = t;
		turtleOriginal = new Turtle(t);  // make a deep copy of the original.  Doubles memory usage!

		myOriginalBounds = turtleToChange.getBounds();
		dx = new JSpinner(new SpinnerNumberModel(myOriginalBounds.getCenterX(),null,null,1));
		dy = new JSpinner(new SpinnerNumberModel(myOriginalBounds.getCenterY(),null,null,1));
		
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets=new Insets(10,10,3,10);

		c.gridx=0;
		c.gridy=0;
		c.weightx=1;
		c.anchor=GridBagConstants.NORTHWEST;
		add(new JLabel("X"),c);
		
		c.gridx=0;
		c.gridy=1;
		c.anchor=GridBagConstants.NORTHWEST;
		add(new JLabel("Y"),c);
		
		c.gridx=1;
		c.gridy=0;
		c.anchor=GridBagConstants.NORTHWEST;
		c.fill=GridBagConstants.HORIZONTAL;
		add(dx,c);

		c.gridx=1;
		c.gridy=1;
		c.anchor=GridBagConstants.NORTHWEST;
		c.fill=GridBagConstants.HORIZONTAL;
		add(dy,c);
		
		dx.addChangeListener(this::onChange);
		dy.addChangeListener(this::onChange);
	}

	private void onChange(ChangeEvent e) {
		double dx2 = (Double) dx.getValue() - myOriginalBounds.x;
		double dy2 = (Double) dy.getValue() - myOriginalBounds.y;

		logger.debug("move {}x{}", dx2, dy2);
		revertOriginalTurtle();
		turtleToChange.translate(dx2, dy2);
	}

	private void revertOriginalTurtle() {
		// reset original turtle to original scale.
		turtleToChange.set(turtleOriginal);
	}

	public static void runAsDialog(Window parent,Turtle t) {
		TranslateTurtlePanel panel = new TranslateTurtlePanel(t);

		JDialog dialog = new JDialog(parent,Translator.get("Translate"));

		JButton okButton = new JButton(Translator.get("OK"));
		JButton cancelButton = new JButton(Translator.get("Cancel"));

		JPanel outerPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.gridwidth=3;
		c.anchor=GridBagConstraints.NORTHWEST;
		c.fill=GridBagConstraints.BOTH;
		outerPanel.add(panel,c);

		c.gridx=1;
		c.gridy=1;
		c.gridwidth=1;
		c.weightx=1;
		outerPanel.add(okButton,c);
		c.gridx=2;
		c.gridwidth=1;
		c.weightx=1;
		outerPanel.add(cancelButton,c);
		
		okButton.addActionListener((e)-> dialog.dispose());
		cancelButton.addActionListener((e)-> {
			panel.revertOriginalTurtle();
			dialog.dispose();
		});
		
		dialog.add(outerPanel);
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);
	}
	
	// TEST
	
	public static void main(String[] args) {
		PreferencesHelper.start();
		Translator.start();

		JFrame frame = new JFrame(TranslateTurtlePanel.class.getSimpleName());
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null);
		runAsDialog(frame,new Turtle());
	}
}
