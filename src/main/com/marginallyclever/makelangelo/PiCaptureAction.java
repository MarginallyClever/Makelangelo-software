package com.marginallyclever.makelangelo;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import com.hopding.jrpicam.RPiCamera;
import com.hopding.jrpicam.enums.AWB;
import com.hopding.jrpicam.enums.DRC;
import com.hopding.jrpicam.enums.Encoding;
import com.hopding.jrpicam.enums.Exposure;
import com.hopding.jrpicam.exceptions.FailedToRunRaspistillException;
import com.marginallyclever.core.log.Log;

/**
 * Raspi camera capture to file for image processing
 */
public class PiCaptureAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    private static final int BUTTON_HEIGHT = 25;

	private RPiCamera piCamera;
	
	// picam controls
	private JButton	buttonCaptureImage, buttonUseCapture, buttonCancelCapture;
	private Makelangelo makelangeloApp;
	private BufferedImage buffImg;
	private boolean useImage;
	private int awb, drc, exp, contrast, quality, sharpness;
	

	public PiCaptureAction(Makelangelo gui,String text) throws FailedToRunRaspistillException {
		super(text);
		makelangeloApp=gui;
        setup();
	}
	
    public PiCaptureAction(Makelangelo gui,String text, ImageIcon icon,
                      String desc, Integer mnemonic) throws FailedToRunRaspistillException {
        super(text, icon);
        putValue(SHORT_DESCRIPTION, desc);
        putValue(MNEMONIC_KEY, mnemonic);
		makelangeloApp=gui;
        setup();
    }
	
    protected void setup() throws FailedToRunRaspistillException {
		// Create a piCamera
		piCamera = new RPiCamera("/home/pi/Pictures");

		// set the initial parameter settings.
		awb = 1;  // Auto
		drc = 1;  // High
		exp = 11; // VeryLong
		contrast = 0;
		quality = 75;
		sharpness = 0;
    }
    
	@Override
	public void actionPerformed(ActionEvent arg0) {
        // let's make the image the correct width and height for the paper
		useImage = false;
        int captureH = 650;
        double aspectRatio = makelangeloApp.getRobot().getPaper().getWidth() 
        					/ makelangeloApp.getRobot().getPaper().getHeight();
        int captureW = (int) ((double) captureH * aspectRatio);

		JDialog dialog = new JDialog(makelangeloApp.getMainFrame(),Translator.get("CaptureImageTitle"), true);
        dialog.setLocation(makelangeloApp.getMainFrame().getLocation());

        final JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		final GridBagConstraints cMain = new GridBagConstraints();
		cMain.fill=GridBagConstraints.HORIZONTAL;
		cMain.anchor=GridBagConstraints.NORTH;
		cMain.gridx=0;
		cMain.gridy=0;
        cMain.gridheight = 1;
        cMain.gridwidth = 1;

		// create a frame to adjust the image

		panel.setBounds(1024, 100, 700, captureH);

        // if you add more things to the right side, you must increase this.
        cMain.gridheight = 16;
        JLabel imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(captureW, captureH));
  		panel.add(imageLabel, cMain);
        cMain.gridheight = 1;

        // all controls to the right
		cMain.gridx++;

		JLabel label = new JLabel(Translator.get("AWB"));
		label.setPreferredSize(new Dimension(100,BUTTON_HEIGHT));
		panel.add(label, cMain);
		cMain.gridy++;

		String[] awbComboBoxChoices = {
		        Translator.get("Off"),
                Translator.get("Auto"),
                Translator.get("Sun"),
                Translator.get("Cloud"),
                Translator.get("Shade"),
                Translator.get("Tungsten"),
                Translator.get("Fluorescent"),
                Translator.get("Incandescent"),
                Translator.get("Flash"),
                Translator.get("Horizon") };
		JComboBox<String> awbComboBox = new JComboBox<>(awbComboBoxChoices);
		awbComboBox.setPreferredSize(new Dimension(100,BUTTON_HEIGHT));
		awbComboBox.setSelectedIndex(awb);
		panel.add(awbComboBox, cMain);
		cMain.gridy++;

		JLabel lblNewLabel = new JLabel(Translator.get("DRC"));
		lblNewLabel.setPreferredSize(new Dimension(100,BUTTON_HEIGHT));
		panel.add(lblNewLabel, cMain);
		cMain.gridy++;

		String[] drcComboBoxChoices = {
                Translator.get("Off"),
                Translator.get("High"),
                Translator.get("Medium"),
                Translator.get("Low") };
		JComboBox<String> drcComboBox = new JComboBox<>(drcComboBoxChoices);
		drcComboBox.setPreferredSize(new Dimension(100,BUTTON_HEIGHT));
		drcComboBox.setSelectedIndex(drc);
		panel.add(drcComboBox, cMain);
		cMain.gridy++;

		JLabel label_1 = new JLabel(Translator.get("Exposure"));
		label_1.setPreferredSize(new Dimension(100,BUTTON_HEIGHT));
		panel.add(label_1, cMain);
		cMain.gridy++;

		String[] expComboBoxChoices = {
                Translator.get("Antishake"),
                Translator.get("Auto"),
                Translator.get("Backlight"),
                Translator.get("Beach"),
                Translator.get("Fireworks"),
                Translator.get("FixedFPS"),
                Translator.get("Night"),
                Translator.get("NightPreview"),
                Translator.get("Snow"),
                Translator.get("Sports"),
                Translator.get("Spotlight"),
                Translator.get("Verylong") };
		JComboBox<String> expComboBox = new JComboBox<>(expComboBoxChoices);
