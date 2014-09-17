package Filters;



import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.StringTokenizer;

import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import DrawingTools.DrawingTool;
import Makelangelo.MachineConfiguration;


/**
 * A base class for image filtering
 * @author Dan
 */
public class Filter {
	// image properties
	int image_width, image_height;
	float w2,h2,scale;
	DrawingTool tool;
	
	int color_channel=0;
	
	// text properties
	protected float kerning=5.0f;
	protected float letter_width=10.0f;
	protected float letter_height=20.0f;
	protected float line_spacing=5.0f;
	protected float padding=5.0f;
	static final String alphabetFolder = new String("ALPHABET/");
	protected int chars_per_line=25;
	protected boolean draw_bounding_box=false;
	
	// text position and alignment
	public enum VAlign { TOP, MIDDLE, BOTTOM };
	public enum Align { LEFT, CENTER, RIGHT };
	protected VAlign align_vertical = VAlign.MIDDLE;
	protected Align  align_horizontal = Align.CENTER;
	protected float posx=0;
	protected float posy=0;

	// file properties
	String dest;
	// pen position optimizing
	boolean lastup;
	float previous_x,previous_y;
	// threading
	ProgressMonitor pm;
	SwingWorker<Void,Void> parent;

	
	public void SetParent(SwingWorker<Void,Void> p) {
		parent=p;
	}
	public void SetProgressMonitor(ProgressMonitor p) {
		pm=p;
	}
	public void SetDestinationFile(String _dest) {
		dest=_dest;
	}
	
	/**
	 * Generate should be called by filters that create GCODE from a bufferedImage.
	 */
	public void Generate() {}

	/**
	 * Replace this with your generator/converter name.
	 */
	public String GetName() {  return "Unnamed";  }
	public BufferedImage Process(BufferedImage img) {
		return img;
	}

	// convert should be called by filters that modify a bufferedImage.
	public void Convert(BufferedImage img) throws IOException {}
	
	
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
	

	protected void liftPen(OutputStreamWriter out) throws IOException {
		tool.WriteOff(out);
		lastup=true;
	}
	
	
	protected void lowerPen(OutputStreamWriter out) throws IOException {
		tool.WriteOn(out);
		lastup=false;
	}

	
	protected void ImageStart(BufferedImage img,OutputStreamWriter out) throws IOException {
		MachineConfiguration mc = MachineConfiguration.getSingleton();
		tool = mc.GetCurrentTool();

		ImageSetupTransform(img);
		
		out.write(mc.GetConfigLine()+";\n");
		out.write(mc.GetBobbinLine()+";\n");

		previous_x=0;
		previous_y=0;
		
		SetAbsoluteMode(out);
	}
	
	protected void SetAbsoluteMode(OutputStreamWriter out) throws IOException {
		out.write("G00 G90;\n");
	}
	
	protected void SetRelativeMode(OutputStreamWriter out) throws IOException {
		out.write("G00 G91;\n");
	}
	
	/**
	 * setup transform from source image dimensions to destination paper dimensions.
	 * @param img source dimensions
	 */
	protected void ImageSetupTransform(BufferedImage img) {
		SetupTransform( img.getWidth(), img.getHeight() );
	}
	
	/**
	 * setup transform when there is no image to convert from.  Essentially a 1:1 transform.
	 */
	protected void SetupTransform() {
		MachineConfiguration mc = MachineConfiguration.getSingleton();
		// TODO I don't remember why these * 20 are needed here.  Find out and comment the code.
		SetupTransform( (int)mc.GetPaperWidth()*20, (int)mc.GetPaperHeight()*20 );
	}
	
