package com.marginallyclever.makelangelo.plotter.plotterRenderer;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import com.marginallyclever.convenience.FileAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DrawingAssistant {
    private static final Logger logger = LoggerFactory.getLogger(DrawingAssistant.class);

    public static void drawCircle(GL2 gl2, float x, float y, float r) {
        gl2.glTranslatef(x, y, 0);
        gl2.glBegin(GL2.GL_LINE_LOOP);
        float f;
        for(f=0;f<2.0*Math.PI;f+=0.3f) {
            gl2.glVertex2d(
                    Math.cos(f)*r,
                    Math.sin(f)*r);
        }
        gl2.glEnd();
        gl2.glTranslatef(-x, -y, 0);
    }

    public static void drawArc(GL2 gl2,float x,float y,float r, float a1, float a2) {
        gl2.glTranslatef(x, y, 0);
        gl2.glBegin(GL2.GL_LINES);
        float f;
        int i;
        int steps=10;
        float delta=(a2-a1)/(float) steps;
        f=a1;
        for(i=0;i<steps;i++) {
            gl2.glVertex2d(	Math.cos(f)*r,Math.sin(f)*r);
            gl2.glVertex2d(	Math.cos(f+delta)*r,Math.sin(f+delta)*r);
            f += delta;
        }
        gl2.glEnd();
        gl2.glTranslatef(-x, -y, 0);
    }

    public static void drawRectangle(GL2 gl2, double top, double right, double bottom, double left) {
        gl2.glBegin(GL2.GL_QUADS);
        gl2.glVertex2d(left, top);
        gl2.glVertex2d(right, top);
        gl2.glVertex2d(right, bottom);
        gl2.glVertex2d(left, bottom);
        gl2.glEnd();
    }

    public static Texture loadTexture(String name) {
        Texture tex = null;
        try {
            tex = TextureIO.newTexture(FileAccess.open(name), false, name.substring(name.lastIndexOf('.') + 1));
        } catch (IOException e) {
            logger.warn("Can't load {}", name, e);
        }
        return tex;
    }
}
