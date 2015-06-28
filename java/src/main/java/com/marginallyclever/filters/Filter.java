package com.marginallyclever.filters;


import com.marginallyclever.drawingtools.DrawingTool;
import com.marginallyclever.makelangelo.MachineConfiguration;
import com.marginallyclever.makelangelo.MainGUI;
import com.marginallyclever.makelangelo.MultilingualSupport;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;


/**
 * base class for image filtering
 * @author Dan
 */
public abstract class Filter {
	// image properties
	protected int image_width, image_height;
	protected float w2,h2,scale;
	protected DrawingTool tool;
	
	protected int color_channel=0;
	
	// text properties
	protected float kerning=5.0f;
	protected float letter_width=10.0f;
	protected float letter_height=20.0f;
	protected float line_spacing=5.0f;
	protected float padding=5.0f;
	static final String alphabetFolder = "ALPHABET/";
	protected int chars_per_line=25;
	protected boolean draw_bounding_box=false;
	
	// text position and alignment
	public enum VAlign { TOP, MIDDLE, BOTTOM }
	public enum Align { LEFT, CENTER, RIGHT }
	protected VAlign align_vertical = VAlign.MIDDLE;
	protected Align  align_horizontal = Align.CENTER;
	protected float posx=0;
	protected float posy=0;

	// file properties
	protected String dest;
	// pen position optimizing
	protected boolean lastup;
	protected float previous_x,previous_y;
	// threading
	protected ProgressMonitor pm;
	protected SwingWorker<Void,Void> parent;

	protected MainGUI mainGUI;
	protected MultilingualSupport translator;
	protected MachineConfiguration machine;
	
	protected float sampleValue;
	protected float sampleSum;

	
	public Filter(MainGUI gui,MachineConfiguration mc,MultilingualSupport ms) {
		mainGUI = gui;
		translator = ms;
		machine = mc;
	}
	
	public void setParent(SwingWorker<Void,Void> p) {
		parent=p;
	}
	public void setProgressMonitor(ProgressMonitor p) {
		pm=p;
	}
	public void setDestinationFile(String _dest) {
		dest=_dest;
	}
	
	/**
	 * Called by filters that create GCODE from nothing.  Fractals might be one example.
	 */
	public void generate() {}

	/**
	 * Replace this with your generator/converter name.
     *
     * @return name of this filter.
     *
	 */
	public String getName() {  return "Unnamed";  }
	
	/**
	 * process should be called by filters that modify a bufferedimage.  Think photoshop filters.
	 * @param img the <code>java.awt.image.BufferedImage</code> this filter is using as source material.
	 * @return the altered image
	 */
	public BufferedImage process(BufferedImage img) {
		return img;
	}

	/**
	 * convert generates GCODE from a bufferedImage.
	 * @param img image to filter.
	 * @throws IOException
	 */
	public void convert(BufferedImage img) throws IOException {}
	
	
	protected int decode(int pixel) {
		int r = ((pixel>>16)&0xff);
		int g = ((pixel>> 8)&0xff);
		int b = ((pixel    )&0xff);
		return (r+g+b)/3;
	}
	
	protected int decode(Color c) {
		int r = c.getRed();
		int g = c.getGreen();
		int b = c.getBlue();
		return (r+g+b)/3;
	}
	
	
	protected int encode(int i) {
		return (0xff<<24) | (i<<16) | (i<< 8) | i;
	}
	

	protected void liftPen(Writer out) throws IOException {
		tool.writeOff(out);
		lastup=true;
	}
	
	
	protected void lowerPen(Writer out) throws IOException {
		tool.writeOn(out);
		lastup=false;
	}

	
	protected void imageStart(BufferedImage img, Writer out) throws IOException {
		tool = machine.getCurrentTool();

		imageSetupTransform(img);
		
		out.write(machine.getConfigLine()+";\n");
		out.write(machine.getBobbinLine()+";\n");
		
		previous_x=0;
		previous_y=0;
		
		setAbsoluteMode(out);
	}
	
	protected void setAbsoluteMode(Writer out) throws IOException {
		out.write("G90;\n");
	}
	