	protected void SetupTransform(int width,int height) {
		image_height = height;
		image_width = width;
		h2=image_height/2;
		w2=image_width/2;
		
		MachineConfiguration mc = MachineConfiguration.getSingleton();
		
		scale=10f;

		int new_width = image_width;
		int new_height = image_height;
		
		if(image_width>mc.GetPaperWidth()) {
			float resize = (float)mc.GetPaperWidth()/(float)image_width;
			scale *= resize;
			new_height *= resize;
		}
		if(new_height>mc.GetPaperHeight()) {
			float resize = (float)mc.GetPaperHeight()/(float)new_height;
			scale *= resize;
			new_width *= resize;
		}
		scale *= mc.paper_margin;
		new_width *= mc.paper_margin;
		new_height *= mc.paper_margin;
		
		TextFindCharsPerLine(new_width);
		
		posx = w2;
		posy = h2;
	}
	
	
	protected int pointSample(BufferedImage img,int x,int y) {
		Color c = new Color(img.getRGB(x, y));
		switch(color_channel) {
		case 1: return c.getRed();
		case 2: return c.getGreen();
		case 3: return c.getBlue();
		default: return decode(c);
		}
	}

	
	protected int TakeImageSample(BufferedImage img,int x,int y) {
		// point sampling

		// 3x3 sampling
		int c=0;
		int values[]=new int[9];
		int weights[]=new int[9];
		if(y>0) {
			if(x>0) {
				values[c]=pointSample(img,x-1, y-1);
				weights[c]=1;
				c++;
			}
			values[c]=pointSample(img,x, y-1);
			weights[c]=2;
			c++;

			if(x<image_width-1) {
				values[c]=pointSample(img,x+1, y-1);
				weights[c]=1;
				c++;
			}
		}

		if(x>0) {
			values[c]=pointSample(img,x-1, y);
			weights[c]=2;
			c++;
		}
		values[c]=pointSample(img,x, y);
		weights[c]=4;
		c++;
		if(x<image_width-1) {
			values[c]=pointSample(img,x+1, y);
			weights[c]=2;
			c++;
		}

		if(y<image_height-1) {
			if(x>0) {
				values[c]=pointSample(img,x-1, y+1);
				weights[c]=1;
				c++;
			}
			values[c]=pointSample(img,x, y+1);
			weights[c]=2;
			c++;
	
			if(x<image_width-1) {
				values[c]=pointSample(img,x+1, y+1);
				weights[c]=1;
				c++;
			}
		}
		
		int value=0,j;
		int sum=0;
		for(j=0;j<c;++j) {
			value+=values[j]*weights[j];
			sum+=weights[j];
		}
		
		return value/sum;
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
	
	
	protected void MoveTo(OutputStreamWriter out,float x,float y,boolean up) throws IOException {
		float x2 = TX(x);
		float y2 = TY(y);
		
		if(up==lastup) {
			previous_x=x2;
			previous_y=y2;
		} else {
			tool.WriteMoveTo(out,previous_x,previous_y);
			tool.WriteMoveTo(out,x2,y2);
			if(up) liftPen(out);
			else   lowerPen(out);
		}
	}
	
	
	protected double RoundOff(double value) {
		return Math.floor(value * 100.0) / 100.0;
	}

	
	public void TextSetPosition(float x,float y) {
		posx=x;
		posy=y;
	}
	
	public void TextSetAlign(Align x) {
		align_horizontal = x;
	}
	
	public void TextSetVAlign(VAlign x) {
		align_vertical = x;
	}
	
	
	public void TextSetCharsPerLine(int numChars) {
		chars_per_line = numChars;
		//System.out.println("MAX="+numChars);
	}
	
	
	public void TextFindCharsPerLine(float width) {
		chars_per_line=(int)Math.floor( (float)(width - padding*2.0f) / (float)(letter_width+kerning) );
		//System.out.println("MAX="+chars_per_line);
	}
	

	protected Rectangle2D TextCalculateBounds(String text) {
		String [] lines = TextWrapToLength(text);
		int len = TextLongestLine(lines);
		
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

	
	protected void TextCreateMessageNow(String text,OutputStreamWriter output) throws IOException {
		if(chars_per_line<=0) return;

		MachineConfiguration mc = MachineConfiguration.getSingleton();
		tool = mc.GetCurrentTool();
		
		// find size of text block
		// TODO count newlines
		Rectangle2D r = TextCalculateBounds(text);

		output.write("G90;\n");
		liftPen(output);
		
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
		String [] lines = TextWrapToLength(text);
		for(int i=0; i<lines.length; i++) {
			if(i>0) {
				// newline
				output.write("G0 Y"+interline+";\n");

				// carriage return
				output.write("G90;\n");
				output.write("G0 X"+message_start+";\n");
				output.write("G91;\n");
			}
			
			TextDrawLine(lines[i],output);
		}

		output.write("G90;\n");
		liftPen(output);
	}

	
	protected String [] TextWrapToLength(String src) {
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
	
	protected int TextLongestLine(String [] lines) {
		int len=0;
		for(int i=0;i<lines.length;++i) {
			if(len < lines[i].length()) len = lines[i].length();
		}
		
		return len;
	}
	
 	protected void TextDrawLine(String a1,OutputStreamWriter output) throws IOException {
		String ud = System.getProperty("user.dir") + "/" + alphabetFolder;
		
		//System.out.println(a1+" ("+a1.length()+")");
		
		int i=0;
		for(i=0;i<a1.length();++i) {
			char letter = a1.charAt(i);
			
			if(letter=='\n' || letter=='\r') continue;

			String name;

			// find the file that goes with this character
			// TODO load these from an XML description
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
				case 'É':  name="SPACE";  break;
				default: name=Character.toString(letter);  break;
				}
			}
			String fn = ud + name  + ".NGC";
			//System.out.print(fn);
			
			
			if(new File(fn).isFile()) {
				if(i>0 && kerning!=0) {
					output.write("G0 X"+SX(kerning)+";\n");
				}
				
				// file found. copy/paste it into the temp file
				//System.out.println(" OK");
				BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fn),"UTF-8"));

				String b;
		        while ( (b = in.readLine()) != null ) {
		        	if(b.trim().length()==0) continue;
		        	if(b.equals("UP")) {
						output.write("G90;\n");
		        		liftPen(output);
						output.write("G91;\n");
		        	} else if(b.equals("DOWN")) { 
						output.write("G90;\n");
						lowerPen(output);
						output.write("G91;\n");
		        	} else {
		        		StringTokenizer st = new StringTokenizer(b);
		        		String gap="";
		        		while (st.hasMoreTokens()) {
		        			String c=st.nextToken();
		        			if(c.startsWith("G")) {
		        				output.write(gap+c);
		        			} else if(c.startsWith("X")) {
				        		// translate coordinates
		        				float x = Float.parseFloat(c.substring(1))*10;  // cm to mm
		        				output.write(gap+"X"+SX(x));
		        			} else if(c.startsWith("Y")) {
				        		// translate coordinates
		        				float x = Float.parseFloat(c.substring(1))*10;  // cm to mm
		        				output.write(gap+"Y"+SY(x));
		        			} else {
				        		output.write(gap+c);
		        			}
		        			gap=" ";
		        		}
		        		output.write(";\n");
		        	}
		        }
			} else {
				// file not found
				System.out.print(fn);
				System.out.println(" NOK");
			}
		}
	}
	
	protected void SignName(OutputStreamWriter out) throws IOException {
		TextSetAlign(Align.RIGHT);
		TextSetVAlign(VAlign.BOTTOM);
		TextSetPosition(image_width, image_height);
		TextCreateMessageNow("Makelangelo #"+Long.toString(MachineConfiguration.getSingleton().GetUID()),out);
		//TextCreateMessageNow("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890<>,?/\"':;[]!@#$%^&*()_+-=\\|~`{}.",out);
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
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */