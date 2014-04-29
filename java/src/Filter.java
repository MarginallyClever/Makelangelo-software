import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


/**
 * A base class for image filtering
 * @author Dan
 */
public class Filter {
	protected float kerning=-5.0f;
	protected float letter_width=20.0f;
	protected float letter_height=20.0f;
	protected float line_spacing=5.0f;
	protected float margin=10.0f;
	static final String alphabetFolder = new String("ALPHABET/");
	protected int chars_per_line=17;
	protected boolean draw_bounding_box=true;
	
	
	
	protected int decode(int pixel) {
		//pixel=(int)( Math.min(Math.max(pixel, 0),255) );
		float r = ((pixel>>16)&0xff);
		float g = ((pixel>> 8)&0xff);
		float b = ((pixel    )&0xff);
		return (int)( (r+g+b)/3 );
	}
	
	
	protected int encode(int i) {
		return (0xff<<24) | (i<<16) | (i<< 8) | i;
	}		

	
	protected double RoundOff(double value) {
		return Math.floor(value * 100) / 100;
	}
	

	protected void CreateMessageNow(String text,BufferedWriter out) throws IOException {
		// find size of text block
		int num_lines = (int)Math.ceil( (float)text.length() / chars_per_line );
		int len = chars_per_line;
		
		float char_width = letter_width + kerning;
		
		float total_height = letter_height * num_lines + line_spacing * (num_lines-1);
		
		float xmax = len/2.0f * char_width + margin;  // center the text, go left 50%
		float xmin = -xmax;
		float ymax = total_height/2 + margin;
		float ymin = -ymax;
		
		DrawingTool tool = MachineConfiguration.getSingleton().GetCurrentTool();			
		
		if(draw_bounding_box) {
			out.write(new String("G90\n"));
			out.write(new String("G0 X"+xmax+" Y"+ymax+"\n"));
			tool.WriteOn(out);
			out.write(new String("G0 X"+xmax+" Y"+ymin+"\n"));
			out.write(new String("G0 X"+xmin+" Y"+ymin+"\n"));
			out.write(new String("G0 X"+xmin+" Y"+ymax+"\n"));
			out.write(new String("G0 X"+xmax+" Y"+ymax+"\n"));
			tool.WriteOff(out);
			out.write(new String("G0 X0 Y0\n"));
		}

		// move to first line height
		float baseline = ymax - margin - letter_height;

		float message_start = -chars_per_line * 0.5f * char_width;
		out.write(new String("G91\n"));
		out.write(new String("G0 X"+message_start+" Y"+baseline+"\n"));

		float line_start = -chars_per_line * char_width;
		float next_line = -(letter_height + line_spacing);
		
		int i=0;
		for(int j=0;j<num_lines;++j) {
			// draw line of text
			int end = i+chars_per_line;
			if(end>text.length()) end = text.length();
			
			String subtext = text.substring(i,end);
			DrawMessageLine(subtext,out);
			out.write(new String("\n"));
			
			if(j<num_lines-1) {
				out.write(new String("G91\n"));
				out.write(new String("G0 X"+line_start+" Y"+next_line+"\n"));
			}
			i+=chars_per_line;
		}

		out.write(new String("G90\n"));
		
    	out.flush();
        out.close();
	}

	
	protected void DrawMessageLine(String a1,BufferedWriter out) throws IOException {
		String wd = System.getProperty("user.dir") + "/";
		String ud = wd + alphabetFolder;
		
		System.out.println(a1);
		System.out.println(a1.length());
		int i=0;
		for(i=0;i<a1.length();++i) {
			char c = a1.charAt(i);
			String name;
			// find the file that goes with this character
			// TODO load these from an XML description?
			if('a'<= c && c <= 'z') {
				name="SMALL_" + Character.toUpperCase(c);
			} else {
				switch(c) {
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
				default: name=Character.toString(c);  break;
				}
			}
			String fn = ud + name  + ".NGC";
			//System.out.print(fn);
			
			
			if(new File(fn).isFile()) {
				// file found. copy/paste it into the temp file
				//System.out.println(" OK");
				BufferedReader in = new BufferedReader(new FileReader(fn));
				char[] buf = new char[1000];
		        int b = 0;
		        while ( (b = in.read(buf)) >= 0) {
		        	out.write(buf, 0, b);
		        }
				out.write(new String("\n"));
				if(kerning!=0) {
					out.write(new String("G0 X"+kerning+"\n"));
				}
			} else {
				// file not found
				System.out.print(fn);
				System.out.println(" NOK");
			}
		}
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