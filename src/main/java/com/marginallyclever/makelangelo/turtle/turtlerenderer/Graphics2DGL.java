package com.marginallyclever.makelangelo.turtle.turtlerenderer;

import com.jogamp.opengl.GL3;
import com.marginallyclever.makelangelo.Mesh;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
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
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>A very limited {@link Graphics2D} implementation for OpenGL 3.0.</p>
 * <p>Assumes gl is already in orthographic projection and only drawing in the xy plane.</p>
 * <p>Don't forget to {@link #dispose()} when you're done.</p>
 */
public class Graphics2DGL extends Graphics2D {
    private static final Logger logger = LoggerFactory.getLogger(Graphics2DGL.class);

    private GL3 gl;
    private final float[] lineWidthBuf = new float[1];
    private Paint paint = null;
    private AtomicBoolean isDisposed = new AtomicBoolean(false);
    private Color currentColor = Color.BLACK;
    private final Mesh mesh = new Mesh();
    private final Matrix4d matrix = new Matrix4d();
    private double thetaSum = 0;

    public Graphics2DGL() {}

    public void renderBegin(GL3 gl2) {
        this.gl = gl2;

        // save the line width
        gl2.glGetFloatv(GL3.GL_LINE_WIDTH, lineWidthBuf, 0);

        // start drawing lines
        mesh.clear();
        mesh.setRenderStyle(GL3.GL_LINES);

        matrix.setIdentity();
        thetaSum=0;
    }

    public void renderFinish() {
        // end drawing lines
        mesh.render(gl);
        // restore pen diameter
        gl.glLineWidth(lineWidthBuf[0]);
    }

    @Override
    public Graphics create() {
        return new Graphics2DGL();
    }

    @Override
    public void translate(int x, int y) {
        matrix.m03+=x;
        matrix.m13+=y;
    }

    @Override
    public void translate(double x, double y) {
        matrix.m03+=x;
        matrix.m13+=y;
    }

    @Override
    public void rotate(double theta) {
        thetaSum += theta;
        double x = matrix.m03;
        double y = matrix.m13;
        Matrix3d m3 = new Matrix3d();
        m3.rotZ(thetaSum);
        matrix.set(m3);
        matrix.m03 = x; // restore translation
        matrix.m13 = y; // restore translation
    }

    @Override
    public void rotate(double theta, double x, double y) {
        translate(x, y);
        rotate(theta);
        translate(-x, -y);
    }

    @Override
    public void scale(double sx, double sy) {
        matrix.m00= sx;
        matrix.m11= sy;
    }

    @Override
    public void shear(double shx, double shy) {
        matrix.m10= shx;
        matrix.m01= shy;
    }

    @Override
    public void transform(AffineTransform Tx) {
        Matrix4d m2 = new Matrix4d();
        double[] flatMatrix = new double[6];
        Tx.getMatrix(flatMatrix);
        m2.m00 = flatMatrix[0];
        m2.m01 = flatMatrix[1];
        m2.m10 = flatMatrix[2];
        m2.m11 = flatMatrix[3];
        m2.m30 = flatMatrix[4];
        m2.m31 = flatMatrix[5];
        m2.m22 = 1;
        m2.m33 = 1;
        matrix.mul(m2);
    }

