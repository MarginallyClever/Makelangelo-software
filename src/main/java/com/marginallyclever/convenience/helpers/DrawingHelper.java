package com.marginallyclever.convenience.helpers;

import com.jogamp.opengl.GL2;
import com.marginallyclever.makelangelo.texture.TextureWithMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DrawingHelper {
    private static final Logger logger = LoggerFactory.getLogger(DrawingHelper.class);

    /**
     * Draw a circle
     * @param gl2 the render context
     * @param x x center coordinate
     * @param y y center coordinate
     * @param radius radius
     */
    public static void drawCircle(GL2 gl2, float x, float y, float radius) {
        gl2.glTranslatef(x, y, 0);
        gl2.glBegin(GL2.GL_LINE_LOOP);
        for (float f = 0; f < 2.0 * Math.PI; f += 0.3f) {
            gl2.glVertex2d(
                    Math.cos(f) * radius,
                    Math.sin(f) * radius);
        }
        gl2.glEnd();
        gl2.glTranslatef(-x, -y, 0);
    }

    /**
     * Draw an arc
     * @param gl2 the render context
     * @param x x center coordinate
     * @param y y center coordinate
     * @param radius radius
     * @param a1 start angle
     * @param a2 end angle
     */
    public static void drawArc(GL2 gl2, float x, float y, float radius, float a1, float a2) {
        gl2.glTranslatef(x, y, 0);
        gl2.glBegin(GL2.GL_LINES);
        int steps = 10;
        float delta = (a2 - a1) / (float) steps;
        float f = a1;
        for (int i = 0; i < steps; i++) {
            gl2.glVertex2d(Math.cos(f) * radius, Math.sin(f) * radius);
            gl2.glVertex2d(Math.cos(f + delta) * radius, Math.sin(f + delta) * radius);
            f += delta;
        }
        gl2.glEnd();
        gl2.glTranslatef(-x, -y, 0);
    }

    /**
     * Draw a rectangle
     * @param gl2 the render context
     * @param top top coordinate
     * @param right right coordinate
     * @param bottom bottom coordinate
     * @param left left coordinate
     */
    public static void drawRectangle(GL2 gl2, double top, double right, double bottom, double left) {
        gl2.glBegin(GL2.GL_QUADS);
        gl2.glVertex2d(left, top);
        gl2.glVertex2d(right, top);
        gl2.glVertex2d(right, bottom);
        gl2.glVertex2d(left, bottom);
        gl2.glEnd();
    }

    /**
     * Paint a quad with the given texture
     * @param gl2 the render context
     * @param x x center coordinate
     * @param y y center coordinate
     * @param width with of the texture
     * @param height height of the texture
     */
    public static void paintTexture(GL2 gl2, TextureWithMetadata texture, double x, double y, double width, double height) {
        texture.bind(gl2);
        gl2.glColor4d(1, 1, 1, 1);
        gl2.glEnable(GL2.GL_TEXTURE_2D);
        gl2.glBegin(GL2.GL_QUADS);
        gl2.glTexCoord2d(0, 0);
        gl2.glVertex2d(x, y);
        gl2.glTexCoord2d(1, 0);
        gl2.glVertex2d(x + width, y);
        gl2.glTexCoord2d(1, 1);
        gl2.glVertex2d(x + width, y + height);
        gl2.glTexCoord2d(0, 1);
        gl2.glVertex2d(x, y + height);
        gl2.glEnd();
        gl2.glDisable(GL2.GL_TEXTURE_2D);
    }
}
