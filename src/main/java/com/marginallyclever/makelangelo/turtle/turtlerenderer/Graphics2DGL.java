package com.marginallyclever.makelangelo.turtle.turtlerenderer;

import com.jogamp.opengl.GL2;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

/**
 * <p>A very limited {@link Graphics2D} implementation for OpenGL 2.0.</p>
 * <p>Assumes gl is already in orthographic projection and only drawing in the xy plane.</p>
 * <p>Don't forget to {@link #dispose()} when you're done.</p>
 */
public class Graphics2DGL extends Graphics2D {
    private final GL2 gl2;
    private final float[] lineWidthBuf = new float[1];
    private Paint paint = null;

    public Graphics2DGL(GL2 gl2) {
        this.gl2 = gl2;

        // save the transformation matrix
        gl2.glPushMatrix();
        // save the line width
        gl2.glGetFloatv(GL2.GL_LINE_WIDTH, lineWidthBuf, 0);

        // start drawing lines
        gl2.glBegin(GL2.GL_LINES);
    }

    @Override
    public Graphics create() {
        return new Graphics2DGL(gl2);
    }

    @Override
    public void translate(int x, int y) {
        gl2.glTranslatef(x, y, 0);
    }

    @Override
    public void translate(double tx, double ty) {
        gl2.glTranslated(tx, ty, 0);
    }

    @Override
    public void rotate(double theta) {
        gl2.glRotated(Math.toDegrees(theta), 0, 0, 1);
    }

    @Override
    public void rotate(double theta, double x, double y) {
        translate(x, y);
        rotate(theta);
        translate(-x, -y);
    }

    @Override
    public void scale(double sx, double sy) {
        gl2.glScaled(sx, sy, 1);
    }

    @Override
    public void shear(double shx, double shy) {
        // OpenGL does not have a direct equivalent to shear, so we use a transformation matrix
        gl2.glMultMatrixd(new double[]{
                1, shy, 0, 0,
                shx, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
        }, 0);
    }

    @Override
    public void transform(AffineTransform Tx) {
        double[] matrix = new double[16];
        double[] flatMatrix = new double[6];
        Tx.getMatrix(flatMatrix);
        matrix[0] = flatMatrix[0];
        matrix[1] = flatMatrix[1];
        matrix[4] = flatMatrix[2];
        matrix[5] = flatMatrix[3];
        matrix[12] = flatMatrix[4];
        matrix[13] = flatMatrix[5];
        matrix[10] = 1;
        matrix[15] = 1;
        gl2.glMultMatrixd(matrix, 0);
    }

    @Override
    public void setTransform(AffineTransform Tx) {
        gl2.glLoadIdentity();
        transform(Tx);
    }

    @Override
    public AffineTransform getTransform() {
        // OpenGL does not provide a direct way to get the current transformation matrix
        return new AffineTransform();
    }

    @Override
    public Paint getPaint() {
        // OpenGL does not handle Paint directly
        return null;
    }

    @Override
    public Composite getComposite() {
        // OpenGL does not handle Composite directly
        return null;
    }