    @Override
    public void setTransform(AffineTransform Tx) {
        matrix.setIdentity();
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
        gl.glClearColor(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
    }

    @Override
    public Color getBackground() {
        float[] color = new float[4];
        gl.glGetFloatv(GL3.GL_COLOR_CLEAR_VALUE, color, 0);
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
        return new Color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), currentColor.getAlpha());
    }

    @Override
    public void setColor(Color c) {
        paint = null;
        currentColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
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
        gl.glScissor(x, y, width, height);
        gl.glEnable(GL3.GL_SCISSOR_TEST);
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
            mesh.addColor(c1.getRed()/255.0f, c1.getGreen()/255.0f, c1.getBlue()/255.0f,1);
            addVertex(x1, y1);
            var c2 = gp.getColor2();
            mesh.addColor(c2.getRed()/255.0f, c2.getGreen()/255.0f, c2.getBlue()/255.0f,1);
            addVertex(x2, y2);
        } else {
            mesh.addColor(currentColor.getRed()/255.0f, currentColor.getGreen()/255.0f, currentColor.getBlue()/255.0f, currentColor.getAlpha()/255.0f);
            addVertex(x1, y1);
            mesh.addColor(currentColor.getRed()/255.0f, currentColor.getGreen()/255.0f, currentColor.getBlue()/255.0f, currentColor.getAlpha()/255.0f);
            addVertex(x2, y2);
        }
    }

    private void addVertex(float x,float y) {
        //Point3d p = new Point3d(x, y, 0);
        //matrix.transform(p);
        //mesh.addVertex((float)p.x, (float)p.y, 0);
        mesh.addVertex(x,y,0);
    }

    @Override
    public void fillRect(int x, int y, int width, int height) {
        /*
        gl2.glBegin(GL3.GL_QUADS);
        gl2.glVertex2i(x, y);
        gl2.glVertex2i(x + width, y);
        gl2.glVertex2i(x + width, y + height);
        gl2.glVertex2i(x, y + height);
        gl2.glEnd();*/
    }

    @Override
    public void clearRect(int x, int y, int width, int height) {
        gl.glClear(GL3.GL_COLOR_BUFFER_BIT);
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
        gl2.glBegin(GL3.GL_LINE_STRIP);
        for (int i = 0; i < nPoints; i++) {
            gl2.glVertex2i(xPoints[i], yPoints[i]);
        }
        gl2.glEnd();*/
    }

    @Override
    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {/*
        gl2.glBegin(GL3.GL_LINE_LOOP);
        for (int i = 0; i < nPoints; i++) {
            gl2.glVertex2i(xPoints[i], yPoints[i]);
        }
        gl2.glEnd();*/
    }

    @Override
    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {/*
        gl2.glBegin(GL3.GL_POLYGON);
        for (int i = 0; i < nPoints; i++) {
            gl2.glVertex2i(xPoints[i], yPoints[i]);
        }
        gl2.glEnd();*/
    }

    @Override
    public void draw(Shape s) {
        // OpenGL does not handle shapes directly
        if(s instanceof Line2D line) {
            drawLine(line);
        } else {
            throw new RuntimeException("Unsupported shape type: " + s.getClass().getName());
        }
    }

    private void drawLine(Line2D line) {
        if (paint instanceof GradientPaint gp) {
            var c1 = gp.getColor1();
            mesh.addColor(c1.getRed() / 255.0f, c1.getGreen() / 255.0f, c1.getBlue() / 255.0f, 1);
            addVertex((float) line.getX1(), (float) line.getY1());
            var c2 = gp.getColor2();
            mesh.addColor(c2.getRed() / 255.0f, c2.getGreen() / 255.0f, c2.getBlue() / 255.0f, 1);
            addVertex((float) line.getX2(), (float) line.getY2());
        } else {
            mesh.addColor(currentColor.getRed() / 255.0f, currentColor.getGreen() / 255.0f, currentColor.getBlue() / 255.0f, currentColor.getAlpha() / 255.0f);
            addVertex((float) line.getX1(), (float) line.getY1());
            mesh.addColor(currentColor.getRed() / 255.0f, currentColor.getGreen() / 255.0f, currentColor.getBlue() / 255.0f, currentColor.getAlpha() / 255.0f);
            addVertex((float) line.getX2(), (float) line.getY2());
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
            gl.glLineWidth(bs.getLineWidth());
        }
    }

    @Override
    public void setRenderingHint(RenderingHints.Key hintKey, Object hintValue) {
        // OpenGL does not handle rendering hints directly
        if(hintKey == RenderingHints.KEY_ANTIALIASING) {
            if(hintValue == RenderingHints.VALUE_ANTIALIAS_ON) {
                gl.glEnable(GL3.GL_LINE_SMOOTH);
            } else if(hintValue == RenderingHints.VALUE_ANTIALIAS_OFF) {
                gl.glDisable(GL3.GL_LINE_SMOOTH);
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
        if(isDisposed.getAndSet(true)) return;  // already disposed.
    }

    public void setContext(GL3 context) {
        gl = context;
    }
}