//		expComboBox.setBounds(584, 362, 90, 20);
		expComboBox.setPreferredSize(new Dimension(100,BUTTON_HEIGHT));
		expComboBox.setSelectedIndex(exp);
		panel.add(expComboBox, cMain);
		cMain.gridy++;

		JLabel lblContrast = new JLabel(Translator.get("Contrast"));
//		lblContrast.setBounds(588, 393, 67, 14);
		lblContrast.setPreferredSize(new Dimension(100,BUTTON_HEIGHT));
		panel.add(lblContrast, cMain);
		cMain.gridy++;

		JSlider contrastSlider = new JSlider();
		contrastSlider.setMinimum(-100);
//		contrastSlider.setBounds(588, 418, 90, 23);
		contrastSlider.setValue(contrast);
		panel.add(contrastSlider, cMain);
		cMain.gridy++;

		JLabel lblQuality = new JLabel(Translator.get("Quality"));
//		lblQuality.setBounds(588, 452, 46, 14);
		lblQuality.setPreferredSize(new Dimension(100,BUTTON_HEIGHT));
		panel.add(lblQuality, cMain);
		cMain.gridy++;

		JSlider qualitySlider = new JSlider();
		qualitySlider.setValue(quality);
//		qualitySlider.setBounds(584, 477, 90, 29);
		panel.add(qualitySlider, cMain);
		cMain.gridy++;

		JLabel lblSharpness = new JLabel(Translator.get("Sharpness"));
//		lblSharpness.setBounds(585, 517, 66, 14);
		lblSharpness.setPreferredSize(new Dimension(100,BUTTON_HEIGHT));
		panel.add(lblSharpness, cMain);
		cMain.gridy++;

		JSlider sharpnessSlider = new JSlider();
		sharpnessSlider.setMinimum(-100);
		sharpnessSlider.setValue(sharpness);
