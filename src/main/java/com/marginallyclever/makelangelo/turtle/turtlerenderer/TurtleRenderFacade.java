package com.marginallyclever.makelangelo.turtle.turtlerenderer;

import com.marginallyclever.makelangelo.preview.RenderListener;
import com.marginallyclever.makelangelo.turtle.Turtle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.vecmath.Point2d;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * <p>{@link TurtleRenderFacade} for rendering a {@link com.marginallyclever.makelangelo.turtle.Turtle} using a
 * {@link TurtleRenderer}.</p>
 * <p>The Facade helps to manage the complexity of the rendering process and provides a clean and simple interface
 * for the client code to interact with.</p>
 * @author Dan Royer
 */
public class TurtleRenderFacade implements RenderListener {
	private static final Logger logger = LoggerFactory.getLogger(TurtleRenderFacade.class);

	private TurtleRenderer myRenderer = TurtleRenderFactory.getTurtleRenderer(TurtleRenderFactory.DEFAULT);
	private Turtle myTurtle = new Turtle();
	private int first=0;
	private int last;
	private Color penDownColor = Color.BLACK;
	private Color penUpColor = Color.GREEN;
	private double penDiameter = 0.8;
	private boolean showTravel;
	private int turtleHash = 0;
    private BufferedImage mipmap0 = null;
    private BufferedImage mipmap1 = null;
    private BufferedImage mipmap2 = null;
    private BufferedImage mipmap3 = null;

	@Override
	public void render(Graphics graphics) {
		if(turtleHash != myTurtle.hashCode()) {
			turtleHash = myTurtle.hashCode();
			render((Graphics2D) graphics);
		}
        if(mipmap0 != null) {
            var g2d = (Graphics2D) graphics;

            // find the scale from the transform.
            var transform = g2d.getTransform();
            var sx = transform.getScaleX();
            var sy = transform.getScaleY();
            var scale = Math.abs(Math.min(sx, sy));
            // use the scale to determine which mipmap to use
            // scale
            // -5 is very close (mipmap0),
            // -2 is close (mipmap1)
            // -1 is medium (mipmap2)
            // -0.5 is far (mipmap3)

            BufferedImage mipmapToUse = mipmap0;
                 if(scale < 0.35) mipmapToUse = mipmap3;
            else if(scale < 0.5) mipmapToUse = mipmap2;
            else if(scale < 1.0) mipmapToUse = mipmap1;

            //System.out.println("Scale="+scale+" using mipmap size "+mipmapToUse.getWidth()+"x"+mipmapToUse.getHeight());

            var bbw = mipmap0.getWidth()/20;
            var bbh = mipmap0.getHeight()/20;
            graphics.drawImage(mipmapToUse,
                    -bbw,
                    -bbh,
                    bbw,
                    bbh,
                    0,
                    0,
                    mipmapToUse.getWidth(),
                    mipmapToUse.getHeight(),
                    null);
        }
	}

	public void dispose() {
		turtleHash = -1;  // force a re-render next time
	}

