package com.marginallyclever.generators;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.marginallyclever.makelangelo.MakelangeloRobotSettings;
import com.marginallyclever.makelangelo.Log;
import com.marginallyclever.makelangelo.Translator;


public class Generator_YourMessageHere extends ImageGenerator {
	// text properties
	private float kerning = 5.0f;
	private float letterWidth = 10.0f;
	private float letterHeight = 20.0f;
	private float lineSpacing = 5.0f;
	private float padding = 5.0f;
	static final String ALPHABET_FOLDER = "ALPHABET/";
	private int charsPerLine = 25;
	private boolean draw_bounding_box = false;

	// text position and alignment
	public enum VAlign {
		TOP, MIDDLE, BOTTOM
	}

	public enum Align {LEFT, CENTER, RIGHT}

	private VAlign align_vertical = VAlign.MIDDLE;
	private Align align_horizontal = Align.CENTER;
	private float posx = 0;
	private float posy = 0;

	private static String lastMessage = "";
	private static int lastFont = 0;
	private static int lastSize = 20;
	private static Font [] fontList;
	private static String [] fontNames;

	public Generator_YourMessageHere(MakelangeloRobotSettings mc) {
		super(mc);
		
		// build list of fonts
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		fontList = ge.getAllFonts();
		fontNames = new String[fontList.length];
		
		Locale locale = Locale.getDefault();
		int i=0;
		for(Font f : fontList) {
			fontNames[i++] = f.getFontName(locale);
		}
	}

	@Override
	public String getName() {
		return Translator.get("YourMsgHereName");
	}

	protected void setupTransform() {
		super.setupTransform();

		double imageHeight = machine.getPaperHeight()*machine.getPaperMargin();
		double imageWidth = machine.getPaperWidth()*machine.getPaperMargin();
		h2 = (float)imageHeight / 2.0f;
		w2 = (float)imageWidth / 2.0f;

		scale = 1;  // 10mm = 1cm

		double newWidth = imageWidth;
		double newHeight = imageHeight;

		if (imageWidth > machine.getPaperWidth()) {
			float resize = (float) machine.getPaperWidth() / (float) imageWidth;
			scale *= resize;
			newHeight *= resize;
			newWidth = machine.getPaperWidth();
		}
		if (newHeight > machine.getPaperHeight()) {
			float resize = (float) machine.getPaperHeight() / (float) newHeight;
			scale *= resize;
			newWidth *= resize;
			newHeight = machine.getPaperHeight();
		}
		newWidth *= machine.getPaperMargin();
		newHeight *= machine.getPaperMargin();

		textFindCharsPerLine(newWidth);

		posx = w2;
		posy = h2;
	}


	protected void setupTransform(int width, int height) {
		int imageHeight = height;
		int imageWidth = width;
		h2 = imageHeight / 2;
		w2 = imageWidth / 2;

		scale = 10f;  // 10mm = 1cm

		double newWidth = imageWidth;
		double newHeight = imageHeight;

		if (imageWidth > machine.getPaperWidth()) {
			float resize = (float) machine.getPaperWidth() / (float) imageWidth;
			scale *= resize;
			newHeight *= resize;
			newWidth = machine.getPaperWidth();
		}
		if (newHeight > machine.getPaperHeight()) {
			float resize = (float) machine.getPaperHeight() / (float) newHeight;
			scale *= resize;
			newWidth *= resize;
			newHeight = machine.getPaperHeight();
		}
		scale *= machine.getPaperMargin();
		newWidth *= machine.getPaperMargin();
		newHeight *= machine.getPaperMargin();

		textFindCharsPerLine(newWidth);

		posx = w2;
		posy = h2;
	}


	@Override
	public boolean generate(String dest) {
		final JTextArea text = new JTextArea(lastMessage, 6, 60);
		final JFormattedTextField size = new JFormattedTextField(NumberFormat.getIntegerInstance());
		size.setValue(lastSize);
		final JComboBox<String> fontChoices = new JComboBox<String>(fontNames);
		fontChoices.setSelectedIndex(lastFont);
		
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 1;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;

		c.gridx=0;
		panel.add(size,c);
		c.gridx=1;
		panel.add(fontChoices,c);
		c.gridx=0;
		c.gridy++;
		c.gridwidth=2;
		panel.add(new JScrollPane(text),c);

		int result = JOptionPane.showConfirmDialog(null, panel, getName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			lastMessage = text.getText();
			lastSize = Integer.parseInt(size.getText());
			lastFont = fontChoices.getSelectedIndex();
			createMessage(fontNames[lastFont],lastSize,lastMessage, dest);
			//createMessage("TimesRoman",مرحبا بالعالم",18");
			//createMessage("TimesRoman",36,"Makelangelo");

			return true;
		}
		return false;
	}

