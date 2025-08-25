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
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;

public class ScaleTurtlePanel extends JPanel {
	private static final Logger logger = LoggerFactory.getLogger(ScaleTurtlePanel.class);
	
	private final String [] unitTypes = new String[]{"mm","%"};
	private final Turtle turtleToChange;
	private final Turtle turtleOriginal;
	private final JSpinner widthSpinner;
	private final JSpinner heightSpinner;
	private final JComboBox<String> units = new JComboBox<String>(unitTypes);
	private int currentUnitType=0; // 0=mm, 1=%
	private final JCheckBox lockRatio = new JCheckBox("🔒");
	private final Rectangle2D.Double myOriginalBounds;

	private double ratioAtTimeOfLock=1;
	private boolean ignoreChange=false;

	private double width,height;
	
	public ScaleTurtlePanel(Turtle t) {
		super();
		setName("ScaleTurtlePanel");
		turtleToChange = t;
		turtleOriginal = new Turtle(t);  // make a deep copy of the original.  Doubles memory usage!

		myOriginalBounds = turtleToChange.getBounds();
		width = myOriginalBounds.width;
		height = myOriginalBounds.height;

		widthSpinner = new JSpinner(new SpinnerNumberModel(width,null,null,1));
		heightSpinner = new JSpinner(new SpinnerNumberModel(height,null,null,1));
		
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets=new Insets(10,10,3,10);

		c.gridx=0;
		c.gridy=0;
		c.weightx=1;
		c.anchor=GridBagConstants.NORTHWEST;
		add(new JLabel(Translator.get("Width")),c);
		
		c.gridx=0;
		c.gridy=1;
		c.anchor=GridBagConstants.NORTHWEST;
		add(new JLabel(Translator.get("Height")),c);
		
		c.gridx=1;
		c.gridy=0;
		c.anchor=GridBagConstants.NORTHWEST;
		c.fill=GridBagConstants.HORIZONTAL;
		add(widthSpinner,c);
		widthSpinner.setName("width");

		c.gridx=1;
		c.gridy=1;
		c.anchor=GridBagConstants.NORTHWEST;
		c.fill=GridBagConstants.HORIZONTAL;
		add(heightSpinner,c);
		heightSpinner.setName("height");
		
		c.gridx=2;
		c.gridy=0;
		c.gridheight=2;
		c.anchor=GridBagConstants.CENTER;
		add(lockRatio,c);
		lockRatio.setName("lockRatio");
		
		c.gridx=3;
		c.gridy=0;
		c.gridheight=2;
		c.anchor=GridBagConstants.CENTER;
		add(units,c);
		units.setName("units");
		
		widthSpinner.addChangeListener(this::onWidthChange);
		heightSpinner.addChangeListener(this::onHeightChange);
		units.addActionListener(this::onUnitChange);
		lockRatio.addActionListener(e -> updateRatioAtTimeOfLock());
		lockRatio.setSelected(true);
		updateRatioAtTimeOfLock();

		updateMinimumWidth(widthSpinner);
		updateMinimumWidth(heightSpinner);
	}
	
	private void updateMinimumWidth(JSpinner spinner) {
		JComponent field = spinner.getEditor();
	    Dimension prefSize = field.getPreferredSize();
	    prefSize = new Dimension(80, prefSize.height);
	    field.setPreferredSize(prefSize);
	}

	private void onWidthChange(ChangeEvent e) {
		if(ignoreChange) return;
		ignoreChange = true;

		if(lockRatio.isSelected()) {
			width = (Double) widthSpinner.getValue();
			height = width / ratioAtTimeOfLock;
			heightSpinner.setValue(height);
		}

		ignoreChange = false;
		scaleNow();
	}

	private void onHeightChange(ChangeEvent e) {
		if(ignoreChange) return;
		ignoreChange = true;

		if(lockRatio.isSelected()) {
			height = (Double) heightSpinner.getValue();
			width = height * ratioAtTimeOfLock;
			widthSpinner.setValue(width);
		}

		ignoreChange = false;
		scaleNow();
	}
	
	private void scaleNow() {
		double ow = myOriginalBounds.getWidth();
		double oh = myOriginalBounds.getHeight();
		ow = (ow == 0) ? 1 : ow;
		oh = (oh == 0) ? 1 : oh;
		
		double w1 = (Double) widthSpinner.getValue();
		double h1 = (Double) heightSpinner.getValue();
		if(units.getSelectedIndex()==0) {
			// mm
			w1 /= ow;
			h1 /= oh;			
		} else {
			// %
			w1*=0.01;
			h1*=0.01;
		}

		logger.debug("scale {}x{} -> {}x{} units={}", ow, oh, w1, h1, units.getSelectedIndex());
		revertOriginalTurtle();
		turtleToChange.scale(w1, h1);
	}

	private void revertOriginalTurtle() {
		turtleToChange.set(turtleOriginal);
	}
	
	private void onUnitChange(ActionEvent e) {
		var choice = ((JComboBox)e.getSource()).getSelectedIndex();
		if(currentUnitType == choice) return;
		currentUnitType = choice;

		width = myOriginalBounds.getWidth();
		height = myOriginalBounds.getHeight();
		width = (width == 0) ? 1 : width;
		height = (height == 0) ? 1 : height;

		double w1 = (Double) widthSpinner.getValue();
		double h1 = (Double) heightSpinner.getValue();

		ignoreChange=true;
		if(units.getSelectedIndex()==0) {
			// switching to mm.  here w1,h1 are in % (0-100)
			widthSpinner .setValue(w1 * 0.01 * width);
			heightSpinner.setValue(h1 * 0.01 * height);
		} else {
			// switching to %.  here w1,h1 are in mm
			widthSpinner.setValue(100.0*(w1 / width));
			heightSpinner.setValue(100.0*(h1 / height));
		}
		updateRatioAtTimeOfLock();
		ignoreChange=false;
	}

	private void updateRatioAtTimeOfLock() {
		if(lockRatio.isSelected()) {
			ratioAtTimeOfLock = (Double) widthSpinner.getValue() / (Double) heightSpinner.getValue();
		}
	}

	public static void runAsDialog(Window parent,Turtle t) {
		ScaleTurtlePanel panel = new ScaleTurtlePanel(t);

		JDialog dialog = new JDialog(parent,Translator.get("Scale"));

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

		// make a Turtle of a rectangle
		Turtle turtle = new Turtle();
		turtle.jumpTo(0, 0);
		turtle.moveTo(100, 0);
		turtle.moveTo(100, 50);
		turtle.moveTo(0, 50);
		turtle.moveTo(0, 0);

		JFrame frame = new JFrame(ScaleTurtlePanel.class.getSimpleName());
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null);
		runAsDialog(frame,turtle);
	}
}