	public void render(@Nonnull Graphics2D g2d) {
		if(myTurtle.isLocked()) return;
		myTurtle.lock();
		try {
            var bounds = myTurtle.getBounds();
            mipmap0 = new BufferedImage(
                    (int)Math.ceil(bounds.width)*10,
                    (int)Math.ceil(bounds.height)*10,
                    BufferedImage.TYPE_INT_ARGB);
            mipmap1 = new BufferedImage(
                    mipmap0.getWidth()/2,
                    mipmap0.getHeight()/2,
                    BufferedImage.TYPE_INT_ARGB);
            mipmap2 = new BufferedImage(
                    mipmap1.getWidth()/2,
                    mipmap1.getHeight()/2,
                    BufferedImage.TYPE_INT_ARGB);
            mipmap3 = new BufferedImage(
                    mipmap2.getWidth()/2,
                    mipmap2.getHeight()/2,
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D bg = mipmap0.createGraphics();
            bg.scale(10,10);
            bg.translate(-bounds.x, -bounds.y);
            renderLockedTurtle(bg);
            renderMipMaps();
		}
		finally {
			myTurtle.unlock();
		}
	}

    private void renderMipMaps() {
        Graphics2D g2d;

        g2d = mipmap1.createGraphics();
        setHints(g2d);
        g2d.drawImage(mipmap0, 0, 0, mipmap1.getWidth(), mipmap1.getHeight(), null);
        g2d.dispose();

        g2d = mipmap2.createGraphics();
        setHints(g2d);
        g2d.drawImage(mipmap1, 0, 0, mipmap2.getWidth(), mipmap2.getHeight(), null);
        g2d.dispose();

        g2d = mipmap3.createGraphics();
        setHints(g2d);
        g2d.drawImage(mipmap2, 0, 0, mipmap3.getWidth(), mipmap3.getHeight(), null);
        g2d.dispose();
    }

    private void setHints(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    private void renderLockedTurtle(Graphics2D g2d) {
		// where we're at in the drawing (to check if we're between first & last)
		int showCount = 0;
		myRenderer.setPenUpColor(penUpColor);
		myRenderer.setPenDownColor(penDownColor);
		myRenderer.setShowTravel(showTravel);
		myRenderer.start(g2d);
		try {
			Point2d prev = new Point2d(0, 0);

			for (var layer : myTurtle.getLayers()) {
				if (layer.isEmpty()) continue;
				myRenderer.setPenDownColor(layer.getColor());
				myRenderer.setPenDiameter(layer.getDiameter());
				for (var line : layer.getAllLines()) {
					if (line.isEmpty()) continue;
					boolean start = true;
					for (var next : line.getAllPoints()) {
						if (showCount >= first && showCount < last) {
							if (start) {
								myRenderer.travel(prev, next);
								start = false;
							} else {
								myRenderer.draw(prev, next);
							}
						}
						prev = next;
						showCount++;
					}
				}
			}
		}
		catch(Exception e) {
			logger.error("Failed to render the turtle", e);
		}
		finally {
			myRenderer.end();
		}
	}

	public Turtle getTurtle() {
		return myTurtle;
	}

	public void setTurtle(Turtle turtle) {
		int size=0;
		if(turtle!=null) size = turtle.countPoints();
		myTurtle = turtle;
		if(myRenderer!=null) {
			myRenderer.reset();
		}

		setFirst(0);
		setLast(size);
	}

	public void setRenderer(@Nonnull TurtleRenderer render) {
		myRenderer = render;
        turtleHash = -1;  // force a re-render next time
	}

	public @Nonnull TurtleRenderer getRenderer() {
		return myRenderer;
	}

	private int getMax() {
		return (myTurtle==null) ? 0 : myTurtle.countPoints();
	}

	public void setFirst(int arg0) {
		first = Math.min(Math.max(arg0, 0), getMax());
		if(last<first) setLast(first);
		turtleHash = 0;
	}
	
	public int getFirst() {
		return first;
	}
	
	public void setLast(int arg0) {
		last = Math.min(Math.max(arg0, 0), getMax());
		if(first>last) setFirst(last);
		turtleHash = 0;
	}

	public int getLast() {
		return last;
	}

	public void setDownColor(Color penDownColor) {
		if(this.penDownColor == penDownColor) return; // no change
		this.penDownColor = penDownColor;
		turtleHash = 0;
	}

	public void setUpColor(Color penUpColor) {
		if(this.penUpColor == penUpColor) return; // no change
		this.penUpColor = penUpColor;
		turtleHash = 0;
	}

	public void setPenDiameter(double penDiameter) {
		if(this.penDiameter == penDiameter) return; // no change
		this.penDiameter = penDiameter;
		turtleHash = 0;
	}

	public void setShowTravel(boolean showTravel) {
		if(this.showTravel == showTravel) return; // no change
		this.showTravel = showTravel;
		turtleHash = 0;
	}
}
