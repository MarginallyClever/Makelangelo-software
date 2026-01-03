package com.marginallyclever.convenience.helpers;

import com.marginallyclever.makelangelo.texture.TextureWithMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;

public class DrawingHelper {
    private static final Logger logger = LoggerFactory.getLogger(DrawingHelper.class);

    /**
     * Draw a circle
     * @param graphics the render context
     * @param x x center coordinate
     * @param y y center coordinate
     * @param radius radius
     */
    public static void drawCircle(Graphics graphics, float x, float y, float radius, Color color) {
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.setColor(color);
        g2d.drawOval((int)(x - radius), (int)(y - radius), (int)(radius * 2), (int)(radius * 2));
    }

    /**
     * Draw an arc
     * @param graphics the render context
     * @param x x center coordinate
     * @param y y center coordinate
     * @param radius radius
     * @param a1 start angle
     * @param a2 end angle
     */
    public static void drawArc(Graphics graphics, float x, float y, float radius, float a1, float a2) {
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.drawArc(
                (int)(x - radius),
                (int)(y - radius),
                (int)(radius * 2),
                (int)(radius * 2),
                (int)Math.toDegrees(a1),
                (int)Math.toDegrees(a2 - a1));/*
        int steps = 10;
        float delta = (a2 - a1) / (float) steps;
        float f = a1;
        for (int i = 0; i <= steps; i++) {
            mesh.addVertex(
                    (float)(x + Math.cos(f) * radius),
                    (float)(y + Math.sin(f) * radius),
                    0);
            f += delta;
        }
        mesh.render(graphics);*/
    }

    /**
     * Draw a rectangle
     * @param graphics the render context
     * @param top top coordinate
     * @param right right coordinate
     * @param bottom bottom coordinate
     * @param left left coordinate
     */
    public static void drawRectangle(Graphics graphics, double top, double right, double bottom, double left, Color color) {
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.setColor(color);
        g2d.fillRect(
                (int)left,
                (int)top,
                (int)Math.abs(right - left),
                (int)Math.abs(bottom - top));
    }

    /**
     * Paint a quad with the given texture
     * @param graphics the render context
     * @param x x center coordinate
     * @param y y center coordinate
     * @param width with of the texture
     * @param height height of the texture
     */
    public static void paintTexture(Graphics graphics, TextureWithMetadata texture, double x, double y, double width, double height) {
        Graphics2D g2d = (Graphics2D) graphics;
        BufferedImage img = texture.getTexture();
        g2d.drawImage(img,
                (int)x,
                (int)(y+height),
                (int)(x+width),
                (int)(y),
                0,
                0,
                img.getWidth(),
                img.getHeight(),
                null);
    }
}
