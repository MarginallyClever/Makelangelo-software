package com.marginallyclever.util;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilitis fonctions to take and save an ImageCapture and/or ScreenCapture of a
 * JComponante. can use Robot.ScreenCapture (if not in an HeadLess
 * environnement) or create a BufferedImage and ask the JComponante to paint
 * itseff on it. (for not visible Jcomponante and in HeadLess environnement)
 *
 * @author PPAC37
 */
public class ImageCaptureJComponant {

	private static final Logger logger = LoggerFactory.getLogger(ImageCaptureJComponant.class);

	/**
	 * Convert Image to BufferedImage.
	 *
	 * Source : https://mkyong.com/java/how-to-write-an-image-to-file-imageio/
	 *
	 * @param img
	 * @return
	 */
	public static BufferedImage convertToBufferedImage(Image img) {
		if (img instanceof BufferedImage) {
			return (BufferedImage) img;
		}
		// Create a buffered image with transparency
		BufferedImage bi = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics2D = bi.createGraphics();
		graphics2D.drawImage(img, 0, 0, null);
		graphics2D.dispose();
		return bi;
	}

	/**
	 * Get a String of all suported ImageIo writer format extension in lower
	 * case separated with '/'.
	 *
	 * @return
	 */
	private static String listImageTypeSupportedAsSimpleLowerCaseString() {
		String[] writerNames = ImageIO.getWriterFormatNames();
		SortedSet<String> sset = new TreeSet<>();
		Arrays.stream(writerNames).forEach(wn -> sset.add(wn.toLowerCase()));
		StringBuilder sb = new StringBuilder();
		sset.forEach(s -> {
			sb.append(s);
			sb.append("/");
		});
		if (sb.length() > 0) {
			sb.setLength(sb.length() - 1);
		}
		return sb.toString();
	}

