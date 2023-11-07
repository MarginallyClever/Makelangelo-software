package com.marginallyclever.makelangelo.makeart.turtlegenerator;

import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangelo.select.SelectInteger;
import com.marginallyclever.makelangelo.select.SelectOneOfMany;
import com.marginallyclever.makelangelo.select.SelectTextArea;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Your message here.  understands font families, styles, sizes, and alignment
 * @author Dan Royer
 *
 */
public class Generator_Text extends TurtleGenerator {
	private static final Logger logger = LoggerFactory.getLogger(Generator_Text.class);
	
	// text properties
	private static float kerning = 5.0f;
	private static float letterWidth = 10.0f;
	private static float letterHeight = 20.0f;
	private static float lineSpacing = 5.0f;
	private static float padding = 5.0f;
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
	private static String [] fontNames;
	
	public Generator_Text() {		
		// build list of fonts
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Font[] fontList = ge.getAllFonts();
		fontNames = new String[fontList.length];
		
		Locale locale = Locale.getDefault();
		int i=0;
		for(Font f : fontList) {
			fontNames[i++] = f.getFontName(locale);
		}

		SelectOneOfMany fontChoices;
		SelectInteger size;
		SelectTextArea text;

		add(fontChoices = new SelectOneOfMany("face",Translator.get("FontFace"),getFontNames(),getLastFont()));
		fontChoices.addSelectListener(evt->{
			setFont(fontChoices.getSelectedIndex());
			generate();
		});
		add(size = new SelectInteger("size",Translator.get("TextSize"),getLastSize()));
		size.addSelectListener(evt->{
			setSize(((Number)size.getValue()).intValue());
			generate();
		});
		add(text = new SelectTextArea("message",Translator.get("TextMessage"),getLastMessage()));
		text.addSelectListener(evt->{
			setMessage(text.getText());
			generate();
		});
	}
	
	public String [] getFontNames() {
		return fontNames;
	}
	public int getLastFont() {
		return lastFont;
	}
	public void setFont(int arg0) {
		if(arg0<0) arg0=0;
		if(arg0>=fontNames.length) arg0 = fontNames.length-1;
		lastFont = arg0;
	}
	
	
	public int getLastSize() {
		return lastSize;
	}
	public void setSize(int size) {
		if(size<1)size=1;
		lastSize=size;
	}
	
	public String getLastMessage() {
		return lastMessage;
	}
	public void setMessage(String msg) {
		lastMessage = msg;
	}

	@Override
	public String getName() {
		return Translator.get("YourMsgHereName");
	}

	protected void setupTransform() {
		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		setupTransform(
				rect.getHeight(),
				rect.getWidth()
		);
	}


	protected void setupTransform(double width, double height) {
		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		if (width > rect.getWidth()) {
			float resize = (float) rect.getWidth() / (float) width;
			height *= resize;
			width = rect.getWidth();
		}
		if (height > rect.getHeight()) {
			float resize = (float) rect.getHeight() / (float) height;
			width *= resize;
		}

		textFindCharsPerLine(width);

		posx = 0;
		posy = 0;
	}

	
	private Turtle writeBeautifulMessage(String fontName,int fontSize, String message) {
		Turtle turtle = new Turtle();
		if(message.length()<=0) {
			return turtle;
		}
		
		String[] messagePieces=message.split("\n");
		logger.debug("lines of text={}", messagePieces.length);
		
		Font font = new Font(fontName, Font.PLAIN, fontSize);
		FontRenderContext frc = new FontRenderContext(null,true,true);

		float yTotal=0;
		float yFirstStep = 0;
		float xMax=0;
		int p;
		for(p=0;p<messagePieces.length;++p) {
			String piece = messagePieces[p];
			if(piece==null || piece.length()==0) piece="\n";
			TextLayout textLayout = new TextLayout(piece,font,frc);
			Shape s = textLayout.getOutline(null);
			Rectangle bounds = s.getBounds();
			yTotal += bounds.getHeight();
			if(yFirstStep==0) yFirstStep = (float)bounds.getHeight();
			if(xMax < bounds.getWidth()) xMax = (float)bounds.getWidth();
		}
/*
		// display bounding box
		float dx = xMax/2.0f;
		float dy = -(yTotal+yFirstStep/2.0f)/2.0f;
		turtle.jumpTo(-dx, dy);
		turtle.moveTo( dx, dy);
		turtle.moveTo( dx,-dy);
		turtle.moveTo(-dx,-dy);
		turtle.moveTo(-dx, dy);
*/
		float dx = xMax / 2.0f;
		float dy = -yTotal/2.0f+yFirstStep/2.0f;

		for(p=0;p<messagePieces.length;++p) {
			String piece = messagePieces[p];
			if(piece==null || piece.length()==0) piece="\n";
			//TextLayout textLayout = new TextLayout(piece,font,frc);
			//Shape s = textLayout.getOutline(null);
			//Rectangle bounds = s.getBounds();

			writeBeautifulString(turtle,font,frc,piece, dx, dy);
			
			dy += fontSize;//bounds.getHeight();
		}
		return turtle;
	}
	