	protected void setRelativeMode(Writer out) throws IOException {
		out.write("G91;\n");
	}
	
	/**
	 * setup transform from source image dimensions to destination paper dimensions.
	 * @param img source dimensions
	 */
	protected void imageSetupTransform(BufferedImage img) {
		setupTransform( img.getWidth(), img.getHeight() );
	}
	
	/**
	 * setup transform when there is no image to convert from.  Essentially a 1:1 transform.
	 */
	protected void setupTransform() {
		// 10mm = 1cm.  letters should be 1cm tall.
		setupTransform( (int)(machine.getPaperWidth()*10.0f), (int)(machine.getPaperHeight()*10.0f) );
	}
	
	protected void setupTransform(int width,int height) {
		image_height = height;
		image_width = width;
		h2=image_height/2;
		w2=image_width/2;
		
		scale=10f;  // 10mm = 1cm

		double new_width = image_width;
		double new_height = image_height;
		
		if(image_width > machine.getPaperWidth()) {
			float resize = (float)machine.getPaperWidth()/(float)image_width;
			scale *= resize;
			new_height *= resize;
			new_width = machine.getPaperWidth();
		}
		if(new_height > machine.getPaperHeight()) {
			float resize = (float)machine.getPaperHeight()/(float)new_height;
			scale *= resize;
			new_width *= resize;
			new_height = machine.getPaperHeight();
		}
		scale *= machine.paperMargin;
		new_width *= machine.paperMargin;
		new_height *= machine.paperMargin;
		
		textFindCharsPerLine(new_width);
		
		posx = w2;
		posy = h2;
	}
	
	
	protected int sample1x1(BufferedImage img,int x,int y) {
		Color c = new Color(img.getRGB(x, y));
		switch(color_channel) {
		case 1: return c.getRed();
		case 2: return c.getGreen();
		case 3: return c.getBlue();
		default: return decode(c);
		}
	}

	
	protected int sample3x3(BufferedImage img,int x,int y) {
		int value=0, weight=0;
		
		if(y>0) {
			if(x>0) {
				value+=sample1x1(img,x-1, y-1);
				weight+=1;
			}
			value+=sample1x1(img,x, y-1)*2;
			weight+=2;

			if(x<image_width-1) {
				value+=sample1x1(img,x+1, y-1);
				weight+=1;
			}
		}

		if(x>0) {
			value+=sample1x1(img,x-1, y)*2;
			weight+=2;
		}
		value+=sample1x1(img,x, y)*4;
		weight+=4;
		if(x<image_width-1) {
			value+=sample1x1(img,x+1, y)*2;
			weight+=2;
		}

		if(y<image_height-1) {
			if(x>0) {
				value+=sample1x1(img,x-1, y+1);
				weight+=1;
			}
			value+=sample1x1(img,x, y+1)*2;
			weight+=2;
	
			if(x<image_width-1) {
				value+=sample1x1(img,x+1, y+1);
				weight+=1;
			}
		}
				
		return value/weight;
	}

	
	
	protected void sample1x1Safe(BufferedImage img,int x,int y,double scale) {
		if(x<0 || x >= image_width) return;
		if(y<0 || y >= image_height) return;
		
		sampleValue += sample1x1(img,x,y) * scale;
		sampleSum += scale;
	}
	