    @Override
    public void setBackground(Color color) {
        gl2.glClearColor(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
    }

    @Override
    public Color getBackground() {
        float[] color = new float[4];
        gl2.glGetFloatv(GL2.GL_COLOR_CLEAR_VALUE, color, 0);
        return new Color(color[0], color[1], color[2], color[3]);
    }

    @Override
    public Stroke getStroke() {
        // OpenGL does not handle Stroke directly
        return null;
    }

    @Override
    public void clip(Shape s) {
        // OpenGL does not handle clipping directly
    }

    @Override
    public FontRenderContext getFontRenderContext() {
        // OpenGL does not handle fonts directly
        return null;
    }

    @Override
    public Color getColor() {
        float[] color = new float[4];
        gl2.glGetFloatv(GL2.GL_CURRENT_COLOR, color, 0);
        return new Color(color[0], color[1], color[2], color[3]);
    }

    @Override
    public void setColor(Color c) {
        paint = null;
        gl2.glColor4f(
                c.getRed() / 255.0f,
                c.getGreen() / 255.0f,
                c.getBlue() / 255.0f,
                c.getAlpha() / 255.0f);
    }

    @Override
    public void setPaintMode() {
        // OpenGL does not have a direct equivalent to AWT's paint mode
    }

    @Override
    public void setXORMode(Color c1) {
        // OpenGL does not have a direct equivalent to AWT's XOR mode
    }

    @Override
    public Font getFont() {
        // OpenGL does not handle fonts directly
        return null;
    }

    @Override
    public void setFont(Font font) {
        // OpenGL does not handle fonts directly
    }

    @Override
    public FontMetrics getFontMetrics(Font f) {
        // OpenGL does not handle fonts directly
        return null;
    }

    @Override
    public Rectangle getClipBounds() {
        // OpenGL does not handle clipping directly
        return null;
    }

    @Override
    public void clipRect(int x, int y, int width, int height) {
        gl2.glScissor(x, y, width, height);
        gl2.glEnable(GL2.GL_SCISSOR_TEST);
    }

    @Override
    public void setClip(int x, int y, int width, int height) {
        clipRect(x, y, width, height);
    }

    @Override
    public Shape getClip() {
        // OpenGL does not handle clipping directly
        return null;
    }

    @Override
    public void setClip(Shape clip) {
        // OpenGL does not handle clipping directly
    }

    @Override
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        // OpenGL does not have a direct equivalent to AWT's copy area
    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2) {
        if(paint instanceof GradientPaint gp) {
            var c1 = gp.getColor1();
            gl2.glColor3d(c1.getRed()/255.0, c1.getGreen()/255.0, c1.getBlue()/255.0);
            gl2.glVertex2i(x1, y1);
            var c2 = gp.getColor2();
            gl2.glColor3d(c2.getRed()/255.0, c2.getGreen()/255.0, c2.getBlue()/255.0);
            gl2.glVertex2i(x2, y2);
        } else {
            gl2.glVertex2i(x1, y1);
            gl2.glVertex2i(x2, y2);
        }
    }

    @Override
    public void fillRect(int x, int y, int width, int height) {
        /*
        gl2.glBegin(GL2.GL_QUADS);
        gl2.glVertex2i(x, y);
        gl2.glVertex2i(x + width, y);
        gl2.glVertex2i(x + width, y + height);
        gl2.glVertex2i(x, y + height);
        gl2.glEnd();*/
    }

    @Override
    public void clearRect(int x, int y, int width, int height) {
        gl2.glClear(GL2.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        // OpenGL does not have a direct equivalent to AWT's draw round rect
    }

    @Override
    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        // OpenGL does not have a direct equivalent to AWT's fill round rect
    }

    @Override
    public void drawOval(int x, int y, int width, int height) {
        // OpenGL does not have a direct equivalent to AWT's draw oval
    }

    @Override
    public void fillOval(int x, int y, int width, int height) {
        // OpenGL does not have a direct equivalent to AWT's fill oval
    }