	private void writeBeautifulString(Turtle turtle,Font font, FontRenderContext frc,String text,float dx, float dy) { 
		TextLayout textLayout = new TextLayout(text,font,frc);
		Shape s = textLayout.getOutline(null);		
		PathIterator pi = s.getPathIterator(null);
		
		float [] coords = new float[6];
		float [] coords2 = new float[6];
		float [] start = new float[6];
		float n,i;
		n = 5;
		
		while(!pi.isDone()) {
			int type = pi.currentSegment(coords);
			switch (type) {
				case PathIterator.SEG_CLOSE -> {
					//logger.debug("CLOSE");
					turtle.moveTo(start[0] - dx, -start[1] - dy);
					turtle.penUp();
					coords2[0] = coords[0];
					coords2[1] = coords[1];
				}
				case PathIterator.SEG_LINETO -> {
					//logger.debug("DRAW_LINE");
					turtle.moveTo(coords[0] - dx, -coords[1] - dy);
					coords2[0] = coords[0];
					coords2[1] = coords[1];
				}
				case PathIterator.SEG_MOVETO -> {
					//logger.debug("MOVE");
					// move without drawing
					start[0] = coords2[0] = coords[0];
					start[1] = coords2[1] = coords[1];
					turtle.jumpTo(start[0] - dx, -start[1] - dy);
				}
				case PathIterator.SEG_CUBICTO -> {
					//P(t) = B(3,0)*CP + B(3,1)*P1 + B(3,2)*P2 + B(3,3)*P3
					//0 <= t <= 1
					//B(n,m) = mth coefficient of nth degree Bernstein polynomial
					//   = C(n,m) * t^(m) * (1 - t)^(n-m)
					//C(n,m) = Combinations of n things, taken m at a time
					//   = n! / (m! * (n-m)!)

					// B(3,0) = (1 - t)^3
					// B(3,1) = 3 * t * (1 - t)^2
					// B(3,2) = 3 * t^2 * (1 - t)
					// B(3,3) = t^3
					//logger.debug("CUBIC");
					for (i = 0; i < n; ++i) {
						float t = i / n;
						float t1 = (1.0f - t);
						float a = t1 * t1 * t1;
						float b = 3 * t * t1 * t1;
						float c = 3 * t * t * t1;
						float d = t * t * t;
						float x = coords2[0] * a + coords[0] * b + coords[2] * c + coords[4] * d;
						float y = coords2[1] * a + coords[1] * b + coords[3] * c + coords[5] * d;
						turtle.moveTo(x - dx, -y - dy);
					}
					turtle.moveTo(coords[4] - dx, -coords[5] - dy);
					coords2[0] = coords[4];
					coords2[1] = coords[5];
				}
				case PathIterator.SEG_QUADTO -> {
					//logger.debug("QUAD");
					for (i = 0; i < n; ++i) {
						float t = i / n;
						//(1-t)²*P0 + 2t*(1-t)*P1 + t²*P2
						float u = (1.0f - t);
						float tt = u * u;
						float ttt = 2.0f * t * u;
						float tttt = t * t;
						float x = coords2[0] * tt + (coords[0] * ttt) + (coords[2] * tttt);
						float y = coords2[1] * tt + (coords[1] * ttt) + (coords[3] * tttt);
						turtle.moveTo(x - dx, -y - dy);
					}
					turtle.moveTo(coords[2] - dx, -coords[3] - dy);
					coords2[0] = coords[2];
					coords2[1] = coords[3];
				}
			}
			pi.next();
		}
	}

