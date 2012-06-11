/**@(#)drawbotGUI.java
 *
 * drawbot application with GUI
 *
 * @author Dan Royer (dan@marginallyclever.com)
 * @version 1.00 2012/2/28
 */


// io functions
import gnu.io.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.MouseInputListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.prefs.Preferences;



public class DrawbotGUI
		extends JPanel
		implements ActionListener, SerialPortEventListener
{
	static final long serialVersionUID=1;
	static private final String cue = "> ";
	static private final String eol = ";";
	static private final String NL = System.getProperty("line.separator");;

	// Serial connection
	static int BAUD_RATE = 57600;
	CommPortIdentifier portIdentifier;
	CommPort commPort;
	SerialPort serialPort;
	InputStream in;
	OutputStream out;
	String[] portsDetected;
	boolean portOpened=false;
	boolean portConfirmed=false;
	boolean readyToReceive=false;
	
	// Preferences
	Preferences prefs = Preferences.userRoot().node("DrawBot");
	String[] recentFiles = {"","","","","","","","","",""};
	String recentPort;
	
	// Robot config
	double limit_top=10;
	double limit_bottom=-10;
	double limit_left=-10;
	double limit_right=10;
	
	// paper area (stock material
	double paper_top=10;
	double paper_bottom=-10;
	double paper_left=-10;
	double paper_right=10;
	
	// GUI elements
	static JFrame mainframe;
    JMenuBar menuBar;
    JMenuItem buttonOpenFile, buttonExit;
    JMenuItem buttonConfig, buttonPaper, buttonRescan, buttonLoad, buttonHome;
    JMenuItem buttonStart, buttonPause, buttonHalt, buttonDrive;
    JCheckBoxMenuItem buttonMoveImage;
    JMenuItem buttonAbout;
    
    JMenuItem [] buttonRecent = new JMenuItem[10];
    JMenuItem [] buttonPorts;

    // tabs
	JTextArea log;
	JScrollPane logPane;
	JTextArea ngcfile;
	//JScrollPane filePane;
	DrawPanel previewPane;

	// status bar
	StatusBar statusBar;
	DecimalFormat fmt = new DecimalFormat("#.##");
	
	// parsing input from Drawbot
	String line3="";

	// reading file
	boolean running=false;
	boolean paused=true;
    Scanner scanner;
	long linesTotal=0;
	long linesProcessed=0;
	boolean fileOpened=false;

	// movement style
	final int MODE_MOVE_CAMERA = 0; 
	final int MODE_MOVE_IMAGE  = 1;

	int movementMode=MODE_MOVE_CAMERA;
	
	// scale image to fit machine
	double imageScale=1;
	double imageOffsetX=0;
	double imageOffsetY=0;
	
	// driving controls
	boolean driving=false;
	double driveScale=100;
	double deadZone=20;
	double maxZone=100;
	
	// TSP resoltion magic
	double tspSaveScale=1.0;
	
	// timing
	long t_draw_start;
	BufferedImage img = null;
	
	
	
	public String formatTime(long millis) {
    	String elapsed="";
    	long s=millis/1000;
    	long m=s/60;
    	long h=m/60;
    	m%=60;
    	s%=60;
    	if(h>0) elapsed+=h+"h";
    	if(h>0||m>0) elapsed+=m+"m";
    	elapsed+=s+"s ";
    	return elapsed;
	}
	
	
	
	// manages the status bar at the bottom of the application window
	public class StatusBar extends JLabel {
		static final long serialVersionUID=1;
		
	    /** Creates a new instance of StatusBar */
	    public StatusBar() {
	        super();
	        super.setPreferredSize(new Dimension(100, 16));
	        SetMessage("Ready");
	    }
	    
	    public void SetMessage(String message) {
	        setText(" "+message);        
	    }        
	    
	    public void SetProgress(long sofar,long total,String msg) {
	    	float progress=0;
	    	String elapsed="";
	    	if(total>0) {
	    		progress = 100.0f*(float)sofar/(float)total;
	    		
	    		long t_draw_now= (sofar>0) ? System.currentTimeMillis()-t_draw_start : 0;
	    		elapsed=formatTime(t_draw_now);
	    	}
		   	statusBar.SetMessage(fmt.format(progress)+"% ("+sofar+"/"+total+") "+elapsed+msg);
	    }
	}
	  
	

	// manages the vertical split in the GUI
	public class Splitter extends JSplitPane {
		static final long serialVersionUID=1;
		
		public Splitter(int split_direction) {
			super(split_direction);
			setResizeWeight(0.8);
			setDividerLocation(0.8);
		}
	}
	
	
	
	// Custom drawing panel written as an inner class to access the instance variables.
	public class DrawPanel extends JPanel implements MouseListener, MouseInputListener  {
		static final long serialVersionUID=2;

		// arc smoothness - increase to make more smooth and run slower.
		double steps_per_degree=10;

		// motion control
		boolean mouseIn=false;
		int buttonPressed=MouseEvent.NOBUTTON;
		int oldx, oldy;

		// scale + position
		int cx,cy;
		double cameraOffsetX=0,cameraOffsetY=0;
		double cameraZoom=20;
		float drawScale=0.1f;

		// driving controls
		public boolean driveOn=false;
		public double drivex=0, drivey=0;
		public double drivexview=0, driveyview=0;
		
		
		
		public DrawPanel() {
			super();
	        addMouseMotionListener(this);
	        addMouseListener(this);
		}

		public void StopDriving() {
	    	drivex=0;
	    	drivey=0;
	    	drivexview=0;    
	    	driveyview=0;
    		driveOn=false;
    		if(driving) SendLineToRobot("J02 X0 Y0");
		}

		public void MoveCamera(int x,int y) {
    		if(driving) {
    			// drive the plotter around
    			drivexview=(x-oldx);
	    		driveyview=(y-oldy);
	    		double len=Math.sqrt(drivexview*drivexview+driveyview*driveyview);
	    		// cap the max velocity
    			if(len>maxZone) {
    				drivexview*=maxZone/len;
    				driveyview*=maxZone/len;
    				len=maxZone;
    			}
    			// add a deadzone in the center
    			double f=len-deadZone;
	    		if(f>0) {
	    			// this scales f to [0....1] so length of (drivex,drivey) will <= 1
	    			f=f/(maxZone-deadZone);
	    			drivex=drivexview*f/len;
	    			drivey=-driveyview*f/len;
	    		} else {
	    			drivex=0;
	    			drivey=0;
	    		}
	    		if(readyToReceive) {
	    			SendLineToRobot("J02 X"+drivex+" Y"+drivey);
	    		}
    		} else {
    			// scroll the gcode preview
	    		double dx=(x-oldx)/cameraZoom;
	    		double dy=(y-oldy)/cameraZoom;
		    	cameraOffsetX-=dx;
		    	cameraOffsetY+=dy;
    		}
		}
		public void MoveImage(int x,int y) {
			// scroll the gcode preview
    		double dx=(x-oldx)/cameraZoom;
    		double dy=(y-oldy)/cameraZoom;
	    	imageOffsetX-=dx;
	    	imageOffsetY+=dy;
		}
		public void ScaleImage(int x,int y) {
			double amnt = (double)(y-oldy)*0.01;
			imageScale += amnt;
			if(imageScale<0.01) imageScale=0.01f;
		}
		public void ZoomCamera(int x,int y) {
			double amnt = (double)(y-oldy)*0.01;
			cameraZoom += amnt;
			if(cameraZoom<0.1) cameraZoom=0.1f;
		}
		public void mousePressed(MouseEvent e) {
			buttonPressed=e.getButton();
	    	oldx=e.getX();
	    	oldy=e.getY();
	    	if(driving && e.getButton()==MouseEvent.BUTTON1) {
	    		driveOn=true;
	    	}
		}
	    public void mouseReleased(MouseEvent e) {
	    	buttonPressed=MouseEvent.NOBUTTON;
	    	if(e.getButton()==MouseEvent.BUTTON1) StopDriving();
	    }
	    public void mouseClicked(MouseEvent e) {}
	    public void mouseEntered(MouseEvent e) {}
	    public void mouseExited(MouseEvent e) {
	    	StopDriving();
	    }
	    public void mouseDragged(MouseEvent e) {
	    	int x=e.getX();
	    	int y=e.getY();
	    	if(buttonPressed==MouseEvent.BUTTON1) {
	    		if(movementMode==MODE_MOVE_IMAGE) MoveImage(x,y);
	    		else MoveCamera(x,y);
	    	} else if(buttonPressed==MouseEvent.BUTTON3) {
	    		if(movementMode==MODE_MOVE_IMAGE) ScaleImage(x,y);
	    		else ZoomCamera(x,y);
	    	}
	    	oldx=x;
	    	oldy=y;
	    	repaint();
	    }
	    public void mouseMoved(MouseEvent e) {}

	    public double TX(double a) {
	    	return cx+(int)((a-cameraOffsetX)*cameraZoom);
	    }
	    public double TY(double a) {
	    	return cy-(int)((a-cameraOffsetY)*cameraZoom);
	    }
	    public double ITX(double a) {
	    	return TX(a*imageScale-imageOffsetX);
	    }
	    public double ITY(double a) {
	    	return TY(a*imageScale-imageOffsetY);
	    }
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);    // paint background
			Graphics2D g2d = (Graphics2D)g;
		   
			cx = this.getWidth()/2;
			cy = this.getHeight()/2;
			
			// draw background
			if(!portConfirmed) {			
				setBackground(Color.WHITE);
			} else {
				setBackground(Color.GRAY);
				g2d.setColor(new Color(194.0f/255.0f,133.0f/255.0f,71.0f/255.0f));
				g2d.fillRect((int)TX(limit_left),(int)TY(limit_top),
						(int)((limit_right-limit_left)*cameraZoom),
						(int)((limit_top-limit_bottom)*cameraZoom));
				g2d.setColor(Color.WHITE);
				g2d.fillRect((int)TX(paper_left),(int)TY(paper_top),
						(int)((paper_right-paper_left)*cameraZoom),
						(int)((paper_top-paper_bottom)*cameraZoom));

			}

			// draw calibration point
			g2d.setColor(Color.RED);
			g2d.drawLine((int)TX(-0.25),(int)TY( 0.00), (int)TX(0.25),(int)TY(0.00));
			g2d.drawLine((int)TX(0),    (int)TY(-0.25), (int)TX(0.00),(int)TY(0.25));
/*
			if(img!=null) {
				int w=img.getWidth();
				int h=img.getHeight();
				g.drawImage(img, 
						(int)ITX(-w/2), (int)ITY(h/2), (int)ITX(w/2), (int)ITY(-h/2), 
						0, 0, w, h,
						null);
				return;
			}*/

			// draw image
			g2d.setColor(Color.BLACK);
			
			String[] instructions = ngcfile.getText().split("\\r?\\n");
			double px=TX(0),py=TY(0),pz=90;
			int i,j;

			for(i=0;i<instructions.length;++i) {
				if(instructions[i].contains("G20")) {
					drawScale=0.393700787f;
				} else if(instructions[i].contains("G21")) {
					drawScale=0.1f;
				} else if(instructions[i].startsWith("G00 ") || instructions[i].startsWith("G0 ") || 
					instructions[i].startsWith("G01 ") || instructions[i].startsWith("G1 ")) {
					// draw a line
					double x=px;
					double y=py;
					double z=pz;
					String[] tokens = instructions[i].split("\\s");
					for(j=0;j<tokens.length;++j) {
						if(tokens[j].startsWith("X")) x = Float.valueOf(tokens[j].substring(1)) * drawScale;
						if(tokens[j].startsWith("Y")) y = Float.valueOf(tokens[j].substring(1)) * drawScale;
						if(tokens[j].startsWith("Z")) z = Float.valueOf(tokens[j].substring(1)) * drawScale;
					}

					if(z<5) g2d.drawLine((int)ITX(px),(int)ITY(py),(int)ITX(x),(int)ITY(y));
					px=x;
					py=y;
					pz=z;
				} else if(instructions[i].startsWith("G02 ") || instructions[i].startsWith("G2 ") ||
					instructions[i].startsWith("G03 ") || instructions[i].startsWith("G3 ")) {
					// draw an arc
					int dir = (instructions[i].startsWith("G02") || instructions[i].startsWith("G2")) ? -1 : 1;
					double x=px;
					double y=py;
					double z=pz;
					double ai=px;
					double aj=py;
					String[] tokens = instructions[i].split("\\s");
					for(j=0;j<tokens.length;++j) {
						if(tokens[j].startsWith("X")) x = Float.valueOf(tokens[j].substring(1)) * drawScale;
						if(tokens[j].startsWith("Y")) y = Float.valueOf(tokens[j].substring(1)) * drawScale;
						if(tokens[j].startsWith("Z")) z = Float.valueOf(tokens[j].substring(1)) * drawScale;
						if(tokens[j].startsWith("I")) ai = px + Float.valueOf(tokens[j].substring(1)) * drawScale;
						if(tokens[j].startsWith("J")) aj = py + Float.valueOf(tokens[j].substring(1)) * drawScale;
					}

					if(z<5) {
						double dx=px - ai;
						double dy=py - aj;
						double radius=Math.sqrt(dx*dx+dy*dy);
	
						// find angle of arc (sweep)
						double angle1=atan3(dy,dx);
						double angle2=atan3(y-aj,x-ai);
						double theta=angle2-angle1;
	
						if(dir>0 && theta<0) angle2+=2.0*Math.PI;
						else if(dir<0 && theta>0) angle1+=2.0*Math.PI;
	
						theta=Math.abs(angle2-angle1);
	
						// Draw the arc from a lot of little line segments.
						for(int k=0;k<=theta*steps_per_degree;++k) {
							double angle3 = (angle2-angle1) * ((double)k/(theta*steps_per_degree)) + angle1;
							float nx = (float)(ai + Math.cos(angle3) * radius);
						    float ny = (float)(aj + Math.sin(angle3) * radius);
	
						    g2d.drawLine((int)ITX(px),(int)ITY(py),(int)ITX(nx),(int)ITY(ny));
							px=nx;
							py=ny;
						}
					    g2d.drawLine((int)ITX(px),(int)ITY(py),(int)ITX(x),(int)ITY(y));
					}
					px=x;
					py=y;
					pz=z;
				}
			}  // for ( each instruction )
			
			if(driveOn) {
				double x=oldx;
				double y=oldy;
				g2d.setColor(Color.BLACK);
				// action line
				g2d.drawLine( (int)x, (int)y, (int)(x+drivexview), (int)(y+driveyview) );
				// action limits
				g2d.setColor(Color.RED);
				g2d.drawArc((int)(x-maxZone), 
						(int)(y-maxZone),
						(int)(maxZone*2),
						(int)(maxZone*2),
						0,
						360);
				g2d.setColor(Color.GREEN);
				g2d.drawArc((int)(x-deadZone), 
							(int)(y-deadZone),
							(int)(deadZone*2),
							(int)(deadZone*2),
							0,
							360);
			}
		}
	}
	
	
	
	/**
	 * A base class for image filtering
	 * @author Dan
	 */
	public class Filter {
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
	}
	
	
	/**
	 * Converts an image to black & white, reduces contrast (washes it out)
	 * @author Dan
	 *
	 */
	public class Filter_BlackAndWhiteContrast extends Filter {
		float max_intensity, min_intensity;
		float max_threshold, min_threshold;

		public BufferedImage Process(BufferedImage img) {
			int h = img.getHeight();
			int w = img.getWidth();
			int x,y,i;

			max_intensity=-1000;
			min_intensity=1000;
			for(y=0;y<h;++y) {
				for(x=0;x<w;++x) {
					i=decode(img.getRGB(x, y));
					if(max_intensity<i) max_intensity=i;
					if(min_intensity>i) min_intensity=i;
					img.setRGB(x, y, encode(i));
				}
			}
			System.out.println("min_intensity="+min_intensity);
			System.out.println("max_intensity="+max_intensity);
			
			for(y=0;y<h;++y) {
				for(x=0;x<w;++x) {
					i=decode(img.getRGB(x, y));
					
					float a = (float)(i - min_intensity) / (float)(max_intensity - min_intensity);
					int b = (int)( a * 95.0f + 190.0f );
					if(b>255) b=255;
					//if(b==255) System.out.println(x+"\t"+y+"\t"+i+"\t"+b);
					img.setRGB(x, y, encode(b));
				}
			}
			
			return img;
		}
	}
	
	
	/**
	 * Floyd/Steinberg dithering
	 * @author Dan
	 * @see {@link http://en.literateprograms.org/Floyd-Steinberg_dithering_%28C%29}<br>
	 * {@link http://www.home.unix-ag.org/simon/gimp/fsdither.c}
	 */
	public class Filter_DitherFloydSteinberg extends Filter {
		private int QuantizeColor(int original) {
			int i=(int)Math.min(Math.max(original, 0),255);
			return ( i > 127 ) ? 255 : 0;
		}
		
		
		private void DitherDirection(BufferedImage img,int y,int[] error,int[] nexterror,int direction) {
			int w = img.getWidth();
			int oldPixel, newPixel, quant_error;
			int start, end, x;

			for(x=0;x<w;++x) nexterror[x]=0;
			
			if(direction>0) {
				start=0;
				end=w;
			} else {
				start=w-1;
				end=-1;
			}
			
			// for each x from left to right
			for(x=start;x!=end;x+=direction) {
				// oldpixel := pixel[x][y]
				oldPixel = decode(img.getRGB(x, y)) + error[x];
				// newpixel := find_closest_palette_color(oldpixel)
				newPixel = QuantizeColor(oldPixel);
				// pixel[x][y] := newpixel
				img.setRGB(x, y, encode(newPixel));
				// quant_error := oldpixel - newpixel
				quant_error = oldPixel - newPixel;
				// pixel[x+1][y  ] := pixel[x+1][y  ] + 7/16 * quant_error
				// pixel[x-1][y+1] := pixel[x-1][y+1] + 3/16 * quant_error
				// pixel[x  ][y+1] := pixel[x  ][y+1] + 5/16 * quant_error
				// pixel[x+1][y+1] := pixel[x+1][y+1] + 1/16 * quant_error
					nexterror[x          ] += 5.0/16.0 * quant_error;
				if(x+direction>=0 && x+direction < w) {
					    error[x+direction] += 7.0/16.0 * quant_error;
					nexterror[x+direction] += 1.0/16.0 * quant_error;
				}
				if(x-direction>=0 && x-direction < w) {
					nexterror[x-direction] += 3.0/16.0 * quant_error;
				}
			}
		}
		
		
		public BufferedImage Process(BufferedImage img) {
			int y;
			int h = img.getHeight();
			int w = img.getWidth();
			int direction=1;
			int[] error=new int[w];
			int[] nexterror=new int[w];
			
			for(y=0;y<w;++y) {
				error[y]=nexterror[y]=0;
			}

			// for each y from top to bottom
			for(y=0;y<h;++y) {
				DitherDirection(img,y,error,nexterror,direction);
				
				direction = direction> 0 ? -1 : 1;
				int [] tmp = error;
				error=nexterror;
				nexterror=tmp;
			}
			
			return img;
		}
	}
	
	
	
	/**
	 * Reduces any picture to a more manageable size
	 * @author Dan
	 */
	public class Filter_Resize {
		protected BufferedImage scaleImage(BufferedImage img, int width, int height, Color background) {
		    int imgWidth = img.getWidth();
		    int imgHeight = img.getHeight();
		    if (imgWidth*height < imgHeight*width) {
		        width = imgWidth*height/imgHeight;
		    } else {
		        height = imgHeight*width/imgWidth;
		    }
		    BufferedImage newImage = new BufferedImage(width, height,
		            BufferedImage.TYPE_INT_RGB);
		    Graphics2D g = newImage.createGraphics();
		    try {
		        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
		                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		        g.setBackground(background);
		        g.clearRect(0, 0, width, height);
		        g.drawImage(img, 0, 0, width, height, null);
		    } finally {
		        g.dispose();
		    }
		    return newImage;
		}


		public BufferedImage Process(BufferedImage img) {
			int w = img.getWidth();
			int h = img.getHeight();
			float cm_to_mm=10.0f;
			float margin=0.9f;
			int max_w=(int)((paper_right-paper_left)*cm_to_mm*margin/tspSaveScale);
			int max_h=(int)((paper_top-paper_bottom)*cm_to_mm*margin/tspSaveScale);
			
			if(w<max_w && h<max_h) {
				if(w>h) {
					h*=(float)max_w/(float)w;
					w=max_w;
				} else {
					w*=(float)max_h/(float)h;
					h=max_h;
				}
			}
			if(w>max_w) {
				h*=(float)max_w/(float)w;
				w=max_w;
			}
			if(h>max_h) {
				w*=(float)max_h/(float)h;
				h=max_h;
			}
			return scaleImage(img, w,h,Color.WHITE);
		}
	}
	
	
	/**
	 * Generate a Gcode file from the BufferedImage supplied.<br>
	 * Use the filename given in the constructor as a basis for the gcode filename, but change the extension to .ngc 
	 * @author Dan
	 */
	class Filter_TSPGcodeGenerator extends Filter implements PropertyChangeListener {
		public class Point {
			int x, y;
			
			public Point(int _x,int _y) {
				x=_x;
				y=_y;
			}
		}
		
		private class TSPOptimizer extends SwingWorker<Void,Void> {
			@Override
			public Void doInBackground() {
				int start, end, i, j, once;
				
				setProgress(0);

				double len=GetTourLength(solution);
				DecimalFormat flen=new DecimalFormat("#.##");
				Log(flen.format(len)+"mm @ 0s\n");

				long t_elapsed=0;
				long t_start = System.currentTimeMillis();

				do {
					once=0;
					for(start=0;start<numPoints-1;++start) {
						for(end=start+2;end<=numPoints;++end) {
							// we have s1,s2...e-1,e
							// check if s1,e-1,...s2,e is shorter
							// before
							long a=CalculateWeight(solution[start],solution[start+1]);
							long b=CalculateWeight(solution[end  ],solution[end  -1]);
							// after
							long c=CalculateWeight(solution[start],solution[end  -1]);
							long d=CalculateWeight(solution[end  ],solution[start+1]);
							
							if(a+b>c+d) {
								once = 1;
								// do the flip
								i=0;
								for(j=start+1;j<end;++j) {
									solution2[i]=solution[j];
									++i;
								}
								for(j=start+1;j<end;++j) {
									--i;
									solution[j]=solution2[i];
								}
								t_elapsed=System.currentTimeMillis()-t_start;
								// find the new tour length
								len-=(Math.sqrt(a)+Math.sqrt(b)) - (Math.sqrt(c)+Math.sqrt(d));
								
								Log(flen.format(len)+"mm @ "+formatTime(t_elapsed)+": "+start+"\t"+end+"\n");
								setProgress((int)((float)t_elapsed/(float)time_limit));
							}
						}
					}
					// check if moving a point to another part of the tour makes the tour shorter
					for(start=0;start<numPoints-1;++start) {
						for(end=start+2;end<=numPoints;++end) {
							if(end>=start && end<start+3) continue;
							
							// we have points s1,s2,...e-2,e-1,e.
							// check if s1,e-1,s2,...e-2,e is shorter
							// before
							long a=CalculateWeight(solution[start],solution[start+1]);
							long b=CalculateWeight(solution[end  ],solution[end  -1]);
							long c=CalculateWeight(solution[end-1],solution[end  -2]);
							// after
							long d=CalculateWeight(solution[start],solution[end  -1]);
							long e=CalculateWeight(solution[end-1],solution[start+1]);
							long f=CalculateWeight(solution[end  ],solution[end  -2]);
							
							if(a+b+c>d+e+f) {
								once = 1;
								// do move
								i=solution[end-1];
								for(j=end-1;j>start+1;--j) {
									solution[j]=solution[j-1];
								}
								solution[j]=i;
								t_elapsed=System.currentTimeMillis()-t_start;
								// find the new tour length
								len-=(Math.sqrt(a)+Math.sqrt(b)+Math.sqrt(c)) - (Math.sqrt(d)+Math.sqrt(e)+Math.sqrt(f));
								
								Log(flen.format(len)+"mm @2 "+formatTime(t_elapsed)+": "+start+"\t"+end+"\n");
								setProgress((int)(100.0f*(float)t_elapsed/(float)time_limit));
							}
						}
					}
					// check if moving a point to another part of the tour makes the tour shorter
					for(start=0;start<numPoints-2;++start) {
						for(end=start+2;end<=numPoints;++end) {
							if(end>=start && end<start+3) continue;
							
							// we have points s1,s2,s3,...e-1,e.
							// check if s1,s3,...e-2,s2,e is shorter
							// before
							long a=CalculateWeight(solution[start  ],solution[start+1]);
							long b=CalculateWeight(solution[start+1],solution[start+2]);
							long c=CalculateWeight(solution[end  -1],solution[end    ]);
							// after
							long d=CalculateWeight(solution[start  ],solution[start+2]);
							long e=CalculateWeight(solution[end  -1],solution[start+1]);
							long f=CalculateWeight(solution[start+1],solution[end    ]);
							
							if(a+b+c>d+e+f) {
								once = 1;
								// do move
								i=solution[start+1];
								for(j=start+1;j<end-1;++j) {
									solution[j]=solution[j+1];
								}
								solution[j]=i;
								t_elapsed=System.currentTimeMillis()-t_start;
								// find the new tour length
								len-=(Math.sqrt(a)+Math.sqrt(b)+Math.sqrt(c)) - (Math.sqrt(d)+Math.sqrt(e)+Math.sqrt(f));
								
								Log(flen.format(len)+"mm @3 "+formatTime(t_elapsed)+": "+start+"\t"+end+"\n");
								setProgress((int)(100.0f*(float)t_elapsed/(float)time_limit));
							}
						}
					}
				} while(once==1 && t_elapsed<time_limit && !isCancelled());
				
				return null;
			}
			
			//@override
			public void done() {
	            Toolkit.getDefaultToolkit().beep();
	            pm.setProgress(0);
	            pm.close();
				int h = img.getHeight();
				int w = img.getWidth();
				ConvertAndSaveToGCode(w,h);

        		String ngcPair = dest.substring(0, dest.lastIndexOf('.')) + ".ngc";
        		OpenFile(ngcPair);
			}
		}

		long time_limit=10*60*1000;  // 10 minutes
		String dest;
		int numPoints;
		Point[] points = null;
		int[] solution = null;
		int[] solution2 = null;
		int scount;
		ProgressMonitor pm;
		TSPOptimizer task;

		
		Filter_TSPGcodeGenerator(String _dest) {
			dest=_dest;
		}

		protected long CalculateWeight(int a,int b) {
			long x = points[a].x - points[b].x;
			long y = points[a].y - points[b].y;
			return x*x+y*y;
		}

		private void GenerateTSP() {
			GreedyTour();

			Log("Running Lin/Kerighan optimization...\n");

			pm = new ProgressMonitor(DrawbotGUI.this, "Optimizing path...", "", 0, 100);
			pm.setProgress(0);
			pm.setMillisToPopup(0);
			task=new TSPOptimizer();
			task.addPropertyChangeListener(this);
			task.execute();
		}

	    /**
	     * Invoked when task's progress property changes.
	     */
	    public void propertyChange(PropertyChangeEvent evt) {
	        if ("progress" == evt.getPropertyName() ) {
	            int progress = (Integer) evt.getNewValue();
	            pm.setProgress(progress);
	            String message = String.format("Completed %d%%.\n", progress);
	            pm.setNote(message);
	            if (pm.isCanceled() || task.isDone()) {
	                Toolkit.getDefaultToolkit().beep();
	                if (pm.isCanceled()) {
	                    task.cancel(true);
	                    Log("Task canceled.\n");
	                } else {
	                	Log("Task completed.\n");
	                }
	            }
	        }
	 
	    }
	    
		private double CalculateLength(int a,int b) {
			return Math.sqrt(CalculateWeight(a,b));
		}
		
		/**
		 * Get the length of a tour segment
		 * @param list an array of indexes into the point list.  the order forms the tour sequence.
		 * @param start the index of the first point of the tour segment
		 * @param end the index of the last point of the tour segment
		 * @return the length of the tour
		 */
		private double GetTourLength(int[] list) {
			double w=0;
			for(int i=0;i<numPoints-1;++i) {
				w+=CalculateLength(list[i],list[i+1]);
			}
			return w;
		}

		
		/**
		 * Starting with point 0, find the next nearest point and repeat until all points have been "found".
		 */
		private void GreedyTour() {
			Log("Finding greedy tour solution...\n");

			int i;
			long w, bestw;
			int besti;
			
			// put all the points in the solution in no particular order.
			for(i=0;i<numPoints;++i) {
				solution[i]=i;
			}
			scount=1;
			
			solution[numPoints]=solution[0];
			
			do {
				// Find the nearest point not already in the line.
				// Any solution[n] where n>scount is not in the line.
				bestw=CalculateWeight(solution[scount],solution[scount-1]);
				besti=solution[scount];
				for( i=scount; i<numPoints; ++i ) {
					w=CalculateWeight(solution[i],solution[scount-1]);
					if( w < bestw ) {
						bestw=w;
						besti=i;
					}
				}
				i=solution[scount];
				solution[scount]=solution[besti];
				solution[besti]=i;
				scount++;
			} while(scount<numPoints);
		}
		
		/**
		 * Open a file and write out the edge list as a set of GCode commands.
		 * Since all the points are connected in a single loop,
		 * start at the tsp point closest to the calibration point and go around until you get back to the start.
		 */
		private void ConvertAndSaveToGCode(int width, int height) {
			Log("Converting to gcode and saving "+dest+"\n");
			
			float tspscale=(float)tspSaveScale;

			int w2=width/2;
			int h2=height/2;
			
			// find the tsp point closest to the calibration point
			int i;
			int besti=-1;
			int bestw=1000000;
			int x,y,w;
			for(i=0;i<numPoints;++i) {
				x=points[solution[i]].x-w2;
				y=points[solution[i]].y-h2;
				w=x*x+y*y;
				if(w<bestw) {
					bestw=w;
					besti=i;
				}
			}
			// rearrange the tsp point list so that the drawing starts at the nearest tsp point
			for(i=besti;i<numPoints;++i) {
				solution2[i-besti]=solution[i];
			}
			for(i=0;i<besti;++i) {
				solution2[i+(numPoints-besti)]=solution[i];
			}

			solution=solution2;
			
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(dest));
				out.write("G00 F200\n");
				out.write("M06 T0\n");
				out.write("G00 Z90\n");
				out.write("G01 X" + (points[solution[0]].x-w2)*tspscale + " Y" + (h2-points[solution[0]].y)*tspscale + "\n");
				out.write("G00 Z10\n");

				for(i=1;i<numPoints;++i) {
					out.write("G01 X" + (points[solution[i]].x-w2)*tspscale + " Y" + (h2-points[solution[i]].y)*tspscale + "\n");
				}
				out.write("G01 X" + (points[solution[0]].x-w2)*tspscale + " Y" + (h2-points[solution[0]].y)*tspscale + "\n");
				out.write("G00 Z90\n");
				out.write("G00 X0 Y0\n");
				out.close();
			}
			catch(IOException e) {
				Log("Error saving "+dest+": "+e.getMessage());
			}
			Log("Completed.\n");
		}
		
		/**
		 * The main entry point
		 * @param img the image to convert.
		 */
		public void Process(BufferedImage img) {
			System.out.println("Processing...");
			int h = img.getHeight();
			int w = img.getWidth();
			int x,y,i;
			
			// count the points
			numPoints=0;
			for(y=0;y<h;++y) {
				for(x=0;x<w;++x) {
					i=decode(img.getRGB(x,y));
					if(i==0) {
						++numPoints;
					}
				}
			}
			
			Log(numPoints + " points\n");
			points = new Point[numPoints+1];
			solution = new int[numPoints+1];
			solution2 = new int[numPoints+1];

			// collect the point data
			numPoints=0;
			for(y=0;y<h;++y) {
				for(x=0;x<w;++x) {
					i=decode(img.getRGB(x,y));
					if(i==0) {
						points[numPoints++]=new Point(x,y);
					}
				}
			}
			
			
			GenerateTSP();
		}
	}
	
	
	
	public void LoadImage(String filename) {
		try {
			img = ImageIO.read(new File(filename));
		}
		catch(IOException e) {}

		Filter_Resize rs = new Filter_Resize(); 
		img = rs.Process(img);
		Filter_BlackAndWhiteContrast bwc = new Filter_BlackAndWhiteContrast(); 
		img = bwc.Process(img);
		Filter_DitherFloydSteinberg dither = new Filter_DitherFloydSteinberg();
		img = dither.Process(img);

		String ngcPair = filename.substring(0, filename.lastIndexOf('.')) + ".ngc";
		Filter_TSPGcodeGenerator tsp = new Filter_TSPGcodeGenerator(ngcPair);
		tsp.Process(img);
	}
	
	
	
	// returns angle of dy/dx as a value from 0...2PI
	public double atan3(double dy,double dx) {
	  double a=Math.atan2(dy,dx);
	  if(a<0) a=(Math.PI*2.0)+a;
	  return a;
	}
	
	
	
	// appends a message to the log tab and system out.
	public void Log(String msg) {
		log.append(msg);
		log.setCaretPosition(log.getText().length());
		//System.out.print(msg);
	}
	
	
		
	public void ClosePort() {
		if(portOpened) {
		    if (serialPort != null) {
		        try {
		            // Close the I/O streams.
		            out.close();
		            in.close();
			        // Close the port.
			        serialPort.removeEventListener();
			        serialPort.close();
		        } catch (IOException e) {
		            // Don't care
		        }
		    }
		    
			portOpened=false;
			portConfirmed=false;
			log.setText("");
			
			UpdateMenuBar();
		}
	}
	
	
	
	// open a serial connection to a device.  We won't know it's the robot until  
	public int OpenPort(String portName) {
		if(portOpened && portName.equals(recentPort)) return 0;
		
		ClosePort();
		
		//Log("Connecting to "+portName+"..."+NL);
		
		// find the port
		try {
			portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
		}
		catch(Exception e) {
			Log("Ports could not be identified:"+e.getMessage()+NL);
			e.printStackTrace();
			return 1;
		}

		if ( portIdentifier.isCurrentlyOwned() ) {
    	    Log("Error: Another program is currently using this port."+NL);
			return 2;
		}

		// open the port
		try {
		    commPort = portIdentifier.open("DrawbotGUI",2000);
		}
		catch(Exception e) {
			Log("Port could not be opened:"+e.getMessage()+NL);
			e.printStackTrace();
			return 3;
		}

	    if( ( commPort instanceof SerialPort ) == false ) {
			Log("Error: Only serial ports are handled by this example."+NL);
			return 4;
		}

		// set the port parameters (like baud rate)
		serialPort = (SerialPort)commPort;
		try {
			serialPort.setSerialPortParams(BAUD_RATE,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
		}
		catch(Exception e) {
			Log("Port could not be configured:"+e.getMessage()+NL);
			return 5;
		}

		try {
			in = serialPort.getInputStream();
			out = serialPort.getOutputStream();
		}
		catch(Exception e) {
			Log("Streams could not be opened:"+e.getMessage()+NL);
			return 6;
		}
		
		try {
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
		}
		catch(TooManyListenersException e) {
			Log("Streams could not be opened:"+e.getMessage()+NL);
			return 7;
		}

		Log("Opened.\n");
		SetRecentPort(portName);
		portOpened=true;
		UpdateMenuBar();
		
		return 0;
	}


	// complete the handshake, update the menu, repaint the preview with the limits.
	public boolean ConfirmPort() {
		if(portConfirmed==true) return true;
		int found=line3.lastIndexOf("== HELLO WORLD ==");
		if(found >= 0) {
			String[] lines = line3.substring(found).split("\\r?\\n");
			try {
				limit_top = Float.parseFloat(lines[1].substring(1));
				limit_bottom = Float.parseFloat(lines[2].substring(1));
				limit_left = Float.parseFloat(lines[3].substring(1));
				limit_right = Float.parseFloat(lines[4].substring(1));
				portConfirmed=true;
				UpdateMenuBar();
				previewPane.repaint();
			}
			catch(NumberFormatException e) {}
		}
		return portConfirmed;
	}
	
	
	
	// find all available serial ports for the settings->ports menu.
	public String[] ListSerialPorts() {
		@SuppressWarnings("unchecked")
	    Enumeration<CommPortIdentifier> ports = (Enumeration<CommPortIdentifier>)CommPortIdentifier.getPortIdentifiers();
	    ArrayList<String> portList = new ArrayList<String>();
	    while (ports.hasMoreElements()) {
	        CommPortIdentifier port = (CommPortIdentifier) ports.nextElement();
	        if (port.getPortType() == CommPortIdentifier.PORT_SERIAL) {
	        	portList.add(port.getName());
	        }
	    }
	    portsDetected = (String[]) portList.toArray(new String[0]);
	    return portsDetected;
	}
	
	
	
	// pull the last connected port from prefs
	public void GetRecentPort() {
		recentPort = prefs.get("recent-port", portsDetected[0]);
	}

	
	
	// update the prefs with the last port connected and refreshes the menus.
	// @TODO: only update when the port is confirmed?
	public void SetRecentPort(String portName) {
		prefs.put("recent-port", portName);
		recentPort=portName;
		UpdateMenuBar();
	}
	

	
	// save paper limits
	public void SetRecentPaperSize() {
		prefs.putDouble("paper_left", paper_left);
		prefs.putDouble("paper_right", paper_right);
		prefs.putDouble("paper_top", paper_top);
		prefs.putDouble("paper_bottom", paper_bottom);
	}

	
	
	public void GetRecentPaperSize() {
		paper_left=Double.parseDouble(prefs.get("paper_left","-10"));
		paper_right=Double.parseDouble(prefs.get("paper_right","10"));
		paper_top=Double.parseDouble(prefs.get("paper_top","10"));
		paper_bottom=Double.parseDouble(prefs.get("paper_bottom","-10"));
	}

	
	
	// close the file, clear the preview tab
	public void CloseFile() {
		ngcfile.setText("");
		if(fileOpened==true && scanner != null) scanner.close();
		linesProcessed=0;
	   	fileOpened=false;
	}
	
	
	
	// Opens the file.  If the file can be opened, repaint the preview tab.
	public void OpenFile(String filename) {
		CloseFile();

	   	// load contents into file pane
	   	StringBuilder text = new StringBuilder();
	    
	    try {
	    	scanner = new Scanner(new FileInputStream(filename));
	    	linesTotal=0;
		    try {
		      while (scanner.hasNextLine()){
		        text.append(scanner.nextLine() + NL);
		        ++linesTotal;
		      }
		    }
		    finally{
		      scanner.close();
		    }

		    ngcfile.setText(text.toString());

    		scanner = new Scanner(new FileInputStream(filename));
	    }
	    catch(IOException e) {
	    	Log("File could not be opened."+NL);
	    	RemoveRecentFile(filename);
	    	return;
	    }
		
	   	UpdateRecentFiles(filename);

	    fileOpened=true;
	    paused=true;
	    statusBar.SetProgress(linesProcessed,linesTotal,"");

	    previewPane.repaint();
	}
	
	
	
	// changes the order of the recent files list in the File submenu,
	// saves the updated prefs, and refreshes the menus.
	public void UpdateRecentFiles(String filename) {
		int cnt = recentFiles.length;
		String [] newFiles = new String[cnt];
		
		newFiles[0]=filename;
		
		int i,j=1;
		for(i=0;i<cnt;++i) {
			if(!filename.equals(recentFiles[i]) && recentFiles[i] != "") {
				newFiles[j++] = recentFiles[i];
				if(j == cnt ) break;
			}
		}

		recentFiles=newFiles;

		// update prefs
		for(i=0;i<cnt;++i) {
			if(recentFiles[i] != null) prefs.put("recent-files-"+i, recentFiles[i]);
		}
		
		UpdateMenuBar();
	}
	
	

	// A file failed to load.  Remove it from recent files, refresh the menu bar.
	public void RemoveRecentFile(String filename) {
		int i;
		for(i=0;i<recentFiles.length-1;++i) {
			if(recentFiles[i]==filename) {
				break;
			}
		}
		for(;i<recentFiles.length-1;++i) {
			recentFiles[i]=recentFiles[i+1];
		}
		recentFiles[recentFiles.length-1]="";

		// update prefs
		for(i=0;i<recentFiles.length;++i) {
			if(recentFiles[i] != null) prefs.put("recent-files-"+i, recentFiles[i]);
		}
		
		UpdateMenuBar();
	}
	
	
	
	// Load recent files from prefs
	public void GetRecentFiles() {
		int i;
		for(i=0;i<recentFiles.length;++i) {
			recentFiles[i] = prefs.get("recent-files-"+i, recentFiles[i]);
		}
	}	
	
	

	// User has asked that a file be opened.
	public void OpenFileOnDemand(String filename) {
		Log("Opening file "+recentFiles[0]+"..."+NL);
		imageScale=1;
		imageOffsetX=0;
		imageOffsetY=0;
		
		String ext=filename.substring(filename.lastIndexOf('.'));
    	if(!ext.equalsIgnoreCase(".ngc")) {
//    		String ngcPair = filename.substring(0, filename.lastIndexOf('.')) + ".ngc";
//    		if(!(new File(ngcPair)).exists()) {
    			LoadImage(filename);
//    		}
    	} else {
    		OpenFile(filename);
    	}
	}

	
	
	// creates a file open dialog. If you don't cancel it opens that file.
	public void OpenFileDialog() {
	    // Note: source for ExampleFileFilter can be found in FileChooserDemo,
	    // under the demo/jfc directory in the Java 2 SDK, Standard Edition.
		String filename = (recentFiles[0].length()>0) ? filename=recentFiles[0] : "";

		FileFilter filterImage  = new FileNameExtensionFilter("Images (jpg/bmp/png/gif)", "jpg", "jpeg", "png", "wbmp", "bmp", "gif");
		FileFilter filterGCODE = new FileNameExtensionFilter("GCODE files (ngc)", "ngc");
		 
		JFileChooser fc = new JFileChooser(new File(filename));
		fc.addChoosableFileFilter(filterImage);
		fc.addChoosableFileFilter(filterGCODE);
	    if(fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
	    	OpenFileOnDemand(fc.getSelectedFile().getAbsolutePath());
	    }
	}
	
	
	
	public void GoHome() {
		String line="HOME;";
		Log(line+NL);
		try {
			out.write(line.getBytes());
		}
		catch(IOException e) {}

		readyToReceive=false;
	}
	
	
	
	/**
	 * Open the load dialog, load the spools.
	 */
	public void UpdateLoad() {
		JTextField left = new JTextField("500");
		JTextField right = new JTextField("500");
		final JComponent[] inputs = new JComponent[] {
						new JLabel("Measurements are in cm.  Positive value winds in."),
		                new JLabel("Left"), 	left,
		                new JLabel("Right"), 	right
		};
		JOptionPane.showMessageDialog(null, inputs, "Load Bobbins", JOptionPane.PLAIN_MESSAGE);

		String line="LOAD";
		if(left.getText().trim() !="") line+=" L"+left.getText().trim();
		if(right.getText().trim()!="") line+=" R"+right.getText().trim();
		line+=";";
		Log(line+NL);
		try {
			out.write(line.getBytes());
		}
		catch(IOException e) {}
	}

	
	
	/**
	 * Open the config dialog, send the config update to the robot, refresh the preview tab.
	 */
	public void UpdateConfig() {
		JTextField top = new JTextField(String.valueOf(limit_top));
		JTextField bottom = new JTextField(String.valueOf(limit_bottom));
		JTextField left = new JTextField(String.valueOf(limit_left));
		JTextField right = new JTextField(String.valueOf(limit_right));
		final JComponent[] inputs = new JComponent[] {
						new JLabel("Measurements are from your calibration point, in cm.  Left and Bottom should be negative."),
		                new JLabel("Top"), 		top,
		                new JLabel("Bottom"),	bottom,
		                new JLabel("Left"), 	left,
		                new JLabel("Right"), 	right
		};
		JOptionPane.showMessageDialog(null, inputs, "Config machine limits", JOptionPane.PLAIN_MESSAGE);

		if(left.getText().trim()!="" || right.getText().trim()!="" ||
			top.getText().trim()!="" || bottom.getText().trim()!="") {
			// Send a command to the robot with new configuration values
			String line="CONFIG T"+top.getText()+" B"+bottom.getText()+" L"+left.getText()+" R"+right.getText()+";";
			Log(line+NL);

			try {
				out.write(line.getBytes());
			}
			catch(IOException e) {}
			
			limit_top = Float.valueOf(top.getText());
			limit_bottom = Float.valueOf(bottom.getText());
			limit_right = Float.valueOf(right.getText());
			limit_left = Float.valueOf(left.getText());
			previewPane.repaint();
		}
	}

	
	
	/**
	 * Open the config dialog, update the paper size, refresh the preview tab.
	 */
	public void UpdatePaper() {
		JTextField top = new JTextField(String.valueOf(limit_top));
		JTextField bottom = new JTextField(String.valueOf(limit_bottom));
		JTextField left = new JTextField(String.valueOf(limit_left));
		JTextField right = new JTextField(String.valueOf(limit_right));
		final JComponent[] inputs = new JComponent[] {
						new JLabel("Measurements are from your calibration point, in cm.  Left and Bottom should be negative."),
		                new JLabel("Top"), 		top,
		                new JLabel("Bottom"),	bottom,
		                new JLabel("Left"), 	left,
		                new JLabel("Right"), 	right
		};
		JOptionPane.showMessageDialog(null, inputs, "Config paper limits", JOptionPane.PLAIN_MESSAGE);

		if(left.getText().trim()!="" || right.getText().trim()!="" ||
			top.getText().trim()!="" || bottom.getText().trim()!="") {
			paper_top = Float.valueOf(top.getText());
			paper_bottom = Float.valueOf(bottom.getText());
			paper_right = Float.valueOf(right.getText());
			paper_left = Float.valueOf(left.getText());
			SetRecentPaperSize();
			previewPane.repaint();
		}
	}

	
	
	// Take the next line from the file and send it to the robot, if permitted. 
	public void SendFileCommand() {
		if(paused==true) return;
		if(fileOpened==false) return;
		if(portConfirmed==false) return;
		if(linesProcessed>=linesTotal) return;
		
		String line;
		do {			
			// are there any more commands?
			line=scanner.nextLine().trim();
			++linesProcessed;
			statusBar.SetProgress(linesProcessed, linesTotal, line+NL);
			// loop until we find a line that gets sent to the robot, at which point we'll
			// pause for the robot to respond.  Also stop at end of file.
		} while(!SendLineToRobot(line) && linesProcessed<linesTotal);
		
		if(linesProcessed==linesTotal) {
			// end of file
			Halt();
		}
	}
	
	
	
	// last minute scale & translate the image 
	public String ProcessLine(String line) {
/*
 		String newLine = "";
		String first="";
		
		double f;
		String[] tokens = line.split("\\s");
		int j;
		for(j=0;j<tokens.length;++j) {
			newLine+=first;
			     if(tokens[j].startsWith("X")) {  f = (Float.valueOf(tokens[j].substring(1))*imageScale)-imageOffsetX;  newLine+="X"+f;  }
			else if(tokens[j].startsWith("Y")) {  f = (Float.valueOf(tokens[j].substring(1))*imageScale)-imageOffsetY;  newLine+="Y"+f;  }
			else if(tokens[j].startsWith("Z")) {  f =  Float.valueOf(tokens[j].substring(1));                           newLine+="Z"+f;  }
			else if(tokens[j].startsWith("I")) {  f = (Float.valueOf(tokens[j].substring(1))*imageScale);               newLine+="I"+f;  }
			else if(tokens[j].startsWith("J")) {  f = (Float.valueOf(tokens[j].substring(1))*imageScale);               newLine+="J"+f;  }
			else newLine+=tokens[j];
			first=" ";
		}
		return newLine;
*/
		return line;
	}
	
	
	
	// processes a single instruction meant for the robot.  Could be anything.
	// return true if the command is sent to the robot.
	// return false if it is not.
	public boolean SendLineToRobot(String line) {
		// tool change request?
		String [] tokens = line.split("\\s");

		// tool change?
		if(Arrays.asList(tokens).contains("M06") || Arrays.asList(tokens).contains("M6")) {
			for(int i=0;i<tokens.length;++i) {
				if(tokens[i].startsWith("T")) {
					JOptionPane.showMessageDialog(null,"Please change to tool #"+tokens[i].substring(1)+" and click OK.");
				}
			}
			// still ready to send
			return false;
		}
		
		// end of program?
		if(tokens[0]=="M02" || tokens[0]=="M2") {
			running=false;
			CloseFile();
			Log(line+NL);
			return false;
		}
		
		// other machine code to ignore?
		if(tokens[0].startsWith("M")) {
			Log(line+NL);
			return false;
		} 

		// contains a comment?  if so remove it
		int index=line.indexOf('(');
		if(index!=-1) {
			String comment=line.substring(index+1,line.lastIndexOf(')'));
			line=line.substring(0,index).trim();
			Log("* "+comment+NL);
			if(line.length()==0) {
				// entire line was a comment.
				return false;  // still ready to send
			}
		}

		// send relevant part of line to the robot
		line=ProcessLine(line)+eol;
		Log(line+NL);
		try {
			readyToReceive=false;
			out.write(line.getBytes());
		}
		catch(IOException e) {}
		
		return true;
	}
	
	
	
	
	/**
	 * stop sending commands to the robot.
	 * @todo add an e-stop command?
	 */
	public void Halt() {
		CloseFile();
		OpenFile(recentFiles[0]);
		running=false;
		paused=true;
		UpdateMenuBar();
	}
	
	
	
	// The user has done something.  respond to it.
	public void actionPerformed(ActionEvent e) {
		Object subject = e.getSource();
		
		if(subject == buttonOpenFile) {
			OpenFileDialog();
			return;
		}

		if( subject == buttonStart ) {
			if(fileOpened) OpenFile(recentFiles[0]);
			if(fileOpened) {
				paused=false;
				running=true;
				driving=false;
				UpdateMenuBar();
				t_draw_start=System.currentTimeMillis();
				SendFileCommand();
			}
			return;
		}
		if( subject == buttonPause ) {
			if(running) {
				if(paused==true) {
					paused=false;
					// @TODO: if the robot is not ready to unpause, this might fail and the program would appear to hang.
					SendFileCommand();
				} else {
					paused=true;
				}
			}
			return;
		}
		if( subject == buttonDrive ) {
			if(driving==true) {
				driving=false;
				SendLineToRobot("J00");
			} else {
				OpenPort(recentPort);
				running=false;
				paused=true;
				driving=true;
				SendLineToRobot("J01");
			}
			UpdateMenuBar();
			return;
		}
		if( subject == buttonHalt ) {
			Halt();
			return;
		}
		if( subject == buttonRescan ) {
			ListSerialPorts();
			UpdateMenuBar();
			return;
			
		}
		if( subject == buttonConfig ) {
			UpdateConfig();
			return;
		}
		if( subject == buttonPaper ) {
			UpdatePaper();
			return;
		}
		if( subject == buttonLoad ) {
			UpdateLoad();
			return;
		}	
		if( subject == buttonHome ) {
			GoHome();
			return;
		}		
		if(subject == buttonAbout ) {
			JOptionPane.showMessageDialog(null,"Created by Dan Royer (dan@marginallyclever.com)."+NL+NL
					+"Find out more at http://www.marginallyclever.com/"+NL
					+"Join the project http://github.com/i-make-robots/DrawBot/");
			return;
		}
		if(subject == buttonExit) {
			System.exit(0);  // @TODO: be more graceful?
			return;
		}
		
		int i;
		for(i=0;i<10;++i) {
			if(subject == buttonRecent[i]) {
				OpenFileOnDemand(recentFiles[i]);
				return;
			}
		}

		for(i=0;i<portsDetected.length;++i) {
			if(subject == buttonPorts[i]) {
				OpenPort(portsDetected[i]);
				return;
			}
		}
		
		if(subject == buttonMoveImage) {
			movementMode  = (movementMode != MODE_MOVE_IMAGE) ? MODE_MOVE_IMAGE : MODE_MOVE_CAMERA;
			
			buttonMoveImage.setState(movementMode==MODE_MOVE_IMAGE);
		}
	}
	
	
	
	// Deal with something robot has sent.
	public void serialEvent(SerialPortEvent events) {
        switch (events.getEventType()) {
            case SerialPortEvent.DATA_AVAILABLE:
	            try {
	            	final byte[] buffer = new byte[1024];
					int len = in.read(buffer);
					if( len>0 ) {
						String line2 = new String(buffer,0,len);
						Log(line2);
						line3+=line2;
						// wait for the cue ("> ") to send another command
						if(line3.lastIndexOf(cue)!=-1) {
							if(ConfirmPort()) {
								line3="";
								readyToReceive=true;
								SendFileCommand();
							}
						}
					}
	            } catch (IOException e) {}
                break;
        }
    }
	
	

	public JMenuBar CreateMenuBar() {
        // If the menu bar exists, empty it.  If it doesn't exist, create it.
        menuBar = new JMenuBar();

        UpdateMenuBar();
        
        return menuBar;
	}
	


	// Rebuild the contents of the menu based on current program state
	public void UpdateMenuBar() {
		JMenu menu;
        int i;
        
        menuBar.removeAll();
        
        //Build the first menu.
        menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menu.getAccessibleContext().setAccessibleDescription("What g-code to run?");
        menuBar.add(menu);
 
        buttonOpenFile = new JMenuItem("Open File...",KeyEvent.VK_O);
        buttonOpenFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.ALT_MASK));
        buttonOpenFile.getAccessibleContext().setAccessibleDescription("Open a g-code file...");
        buttonOpenFile.addActionListener(this);
        menu.add(buttonOpenFile);

        menu.addSeparator();

        // list recent files
        GetRecentFiles();
        if(recentFiles.length>0) {
        	// list files here
        	for(i=0;i<recentFiles.length;++i) {
        		if(recentFiles[i].length()==0) break;
            	buttonRecent[i] = new JMenuItem((1+i) + " "+recentFiles[i],KeyEvent.VK_1+i);
            	buttonRecent[i].addActionListener(this);
            	menu.add(buttonRecent[i]);
        	}
        	if(i!=0) menu.addSeparator();
        }

        buttonExit = new JMenuItem("Exit",KeyEvent.VK_Q);
        buttonExit.getAccessibleContext().setAccessibleDescription("Goodbye...");
        buttonExit.addActionListener(this);
        menu.add(buttonExit);

        menuBar.add(menu);

        menu = new JMenu("Settings");
        menu.setMnemonic(KeyEvent.VK_T);
        menu.getAccessibleContext().setAccessibleDescription("Adjust the robot settings.");
        
        JMenu subMenu = new JMenu("Port");
        subMenu.setMnemonic(KeyEvent.VK_P);
        subMenu.getAccessibleContext().setAccessibleDescription("What port to connect to?");
        subMenu.setEnabled(!running && !driving);
        ButtonGroup group = new ButtonGroup();

        ListSerialPorts();
        GetRecentPort();
        buttonPorts = new JRadioButtonMenuItem[portsDetected.length];
        for(i=0;i<portsDetected.length;++i) {
        	buttonPorts[i] = new JRadioButtonMenuItem(portsDetected[i]);
            if(recentPort.equals(portsDetected[i])) buttonPorts[i].setSelected(true);
            buttonPorts[i].addActionListener(this);
            group.add(buttonPorts[i]);
            subMenu.add(buttonPorts[i]);
        }

        subMenu.addSeparator();

        buttonRescan = new JMenuItem("Rescan",KeyEvent.VK_N);
        buttonRescan.getAccessibleContext().setAccessibleDescription("Rescan the available ports.");
        buttonRescan.addActionListener(this);
        subMenu.add(buttonRescan);
        
        menu.add(subMenu);

        buttonConfig = new JMenuItem("Configure machine limits",KeyEvent.VK_C);
        buttonConfig.getAccessibleContext().setAccessibleDescription("Adjust the robot shape.");
        buttonConfig.addActionListener(this);
        buttonConfig.setEnabled(portConfirmed && !running && !driving);
        menu.add(buttonConfig);

        buttonPaper = new JMenuItem("Paperure paper limits",KeyEvent.VK_C);
        buttonPaper.getAccessibleContext().setAccessibleDescription("Adjust the paper shape.");
        buttonPaper.addActionListener(this);
        buttonPaper.setEnabled(portConfirmed && !running && !driving);
        menu.add(buttonPaper);

        buttonLoad = new JMenuItem("Load bobbins");
        buttonLoad.getAccessibleContext().setAccessibleDescription("Load string onto the bobbin.");
        buttonLoad.addActionListener(this);
        buttonLoad.setEnabled(portConfirmed && !running && !driving);
        menu.add(buttonLoad);

        buttonHome = new JMenuItem("Home",KeyEvent.VK_O);
        buttonHome.getAccessibleContext().setAccessibleDescription("Recenter the plotter");
        buttonHome.addActionListener(this);
        buttonHome.setEnabled(portConfirmed && !running && !driving);
        menu.add(buttonHome);

        menuBar.add(menu);
