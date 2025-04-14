package com.marginallyclever.makelangelo.makeart.io;

import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Load THR files into a {@link Turtle}.
 * @author Dan Royer
 * See <a href="https://sisyphus-industries.com/wp-content/uploads/wpforo/default_attachments/1568584503-sisyphus-table-programming-logic3.pdf">THR</a>
 */
public class LoadTHR implements TurtleLoader {
	private static final Logger logger = LoggerFactory.getLogger(LoadTHR.class);

	public static final FileNameExtensionFilter filter = new FileNameExtensionFilter("Theta R (thr)", "thr");
	private Turtle myTurtle;

	public LoadTHR() {
		super();
	}

	@Override
	public FileNameExtensionFilter getFileNameFilter() {
		return filter;
	}

	@Override
	public boolean canLoad(String filename) {
		String ext = filename.substring(filename.lastIndexOf('.')+1);
		return Arrays.stream(filter.getExtensions()).anyMatch(ext::equalsIgnoreCase);
	}

	@Override
	public Turtle load(InputStream in) throws Exception {
		if (in == null) {
			throw new NullPointerException("Input stream is null");
		}

		logger.debug("Loading...");

		myTurtle = new Turtle();
		myTurtle.setStroke(Color.BLACK);  // initial pen color

		// plotter coordinates are inverted in Y so flip the image.
		myTurtle.scale(1, -1);

		var w2=200.0;

		// read one line at a time
		myTurtle.penUp();
		String line;
		try(var reader = new java.io.BufferedReader(new java.io.InputStreamReader(in))) {
			int count = 0;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				count++;
				if(line.isEmpty()) continue;
				if(line.startsWith("#")) continue;  // comment
				String [] parts = line.split(" ");
				if(parts.length!=2) throw new IllegalArgumentException("syntax error on line "+count);
				double theta = Double.parseDouble(parts[0]);
				double r = Double.parseDouble(parts[1]) * w2;

				var dx = Math.cos(theta) * r;
				var dy = Math.sin(theta) * r;
				myTurtle.moveTo(dx, dy);
				myTurtle.penDown();
			}
		}

		// plotter coordinates are inverted in Y so flip the image.
		myTurtle.scale(1, -1);
		myTurtle.rotate(90);

		return myTurtle;
	}
}