	void writeBeautifulMessage(String fontName,int fontSize, String text, Writer output) throws IOException {
		Font font = new Font(fontName, Font.PLAIN, fontSize);
		FontRenderContext frc = new FontRenderContext(null,true,true);
		TextLayout textLayout = new TextLayout(text,font,frc);
		Shape s = textLayout.getOutline(null);
		Rectangle bounds = s.getBounds();
		float dx = (float)(bounds.getWidth()/2.0);
		float dy = (float)(bounds.getHeight()/2.0);
		
		PathIterator pi = s.getPathIterator(null);
		float [] coords = new float[6];
		float [] coords2 = new float[6];
		float [] start = new float[6];
		
		while(pi.isDone() == false ) {
			int type = pi.currentSegment(coords);
			switch(type) {
			case PathIterator.SEG_CLOSE:
				tool.writeMoveTo(output, start[0]-dx, -start[1]-dy);
				tool.writeOff(output);
				break;
			case PathIterator.SEG_LINETO:
				tool.writeMoveTo(output, coords[0]-dx, -coords[1]-dy);
				coords2[0] = coords[0];
				coords2[1] = coords[1];
				break;
			case PathIterator.SEG_MOVETO:
				// move without drawing
				start[0] = coords2[0] = coords[0];
				start[1] = coords2[1] = coords[1];
				tool.writeMoveTo(output, start[0]-dx, -start[1]-dy);
				tool.writeOn(output);
				break;
			case PathIterator.SEG_CUBICTO:
				for(int i=0;i<8;++i) {
					float t = (float)i/10.0f;
					// p = a0 + a1*t + a2 * tt + a3*ttt;
					float tt=t*t;
					float ttt=tt*t;
					float x = coords2[0] + (coords[0]*t) + (coords[2]*tt) + (coords[4]*ttt);
					float y = coords2[1] + (coords[1]*t) + (coords[3]*tt) + (coords[5]*ttt);
					tool.writeMoveTo(output, x-dx,-y-dy);
				}
				tool.writeMoveTo(output, coords[4]-dx,-coords[5]-dy);
				coords2[0] = coords[4];
				coords2[1] = coords[5];
				break;
			case PathIterator.SEG_QUADTO:
				for(int i=0;i<8;++i) {
					float t = (float)i/10.0f;
					//(1-t)²*P0 + 2t*(1-t)*P1 + t²*P2
					float u = (1.0f-t);
					float tt=u*u;
					float ttt=2.0f*t*u;
					float tttt=t*t;
					float x = coords2[0]*tt + (coords[0]*ttt) + (coords[2]*tttt);
					float y = coords2[1]*tt + (coords[1]*ttt) + (coords[3]*tttt);
					tool.writeMoveTo(output, x-dx,-y-dy);
				}
				tool.writeMoveTo(output, coords[2]-dx,-coords[3]-dy);
				coords2[0] = coords[2];
				coords2[1] = coords[3];
				break;
			}
			pi.next();
		}
	}