//		sharpnessSlider.setBounds(588, 542, 90, 23);
		panel.add(sharpnessSlider, cMain);
		cMain.gridy++;

		// I need 3 buttons one for Capture and one for Use if we have captured an image and one to just Cancel

        // a little space between everything else
        cMain.insets = new Insets(10,0,0,0);  //top padding

        buttonCaptureImage = new JButton(Translator.get("CaptureImage"));
		buttonCaptureImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					piCamera.turnOnPreview(
							makelangeloApp.getMainFrame().getLocationOnScreen().x + 50,
							makelangeloApp.getMainFrame().getLocationOnScreen().y + 100,
							captureW,
							captureH);
					piCamera.setAWB(AWB.valueOf(((String) awbComboBox.getSelectedItem()).toUpperCase()));
					piCamera.setDRC(DRC.valueOf(((String) drcComboBox.getSelectedItem()).toUpperCase()));
					piCamera.setExposure(Exposure.valueOf(((String) expComboBox.getSelectedItem()).toUpperCase()));
					piCamera.setEncoding(Encoding.JPG);
					piCamera.setWidth(captureW);
					piCamera.setHeight(captureH);
					piCamera.setContrast(contrastSlider.getValue());
					piCamera.setQuality(qualitySlider.getValue());
					piCamera.setSharpness(sharpnessSlider.getValue());
					piCamera.setTimeout(3000);
					buffImg = piCamera.takeBufferedStill();
					Log.message("Executed this command:\n\t" + piCamera.getPrevCommand());
					ImageIcon icon = new ImageIcon(buffImg);
					imageLabel.setIcon(icon);
					buttonUseCapture.setEnabled(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		buttonCaptureImage.setPreferredSize(new Dimension(89, BUTTON_HEIGHT));
		panel.add(buttonCaptureImage, cMain);
		cMain.gridy++;
        cMain.insets = new Insets(2,0,0,0);  //top padding

		buttonUseCapture = new JButton(Translator.get("UseCapture"));
		buttonUseCapture.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// we like this image, save off the parameters used.
				awb = awbComboBox.getSelectedIndex();
				drc = drcComboBox.getSelectedIndex();
				exp = expComboBox.getSelectedIndex();
				contrast = contrastSlider.getValue();
				quality = qualitySlider.getValue();
				sharpness = sharpnessSlider.getValue();

				File saveFile = new File("/home/pi/Pictures/capture.jpg");
				try {
					ImageIO.write(buffImg, "jpg", saveFile);
					useImage = true;
				} catch (IOException e) {
					e.printStackTrace();
				}

				dialog.dispose();
			};
		});
		buttonUseCapture.setPreferredSize(new Dimension(89, BUTTON_HEIGHT));
		buttonUseCapture.setEnabled(false);
		panel.add(buttonUseCapture, cMain);
		cMain.gridy++;

		buttonCancelCapture = new JButton(Translator.get("CancelCapture"));
		buttonCancelCapture.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dialog.dispose();
				useImage = false;
			};
		});
		buttonCancelCapture.setPreferredSize(new Dimension(89, BUTTON_HEIGHT));
		buttonCancelCapture.setEnabled(true);
		panel.add(buttonCancelCapture, cMain);

//		piCamera.setAWB(AWB.AUTO);	    // Change Automatic White Balance setting to automatic
//		piCamera.setDRC(DRC.OFF); 			// Turn off Dynamic Range Compression
//		piCamera.setContrast(100); 			// Set maximum contrast
//		piCamera.setSharpness(100);		    // Set maximum sharpness
//		piCamera.setQuality(100); 		    // Set maximum quality
//		piCamera.setTimeout(10000);		    // Wait 1 second to take the image
//		piCamera.turnOnPreview(200, 200, captureW, captureH);            // Turn on image preview
//		piCamera.setEncoding(Encoding.JPG); // Change encoding of images to PNG

		// Take a still image and save it as "/home/pi/Pictures/cameraCapture.jpg"

		Log.message("We are about to display dialog\n");
		dialog.add(panel);
		dialog.pack();
		dialog.setVisible(true);
//			Log.message("We are about to take a still image\n");
//			File image = piCamera.takeStill("cameraCapture.jpg", captureW, captureH);
//			Log.message("New JPG capture saved to:\n\t" + image.getAbsolutePath());
//			piCamera.turnOffPreview();
		// setup for reopen

		if (useImage) {
			// process the image
			makelangeloApp.openFileOnDemand("/home/pi/Pictures/capture.jpg");
		}
	}

}
