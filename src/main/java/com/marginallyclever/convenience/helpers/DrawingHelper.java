package com.marginallyclever.convenience.helpers;

import com.jogamp.opengl.GL3;
import com.marginallyclever.makelangelo.Mesh;
import com.marginallyclever.makelangelo.preview.ShaderProgram;
import com.marginallyclever.makelangelo.texture.TextureWithMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

public class DrawingHelper {
    private static final Logger logger = LoggerFactory.getLogger(DrawingHelper.class);

    /**
     * Draw a circle
     * @param gl2 the render context
     * @param x x center coordinate
     * @param y y center coordinate
     * @param radius radius
     */
    public static void drawCircle(GL3 gl2, float x, float y, float radius, Color color) {
        Mesh mesh = new Mesh();
        mesh.setRenderStyle(GL3.GL_LINE_LOOP);
        for (float f = 0; f < 2.0 * Math.PI; f += 0.3f) {
            mesh.addColor(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, color.getAlpha()/255f);
            mesh.addVertex(
                    (float)(x + Math.cos(f) * radius),
                    (float)(y + Math.sin(f) * radius),
                    0);
        }
        mesh.render(gl2);
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
        Mesh mesh = new Mesh();
        mesh.setRenderStyle(GL3.GL_LINE_STRIP);
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
        mesh.render(gl);
    }

    /**
     * Draw a rectangle
     * @param gl the render context
     * @param top top coordinate
     * @param right right coordinate
     * @param bottom bottom coordinate
     * @param left left coordinate
     */
    public static void drawRectangle(GL3 gl, double top, double right, double bottom, double left, Color color) {
        Mesh mesh = new Mesh();
        mesh.setRenderStyle(GL3.GL_QUADS);
        mesh.addVertex((float)left, (float)top,0);
        mesh.addVertex((float)right, (float)top,0);
        mesh.addVertex((float)right, (float)bottom,0);
        mesh.addVertex((float)left, (float)bottom,0);

        for(int i=0;i<4;++i) {
            mesh.addColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        }

        mesh.render(gl);
    }

    /**
     * Paint a quad with the given texture
     * @param shader the render context
     * @param x x center coordinate
     * @param y y center coordinate
     * @param width with of the texture
     * @param height height of the texture
     */
    public static void paintTexture(ShaderProgram shader, GL3 gl, TextureWithMetadata texture, double x, double y, double width, double height) {
        texture.bind(gl);
        gl.glEnable(GL3.GL_TEXTURE_2D);
        shader.set1i(gl,"useTexture", 1);

        Mesh mesh = new Mesh();
        mesh.setRenderStyle(GL3.GL_QUADS);
        mesh.addColor(1, 1, 1, 1);  mesh.addTexCoord(0, 0);  mesh.addVertex((float)(x        ), (float)(y         ), 0);
        mesh.addColor(1, 1, 1, 1);  mesh.addTexCoord(1, 0);  mesh.addVertex((float)(x + width), (float)(y         ), 0);
        mesh.addColor(1, 1, 1, 1);  mesh.addTexCoord(1, 1);  mesh.addVertex((float)(x + width), (float)(y + height), 0);
        mesh.addColor(1, 1, 1, 1);  mesh.addTexCoord(0, 1);  mesh.addVertex((float)(x        ), (float)(y + height), 0);
        mesh.render(gl);

        gl.glDisable(GL3.GL_TEXTURE_2D);
        shader.set1i(gl,"useTexture", 0);
    }
}
