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
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.prefs.Preferences;

/**
 * Control UI for {@link LineWeightByImageIntensity}.  This is a panel that allows the user to select an image, set the
 * line weight parameters, and then generate a new turtle based on the original turtle and the image.
 */
public class LineWeightByImageIntensityPanel extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(LineWeightByImageIntensityPanel.class);
    private final EditorContext context;
    private final Turtle turtleOriginal;

    // source of weight image
    private static String imageName = null;

    private static double previousThickness = 3.0;
    private static double previousPenDiameter = 0.8;
    private static double previousStepSize = 1.0;

    private final JSpinner thicknessSpinner = new JSpinner(new SpinnerNumberModel(previousThickness, 0.1, 100.0, 0.1));
    private final JSpinner penDiameterSpinner = new JSpinner(new SpinnerNumberModel(previousPenDiameter, 0.01, 10.0, 0.01));
    private final JSpinner stepSizeSpinner = new JSpinner(new SpinnerNumberModel(previousStepSize, 0.1, 100.0, 0.1));

    public LineWeightByImageIntensityPanel(EditorContext context) {
        super(new GridBagLayout());
        setName("LineWeightByImageIntensityPanel");
        this.context = context;
        this.turtleOriginal = context.getTurtle();

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 3, 10);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.anchor = GridBagConstants.NORTHWEST;
        c.fill = GridBagConstants.NONE;

        JFileChooser fileChooser = new JFileChooser();
        String names = String.join(", ", SelectImageConverterPanel.IMAGE_FILE_EXTENSIONS);
        FileNameExtensionFilter images = new FileNameExtensionFilter(Translator.get("OpenFileChooser.FileTypeImage", new String[]{names}), SelectImageConverterPanel.IMAGE_FILE_EXTENSIONS);
        fileChooser.addChoosableFileFilter(images);

        Preferences preferences = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.FILE);
        String lastPath = preferences.get(OpenFileChooser.KEY_PREFERENCE_LOAD_PATH, FileAccess.getWorkingDirectory());
        fileChooser.setCurrentDirectory(new File(lastPath));

        add(new JLabel(Translator.get("LineWeightByImageIntensityPanel.image")), c);
        c.gridx = 1;
        c.fill = GridBagConstants.HORIZONTAL;
        JPanel imageSelectionPanel = new JPanel(new BorderLayout());
        JTextField textField = new JTextField();
        textField.setEditable(false);
        textField.setText(imageName);
        JButton selectButton = new JButton("...");
        imageSelectionPanel.add(textField, BorderLayout.CENTER);
        imageSelectionPanel.add(selectButton, BorderLayout.EAST);
        selectButton.addActionListener(e -> {
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                imageName = fileChooser.getSelectedFile().getAbsolutePath();
                textField.setText(imageName);
                preferences.put(OpenFileChooser.KEY_PREFERENCE_LOAD_PATH, fileChooser.getSelectedFile().getParent());
                generate();
            }
        });
        add(imageSelectionPanel, c);

        c.gridy++;
        c.gridx = 0;
        c.fill = GridBagConstants.NONE;
        add(new JLabel(Translator.get("LineWeightByImageIntensityPanel.thickness")), c);
        c.gridx = 1;
        c.fill = GridBagConstants.HORIZONTAL;
        add(thicknessSpinner, c);
        thicknessSpinner.setName("Thickness");
        thicknessSpinner.addChangeListener(e -> generate());

        c.gridy++;
        c.gridx = 0;
        c.fill = GridBagConstants.NONE;
        add(new JLabel(Translator.get("LineWeightByImageIntensityPanel.penDiameter")), c);
        c.gridx = 1;
        c.fill = GridBagConstants.HORIZONTAL;
        add(penDiameterSpinner, c);
        penDiameterSpinner.setName("Pen Diameter");
        penDiameterSpinner.addChangeListener(e -> generate());

        c.gridy++;
        c.gridx = 0;
        c.fill = GridBagConstants.NONE;
        add(new JLabel(Translator.get("LineWeightByImageIntensityPanel.stepSize")), c);
        c.gridx = 1;
        c.fill = GridBagConstants.HORIZONTAL;
        add(stepSizeSpinner, c);
        stepSizeSpinner.setName("Step Size");
        stepSizeSpinner.addChangeListener(e -> generate());
    }

    private void generate() {
        if (imageName == null || imageName.trim().isEmpty()) {
            return;
        }

        TransformedImage sourceImage;
        try (FileInputStream stream = new FileInputStream(imageName)) {
            sourceImage = new TransformedImage(ImageIO.read(stream));
        } catch (Exception e) {
            logger.error("failed to load intensity image. ", e);
            return;
        }

        TurtleGeneratorHelper.scaleImage(sourceImage, context.getPaper(), 1);

        previousThickness = ((Number) thicknessSpinner.getValue()).doubleValue();
        previousPenDiameter = ((Number) penDiameterSpinner.getValue()).doubleValue();
        previousStepSize = ((Number) stepSizeSpinner.getValue()).doubleValue();

        LineWeightByImageIntensity job = new LineWeightByImageIntensity();
        var newTurtle = job.run(turtleOriginal, sourceImage, previousStepSize, previousThickness, previousPenDiameter);
        context.setTurtle(newTurtle);
    }

    private void revertOriginalTurtle() {
        context.setTurtle(turtleOriginal);
    }

    public static void runAsDialog(Window parent, EditorContext context) {
        LineWeightByImageIntensityPanel panel = new LineWeightByImageIntensityPanel(context);
        JDialog dialog = new JDialog(parent, Translator.get("LineWeightByImageIntensityPanel.title"));

        JButton okButton = new JButton(Translator.get("OK"));
        JButton cancelButton = new JButton(Translator.get("Cancel"));

        JPanel outerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        outerPanel.add(panel, c);

        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        c.weightx = 1;
        outerPanel.add(okButton, c);
        c.gridx = 2;
        c.gridwidth = 1;
        c.weightx = 1;
        outerPanel.add(cancelButton, c);

        okButton.addActionListener((e) -> dialog.dispose());
        cancelButton.addActionListener((e) -> {
            panel.revertOriginalTurtle();
            dialog.dispose();
        });

        dialog.add(outerPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }
}
