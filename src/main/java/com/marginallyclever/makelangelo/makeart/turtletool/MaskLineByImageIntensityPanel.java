package com.marginallyclever.makelangelo.makeart.turtletool;

import com.marginallyclever.convenience.FileAccess;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.editorcontext.EditorContext;
import com.marginallyclever.makelangelo.makeart.TransformedImage;
import com.marginallyclever.makelangelo.makeart.imageconverter.SelectImageConverterPanel;
import com.marginallyclever.makelangelo.makeart.io.OpenFileChooser;
import com.marginallyclever.makelangelo.makeart.turtlegenerator.TurtleGeneratorHelper;
import com.marginallyclever.makelangelo.turtle.Turtle;
import com.marginallyclever.util.PreferencesHelper;
import org.apache.batik.ext.swing.GridBagConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.text.NumberFormat;
import java.util.prefs.Preferences;

public class MaskLineByImageIntensityPanel extends JPanel {
	private static final Logger logger = LoggerFactory.getLogger(MaskLineByImageIntensityPanel.class);
	private final EditorContext context;
	private final Turtle turtleOriginal;

	// source of weight image
	private static String imageName = null;

	private static double previousM=0;
	private static double previousT=127;
	private final JSpinner monteCarloSpinner = new JSpinner(new SpinnerNumberModel(previousM,0,100,1));;  // 0...100
	private final JSpinner thresholdSpinner = new JSpinner(new SpinnerNumberModel(previousT,0,255,1));;  // 0...255
	private long seed;

	public MaskLineByImageIntensityPanel(EditorContext context) {
		super(new GridBagLayout());
		setName("MaskLineByImageIntensityPanel");
		this.context = context;
		this.turtleOriginal = context.getTurtle();

		GridBagConstraints c = new GridBagConstraints();
		c.insets=new Insets(10,10,3,10);
		c.gridx=0;
		c.gridy=0;
		c.weightx=1;
		c.anchor= GridBagConstants.NORTHWEST;
		c.fill=GridBagConstants.NONE;

		JFileChooser fileChooser = new JFileChooser();
		// add all image formats
		String names = String.join(", ", SelectImageConverterPanel.IMAGE_FILE_EXTENSIONS);
		FileNameExtensionFilter images = new FileNameExtensionFilter(Translator.get("OpenFileChooser.FileTypeImage",new String[]{names}), SelectImageConverterPanel.IMAGE_FILE_EXTENSIONS);
		fileChooser.addChoosableFileFilter(images);

		// load the last path from preferences
		Preferences preferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.FILE);
		String lastPath = preferences.get(OpenFileChooser.KEY_PREFERENCE_LOAD_PATH, FileAccess.getWorkingDirectory());
		fileChooser.setCurrentDirectory(new File(lastPath));

		add(new JLabel(Translator.get("MaskLineByImageIntensityPanel.image")));
		c.gridx=1;
		c.fill=GridBagConstants.HORIZONTAL;
		// add a file selection for an image.
		JPanel imageSelectionPanel = new JPanel(new BorderLayout());
		JTextField textField = new JTextField();
		textField.setEditable(false);
		textField.setText(imageName);
		JButton selectButton = new JButton("...");
		imageSelectionPanel.add(textField, BorderLayout.CENTER);
		imageSelectionPanel.add(selectButton, BorderLayout.EAST);
		selectButton.addActionListener( e->{
			if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				imageName = fileChooser.getSelectedFile().getAbsolutePath();
				textField.setText(imageName);
				preferences.put(OpenFileChooser.KEY_PREFERENCE_LOAD_PATH, fileChooser.getSelectedFile().getParent());
				generate();
			}
		});
		add(imageSelectionPanel);

		c.gridy++;
		c.gridx=0;
		c.fill=GridBagConstants.NONE;
		add(new JLabel(Translator.get("MaskLineByImageIntensityPanel.seed")),c);
		c.gridx=1;
		c.fill=GridBagConstants.HORIZONTAL;
		JFormattedTextField seedTextField = new JFormattedTextField(getNumberFormatterInt());
		seedTextField.setValue(seed);
		seedTextField.setColumns(6);
		seedTextField.setMinimumSize(new Dimension(0,20));
		seedTextField.setName("Seed");
		seedTextField.addPropertyChangeListener("value",e -> generate() );
		add(seedTextField,c);

		c.gridy++;
		c.gridx=0;
		c.fill=GridBagConstants.NONE;
		add(new JLabel(Translator.get("MaskLineByImageIntensityPanel.threshold")),c);
		c.gridx=1;
		c.fill=GridBagConstants.HORIZONTAL;
		add(thresholdSpinner,c);
		thresholdSpinner.setName("Threshold");
		thresholdSpinner.addChangeListener(e -> generate() );

		c.gridy++;
		c.gridx=0;
		c.fill=GridBagConstants.NONE;
		add(new JLabel(Translator.get("MaskLineByImageIntensityPanel.monteCarlo")),c);
		c.gridx=1;
		c.fill=GridBagConstants.HORIZONTAL;
		add(monteCarloSpinner,c);
		monteCarloSpinner.setName("Monte carlo");
		monteCarloSpinner.addChangeListener(e -> generate() );
	}

	public NumberFormatter getNumberFormatterInt() {
		NumberFormat format = NumberFormat.getIntegerInstance();
		NumberFormatter formatter = new NumberFormatter(format);
		//formatter.setValueClass(Integer.class);
		formatter.setAllowsInvalid(true);
		formatter.setCommitsOnValidEdit(true);
		return formatter;
	}

	private void generate() {
		TransformedImage sourceImage;

		// load intensity image
		try (FileInputStream stream = new FileInputStream(imageName)) {
			sourceImage = new TransformedImage(ImageIO.read(stream));
		} catch(Exception e) {
			logger.error("failed to load intensity image. ",e);
			return;
		}

		TurtleGeneratorHelper.scaleImage(sourceImage,context.getPaper(),1);  // fill paper

		previousT = ((Number) thresholdSpinner.getValue()).doubleValue();
		previousM = ((Number) monteCarloSpinner.getValue()).doubleValue();

		logger.debug("mask t={} m={}", previousT,previousM);
		MaskLineByImageIntensity job = new  MaskLineByImageIntensity();
		var newTurtle = job.run(turtleOriginal,sourceImage,previousT,previousM,seed);
		context.setTurtle(newTurtle);
	}


	private void revertOriginalTurtle() {
		context.setTurtle(turtleOriginal);
	}

	public static void runAsDialog(Window parent, EditorContext context) {
		MaskLineByImageIntensityPanel panel = new MaskLineByImageIntensityPanel(context);

		JDialog dialog = new JDialog(parent,Translator.get("MaskLineByImageIntensityPanel.title"));

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

		EditorContext context = new EditorContext();

		// make a Turtle of a rectangle
		Turtle turtle = context.getTurtle();
		turtle.jumpTo(0, 0);
		turtle.moveTo(100, 0);
		turtle.moveTo(100, 50);
		turtle.moveTo(0, 50);
		turtle.moveTo(0, 0);

		JFrame frame = new JFrame(MaskLineByImageIntensityPanel.class.getSimpleName());
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null);
		runAsDialog(frame,context);
	}
}