	/**
	 * sample the image, taking into account fractions of pixels.
	 * @param img the image to sample
	 * @param x0 top left corner
	 * @param y0 top left corner
	 * @param x1 bottom right corner
	 * @param y1 bottom right corner
	 * @return greyscale intensity in this region. range 0...255 inclusive
	 */
	protected int sample(BufferedImage img,double x0,double y0,double x1,double y1) {
		sampleValue=0;
		sampleSum=0;

		double xceil = Math.ceil(x0);
		double xweightstart = ( x0 != xceil ) ? xceil - x0 : 1;

		double xfloor = Math.floor(x1);
		double xweightend = ( x1 != xceil ) ? xfloor - x1 : 0;
		
		int left = (int)(x0+1);
		int right = (int)x1;
		
		// top edge
		double yceil = Math.ceil(y0);
		if( y0 != yceil ) {
			double yweightstart = yceil - y0;
			
			// left edge
			sample1x1Safe(img,(int)x0,(int)y0, xweightstart * yweightstart);
			
			for(int i=left;i<right;++i) {
				sample1x1Safe(img,i,(int)y0, yweightstart);
			}	
			// right edge
			sample1x1Safe(img,right,(int)y0, xweightend * yweightstart);
		}
		
		int bottom = (int)(y0+1);
		int top = (int)y1;
		for(int j = bottom; j < top; ++j ) {
			// left edge
			sample1x1Safe(img,(int)x0,j, xweightstart);
			
			for(int i=left;i<right;++i) {
				sample1x1Safe(img,i,j,1);
			}
			// right edge
			sample1x1Safe(img,right,j, xweightend);
		}
		
		// bottom edge
		double yfloor = Math.floor(y1);
		if( y1 != yfloor ) {
			double yweightend = yfloor - y1;

			// left edge
			sample1x1Safe(img,(int)x0,(int)y1, xweightstart * yweightend);
			
			for(int i=left;i<right;++i) {
				sample1x1Safe(img,i,(int)y1, yweightend);
			}
			// right edge
			sample1x1Safe(img,right,(int)y1, xweightend * yweightend);
		}
		
		return (int)(sampleValue/sampleSum);
	}
	
	protected float SX(float x) {
		return x*scale;
	}
	protected float SY(float y) {
		return y*scale;
	}
	protected float PX(float x) {
		return x-w2;
	}
	protected float PY(float y) {
		return h2-y;
	}
	protected float TX(float x) {
		return SX(PX(x));
	}
	protected float TY(float y) {
		return SY(PY(y));
	}
	
	
	protected void moveTo(Writer out,float x,float y,boolean up) throws IOException {
		float x2 = TX(x);
		float y2 = TY(y);
		
		if(up==lastup) {
			previous_x=x2;
			previous_y=y2;
		} else {
			tool.writeMoveTo(out,previous_x,previous_y);
			tool.writeMoveTo(out,x2,y2);
			if(up) liftPen(out);
			else   lowerPen(out);
		}
	}
	
	protected void moveToPaper(Writer out,double x,double y,boolean up) throws IOException {
		tool.writeMoveTo(out,(float)x,(float)y);
		if(up) liftPen(out);
		else   lowerPen(out);
	}
	
	
	protected double roundOff(double value) {
		return Math.floor(value * 100.0) / 100.0;
	}

	
	public void textSetPosition(float x,float y) {
		posx=x;
		posy=y;
	}
	
	public void textSetAlign(Align x) {
		align_horizontal = x;
	}
	
	public void textSetVAlign(VAlign x) {
		align_vertical = x;
	}
	
	
	public void textSetCharsPerLine(int numChars) {
		chars_per_line = numChars;
		//System.out.println("MAX="+numChars);
	}
	
	
	public void textFindCharsPerLine(double width) {
		chars_per_line=(int)Math.floor( (float)(width*10.0f - padding*2.0f) / (float)(letter_width+kerning) );
		//System.out.println("MAX="+chars_per_line);
	}
	