/*
        // Image menu
        menu = new JMenu("Image");
        menu.getAccessibleContext().setAccessibleDescription("Change the image");

        buttonMoveImage = new JCheckBoxMenuItem("Move & Scale");
        buttonMoveImage.addActionListener(this);
        buttonMoveImage.setState(movementMode==MODE_MOVE_IMAGE);
        menu.add(buttonMoveImage);
*/
        menuBar.add(menu);

        // Draw menu
        menu = new JMenu("Draw");
        menu.setMnemonic(KeyEvent.VK_D);
        menu.getAccessibleContext().setAccessibleDescription("Start & Stop progress");
        menu.setEnabled(portConfirmed);

        buttonStart = new JMenuItem("Start",KeyEvent.VK_S);
        buttonStart.getAccessibleContext().setAccessibleDescription("Start sending g-code");
        buttonStart.addActionListener(this);
    	buttonStart.setEnabled(portConfirmed && !running && !driving);
        menu.add(buttonStart);

        buttonPause = new JMenuItem("Pause",KeyEvent.VK_P);
        buttonPause.getAccessibleContext().setAccessibleDescription("Pause sending g-code");
        buttonPause.addActionListener(this);
        buttonPause.setEnabled(portConfirmed && running && !driving);
        menu.add(buttonPause);

        buttonHalt = new JMenuItem("Halt",KeyEvent.VK_H);
        buttonHalt.getAccessibleContext().setAccessibleDescription("Halt sending g-code");
        buttonHalt.addActionListener(this);
        buttonHalt.setEnabled(portConfirmed && running && !driving);
        menu.add(buttonHalt);

        menu.addSeparator();

        buttonDrive = new JMenuItem((driving?"Stop":"Start") + " Driving",KeyEvent.VK_R);
        buttonDrive.getAccessibleContext().setAccessibleDescription("Etch-a-sketch style driving");
        buttonDrive.addActionListener(this);
        buttonDrive.setEnabled(portConfirmed && !running);
        menu.add(buttonDrive);

        menuBar.add(menu);

        //Build in the menu bar.
        menu = new JMenu("Help");
        menu.setMnemonic(KeyEvent.VK_H);
        menu.getAccessibleContext().setAccessibleDescription("Get help");

        buttonAbout = new JMenuItem("About",KeyEvent.VK_A);
        menu.getAccessibleContext().setAccessibleDescription("Find out about this program");
        buttonAbout.addActionListener(this);
        menu.add(buttonAbout);

        menuBar.add(menu);

        // finish
        menuBar.updateUI();
    }
	
	
	
    public Container CreateContentPane() {
        //Create the content-pane-to-be.
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setOpaque(true);
        
        // the log panel
        log = new JTextArea();
        log.setEditable(false);
        log.setForeground(Color.GREEN);
        log.setBackground(Color.BLACK);
        logPane = new JScrollPane(log);
        // the file panel
        ngcfile = new JTextArea();
        ngcfile.setEditable(false);
        //filePane = new JScrollPane(ngcfile);
        // the preview panel
        previewPane = new DrawPanel();
        
        // the tabs
        //JTabbedPane tabs = new JTabbedPane();
        //tabs.add("Preview",previewPane);
        //tabs.add("File",filePane);
        // status bar
        statusBar = new StatusBar();

        // layout
        Splitter split = new Splitter(JSplitPane.VERTICAL_SPLIT);
        //split.add(tabs);
        split.add(previewPane);
        split.add(logPane);
        split.setDividerSize(2);
        
        contentPane.add(split,BorderLayout.CENTER);
        contentPane.add(statusBar,BorderLayout.SOUTH);

        // open the file
		GetRecentFiles();
		if(recentFiles[0].length()>0) {
			OpenFileOnDemand(recentFiles[0]);
		}
		
		// connect to the last port
		ListSerialPorts();
		GetRecentPort();
		if(Arrays.asList(portsDetected).contains(recentPort)) {
			OpenPort(recentPort);
		}
		
		GetRecentPaperSize();
		
        return contentPane;
    }
    
    
    
    // Create the GUI and show it.  For thread safety, this method should be invoked from the event-dispatching thread.
    private static void CreateAndShowGUI() {
        //Create and set up the window.
    	mainframe = new JFrame("Drawbot GUI v2012-03-26");
        mainframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        //Create and set up the content pane.
        DrawbotGUI demo = new DrawbotGUI();
        mainframe.setJMenuBar(demo.CreateMenuBar());
        mainframe.setContentPane(demo.CreateContentPane());
 
        //Display the window.
        mainframe.setSize(500,700);
        mainframe.setVisible(true);
    }
    
    
    
    public static void main(String[] args) {
	    //Schedule a job for the event-dispatching thread:
	    //creating and showing this application's GUI.
	    javax.swing.SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	            CreateAndShowGUI();
	        }
	    });
    }
}
