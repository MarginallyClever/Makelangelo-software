package com.marginallyclever.convenience.helpers;

import com.jogamp.opengl.GL3;
import com.marginallyclever.makelangelo.texture.TextureWithMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DrawingHelper {
    private static final Logger logger = LoggerFactory.getLogger(DrawingHelper.class);

    /**
     * Draw a circle
     * @param gl the render context
     * @param x x center coordinate
     * @param y y center coordinate
     * @param radius radius
     */
    public static void drawCircle(GL3 gl, float x, float y, float radius) {
        gl.glTranslatef(x, y, 0);
        gl.glBegin(GL3.GL_LINE_LOOP);
        for (float f = 0; f < 2.0 * Math.PI; f += 0.3f) {
            gl.glVertex2d(
                    Math.cos(f) * radius,
                    Math.sin(f) * radius);
        }
        gl.glEnd();
        gl.glTranslatef(-x, -y, 0);
    }

    /**
     * Draw an arc
     * @param gl the render context
     * @param x x center coordinate
     * @param y y center coordinate
     * @param radius radius
     * @param a1 start angle
     * @param a2 end angle
     */
    public static void drawArc(GL3 gl, float x, float y, float radius, float a1, float a2) {
        gl.glBegin(GL3.GL_LINE_STRIP);
        int steps = 10;
        float delta = (a2 - a1) / (float) steps;
        float f = a1;
        for (int i = 0; i <= steps; i++) {
            gl.glVertex2d(
                    x + Math.cos(f) * radius,
                    y + Math.sin(f) * radius);
            f += delta;
        }
        gl.glEnd();
    }

    /**
     * Draw a rectangle
     * @param gl the render context
     * @param top top coordinate
     * @param right right coordinate
     * @param bottom bottom coordinate
     * @param left left coordinate
     */
    public static void drawRectangle(GL3 gl, double top, double right, double bottom, double left) {
        gl.glBegin(GL3.GL_QUADS);
        gl.glVertex2d(left, top);
        gl.glVertex2d(right, top);
        gl.glVertex2d(right, bottom);
        gl.glVertex2d(left, bottom);
        gl.glEnd();
    }

    /**
     * Paint a quad with the given texture
     * @param gl the render context
     * @param x x center coordinate
     * @param y y center coordinate
     * @param width with of the texture
     * @param height height of the texture
     */
    public static void paintTexture(GL3 gl, TextureWithMetadata texture, double x, double y, double width, double height) {
        texture.use(gl);
        gl.glColor4d(1, 1, 1, 1);
        gl.glEnable(GL3.GL_TEXTURE_2D);
        gl.glBegin(GL3.GL_QUADS);
        gl.glTexCoord2d(0, 0);
        gl.glVertex2d(x, y);
        gl.glTexCoord2d(1, 0);
        gl.glVertex2d(x + width, y);
        gl.glTexCoord2d(1, 1);
        gl.glVertex2d(x + width, y + height);
        gl.glTexCoord2d(0, 1);
        gl.glVertex2d(x, y + height);
        gl.glEnd();
        gl.glDisable(GL3.GL_TEXTURE_2D);
    }
}