	// TODO count newlines?
	protected Rectangle2D textCalculateBounds(String text) {
		String [] lines = textWrapToLength(text);
		int len = textLongestLine(lines);
		
		int num_lines = lines.length;
		float h = padding*2 + ( letter_height + line_spacing ) * num_lines;//- line_spacing; removed because of letters that hang below the line
		float w = padding*2 + ( letter_width + kerning ) * len - kerning;
		float xmax=0, xmin=0, ymax=0, ymin=0;
		
		switch(align_horizontal) {
		case LEFT:
			xmax=posx + w;
			xmin=posx;
			break;
		case CENTER:
			xmax = posx + w/2;
			xmin = posx - w/2;
			break;
		case RIGHT:
			xmax = posx;
			xmin = posx - w;
			break;
		}
		
		switch(align_vertical) {
		case BOTTOM:
			ymax=posy + h;
			ymin=posy;
			break;
		case MIDDLE:
			ymax = posy + h/2;
			ymin = posy - h/2;
			break;
		case TOP:
			ymax = posy;
			ymin = posy - h;
			break;
		}
		/*
		System.out.println(num_lines + " lines");
		System.out.println("longest "+len+" chars");
		System.out.println("x "+xmin+" to "+xmax);
		System.out.println("y "+ymin+" to "+ymax);
		*/
		Rectangle2D r = new Rectangle2D.Float();
		r.setRect(xmin, ymin, xmax - xmin, ymax - ymin);
		
		return r;
	}

	
	protected void textCreateMessageNow(String text, Writer output) throws IOException {
		if(chars_per_line<=0) return;

		tool = machine.getCurrentTool();
		
		// find size of text block
		Rectangle2D r = textCalculateBounds(text);

		output.write("G90;\n");
		liftPen(output);
		
		//if(true) {
		if(draw_bounding_box) {
			// draw bounding box
			output.write("G0 X"+TX((float)r.getMinX())+" Y"+TY((float)r.getMaxY())+";\n");
			lowerPen(output);
			output.write("G0 X"+TX((float)r.getMaxX())+" Y"+TY((float)r.getMaxY())+";\n");
			output.write("G0 X"+TX((float)r.getMaxX())+" Y"+TY((float)r.getMinY())+";\n");
			output.write("G0 X"+TX((float)r.getMinX())+" Y"+TY((float)r.getMinY())+";\n");
			output.write("G0 X"+TX((float)r.getMinX())+" Y"+TY((float)r.getMaxY())+";\n");
			liftPen(output);
		}
		
		// move to first line height
		// assumes we are still G90
		float message_start = TX((float)r.getMinX()) + SX(padding);
		float firstline = TY((float)r.getMinY()) - SY(padding + letter_height);
		float interline = -SY(letter_height + line_spacing); 

		output.write("G0 X"+message_start+" Y"+firstline+";\n");
		output.write("G91;\n");

		// draw line of text
		String [] lines = textWrapToLength(text);
		for(int i=0; i<lines.length; i++) {
			if(i>0) {
				// newline
				output.write("G0 Y"+interline+";\n");

				// carriage return
				output.write("G90;\n");
				output.write("G0 X"+message_start+";\n");
				output.write("G91;\n");
			}
			
			textDrawLine(lines[i], output);
		}

		output.write("G90;\n");
		liftPen(output);
	}

	
	// break the text into an array of strings.  each string is one line of text made to fit into the chars_per_line limit.
	protected String [] textWrapToLength(String src) {
		String [] test_lines = src.split("\n");
		int i,j;
		
		int num_lines = 0;
		for(i=0;i<test_lines.length;++i) {
			if( test_lines[i].length() > chars_per_line ) {
				int x = (int)Math.ceil( (double)test_lines[i].length() / (double)chars_per_line );
				num_lines += x;	
			} else {
				num_lines++;
			}
		}

		String [] lines = new String[num_lines];
		j=0;
		for(i=0;i<test_lines.length;++i) {
			if(test_lines[i].length() <= chars_per_line) {
				lines[j++] = test_lines[i];
			} else {
				String [] temp = test_lines[i].split("(?<=\\G.{"+chars_per_line+"})");
				for(int k=0;k<temp.length;++k) {
					lines[j++] = temp[k];
				}
			}
		}
		
		return lines;
	}
	
	protected int textLongestLine(String [] lines) {
		int len=0;
		for(int i=0;i<lines.length;++i) {
			if(len < lines[i].length()) len = lines[i].length();
		}
		
		return len;
	}
	
