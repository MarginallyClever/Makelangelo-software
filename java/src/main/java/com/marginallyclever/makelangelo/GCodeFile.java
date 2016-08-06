package com.marginallyclever.makelangelo;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;
import java.util.prefs.Preferences;

import com.jogamp.opengl.GL2;
import com.marginallyclever.drawingtools.DrawingTool;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;

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
	public float estimatedTime = 0;  // ms
	public float estimatedLength = 0;  // cm
	public int estimateCount = 0;
	public float scale = 1.0f;
	public float feedRate = 1.0f;
	public boolean changed = false;

	private Preferences prefs = PreferencesHelper.getPreferenceNode(PreferencesHelper.MakelangeloPreferenceKey.GRAPHICS);
	private ReentrantLock lock = new ReentrantLock();

	// optimization - turn gcode into vectors once on load, draw vectors after that.
	public enum GCodeNodeType {
		COLOR, POS, TOOL
	}
	
	class GCodeNode {
		double x1, y1, x2, y2;
		Color c;
		int toolID;
		int lineNumber;
		GCodeNodeType type;
	}

	ArrayList<GCodeNode> fastNodes = new ArrayList<GCodeNode>();


	
	public GCodeFile() {}
	
	
	public GCodeFile(String filename,boolean flipHorizontally) throws IOException {
		load(filename,flipHorizontally);
	}
	
	
	public GCodeFile(InputStream in,boolean flipHorizontally) throws IOException {
		load(in,flipHorizontally);
	}


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

		int lineCount=0;
		Iterator<String> iLine = lines.iterator();
		while (iLine.hasNext()) {
			lineCount++;
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
			try {
				for (j = 1; j < tokens.length; ++j) {
					if (tokens[j].startsWith("X")) x = Float.valueOf(tokens[j].substring(1)) * scale;
					if (tokens[j].startsWith("Y")) y = Float.valueOf(tokens[j].substring(1)) * scale;
					if (tokens[j].startsWith("Z")) z = Float.valueOf(tokens[j].substring(1)) * scale;
					if (tokens[j].startsWith("I")) ai = px + Float.valueOf(tokens[j].substring(1)) * scale;
					if (tokens[j].startsWith("J")) aj = py + Float.valueOf(tokens[j].substring(1)) * scale;
				}
			} catch(Exception e) {
				System.out.println("Error on line "+lineCount);
				e.printStackTrace();
			}

			if (z != pz) {
				// pen up/down action
				estimatedTime += (z - pz) / feedRate;  // seconds?
				assert (!Float.isNaN(estimatedTime));
			}

			String firstToken = tokens[0];
			if (firstToken.equals("G00") || firstToken.equals("G0") ||
					firstToken.equals("G01") || firstToken.equals("G1")) {
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
			} else if (firstToken.equals("G02") || firstToken.equals("G2") ||
					firstToken.equals("G03") || firstToken.equals("G3")) {
				// draw an arc
				int dir = (firstToken.equals("G02") || firstToken.equals("G2")) ? -1 : 1;
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

	public void load(InputStream in,boolean flipHorizontally) throws IOException {
		Scanner scanner = new Scanner(in);
		loadFromScanner(scanner,flipHorizontally);
	}

	public void load(String filename,boolean flipHorizontally) throws IOException {
		Scanner scanner = new Scanner(new FileInputStream(filename));	
		loadFromScanner(scanner,flipHorizontally);
	}
	
	
	private void loadFromScanner(Scanner scanner,boolean flipHorizontally) {
		closeFile();

		setLinesTotal(0);
		setLines(new ArrayList<String>());
		try {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if(flipHorizontally) {
					// find X, change to X-
					// find X--, change to X-
					line = line.replace("X","X-");
					line = line.replace("X--","X");
					// do it again for I, the X portion of arc centers.
					if(!line.contains("M101")) {
						line = line.replace("I","I-");
						line = line.replace("I--","I");
					}
					// reverse the direction of arcs?
					if(line.contains("G02") || line.contains("G2")) {
						line = line.replace("G02","G03");
						line = line.replace("G2","G3");
					} else if(line.contains("G03") || line.contains("G3")) {
						line = line.replace("G03","G02");
						line = line.replace("G3","G2");
					}
				}
				lines.add(line);
				setLinesTotal(getLinesTotal() + 1);
			}
		} finally {
			scanner.close();
		}
		setFileOpened(true);
		estimateDrawTime();
		changed=true;
	}


	public void save(String filename) throws IOException {
		FileOutputStream out = new FileOutputStream(filename);
		String temp;

		for (int i = 0; i < getLinesTotal(); ++i) {
			temp = lines.get(i);
			if (!temp.endsWith(";") && !temp.endsWith(";\n")) {
				temp += ";";
			}
			if (!temp.endsWith("\n")) temp += "\n";
			out.write(temp.getBytes());
		}

		out.flush();
		out.close();
	}

	
	public int findLastPenUpBefore(int startAtLine,String toMatch) {
		int x = startAtLine;
		if( linesTotal==0 ) return 0;
		if(x >= linesTotal) x = linesTotal-1;
		
		toMatch = toMatch.trim();
		while(x>1) {
			String line = lines.get(x).trim();
			if(line.equals(toMatch)) {
				return x;
			}
			--x;
		}
				
		return x;
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
		String line = lines.get(lineNumber).trim();
		return line;
	}

	public boolean isFileOpened() {
		return fileOpened;
	}

	private void setFileOpened(boolean fileOpened) {
		this.fileOpened = fileOpened;
	}

	public boolean isLoaded() {
		return (isFileOpened() && lines != null && lines.size() > 0);
	}

	public void render( GL2 gl2, MakelangeloRobot robot ) {
		if(lock.isLocked()) return;
		lock.lock();
		try {
			renderLocked(gl2,robot);
		}
		finally {
			lock.unlock();
		}
	}
	
	
	private void renderLocked( GL2 gl2, MakelangeloRobot robot ) {
		int linesProcessed = getLinesProcessed();
		optimizeNodes(robot);

		int lookAhead=320;

		DrawingTool tool = robot.getSettings().getTool(0);
		gl2.glColor3f(0, 0, 0);

		boolean drawAllWhileRunning = false;
		if (robot.isRunning()) drawAllWhileRunning = prefs.getBoolean("Draw all while running", true);
			
		// draw image
		if (fastNodes.size() > 0) {
			// draw the nodes
			Iterator<GCodeNode> nodes = fastNodes.iterator();
			while (nodes.hasNext()) {
				GCodeNode n = nodes.next();

				if (robot.isRunning()) {
					if (n.lineNumber < linesProcessed) {
						gl2.glColor3f(1, 0, 0);
						//g2d.setColor(Color.RED);
						if(n.type==GCodeNodeType.POS) {
							robot.gondolaX=(float)n.x1*10;
							robot.gondolaY=(float)n.y1*10;
						}
					} else if (n.lineNumber <= linesProcessed + lookAhead) {
						gl2.glColor3f(0, 1, 0);
						//g2d.setColor(Color.GREEN);
					} else if (drawAllWhileRunning == false) {
						break;
					}
				}

				switch (n.type) {
				case TOOL:
					tool = robot.getSettings().getTool(n.toolID);
					gl2.glLineWidth(tool.getDiameter());
					break;
				case COLOR:
					if (!robot.isRunning() || n.lineNumber > linesProcessed + lookAhead) {
						//g2d.setColor(n.c);
						gl2.glColor3f(n.c.getRed() / 255.0f, n.c.getGreen() / 255.0f, n.c.getBlue() / 255.0f);
					}
					break;
				default:
					tool.drawLine(gl2, n.x1, n.y1, n.x2, n.y2);
					break;
				}
			}
		}
	}

	
	private void addNodePos(int i, double x1, double y1, double x2, double y2) {
		GCodeNode n = new GCodeNode();
		n.lineNumber = i;
		n.x1 = x1;
		n.x2 = x2;
		n.y1 = y1;
		n.y2 = y2;
		n.type = GCodeNodeType.POS;

		fastNodes.add(n);
	}

	private void addNodeColor(int i, Color c) {
		GCodeNode n = new GCodeNode();
		n.lineNumber = i;
		n.c = c;
		n.type = GCodeNodeType.COLOR;

		fastNodes.add(n);
	}

	private void addNodeTool(int i, int tool_id) {
		GCodeNode n = new GCodeNode();
		n.lineNumber = i;
		n.toolID = tool_id;
		n.type = GCodeNodeType.TOOL;

		fastNodes.add(n);
	}

	public void emptyNodeBuffer() {
		while(lock.isLocked());
		lock.lock();
		fastNodes.clear();
		lock.unlock();
	}

	private void optimizeNodes( MakelangeloRobot robot ) {
		if (!fastNodes.isEmpty() && changed == false) return;
		changed = false;

		DrawingTool tool = robot.getSettings().getTool(0);

		float drawScale = 0.1f;
		// arc smoothness - increase to make more smooth and run slower.
		double STEPS_PER_DEGREE=1;
		
		
		float px = 0, py = 0, pz = 90;
		float x, y, z, ai, aj;
		int i, j;
		boolean absMode = true;
		boolean isLifted=true;
		String tool_change = "M06 T";
		Color tool_color = Color.BLACK;

		Iterator<String> commands = getLines().iterator();
		i = 0;
		while (commands.hasNext()) {
			String line = commands.next();
			++i;
			String[] pieces = line.split(";");
			if (pieces.length == 0) continue;

			if (line.startsWith(tool_change)) {
				String numberOnly = pieces[0].substring(tool_change.length()).replaceAll("[^0-9]", "");
				int id = (int) Integer.valueOf(numberOnly, 10);
				addNodeTool(i, id);
				switch (id) {
				case 1:		tool_color = Color.RED;		break;
				case 2:		tool_color = Color.GREEN;	break;
				case 3:		tool_color = Color.BLUE;	break;
				default:	tool_color = Color.BLACK;	break;
				}
				continue;
			}

			String[] tokens = pieces[0].split("\\s");
			if (tokens.length == 0) continue;

			// have we changed scale?
			// what are our coordinates?
			x = px;
			y = py;
			z = pz;
			ai = px;
			aj = py;
			for (j = 0; j < tokens.length; ++j) {
				if (tokens[j].equals("G20")) drawScale = 2.54f; // in->cm
				else if (tokens[j].equals("G21")) drawScale = 0.10f; // mm->cm
				else if (tokens[j].equals("G90")) {
					absMode = true;
					break;
				} else if (tokens[j].equals("G91")) {
					absMode = false;
					break;
				} else if (tokens[j].equals("G54")) break;
				else if (tokens[j].startsWith("X")) {
					float tx = Float.valueOf(tokens[j].substring(1)) * drawScale;
					x = absMode ? tx : x + tx;
				} else if (tokens[j].startsWith("Y")) {
					float ty = Float.valueOf(tokens[j].substring(1)) * drawScale;
					y = absMode ? ty : y + ty;
				} else if (tokens[j].startsWith("Z")) {
					float tz = z = Float.valueOf(tokens[j].substring(1));// * drawScale;
					z = absMode ? tz : z + tz;
					
					isLifted = (tool.getPenUpAngle()==z);
				}
				if (tokens[j].startsWith("I")) ai = Float.valueOf(tokens[j].substring(1)) * drawScale;
				if (tokens[j].startsWith("J")) aj = Float.valueOf(tokens[j].substring(1)) * drawScale;
			}
			if (j < tokens.length) continue;

			if (isLifted) {
				if (robot.getShowPenUp() == false) {
					px = x;
					py = y;
					pz = z;
					continue;
				}
				addNodeColor(i, Color.BLUE);
			} else {
				addNodeColor(i, tool_color);  // TODO use actual pen color
			}

			// what kind of motion are we going to make?
			String firstToken = tokens[0];
			if (firstToken.equals("G00") || firstToken.equals("G0") ||
				firstToken.equals("G01") || firstToken.equals("G1")) {
				addNodePos(i, px, py, x, y);
			} else if (firstToken.equals("G02") || firstToken.equals("G2") ||
					firstToken.equals("G03") || firstToken.equals("G3")) {
				// draw an arc

				// clockwise or counter-clockwise?
				int dir = (firstToken.equals("G02") || firstToken.equals("G2")) ? -1 : 1;

				double dx = px - ai;
				double dy = py - aj;
				double radius = Math.sqrt(dx * dx + dy * dy);

				// find angle of arc (sweep)
				double angle1 = atan3(dy, dx);
				double angle2 = atan3(y - aj, x - ai);
				double theta = angle2 - angle1;

				if (dir > 0 && theta < 0) angle2 += Math.PI * 2.0;
				else if (dir < 0 && theta > 0) angle1 += Math.PI * 2.0;

				theta = angle2 - angle1;

				double len = Math.abs(theta) * radius;
				double segments = len * STEPS_PER_DEGREE * 2.0;
				double nx, ny, angle3, scale;

				// Draw the arc from a lot of little line segments.
				for (int k = 0; k < segments; ++k) {
					scale = (double) k / segments;
					angle3 = theta * scale + angle1;
					nx = ai + Math.cos(angle3) * radius;
					ny = aj + Math.sin(angle3) * radius;

					addNodePos(i, px, py, nx, ny);
					px = (float) nx;
					py = (float) ny;
				}
				addNodePos(i, px, py, x, y);
			}

			px = x;
			py = y;
			pz = z;
		}  // for ( each instruction )
	}
}


/**
 * This file is part of Makelangelo.
 * <p>
 * Makelangelo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Makelangelo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Makelangelo.  If not, see <http://www.gnu.org/licenses/>.
 */
