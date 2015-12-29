package com.marginallyclever.makelangelo;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

/**
 * contains the text for a gcode file.
 * also provides methods for estimating the total length of lines drawn
 * also provides methods for "walking" a file and remembering certain states
 * also provides bounding information?
 * also provides file scaling?
 *
 * @author danroyer
 */
public class GCodeFile {
	private int linesTotal = 0;
	private int linesProcessed = 0;
	private boolean fileOpened = false;
	private ArrayList<String> lines = new ArrayList<String>();
	public float estimatedTime = 0;
	public float estimatedLength = 0;
	public int estimateCount = 0;
	public float scale = 1.0f;
	public float feedRate = 1.0f;
	public boolean changed = false;


	public void reset() {
		setLinesTotal(0);
		setLinesProcessed(0);
		setFileOpened(false);
		setLines(new ArrayList<String>());
		estimatedTime = 0;
		estimatedLength = 0;
		estimateCount = 0;
		scale = 1.0f;
		feedRate = 1.0f;
		changed = true;
	}

	// returns angle of dy/dx as a value from 0...2PI
	private double atan3(double dy, double dx) {
		double a = Math.atan2(dy, dx);
		if (a < 0) a = (Math.PI * 2.0) + a;
		return a;
	}


	/**
	 * Estimate the time required to execute all the gcode commands.
	 * Requires machine settings, and should probably be moved into MakelangeloRobot
	 */
	private void estimateDrawTime() {
		int j;

		double px = 0, py = 0, pz = 0, length = 0, x, y, z, ai, aj;
		feedRate = 1.0f;
		scale = 0.1f;
		estimatedTime = 0;
		estimatedLength = 0;
		estimateCount = 0;

		Iterator<String> iLine = getLines().iterator();
		while (iLine.hasNext()) {
			String line = iLine.next();
			String[] pieces = line.split(";");  // comments come after a semicolon.
			if (pieces.length == 0) continue;

			String[] tokens = pieces[0].split("\\s");

			for (j = 0; j < tokens.length; ++j) {
				if (tokens[j].equals("G20")) scale = 2.54f;  // in->cm
				if (tokens[j].equals("G21")) scale = 0.10f;  // mm->cm
				if (tokens[j].startsWith("F")) {
					try {
						feedRate = Float.valueOf(tokens[j].substring(1)) * scale;
					}
					catch(Exception e) {
						e.printStackTrace(); 
					}
					assert (!Float.isNaN(feedRate) && feedRate != 0);
				}
			}

			x = px;
			y = py;
			z = pz;
			ai = px;
			aj = py;
			for (j = 1; j < tokens.length; ++j) {
				if (tokens[j].startsWith("X")) x = Float.valueOf(tokens[j].substring(1)) * scale;
				if (tokens[j].startsWith("Y")) y = Float.valueOf(tokens[j].substring(1)) * scale;
				if (tokens[j].startsWith("Z")) z = Float.valueOf(tokens[j].substring(1)) * scale;
				if (tokens[j].startsWith("I")) ai = px + Float.valueOf(tokens[j].substring(1)) * scale;
				if (tokens[j].startsWith("J")) aj = py + Float.valueOf(tokens[j].substring(1)) * scale;
			}

			if (z != pz) {
				// pen up/down action
				estimatedTime += (z - pz) / feedRate;  // seconds?
				assert (!Float.isNaN(estimatedTime));
			}

			if (tokens[0].equals("G00") || tokens[0].equals("G0") ||
					tokens[0].equals("G01") || tokens[0].equals("G1")) {
				// draw a line
				double ddx = x - px;
				double ddy = y - py;
				length = Math.sqrt(ddx * ddx + ddy * ddy);
				estimatedTime += length / feedRate;
				assert (!Float.isNaN(estimatedTime));
				estimatedLength += length;
				++estimateCount;
				px = x;
				py = y;
				pz = z;
			} else if (tokens[0].equals("G02") || tokens[0].equals("G2") ||
					tokens[0].equals("G03") || tokens[0].equals("G3")) {
				// draw an arc
				int dir = (tokens[0].equals("G02") || tokens[0].equals("G2")) ? -1 : 1;
				double dx = px - ai;
				double dy = py - aj;
				double radius = Math.sqrt(dx * dx + dy * dy);

				// find angle of arc (sweep)
				double angle1 = atan3(dy, dx);
				double angle2 = atan3(y - aj, x - ai);
				double theta = angle2 - angle1;

				if (dir > 0 && theta < 0) angle2 += 2.0 * Math.PI;
				else if (dir < 0 && theta > 0) angle1 += 2.0 * Math.PI;

				theta = Math.abs(angle2 - angle1);
				// length of arc=theta*r (http://math.about.com/od/formulas/ss/surfaceareavol_9.htm)
				length = theta * radius;
				estimatedTime += length / feedRate;
				assert (!Float.isNaN(estimatedTime));
				estimatedLength += length;
				++estimateCount;
				px = x;
				py = y;
				pz = z;
			}
		}  // for ( each instruction )
		assert (!Float.isNaN(estimatedTime));
		// processing time for each instruction
		estimatedTime += estimateCount * 0.007617845117845f;
		// conversion to ms?
		estimatedTime *= 10000;
	}


	// close the file, clear the preview tab
	public void closeFile() {
		if (isFileOpened() == true) {
			setFileOpened(false);
			lines.clear();
		}
	}


	public void load(String filename) throws IOException {
		closeFile();

		Scanner scanner = new Scanner(new FileInputStream(filename));

		setLinesTotal(0);
		setLines(new ArrayList<String>());
		try {
			while (scanner.hasNextLine()) {
				getLines().add(scanner.nextLine());
				setLinesTotal(getLinesTotal() + 1);
			}
		} finally {
			scanner.close();
		}
		setFileOpened(true);
		estimateDrawTime();
	}


	public void save(String filename) throws IOException {
		FileOutputStream out = new FileOutputStream(filename);
		String temp;

		for (int i = 0; i < getLinesTotal(); ++i) {
			temp = getLines().get(i);
			if (!temp.endsWith(";") && !temp.endsWith(";\n")) {
				temp += ";";
			}
			if (!temp.endsWith("\n")) temp += "\n";
			out.write(temp.getBytes());
		}

		out.flush();
		out.close();
	}

	public int getLinesProcessed() {
		return linesProcessed;
	}

	public void setLinesProcessed(int linesProcessed) {
		this.linesProcessed = linesProcessed;
	}

	public int getLinesTotal() {
		return linesTotal;
	}

	private void setLinesTotal(int linesTotal) {
		this.linesTotal = linesTotal;
	}

	public ArrayList<String> getLines() {
		return lines;
	}

	private void setLines(ArrayList<String> lines) {
		this.lines = lines;
	}

	public boolean moreLinesAvailable() {
		if( isFileOpened() == false ) return false;
		if( getLinesProcessed() >= getLinesTotal() ) return false;
		
		return true;
	}
	
	public String nextLine() {
		int lineNumber = getLinesProcessed();
		setLinesProcessed(lineNumber + 1);
		String line = getLines().get(lineNumber).trim();
		return line;
	}

	public boolean isFileOpened() {
		return fileOpened;
	}

	private void setFileOpened(boolean fileOpened) {
		this.fileOpened = fileOpened;
	}

	public boolean isLoaded() {
		return (isFileOpened() && getLines() != null && getLines().size() > 0);
	}
}


/**
 * This file is part of DrawbotGUI.
 * <p>
 * DrawbotGUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * DrawbotGUI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with DrawbotGUI.  If not, see <http://www.gnu.org/licenses/>.
 */