	private void createMessage(String fontName, int fontSize, String str, String dest) {
		try (final OutputStream fileOutputStream = new FileOutputStream(dest);
				final Writer output = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)) {

			tool = machine.getCurrentTool();

			w2=0;
			h2=0;
			posx=0;
			posy=0;
			scale=1;

			textFindCharsPerLine(machine.getPaperWidth()*machine.getPaperMargin());

			output.write(machine.getConfigLine() + ";\n");
			output.write(machine.getBobbinLine() + ";\n");
			tool.writeChangeTo(output);

			textSetAlign(Align.CENTER);
			textSetVAlign(VAlign.MIDDLE);
			//textCreateMessageNow(lastMessage, output);
			writeBeautifulMessage(fontName,fontSize,lastMessage,output);

			w2=0;
			h2=0;
			posx=0;
			posy=0;
			scale=1;
			textSetAlign(Align.RIGHT);
			textSetVAlign(VAlign.TOP);
			textSetPosition((float)((machine.getPaperWidth()/2.0f)*10.0f*machine.getPaperMargin()),
					(float)((machine.getPaperHeight()/2.0f)*10.0f*machine.getPaperMargin()));
		} catch (IOException e) {
			Log.error( e.getMessage() );
		}
	}
	public void textSetPosition(float x, float y) {
		posx = x;
		posy = y;
	}

	public void textSetAlign(Align x) {
		align_horizontal = x;
	}

	public void textSetVAlign(VAlign x) {
		align_vertical = x;
	}


	public void textSetCharsPerLine(int numChars) {
		charsPerLine = numChars;
		//System.out.println("MAX="+numChars);
	}


	public void textFindCharsPerLine(double width) {
		charsPerLine = (int) Math.floor((float) (width * 10.0f - padding * 2.0f) / (float) (letterWidth + kerning));
		//System.out.println("MAX="+chars_per_line);
	}


	/**
	 * calculate the smallest rectangle that would fit around the string of text
	 * @param text the message to fit around
	 * @return a Rectangle2D that describes the minimum fit
	 */
	private Rectangle2D textCalculateBounds(String text) {
		String[] lines = textWrapToLength(text);
		int len = textLongestLine(lines);

		int num_lines = lines.length;
		float h = padding * 2 + (letterHeight + lineSpacing) * num_lines;//- line_spacing; removed because of letters that hang below the line
		float w = padding * 2 + (letterWidth + kerning) * len - kerning;
		float xmax = 0, xmin = 0, ymax = 0, ymin = 0;

		switch (align_horizontal) {
		case LEFT:
			xmax = posx + w;
			xmin = posx;
			break;
		case CENTER:
			xmax = posx + w / 2;
			xmin = posx - w / 2;
			break;
		case RIGHT:
			xmax = posx;
			xmin = posx - w;
			break;
		}

		switch (align_vertical) {
		case BOTTOM:
			ymax = posy + h;
			ymin = posy;
			break;
		case MIDDLE:
			ymax = posy + h / 2;
			ymin = posy - h / 2;
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


	private void textCreateMessageNow(String text, Writer output) throws IOException {
		if (charsPerLine <= 0) return;

		tool = machine.getCurrentTool();

		// find size of text block
		Rectangle2D r = textCalculateBounds(text);

		output.write("G90;\n");
		liftPen(output);

		if (draw_bounding_box) {
			// draw bounding box
			output.write("G0 X" + TX((float) r.getMinX()) + " Y" + TY((float) r.getMaxY()) + ";\n");
			lowerPen(output);
			output.write("G0 X" + TX((float) r.getMaxX()) + " Y" + TY((float) r.getMaxY()) + ";\n");
			output.write("G0 X" + TX((float) r.getMaxX()) + " Y" + TY((float) r.getMinY()) + ";\n");
			output.write("G0 X" + TX((float) r.getMinX()) + " Y" + TY((float) r.getMinY()) + ";\n");
			output.write("G0 X" + TX((float) r.getMinX()) + " Y" + TY((float) r.getMaxY()) + ";\n");
			liftPen(output);
		}

		// move to first line height
		// assumes we are still G90
		float message_start = TX((float) r.getMinX()) + SX(padding);
		float firstline = TY((float) r.getMinY()) - SY(padding + letterHeight);
		float interline = -SY(letterHeight + lineSpacing);

		output.write("G0 X" + message_start + " Y" + firstline + ";\n");
		output.write("G91;\n");

		// draw line of text
		String[] lines = textWrapToLength(text);
		for (int i = 0; i < lines.length; i++) {
			if (i > 0) {
				// newline
				output.write("G0 Y" + interline + ";\n");

				// carriage return
				output.write("G90;\n");
				output.write("G0 X" + message_start + ";\n");
				output.write("G91;\n");
			}

			textDrawLine(lines[i], output);
		}

		output.write("G90;\n");
		liftPen(output);
	}


	// break the text into an array of strings.  each string is one line of text made to fit into the chars_per_line limit.
	private String[] textWrapToLength(String src) {
		String[] test_lines = src.split("\n");
		int i, j;

		int num_lines = 0;
		for (i = 0; i < test_lines.length; ++i) {
			if (test_lines[i].length() > charsPerLine) {
				int x = (int) Math.ceil((double) test_lines[i].length() / (double) charsPerLine);
				num_lines += x;
			} else {
				num_lines++;
			}
		}

		String[] lines = new String[num_lines];
		j = 0;
		for (i = 0; i < test_lines.length; ++i) {
			if (test_lines[i].length() <= charsPerLine) {
				lines[j++] = test_lines[i];
			} else {
				String[] temp = test_lines[i].split("(?<=\\G.{" + charsPerLine + "})");
				for (String aTemp : temp) {
					lines[j++] = aTemp;
				}
			}
		}

		return lines;
	}

	private int textLongestLine(String[] lines) {
		int len = 0;
		for (String line : lines) {
			if (len < line.length()) len = line.length();
		}

		return len;
	}

	private void textDrawLine(String a1, Writer output) throws IOException {
		String ud = ALPHABET_FOLDER;

		Log.message( a1 +"("+ a1.length() +")" );

		int i = 0;
		for (i = 0; i < a1.length(); ++i) {
			char letter = a1.charAt(i);

			if (letter == '\n' || letter == '\r') continue;

			String name;

			// find the file that goes with this character
			if ('a' <= letter && letter <= 'z') {
				name = "SMALL_" + Character.toUpperCase(letter);
			} else {
				switch (letter) {
				case ' ':
					name = "SPACE";
					break;
				case '!':
					name = "EXCLAMATION";
					break;
				case '"':
					name = "DOUBLEQ";
					break;
				case '$':
					name = "DOLLAR";
					break;
				case '#':
					name = "POUND";
					break;
				case '%':
					name = "PERCENT";
					break;
				case '&':
					name = "AMPERSAND";
					break;
				case '\'':
					name = "SINGLEQ";
					break;
				case '(':
					name = "B1OPEN";
					break;
				case ')':
					name = "B1CLOSE";
					break;
				case '*':
					name = "ASTERIX";
					break;
				case '+':
					name = "PLUS";
					break;
				case ',':
					name = "COMMA";
					break;
				case '-':
					name = "HYPHEN";
					break;
				case '.':
					name = "PERIOD";
					break;
				case '/':
					name = "FSLASH";
					break;
				case ':':
					name = "COLON";
					break;
				case ';':
					name = "SEMICOLON";
					break;
				case '<':
					name = "GREATERTHAN";
					break;
				case '=':
					name = "EQUAL";
					break;
				case '>':
					name = "LESSTHAN";
					break;
				case '?':
					name = "QUESTION";
					break;
				case '@':
					name = "AT";
					break;
				case '[':
					name = "B2OPEN";
					break;
				case ']':
					name = "B2CLOSE";
					break;
				case '^':
					name = "CARET";
					break;
				case '_':
					name = "UNDERSCORE";
					break;
				case '`':
					name = "GRAVE";
					break;
				case '{':
					name = "B3OPEN";
					break;
				case '|':
					name = "BAR";
					break;
				case '}':
					name = "B3CLOSE";
					break;
				case '~':
					name = "TILDE";
					break;
				case '\\':
					name = "BSLASH";
					break;
				case '…':
					name = "SPACE";
					break;
				default:
					name = Character.toString(letter);
					break;
				}
			}
			String fn = ud + name + ".NGC";
			final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(fn);
			if (inputStream != null) {
				if (i > 0 && kerning != 0) {
					output.write("G0 X" + SX(kerning) + ";\n");
				}
				try (final InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
						final BufferedReader in = new BufferedReader(inputStreamReader)) {

					String b;
					while ((b = in.readLine()) != null) {
						if (b.trim().length() == 0)
							continue;
						switch (b) {
						case "UP":
							output.write("G90;\n");
							liftPen(output);
							output.write("G91;\n");
							break;
						case "DOWN":
							output.write("G90;\n");
							lowerPen(output);
							output.write("G91;\n");
							break;
						default:
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
							break;
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

	public void signName(Writer out) throws IOException {
		setupTransform();

		float desired_scale = 0.5f;  // changes the size of the font.  large number = larger font

		textSetAlign(Align.RIGHT);
		textSetVAlign(VAlign.BOTTOM);
		textSetPosition((float)(machine.getPaperWidth() *10.0f*machine.getPaperMargin()),
				(float)(machine.getPaperHeight()*10.0f*machine.getPaperMargin()));

		float xx = w2;
		float yy = h2;
		float old_scale = scale;
		h2 = 0;
		w2 = 0;
		scale = desired_scale;

		textSetCharsPerLine(25);

		textCreateMessageNow("Makelangelo #" + Long.toString(machine.getUID()), out);
		//TextCreateMessageNow("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890<>,?/\"':;[]!@#$%^&*()_+-=\\|~`{}.",out);
		h2 = yy;
		w2 = xx;
		scale = old_scale;
	}
}
