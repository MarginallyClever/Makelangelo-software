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

public class RotateTurtlePanel extends JPanel {
	private static final Logger logger = LoggerFactory.getLogger(RotateTurtlePanel.class);
	private final Turtle turtleToChange;
	private final Turtle turtleOriginal;
	private final JSpinner degrees = new JSpinner(new SpinnerNumberModel(0, -360, 360, 1));

	public RotateTurtlePanel(Turtle t) {
		super();
		turtleToChange = t;
		turtleOriginal = new Turtle(t);  // make a deep copy of the original.

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets=new Insets(10,10,3,10);

		c.gridx=0;
		c.gridy=0;
		c.weightx=1;
		c.gridheight=1;
		c.anchor=GridBagConstants.CENTER;
		add(degrees,c);
		
		c.gridx=1;
		c.gridy=0;
		c.gridheight=2;
		c.anchor=GridBagConstants.CENTER;
		add(new JLabel("°"),c);

		degrees.addChangeListener(this::onAngleChange);
	}
	
	private void updateMinimumWidth(JSpinner spinner) {
		JComponent field = spinner.getEditor();
	    Dimension prefSize = field.getPreferredSize();
	    prefSize = new Dimension(80, prefSize.height);
	    field.setPreferredSize(prefSize);
	}

	private void onAngleChange(ChangeEvent e) {
		double angle = 0;
		try {
			angle = Double.parseDouble(degrees.getValue().toString());
		} catch(NumberFormatException err) {
			logger.error("Failed to parse angle", err);
		}

		logger.debug("rotate {}", angle);
		revertOriginalTurtle();
		turtleToChange.rotate(angle);
	}

	private void revertOriginalTurtle() {
		turtleToChange.set(turtleOriginal);
	}

	public static void runAsDialog(Window parent,Turtle t) {
		RotateTurtlePanel panel = new RotateTurtlePanel(t);

		JDialog dialog = new JDialog(parent,Translator.get("Rotate"));

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

		JFrame frame = new JFrame(RotateTurtlePanel.class.getSimpleName());
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null);
		runAsDialog(frame,new Turtle());
	}
}
