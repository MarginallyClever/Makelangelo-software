package com.marginallyclever.makelangeloRobot;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class CartesianButtons extends JComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final int NUM_ZONES_PER_QUADRANT=3;
	public static final int TOTAL_ZONES=NUM_ZONES_PER_QUADRANT*4+1;
	public static final int ZONE_OUTSIDE=0;
	public static final int ZONE_MIDDLE=1;
	public static final int ZONE_INSIDE=2;
	public static final int ZONE_CENTER = NUM_ZONES_PER_QUADRANT*4;
		
	private int centerRadius=30;
	private int buttonWidth=30;
	private int highlightZone=-1;
	private Color highlightColor;
	private String [] labels = new String[TOTAL_ZONES];

	private ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();
	
	public CartesianButtons() {
		super();
		
		assignDefaultLabels();
		
		setMinimumSize(getPreferredSize());
		setMaximumSize(getPreferredSize());

		addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseDragged(MouseEvent e) {}

			@Override
			public void mouseMoved(MouseEvent e) {
				if(!isEnabled()) return;
				//System.out.println("moved"); 
				int zone = getZoneUnderPoint(e.getPoint());
				if( highlightZone != zone ) {
					highlightZone = zone;
					highlightColor = UIManager.getColor("Button.highlight");
					repaint();
				}
			}
		});
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {}

			@Override
			public void mousePressed(MouseEvent e) {
				if(!isEnabled()) return; 
				highlightZone = getZoneUnderPoint(e.getPoint());
				highlightColor = UIManager.getColor("Button.select");
				//System.out.println("pressed");
				repaint();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				highlightColor = UIManager.getColor("Button.highlight");
				//System.out.println("released");
				int zone = getZoneUnderPoint(e.getPoint());
				if(highlightZone == zone) { 
					//System.out.println("clicked zone "+zone);
					notifyActionListeners(new ActionEvent(this,zone,"clicked"));
				}
				highlightZone=-1;
				repaint();
			}
		});
	}
	
	private void assignDefaultLabels() {
		int j=0;
		int v = 2;
		for(int a=0;a<4;++a) {
			int n = 100 * (v>0?1:-1);
			for(int i=NUM_ZONES_PER_QUADRANT-1;i>=0;--i) {
				n/=10;
				labels[j++]=Integer.toString(n);
			}
			--v;
		}
		
		labels[ZONE_CENTER]="Home";
	}
	

	@Override
	public void paint(Graphics g) {
		drawAllQuadrantButtons(g);
		drawCenterButton(g);
		super.paint(g);
	}

	private void drawAllQuadrantButtons(Graphics g) {
		Rectangle r = this.getBounds();
		g.translate(r.width/2, r.height/2);
		g.setColor(new Color(1.0f,0.0f,0.0f));

		int k = 0;
		for(int a=0;a<4;++a) {
			for(int i=NUM_ZONES_PER_QUADRANT-1;i>=0;--i) {
				int j = i+1;
				int angle = a*90;
				drawArcingButtonInternal(g,
						angle-44,
						angle+44,
						centerRadius+i*buttonWidth,
						centerRadius+j*buttonWidth,
						(k==highlightZone),
						labels[k]);
				k++;
			}
		}
	}

	private void drawCenterButton(Graphics g) {
		if(highlightZone==NUM_ZONES_PER_QUADRANT*4 && this.isEnabled()) {
			g.setColor(highlightColor);
		} else {
			g.setColor(UIManager.getColor("control"));
		}
		g.fillArc(-centerRadius, -centerRadius, centerRadius*2, centerRadius*2, 0, 360);
		g.setColor(this.isEnabled() ? UIManager.getColor("Button.darkShadow") : UIManager.getColor("Button.disabledText") );
		g.drawArc(-centerRadius, -centerRadius, centerRadius*2, centerRadius*2, 0, 360);
		drawCenteredText(g,labels[ZONE_CENTER],0,0);
	}

	private void drawCenteredText(Graphics g, String string, int x, int y) {
		FontMetrics fm = g.getFontMetrics();
		Rectangle2D r = fm.getStringBounds(string, g);
		int w = (int)r.getWidth();
		int h = (int)r.getHeight()-fm.getLeading();
		
		g.setColor(this.isEnabled() ? UIManager.getColor("Label.foreground") : UIManager.getColor("Button.disabledText"));
		g.drawString(string, x-w/2, y+h/2);
	}

	/**
	 * @param p
	 * @return The zone under point p.  in each quadrant, outside zones are lower numbers than inside zones.  Quadrants are numbered counter-clockwise, starting with eastern quadrant.  
	 */
	private int getZoneUnderPoint(Point p) {
		Rectangle r = this.getBounds();
		double dx = p.x - r.width /2;
		double dy = -(p.y - r.height/2);
		int len = (int)Math.sqrt(dx*dx+dy*dy);
		if(len<centerRadius) return NUM_ZONES_PER_QUADRANT*4;

		double mouseAngle = (Math.toDegrees(Math.atan2(dy, dx)+Math.PI)+180)%360;
		int quadrant = (int)((mouseAngle+45)/90)%4;
		// 0 west 1 north 2 east 3 south
		int zone = (int)((len-centerRadius)/buttonWidth);
		if(zone>=NUM_ZONES_PER_QUADRANT) return -1;  // miss
		zone = NUM_ZONES_PER_QUADRANT-1-zone;
		
		//System.out.println(dx+"\t"+dy+"\t"+mouseAngle+"\t"+quadrant+"\t"+len);

		return quadrant*NUM_ZONES_PER_QUADRANT+zone;
	}

	private void drawArcingButtonInternal(Graphics g,int startAngle,int endAngle,int r0,int r1,boolean highlight,String label) {
		if(highlight && this.isEnabled()) {
			g.setColor(highlightColor);
		} else {
			g.setColor(UIManager.getColor("control"));
		}
		g.fillArc(-r1, -r1, r1*2, r1*2, startAngle, endAngle-startAngle);
		g.setColor(UIManager.getColor("control"));
		g.fillArc(-r0, -r0, r0*2, r0*2, startAngle, endAngle-startAngle);

		g.setColor(this.isEnabled() ? UIManager.getColor("Button.darkShadow") : UIManager.getColor("Button.disabledText") );
		//g.drawArc(-r0, -r0, r0*2, r0*2, startAngle, endAngle-startAngle);
		g.drawArc(-r1, -r1, r1*2, r1*2, startAngle, endAngle-startAngle);
		drawLineInternal(g,startAngle,r0,r1);
		drawLineInternal(g,endAngle,r0,r1);

		drawLabel(g,(endAngle+startAngle)/2,(r1+r0)/2,label);
	}
	
	private void drawLabel(Graphics g, int angle, int radius, String label) {
		double r = Math.toRadians(angle);
		double s = Math.sin(r);
		double c = Math.cos(r);
		int x1=(int)Math.round(c*radius);
		int y1=(int)Math.round(s*radius);
		drawCenteredText(g,label,x1,-y1);
	}

	private void drawLineInternal(Graphics g, int angle, int r0, int r1) {
		double r = Math.toRadians(angle);
		double s = Math.sin(r);
		double c = Math.cos(r);
		
		int x1=(int)Math.round(c*r0);
		int y1=(int)Math.round(s*r0);
		
		int x2=(int)Math.round(c*r1);
		int y2=(int)Math.round(s*r1);
		
		g.drawLine(x1,-y1,x2,-y2);
	}
	
	public void addActionListener(ActionListener a) {
		listeners.add(a);
	}
	
	public void removeActionListener(ActionListener a) {
		listeners.remove(a);
	}
	
	private void notifyActionListeners(ActionEvent ae) {
		for( ActionListener a : listeners ) a.actionPerformed(ae);
	}

	@Override
	public Dimension getPreferredSize() {
		int w = (centerRadius+NUM_ZONES_PER_QUADRANT*buttonWidth)*2;
		int h = (centerRadius+NUM_ZONES_PER_QUADRANT*buttonWidth)*2;
		return new Dimension(w+1,h+1);
	}
	
	/**
	 * 
	 * @param id
	 * @return quandrant 0-4, or 5 for center button.
	 */
	public static boolean isCenterZone(int id) {
		return id==ZONE_CENTER;
	}

	/**
	 * 
	 * @param id
	 * @return quandrant 0-4, or 5 for center button.
	 */
	public static int getQuadrant(int id) {
		return (int)(id/NUM_ZONES_PER_QUADRANT);
	}

	/**
	 * 
	 * @param id
	 * @return zone number, or -1 for 
	 */
	public static int getZone(int id) {
		if(id>=ZONE_CENTER) return -1;
		return (int)(id%NUM_ZONES_PER_QUADRANT);
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("Button Test");
		frame.setSize(400,400);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel p = new JPanel();
		frame.add(p);
		CartesianButtons button = new CartesianButtons();
		p.add(button);
		button.addActionListener((e)->{
			System.out.println(e.getActionCommand()+" "+e.getID());
		});
		
		frame.setVisible(true);
	}
}
