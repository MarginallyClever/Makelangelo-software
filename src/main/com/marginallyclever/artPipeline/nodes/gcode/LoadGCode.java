package com.marginallyclever.artPipeline.nodes.gcode;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.artPipeline.Node;
import com.marginallyclever.artPipeline.NodePanel;
import com.marginallyclever.artPipeline.nodes.LoadAndSaveFile;
import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.convenience.turtle.Turtle;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.MakelangeloRobot;

/**
 * LoadGCode loads gcode into memory. 
 * @author Dan Royer
 *
 */
public class LoadGCode extends Node implements LoadAndSaveFile {
	private FileNameExtensionFilter filter = new FileNameExtensionFilter(Translator.get("FileTypeGCode"), "ngc");
	
	@Override
	public FileNameExtensionFilter getFileNameFilter() {
		return filter;
	}
	
	@Override
	public boolean canLoad() {
		return true;
	}

	@Override
	public boolean canSave() {
		return false;
	}

	@Override
	public boolean canLoad(String filename) {
		return false;
	}

	@Override
	public boolean canSave(String filename) {
		String ext = filename.substring(filename.lastIndexOf('.'));
		return (ext.equalsIgnoreCase(".ngc") || ext.equalsIgnoreCase(".gc"));
	}
	
	// search all tokens for one that starts with key and return it.
	protected String tokenExists(String key,String[] tokens) {
		for( String t : tokens ) {
			if(t.startsWith(key)) return t;
		}
		return null;
	}

	// returns angle of dy/dx as a value from 0...2PI
	private double atan3(double dy, double dx) {
		double a = Math.atan2(dy, dx);
		if (a < 0) a = (Math.PI * 2.0) + a;
		return a;
	}
	
	@Override
	public boolean load(InputStream in) {
		Turtle turtle = new Turtle();
		setTurtleResult(null);
		
		ColorRGB penDownColor = turtle.getColor();
		double scaleXY=1;
		boolean isAbsolute=true;
		
		Scanner scanner = new Scanner(in);	
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			try {
				// lose anything after a ; because it's a comment 
				String[] pieces = line.split(";");
				if (pieces.length == 0) continue;
				
				// the line isn't empty.  Split by spaces.
				// TODO some poorly formated gcode might not have spaces between letters.  do we care?
				String[] tokens = pieces[0].split("\\s");
				if (tokens.length == 0) continue;
				
				String mCodeToken=tokenExists("M",tokens);
				if(mCodeToken!=null) {
					int mCode = Integer.parseInt(mCodeToken.substring(1));
					switch(mCode) {
					case 6:
						// tool change
						String color = tokenExists("T",tokens);
						penDownColor = new ColorRGB(Integer.parseInt(color.substring(1)));
						turtle.setColor(penDownColor);
						break;
					default:
						// ignore all others
						break;
					}
				}
				
				String gCodeToken=tokenExists("G",tokens);
				if(gCodeToken!=null) {
					int gCode = Integer.parseInt(gCodeToken.substring(1));
					switch(gCode) {
					case 20: scaleXY=25.4;  break;  // in -> mm
					case 21: scaleXY= 1.0;  break;  // mm
					case 90: isAbsolute=true;	break;  // absolute mode
					case 91: isAbsolute=false;	break;  // relative mode
					default:
						break;
					}
				}
	
				turtle.penUp();
				double nx = turtle.getX();
				double ny = turtle.getY();
				double nz = 90;
				double ni = nx;
				double nj = ny;
				double ox=nx;
				double oy=ny;
				double oz=nz;
						
				if(tokenExists("X",tokens)!=null) {
					double v = Float.valueOf(tokenExists("X",tokens).substring(1)) * scaleXY;
					nx = isAbsolute ? v : nx+v;
				}
				if(tokenExists("Y",tokens)!=null) {
					double v = Float.valueOf(tokenExists("Y",tokens).substring(1)) * scaleXY;
					ny = isAbsolute ? v : ny+v;
				}
				if(tokenExists("Z",tokens)!=null) {
					double v = Float.valueOf(tokenExists("Z",tokens).substring(1));  // do not scale
					nz = isAbsolute ? v : nz+v;
				}
				if(tokenExists("I",tokens)!=null) {
					double v = Float.valueOf(tokenExists("I",tokens).substring(1)) * scaleXY;
					ni = isAbsolute ? v : ni+v;
				}
				if(tokenExists("J",tokens)!=null) {
					double v = Float.valueOf(tokenExists("J",tokens).substring(1)) * scaleXY;
					nj = isAbsolute ? v : nj+v;
				}
				
				if(gCodeToken!=null) {
					int gCode = Integer.parseInt(gCodeToken.substring(1));
					if(gCode==0 || gCode==1) {
						if(nz!=oz) {
							// z change
							if(turtle.isUp()) turtle.penDown();
							else turtle.penUp();
						}
						if(nx!=ox || ny!=oy) {
							turtle.moveTo(nx, ny);
						}
					} else if(gCode==2 || gCode==3) {
						// arc
						int dir = (gCode==2) ? -1 : 1;
	
						double dx = ox - ni;
						double dy = oy - nj;
						double radius = Math.sqrt(dx * dx + dy * dy);
	
						// find angle of arc (sweep)
						double angle1 = atan3(dy, dx);
						double angle2 = atan3(ny - nj, nx - ni);
						double theta = angle2 - angle1;
	
						if (dir > 0 && theta < 0) angle2 += Math.PI * 2.0;
						else if (dir < 0 && theta > 0) angle1 += Math.PI * 2.0;
	
						theta = angle2 - angle1;
	
						double stepSize=1.0;
						double len = Math.abs(theta) * radius;
						double angle3, scale;
	
						// Draw the arc from a lot of little line segments.
						for(double k = 0; k < len; k+=stepSize) {
							scale = k / len;
							angle3 = theta * scale + angle1;
							double ix = ni + Math.cos(angle3) * radius;
							double iy = nj + Math.sin(angle3) * radius;
	
							turtle.moveTo(ix,iy);
						}
						turtle.moveTo(nx,ny);
					} else {
						// do nothing.
					}
				}
			} catch(Exception e) {
				Log.error("At line "+line);
				e.printStackTrace();
			}
		}
		scanner.close();

		ArrayList<Turtle> list = new ArrayList<Turtle>();
		list.add(turtle);
		setTurtleResult(list);
		return true;
	}

	@Override
	public boolean save(OutputStream outputStream,ArrayList<Turtle> turtles, MakelangeloRobot robot) {
		return false;
	}

	@Override
	public String getName() {
		return Translator.get("LoadGCODE");
	}

	@Override
	public NodePanel getPanel() {
		// TODO Auto-generated method stub
		return null;
	}
}