	@Override
	public void generate() {
		String fontName = fontNames[lastFont];

		posx=0;
		posy=0;
		textFindCharsPerLine(myPaper.getPaperWidth()*myPaper.getPaperMargin());
		textSetAlign(Align.CENTER);
		textSetVAlign(VAlign.MIDDLE);
		Turtle turtle = writeBeautifulMessage(fontName,lastSize,lastMessage);

		turtle.translate(myPaper.getCenterX(),myPaper.getCenterY());

		notifyListeners(turtle);
	}
	
	public void textSetPosition(float x, float y) {
		posx = x;
		posy = y;
	}

	public void textSetAlign(Align x) {
		align_horizontal = x;
	}

	public void textSetVAlign(VAlign y) {
		align_vertical = y;
	}


	public void textSetCharsPerLine(int numChars) {
		charsPerLine = numChars;
		//logger.debug("MAX="+numChars);
	}


	public void textFindCharsPerLine(double width) {
		charsPerLine = (int) Math.floor((float) (width * 10.0f - padding * 2.0f) / (float) (letterWidth + kerning));
		//logger.debug("MAX="+chars_per_line);
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
			case LEFT -> {
				xmax = posx + w;
				xmin = posx;
			}
			case CENTER -> {
				xmax = posx + w / 2;
				xmin = posx - w / 2;
			}
			case RIGHT -> {
				xmax = posx;
				xmin = posx - w;
			}
		}

		switch (align_vertical) {
			case BOTTOM -> {
				ymax = posy + h;
				ymin = posy;
			}
			case MIDDLE -> {
				ymax = posy + h / 2;
				ymin = posy - h / 2;
			}
			case TOP -> {
				ymax = posy;
				ymin = posy - h;
			}
		}
		/*
	    logger.debug("{} lines", num_lines);
	    logger.debug("longest {} chars", len);
	    logger.debug("x {} to {}", xmin, xmax);
	    logger.debug("y {} to {}", ymin, ymax);
		 */
		Rectangle2D r = new Rectangle2D.Float();
		r.setRect(xmin, ymin, xmax - xmin, ymax - ymin);