    @Override
    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        // OpenGL does not have a direct equivalent to AWT's draw arc
    }

    @Override
    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        // OpenGL does not have a direct equivalent to AWT's fill arc
    }

    @Override
    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {/*
        gl2.glBegin(GL2.GL_LINE_STRIP);
        for (int i = 0; i < nPoints; i++) {
            gl2.glVertex2i(xPoints[i], yPoints[i]);
        }
        gl2.glEnd();*/
    }

    @Override
    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {/*
        gl2.glBegin(GL2.GL_LINE_LOOP);
        for (int i = 0; i < nPoints; i++) {
            gl2.glVertex2i(xPoints[i], yPoints[i]);
        }
        gl2.glEnd();*/
    }

    @Override
    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {/*
        gl2.glBegin(GL2.GL_POLYGON);
        for (int i = 0; i < nPoints; i++) {
            gl2.glVertex2i(xPoints[i], yPoints[i]);
        }
        gl2.glEnd();*/
    }

    @Override
    public void draw(Shape s) {
        // OpenGL does not handle shapes directly
        if(s instanceof Line2D line) {
            if(paint instanceof GradientPaint gp) {
                var c1 = gp.getColor1();
                gl2.glColor3d(c1.getRed()/255.0, c1.getGreen()/255.0, c1.getBlue()/255.0);
                gl2.glVertex2d(line.getX1(), line.getY1());
                var c2 = gp.getColor2();
                gl2.glColor3d(c2.getRed()/255.0, c2.getGreen()/255.0, c2.getBlue()/255.0);
                gl2.glVertex2d(line.getX2(), line.getY2());
            } else {
                gl2.glVertex2d(line.getX1(), line.getY1());
                gl2.glVertex2d(line.getX2(), line.getY2());
            }
        }
    }

    @Override
    public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
        // OpenGL does not handle image rendering directly
        return false;
    }

    @Override
    public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
        // OpenGL does not handle image rendering directly
    }

    @Override
    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
        // OpenGL does not handle image rendering directly
    }

    @Override
    public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
        // OpenGL does not handle image rendering directly
    }

    @Override
    public void drawString(@NotNull String str, int x, int y) {
        // OpenGL does not handle text rendering directly
    }

    @Override
    public void drawString(String str, float x, float y) {
        // OpenGL does not handle text rendering directly
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator, int x, int y) {
        // OpenGL does not handle text rendering directly
    }

    @Override
    public void drawString(AttributedCharacterIterator iterator, float x, float y) {
        // OpenGL does not handle text rendering directly
    }

    @Override
    public void drawGlyphVector(GlyphVector g, float x, float y) {
        // OpenGL does not handle text rendering directly
    }

    @Override
    public void fill(Shape s) {
        // OpenGL does not handle shapes directly
    }

    @Override
    public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
        // OpenGL does not handle hit testing directly
        return false;
    }

    @Override
    public GraphicsConfiguration getDeviceConfiguration() {
        // OpenGL does not handle device configuration directly
        return null;
    }

    @Override
    public void setComposite(Composite comp) {
        // OpenGL does not handle Composite directly
    }

    @Override
    public void setPaint(Paint paint) {
        // OpenGL does not handle Paint directly
        this.paint = paint;
    }

    @Override
    public void setStroke(Stroke s) {
        // OpenGL does not handle Stroke directly
        if(s instanceof BasicStroke bs) {
            gl2.glLineWidth(bs.getLineWidth());
        }
    }

    @Override
    public void setRenderingHint(RenderingHints.Key hintKey, Object hintValue) {
        // OpenGL does not handle rendering hints directly
        if(hintKey == RenderingHints.KEY_ANTIALIASING) {
            if(hintValue == RenderingHints.VALUE_ANTIALIAS_ON) {
                gl2.glEnable(GL2.GL_LINE_SMOOTH);
            } else if(hintValue == RenderingHints.VALUE_ANTIALIAS_OFF) {
                gl2.glDisable(GL2.GL_LINE_SMOOTH);
            }
        }
    }

    @Override
    public Object getRenderingHint(RenderingHints.Key hintKey) {
        // OpenGL does not handle rendering hints directly
        return null;
    }

    @Override
    public void setRenderingHints(Map<?, ?> hints) {
        hints.forEach((a,b)->this.setRenderingHint((RenderingHints.Key) a,b));
    }

    @Override
    public void addRenderingHints(Map<?, ?> hints) {
        hints.forEach((a,b)->this.setRenderingHint((RenderingHints.Key) a,b));
    }

    @Override
    public RenderingHints getRenderingHints() {
        // OpenGL does not handle rendering hints directly
        return null;
    }

    @Override
    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        // OpenGL does not handle image rendering directly
        return false;
    }

    @Override
    public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
        // OpenGL does not handle image rendering directly
        return false;
    }

    @Override
    public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
        // OpenGL does not handle image rendering directly
        return false;
    }

    @Override
    public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer) {
        // OpenGL does not handle image rendering directly
        return false;
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
        // OpenGL does not handle image rendering directly
        return false;
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer) {
        // OpenGL does not handle image rendering directly
        return false;
    }

    @Override
    public void dispose() {
        // end drawing lines
        gl2.glEnd();
        // restore pen diameter
        gl2.glLineWidth(lineWidthBuf[0]);
        // restore the transformation matrix
        gl2.glPopMatrix();
    }
}