package com.marginallyclever.makelangelo.plotter.plotterControls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

/**
 * {@link CartesianButtons} is a GUI to provide Pronterface-style buttons for XY
 * driving. {@link ActionListener}s can listen for click events on each button.
 * Button {@link ActionEvent}s are named for their button. Button are divided
 * into quadrants. In each quadrant, outside zones are lower numbers than inside
 * zones. Quadrants are numbered counter-clockwise, starting with eastern
 * quadrant.  So the zone 1 is the +100 east and zone 11 is the -1 south.
 * The center button is 12.
 * 
 * @author Dan Royer
 * @since 7.28.0
 */
public class CartesianButtons extends JComponent {
	private static final Logger logger = LoggerFactory.getLogger(CartesianButtons.class);

	private static final long serialVersionUID = 1L;

	public static final int NUM_ZONES_PER_QUADRANT = 3;
	public static final int TOTAL_ZONES = NUM_ZONES_PER_QUADRANT * 4 + 1;
	public static final int ZONE_CENTER = NUM_ZONES_PER_QUADRANT * 4;

	private int centerRadius = 30;
	private int buttonWidth = 30;
	private int highlightZone = -1;
	private Color highlightColor;
	private String[] labels = new String[TOTAL_ZONES];

	private ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();

	public CartesianButtons() {
		super();

		assignDefaultLabels();

		setMinimumSize(getPreferredSize());
		setMaximumSize(getPreferredSize());

		addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				if (!isEnabled())
					return;
				// Log.message("moved");
				int zone = getZoneUnderPoint(e.getPoint());
				if (highlightZone != zone) {
					highlightZone = zone;
					highlightColor = getColorButtonHighlight();
					repaint();
				}
			}
		});

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (!isEnabled())
					return;
				highlightZone = getZoneUnderPoint(e.getPoint());
				highlightColor = getColorButtonSelect();
				// Log.message("pressed");
				repaint();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				highlightColor = getColorButtonHighlight();
				// Log.message("released");
				int zone = getZoneUnderPoint(e.getPoint());
				if (highlightZone == zone) {
					// Log.message("clicked zone "+zone);
					notifyActionListeners(new ActionEvent(this, zone, "clicked"));
				}
				highlightZone = -1;
				repaint();
			}
		});
	}

	private void assignDefaultLabels() {
		int j = 0;
		int v = 2;
		for (int a = 0; a < 4; ++a) {
			int n = 100 * (v > 0 ? 1 : -1);
			for (int i = NUM_ZONES_PER_QUADRANT - 1; i >= 0; --i) {
				labels[j++] = Integer.toString(n);
				n /= 10;
			}
			--v;
		}

		labels[ZONE_CENTER] = "Home";
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Rectangle r = this.getBounds();
		g.translate(r.width / 2, r.height / 2);

		drawAllQuadrantButtons(g);
		drawCenterButton(g);
	}

	private void drawAllQuadrantButtons(Graphics g) {
		g.setColor(new Color(1.0f, 0.0f, 0.0f));

		int k = 0;
		for (int a = 0; a < 4; ++a) {
			for (int i = NUM_ZONES_PER_QUADRANT - 1; i >= 0; --i) {
				int j = i + 1;
				int angle = a * 90;
				drawArcingButtonInternal(g, angle - 44, angle + 44, centerRadius + i * buttonWidth,
						centerRadius + j * buttonWidth, (k == highlightZone), labels[k]);
				k++;
			}
		}
	}

	private void drawCenterButton(Graphics g) {
		if (highlightZone == NUM_ZONES_PER_QUADRANT * 4 && this.isEnabled()) {
			g.setColor(highlightColor);
		} else {
			g.setColor(getColorControl());
		}
		g.fillArc(-centerRadius, -centerRadius, centerRadius * 2, centerRadius * 2, 0, 360);
		g.setColor(this.isEnabled() ? getColorForegroundText() : getColorDisabledText());
		g.drawArc(-centerRadius, -centerRadius, centerRadius * 2, centerRadius * 2, 0, 360);

		drawCenteredText(g, labels[ZONE_CENTER], 0, 0);
	}

	private void drawCenteredText(Graphics g, String string, int x, int y) {
		FontMetrics fm = g.getFontMetrics();
		Rectangle2D r = fm.getStringBounds(string, g);
		int w = (int) r.getWidth();
		int h = (int) r.getHeight() - fm.getLeading();

		g.setColor(this.isEnabled() ? getColorForegroundText() : getColorDisabledText());
		g.drawString(string, x - w / 2, y + h / 2);
	}

	private Color getColorControl() {
		Color c = UIManager.getColor("control");
		if (c == null)
			c = SystemColor.control;
		return c;
	}

	private Color getColorDisabledText() {
		Color c = UIManager.getColor("Button.disabledText");
		if (c == null)
			c = SystemColor.lightGray;
		return c;
	}

	// UIManager.getColor("Button.darkShadow")

	private Color getColorForegroundText() {
		Color c = UIManager.getColor("Label.foreground");
		if (c == null)
			c = SystemColor.darkGray;
		return c;
	}

	private Color getColorButtonHighlight() {
		Color c = UIManager.getColor("Button.highlight");
		if (c == null)
			c = SystemColor.controlHighlight;
		return c;
	}

	private Color getColorButtonSelect() {
		Color c = UIManager.getColor("Button.select");
		if (c == null)
			c = SystemColor.textHighlight;
		return c;
	}

	/**
	 * @param p
	 * @return The zone under point p. in each quadrant, outside zones are lower
	 *         numbers than inside zones. Quadrants are numbered counter-clockwise,
	 *         starting with eastern quadrant.
	 */
	private int getZoneUnderPoint(Point p) {
		Rectangle r = this.getBounds();
		double dx = p.x - r.width / 2;
		double dy = -(p.y - r.height / 2);
		int len = (int) Math.sqrt(dx * dx + dy * dy);
		if (len < centerRadius)
			return NUM_ZONES_PER_QUADRANT * 4;

		double mouseAngle = (Math.toDegrees(Math.atan2(dy, dx) + Math.PI) + 180) % 360;
		int quadrant = (int) ((mouseAngle + 45) / 90) % 4;
		// 0 west 1 north 2 east 3 south
		int zone = (int) ((len - centerRadius) / buttonWidth);
		if (zone >= NUM_ZONES_PER_QUADRANT)
			return -1; // miss
		zone = NUM_ZONES_PER_QUADRANT - 1 - zone;

		// Log.message(dx+"\t"+dy+"\t"+mouseAngle+"\t"+quadrant+"\t"+len);

		return quadrant * NUM_ZONES_PER_QUADRANT + zone;
	}

	private void drawArcingButtonInternal(Graphics g, int startAngle, int endAngle, int r0, int r1, boolean highlight,
			String label) {
		boolean shouldHighlight = highlight && this.isEnabled();
		g.setColor(shouldHighlight ? highlightColor : getColorControl());

		g.fillArc(-r1, -r1, r1 * 2, r1 * 2, startAngle, endAngle - startAngle);
		g.setColor(getColorControl());
		g.fillArc(-r0, -r0, r0 * 2, r0 * 2, startAngle, endAngle - startAngle);

		g.setColor(this.isEnabled() ? getColorForegroundText() : getColorDisabledText());
		// g.setColor(SystemColor.BLACK);
		// g.drawArc(-r0, -r0, r0*2, r0*2, startAngle, endAngle-startAngle);
		g.drawArc(-r1, -r1, r1 * 2, r1 * 2, startAngle, endAngle - startAngle);
		drawLineInternal(g, startAngle, r0, r1);
		drawLineInternal(g, endAngle, r0, r1);

		drawLabel(g, (endAngle + startAngle) / 2, (r1 + r0) / 2, label);
	}

	private void drawLabel(Graphics g, int angle, int radius, String label) {
		double r = Math.toRadians(angle);
		double s = Math.sin(r);
		double c = Math.cos(r);
		int x1 = (int) Math.round(c * radius);
		int y1 = (int) Math.round(s * radius);
		drawCenteredText(g, label, x1, -y1);
	}

	private void drawLineInternal(Graphics g, int angle, int r0, int r1) {
		double r = Math.toRadians(angle);
		double s = Math.sin(r);
		double c = Math.cos(r);

		int x1 = (int) Math.round(c * r0);
		int y1 = (int) Math.round(s * r0);

		int x2 = (int) Math.round(c * r1);
		int y2 = (int) Math.round(s * r1);

		g.drawLine(x1, -y1, x2, -y2);
	}

	public void addActionListener(ActionListener a) {
		listeners.add(a);
	}

	public void removeActionListener(ActionListener a) {
		listeners.remove(a);
	}

	private void notifyActionListeners(ActionEvent ae) {
		for (ActionListener a : listeners)
			a.actionPerformed(ae);
	}

	@Override
	public Dimension getPreferredSize() {
		int w = (centerRadius + NUM_ZONES_PER_QUADRANT * buttonWidth) * 2;
		int h = (centerRadius + NUM_ZONES_PER_QUADRANT * buttonWidth) * 2;
		return new Dimension(w + 1, h + 1);
	}

	/**
	 * 
	 * @param id
	 * @return quandrant 0-4, or 5 for center button.
	 */
	public static boolean isCenterZone(int id) {
		return id == ZONE_CENTER;
	}

	/**
	 * 
	 * @param id
	 * @return quandrant 0-4, or 5 for center button.
	 */
	public static int getQuadrant(int id) {
		return (int) (id / NUM_ZONES_PER_QUADRANT);
	}

	/**
	 * 
	 * @param id
	 * @return zone number, or -1 for
	 */
	public static int getZone(int id) {
		if (id >= ZONE_CENTER)
			return -1;
		return (int) (id % NUM_ZONES_PER_QUADRANT);
	}

	public String getLabel(int id) throws IllegalArgumentException {
		if (id < 0 || id >= TOTAL_ZONES)
			throw new IllegalArgumentException("must be 0...TOTAL_ZONES-1");
		return labels[id];
	}

	public void setLabel(int id, String arg0) throws IllegalArgumentException {
		if (id < 0 || id >= TOTAL_ZONES)
			throw new IllegalArgumentException("must be 0...TOTAL_ZONES-1");
		labels[id] = arg0;
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("CartesianButtons");
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel p = new JPanel();
		frame.add(p);
		CartesianButtons button = new CartesianButtons();
		p.add(button);
		button.addActionListener((e) -> {
			logger.debug("{} {}", e.getActionCommand(), button.getLabel(e.getID()));
		});

		frame.pack();
		frame.setVisible(true);
	}
}
