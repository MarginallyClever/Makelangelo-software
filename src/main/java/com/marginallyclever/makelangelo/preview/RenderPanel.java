package com.marginallyclever.makelangelo.preview;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import static com.marginallyclever.donatello.graphview.GraphViewPanel.setHints;

/**
 * Software rendering WYSIWYG preview of the the {@link com.marginallyclever.makelangelo.paper.Paper}, the current
 * {@link com.marginallyclever.makelangelo.turtle.Turtle}, and the
 * {@link com.marginallyclever.makelangelo.plotter.Plotter} (in that order).
 */
public class RenderPanel extends JPanel implements MouseWheelListener, MouseListener, MouseMotionListener {
	private static final Logger logger = LoggerFactory.getLogger(RenderPanel.class);

	private int canvasWidth, canvasHeight;

	private final List<RenderListener> renderListeners = new ArrayList<>();
	
	private Camera camera;

	public Color backgroundColor = new Color(255-67,255-67,255-67);

	/**
	 * button state tracking
	 */
	private int buttonPressed = MouseEvent.NOBUTTON;

	/**
	 * previous mouse position
	 */
	private int mouseOldX, mouseOldY;
	private int mouseX,mouseY;

	/**
	 * mouseLastZoomDirection is used to prevent reverse zooming on track pads, bug #559.
	 */
	private int mouseLastZoomDirection = 0;

	public RenderPanel() {
		super(new BorderLayout());
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension size = getSize();
                reshape(size.width, size.height);
            }
        });

		// start animation system
		logger.debug("  starting animator...");
	}

	@Override
	public void mousePressed(MouseEvent e) {
		buttonPressed = e.getButton();
		mouseOldX = e.getX();
		mouseOldY = e.getY();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		buttonPressed = MouseEvent.NOBUTTON;
	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mouseDragged(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		mouseX = x;
		mouseY = y;
		setTipXY();

		if (buttonPressed == MouseEvent.BUTTON1) {
			int dx = x - mouseOldX;
			int dy = y - mouseOldY;
			mouseOldX = x;
			mouseOldY = y;
			camera.moveRelative(-dx, -dy);
			repaint();
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int notches = e.getWheelRotation();
		if (notches == 0) return;

		Point2d p = new Point2d(e.getPoint().x,e.getPoint().y);
		Rectangle r = this.getBounds();
		p.x -= r.getCenterX();
		p.y -= r.getCenterY();

		if (notches > 0) {
			if (mouseLastZoomDirection == -1) camera.zoom(-1,p);
			mouseLastZoomDirection = -1;
		} else {
			if (mouseLastZoomDirection == 1) camera.zoom(1,p);
			mouseLastZoomDirection = 1;
		}
		repaint();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		mouseOldX = x;
		mouseOldY = y;
		mouseX = x;
		mouseY = y;
		setTipXY();
	}

	public void addRenderListener(RenderListener arg0) {
		renderListeners.add(arg0);
	}
	
	public void removePreviewListener(RenderListener arg0) {
		renderListeners.remove(arg0);
	}

	/**
	 * Set up the correct projection so the image appears in the right location and aspect ratio.
	 */
	private void reshape(int width, int height) {
		canvasWidth = width;
		canvasHeight = height;
        if(camera!=null) {
            camera.setWidth(canvasWidth);
            camera.setHeight(canvasHeight);
        }
	}

	public Vector2d getMousePositionInWorld() {
        if(camera==null) return new Vector2d(0,0);

		double w2 = camera.getWidth()/2.0;
		double h2 = camera.getHeight()/2.0;
		double z = camera.getZoom();
		Vector2d cursorInSpace = new Vector2d(mouseX-w2, mouseY-h2);
		cursorInSpace.scale(1.0/z);
		return new Vector2d(camera.getX()+cursorInSpace.x,
							-(camera.getY()+cursorInSpace.y));
	}

	private void setTipXY() {
		Vector2d p = getMousePositionInWorld();
		this.setToolTipText((int)p.x + ", " + (int)p.y);
	}

    /**
     * Refresh the image in the view.  This is where drawing begins.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        var g2d =(Graphics2D) g;
        setHints(g2d);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        eraseEverything(g);

        try {
            applyCameraTransform(g2d);
            g2d.scale(1,-1);

            for (RenderListener p : renderListeners) {
                p.render(g2d);
            }
            g2d.scale(1,-1);
        } finally {
            g2d.dispose();
        }
	}

    /**
     * Applies the transformation of the camera to the provided {@link Graphics} object.
     * It modifies the graphics context to reflect the camera's position, zoom level, and dimensions,
     * ensuring that subsequent renderings are aligned with the view of the camera.
     *
     * @param g the {@link Graphics} object to which the camera transformation will be applied
     */
    private void applyCameraTransform(Graphics g) {
        if(camera==null) return;

        Graphics2D g2d = (Graphics2D)g;
        double z = camera.getZoom();
        g2d.translate(camera.getWidth()/2.0, camera.getHeight()/2.0);
        g2d.scale(z, z);
        g2d.translate(-camera.getX(), -camera.getY());
    }

	private void eraseEverything(Graphics g) {
        g.setColor(backgroundColor);
        g.fillRect(0,0,getWidth(),getHeight());
    }

	public void setCamera(Camera camera) {
		this.camera = camera;
	}

	@Override
	public void addNotify() {
		super.addNotify();
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addMouseWheelListener(this);
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		this.removeMouseListener(this);
		this.removeMouseMotionListener(this);
		this.removeMouseWheelListener(this);
	}
}