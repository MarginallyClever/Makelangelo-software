package com.marginallyclever.convenience;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.triangulate.VoronoiDiagramBuilder;

import java.awt.geom.Rectangle2D;

/**
 * VoronoiTesselator2 uses the locationtech.jts library to generate a voronoi diagram.
 * @author Dan
 * @since 2022-04-08
 */
public class VoronoiTesselator2 {
    private Geometry diagram;

    public VoronoiTesselator2(Point2D[] points, Rectangle2D bounds, double tolerance) {
        Coordinate[] coordinates = new Coordinate[points.length];
        for (int i = 0; i < points.length; i++) {
            coordinates[i] = new Coordinate(points[i].x,points[i].y);
        }
        GeometryFactory geometryFactory = new GeometryFactory();
        MultiPoint multiPoint = geometryFactory.createMultiPointFromCoords(coordinates);

        VoronoiDiagramBuilder builder = new VoronoiDiagramBuilder();
        builder.setSites(multiPoint);
        builder.setClipEnvelope(new Envelope(bounds.getMinX(),bounds.getMaxX(),bounds.getMinY(),bounds.getMaxY()));
        builder.setTolerance(tolerance);

        diagram = builder.getDiagram(multiPoint.getFactory());
    }

    public int getNumHulls() {
        return diagram.getNumGeometries();
    }

    public Polygon getHull(int i) {
        return (Polygon)diagram.getGeometryN(i);
    }
}