		return r;
	}


	private void textCreateMessageNow(String text) {
		if (charsPerLine <= 0) return;

		// find size of text block
		Rectangle2D r = textCalculateBounds(text);

		Turtle turtle = new Turtle();

		if (draw_bounding_box) {
			// draw bounding box
			turtle.moveTo(r.getMinX(),r.getMinY());
			turtle.penDown();
			turtle.moveTo(r.getMinX(),r.getMaxY());
			turtle.moveTo(r.getMaxX(),r.getMaxY());
			turtle.moveTo(r.getMaxX(),r.getMinY());
			turtle.moveTo(r.getMinX(),r.getMinY());
			turtle.penUp();
		}

		// move to first line height
		// assumes we are still G90
		float message_start = (float) r.getMinX() + padding;
		float firstline = (float) r.getMinY() - (padding + letterHeight);
		float interline = -(letterHeight + lineSpacing);

		turtle.moveTo(message_start, firstline);

		// draw line of text
		String[] lines = textWrapToLength(text);
		for (int i = 0; i < lines.length; i++) {
			if (i > 0) {
				// newline
				turtle.moveTo(message_start, turtle.getY() + interline);
			}

			textDrawLine(turtle,lines[i]);
		}
		
		turtle.penUp();
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
				String[] lineSegments = test_lines[i].split("(?<=\\G.{" + charsPerLine + "})");
				for (String aLine : lineSegments) {
					lines[j++] = aLine;
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

	private void textDrawLine(Turtle turtle,String a1) {
		logger.debug("{} ({})", a1, a1.length());
		
		int i = 0;
		for (i = 0; i < a1.length(); ++i) {
			char letter = a1.charAt(i);

			if (letter == '\n' || letter == '\r') continue;

			String name;

			// find the file that goes with this character
			if ('a' <= letter && letter <= 'z') {
				name = "SMALL_" + Character.toUpperCase(letter);
			} else {
				name = switch (letter) {
					case ' ' -> "SPACE";
					case '!' -> "EXCLAMATION";
					case '"' -> "DOUBLEQ";
					case '$' -> "DOLLAR";
					case '#' -> "POUND";
					case '%' -> "PERCENT";
					case '&' -> "AMPERSAND";
					case '\'' -> "SINGLEQ";
					case '(' -> "B1OPEN";
					case ')' -> "B1CLOSE";
					case '*' -> "ASTERIX";
					case '+' -> "PLUS";
					case ',' -> "COMMA";
					case '-' -> "HYPHEN";
					case '.' -> "PERIOD";
					case '/' -> "FSLASH";
					case ':' -> "COLON";
					case ';' -> "SEMICOLON";
					case '<' -> "GREATERTHAN";
					case '=' -> "EQUAL";
					case '>' -> "LESSTHAN";
					case '?' -> "QUESTION";
					case '@' -> "AT";
					case '[' -> "B2OPEN";
					case ']' -> "B2CLOSE";
					case '^' -> "CARET";
					case '_' -> "UNDERSCORE";
					case '`' -> "GRAVE";
					case '{' -> "B3OPEN";
					case '|' -> "BAR";
					case '}' -> "B3CLOSE";
					case '~' -> "TILDE";
					case '\\' -> "BSLASH";
					case '…' -> "SPACE";
					default -> Character.toString(letter);
				};
			}
			String fn = ALPHABET_FOLDER + name + ".NGC";
			final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(fn);
			if (inputStream != null) {
				if (i > 0 && kerning != 0) {
					turtle.moveTo(turtle.getX()+kerning, turtle.getY());
				}
				try (final InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
						final BufferedReader in = new BufferedReader(inputStreamReader)) {

					String b;
					while ((b = in.readLine()) != null) {
						if (b.trim().length() == 0)
							continue;
						switch (b) {
							case "UP" -> turtle.penUp();
							case "DOWN" -> turtle.penDown();
							default -> {
								StringTokenizer st = new StringTokenizer(b);
								while (st.hasMoreTokens()) {
									String c = st.nextToken();
									if (c.startsWith("X")) {
										// translate coordinates
										final float x = Float.parseFloat(c.substring(1)) * 10; // cm to mm
										turtle.moveTo(turtle.getX() + x, turtle.getY());
									} else if (c.startsWith("Y")) {
										// translate coordinates
										final float y = Float.parseFloat(c.substring(1)) * 10; // cm to mm
										turtle.moveTo(turtle.getX(), turtle.getY() + y);
									}
								}
							}
						}
					}
				}
				catch(IOException e) {
					logger.error("Failed to load font", e);
				}
			} else {
				// file not found
				logger.debug("file not found. Making best guess as to where it is. {} NOK", fn);
			}
		}
	}

	public void signName() {
		setupTransform();

		Rectangle2D.Double rect = myPaper.getMarginRectangle();
		textSetAlign(Align.RIGHT);
		textSetVAlign(VAlign.BOTTOM);
		textSetPosition(
				(float)(rect.getWidth() *10.0f),
				(float)(rect.getHeight()*10.0f));

		textSetCharsPerLine(25);

		textCreateMessageNow("Makelangelo robot");
		//TextCreateMessageNow("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890<>,?/\"':;[]!@#$%^&*()_+-=\\|~`{}.");
	}
}
