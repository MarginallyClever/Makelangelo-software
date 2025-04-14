package com.marginallyclever.makelangelo.makeart.io;

import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Scanner;

public class LoadGCode implements TurtleLoader {

	private static final Logger logger = LoggerFactory.getLogger(LoadGCode.class);

	private final FileNameExtensionFilter filter = new FileNameExtensionFilter("GCode", "gcode","ngc");
	
	@Override
	public FileNameExtensionFilter getFileNameFilter() {
		return filter;
	}

	@Override
	public boolean canLoad(String filename) {
		String ext = filename.substring(filename.lastIndexOf('.')+1);
		return Arrays.stream(filter.getExtensions()).anyMatch(ext::equalsIgnoreCase);
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
	public Turtle load(InputStream in) throws Exception {
		if (in == null) {
			throw new NullPointerException("Input stream is null");
		}

		logger.debug("Loading...");

		Turtle turtle = new Turtle();
		double scaleXY=1;
		boolean isAbsolute=true;
		
		double oz=turtle.isUp()?90:0;
		double ox=turtle.getX();
		double oy=turtle.getY();
		
		int lineNumber=0;
		String line="";
		try (Scanner scanner = new Scanner(in)) {
			while (scanner.hasNextLine()) {
				line = scanner.nextLine();
				lineNumber++;
				// lose anything in parenthesis () because it's a comment
				line = line.replaceAll("\\(.*\\)", "");
				// lose anything after a ; because it's a comment
				String[] pieces = line.split(";");
				if (pieces.length == 0) continue;
				// the line isn't empty.

				String[] tokens = pieces[0].split("\\s");
				if (tokens.length == 0) continue;

				double nx = turtle.getX();
				double ny = turtle.getY();
				double nz = oz;
				double ni = nx;
				double nj = ny;

				String mCodeToken=tokenExists("M",tokens);
				if(mCodeToken!=null) {
					int mCode = Integer.parseInt(mCodeToken.substring(1));
					switch(mCode) {
					case 6:
						// tool change
						String color = tokenExists("T",tokens);
						Color penDownColor = new Color(Integer.parseInt(color.substring(1)));
						turtle.setStroke(penDownColor);
						break;
					case 280:
						// z move
						double v = Double.parseDouble(tokenExists("S",tokens).substring(1));
						nz = isAbsolute ? v : nz+v;
						if(nz!=oz) {
							// z change
							if(turtle.isUp()) turtle.penDown();
							else turtle.penUp();
							oz=nz;
						}
						break;
					default:
						// ignore all others
						break;
					}
					continue;
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

					if( gCode==28 ) {
						// TODO set to machine home position.
						nx = ni = 0;
						ny = nj = 0;
						continue;
					}

					nx = parseScaled(tokens,"X",nx,scaleXY,isAbsolute);
					ny = parseScaled(tokens,"Y",ny,scaleXY,isAbsolute);
					nz = parseUnscaled(tokens,nz,isAbsolute);

					if(gCode==0 || gCode==1) {
						if(nz!=oz) {
							// z change
							if(turtle.isUp()) turtle.penDown();
							else turtle.penUp();
							oz=nz;
						}
						if(nx!=ox || ny!=oy) {
							turtle.moveTo(nx, ny);
							ox=nx;
							oy=ny;
						}
					} else if(gCode==2 || gCode==3) {
						// arc
						int dir = (gCode==2) ? -1 : 1;
						ni = parseScaled(tokens,"I",nx,scaleXY,isAbsolute);
						nj = parseScaled(tokens,"J",ny,scaleXY,isAbsolute);

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

						double len = Math.abs(theta) * radius;
						double angle3, scale;

						// TODO turtle support for arcs
						// Draw the arc from a lot of little line segments.
						for(double k = 0; k < len; k++) {
							scale = k / len;
							angle3 = theta * scale + angle1;
							double ix = ni + Math.cos(angle3) * radius;
							double iy = nj + Math.sin(angle3) * radius;

							turtle.moveTo(ix,iy);
						}
						turtle.moveTo(nx,ny);
						ox=nx;
						oy=ny;
						continue;
					}
					// else do nothing.
				}
			}
		}
		catch(Exception e) {
			throw new Exception("GCODE parse failure ("+lineNumber+"): "+line, e);
		}

		return turtle;
	}

	private double parseUnscaled(String[] tokens, double nz, boolean isAbsolute) {
		if(tokenExists("Z",tokens)!=null) {
			double v = Float.parseFloat(tokenExists("Z",tokens).substring(1));  // do not scale
			nz = isAbsolute ? v : nz+v;
		}
		return nz;
	}

	private double parseScaled(String[] tokens, String key,double nx, double scaleXY, boolean isAbsolute) {
		if(tokenExists(key,tokens)!=null) {
			double v = Float.parseFloat(tokenExists(key,tokens).substring(1)) * scaleXY;
			nx = isAbsolute ? v : nx+v;
		}
		return nx;
	}
}
