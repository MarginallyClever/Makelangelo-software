package com.marginallyclever.makelangelo.makeart.turtletool;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.editorcontext.EditorContext;
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

	private final EditorContext context;
	private final Turtle turtleOriginal;
	private final JSpinner xSpinner;
	private final JSpinner ySpinner;
	private final Rectangle2D.Double myOriginalBounds;

	public TranslateTurtlePanel(EditorContext context) {
		super();
		this.context = context;
		turtleOriginal = new Turtle(context.getTurtle());  // make a deep copy of the original.  Doubles memory usage!

		myOriginalBounds = turtleOriginal.getBounds();
		var cx = myOriginalBounds.getCenterX();
		var cy = myOriginalBounds.getCenterY();
		xSpinner = new JSpinner(new SpinnerNumberModel(cx,null,null,1));
		ySpinner = new JSpinner(new SpinnerNumberModel(cy,null,null,1));

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
		add(xSpinner,c);

		c.gridx=1;
		c.gridy=1;
		c.anchor=GridBagConstants.NORTHWEST;
		c.fill=GridBagConstants.HORIZONTAL;
		add(ySpinner,c);
		
		xSpinner.addChangeListener(this::onChange);
		ySpinner.addChangeListener(this::onChange);
	}

	private void onChange(ChangeEvent e) {
		double nx = (Double) xSpinner.getValue();
		double ny = (Double) ySpinner.getValue();
		var cx = myOriginalBounds.getCenterX();
		var cy = myOriginalBounds.getCenterY();
		double dx2 = nx - cx;
		double dy2 = ny - cy;

		logger.debug("move {}x{}", dx2, dy2);
		Turtle temp = new Turtle(turtleOriginal);
		temp.translate(dx2, dy2);
		context.setTurtle(temp);
	}

	private void revertOriginalTurtle() {
		// reset original turtle to original scale.
		context.setTurtle(turtleOriginal);
	}

	public static void runAsDialog(Window parent,EditorContext context) {
		TranslateTurtlePanel panel = new TranslateTurtlePanel(context);

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
		runAsDialog(frame,new EditorContext());
	}
}
