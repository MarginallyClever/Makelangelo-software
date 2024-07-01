package com.marginallyclever.makelangelo.apps.previewpanel.turtlerenderer;

import com.marginallyclever.makelangelo.Mesh;
import com.marginallyclever.makelangelo.turtle.TurtleMove;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;
import java.awt.*;

/**
 * Helper class to convert lines into quads.
 */
public class Line2QuadHelper {    /**
     * Add a quad to the mesh that represents a line between two points.
     * @param mesh the mesh to add the quad to
     * @param p0 the start of the line
     * @param p1 the end of the line
     * @param c the color of the line
     * @param thickness the thickness of the line
     */
    public static void thicken(Mesh mesh, TurtleMove p0, TurtleMove p1, Color c, double thickness) {
        Vector3d p0v = new Vector3d(p0.x,p0.y,0);
        Vector3d p1v = new Vector3d(p1.x,p1.y,0);
        thicken(mesh,p0v,p1v,c,thickness);
    }

    /**
     * Add a quad to the mesh that represents a line between two points.
     * @param mesh the mesh to add the quad to
     * @param p0 the start of the line
     * @param p1 the end of the line
     * @param c the color of the line
     * @param thickness the thickness of the line
     */
    public static void thicken(Mesh mesh, Vector3d p0, Vector3d p1, Color c, double thickness) {
        thicken(mesh,p0,p1,c,c,thickness);
    }

    public static void thicken(Mesh mesh, Vector3d p0, Vector3d p1, Color c0, Color c1, double thickness) {
        float r0 = c0.getRed() / 255.0f;
        float g0 = c0.getGreen() / 255.0f;
        float b0 = c0.getBlue() / 255.0f;
        float a0 = c0.getAlpha() / 255.0f;

        float r1 = c1.getRed() / 255.0f;
        float g1 = c1.getGreen() / 255.0f;
        float b1 = c1.getBlue() / 255.0f;
        float a1 = c1.getAlpha() / 255.0f;

        // d is the line p0->p1 scaled to thickness/2
        Vector2d d = new Vector2d(p1.x-p0.x,p1.y-p0.y);
        d.normalize();
        d.scale(thickness/2f);
        // n is orthogonal to d
        Vector2d n = new Vector2d(-d.y,d.x);

        // our four points are the line grown in all directions by thickness/2
        Point2d p0d = new Point2d(p0.x-d.x+n.x,p0.y-d.y+n.y);
        Point2d p1d = new Point2d(p1.x+d.x+n.x,p1.y+d.y+n.y);
        Point2d p2d = new Point2d(p1.x+d.x-n.x,p1.y+d.y-n.y);
        Point2d p3d = new Point2d(p0.x-d.x-n.x,p0.y-d.y-n.y);

        mesh.addVertex((float)p0d.x, (float)p0d.y,0);
        mesh.addVertex((float)p1d.x, (float)p1d.y,0);
        mesh.addVertex((float)p2d.x, (float)p2d.y,0);

        mesh.addVertex((float)p2d.x, (float)p2d.y,0);
        mesh.addVertex((float)p3d.x, (float)p3d.y,0);
        mesh.addVertex((float)p0d.x, (float)p0d.y,0);

        for(int i=0;i<6;++i) {
            mesh.addNormal((float)p0.x, (float)p0.y, 0);
            mesh.addTexCoord((float)p1.x, (float)p1.y);
        }

        mesh.addColor(r0, g0, b0, a0);
        mesh.addColor(r1, g1, b1, a1);
        mesh.addColor(r1, g1, b1, a1);

        mesh.addColor(r1, g1, b1, a1);
        mesh.addColor(r0, g0, b0, a0);
        mesh.addColor(r0, g0, b0, a0);
    }
}