	/**
	 *
	 * @param cToScreenShoot the value of cToScreenShoot
	 * @param fileDestDir the value of fileTested
	 * @param fileDestName the value of fileDest
	 * @param inHeadlessMode the value of inHeadlessMode to ste to true to force
	 * a addNotify and a doLayout to see the sub JComponent in headless mode
	 * (not added to an visible JFrame/JComponent)
	 * @throws IOException
	 */
	public static void doACapture(Component cToScreenShoot, File fileDestDir, String fileDestName, boolean inHeadlessMode) throws IOException {
		if (inHeadlessMode) {
			try {
				cToScreenShoot.addNotify(); // Needed in an headless env to doLayout recursively of all sub component
			} catch (Exception e) {
				// But if not done by the Thread (Swing thread normaly own it ? ) this can throw an exception
			}
			cToScreenShoot.doLayout();
		}
		BufferedImage bufferImage = new BufferedImage(cToScreenShoot.getWidth(), cToScreenShoot.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D bufferGraphics = bufferImage.createGraphics();
		// Clear the buffer:
		bufferGraphics.clearRect(0, 0, cToScreenShoot.getWidth(), cToScreenShoot.getHeight());
		//define some rendering option (optional ?)
		bufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		bufferGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		// make the JComponent paint on the bufferedGraphics to get a copy of the render.
		cToScreenShoot.paintAll(bufferGraphics);
		final File fileOutputDest = new File(fileDestDir, fileDestName);
		System.out.println("output dir " + fileDestDir.getAbsolutePath());
		System.out.println("try writing " + fileOutputDest.toString());
		// save the "image" painted in the bufferImage to a file
		ImageIO.write(bufferImage, "png", fileOutputDest);
		//		    to save a resize image to
		if (true) {
			int dimW = cToScreenShoot.getWidth() / 2;
			int dimH = cToScreenShoot.getHeight() / 2;
			Image scaledInstance = bufferImage.getScaledInstance(dimW, dimH, Image.SCALE_DEFAULT);
			final File fileOutputDestResised = new File(fileDestDir, "resised_" + fileDestName);
					System.out.println("writing resized " + fileOutputDest.toString());
			ImageIO.write(convertToBufferedImage(scaledInstance), "png", fileOutputDestResised);
		}
		
		
		if (cToScreenShoot.isVisible() && !inHeadlessMode) {
			SwingUtilities.invokeLater(() -> {
				try {
					//				cToScreenShoot.notify();
					cToScreenShoot.doLayout();
					//				cToScreenShoot.repaint();
					final File fileOutputDestRobot = new File(fileDestDir, "robot_" + fileDestName);
					System.out.println("writing robot screenshot " + fileOutputDest.toString());
					Robot robot = new Robot();
					// ko in this context robot.waitForIdle();//delay(500);
					//cToScreenShoot.getBounds() // KO ...
					Rectangle r = new Rectangle(cToScreenShoot.getLocationOnScreen(), cToScreenShoot.getSize());
					BufferedImage screenShot = robot.createScreenCapture(r);
					ImageIO.write(screenShot, "png", fileOutputDestRobot);
				} catch (AWTException ex) {
					logger.error("{}", ex.getMessage(), ex);
				} catch (IOException ex) {
					logger.error("{}", ex.getMessage(), ex);
				}
			});
		}
	}

	/**
	 * output all the Image IO writer supported formats. Source :
	 * https://mkyong.com/java/how-to-write-an-image-to-file-imageio/
	 */
	private static void outputImageIOFormatTypeSupported() {
		String[] writerNames = ImageIO.getWriterFormatNames();
		Arrays.stream(writerNames).sorted().forEach(System.out::println);
	}

	/*
	if (doFullJFrame) javax.swing.SwingUtilities.invokeLater(() -> {
	try {
	if (doFullJFrame) {
	//https://stackoverflow.com/questions/58305/is-there-a-way-to-take-a-screenshot-using-java-and-save-it-to-some-sort-of-image
	//	18
	//If you'd like to capture all monitors, you can use the following code:
	GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	GraphicsDevice[] screens = ge.getScreenDevices();
	Rectangle allScreenBounds = new Rectangle();
	for (GraphicsDevice screen : screens) {
	Rectangle screenBounds = screen.getDefaultConfiguration().getBounds();
	allScreenBounds.width += screenBounds.width;
	allScreenBounds.height = Math.max(allScreenBounds.height, screenBounds.height);
	}
	//Rectangle 					rFrame = null;
	if (rFramef != null) {
	allScreenBounds = rFramef;
	}
	Robot robot = new Robot();
	//
	BufferedImage screenShot = robot.createScreenCapture(allScreenBounds);
	ImageIO.write(screenShot, "png", new File(dirScreenShotDest,"screenshot_robot.png"));
	}
	} catch (AWTException ex) {
	logger.error("{}",ex.getMessage(),ex);
	} catch (IOException ex) {
	logger.error("{}",ex.getMessage(),ex);
	}
	});
	 */
	/**
	 * List all files and sub files in this path. Using
	 * <code>Files.walk(path)</code> (so this take care of recursive path
	 * exploration ) And applying filter ( RegularFile and ReadableFile ) and
	 * filtering FileName ...
	 *
	 * @param path where to look.
	 * @param fileNameEndsWithSuffix use ".sb3" to get only ... ( this is not a
	 * regexp so no '.' despecialization required ) can be set to
	 * <code>""</code> to get all files.
	 * @return a list of files (may be empty if nothing is found) or null if
	 * something is wrong.
	 * @throws IOException
	 */
	public static List<File> listFiles(Path path, String fileNameEndsWithSuffix) throws IOException {
		List<File> result;
		try (final Stream<Path> walk = Files.walk(path)) {
			result = walk.filter(Files::isRegularFile).filter(Files::isReadable).map(Path::toFile).filter(f -> f.getName().endsWith(fileNameEndsWithSuffix)).collect(Collectors.toList());
		}
		return result;
	}

}