 	protected void textDrawLine(String a1, Writer output) throws IOException {
		String ud = alphabetFolder;//System.getProperty("user.dir") + "/" + alphabetFolder;
		
		//System.out.println(a1+" ("+a1.length()+")");
		
		int i=0;
		for(i=0;i<a1.length();++i) {
			char letter = a1.charAt(i);
			
			if(letter=='\n' || letter=='\r') continue;

			String name;

			// find the file that goes with this character
			// TODO load these from an XML description?
			if('a'<= letter && letter <= 'z') {
				name="SMALL_" + Character.toUpperCase(letter);
			} else {
				switch(letter) {
				case ' ':  name="SPACE";  break;
				case '!':  name="EXCLAMATION";  break;
				case '"':  name="DOUBLEQ";  break;
				case '$':  name="DOLLAR";  break;
				case '#':  name="POUND";  break;
				case '%':  name="PERCENT";  break;
				case '&':  name="AMPERSAND";  break;				
				case '\'':  name="SINGLEQ";  break;
				case '(':  name="B1OPEN";  break;
				case ')':  name="B1CLOSE";  break;
				case '*':  name="ASTERIX";  break;
				case '+':  name="PLUS";  break;
				case ',':  name="COMMA";  break;
				case '-':  name="HYPHEN";  break;
				case '.':  name="PERIOD";  break;
				case '/':  name="FSLASH";  break;
				case ':':  name="COLON";  break;
				case ';':  name="SEMICOLON";  break;
				case '<':  name="GREATERTHAN";  break;
				case '=':  name="EQUAL";  break;
				case '>':  name="LESSTHAN";  break;
				case '?':  name="QUESTION";  break;
				case '@':  name="AT";  break;
				case '[':  name="B2OPEN";  break;
				case ']':  name="B2CLOSE";  break;
				case '^':  name="CARET";  break;
				case '_':  name="UNDERSCORE";  break;
				case '`':  name="GRAVE";  break;
				case '{':  name="B3OPEN";  break;
				case '|':  name="BAR";  break;
				case '}':  name="B3CLOSE";  break;
				case '~':  name="TILDE";  break;
				case '\\':  name="BSLASH";  break;
				case '…':  name="SPACE";  break;
				default: name=Character.toString(letter);  break;
				}
			}
			String fn = ud + name  + ".NGC";
			final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(fn);
			if(inputStream != null) {
				if(i>0 && kerning!=0) {
					output.write("G0 X"+SX(kerning)+";\n");
				}
				try (	final InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
						final BufferedReader in = new BufferedReader(inputStreamReader)) {

					String b;
					while ((b = in.readLine()) != null) {
						if (b.trim().length() == 0)
							continue;
						if (b.equals("UP")) {
							output.write("G90;\n");
							liftPen(output);
							output.write("G91;\n");
						} else if (b.equals("DOWN")) {
							output.write("G90;\n");
							lowerPen(output);
							output.write("G91;\n");
						} else {
							StringTokenizer st = new StringTokenizer(b);
							String gap = "";
							while (st.hasMoreTokens()) {
								String c = st.nextToken();
								if (c.startsWith("G")) {
									output.write(gap + c);
								} else if (c.startsWith("X")) {
									// translate coordinates
									final float x = Float.parseFloat(c.substring(1)) * 10; // cm to mm
									output.write(gap + "X" + SX(x));
								} else if (c.startsWith("Y")) {
									// translate coordinates
									final float y = Float.parseFloat(c.substring(1)) * 10; // cm to mm
									output.write(gap + "Y" + SY(y));
								} else {
									output.write(gap + c);
								}
								gap = " ";
							}
							output.write(";\n");
						}
					}
				}
			} else {
				// file not found
				System.out.println("file not found. Making best guess as to where it is.");
				System.out.print(fn);
				System.out.println(" NOK");
			}
		}
	}
	
	protected void signName(Writer out) throws IOException {
		float desired_scale=0.5f;  // changes the size of the font.  large number = larger font
		
		textSetAlign(Align.RIGHT);
		textSetVAlign(VAlign.BOTTOM);
		textSetPosition(TX(image_width)*(1.0f/desired_scale), 
				       -TY(image_height)*(1.0f/desired_scale));

		float xx=w2;
		float yy=h2;
		float old_scale = scale;
		h2=0;
		w2=0;
		scale=desired_scale;
		
		textSetCharsPerLine(25);

		textCreateMessageNow("Makelangelo #"+Long.toString(machine.getUID()),out);
		//TextCreateMessageNow("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890<>,?/\"':;[]!@#$%^&*()_+-=\\|~`{}.",out);
		h2=yy;
		w2=xx;
		scale = old_scale;
	}
}

/**
 * This file is part of DrawbotGUI.
 *
 * DrawbotGUI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * DrawbotGUI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with DrawbotGUI.  If not, see <http://www.gnu.org/licenses/>.
 */